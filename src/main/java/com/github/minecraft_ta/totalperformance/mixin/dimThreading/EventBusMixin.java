package com.github.minecraft_ta.totalperformance.mixin.dimThreading;

import com.github.minecraft_ta.totalperformance.dimThreading.ForwardingEventHandlerInvocationException;
import com.github.minecraft_ta.totalperformance.dimThreading.IConcurrentEventHandler;
import com.google.common.base.Throwables;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

/**
 * This mixin prevents events from running at the same time to avoid concurrency issues in mod code (for example if two
 * post world tick handler from the same mod run at the same time).
 */
@Mixin(EventBus.class)
public abstract class EventBusMixin {

    @Shadow(remap = false)
    private boolean shutdown;
    @Shadow(remap = false)
    @Final
    private int busID;
    @Shadow(remap = false)
    private IEventExceptionHandler exceptionHandler;

    /**
     * @author tth05
     */
    @Overwrite(remap = false)
    public boolean post(Event event) {
        if (shutdown) return false;

        IEventListener[] listeners = event.getListenerList().getListeners(busID);

        //Run old code for these events
        if (!(event instanceof TickEvent.WorldTickEvent)) {
            int index = 0;
            try {
                for (; index < listeners.length; index++) {
                    listeners[index].invoke(event);
                }
            } catch (Throwable throwable) {
                handleException(event, listeners, index, throwable);
            }
        } else if (listeners.length != 0) {
            IntList indices = null;
            int index = 0;
            try {
                for (; index < listeners.length; index++) {
                    IEventListener listener = listeners[index];

                    //Before we move to the next priority, we should invoke all pending handlers
                    if (listener instanceof EventPriority) {
                        runAllMissingHandlers(indices, listeners, event);
                        continue;
                    }

                    IConcurrentEventHandler concurrentHandler = (IConcurrentEventHandler) listener;
                    //The timeout just saves some CPU if all locks are already locked.
                    if (!concurrentHandler.getEventHandlerLock().tryLock(1, TimeUnit.MILLISECONDS)) {
                        if (indices == null)
                            indices = new IntArrayList();
                        indices.add(index);
                        continue;
                    }

                    listeners[index].invoke(event);
                    concurrentHandler.getEventHandlerLock().unlock();
                }

                //Run again to clean up
                runAllMissingHandlers(indices, listeners, event);
            } catch (ForwardingEventHandlerInvocationException e) {
                handleException(event, listeners, e.getListenerIndex(), e.getCause());
            } catch (Throwable throwable) {
                handleException(event, listeners, index, throwable);
            }
        }
        return event.isCancelable() && event.isCanceled();
    }

    private void handleException(Event event, IEventListener[] listeners, int index, Throwable throwable) {
        exceptionHandler.handleException((EventBus) (Object) this, event, listeners, index, throwable);
        Throwables.throwIfUnchecked(throwable);
        throw new RuntimeException(throwable);
    }

    private void runAllMissingHandlers(@Nullable IntList indices, IEventListener[] listeners, Event event) throws ForwardingEventHandlerInvocationException {
        if (indices == null || indices.isEmpty())
            return;

        int index = 0;

        try {
            while (!indices.isEmpty()) {
                if (index >= indices.size())
                    index = 0;

                IConcurrentEventHandler concurrentHandler = (IConcurrentEventHandler) listeners[indices.get(index++)];
                //The timeout just saves some CPU if all locks are already locked.
                if (!concurrentHandler.getEventHandlerLock().tryLock(1, TimeUnit.MILLISECONDS)) {
                    continue;
                }

                ((IEventListener) concurrentHandler).invoke(event);

                indices.remove(index - 1);
                concurrentHandler.getEventHandlerLock().unlock();
            }
        } catch (Throwable t) {
            throw new ForwardingEventHandlerInvocationException(t, index - 1);
        }
    }
}
