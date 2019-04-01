package amata1219.mamiya.chat.bungee;

import java.util.HashSet;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class JapanizeCommand extends Command {

	public JapanizeCommand(String name, String permission, String... aliases) {
		super(name, permission, aliases);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!(sender instanceof ProxiedPlayer)){
			sender.sendMessage(new TextComponent(ChatColor.RED + "ゲーム内から実行して下さい。"));
			return;
		}

		ProxiedPlayer player = (ProxiedPlayer) sender;
		UUID uuid = player.getUniqueId();
		HashSet<UUID> set = Main.plugin.notUseJapanize;
		if(set.contains(uuid)){
			set.remove(uuid);
			player.sendMessage(new TextComponent(ChatColor.AQUA + "ローマ字変換機能を有効にしました。"));
		}else{
			set.add(uuid);
			player.sendMessage(new TextComponent(ChatColor.AQUA + "ローマ字変換機能を無効にしました。"));
		}
	}

}
