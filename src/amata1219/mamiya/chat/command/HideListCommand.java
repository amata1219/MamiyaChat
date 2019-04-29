package amata1219.mamiya.chat.command;

import java.util.HashSet;
import java.util.UUID;

import amata1219.mamiya.chat.bungee.Main;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class HideListCommand extends Command {

	public HideListCommand(String name, String permission, String... aliases) {
		super(name, permission, aliases);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!(sender instanceof ProxiedPlayer)){
			sender.sendMessage(new TextComponent(ChatColor.RED + "ゲーム内から実行して下さい。"));
			return;
		}

		ProxiedPlayer player = (ProxiedPlayer) sender;
		if(Main.plugin.isInvalidAccess(player))
			return;
		HashSet<UUID> muted = Main.plugin.hidden.get(player.getUniqueId());
		if(muted == null){
			player.sendMessage(new TextComponent(ChatColor.RED + "非表示にしているプレイヤーはいません。"));
			return;
		}

		player.sendMessage(new TextComponent(ChatColor.AQUA + "非表示中のプレイヤー一覧"));
		for(UUID uuid : muted)
			player.sendMessage(new TextComponent(ChatColor.WHITE + "- " + Main.plugin.names.get(uuid)));
	}

}
