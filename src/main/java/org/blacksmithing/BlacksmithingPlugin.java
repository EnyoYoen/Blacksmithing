package org.blacksmithing;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class BlacksmithingPlugin extends JavaPlugin {
    public static BlacksmithingPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        Bukkit.getConsoleSender().sendMessage("§2[Blacksmithing] §rLoading...");
        BSListener listener = new BSListener();
        listener.loadRecipes();
        getServer().getPluginManager().registerEvents(listener, this);
        Bukkit.getConsoleSender().sendMessage("§2[Blacksmithing] §rLoading finished!");
    }
}
