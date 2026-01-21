package org.tsitle.demo_cli_app_critical_path.json;

import io.github.tsitle.criticalpath.CpmHourInterval;
import io.github.tsitle.criticalpath.CpmTimeUnit;
import org.jspecify.annotations.NonNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

public class AppConfig {
	public record Debugging(
				boolean debugMain,
				boolean debugCpgInternals,
				boolean debugCpgVerboseInternals,
				boolean debugCpcInternals,
				boolean debugCpcShowPath
			) {

		public @NonNull Map<@NonNull String, @NonNull String> asMap() {
			return new LinkedHashMap<>() {{
					put("Main", String.valueOf(debugMain));
					put("CPG", String.valueOf(debugCpgInternals));
					put("CPG Verbose", String.valueOf(debugCpgVerboseInternals));
					put("CPC", String.valueOf(debugCpcInternals));
					put("CPC Show Path", String.valueOf(debugCpcShowPath));
				}};
		}
	}

	public record Holiday(String templateStr, LocalDate date) { }

	public record OffDutyTimes(
				Set<DayOfWeek> workDays,
				Set<CpmHourInterval> workHours,
				Set<Holiday> holidays
			) {
		public @NonNull Set<@NonNull LocalDate> holidaysAsLocalDates() {
			List<LocalDate> tmpList = holidays.stream().map(Holiday::date).toList();
			Set<LocalDate> resSet = new TreeSet<>();
			tmpList.stream().sorted().forEach(resSet::add);
			return resSet;
		}

		public @NonNull Map<@NonNull String, @NonNull String> asMap() {
			return new LinkedHashMap<>() {{
					put("Work Days", workDays.toString());
					put("Work Hours", workHours.toString());
					put("Holidays", holidaysAsLocalDates().toString());
				}};
		}
	}

	public enum IndicesType { NUM, STR }

	public record InputDataBasic(
				int amountResourceUnits
			) {
		public @NonNull Map<@NonNull String, @NonNull String> asMap() {
			return new LinkedHashMap<>() {{
					put("Amount Resource Units", String.valueOf(amountResourceUnits));
				}};
		}
	}

	public record InputDataAdvanced(
				String filenameRunits,
				String filenameRgroups,
				String filenameAssocRunitsWithRgroups,
				String filenameAssocRgroupsWithTasks
			) {
		public @NonNull Map<@NonNull String, @NonNull String> asMap() {
			return new LinkedHashMap<>() {{
					put("Filename Runits", "\"" + escapeQuotes(filenameRunits) + "\"");
					put("Filename Rgroups", "\"" + escapeQuotes(filenameRgroups) + "\"");
					put("Filename AssocRunitsWithRgroups", "\"" + escapeQuotes(filenameAssocRunitsWithRgroups) + "\"");
					put("Filename AssocRgroupsWithTasks", "\"" + escapeQuotes(filenameAssocRgroupsWithTasks) + "\"");
				}};
		}
		private static String escapeQuotes(String str) {
			return str.replaceAll("\"", "\\\\\"");
		}
	}

	public record InputData(
				IndicesType indicesType,
				String filenameTasks,
				boolean useBasicInputData,
				InputDataBasic inputDataBasic,
				InputDataAdvanced inputDataAdvanced
			) {
		public @NonNull Map<@NonNull String, @NonNull String> asMap() {
			return new LinkedHashMap<>() {{
					put("Indices Type", indicesType.toString());
					put("Filename Tasks", "\"" + escapeQuotes(filenameTasks) + "\"");
					if (useBasicInputData) {
						put("Input Data (basic)", convertMapToString(inputDataBasic.asMap()));
					} else {
						put("Input Data (advanced)", convertMapToString(inputDataAdvanced.asMap()));
					}
				}};
		}

		private static String escapeQuotes(String str) {
			return str.replaceAll("\"", "\\\\\"");
		}

		private @NonNull String convertMapToString(@NonNull Map<@NonNull String, @NonNull String> map) {
			StringBuilder sb = new StringBuilder("[");
			Iterator<String> keyIterator = map.keySet().iterator();
			while (keyIterator.hasNext()) {
				String key = keyIterator.next();
				if (map.get(key) == null) {
					continue;
				}
				sb.append(key).append("=").append(map.get(key));
				if (keyIterator.hasNext()) {
					sb.append(", ");
				}
			}
			sb.append("]");
			return sb.toString();
		}
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	public Debugging debugging;
	public CpmTimeUnit timeUnit;
	public OffDutyTimes offDutyTimes;
	public InputData inputData;

	public @NonNull Map<@NonNull String, @NonNull Map<@NonNull String, @NonNull String>> asMap(boolean includeDebugInfo) {
		return new LinkedHashMap<>() {{
				if (includeDebugInfo) {
					put("Debugging", debugging.asMap());
				}
				put("Time Unit", new LinkedHashMap<>() {{ put("Unit", timeUnit.name()); }});
				put("Off-Duty Times", offDutyTimes.asMap());
				put("Input Data", inputData.asMap());
			}};
	}
}
