package amata1219.mamiya.chat.bungee;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.google.common.io.ByteStreams;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class Conf {

	private static final ConfigurationProvider PROVIDER = ConfigurationProvider.getProvider(YamlConfiguration.class);

	private final Main plugin = Main.plugin;
	private final String name;
	private final File file;
	Configuration conf;

	public Conf(String name){
		this.name = name;
		file = new File(plugin.getDataFolder() + File.separator + name);
	}

	public void saveDefault(){
		if(!file.exists()){
			try{
				plugin.getDataFolder().mkdirs();
				file.createNewFile();
			}catch(IOException e){

			}

			try(FileOutputStream output = new FileOutputStream(file);
					InputStream input = plugin == null ? plugin.getResourceAsStream(name) : plugin.getResourceAsStream(name)){
					ByteStreams.copy(input, output);
			}catch(IOException e){
				e.printStackTrace();
			}
		}

		try{
            conf = PROVIDER.load(file);
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	public void save(){
		try{
			PROVIDER.save(conf, file);
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	public void reload(){
		try{
            conf = PROVIDER.load(file);
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	public void update(){
		save();
		reload();
	}

}
