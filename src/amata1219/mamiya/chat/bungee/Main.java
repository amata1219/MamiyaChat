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

import amata1219.library.command.Args;
import amata1219.mamiya.chat.ByteArrayDataMaker;
import amata1219.mamiya.chat.Converter;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;

public class Main extends Plugin implements Listener {

	public static Main plugin;

	private Conf conf, data, mail, players;
	private final HashMap<UUID, HashSet<UUID>> muted = new HashMap<>();
	//mutedは空のHashSetを許容しない
	private ServerInfo coreServer;
	String format, normal, converted, privateformat, mailformat;
	private final HashMap<UUID, ArrayList<Mail>> mails = new HashMap<>();
	private long maildays = 2592000000000000L;
	public final HashBiMap<UUID, String> nameData = HashBiMap.create();

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

		conf = new Conf("conf.yml");
		conf.saveDefault();

		loadValues();

		data = new Conf("data.yml");
		data.saveDefault();

		Configuration datastore = data.conf;
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

		mail = new Conf("mail.yml");
		mail.saveDefault();
		Configuration mailstore = mail.conf;
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

		players = new Conf("players.yml");
		players.saveDefault();
		Configuration plconf = players.conf;
		for(String key : plconf.getKeys())
			nameData.put(UUID.fromString(key), plconf.getString(key));

		PluginManager manager = getProxy().getPluginManager();
		/*
		 * tell player message
		 * mail send/clear player message
		 * mute player
		 * unmute player
		 */

		manager.registerCommand(this, new Command("tell", "mamiya.chat.tell", "msg", "message", "mctell"){

			@Override
			public void execute(CommandSender sender, String[] strs) {
				Args args = new Args(strs);
				String name = args.next();
				ProxiedPlayer player = getProxy().getPlayer(name);
				if(player == null){
					sender.sendMessage(new TextComponent(ChatColor.RED + "指定されたプレイヤーはオフライン又は存在しません。"));
					return;
				}

				if(!args.hasNext()){
					sender.sendMessage(new TextComponent(ChatColor.RED + "メッセージを入力して下さい。"));
				}else{
					String senderName = sender instanceof ProxiedPlayer ? ((ProxiedPlayer) sender).getName() : "Console";
					String message = privateformat.replace("[sender]", senderName)
							.replace("[receiver]", player.getName())
							.replace("[message]", formatMessage(coloring(args.get(1, args.length() - 1))));
					TextComponent component = new TextComponent(message);
					sender.sendMessage(component);
					player.sendMessage(component);
				}
			}

		});

		manager.registerCommand(this, new Command("mail", "mamiya.chat.mail", "mcmail"){

			@Override
			public void execute(CommandSender sender, String[] strs) {
				if(!(sender instanceof ProxiedPlayer)){
					sender.sendMessage(new TextComponent(ChatColor.RED + "ゲーム内から実行して下さい。"));
					return;
				}

				ProxiedPlayer player = (ProxiedPlayer) sender;
				UUID uuid = player.getUniqueId();
				Args args = new Args(strs);
				switch(args.next()){
				case "send":
					if(!args.hasNext()){
						sender.sendMessage(new TextComponent(ChatColor.RED + "送信先を指定して下さい。"));
						return;
					}

					String receiver = args.get(1);
					if(!nameData.containsValue(receiver)){
						sender.sendMessage(new TextComponent(ChatColor.RED + "指定されたプレイヤーは存在しません。"));
						return;
					}

					if(!args.hasNext()){
						sender.sendMessage(new TextComponent(ChatColor.RED + "メッセージを入力して下さい。"));
						return;
					}

					UUID receiverUUID = nameData.inverse().get(receiver);
					Mail mail = new Mail(System.nanoTime(), receiverUUID, uuid, coloring(args.get(2, args.length())));
					if(getProxy().getPlayer(receiverUUID) != null)
						mail.send();

					ArrayList<Mail> mailList = plugin.mails.get(receiverUUID);
					if(mailList == null)
						plugin.mails.put(receiverUUID, mailList = new ArrayList<>());
					mailList.add(mail);
					player.sendMessage(new TextComponent(ChatColor.AQUA + receiver + "さんにメールを送信しました。"));
					player.sendMessage(new TextComponent(mail.getMessage()));
					break;
				case "clear":
					if(!mails.containsKey(uuid)){
						player.sendMessage(new TextComponent(ChatColor.RED + "受信したメールはありません。"));
						return;
					}

					ArrayList<Mail> mails = plugin.mails.get(uuid);
					int count = mails.size();
					Conf mailstore = plugin.mail;
					for(Mail ml : mails)
						mailstore.conf.set(String.valueOf(ml.time), null);
					plugin.mails.remove(uuid);
					mailstore.update();
					player.sendMessage(new TextComponent(ChatColor.AQUA + String.valueOf(count) + "件のメールを削除しました。"));
					break;
				}
			}

		});

