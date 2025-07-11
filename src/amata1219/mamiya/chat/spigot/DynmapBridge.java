package amata1219.mamiya.chat.spigot;

import java.util.List;
import java.util.Random;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.dynmap.DynmapAPI;
import org.dynmap.DynmapWebChatEvent;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import amata1219.mamiya.chat.ByteArrayDataMaker;


public class DynmapBridge implements Listener, PluginMessageListener {

	private final DynmapAPI dynmap;
	private final Random random = new Random();

	public static DynmapBridge newInstance(Plugin dynmap){
		DynmapBridge bridge = new DynmapBridge(dynmap);

		Main plugin = Main.getPlugin();
		Server server = plugin.getServer();
		plugin.getServer().getPluginManager().registerEvents(bridge, plugin);
		Messenger messenger = server.getMessenger();
		messenger.registerIncomingPluginChannel(plugin, "BungeeCord", bridge);
		messenger.registerOutgoingPluginChannel(plugin, "BungeeCord");

		return bridge;
	}


	private DynmapBridge(Plugin dynmap){
		this.dynmap = (DynmapAPI) dynmap;
	}

	public void unload(){
		Main plugin = Main.getPlugin();
		Messenger messenger = plugin.getServer().getMessenger();
		messenger.unregisterIncomingPluginChannel(plugin, "BungeeCord", this);
		messenger.unregisterOutgoingPluginChannel(plugin, "BungeeCord");
	}

	@SuppressWarnings("unchecked")
	@EventHandler
	public void onChat(DynmapWebChatEvent e){
		List<Player> players = (List<Player>) Main.getPlugin().getServer().getOnlinePlayers();
		if(players.isEmpty())
			return;

		Player player = players.get(random.nextInt(players.size()));
		String name = e.getName();
		String message = e.getMessage();
		player.sendPluginMessage(Main.getPlugin(), "BungeeCord", ByteArrayDataMaker.makeByteArrayDataOutput("MamiyaChat", "WebChat", name, message));
		dynmap.sendBroadcastToWeb(name, message);
		e.setCancelled(true);
	}

	@Override
	public void onPluginMessageReceived(String tag, Player player, byte[] data) {
		if(!(tag.equals("BungeeCord") || tag.equals("bungeecord:main")))
			return;

		ByteArrayDataInput in = ByteStreams.newDataInput(data);
		if(!in.readUTF().equals("MamiyaChat"))
			return;

		String type = in.readUTF();
		if(type.equals("Dynmap")){
			String name = in.readUTF();
			String message = in.readUTF().replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;").replace("'", "&#39;");
			Player speaker = Main.getPlugin().getServer().getPlayerExact(name);
			if(speaker != null)
				dynmap.postPlayerMessageToWeb(speaker, message);
			else
				dynmap.sendBroadcastToWeb(name, message);
		}else if(type.equals("Broadcast")){
			dynmap.sendBroadcastToWeb("Broadcast", in.readUTF());
		}
	}

}
