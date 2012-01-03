package joe.util;


import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import joe.util.StringUtils;

import org.junit.Test;

public class StringUnescapeTest {
	private static String windowsNewline = "\r\f";
	private static String unixNewline = "\n";
	
	@Test
	public void testUnescapeWindowsNewline() throws Exception {
		String unescaped = StringUtils.unescapeJava(windowsNewline);
		assertThat(unescaped.length(), is(4));
		assertThat(unescaped, is("\\r\\f"));
	}
	@Test
	public void testUnescapeUnixNewline() throws Exception {
		String unescaped = StringUtils.unescapeJava(unixNewline);
		assertThat(unescaped.length(), is(2));
		assertThat(unescaped, is("\\n"));
	}
}
