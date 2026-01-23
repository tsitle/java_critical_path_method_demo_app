package org.tsitle.demo_cli_app_critical_path.json;

import io.github.tsitle.criticalpath.CpmHourInterval;
import io.github.tsitle.criticalpath.CpmTimeUnit;
import io.github.tsitle.criticalpath.exceptions.InvalidInputDataException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

public record AppConfig(
			@NonNull Debugging debugging,
			@NonNull CpmTimeUnit timeUnit,
			@NonNull OffDutyTimes offDutyTimes,
			@NonNull InputData inputData
		) {

	public record Debugging(
				boolean debugMain,
				@Nullable Boolean debugVerboseMain,
				boolean debugCpgInternals,
				@Nullable Boolean debugCpgVerboseInternals,
				boolean debugCpcInternals,
				@Nullable Boolean debugCpcVerboseInternals
			) {

		public @NonNull Map<@NonNull String, @NonNull String> asMap() {
			return new LinkedHashMap<>() {{
					put("Main", String.valueOf(debugMain));
					put("Main Verbose", String.valueOf(debugVerboseMain != null ? debugVerboseMain : false));
					put("CPG", String.valueOf(debugCpgInternals));
					put("CPG Verbose", String.valueOf(debugCpgVerboseInternals != null ? debugCpgVerboseInternals : false));
					put("CPC", String.valueOf(debugCpcInternals));
					put("CPC Verbose", String.valueOf(debugCpcVerboseInternals != null ? debugCpcVerboseInternals : false));
				}};
		}
	}

	public record Holiday(@NonNull String templateStr, @NonNull LocalDate date) { }

	public record OffDutyTimes(
				@NonNull Set<@NonNull DayOfWeek> workDays,
				@NonNull Set<@NonNull CpmHourInterval> workHours,
				@NonNull Set<@NonNull Holiday> holidays
			) {
		@SuppressWarnings("ConstantValue")
		public @NonNull Set<@NonNull LocalDate> holidaysAsLocalDates() {
			Set<LocalDate> resSet = new TreeSet<>();
			if (holidays != null) {
				List<LocalDate> tmpList = holidays.stream()
						.filter(Objects::nonNull)
						.map(Holiday::date)
						.toList();
				tmpList.stream().sorted().forEach(resSet::add);
			}
			return resSet;
		}

		@SuppressWarnings("ConstantValue")
		public @NonNull Map<@NonNull String, @NonNull String> asMap() {
			return new LinkedHashMap<>() {{
					if (workDays != null) {
						put("Work Days", workDays.toString());
					}
					if (workHours != null) {
						put("Work Hours", workHours.toString());
					}
					if (holidays != null) {
						put("Holidays", holidaysAsLocalDates().toString());
					}
				}};
		}

		@SuppressWarnings("ConstantValue")
		public void validate() throws InvalidInputDataException {
			final String prefix = "AppConfig.OffDutyTimes.";
			if (workDays == null) {
				throw new InvalidInputDataException(prefix + "workDays may not be null");
			}
			for (DayOfWeek dayOfWeek : workDays) {
				if (dayOfWeek == null) {
					throw new InvalidInputDataException(prefix + "workDays may not contain null elements");
				}
			}
			if (workHours == null) {
				throw new InvalidInputDataException(prefix + "workHours may not be null");
			}
			for (CpmHourInterval interval : workHours) {
				if (interval == null) {
					throw new InvalidInputDataException(prefix + "workHours may not contain null elements");
				}
			}
			if (holidays == null) {
				throw new InvalidInputDataException(prefix + "holidays may not be null");
			}
			for (Holiday holiday : holidays) {
				if (holiday == null) {
					throw new InvalidInputDataException(prefix + "holidays may not contain null elements");
				}
			}
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

		public void validate(boolean useBasicInputData) throws InvalidInputDataException {
			final String prefix = "AppConfig.InputData.InputDataBasic.";
			if (useBasicInputData && amountResourceUnits < 1) {
				throw new InvalidInputDataException(prefix + "amountResourceUnits must be >= 1");
			}
		}
	}

	public record InputDataAdvanced(
				@NonNull String filenameRunits,
				@NonNull String filenameRgroups,
				@NonNull String filenameAssocRunitsWithRgroups,
				@NonNull String filenameAssocRgroupsWithTasks
			) {
		@SuppressWarnings("ConstantValue")
		public @NonNull Map<@NonNull String, @NonNull String> asMap() {
			return new LinkedHashMap<>() {{
					if (filenameRunits != null) {
						put("Filename Runits", "\"" + escapeQuotes(filenameRunits) + "\"");
					}
					if (filenameRgroups != null) {
						put("Filename Rgroups", "\"" + escapeQuotes(filenameRgroups) + "\"");
					}
					if (filenameAssocRunitsWithRgroups != null) {
						put("Filename AssocRunitsWithRgroups", "\"" + escapeQuotes(filenameAssocRunitsWithRgroups) + "\"");
					}
					if (filenameAssocRgroupsWithTasks != null) {
						put("Filename AssocRgroupsWithTasks", "\"" + escapeQuotes(filenameAssocRgroupsWithTasks) + "\"");
					}
				}};
		}

		private static String escapeQuotes(String str) {
			return str.replaceAll("\"", "\\\\\"");
		}

		private static void validateFn(boolean useBasicInputData, @Nullable String fn, String fieldName)
				throws InvalidInputDataException {
			final String prefix = "AppConfig.InputData.InputDataAdvanced.";
			if (fn == null) {
				throw new InvalidInputDataException(prefix + fieldName + " may not be null");
			}
			if (! useBasicInputData && fn.isBlank()) {
				throw new InvalidInputDataException(prefix + fieldName + " may not be blank");
			}
		}

		public void validate(boolean useBasicInputData) throws InvalidInputDataException {
			validateFn(useBasicInputData, filenameRunits, "filenameRunits");
			validateFn(useBasicInputData, filenameRgroups, "filenameRgroups");
			validateFn(useBasicInputData, filenameAssocRunitsWithRgroups, "filenameAssocRunitsWithRgroups");
			validateFn(useBasicInputData, filenameAssocRgroupsWithTasks, "filenameAssocRgroupsWithTasks");
		}
	}

	public record InputData(
				@NonNull IndicesType indicesType,
				@NonNull String filenameTasks,
				boolean useBasicInputData,
				@NonNull InputDataBasic inputDataBasic,
				@NonNull InputDataAdvanced inputDataAdvanced
			) {
		@SuppressWarnings("ConstantValue")
		public @NonNull Map<@NonNull String, @NonNull String> asMap() {
			return new LinkedHashMap<>() {{
					if (indicesType != null) {
						put("Indices Type", indicesType.toString());
					}
					if (filenameTasks != null) {
						put("Filename Tasks", "\"" + escapeQuotes(filenameTasks) + "\"");
					}
					if (useBasicInputData && inputDataBasic != null) {
						put("Input Data (basic)", convertMapToString(inputDataBasic.asMap()));
					} else if (! useBasicInputData && inputDataAdvanced != null) {
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

		@SuppressWarnings("ConstantValue")
		public void validate() throws InvalidInputDataException {
			final String prefix = "AppConfig.InputData.";
			if (indicesType == null) {
				throw new InvalidInputDataException(prefix + "indicesType may not be null");
			}

			if (filenameTasks == null) {
				throw new InvalidInputDataException(prefix + "filenameTasks may not be null");
			}
			if (filenameTasks.isBlank()) {
				throw new InvalidInputDataException(prefix + "filenameTasks may not be blank");
			}

			if (inputDataBasic == null) {
				throw new InvalidInputDataException(prefix + "inputDataBasic may not be null");
			}
			inputDataBasic.validate(useBasicInputData);

			if (inputDataAdvanced == null) {
				throw new InvalidInputDataException(prefix + "inputDataAdvanced may not be null");
			}
			inputDataAdvanced.validate(useBasicInputData);
		}
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * Constructor used by GSON during deserialization
	 */
	@SuppressWarnings({"DataFlowIssue", "unused"})
	public AppConfig() {
		this(null, null, null, null);
	}

	@SuppressWarnings("ConstantValue")
	public @NonNull Map<@NonNull String, @NonNull Map<@NonNull String, @NonNull String>> asMap(boolean includeDebugInfo) {
		return new LinkedHashMap<>() {{
				if (includeDebugInfo && debugging != null) {
					put("Debugging", debugging.asMap());
				}
				if (timeUnit != null) {
					put("Time Unit", new LinkedHashMap<>() {{ put("Unit", timeUnit.name()); }});
				}
				if (offDutyTimes != null) {
					put("Off-Duty Times", offDutyTimes.asMap());
				}
				if (inputData != null) {
					put("Input Data", inputData.asMap());
				}
			}};
	}

	@SuppressWarnings("ConstantValue")
	public void validate() throws InvalidInputDataException {
		final String prefix = "AppConfig.";
		if (debugging == null) {
			throw new InvalidInputDataException(prefix + "debugging may not be null");
		}

		if (timeUnit == null) {
			throw new InvalidInputDataException(prefix + "timeUnit may not be null");
		}

		if (offDutyTimes == null) {
			throw new InvalidInputDataException(prefix + "offDutyTimes may not be null");
		}
		offDutyTimes.validate();

		if (inputData == null) {
			throw new InvalidInputDataException(prefix + "inputData may not be null");
		}
		inputData.validate();
	}
}
