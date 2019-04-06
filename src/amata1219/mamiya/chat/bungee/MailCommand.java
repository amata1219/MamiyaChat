package amata1219.mamiya.chat.bungee;

import java.util.ArrayList;
import java.util.UUID;

import amata1219.mamiya.chat.command.Args;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class MailCommand extends Command {

	private final Main plugin = Main.plugin;

	public MailCommand(String name, String permission, String... aliases) {
		super(name, permission, aliases);
	}

	@Override
	public void execute(CommandSender sender, String[] strs) {
		if(!(sender instanceof ProxiedPlayer)){
			sender.sendMessage(new TextComponent(ChatColor.RED + "ゲーム内から実行して下さい。"));
			return;
		}

		ProxiedPlayer player = (ProxiedPlayer) sender;
		if(plugin.isInvalidAccess(player))
			return;

		UUID uuid = player.getUniqueId();
		Args args = new Args(strs);
		switch(args.next()){
		case "send":
			if(!args.hasNext()){
				sender.sendMessage(new TextComponent(ChatColor.RED + "送信先を指定して下さい。"));
				return;
			}

			String receiver = args.next();
			if(!plugin.names.containsValue(receiver)){
				sender.sendMessage(new TextComponent(ChatColor.RED + "指定されたプレイヤーは存在しません。"));
				return;
			}

			if(!args.hasNext()){
				sender.sendMessage(new TextComponent(ChatColor.RED + "メッセージを入力して下さい。"));
				return;
			}

			UUID receiverUUID = plugin.names.inverse().get(receiver);
			Mail mail = new Mail(System.nanoTime(), receiverUUID, uuid, plugin.coloring(args.get(2, args.length() - 1)));
			ProxiedPlayer receiverr = plugin.getProxy().getPlayer(receiverUUID);
			if(receiverr != null){
				receiverr.sendMessage(new TextComponent(ChatColor.AQUA + "メールを受信しました。"));
				mail.send();
			}

			ArrayList<Mail> mailList = plugin.mails.get(receiverUUID);
			if(mailList == null)
				plugin.mails.put(receiverUUID, mailList = new ArrayList<>());
			mailList.add(mail);
			player.sendMessage(new TextComponent(ChatColor.AQUA + receiver + "さんにメールを送信しました。"));
			player.sendMessage(new TextComponent(mail.getMessage()));
			break;
		case "read":
			if(plugin.mails.containsKey(uuid)){
				ArrayList<Mail> mails = plugin.mails.get(uuid);
				player.sendMessage(new TextComponent(ChatColor.AQUA + String.valueOf(mails.size()) + "件のメールを受信しました。"));
				for(Mail ml : mails)
					ml.send();
			}else{
				player.sendMessage(new TextComponent(ChatColor.RED + "受信したメールはありません。"));
			}
			break;
		case "clear":
			if(!plugin.mails.containsKey(uuid)){
				player.sendMessage(new TextComponent(ChatColor.RED + "受信したメールはありません。"));
				return;
			}

			ArrayList<Mail> mails = plugin.mails.get(uuid);
			int count = mails.size();
			Config mailstore = plugin.maildata;
			for(Mail ml : mails)
				mailstore.config.set(String.valueOf(ml.time), null);
			plugin.mails.remove(uuid);
			mailstore.update();
			player.sendMessage(new TextComponent(ChatColor.AQUA + String.valueOf(count) + "件のメールを削除しました。"));
			break;
		}
	}

}
