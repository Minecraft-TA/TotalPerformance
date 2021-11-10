package com.github.minecraft_ta.totalperformance.dimThreading;

import net.minecraft.entity.Entity;

public interface IWorldAcceptsTeleport {

    void queueEntityForTeleport(Entity entity);
}
