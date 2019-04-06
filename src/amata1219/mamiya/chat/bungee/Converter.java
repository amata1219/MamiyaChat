package amata1219.mamiya.chat.bungee;

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
		return text.getBytes().length == text.length() || !text.matches("[\\uFF61-\\uFF9F]+");
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
				StringBuilder buffer = new StringBuilder();
				int level = 0, index = 0;
				while(index < line.length()){
					if(level < 3){
						int start = line.indexOf("[", index);
						int end = line.indexOf("]", index);
						if(start == -1)
							break;

						if(start < end){
							level++;
							index = start + 1;
						}else{
							level--;
							index = end + 1;
						}
					}else{
						int start = line.indexOf("\"", index);
						int end = line.indexOf("\"", start + 1);
						if(start == -1 || end == -1)
							break;

						buffer.append(line.substring(start + 1, end));
						int next = line.indexOf("]", end);
						if(next == -1)
							break;

						level--;
						index = next + 1;
					}
				}
				builder.append(buffer.toString());
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
		String last = "";
		for(int i = 0; i < text.length(); i++){
			char tmp = text.charAt(i);
			switch(tmp){
			case 'a':
				builder.append(getHiragana(last, 0));
				last = "";
				break;
			case 'i':
				builder.append(getHiragana(last, 1));
				last = "";
				break;
			case 'u':
				builder.append(getHiragana(last, 2));
				last = "";
				break;
			case 'e':
				builder.append(getHiragana(last, 3));
				last = "";
				break;
			case 'o':
				builder.append(getHiragana(last, 4));
				last = "";
				break;
			case ' ':
				builder.append(" ");
				last = "";
				break;
			default:
				if(last.equals("n") && !last.equals("y")){
					builder.append("ん");
					last = "";
					if(tmp == 'n')
						continue;
				}

				if(Character.isLetter(tmp)){
					if(Character.isUpperCase(tmp)){
						builder.append(last + tmp);
						last = "";
					}else if(!last.isEmpty() && last.charAt(0) == tmp){
						builder.append("っ");
						last = String.valueOf(tmp);
					}else{
						last = last + tmp;
					}
				}else{
					switch(tmp){
					case ',':
						builder.append(last + "、");
						break;
					case '.':
						builder.append(last + "。");
						break;
					case '-':
						builder.append(last + "ー");
						break;
					case '[':
						builder.append(last + "「");
						break;
					case ']':
						builder.append(last + "」");
						break;
					case '!':
						builder.append(last + "！");
						break;
					case '?':
						builder.append(last + "？");
						break;
					default:
						continue;
					}
					last = "";
				}
			}
		}
		return builder.append(last).toString();
	}

	private static String getHiragana(String s, int index){
		return TABLE.containsKey(s) ? TABLE.get(s)[index] : s + TABLE.get("")[index];
	}

	private static String[] toArray(String... args) {
		return args;
	}
}
