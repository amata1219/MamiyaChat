package amata1219.mamiya.chat.spigot;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.dynmap.DynmapAPI;
import org.dynmap.DynmapWebChatEvent;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import amata1219.mamiya.chat.ByteArrayDataMaker;

public class Main extends JavaPlugin implements Listener, PluginMessageListener {

	public static Main plugin;

	private DynmapAPI dynmap;
	private Random rand = new Random();

	@Override
	public void onEnable(){
		plugin = this;

		PluginManager manager = getServer().getPluginManager();

		Plugin dynpl = manager.getPlugin("Dynmap");
		if(!(dynpl instanceof DynmapAPI))
			new NullPointerException("Dynmap is not found");

		manager.registerEvents(this, this);
	}

	@Override
	public void onDisable(){
		HandlerList.unregisterAll((JavaPlugin) this);
	}

	@SuppressWarnings("unchecked")
	@EventHandler
	public void onChat(DynmapWebChatEvent e){
		ArrayList<Player> list = (ArrayList<Player>) getServer().getOnlinePlayers();
		if(list.isEmpty())
			return;

		byte[] data = ByteArrayDataMaker.makeByteArrayDataOutput("MamiyaChat", "DynmapWebChat", e.getName(), e.getMessage());
		list.get(rand.nextInt(list.size())).sendPluginMessage(this, "BungeeCord", data);
	}

	@Override
	public void onPluginMessageReceived(String tag, Player player, byte[] data) {
		if(!(tag.equals("BungeeCord") || tag.equals("bungeecord:main")))
			return;

		ByteArrayDataInput in = ByteStreams.newDataInput(data);
		if(!in.readUTF().equals("MamiyaChat"))
			return;

		if(!in.readUTF().equals("Dynmap"))
			return;

		dynmap.sendBroadcastToWeb(in.readUTF(), in.readUTF());
	}

}
