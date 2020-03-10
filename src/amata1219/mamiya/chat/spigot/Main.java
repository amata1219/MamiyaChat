package amata1219.mamiya.chat.spigot;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import amata1219.mamiya.chat.ByteArrayDataMaker;

public class Main extends JavaPlugin {

	private static Main plugin;

	private DynmapBridge bridge;

	@Override
	public void onEnable(){
		plugin = this;

		PluginManager manager = getServer().getPluginManager();
		if(manager.isPluginEnabled("dynmap"))
			bridge = DynmapBridge.newInstance(manager.getPlugin("dynmap"));
		
		getCommand("jp").setExecutor(this);
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
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
		if(!(sender instanceof Player)){
			sender.sendMessage(ChatColor.RED + "ゲーム内から実行して下さい。");
			return true;
		}
		
		if(label.equalsIgnoreCase("jp")){
			Player player = (Player) sender;
			player.sendPluginMessage(plugin, "BungeeCord", ByteArrayDataMaker.makeByteArrayDataOutput("MamiyaChat", "JpCmd"));
		}
		return true;
	}

}
