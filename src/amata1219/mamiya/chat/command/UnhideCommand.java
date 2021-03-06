package amata1219.mamiya.chat.command;

import java.util.HashSet;
import java.util.UUID;

import amata1219.mamiya.chat.bungee.Main;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class UnhideCommand extends Command {

	private final Main plugin = Main.plugin;

	public UnhideCommand(String name, String permission, String... aliases) {
		super(name, permission, aliases);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!(sender instanceof ProxiedPlayer)){
			sender.sendMessage(new TextComponent(ChatColor.RED + "ゲーム内から実行して下さい。"));
			return;
		}

		ProxiedPlayer player = (ProxiedPlayer) sender;
		if(plugin.isInvalidAccess(player))
			return;

		if(args.length == 0){
			sender.sendMessage(new TextComponent(ChatColor.RED + "プレイヤーを指定して下さい。"));
			return;
		}

		String target = args[0];
		if(!plugin.names.containsValue(target)){
			sender.sendMessage(new TextComponent(ChatColor.RED + "指定されたプレイヤーは存在しません。"));
			return;
		}

		UUID uuid = player.getUniqueId();
		UUID targetUUID = plugin.names.inverse().get(target);
		HashSet<UUID> set = plugin.hidden.get(uuid);
		if(set == null || !set.contains(targetUUID)){
			sender.sendMessage(new TextComponent(ChatColor.RED + "指定されたプレイヤーは非表示にしていません。"));
			return;
		}

		set.remove(targetUUID);
		if(set.isEmpty())
			plugin.hidden.remove(uuid);
		sender.sendMessage(new TextComponent(ChatColor.AQUA + target + "さんを表示しました。"));
	}

}
