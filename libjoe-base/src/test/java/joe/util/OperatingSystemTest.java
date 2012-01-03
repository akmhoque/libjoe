package joe.util;


import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import joe.util.OperatingSystem;

import org.junit.Test;

public class OperatingSystemTest {
	@Test
	public void testWin7() {
		assertThat(OperatingSystem.parse("Windows 7"), is(OperatingSystem.WINDOWS));
	}
	@Test
	public void testWin2000() {
		assertThat(OperatingSystem.parse("Windows 2000"), is(OperatingSystem.WINDOWS));
	}
	@Test
	public void testWinXP() {
		assertThat(OperatingSystem.parse("Windows XP"), is(OperatingSystem.WINDOWS));
	}
	@Test
	public void testLinux() {
		assertThat(OperatingSystem.parse("Linux"), is(OperatingSystem.UNIX));
	}
	@Test
	public void testSolaris() {
		assertThat(OperatingSystem.parse("Solaris"), is(OperatingSystem.UNIX));
	}
	@Test
	public void testSunOS() {
		assertThat(OperatingSystem.parse("SunOS"), is(OperatingSystem.UNIX));
	}
	@Test
	public void testCurrentIsSensible() {
		assertThat(OperatingSystem.thisOperatingSystem(), notNullValue());
	}
}
