package uk.me.thega;

import org.apache.commons.cli.MissingOptionException;
import org.junit.Assert;

import org.junit.Test;

/**
 * Test for the {@link AnalyserCLI} class.
 * 
 * @author pwhittlesea
 *
 */
public class AnalyserCLITest {

	/**
	 * Test that the file argument is detected.
	 * 
	 * @throws Exception the exception
	 */
	@Test
	public void testAllRequired() throws Exception {
		final String[] args = {
				"-" + AnalyserCLI.FILE_ARGUMENT, 
				"a"
		};
		final AnalyserCLI cli = new AnalyserCLI();
		cli.parse(args);

		Assert.assertEquals("Expected file to be 'a'", "a", cli.getFile());
	}

	/**
	 * Test that the restriction is detected.
	 * 
	 * @throws Exception the exception
	 */
	@Test
	public void testRestricitonProvided() throws Exception {
		final String[] args = {
				"-" + AnalyserCLI.FILE_ARGUMENT, 
				"a", 
				"-" + AnalyserCLI.RESTRICTION_ARGUMENT, 
				"uk.me.thega"
		};
		final AnalyserCLI cli = new AnalyserCLI();
		cli.parse(args);

		Assert.assertEquals("Expected restriction to be 'uk.me.thega'", "uk.me.thega", cli.getRestriction());
	}

	/**
	 * Test that the password is detected.
	 * 
	 * @throws Exception the exception
	 */
	@Test
	public void testPasswordProvided() throws Exception {
		final String[] args = {
				"-" + AnalyserCLI.FILE_ARGUMENT, 
				"a", 
				"-" + AnalyserCLI.PASSWORD_ARGUMENT, 
				"abcd"
		};
		final AnalyserCLI cli = new AnalyserCLI();
		cli.parse(args);

		Assert.assertEquals("Expected password to be 'abcd'", "abcd", cli.getPassword());
	}

	/**
	 * Test that the username is detected.
	 * 
	 * @throws Exception the exception
	 */
	@Test
	public void testUsernameProvided() throws Exception {
		final String[] args = {
				"-" + AnalyserCLI.FILE_ARGUMENT, 
				"a", 
				"-" + AnalyserCLI.USERNAME_ARGUMENT, 
				"pwhittlesea"
		};
		final AnalyserCLI cli = new AnalyserCLI();
		cli.parse(args);

		Assert.assertEquals("Expected password to be 'pwhittlesea'", "pwhittlesea", cli.getUsername());
	}

	/**
	 * Test that failing to specify an input file will cause an
	 * exception, specifically a {@link MissingOptionException}.
	 * 
	 * @throws Exception the exception
	 */
	@Test(expected = MissingOptionException.class)
	public void testMissingFile() throws Exception {
		final String[] args = {};
		final AnalyserCLI cli = new AnalyserCLI();
		cli.parse(args);
	}
}
