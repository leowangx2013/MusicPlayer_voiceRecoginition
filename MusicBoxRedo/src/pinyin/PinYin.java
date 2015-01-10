package pinyin;

import java.util.ArrayList;

public class PinYin {
 	public static String getPinYin(String input) {
		ArrayList<pinyin.HanziToPinyin.Token> tokens = HanziToPinyin.getInstance().get(input);
		StringBuilder sb = new StringBuilder();
		if (tokens != null && tokens.size() > 0) {
			for (pinyin.HanziToPinyin.Token token : tokens) {
				if (pinyin.HanziToPinyin.Token.PINYIN == token.type) {
					sb.append(token.target);
				} else {
					sb.append(token.source);
				}
			}
		}
		return sb.toString().toLowerCase();
	}
}
