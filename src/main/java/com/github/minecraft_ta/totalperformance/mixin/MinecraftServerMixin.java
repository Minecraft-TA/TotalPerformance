package com.github.minecraft_ta.totalperformance.mixin;

import net.minecraft.crash.CrashReport;
import net.minecraft.network.ServerStatusResponse;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ReportedException;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.StartupQuery;
import net.openhft.affinity.AffinityLock;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {


    @Shadow public abstract boolean init() throws IOException;

    @Shadow protected long currentTime;

    @Shadow
    public static long getCurrentTimeMillis() {
        return 0;
    }

    @Shadow @Final private ServerStatusResponse statusResponse;
    @Shadow private String motd;

    @Shadow public abstract void applyServerIconToResponse(ServerStatusResponse response);

    @Shadow private boolean serverRunning;
    @Shadow private long timeOfLastWarning;
    @Shadow @Final private static Logger LOGGER;
    @Shadow public WorldServer[] worlds;

    @Shadow public abstract void tick();

    @Shadow private boolean serverIsRunning;

    @Shadow public abstract void finalTick(CrashReport report);

    @Shadow public abstract CrashReport addServerInfoToCrashReport(CrashReport report);

    @Shadow public abstract File getDataDirectory();

    @Shadow public abstract void stopServer();

    @Shadow private boolean serverStopped;

    @Shadow public abstract void systemExitNow();

    /**
     * @author fortnite
     */
    @Overwrite
    public void run() {
        try (AffinityLock affinityLock = AffinityLock.acquireCore()){
            System.out.println(AffinityLock.dumpLocks());
            if (this.init()) {
                FMLCommonHandler.instance().handleServerStarted();
                this.currentTime = getCurrentTimeMillis();
                long i = 0L;
                this.statusResponse.setServerDescription(new TextComponentString(this.motd));
                this.statusResponse.setVersion(new ServerStatusResponse.Version("1.12.2", 340));
                this.applyServerIconToResponse(this.statusResponse);

                while (this.serverRunning) {
                    long k = getCurrentTimeMillis();
                    long j = k - this.currentTime;

                    if (j > 2000L && this.currentTime - this.timeOfLastWarning >= 15000L) {
                        LOGGER.warn("Can't keep up! Did the system time change, or is the server overloaded? Running {}ms behind, skipping {} tick(s)", Long.valueOf(j), Long.valueOf(j / 50L));
                        j = 2000L;
                        this.timeOfLastWarning = this.currentTime;
                    }

                    if (j < 0L) {
                        LOGGER.warn("Time ran backwards! Did the system time change?");
                        j = 0L;
                    }

                    i += j;
                    this.currentTime = k;

                    if (this.worlds[0].areAllPlayersAsleep()) {
                        this.tick();
                        i = 0L;
                    } else {
                        while (i > 50L) {
                            i -= 50L;
                            this.tick();
                        }
                    }

                    Thread.sleep(Math.max(1L, 50L - i));
                    this.serverIsRunning = true;
                }
                FMLCommonHandler.instance().handleServerStopping();
                FMLCommonHandler.instance().expectServerStopped(); // has to come before finalTick to avoid race conditions
            } else {
                FMLCommonHandler.instance().expectServerStopped(); // has to come before finalTick to avoid race conditions
                this.finalTick((CrashReport) null);
            }
        } catch (StartupQuery.AbortedException e) {
            // ignore silently
            FMLCommonHandler.instance().expectServerStopped(); // has to come before finalTick to avoid race conditions
        } catch (Throwable throwable1) {
            LOGGER.error("Encountered an unexpected exception", throwable1);
            CrashReport crashreport = null;

            if (throwable1 instanceof ReportedException) {
                crashreport = this.addServerInfoToCrashReport(((ReportedException) throwable1).getCrashReport());
            } else {
                crashreport = this.addServerInfoToCrashReport(new CrashReport("Exception in server tick loop", throwable1));
            }

            File file1 = new File(new File(this.getDataDirectory(), "crash-reports"), "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-server.txt");

            if (crashreport.saveToFile(file1)) {
                LOGGER.error("This crash report has been saved to: {}", (Object) file1.getAbsolutePath());
            } else {
                LOGGER.error("We were unable to save this crash report to disk.");
            }

            FMLCommonHandler.instance().expectServerStopped(); // has to come before finalTick to avoid race conditions
            this.finalTick(crashreport);
        } finally {
            try {
                this.stopServer();
            } catch (Throwable throwable) {
                LOGGER.error("Exception stopping the server", throwable);
            } finally {
                FMLCommonHandler.instance().handleServerStopped();
                this.serverStopped = true;
                this.systemExitNow();
            }
        }
    }
}
