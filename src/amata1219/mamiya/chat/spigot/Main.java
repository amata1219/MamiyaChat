package amata1219.mamiya.chat.spigot;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

	private static Main plugin;

	private DynmapBridge bridge;

	@Override
	public void onEnable(){
		plugin = this;

		PluginManager manager = getServer().getPluginManager();
		if(manager.isPluginEnabled("dynmap"))
			bridge = DynmapBridge.newInstance(manager.getPlugin("dynmap"));
	}

	@Override
	public void onDisable(){
		HandlerList.unregisterAll((JavaPlugin) this);

		if(bridge != null)
			bridge.unload();
	}

	public static Main getPlugin(){
		return plugin;
	}

}
