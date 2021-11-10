package com.github.minecraft_ta.totalperformance.dimThreading;

import net.minecraft.crash.CrashReport;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.concurrent.Phaser;

public class WorldRunnable implements Runnable {

    private final WorldServer world;
    private final Phaser phaser;

    private CrashReport crashReport;

    private long lastTickTime;
    private boolean stop;

    public WorldRunnable(WorldServer world, Phaser phaser) {
        this.world = world;
        this.phaser = phaser;
        this.phaser.register();
    }

    @Override
    public void run() {
        while (!this.stop) {
            //Tick start barrier
            this.phaser.arriveAndAwaitAdvance();

            this.lastTickTime = System.nanoTime();

            //Pre world tick event
            FMLCommonHandler.instance().onPreWorldTick(this.world);

            //Tick world and entities
            try {
                this.world.tick();
            } catch (Throwable throwable1) {
                this.crashReport = CrashReport.makeCrashReport(throwable1, "Exception ticking world");
                this.world.addWorldInfoToCrashReport(this.crashReport);
                break;
            }

            try {
                this.world.updateEntities();
            } catch (Throwable throwable) {
                this.crashReport = CrashReport.makeCrashReport(throwable, "Exception ticking world entities");
                this.world.addWorldInfoToCrashReport(this.crashReport);
                break;
            }

            //Post world tick event
            FMLCommonHandler.instance().onPostWorldTick(this.world);
            //Update entity tracker
            this.world.getEntityTracker().tick();

            this.lastTickTime = System.nanoTime() - this.lastTickTime;

            //Tick end barrier
            this.phaser.arriveAndAwaitAdvance();
        }

        this.phaser.arriveAndDeregister();
    }

    public void stopThread() {
        this.stop = true;
    }

    public long getLastTickTime() {
        return this.lastTickTime;
    }

    public WorldServer getWorld() {
        return this.world;
    }

    public boolean hasCrashed() {
        return this.crashReport != null;
    }

    public CrashReport getCrashReport() {
        return crashReport;
    }
}
