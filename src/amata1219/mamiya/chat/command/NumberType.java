package amata1219.mamiya.chat.command;

import java.util.function.Consumer;

public enum NumberType {

	BYTE(s -> Byte.parseByte(s)),
	SHORT(s -> Short.parseShort(s)),
	INT(s -> Integer.parseInt(s)),
	LONG(s -> Long.parseLong(s)),
	FLOAT(s -> Float.parseFloat(s)),
	DOUBLE(s -> Double.parseDouble(s));

	private final Consumer<String> consumer;

	private NumberType(Consumer<String> consumer){
		this.consumer = consumer;
	}

	public Consumer<String> getChecker(){
		return consumer;
	}

}
