package amata1219.mamiya.chat.command;

import java.util.UUID;

import amata1219.mamiya.chat.bungee.Main;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class MuteListCommand extends Command {

	private final Main main = Main.plugin;

	public MuteListCommand(String name, String permission, String... aliases) {
		super(name, permission, aliases);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(main.muted.isEmpty()){
			sender.sendMessage(new TextComponent(ChatColor.RED + "ミュートしているプレイヤーはいません。"));
			return;
		}

		sender.sendMessage(new TextComponent(ChatColor.AQUA + "ミュート中のプレイヤー一覧"));
		for(UUID uuid : main.muted)
			sender.sendMessage(new TextComponent(ChatColor.WHITE + "- " + Main.plugin.names.get(uuid)));
	}


}
