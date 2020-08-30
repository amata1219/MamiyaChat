package amata1219.mamiya.chat.spigot;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

	private static Main plugin;

	private DynmapBridge bridge;

	@Override
	public void onEnable(){
		plugin = this;

		PluginManager manager = getServer().getPluginManager();
		Plugin pl = manager.getPlugin("dynmap");
		if(pl != null) bridge = DynmapBridge.newInstance(pl);
		else System.out.println("MamiyaChat ccould not found dynmap.");
	}

	@Override
	public void onDisable(){
		HandlerList.unregisterAll(this);

		if(bridge != null)
			bridge.unload();
	}

	public static Main getPlugin(){
		return plugin;
	}

}
