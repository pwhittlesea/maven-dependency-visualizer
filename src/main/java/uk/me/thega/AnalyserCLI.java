package uk.me.thega;

import javax.naming.ConfigurationException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Command line parser for the Analyser application.
 * 
 * @author pwhittlesea
 *
 */
public class AnalyserCLI {

	/** The file argument the user must specify. */
	final static String FILE_ARGUMENT = "file";

	/** The restriction argument the user must specify. */
	final static String RESTRICTION_ARGUMENT = "restriction";

	/** The user name argument the user must specify. */
	final static String USERNAME_ARGUMENT = "username";

	/** The password argument the user must specify. */
	final static String PASSWORD_ARGUMENT = "password";

	/** The test argument the user must specify. */
	final static String TEST_ARGUMENT = "printTest";

	/** The parsed command line arguments. */
	private CommandLine cmd;

	/**
	 * Fetch the command line options that are available to the user.
	 * <br>
	 * For example:
	 * <table>
	 * <tr>
	 * <td>-f</td><td>file</td><td>File to read repos from</td>
	 * </tr>
	 * </table>
	 * 
	 * @return the {@link Options} for the application.
	 */
	static Options getCLIOptions() {
		final Options options = new Options();

		final Option file = new Option(FILE_ARGUMENT, true, "File to read repos from");
		final Option restriction = new Option(RESTRICTION_ARGUMENT, true, "Artifact restriction");
		final Option username = new Option(USERNAME_ARGUMENT, true, "Username for web requests (if needed)");
		final Option password = new Option(PASSWORD_ARGUMENT, true, "Password for web requests (if needed)");
		final Option test = new Option(TEST_ARGUMENT, true, "Include test dependencies");
		
		file.setRequired(true);

		options.addOption(file);
		options.addOption(restriction);
		options.addOption(username);
		options.addOption(password);
		options.addOption(test);

		return options;
	}

	/**
	 * Parse an array of strings for the arguments specified by {@link #getCLIOptions()}
	 * 
	 * @param args the arguments to parse
	 * @throws ConfigurationException thrown if the user fails to specify a required parameter
	 * @throws ParseException thrown if the input cannot be parsed
	 */
	public void parse(final String[] args) throws ConfigurationException, ParseException {
		// create the command line parser
		final CommandLineParser parser = new BasicParser();
		final Options options = getCLIOptions();

		try {
			cmd = parser.parse(options, args);
		} catch (final MissingOptionException e) {
			final HelpFormatter formatter = new HelpFormatter();
			formatter.setOptPrefix(" --");
			formatter.printHelp(AnalyserCLI.class.getSimpleName(), options);
			throw e;
		}
	}

	/**
	 * Get the command line and check it is not null
	 * 
	 * @return the command line
	 * @throws ConfigurationException if the cmd is null
	 */
	private CommandLine getCommandLine() throws ConfigurationException {
		if (cmd == null) {
			throw new ConfigurationException("Cmd not initialised");
		}
		return cmd;
	}

	/**
	 * Get the path of the file containing the repos to scan.
	 * 
	 * @return the string of the file passed to the application
	 * @throws ConfigurationException if cmd is null
	 */
	public String getFile() throws ConfigurationException {
		final CommandLine cmd = getCommandLine();
		return cmd.getOptionValue(FILE_ARGUMENT);
	}

	/**
	 * Get the restriction on artifacts specified by the user
	 * 
	 * @return the restriction
	 * @throws ConfigurationException if cmd is null
	 */
	public String getRestriction() throws ConfigurationException {
		final CommandLine cmd = getCommandLine();
		if (cmd.hasOption(RESTRICTION_ARGUMENT)) {
			return cmd.getOptionValue(RESTRICTION_ARGUMENT);
		}
		return "";
	}

	/**
	 * Get the HTTP auth username
	 * 
	 * @return the HTTP auth username
	 * @throws ConfigurationException if cmd is null
	 */
	public String getUsername() throws ConfigurationException {
		final CommandLine cmd = getCommandLine();
		if (cmd.hasOption(USERNAME_ARGUMENT)) {
			return cmd.getOptionValue(USERNAME_ARGUMENT);
		}
		return null;
	}

	/**
	 * Get the HTTP auth password
	 * 
	 * @return the HTTP auth password
	 * @throws ConfigurationException if cmd is null
	 */
	public String getPassword() throws ConfigurationException {
		final CommandLine cmd = getCommandLine();
		if (cmd.hasOption(PASSWORD_ARGUMENT)) {
			return cmd.getOptionValue(PASSWORD_ARGUMENT);
		}
		return null;
	}

	/**
	 * Does the user want test dependencies?
	 * 
	 * @return if we want test dependencies
	 * @throws ConfigurationException if cmd is null
	 */
	public boolean getTestPreference() throws ConfigurationException {
		final CommandLine cmd = getCommandLine();
		if (cmd.hasOption(TEST_ARGUMENT)) {
			return (cmd.getOptionValue(TEST_ARGUMENT).equals("true")) ? true : false;
		}
		return false;
	}
}
