package org.tsitle.demo_cli_app_critical_path;

import io.github.tsitle.criticalpath.exceptions.InvalidInputDataException;
import org.jspecify.annotations.Nullable;
import org.tsitle.demo_cli_app_critical_path.json.AppConfig;
import org.tsitle.demo_cli_app_critical_path.json.Deserializer;
import picocli.CommandLine;

import java.io.*;

/**
 * Command line tool for using the Critical Path Method library
 */
@CommandLine.Command(
		name = "java -jar app_demo_critical_path_method.jar",
		description = "%nCommand line tool for using the Critical Path Method library%n"
	)
public class Main implements Runnable {
	@SuppressWarnings("unused")
	@CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
	private boolean doPrintHelp;

	@SuppressWarnings("unused")
	@CommandLine.Option(names = {"--output-html"})
	private @Nullable String outputHtmlFilename;

	@SuppressWarnings("unused")
	@CommandLine.Parameters(
			index = "0",
			description = "path to the JSON configuration file (may be prefixed with 'rsc:')"
		)
	private String configJsonFilename;

	public static void main(String[] args) {
		int resI = new CommandLine(new Main())
				.setExecutionExceptionHandler(new PrintExceptionMessageHandler())
				.execute(args);
		System.exit(resI);
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	@Override
	public void run() {
		final AppConfig appConfig;
		try {
			appConfig = Deserializer.readAppConfigFromFile(configJsonFilename);
		} catch (IOException e) {
			throw new RuntimeException("M: " + e.getMessage());
		} catch (InvalidInputDataException e) {
			throw new RuntimeException("M: InvalidInputDataException: " + e.getMessage());
		}

		final CliApp cliApp = new CliApp(appConfig, outputHtmlFilename);

		try {
			cliApp.start();
		} catch (IOException e) {
			throw new RuntimeException("M: " + e.getMessage());
		} catch (InvalidInputDataException e) {
			throw new RuntimeException("M: InvalidInputDataException: " + e.getMessage());
		}
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	private static class PrintExceptionMessageHandler implements CommandLine.IExecutionExceptionHandler {
		public int handleExecutionException(Exception ex, CommandLine cmd, CommandLine.ParseResult parseResult) {
			// bold red error message
			cmd.getErr().println(cmd.getColorScheme().errorText(ex.getMessage()));

			return cmd.getExitCodeExceptionMapper() != null
					? cmd.getExitCodeExceptionMapper().getExitCode(ex)
					: cmd.getCommandSpec().exitCodeOnExecutionException();
		}
	}
}
