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
		name = "cpm",
		description = "%nCommand line tool for using the Critical Path Method library%n",
		versionProvider = Main.ManifestVersionProvider.class
	)
public class Main implements Runnable {
	@SuppressWarnings("unused")
	@CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
	private boolean doPrintHelp;

	@SuppressWarnings("unused")
	@CommandLine.Option(names = {"-V", "--version"}, versionHelp = true, description = "display version info")
	boolean doPrintVersion;

	@SuppressWarnings("unused")
	@CommandLine.Option(names = {"--output-html"}, description = "optional: write results to an HTML file")
	private @Nullable String outputHtmlFilename;

	@SuppressWarnings("unused")
	@CommandLine.Parameters(description = "path to the JSON configuration file (may be prefixed with 'rsc:')")
	private String configJsonFilename;

	private final static String APP_NAME = "cpm_demo";

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

	/**
	 * {@link CommandLine.IVersionProvider} implementation that returns version information from the {@code /META-INF/MANIFEST.MF} file.
	 */
	static class ManifestVersionProvider implements CommandLine.IVersionProvider {
		public String[] getVersion() throws Exception {
			java.util.Enumeration<java.net.URL> resources = Main.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
			while (resources.hasMoreElements()) {
				java.net.URL url = resources.nextElement();
				try {
					java.util.jar.Manifest manifest = new java.util.jar.Manifest(url.openStream());
					if (isApplicableManifest(manifest)) {
						java.util.jar.Attributes attr = manifest.getMainAttributes();
						return new String[] { get(attr, "Implementation-Title") + " version \"" +
								get(attr, "Implementation-Version") + "\"" };
					}
				} catch (IOException ex) {
					return new String[] { "Unable to read from " + url + ": " + ex };
				}
			}
			return new String[0];
		}

		private boolean isApplicableManifest(java.util.jar.Manifest manifest) {
			java.util.jar.Attributes attributes = manifest.getMainAttributes();
			return Main.APP_NAME.equals(get(attributes, "Implementation-Title"));
		}

		private static Object get(java.util.jar.Attributes attributes, String key) {
			return attributes.get(new java.util.jar.Attributes.Name(key));
		}
	}
}
