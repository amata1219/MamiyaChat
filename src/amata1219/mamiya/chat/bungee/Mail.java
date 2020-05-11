package amata1219.mamiya.chat.bungee;

import java.util.UUID;

import amata1219.mamiya.chat.bungee.Main.Async;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class Mail {

	public final long time;
	private final UUID receiver, sender;
	private final String message;

	public Mail(long time, UUID receiver, UUID sender, String message){
		this.time = time;
		this.receiver = receiver;
		this.sender = sender;
		this.message = message;
	}

	public void send(){
		Async.write(() -> {
			Main plugin = Main.plugin;
			ProxiedPlayer player = plugin.getProxy().getPlayer(receiver);
			if(player == null || plugin.isInvalidAccess(player)) return;
			player.sendMessage(new TextComponent(getMessage()));
		}).execute();
	}

	public String getMessage(){
		Main plugin = Main.plugin;
		return plugin.mailFormat.replace("[sender]", plugin.names.get(sender))
				.replace("[receiver]", plugin.names.get(receiver))
				.replace("[message]", plugin.formatMessage(message, plugin.notUseJapanize.contains(sender)));
	}

	@Override
	public String toString(){
		return receiver.toString() + "," + sender.toString() + "," + message;
	}

}
