package amata1219.mamiya.chat.bungee;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

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
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;

public class Main extends Plugin implements Listener {

	public static Main plugin;

	private Conf conf, data;
	private HashMap<UUID, HashSet<UUID>> muted;
	//mutedは空のHashSetを許容しない
	private ServerInfo coreServer;
	private String dynserver, format, normal, converted;

	@Override
	public void onEnable(){
		plugin = this;

		conf = new Conf("conf.yml");
		conf.saveDefault();

		Configuration config = conf.conf;
		ServerInfo coreServer = getProxy().getServerInfo(config.getString("CoreServer"));
		format = coloring(config.getString("Format"));
		normal = coloring(config.getString("Normal"));
		converted = coloring(config.getString("Converted"));

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

		getProxy().getPluginManager().registerListener(this, this);
	}

	@Override
	public void onDisable(){
		getProxy().getPluginManager().unregisterListeners(this);
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

		String type = null;
		if(Converter.canConvert(message)){
			type = converted;
			type = type.replace("[original]", message);
			type = type.replace("[converted]", message = Converter.convert(message));
		}else{
			type = normal;
			type = type.replace("[original]", message);
		}

		format = format.replace("[message]", message);
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
		format = format.replace("[message]", in.readUTF());
	}

	public String coloring(String text){
		return ChatColor.translateAlternateColorCodes('&', text);
	}

}
