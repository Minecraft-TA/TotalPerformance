package com.github.minecraft_ta.totalperformance.dimThreading;

import net.minecraft.crash.CrashReport;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Phaser;

public class WorldRunnable implements Runnable {

    private final WorldServer world;
    private final Phaser phaser;
    private final Logger logger;
    private Thread thread;

    private CrashReport crashReport;

    private long lastTickTime;
    private boolean stop;

    public WorldRunnable(WorldServer world, Phaser phaser, Logger logger) {
        this.world = world;
        this.phaser = phaser;
        this.logger = logger;
        this.phaser.register();
    }

    @Override
    public void run() {
        try {
            while (!this.stop) {
                //Tick start barrier
                this.phaser.arriveAndAwaitAdvance();

                this.lastTickTime = System.nanoTime();

                //Pre world tick event
                FMLCommonHandler.instance().onPreWorldTick(this.world);

                this.lastTickTime = System.nanoTime() - this.lastTickTime;

                //Post world tick barrier
                this.phaser.arriveAndAwaitAdvance();

                long time = System.nanoTime();

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

                this.lastTickTime += System.nanoTime() - time;

                //Post world tick barrier
                this.phaser.arriveAndAwaitAdvance();

                time = System.nanoTime();

                //Post world tick event
                FMLCommonHandler.instance().onPostWorldTick(this.world);
                //Update entity tracker
                this.world.getEntityTracker().tick();

                this.lastTickTime += System.nanoTime() - time;

                //Tick end barrier
                this.phaser.arriveAndAwaitAdvance();
            }
        } catch (Throwable t) {
            this.crashReport = CrashReport.makeCrashReport(t, "Uncaught exception in world");
            this.world.addWorldInfoToCrashReport(this.crashReport);
        } finally {
            this.phaser.arriveAndDeregister();
            ((IConcurrentWorldServer) this.world).forceUnlockAll();

            this.logger.info("Stopped thread for world {}", this.world.provider.getDimension());
        }
    }

    public void stopThread() {
        this.logger.info("Queued thread stop for world {}", this.world.provider.getDimension());
        this.stop = true;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
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

    public void interruptAndStopThread() {
        this.logger.info("Interrupt and stop thread {}", this.world.provider.getDimension());
        this.thread.interrupt();
        this.stop = true;
    }
}
