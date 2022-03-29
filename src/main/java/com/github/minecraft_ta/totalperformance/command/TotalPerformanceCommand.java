package com.github.minecraft_ta.totalperformance.command;

import com.github.minecraft_ta.totalperformance.TotalPerformance;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class TotalPerformanceCommand extends CommandBase {

    @Override
    public String getName() {
        return "totalperformance";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            return;
        }

        if (args[0].equals("autoSaveInterval")) {
            if (args.length > 1) {
                try {
                    TotalPerformance.CONFIG.autoSaveInterval = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    throw new CommandException("Invalid number: " + args[1]);
                }
            }
        } else if (args[0].equals("loadSpawnChunks")) {
            if (args.length > 1) {
                TotalPerformance.CONFIG.loadSpawnChunks = Boolean.parseBoolean(args[1]);
            }
        }
        TotalPerformance.CONFIG.getConfig().save();
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "autoSaveInterval", "loadSpawnChunks");
        } else if (args.length == 2 && args[0].equals("loadSpawnChunks")) {
            return getListOfStringsMatchingLastWord(args, "true", "false");
        }
        return Collections.emptyList();
    }
}
