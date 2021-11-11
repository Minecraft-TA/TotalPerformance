package com.github.minecraft_ta.totalperformance.mixin.dimThreading;

import com.github.minecraft_ta.totalperformance.dimThreading.WorldRunnable;
import com.mojang.authlib.GameProfile;
import net.minecraft.advancements.FunctionManager;
import net.minecraft.network.NetworkSystem;
import net.minecraft.network.ServerStatusResponse;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.chunkio.ChunkIOExecutor;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Phaser;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Shadow
    public WorldServer[] worlds;
    private final List<WorldRunnable> worldRunnables = new ArrayList<>();
    private final List<WorldServer> queuedWorldRunnables = new ArrayList<>();

    private final Phaser phaser = new Phaser();

    @Inject(method = "loadAllWorlds", at = @At("RETURN"))
    public void initThreads(CallbackInfo ci) {
        this.phaser.register();

        for (WorldServer world : this.worlds) {
            createAndStartThread(world);
        }

        MinecraftForge.EVENT_BUS.register(new Object() {
            @SubscribeEvent
            public void worldUnload(WorldEvent.Unload e) {
                worldRunnables.removeIf(w -> {
                    if (w.getWorld() == e.getWorld()) {
                        LOGGER.info("Stopped thread for world {}", e.getWorld().provider.getDimension());
                        w.stopThread();
                        return true;
                    }

                    return false;
                });
            }

            @SubscribeEvent
            public void worldLoad(WorldEvent.Load e) {
                queuedWorldRunnables.add((WorldServer) e.getWorld());
                LOGGER.info("Added world to thread start queue {}", e.getWorld().provider.getDimension());
            }
        });
    }

    /**
     * @author tth05
     */
    @Overwrite
    public void tick() {
        long i = System.nanoTime();
        FMLCommonHandler.instance().onPreServerTick();
        ++this.tickCounter;

        this.updateTimeLightAndEntities();

        if (i - this.nanoTimeSinceStatusRefresh >= 5000000000L) {
            this.nanoTimeSinceStatusRefresh = i;
            this.statusResponse.setPlayers(new ServerStatusResponse.Players(this.getMaxPlayers(), this.getCurrentPlayerCount()));
            GameProfile[] agameprofile = new GameProfile[Math.min(this.getCurrentPlayerCount(), 12)];
            int j = MathHelper.getInt(this.random, 0, this.getCurrentPlayerCount() - agameprofile.length);

            for (int k = 0; k < agameprofile.length; ++k) {
                agameprofile[k] = this.playerList.getPlayers().get(j + k).getGameProfile();
            }

            Collections.shuffle(Arrays.asList(agameprofile));
            this.statusResponse.getPlayers().setPlayers(agameprofile);
            this.statusResponse.invalidateJson();
        }

        if (this.tickCounter % 900 == 0) {
            this.playerList.saveAllPlayerData();
            this.saveAllWorlds(true);
        }

        this.tickTimeArray[this.tickCounter % 100] = System.nanoTime() - i;
        FMLCommonHandler.instance().onPostServerTick();
    }

    /**
     * @author tth05
     */
    @Overwrite
    public void updateTimeLightAndEntities() {
        //noinspection SynchronizeOnNonFinalField
        synchronized (this.futureTaskQueue) {
            while (!this.futureTaskQueue.isEmpty()) {
                Util.runTask(this.futureTaskQueue.poll(), LOGGER);
            }
        }

        //Tick chunk io
        ChunkIOExecutor.tick();

        //TODO: Call getIDs to check for leaked worlds...?
        DimensionManager.getIDs(this.tickCounter % 200 == 0);

        //Update dimension time
        if (this.tickCounter % 20 == 0) {
            for (WorldRunnable worldRunnable : this.worldRunnables) {
                WorldServer world = worldRunnable.getWorld();
                this.playerList.sendPacketToAllPlayersInDimension(new SPacketTimeUpdate(world.getTotalWorldTime(), world.getWorldTime(), world.getGameRules().getBoolean("doDaylightCycle")), world.provider.getDimension());
            }
        }

        //Tick start barrier
        this.phaser.arriveAndAwaitAdvance();
        //Tick end barrier
        this.phaser.arriveAndAwaitAdvance();

        //Check for crashes
        for (WorldRunnable worldRunnable : this.worldRunnables) {
            if (!worldRunnable.hasCrashed())
                continue;
            throw new ReportedException(worldRunnable.getCrashReport());
        }

        //Update world tick times
        for (WorldRunnable worldRunnable : this.worldRunnables) {
            this.worldTickTimes.get(worldRunnable.getWorld().provider.getDimension())[this.tickCounter % 100] = worldRunnable.getLastTickTime();
        }

        //Start threads of newly loaded worlds
        for (Iterator<WorldServer> iterator = this.queuedWorldRunnables.iterator(); iterator.hasNext(); ) {
            WorldServer world = iterator.next();
            createAndStartThread(world);
            iterator.remove();
        }

        DimensionManager.unloadWorlds(worldTickTimes);
        this.getNetworkSystem().networkTick();
        this.playerList.onTick();
        this.getFunctionManager().update();
    }

    public void createAndStartThread(WorldServer world) {
        WorldRunnable worldRunnable = new WorldRunnable(world, this.phaser);
        Thread worldThread = new Thread(worldRunnable);
        worldThread.setName("TP Dim Thread - " + world.provider.getDimension());
        worldThread.setDaemon(true);
        worldThread.start();

        LOGGER.info("Started thread for world {}", world.provider.getDimension());

        this.worldRunnables.add(worldRunnable);
    }

    @Shadow
    private int tickCounter;

    @Shadow
    private long nanoTimeSinceStatusRefresh;
    @Shadow
    @Final
    private ServerStatusResponse statusResponse;

    @Shadow
    public abstract int getMaxPlayers();

    @Shadow
    public abstract int getCurrentPlayerCount();

    @Shadow
    @Final
    private Random random;
    @Shadow
    private PlayerList playerList;

    @Shadow
    public abstract void saveAllWorlds(boolean isSilent);

    @Shadow
    @Final
    public long[] tickTimeArray;

    @Shadow
    @Final
    public Queue<FutureTask<?>> futureTaskQueue;
    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    public Hashtable<Integer, long[]> worldTickTimes;

    @Shadow
    public abstract NetworkSystem getNetworkSystem();

    @Shadow
    public abstract FunctionManager getFunctionManager();
}
