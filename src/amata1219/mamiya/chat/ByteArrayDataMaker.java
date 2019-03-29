package amata1219.mamiya.chat;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class ByteArrayDataMaker {

	public static byte[] makeByteArrayDataOutput(String... strs){
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		for(String str : strs)
			out.writeUTF(str);
		return out.toByteArray();
	}

}
