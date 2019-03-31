package amata1219.library.async;

import org.bukkit.Bukkit;

import amata1219.mamiya.chat.spigot.Main;

public interface Async extends Runnable {

	public static Async write(Async async){
		return async;
	}

	public default void execute(){
		Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin(), this);
	}

}
