package com.github.minecraft_ta.totalperformance.dimThreading;

import net.minecraft.world.WorldServer;

public interface ITeleportableEntity {

    void finishTeleport(WorldServer newWorld);
}
