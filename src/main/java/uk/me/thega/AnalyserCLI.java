package uk.me.thega;

import java.io.IOException;
import java.util.List;

import javax.naming.ConfigurationException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import uk.me.thega.file.RepoFileReader;


public class AnalyserCLI {

	final static String FILE_ARGUMENT = "file";

	final static String RESTRICTION_ARGUMENT = "restriction";

	final static String USERNAME_ARGUMENT = "username";

	final static String PASSWORD_ARGUMENT = "password";

	private CommandLine cmd;

	static Options getCLIOptions() {
		final Options options = new Options();
		options.addOption(FILE_ARGUMENT, true, "File to read repos from");
		options.addOption(RESTRICTION_ARGUMENT, false, "Artifact restriction");
		options.addOption(USERNAME_ARGUMENT, false, "Username for web requests (if needed)");
		options.addOption(PASSWORD_ARGUMENT, false, "Password for web requests (if needed)");
		return options;
	}

	public void parse(final String[] args) throws ConfigurationException, ParseException {
		// create the command line parser
		final CommandLineParser parser = new BasicParser();
		final Options options = getCLIOptions();

		cmd = parser.parse(options, args);
		for (final Object req : options.getRequiredOptions()) {
			final Option option = (Option) req;
			if (!cmd.hasOption(option.getArgName())) {
				final HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp(AnalyserCLI.class.getSimpleName(), options);
				throw new ConfigurationException("Required option was abscent: [" + option.getArgName() + "]");
			}
		}
	}

	public List<String> getRepoPaths() throws ConfigurationException, IOException {
		if (cmd == null) {
			throw new ConfigurationException("Cmd not initialised");
		}
		return RepoFileReader.getReposFromFile(cmd.getOptionValue(FILE_ARGUMENT));
	}

	public String getRestriction() throws ConfigurationException {
		if (cmd == null) {
			throw new ConfigurationException("Cmd not initialised");
		}
		if (cmd.hasOption(RESTRICTION_ARGUMENT)) {
			return cmd.getOptionValue(RESTRICTION_ARGUMENT);
		}
		return "";
	}

	public String getUsername() throws ConfigurationException {
		if (cmd == null) {
			throw new ConfigurationException("Cmd not initialised");
		}
		if (cmd.hasOption(USERNAME_ARGUMENT)) {
			return cmd.getOptionValue(USERNAME_ARGUMENT);
		}
		return null;
	}

	public String getPassword() throws ConfigurationException {
		if (cmd == null) {
			throw new ConfigurationException("Cmd not initialised");
		}
		if (cmd.hasOption(PASSWORD_ARGUMENT)) {
			return cmd.getOptionValue(PASSWORD_ARGUMENT);
		}
		return null;
	}
}
