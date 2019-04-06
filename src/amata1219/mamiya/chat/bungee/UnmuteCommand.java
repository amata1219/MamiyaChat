package amata1219.mamiya.chat.bungee;

import java.util.HashSet;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class UnmuteCommand extends Command {

	private final Main plugin = Main.plugin;

	public UnmuteCommand(String name, String permission, String... aliases) {
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
		HashSet<UUID> set = plugin.muted.get(uuid);
		if(set == null || !set.contains(targetUUID)){
			sender.sendMessage(new TextComponent(ChatColor.RED + "指定されたプレイヤーはミュートしていません。"));
			return;
		}

		set.remove(targetUUID);
		if(set.isEmpty())
			plugin.muted.remove(uuid);
		sender.sendMessage(new TextComponent(ChatColor.AQUA + target + "さんのミュートを解除しました。"));
	}

}
