package org.tsitle.demo_cli_app_critical_path;

import io.github.tsitle.criticalpath.CpmHourInterval;
import io.github.tsitle.criticalpath.exceptions.InvalidInputDataException;
import io.github.tsitle.criticalpath.CpmTimeUnit;

import java.io.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Command line tool for using the Critical Path Method
 */
public class Main {
	private static final boolean DEBUG_MAIN = true;
	private static final boolean DEBUG_CPG_INTERNALS = false;
	private static final boolean DEBUG_CPG_VERBOSE_INTERNALS = false;
	private static final boolean DEBUG_CPC_INTERNALS = true;
	private static final boolean DEBUG_CPC_SHOW_PATH = false;
	private static final CpmTimeUnit DEFAULT_TIME_UNIT = CpmTimeUnit.DAYS;
	private static final Set<DayOfWeek> DEFAULT_WORK_DAYS = new LinkedHashSet<>() {{
			add(DayOfWeek.MONDAY);
			add(DayOfWeek.TUESDAY);
			add(DayOfWeek.WEDNESDAY);
			add(DayOfWeek.THURSDAY);
			add(DayOfWeek.FRIDAY);
		}};
	private static final Set<CpmHourInterval> DEFAULT_WORK_HOURS = new LinkedHashSet<>() {{
			add(new CpmHourInterval(8, 12));
			add(new CpmHourInterval(13, 17));
		}};
	private static final Set<LocalDate> DEFAULT_HOLIDAYS = new LinkedHashSet<>() {{
			// add dynamically generated holiday
			add(LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonth(), LocalDate.now().getDayOfMonth()).plusDays(2));
			add(LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonth().plus(1), 10));
			// add fixed holidays
			add(LocalDate.of(LocalDate.now().getYear(), 12, 25));
			add(LocalDate.of(LocalDate.now().getYear(), 12, 26));
			add(LocalDate.of(LocalDate.now().getYear() + 1, 1, 1));
		}};

	public static void main(String[] args) {
		int adjustedArgsLen = args.length;
		if ((adjustedArgsLen == 1 && args[0].equals("help")) ||
				(adjustedArgsLen >= 1 && ! (args[0].equalsIgnoreCase("NUM") || args[0].equalsIgnoreCase("STR"))) ||
				(adjustedArgsLen != 3 && adjustedArgsLen != 4 && adjustedArgsLen != 6 && adjustedArgsLen != 7)) {
			System.err.println("M: Basic usage:    <NUM|STR> <input-file-tasks> <amountResourceUnits> [of=<output-html-filename>]");
			System.err.println("M: Advanced usage: <NUM|STR>");
			System.err.println("                   <input-file-tasks> <input-file-runits> <input-file-rgroups> ");
			System.err.println("                   <input-file-assoc-runits-with-rgroups> <input-file-assoc-rgroups-with-tasks>");
			System.err.println("                   [of=<output-html-filename>]");
			System.err.println();
			System.err.println("M: The argument <NUM|STR> indicates the data type of all indices in the input files.");
			System.err.println("M:   'NUM' for numerical indices (0-" + Long.MAX_VALUE + ") and 'STR' for alphanumeric indices (e.g. UUIDs).");
			System.err.println();
			System.err.println("M: If an input file is in the Java resources folder, " +
					"then the prefix 'rsc:' can be used for the filename.");
			System.err.println("M:   E.g.: 'rsc:some-file.csv'");
			System.err.println();
			System.err.println("M: If an output filename has been specified, the results will be written to that file.");
			System.err.println("M:   E.g.: 'of=output.html'");
			System.exit(1);
		}
		System.out.println("M: Command line arguments:");
		Arrays.stream(args).forEach(s -> System.out.println("M:   - [" + s + "]"));

		final CliApp cliApp;

		int argIx = 0;

		final boolean argIndicesNum = args[argIx++].equalsIgnoreCase("NUM");
		final String argFilenameOutput = (adjustedArgsLen == 4 || adjustedArgsLen == 7 ? args[adjustedArgsLen - 1] : null);
		if (argFilenameOutput != null && ! argFilenameOutput.startsWith("of=")) {
			System.err.println("M: Output filename must start with 'of='");
			System.exit(1);
		}
		if (argFilenameOutput != null) {
			--adjustedArgsLen;
		}
		CliArgs.ArgsApp argsApp = new CliArgs.ArgsApp(
				DEBUG_MAIN,
				DEBUG_CPG_INTERNALS,
				DEBUG_CPG_VERBOSE_INTERNALS,
				DEBUG_CPC_INTERNALS,
				DEBUG_CPC_SHOW_PATH,
				argFilenameOutput != null ? argFilenameOutput.substring(3) : null
			);
		CliArgs.ArgsOffDutyTimes argsOffDuty = new CliArgs.ArgsOffDutyTimes(
				DEFAULT_WORK_DAYS,
				DEFAULT_WORK_HOURS,
				DEFAULT_HOLIDAYS
			);
		CliArgs.ArgsCommon argsCommon = new CliArgs.ArgsCommon(
				argIndicesNum,
				DEFAULT_TIME_UNIT
			);

		final String argFilenameRawTasks = args[argIx++];
		CliArgs.ArgsGraph argsGraph = new CliArgs.ArgsGraph(argFilenameRawTasks);

		CliArgs.ArgsComputeBasic argsComputeBasic = null;
		CliArgs.ArgsComputeAdvanced argsComputeAdvanced = null;
		if (adjustedArgsLen == 3) {
			try {
				Integer.parseInt(args[argIx]);
			} catch (NumberFormatException e) {
				System.err.println("M: Argument <amountResourceUnits> must be an integer");
				System.exit(1);
			}
			final int argAmountRunits = Integer.parseInt(args[argIx]);
			argsComputeBasic = new CliArgs.ArgsComputeBasic(argAmountRunits);
		} else {
			final String argFilenameRunits = args[argIx++];
			final String argFilenameRgroups = args[argIx++];
			final String argFilenameAssocRunitsWithRgroups = args[argIx++];
			final String argFilenameAssocRgroupsWithTasks = args[argIx];
			argsComputeAdvanced = new CliArgs.ArgsComputeAdvanced(
					argFilenameRunits,
					argFilenameRgroups,
					argFilenameAssocRunitsWithRgroups,
					argFilenameAssocRgroupsWithTasks
				);
		}
		final CliArgs.AllArgs argsAll = new CliArgs.AllArgs(
				argsApp,
				argsCommon,
				argsGraph,
				argsComputeBasic,
				argsComputeAdvanced,
				argsOffDuty
			);
		cliApp = new CliApp(argsAll);

		try {
			cliApp.start();
		} catch (IOException e) {
			System.err.println("M: " + e.getMessage());
			System.exit(1);
		} catch (InvalidInputDataException e) {
			System.err.println("M: InvalidInputDataException: " + e.getMessage());
			System.exit(1);
		}
	}
}
