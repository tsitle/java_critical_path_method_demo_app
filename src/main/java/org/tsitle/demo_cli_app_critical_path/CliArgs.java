package org.tsitle.demo_cli_app_critical_path;

import io.github.tsitle.criticalpath.CpmHourInterval;
import io.github.tsitle.criticalpath.CpmTimeUnit;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class CliArgs {
	public record ArgsApp(
				boolean debugMain,
				boolean debugCpgInternals,
				boolean debugCpgVerboseInternals,
				boolean debugCpcInternals,
				boolean debugCpcShowPath,
				@Nullable String filenameOutput
			) {
		public ArgsApp {
			if (filenameOutput != null) {
				validateFn(getClass().getSimpleName(), "filenameOutput", filenameOutput);
			}
		}
		public Map<@NonNull String, @NonNull String> asMap() {
			return new LinkedHashMap<>() {{
					put("debugMain", String.valueOf(debugMain));
					put("debugCpgInternals", String.valueOf(debugCpgInternals));
					put("debugCpgVerboseInternals", String.valueOf(debugCpgVerboseInternals));
					put("debugCpcInternals", String.valueOf(debugCpcInternals));
					put("debugCpcShowPath", String.valueOf(debugCpcShowPath));
					put("filenameOutput", "\"" + (filenameOutput != null ? filenameOutput : "") + "\"");
				}};
		}
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	public record ArgsCommon(boolean indicesNumerical, @NonNull CpmTimeUnit timeUnit) {
		public Map<@NonNull String, @NonNull String> asMap() {
			return new LinkedHashMap<>() {{
					put("indicesNumerical", String.valueOf(indicesNumerical));
					put("timeUnit", timeUnit.name());
				}};
		}
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	public record ArgsGraph(@NonNull String filenameRawTasks) {
		public ArgsGraph {
			validateFn(getClass().getSimpleName(), "filenameRawTasks", filenameRawTasks);
		}
		public Map<@NonNull String, @NonNull String> asMap() {
			return new LinkedHashMap<>() {{
					put("filenameRawTasks", "\"" + filenameRawTasks + "\"");
				}};
		}
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	public record ArgsComputeBasic(int amountResourceUnits) {
		public ArgsComputeBasic {
			if (amountResourceUnits < 0) {
				throw new IllegalArgumentException(getClass().getSimpleName() + ".amountResourceUnits cannot be negative");
			}
		}
		public Map<@NonNull String, @NonNull String> asMap() {
			return new LinkedHashMap<>() {{
					put("amountResourceUnits", String.valueOf(amountResourceUnits));
				}};
		}
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	public record ArgsComputeAdvanced(
				@NonNull String filenameRawRunits,
				@NonNull String filenameRawRgroups,
				@NonNull String filenameRawAssocRunitsWithRgroups,
				@NonNull String filenameRawAssocRgroupsWithTasks
			) {
		public ArgsComputeAdvanced {
			validateFn(getClass().getSimpleName(), "filenameRawRunits", filenameRawRunits);
			validateFn(getClass().getSimpleName(), "filenameRawRgroups", filenameRawRgroups);
			validateFn(getClass().getSimpleName(), "filenameRawAssocRunitsWithRgroups", filenameRawAssocRunitsWithRgroups);
			validateFn(getClass().getSimpleName(), "filenameRawAssocRgroupsWithTasks", filenameRawAssocRgroupsWithTasks);
		}
		public Map<@NonNull String, @NonNull String> asMap() {
			return new LinkedHashMap<>() {{
					put("filenameRawRunits", "\"" + filenameRawRunits + "\"");
					put("filenameRawRgroups", "\"" + filenameRawRgroups + "\"");
					put("filenameRawAssocRunitsWithRgroups", "\"" + filenameRawAssocRunitsWithRgroups + "\"");
					put("filenameRawAssocRgroupsWithTasks", "\"" + filenameRawAssocRgroupsWithTasks + "\"");
				}};
		}
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	public record ArgsOffDutyTimes(
				@NonNull Set<DayOfWeek> workDays,
				@NonNull Set<CpmHourInterval> workHours,
				@NonNull Set<LocalDate> holidays
			) {
		public Map<@NonNull String, @NonNull String> asMap() {
			return new LinkedHashMap<>() {{
					put("workDays", workDays.toString());
					put("workHours", workHours.toString());
					put("holidays", holidays.toString());
				}};
		}
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	public record AllArgs(
				@NonNull ArgsApp argsApp,
				@NonNull ArgsCommon argsCommon,
				@NonNull ArgsGraph argsGraph,
				@Nullable ArgsComputeBasic argsComputeBasic,
				@Nullable ArgsComputeAdvanced argsComputeAdvanced,
				@NonNull ArgsOffDutyTimes argsOffDutyTimes
			) {
		public Map<@NonNull String, @NonNull Map<@NonNull String, @NonNull String>> asMap() {
			return new LinkedHashMap<>() {{
					put("App", argsApp.asMap());
					put("Common", argsCommon.asMap());
					put("Graph", argsGraph.asMap());
					if (argsComputeBasic != null) {
						put("Compute (basic)", argsComputeBasic.asMap());
					}
					if (argsComputeAdvanced != null) {
						put("Compute (advanced)", argsComputeAdvanced.asMap());
					}
					put("OffDutyTimes", argsOffDutyTimes.asMap());
				}};
		}
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	private static void validateFn(@NonNull String className, @NonNull String desc, @NonNull String fn) {
		if (fn.isBlank()) {
			throw new IllegalArgumentException("Filename " + className + "." + desc + " cannot be blank");
		}
	}
}
