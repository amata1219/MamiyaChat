package amata1219.mamiya.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Converter {

	private static final String URL = "http://www.google.com/transliterate?langpair=ja-Hira|ja&text=";
	private static final Map<String, String[]> TABLE;

	static{
		Map<String, String[]> map = new HashMap<>();
		map.put("", toArray("あ","い","う","え","お"));
		map.put("k", toArray("か","き","く","け","こ"));
		map.put("s", toArray("さ","し","す","せ","そ"));
		map.put("t", toArray("た","ち","つ","て","と"));
		map.put("n", toArray("な","に","ぬ","ね","の"));
		map.put("h", toArray("は","ひ","ふ","へ","ほ"));
		map.put("m", toArray("ま","み","む","め","も"));
		map.put("y", toArray("や","い","ゆ","いぇ","よ"));
		map.put("r", toArray("ら","り","る","れ","ろ"));
		map.put("w", toArray("わ","うぃ","う","うぇ","を"));
		map.put("g", toArray("が","ぎ","ぐ","げ","ご"));
		map.put("z", toArray("ざ","じ","ず","ぜ","ぞ"));
		map.put("j", toArray("じゃ","じ","じゅ","じぇ","じょ"));
		map.put("d", toArray("だ","ぢ","づ","で","ど"));
		map.put("b", toArray("ば","び","ぶ","べ","ぼ"));
		map.put("p", toArray("ぱ","ぴ","ぷ","ぺ","ぽ"));
		map.put("gy", toArray("ぎゃ","ぎぃ","ぎゅ","ぎぇ","ぎょ"));
		map.put("gw", toArray("ぐぁ","ぐぃ","ぐぅ","ぐぇ","ぐぉ"));
		map.put("zy", toArray("じゃ","じぃ","じゅ","じぇ","じょ"));
		map.put("jy", toArray("じゃ","じぃ","じゅ","じぇ","じょ"));
		map.put("dy", toArray("ぢゃ","ぢぃ","ぢゅ","ぢぇ","ぢょ"));
		map.put("dh", toArray("でゃ","でぃ","でゅ","でぇ","でょ"));
		map.put("dw", toArray("どぁ","どぃ","どぅ","どぇ","どぉ"));
		map.put("by", toArray("びゃ","びぃ","びゅ","びぇ","びょ"));
		map.put("py", toArray("ぴゃ","ぴぃ","ぴゅ","ぴぇ","ぴょ"));
		map.put("v", toArray("ヴぁ","ヴぃ","ヴ","ヴぇ","ヴぉ"));
		map.put("vy", toArray("ヴゃ","ヴぃ","ヴゅ","ヴぇ","ヴょ"));
		map.put("sh", toArray("しゃ","し","しゅ","しぇ","しょ"));
		map.put("sy", toArray("しゃ","し","しゅ","しぇ","しょ"));
		map.put("c", toArray("か","し","く","せ","こ"));
		map.put("ch", toArray("ちゃ","ち","ちゅ","ちぇ","ちょ"));
		map.put("cy", toArray("ちゃ","ち","ちゅ","ちぇ","ちょ"));
		map.put("f", toArray("ふぁ","ふぃ","ふ","ふぇ","ふぉ"));
		map.put("fy", toArray("ふゃ","ふぃ","ふゅ","ふぇ","ふょ"));
		map.put("fw", toArray("ふぁ","ふぃ","ふ","ふぇ","ふぉ"));
		map.put("q", toArray("くぁ","くぃ","く","くぇ","くぉ"));
		map.put("ky", toArray("きゃ","きぃ","きゅ","きぇ","きょ"));
		map.put("kw", toArray("くぁ","くぃ","く","くぇ","くぉ"));
		map.put("ty", toArray("ちゃ","ちぃ","ちゅ","ちぇ","ちょ"));
		map.put("ts", toArray("つぁ","つぃ","つ","つぇ","つぉ"));
		map.put("th", toArray("てゃ","てぃ","てゅ","てぇ","てょ"));
		map.put("tw", toArray("とぁ","とぃ","とぅ","とぇ","とぉ"));
		map.put("ny", toArray("にゃ","にぃ","にゅ","にぇ","にょ"));
		map.put("hy", toArray("ひゃ","ひぃ","ひゅ","ひぇ","ひょ"));
		map.put("my", toArray("みゃ","みぃ","みゅ","みぇ","みょ"));
		map.put("ry", toArray("りゃ","りぃ","りゅ","りぇ","りょ"));
		map.put("l", toArray("ぁ","ぃ","ぅ","ぇ","ぉ"));
		map.put("x", toArray("ぁ","ぃ","ぅ","ぇ","ぉ"));
		map.put("ly", toArray("ゃ","ぃ","ゅ","ぇ","ょ"));
		map.put("lt", toArray("た","ち","っ","て","と"));
		map.put("lk", toArray("ヵ","き","く","ヶ","こ"));
		map.put("xy", toArray("ゃ","ぃ","ゅ","ぇ","ょ"));
		map.put("xt", toArray("た","ち","っ","て","と"));
		map.put("xk", toArray("ヵ","き","く","ヶ","こ"));
		map.put("wy", toArray("わ","ゐ","う","ゑ","を"));
		map.put("wh", toArray("うぁ","うぃ","う","うぇ","うぉ"));

		TABLE = Collections.unmodifiableMap(map);
	}

	public static boolean canConvert(String text){
		return text.matches("[ \\uFF61-\\uFF9F]+");
	}

	public static String convert(String text){
		return toKanaOrKanji(toHiragana(text));
	}

	public static String toKanaOrKanji(String text){
		if(text.isEmpty())
			return "";

		HttpURLConnection connection = null;
		BufferedReader reader = null;
		try{
			URL url = new URL(URL + URLEncoder.encode(text, "UTF-8"));
			connection = (HttpURLConnection) url.openConnection();

			connection.setRequestMethod("GET");
			connection.setInstanceFollowRedirects(false);

			connection.connect();
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
			StringBuilder builder = new StringBuilder();
			String line = "";
			while((line = reader.readLine()) != null){
				StringBuilder mBuilder = new StringBuilder();
				int mLine = 0, mIndex = 0;
				while(mIndex < line.length()){
					int start = 0, end = 0;
					if(mLine < 3){
						start = line.indexOf("[", mIndex);
						end = line.indexOf("]", mIndex);
						if(start == -1)
							break;

						if(start < end){
							mLine++;
							mIndex = start + 1;
						}else{
							mLine--;
							mIndex = end + 1;
						}
					}else{
						start = line.indexOf("\"", mIndex);
						end = line.indexOf("\"", start + 1);
						if(start == -1 || end == -1)
							break;

						mBuilder.append(line.substring(start, end + 1));
						int n = line.indexOf("[", end);
						if(n == -1)
							break;

						mLine--;
						mIndex = n + 1;
					}
				}
				builder.append(mBuilder.toString());
			}

			return builder.toString();
		}catch(MalformedURLException | ProtocolException e){
			e.printStackTrace();
		}catch(UnsupportedEncodingException e){
			e.printStackTrace();
		}catch(IOException e) {
			e.printStackTrace();
		}

		return "";
	}

	public static String toHiragana(String text){
		StringBuilder builder = new StringBuilder();
		String line = "";
		for(int i = 0; i < text.length(); i++){
			String tmp = text.substring(i, i + 1);
			switch(tmp){
			case "a":
				builder.append(getHiragana(line, 0));
				line = "";
				break;
			case "i":
				builder.append(getHiragana(line, 1));
				line = "";
				break;
			case "u":
				builder.append(getHiragana(line, 2));
				line = "";
				break;
			case "e":
				builder.append(getHiragana(line, 3));
				line = "";
				break;
			case "o":
				builder.append(getHiragana(line, 4));
				line = "";
				break;
			default:
				if(line.equals("n") && !line.equals("y")){
					builder.append("ん");
					line = "";
					if(tmp.equals("n"))
						continue;
				}
				char firstChar = tmp.charAt(0);
				if(Character.isLetter(firstChar)){
					if(Character.isUpperCase(firstChar)){
						builder.append(line + tmp);
						line = "";
					}else if(line.equals(tmp)){
						builder.append("っ");
						line = tmp;
					}else{
						line = line + tmp;
					}
				}else{
					switch(tmp){
					case ",":
						builder.append(line + "、");
						break;
					case ".":
						builder.append(line + "。");
						break;
					case "-":
						builder.append(line + "ー");
						break;
					case "[":
						builder.append(line + "「");
						break;
					case "]":
						builder.append(line + "」");
						break;
					case "!":
						builder.append(line + "！");
						break;
					case "?":
						builder.append(line + "？");
						break;
					default:
						break;
					}
					line = "";
				}
				break;
			}
		}
		return builder.append(line).toString();
	}

	private static String getHiragana(String s, int index){
		return TABLE.containsKey(s) ? TABLE.get(s)[index] : s + TABLE.get("")[index];
	}

	private static String[] toArray(String... args) {
		return args;
	}
}
