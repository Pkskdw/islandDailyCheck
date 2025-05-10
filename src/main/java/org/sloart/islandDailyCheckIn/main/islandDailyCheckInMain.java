package org.sloart.islandDailyCheckIn.main;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.sloart.islandDailyCheckIn.DataBase.islandDailyCheckInDB;
import org.sloart.islandDailyCheckIn.DataBase.islandDailyCheckInDBAsync;
import org.sloart.islandDailyCheckIn.command.islandDailyCheckInCommandManager;
import org.sloart.islandDailyCheckIn.command.islandDailyCheckInTabCompleter;
import org.sloart.islandDailyCheckIn.config.islandDailyCheckInConfig;
import org.sloart.islandDailyCheckIn.gui.islandDailyCheckInGui;
import org.sloart.islandDailyCheckIn.gui.islandDailyCheckInGuiListener;
import org.sloart.islandDailyCheckIn.handler.islandDailyCheckInHandler;
import org.sloart.islandDailyCheckIn.listener.islandDailyCheckInListener;
import org.sloart.islandDailyCheckIn.command.islandDailyCheckInCommand;

import java.util.Objects;

public final class islandDailyCheckInMain extends JavaPlugin implements Listener {
    private islandDailyCheckInConfig islandDailyCheckInConfig;
    private islandDailyCheckInDB islandDailyCheckInDB;
    private islandDailyCheckInHandler islandDailyCheckInHandler;
    private islandDailyCheckInListener islandDailyCheckInListener;
    private islandDailyCheckInDBAsync islandDailyCheckInDBAsync;
    private islandDailyCheckInGui islandDailyCheckInGui;
    private islandDailyCheckInGuiListener islandDailyCheckInGuiListener;
    private islandDailyCheckInCommandManager islandDailyCheckInCommandManager;
    private islandDailyCheckInCommand islandDailyCheckInCommand;
    private islandDailyCheckInTabCompleter islandDailyCheckInTabCompleter;

    @Override
    public void onEnable() {
        reset();
        listener();
        command();
    }

    @Override
    public void onDisable() {
        disable();
    }

    private void reset() {
        islandDailyCheckInConfig = new islandDailyCheckInConfig(this);

        islandDailyCheckInDB = new islandDailyCheckInDB(this);
        islandDailyCheckInDBAsync = new islandDailyCheckInDBAsync(islandDailyCheckInDB, this);

        islandDailyCheckInListener = new islandDailyCheckInListener(islandDailyCheckInDBAsync, islandDailyCheckInConfig);

        islandDailyCheckInGui = new islandDailyCheckInGui(islandDailyCheckInConfig);

        islandDailyCheckInHandler = new islandDailyCheckInHandler(islandDailyCheckInConfig, islandDailyCheckInGui, islandDailyCheckInDBAsync);

        islandDailyCheckInGuiListener = new islandDailyCheckInGuiListener(islandDailyCheckInConfig, islandDailyCheckInHandler, this);

        islandDailyCheckInCommand = new islandDailyCheckInCommand(islandDailyCheckInDBAsync, islandDailyCheckInGui);
        islandDailyCheckInCommandManager = new islandDailyCheckInCommandManager(islandDailyCheckInGui, islandDailyCheckInDBAsync, islandDailyCheckInConfig);
        islandDailyCheckInTabCompleter = new islandDailyCheckInTabCompleter(islandDailyCheckInConfig);

    }

    private void listener() {
        getServer().getPluginManager().registerEvents(islandDailyCheckInListener, this);
        getServer().getPluginManager().registerEvents(islandDailyCheckInGuiListener, this);
    }

    private void command() {
        Objects.requireNonNull(getCommand("dailyreward")).setExecutor(islandDailyCheckInCommand);
        Objects.requireNonNull(getCommand("dailysetreward")).setExecutor(islandDailyCheckInCommandManager);
        Objects.requireNonNull(getCommand("dailysetreward")).setTabCompleter(islandDailyCheckInTabCompleter);


    }

    private void disable() {
        islandDailyCheckInDB.disconnect();
    }
}
