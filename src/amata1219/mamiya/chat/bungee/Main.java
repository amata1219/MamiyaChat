package amata1219.mamiya.chat.bungee;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import java.util.UUID;

import com.google.common.collect.HashBiMap;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import amata1219.mamiya.chat.ByteArrayDataMaker;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;

public class Main extends Plugin implements Listener {

	public static Main plugin;

	public Config config, mutedata, playerdata, maildata, namedata;
	public final HashSet<String> servers = new HashSet<>();
	public ServerInfo dynmapServer;
	public String mainChatFormat, normalFormat, convertedFormat, privateChatFormat, mailFormat;
	public final HashBiMap<UUID, String> names = HashBiMap.create();
	public final HashMap<UUID, HashSet<UUID>> muted = new HashMap<>();
	public final HashMap<UUID, ArrayList<Mail>> mails = new HashMap<>();
	public final HashSet<UUID> notUseJapanize = new HashSet<>();
	public long maildays = 2592000000000000L;


	@Override
	public void onEnable(){
		plugin = this;

		config = new Config("config.yml");

		loadValues();

		mutedata = new Config("mutedata.yml");
		Configuration datastore = mutedata.config;
		for(String key : datastore.getKeys()){
			UUID uuid = UUID.fromString(key);
			List<String> list = datastore.getStringList(key);
			if(list.isEmpty())
				continue;

			HashSet<UUID> set = new HashSet<>(list.size());
			for(String mute : list)
				set.add(UUID.fromString(mute));

			muted.put(uuid, set);
		}

		playerdata = new Config("playerdata.yml");
		Configuration players = playerdata.config;
		for(String uuid : players.getStringList("Players"))
			notUseJapanize.add(UUID.fromString(uuid));


		maildata = new Config("maildata.yml");
		Configuration mailstore = maildata.config;
		for(String key : mailstore.getKeys()){
			long time = Long.parseLong(key);
			if(System.nanoTime() - time >= maildays){
				mailstore.set(key, null);
				continue;
			}

			String[] data = mailstore.getString(key).split(",");
			UUID receiver = UUID.fromString(data[0]);
			ArrayList<Mail> mails = this.mails.get(receiver);
			if(mails == null)
				this.mails.put(receiver, mails = new ArrayList<>());
			mails.add(new Mail(time, receiver, UUID.fromString(data[1]), data[2]));
		}

		namedata = new Config("namedata.yml");
		Configuration plconf = namedata.config;
		for(String key : plconf.getKeys())
			names.put(UUID.fromString(key), plconf.getString(key));

		PluginManager manager = getProxy().getPluginManager();

		manager.registerCommand(this, new MamiyaChatCommand("mamiyachat", "mamiya.chat.admin", "mchat"));
		manager.registerCommand(this, new TellCommand("tell", "mamiya.chat", "msg", "message"));
		manager.registerCommand(this, new MailCommand("mail", "mamiya.chat", "mmail"));
		manager.registerCommand(this, new MuteCommand("mute", "mamiya.chat", "mmute"));
		manager.registerCommand(this, new UnmuteCommand("unmute", "mamiya.chat", "munmute"));
		manager.registerCommand(this, new MuteListCommand("mutelist", "mamiya.chat", "mutel", "mmutelist"));
		manager.registerCommand(this, new JapanizeCommand("japanize", "mamiya.chat", "jp"));

		manager.registerListener(this, this);
	}

	@Override
	public void onDisable(){
		getProxy().getPluginManager().unregisterListeners(this);

		Configuration datastore = mutedata.config;
		StringBuilder builder = new StringBuilder();
		for(Entry<UUID, HashSet<UUID>> entry : muted.entrySet()){
			String uuid = entry.getKey().toString();
			HashSet<UUID> set = entry.getValue();
			if(set.isEmpty()){
				datastore.set(uuid, null);
			}else{
				for(UUID mute : set)
					builder.append(",").append(mute.toString());

				datastore.set(uuid, builder.toString().substring(1));
				builder.setLength(0);
			}
		}
		mutedata.save();

		Configuration players = playerdata.config;
		List<String> uuids = new ArrayList<>(notUseJapanize.size());
		for(UUID uuid : notUseJapanize)
			uuids.add(uuid.toString());
		players.set("Players", uuids);
		playerdata.save();

		Configuration mailstore = maildata.config;
		for(ArrayList<Mail> list : mails.values()){
			for(Mail mail : list){
				mailstore.set(String.valueOf(mail.time), mail.toString());
			}
		}
		maildata.save();

		Configuration namestore = namedata.config;
		for(Entry<UUID, String> entry : names.entrySet())
			namestore.set(entry.getKey().toString(), entry.getValue());
		namedata.save();
	}

