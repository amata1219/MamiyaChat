package amata1219.mamiya.chat.bungee;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.HashBiMap;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import amata1219.mamiya.chat.ByteArrayDataMaker;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;

public class Main extends Plugin implements Listener {

	public static Main plugin;

	public Config config, mutedata, playerdata, maildata, namedata;
	public ServerInfo dynmapServer;
	public String mainChatFormat, normalFormat, convertedFormat, privateChatFormat, mailFormat, broadcastFormat, mailMessage;
	public final HashBiMap<UUID, String> names = HashBiMap.create();
	public final HashMap<UUID, HashSet<UUID>> muted = new HashMap<>();
	public final HashMap<UUID, ArrayList<Mail>> mails = new HashMap<>();
	public final HashSet<UUID> notUseJapanize = new HashSet<>();
	public final HashMap<String, String> servers = new HashMap<>();
	public long maildays = 2592000000000000L;
	public final Pattern urlMatcher = Pattern.compile("(http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+", Pattern.CASE_INSENSITIVE);


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

		manager.registerCommand(this, new MamiyaChatCommand("mamiyachat", "mamiya.chat.admin", new String[0]));
		manager.registerCommand(this, new TellCommand("tell", "mamiya.chat", "msg", "message"));
		manager.registerCommand(this, new MailCommand("mail", "mamiya.chat", new String[0]));
		manager.registerCommand(this, new MuteCommand("mute", "mamiya.chat", new String[0]));
		manager.registerCommand(this, new UnmuteCommand("unmute", "mamiya.chat", new String[0]));
		manager.registerCommand(this, new MuteListCommand("mutelist", "mamiya.chat", new String[0]));
		manager.registerCommand(this, new JapanizeCommand("japanize", "mamiya.chat", "jp"));
		manager.registerCommand(this, new BroadcastCommand("bcast", "mamiya.chat.admin", new String[0]));

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
		broadcastFormat = coloring(config.getString("TextFormat.Broadcast"));

		normalFormat = coloring(config.getString("MessageFormat.Normal"));
		convertedFormat = coloring(config.getString("MessageFormat.Converted"));

		servers.clear();
		for(String serverAliase : config.getStringList("Servers")){
			String[] strs = serverAliase.split(":");
			servers.put(strs[0], strs.length < 2 ? "" : coloring(strs[1]));
		}

		dynmapServer = getProxy().getServerInfo(config.getString("DynmapServer"));

		maildays = config.getInt("MailDays") * 86400000000000L;

		mailMessage = coloring(config.getString("MailMessage"));
	}

	@EventHandler
	public void onJoin(ServerConnectedEvent e){
		ProxiedPlayer player = e.getPlayer();
		UUID uuid = player.getUniqueId();
		String name = player.getName();

		if(!names.containsKey(uuid) || !names.get(uuid).equals(name))
			names.put(uuid, name);

		getProxy().getScheduler().schedule(this, new Runnable(){

			@Override
			public void run() {
				if(mails.containsKey(uuid)){
					ArrayList<Mail> mails = Main.plugin.mails.get(uuid);
					player.sendMessage(new TextComponent(mailMessage.replace("[size]", String.valueOf(mails.size()))));
					for(Mail mail : mails)
						mail.send();
				}
			}

		}, 400, TimeUnit.MILLISECONDS);
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

			Matcher matcher = urlMatcher.matcher(message);

			message = formatMessage(message, notUseJapanize.contains(senderUUID));
			byte[] dynmap = ByteArrayDataMaker.makeByteArrayDataOutput("MamiyaChat", "Dynmap", senderName, message);
			dynmapServer.sendData("BungeeCord", dynmap);
			TextComponent component = new TextComponent(message = mainChatFormat.replace("[player]", senderName).replace("[message]", message).replace("[server]", servers.get(sender.getServer().getInfo().getName())));
			if(matcher.find())
				component.setClickEvent(new ClickEvent(Action.OPEN_URL, matcher.group()));
			System.out.println(message);
			for(ProxiedPlayer player : getProxy().getPlayers()){
				if(isInvalidAccess(player))
					continue;

				HashSet<UUID> set = muted.get(player.getUniqueId());
				if(set == null || !set.contains(senderUUID))
					player.sendMessage(component);
			}
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
				.replace("[message]", formatMessage(in.readUTF(), false)).replace("[server]", "");

		System.out.println(message);
		TextComponent component = new TextComponent(message);
		for(ProxiedPlayer player : getProxy().getPlayers()){
			if(!isInvalidAccess(player))
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
		return !servers.containsKey(server.getInfo().getName());
	}

	public boolean isInvalidAccess(ProxiedPlayer player){
		if(player == null)
			return true;

		Server server = player.getServer();
		if(server == null)
			return true;

		ServerInfo info = server.getInfo();
		if(info == null)
			return true;

		return !servers.containsKey(info.getName());
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
