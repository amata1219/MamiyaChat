package amata1219.library.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Sender {

	private final CommandSender sender;

	public Sender(CommandSender sender){
		this.sender = sender;
	}

	public CommandSender getSender(){
		return sender;
	}

	public boolean isNotPlayer(){
		return !(sender instanceof Player);
	}

	public Player castPlayer(){
		return (Player) sender;
	}

	public void send(String s){
		sender.sendMessage(s);
	}

	public void info(String s){
		send(ChatColor.AQUA + s);
	}

	public void add(String s){
		send(ChatColor.GRAY + s);
	}

	public void warn(String s){
		send(ChatColor.RED + s);
	}

}