		manager.registerCommand(this, new Command("mute", "mamiya.chat.mute", "mcmute"){

			@Override
			public void execute(CommandSender sender, String[] args) {
				if(!(sender instanceof ProxiedPlayer)){
					sender.sendMessage(new TextComponent(ChatColor.RED + "ゲーム内から実行して下さい。"));
					return;
				}

				if(args.length == 0){
					sender.sendMessage(new TextComponent(ChatColor.RED + "プレイヤーを指定して下さい。"));
					return;
				}

				String target = args[0];
				if(!nameData.containsValue(target)){
					sender.sendMessage(new TextComponent(ChatColor.RED + "指定されたプレイヤーは存在しません。"));
					return;
				}

				ProxiedPlayer player = (ProxiedPlayer) sender;
				UUID uuid = player.getUniqueId();
				UUID tuuid = nameData.inverse().get(target);
				HashSet<UUID> set = muted.get(uuid);
				if(set == null)
					muted.put(uuid, new HashSet<>());

				if(set.contains(tuuid)){
					sender.sendMessage(new TextComponent(ChatColor.RED + "指定されたプレイヤーは既にミュートしています。"));
					return;
				}

				set.add(tuuid);
				sender.sendMessage(new TextComponent(ChatColor.AQUA + target + "さんをミュートしました。"));
			}

		});

		manager.registerCommand(this, new Command("unmute", "mamiya.chat.unmute", "mcunmute"){

			@Override
			public void execute(CommandSender sender, String[] args) {
				if(!(sender instanceof ProxiedPlayer)){
					sender.sendMessage(new TextComponent(ChatColor.RED + "ゲーム内から実行して下さい。"));
					return;
				}

				if(args.length == 0){
					sender.sendMessage(new TextComponent(ChatColor.RED + "プレイヤーを指定して下さい。"));
					return;
				}

				String target = args[0];
				if(!nameData.containsValue(target)){
					sender.sendMessage(new TextComponent(ChatColor.RED + "指定されたプレイヤーは存在しません。"));
					return;
				}

				ProxiedPlayer player = (ProxiedPlayer) sender;
				UUID uuid = player.getUniqueId();
				UUID tuuid = nameData.inverse().get(target);
				HashSet<UUID> set = muted.get(uuid);
				if(set == null || set.contains(tuuid)){
					sender.sendMessage(new TextComponent(ChatColor.RED + "指定されたプレイヤーはミュートしていません。"));
					return;
				}

				set.remove(tuuid);
				if(set.isEmpty())
					muted.remove(uuid);
				sender.sendMessage(new TextComponent(ChatColor.AQUA + target + "さんのミュートを解除しました。"));
			}

		});

		manager.registerListener(this, this);
	}

	@Override
	public void onDisable(){
		getProxy().getPluginManager().unregisterListeners(this);

		Configuration datastore = data.conf;
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
		data.save();

		Configuration mailstore = mail.conf;
		for(ArrayList<Mail> mails : this.mails.values()){
			for(Mail mail : mails){
				mailstore.set(String.valueOf(mail.time), mail.toString());
			}
		}
		mail.save();
	}

	public void loadValues(){
		Configuration config = conf.conf;
		coreServer = getProxy().getServerInfo(config.getString("CoreServer"));
		format = coloring(config.getString("Format"));
		normal = coloring(config.getString("Normal"));
		converted = coloring(config.getString("Converted"));
		privateformat = coloring(config.getString("PrivateChatFormat"));
		mailformat = coloring(config.getString("MailFormat"));
		maildays = config.getInt("MailDays") * 86400000000000L;
	}

	@EventHandler
	public void onJoin(PostLoginEvent e){
		ProxiedPlayer player = e.getPlayer();
		UUID uuid = player.getUniqueId();
		String name = player.getName();
		if(!nameData.containsKey(uuid) || !nameData.get(uuid).equals(name))
			nameData.put(uuid, name);
		ArrayList<Mail> mails = this.mails.get(uuid);
		if(mails != null) for(Mail mail : mails)
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

		if(e.isCancelled() || e.isCommand())
			return;

		UserConnection sender = (UserConnection) e.getSender();
		String format = this.format;
		String name = sender.getName();
		format = format.replace("[player]", name);

		String message = e.getMessage();

		//着色
		if(message.indexOf("&") != 0)
			message = coloring(message);

		format = format.replace("[message]", formatMessage(message));

		UUID senderUUID = sender.getUniqueId();
		TextComponent component = new TextComponent(message);
		//全プレイヤーにメッセージ送信(ミュート判定有り)
		for(ProxiedPlayer player : getProxy().getPlayers()){
			HashSet<UUID> set = muted.get(player.getUniqueId());
			if(set != null && set.contains(senderUUID))
				continue;

			player.sendMessage(component);
		}

		//Dynmap用のメッセージ送信
		byte[] dynmap = ByteArrayDataMaker.makeByteArrayDataOutput("MamiyaChat", "Dynmap", name, message);
		coreServer.sendData("BungeeCord", dynmap);

		e.setMessage(message);
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

		if(!in.readUTF().equals("DynmapWebChat"))
			return;

		String format = this.format;
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
		String type = null;
		if(Converter.canConvert(message)){
			type = converted;
			type = type.replace("[original]", message);
			type = type.replace("[converted]", message = Converter.convert(message));
		}else{
			type = normal;
			type = type.replace("[original]", message);
		}
		return message;
	}

}
