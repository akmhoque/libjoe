package joe.util;


import java.io.StringWriter;

import com.google.common.base.Function;

/**
 * String utilities.
 * 
 * @author Joe Kearney
 */
public final class StringUtils {
	private StringUtils() {}
	
	public static final Function<String, String> UNESCAPE = new Function<String, String>() {
		@Override
		public String apply(String input) {
			return unescapeJava(input);
		}
	};

	/**
	 * Unescapes Java literals found in the <code>String</code>.
	 */
	public static String unescapeJava(String str) {
		if (str == null) {
			return null;
		}
		
		StringWriter writer = new StringWriter(str.length() + (str.length() / 50) + 1); // guesstimate
		int sz = str.length();

		for (int i = 0; i < sz; i++) {
			char ch = str.charAt(i);
			switch (ch) {
				case '\b':
					writer.write("\\b");
					break;
				case '\t':
					writer.write("\\t");
					break;
				case '\n':
					writer.write("\\n");
					break;
				case '\f':
					writer.write("\\f");
					break;
				case '\r':
					writer.write("\\r");
					break;
				default:
					writer.write(ch);
					break;
			}
		}
		return writer.toString();
	}
}
