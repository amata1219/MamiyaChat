package amata1219.mamiya.chat.spigot;

import java.util.HashMap;

import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import amata1219.library.command.Args;
import amata1219.library.command.Command;
import amata1219.library.command.Sender;

public class Main extends JavaPlugin {

	private static Main plugin;

	private final HashMap<String, Command> commands = new HashMap<>();
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

	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args){
		commands.get(command.getName()).onCommand(new Sender(sender), new Args(args));
		return true;
	}

	public static Main getPlugin(){
		return plugin;
	}

}
