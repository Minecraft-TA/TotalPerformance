package com.github.minecraft_ta.totalperformance.dimThreading;

import java.util.concurrent.locks.ReentrantLock;

public interface IThreadedWorldServer {

    ReentrantLock getEntityTrackerLock();

    ReentrantLock getChunkLock();

    void forceUnlockAll();
}
