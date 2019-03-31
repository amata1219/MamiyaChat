package amata1219.mamiya.chat.bungee;

import java.util.HashSet;
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
		if(!(sender instanceof ProxiedPlayer)){
			sender.sendMessage(new TextComponent(ChatColor.RED + "ゲーム内から実行して下さい。"));
			return;
		}

		if(args.length == 0){
			sender.sendMessage(new TextComponent(ChatColor.RED + "プレイヤーを指定して下さい。"));
			return;
		}

		String target = args[0];
		if(!plugin.names.containsValue(target)){
			sender.sendMessage(new TextComponent(ChatColor.RED + "指定されたプレイヤーは存在しません。"));
			return;
		}

		ProxiedPlayer player = (ProxiedPlayer) sender;
		UUID uuid = player.getUniqueId();
		UUID tuuid = plugin.names.inverse().get(target);
		HashSet<UUID> set = plugin.muted.get(uuid);
		if(set == null)
			plugin.muted.put(uuid, new HashSet<>());

		if(set.contains(tuuid)){
			sender.sendMessage(new TextComponent(ChatColor.RED + "指定されたプレイヤーは既にミュートしています。"));
			return;
		}

		set.add(tuuid);
		sender.sendMessage(new TextComponent(ChatColor.AQUA + target + "さんをミュートしました。"));
	}

}
