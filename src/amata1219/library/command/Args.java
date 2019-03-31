package amata1219.library.command;

public class Args {

	private final String[] args;
	private int index = 0;

	public Args(String[] args){
		this.args = args;
	}

	public int length(){
		return args.length;
	}

	public String get(int index){
		return index < args.length ? args[index] : "";
	}

	public String get(int start, int end){
		String text = get(start);
		while(start < end)
			text += get(start++);
		return text;
	}

	public boolean hasNext(){
		return index + 1 < args.length;
	}

	public String next(){
		return hasNext() ? args[index++] : "";
	}

	public boolean isNumber(NumberType type){
		if(type == null || !hasNext())
			return false;

		try{
			type.getChecker().accept(args[index + 1]);
		}catch(NumberFormatException e){
			return false;
		}
		return true;
	}

	public byte nextByte(){
		return hasNext() ? Byte.parseByte(next()) : -1;
	}

	public short nextShort(){
		return hasNext() ? Short.parseShort(next()) : -1;
	}

	public int nextInt(){
		return hasNext() ? Integer.parseInt(next()) : -1;
	}

	public long nextLong(){
		return hasNext() ? Long.parseLong(next()) : -1;
	}

	public float nextFloat(){
		return hasNext() ? Float.parseFloat(next()) : -1;
	}

	public double nextDouble(){
		return hasNext() ? Double.parseDouble(next()) : -1;
	}

}
