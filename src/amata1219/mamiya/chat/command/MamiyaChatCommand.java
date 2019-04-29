package amata1219.mamiya.chat.command;

import amata1219.mamiya.chat.bungee.Main;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class MamiyaChatCommand extends Command {

	public MamiyaChatCommand(String name, String permission, String... aliases) {
		super(name, permission, aliases);
	}

	@Override
	public void execute(CommandSender sender, String[] strs) {
		Args args = new Args(strs);
		switch(args.next()){
		case "reload":
			Main plugin = Main.plugin;
			plugin.config.reload();
			plugin.loadValues();
			sender.sendMessage(new TextComponent(ChatColor.AQUA + "コンフィグを再読み込みしました。"));
			break;
		default:
			break;
		}
	}

}
