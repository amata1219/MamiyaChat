package amata1219.mamiya.chat.bungee;

import java.util.UUID;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class MuteCommand extends Command {

	private final Main plugin = Main.plugin;

	public MuteCommand(String name, String permission, String... aliases) {
		super(name, permission, aliases);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(args.length == 0){
			sender.sendMessage(new TextComponent(ChatColor.RED + "プレイヤーを指定して下さい。"));
			return;
		}

		String target = args[0];
		if(!plugin.names.containsValue(target)){
			sender.sendMessage(new TextComponent(ChatColor.RED + "指定されたプレイヤーは存在しません。"));
			return;
		}

		UUID uuid = plugin.names.inverse().get(target);
		if(plugin.muted.contains(uuid)){
			sender.sendMessage(new TextComponent(ChatColor.RED + "指定されたプレイヤーは既にミュートしています。"));
			return;
		}

		plugin.muted.add(uuid);
		sender.sendMessage(new TextComponent(ChatColor.AQUA + target + "さんをミュートしました。"));
		ProxiedPlayer player = plugin.getProxy().getPlayer(uuid);
		if(player != null)
			player.sendMessage(new TextComponent(ChatColor.RED + "ミュートされました。"));
	}
}
