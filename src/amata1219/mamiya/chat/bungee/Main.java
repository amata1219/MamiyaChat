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
import amata1219.mamiya.chat.Converter;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
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

	public Config config, mutedata, maildata, playerdata;
	public ServerInfo dynmapServer;
	public String mainChatFormat, normalFormat, convertedFormat, privateChatFormat, mailFormat;
	public final HashBiMap<UUID, String> names = HashBiMap.create();
	public final HashMap<UUID, HashSet<UUID>> muted = new HashMap<>();
	public final HashMap<UUID, ArrayList<Mail>> mails = new HashMap<>();
	public long maildays = 2592000000000000L;


	@Override
	public void onEnable(){
		plugin = this;

		/*
		 * bugs
		 *
		 * section -> tula
		 * nanashityatto
		 *
		 */

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

		maildata = new Config("maildata.yml");
		maildata.saveDefault();
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

		playerdata = new Config("playerdata.yml");
		playerdata.saveDefault();
		Configuration plconf = playerdata.config;
		for(String key : plconf.getKeys())
			names.put(UUID.fromString(key), plconf.getString(key));

		PluginManager manager = getProxy().getPluginManager();

		manager.registerCommand(this, new TellCommand("tell", "mamiya.chat.tell", "msg", "message", "mctell"));
		manager.registerCommand(this, new MailCommand("mail", "mamiya.chat.mail", "mcmail"));
		manager.registerCommand(this, new MuteCommand("mute", "mamiya.chat.mute", "mcmute"));
		manager.registerCommand(this, new UnmuteCommand("unmute", "mamiya.chat.unmute", "mcunmute"));

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

		Configuration mailstore = maildata.config;
		for(ArrayList<Mail> list : mails.values()){
			for(Mail mail : list){
				mailstore.set(String.valueOf(mail.time), mail.toString());
			}
		}
		maildata.save();
	}

	public void loadValues(){
		Configuration config = this.config.config;

		mainChatFormat = coloring(config.getString("TextFormat.MainChat"));
		privateChatFormat = coloring(config.getString("TextFormat.PrivateChat"));
		mailFormat = coloring(config.getString("TextFormat.Mail"));

		normalFormat = coloring(config.getString("MessageFormat.Normal"));
		convertedFormat = coloring(config.getString("MessageFormat.Converted"));

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

		if(mails.containsKey(uuid)) for(Mail mail : mails.get(uuid))
			mail.send();
	}

	@EventHandler
	public void onChat(ChatEvent e){
		/*
		 * ミュート判定をこちらで行う都合により、こちらで各プレイヤーにメッセージ送信
		 * DynmapのためテキストはpluginMessageで送る(プレイヤー名、メッセージ)
		 *
		 * 着色(&を§に変換)
		 * アンチスパム(挨拶が引っかからないようにクールダウン式にすると良いかも)
		 *  - 最終発言と同じならキャンセル
		 *  - 最終発言は3秒間保持
		 *  - 悪質なやつはずば抜けて悪質なのでもう無視(そもそもそんなやつたまにしか来ない)
		 *  ローマ字変換(これあるだけで神になれる)
		 */
		System.out.println("onChat: 1");

		if(e.isCancelled() || e.isCommand())
			return;

		System.out.println("onChat: 2");

		UserConnection sender = (UserConnection) e.getSender();
		UUID senderUUID = sender.getUniqueId();
		String senderName = sender.getName();

		String message = e.getMessage();
		if(message.indexOf("&") != -1)
			message = coloring(message);

		TextComponent component = new TextComponent(mainChatFormat.replace("[player]", senderName).replace("[message]", formatMessage(message)));
		for(ProxiedPlayer player : getProxy().getPlayers()){
			HashSet<UUID> set = muted.get(player.getUniqueId());
			if(set == null || !set.contains(senderUUID))
				player.sendMessage(component);
		}

		byte[] dynmap = ByteArrayDataMaker.makeByteArrayDataOutput("MamiyaChat", "Dynmap", senderName, message);
		dynmapServer.sendData("BungeeCord", dynmap);

		e.setCancelled(true);
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

		String format = this.mainChatFormat;
		format = format.replace("[player]", in.readUTF());

		String message = in.readUTF();
		format = format.replace("[message]", formatMessage(message));

		TextComponent component = new TextComponent(message);
		for(ProxiedPlayer player : getProxy().getPlayers())
			player.sendMessage(component);
	}

	public String coloring(String text){
		return ChatColor.translateAlternateColorCodes('&', text);
	}

	public String formatMessage(String message){
		return Converter.canConvert(message) ? convertedFormat.replace("[original]", message)
				.replace("[converted]", Converter.convert(message)) : normalFormat.replace("[original]", message);
	}

}