	public void loadValues(){
		Configuration config = this.config.config;

		mainChatFormat = coloring(config.getString("TextFormat.MainChat"));
		privateChatFormat = coloring(config.getString("TextFormat.PrivateChat"));
		mailFormat = coloring(config.getString("TextFormat.Mail"));

		normalFormat = coloring(config.getString("MessageFormat.Normal"));
		convertedFormat = coloring(config.getString("MessageFormat.Converted"));

		servers.clear();
		servers.addAll(config.getStringList("Servers"));

		dynmapServer = getProxy().getServerInfo(config.getString("DynmapServer"));

		maildays = config.getInt("MailDays") * 86400000000000L;
	}

	@EventHandler
	public void onJoin(PostLoginEvent e){
		ProxiedPlayer player = e.getPlayer();
		UUID uuid = player.getUniqueId();
		String name = player.getName();

		if(!names.containsKey(uuid) || !names.get(uuid).equals(name))
			names.put(uuid, name);

		if(isInvalidAccess(player))
			return;

		if(mails.containsKey(uuid)){
			ArrayList<Mail> mails = this.mails.get(uuid);
			player.sendMessage(new TextComponent(ChatColor.AQUA + String.valueOf(mails.size()) + "件のメールを受信しました。"));
			for(Mail mail : mails)
				mail.send();
		}
	}

	@EventHandler
	public void onChat(ChatEvent e){
		if(e.isCancelled() || e.isCommand())
			return;

		UserConnection sender = (UserConnection) e.getSender();
		if(isInvalidAccess(sender.getServer()))
			return;

		e.setCancelled(true);

		Async.write(() -> {
			UUID senderUUID = sender.getUniqueId();
			String senderName = sender.getName();

			String message = e.getMessage();
			if(message.indexOf("&") != -1)
				message = coloring(message);

			message = formatMessage(message, notUseJapanize.contains(senderUUID));
			TextComponent component = new TextComponent(message = mainChatFormat.replace("[player]", senderName).replace("[message]", message));
			System.out.println(message);
			for(ProxiedPlayer player : getProxy().getPlayers()){
				if(isInvalidAccess(player))
					continue;

				HashSet<UUID> set = muted.get(player.getUniqueId());
				if(set == null || !set.contains(senderUUID))
					player.sendMessage(component);
			}

			byte[] dynmap = ByteArrayDataMaker.makeByteArrayDataOutput("MamiyaChat", "Dynmap", senderName, message);
			dynmapServer.sendData("BungeeCord", dynmap);
		}).execute();
	}

	@EventHandler
	public void onReceive(PluginMessageEvent e){
		String tag = e.getTag();
		if(!(tag.equals("BungeeCord") || tag.equals("bungeecord:main")))
			return;

		ByteArrayDataInput in = ByteStreams.newDataInput(e.getData());
		if(!in.readUTF().equals("MamiyaChat"))
			return;

		if(!in.readUTF().equals("WebChat"))
			return;

		String name = in.readUTF();
		String message = mainChatFormat.replace("[player]", "[WEB]" + ((name == null || name.isEmpty()) ? "" : name))
				.replace("[message]", formatMessage(in.readUTF(), false));

		System.out.println(message);
		TextComponent component = new TextComponent(message);
		for(ProxiedPlayer player : getProxy().getPlayers()){
			if(isInvalidAccess(player))
				player.sendMessage(component);
		}
	}

	public String coloring(String text){
		return ChatColor.translateAlternateColorCodes('&', text);
	}

	public String formatMessage(String message, boolean notUseJapanize){
		return Converter.canConvert(message) && !notUseJapanize ? convertedFormat.replace("[original]", message)
				.replace("[converted]", Converter.convert(message)) : normalFormat.replace("[original]", message);
	}

	public boolean isInvalidAccess(Server server){
		return !servers.contains(server.getInfo().getName());
	}

	public boolean isInvalidAccess(ProxiedPlayer player){
		return !servers.contains(player.getServer().getInfo().getName());
	}

	public interface Async extends Runnable {

		public static Async write(Async async){
			return async;
		}

		public default void execute(){
			Main plugin = Main.plugin;
			plugin.getProxy().getScheduler().runAsync(plugin, this);
		}

	}

}
