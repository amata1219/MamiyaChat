package amata1219.mamiya.chat.command;

import amata1219.mamiya.chat.ByteArrayDataMaker;
import amata1219.mamiya.chat.bungee.Main;
import amata1219.mamiya.chat.bungee.Main.Async;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class BroadcastCommand extends Command {

	private Main plugin = Main.plugin;

	public BroadcastCommand(String name, String permission, String... aliases) {
		super(name, permission, aliases);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		Async.write(() -> {
			String message = String.join(" ", args);
			byte[] dynmap = ByteArrayDataMaker.makeByteArrayDataOutput("MamiyaChat", "Broadcast", message);
			plugin.dynmapServer.sendData("BungeeCord", dynmap);
			TextComponent component = new TextComponent(message = plugin.broadcastFormat.replace("[message]", message));
			System.out.println(message);
			for(ProxiedPlayer player : plugin.getProxy().getPlayers()){
				if(!plugin.isInvalidAccess(player))
					player.sendMessage(component);
			}
		}).execute();;
	}

}
