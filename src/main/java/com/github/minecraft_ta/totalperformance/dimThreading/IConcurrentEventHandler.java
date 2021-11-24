package com.github.minecraft_ta.totalperformance.dimThreading;

import java.util.concurrent.locks.ReentrantLock;

public interface IConcurrentEventHandler {

    ReentrantLock getEventHandlerLock();

    Class<?> getTargetClass();

}
