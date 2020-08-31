package amata1219.mamiya.chat.command;

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

public class TellCommand extends Command {

	private final Main plugin = Main.plugin;

	public TellCommand(String name, String permission, String... aliases) {
		super(name, permission, aliases);
	}

	@Override
	public void execute(CommandSender sender, String[] strs) {
		Args args = new Args(strs);
		ProxiedPlayer player = plugin.getProxy().getPlayer(args.next());
		if(player == null){
			sender.sendMessage(new TextComponent(ChatColor.RED + "指定されたプレイヤーはオフライン又は存在しません。"));
			return;
		}

		if(plugin.isInvalidAccess(player)){
			sender.sendMessage(new TextComponent(ChatColor.RED + "指定されたプレイヤーはチャット共有の範囲対象外にいます。"));
			return;
		}

		if(!args.hasNext()){
			sender.sendMessage(new TextComponent(ChatColor.RED + "メッセージを入力して下さい。"));
		}else{
			Async.write(() -> {
				String senderName = null;
				String senderServer = null;
				UUID senderUUID = null;
				boolean isNonJapanize = false;
				if(sender instanceof ProxiedPlayer){
					ProxiedPlayer plyr = (ProxiedPlayer) sender;
					senderName = plyr.getName();
					senderServer = plugin.servers.get(plyr.getServer().getInfo().getName());
					senderUUID = plyr.getUniqueId();
					isNonJapanize = plugin.notUseJapanize.contains(senderUUID);
				}else{
					senderName = "Console";
					senderServer = "";
				}

				if(plugin.muted.contains(senderUUID)){
					sender.sendMessage(new TextComponent(ChatColor.RED + "ミュートされているため発言出来ません！"));
					return;
				}
				if(!senderName.equals("Console")){
					ReplyCommand.map.put(player.getUniqueId(), senderUUID);
					ReplyCommand.map.put(senderUUID, player.getUniqueId());
				}

				String text = args.get(1, args.length());
				Matcher matcher = plugin.urlMatcher.matcher(text);
				boolean find = matcher.find();
				String group = null;
				if(find)
					group = matcher.group();

				String message = plugin.privateChatFormat.replace("[sender]", senderName)
						.replace("[s_server]", senderServer)
						.replace("[receiver]", player.getName())
						.replace("[r_server]", plugin.servers.get(player.getServer().getInfo().getName()))
						.replace("[message]", plugin.formatMessage(plugin.coloring(text), isNonJapanize));
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
