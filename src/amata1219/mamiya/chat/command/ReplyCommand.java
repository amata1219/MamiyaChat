package amata1219.mamiya.chat.command;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.regex.Matcher;

import amata1219.mamiya.chat.bungee.Main;
import amata1219.mamiya.chat.bungee.Main.Async;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class ReplyCommand extends Command {

	private final Main plugin = Main.plugin;

	public static final HashMap<UUID, UUID> map = new HashMap<>();

	public ReplyCommand(String name, String permission, String... aliases) {
		super(name, permission, aliases);
	}

	@Override
	public void execute(CommandSender commander, String[] strs) {
		if(!(commander instanceof ProxiedPlayer)){
			commander.sendMessage(new TextComponent(ChatColor.RED + "ゲーム内から実行して下さい。"));
			return;
		}


		ProxiedPlayer sender = (ProxiedPlayer) commander;
		UUID uuid = map.get(sender.getUniqueId());
		if(uuid == null){
			sender.sendMessage(new TextComponent(ChatColor.RED + "返信相手が存在しません。"));
			return;
		}

		ProxiedPlayer player = plugin.getProxy().getPlayer(uuid);
		if(player == null){
			sender.sendMessage(new TextComponent(ChatColor.RED + "指定されたプレイヤーはオフライン又は存在しません。"));
			return;
		}

		if(plugin.isInvalidAccess(player)){
			sender.sendMessage(new TextComponent(ChatColor.RED + "指定されたプレイヤーはチャット共有の範囲対象外にいます。"));
			return;
		}

		Args args = new Args(strs);
		if(!args.hasNext()){
			sender.sendMessage(new TextComponent(ChatColor.RED + "メッセージを入力して下さい。"));
		}else{
			Async.write(() -> {
				String senderName = null;
				String senderServer = null;
				UUID senderUUID = null;
				if(sender instanceof ProxiedPlayer){
					senderName = sender.getName();
					senderServer = plugin.servers.get(sender.getServer().getInfo().getName());
					senderUUID = sender.getUniqueId();
				}else{
					senderName = "Console";
					senderServer = "";
				}

				if(plugin.muted.contains(senderUUID)){
					sender.sendMessage(new TextComponent(ChatColor.RED + "ミュートされているため発言出来ません！"));
					return;
				}

				String text = args.get(0, args.length());
				Matcher matcher = plugin.urlMatcher.matcher(text);
				boolean find = matcher.find();
				String group = null;
				if(find)
					group = matcher.group();

				String message = plugin.privateChatFormat.replace("[sender]", senderName)
						.replace("[s_server]", senderServer)
						.replace("[receiver]", player.getName())
						.replace("[r_server]", plugin.servers.get(player.getServer().getInfo().getName()))
						.replace("[message]", plugin.formatMessage(plugin.coloring(text), plugin.notUseJapanize.contains(player.getUniqueId())));
				TextComponent component = new TextComponent(message);
				if(find)
					component.setClickEvent(new ClickEvent(Action.OPEN_URL, group));
				sender.sendMessage(component);

				if(senderUUID == null){
					player.sendMessage(component);
				}else{
					HashSet<UUID> set = plugin.hidden.get(player.getUniqueId());
					if(set == null || !set.contains(senderUUID))
						player.sendMessage(component);
				}
			}).execute();
		}

	}

}
