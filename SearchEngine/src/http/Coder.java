package http;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ZHS
 */
public class Coder {

	public static String escapeHTML(String string) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < string.length(); i++) {
			char ch = string.charAt(i);
			switch (ch) {
				case ' ':
					if (i > 0 && string.charAt(i - 1) == ' ')
						builder.append("&nbsp;");
					else
						builder.append(' ');
					break;
				case '&':
					builder.append("&amp;");
					break;
				case '<':
					builder.append("&lt;");
					break;
				case '>':
					builder.append("&gt;");
					break;
				case '"':
					builder.append("&quot;");
					break;
				case '\'':
					builder.append("&#x27;");
					break;
				case '/':
					builder.append("&#x2F;");
					break;
				case '\n':
					builder.append("<br/>");
					break;
				default:
					builder.append(ch);
					break;
			}
		}
		return builder.toString();
	}

	public static String encodeURL(String string) {
		try {
			return URLEncoder.encode(string, "UTF-8");
		}
		catch (UnsupportedEncodingException ex) {
			Logger.getLogger(Coder.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public static String decodeURL(String string) {
		try {
			return URLDecoder.decode(string, "UTF-8");
		}
		catch (UnsupportedEncodingException ex) {
			Logger.getLogger(Coder.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
}
