package amata1219.mamiya.chat.bungee;

import amata1219.mamiya.chat.command.Args;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
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
		if(plugin.isInvalidAccess(player))
			return;

		if(player == null){
			sender.sendMessage(new TextComponent(ChatColor.RED + "指定されたプレイヤーはオフライン又は存在しません。"));
			return;
		}

		if(!args.hasNext()){
			sender.sendMessage(new TextComponent(ChatColor.RED + "メッセージを入力して下さい。"));
		}else{
			String senderName = null;
			String senderServer = null;
			if(sender instanceof ProxiedPlayer){
				ProxiedPlayer plyr = (ProxiedPlayer) sender;
				senderName = plyr.getName();
				senderServer = plugin.servers.get(plyr.getServer().getInfo().getName());
			}else{
				senderName = "Console";
				senderServer = "";
			}
			String message = plugin.privateChatFormat.replace("[sender]", senderName)
					.replace("[s_server]", senderServer)
					.replace("[receiver]", player.getName())
					.replace("[r_server]", plugin.servers.get(player.getServer().getInfo().getName()))
					.replace("[message]", plugin.formatMessage(plugin.coloring(args.get(1, args.length() - 1)), plugin.notUseJapanize.contains(player.getUniqueId())));
			TextComponent component = new TextComponent(message);
			sender.sendMessage(component);
			player.sendMessage(component);
		}
	}

}
