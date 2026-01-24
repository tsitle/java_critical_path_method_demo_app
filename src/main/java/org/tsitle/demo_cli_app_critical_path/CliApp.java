package org.tsitle.demo_cli_app_critical_path;

import io.github.tsitle.criticalpath.internaldata.ConvertRawToInternalDataForCompute;
import io.github.tsitle.criticalpath.rawdata.filereaders.*;
import io.github.tsitle.criticalpath.CpmTimeUnit;
import io.github.tsitle.criticalpath.CriticalPathCompute;
import io.github.tsitle.criticalpath.CriticalPathGraph;
import io.github.tsitle.criticalpath.cpmresults.CpmResult;
import io.github.tsitle.criticalpath.cpmresults.CpmSubResultRgroup;
import io.github.tsitle.criticalpath.cpmresults.CpmSubResultRunit;
import io.github.tsitle.criticalpath.cpmresults.CpmSubResultTask;
import io.github.tsitle.criticalpath.exceptions.InvalidInputDataException;
import io.github.tsitle.criticalpath.internaldata.ConvertRawToInternalDataForGraph;
import io.github.tsitle.criticalpath.rawdata.RawDataForCompute;
import io.github.tsitle.criticalpath.rawdata.RawDataForGraph;
import io.github.tsitle.criticalpath.rawdata.containers.BaseRawData;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.tsitle.demo_cli_app_critical_path.html.CpmResultsToHtml;
import org.tsitle.demo_cli_app_critical_path.json.AppConfig;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

/**
 * Command line tool for using the Critical Path Method
 */
public final class CliApp {
	private final String CLASS_NAME = getClass().getSimpleName();
	private final @NonNull AppConfig appConfig;
	private final @NonNull String filenameHtmlOutput;

	private final RawDataForGraph inputRawDataForGraph = new RawDataForGraph();
	private final RawDataForCompute inputRawDataForCompute = new RawDataForCompute();

	private final LocalDateTime presentDateTime;

	public CliApp(@NonNull AppConfig appConfig, @Nullable String filenameHtmlOutput) {
		this.appConfig = appConfig;
		this.filenameHtmlOutput = (filenameHtmlOutput == null ? "" : filenameHtmlOutput);

		presentDateTime = determinePresentDateTime();
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	public void start() throws IOException, InvalidInputDataException {
		final boolean debugMain = appConfig.debugging().debugMain();
		final boolean debugVerboseMain = (debugMain && Objects.equals(true, appConfig.debugging().debugVerboseMain()));
		final boolean debugMainOrCpg = (debugMain || appConfig.debugging().debugCpgInternals());
		final boolean debugMainOrCpgOrCpc = (debugMainOrCpg || appConfig.debugging().debugCpcInternals());

		// read input files
		readInputData();

		if (debugVerboseMain) {
			debugOutput(CLASS_NAME + ": Raw graph:");
			inputRawDataForGraph.tasks.forEach((item) -> debugOutput(CLASS_NAME + ":   - " + item));
		}

		// create graph
		if (debugMainOrCpg) {
			debugOutput(CLASS_NAME + ": Create CPM graph...");
		}
		CriticalPathGraph cpGraph = buildCriticalPathGraph(inputRawDataForGraph);

		if (debugVerboseMain) {
			cpGraph.printGraph(this::debugOutput, true);

			debugOutput(CLASS_NAME + ": Initial critical path:");
			cpGraph.printCriticalPath(this::debugOutput);
		}

		// compute CPM results
		if (debugMainOrCpgOrCpc) {
			debugOutput(CLASS_NAME + ": Compute CPM results...");
		}
		final CpmResult cpmResult = computeCpmResult(cpGraph);

		// print results
		printResults(cpmResult);

		// output results as an HTML file
		if (filenameHtmlOutput.isEmpty()) {
			defaultOutput("(not writing results to HTML file)");
			return;
		}
		defaultOutput("Writing results to HTML file '" + filenameHtmlOutput + "'");
		CpmResultsToHtml cpmResultsToHtml = buildCpmResultsToHtml(cpmResult, filenameHtmlOutput);
		cpmResultsToHtml.output();
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	private void defaultOutput(String msg) {
		System.out.println(CLASS_NAME + ": " + msg);
	}

	private void debugOutput(String msg) {
		System.out.println(CLASS_NAME + "_DEBUG: " + msg);
	}

	// -----------------------------------------------------------------------------------------------------------------

	private @NonNull CpmResult computeCpmResult(@NonNull CriticalPathGraph criticalPathGraph) {
		final CriticalPathCompute cpCompute;
		if (appConfig.inputData().useBasicInputData()) {
			cpCompute = buildCriticalPathComputeBasic(criticalPathGraph);
		} else {
			cpCompute = buildCriticalPathComputeAdvanced(criticalPathGraph);
		}

		//
		return cpCompute.computeCpmResults();
	}

	private void printResults(@NonNull CpmResult cpmResult) {
		defaultOutput("Full results:");

		final String timeUnitLabel = appConfig.timeUnit().getLabel();

		defaultOutput("  Tasks:");
		for (CpmSubResultTask tempResultsTask : cpmResult.resultsTasks()) {
			@SuppressWarnings("StringBufferReplaceableByString") StringBuilder sb = new StringBuilder();
			sb.append("    - Task id=").append(tempResultsTask.id());
			sb.append(", extId=").append(tempResultsTask.externalId());
			sb.append(", started=").append(tempResultsTask.timeStarted()).append(timeUnitLabel)
					.append(", finished=").append(tempResultsTask.timeFinished()).append(timeUnitLabel)
					.append(", runitId=").append(tempResultsTask.idOfRunitThatFinishedTask());
			defaultOutput(sb.toString());
		}

		defaultOutput("  Resource Groups:");
		for (CpmSubResultRgroup tempResultsRgroup : cpmResult.resultsRgroups()) {
			@SuppressWarnings("StringBufferReplaceableByString") StringBuilder sb = new StringBuilder();
			sb.append("    - Resource Group id=").append(tempResultsRgroup.id());
			sb.append(", extId=").append(tempResultsRgroup.externalId())
					.append(", timeIdled=").append(tempResultsRgroup.timeIdled()).append(timeUnitLabel)
					.append(", timeBusy=").append(tempResultsRgroup.timeBusy()).append(timeUnitLabel)
					.append(", maxRunitsUsed=").append(tempResultsRgroup.maxRunitsUsed());
			defaultOutput(sb.toString());
			for (CpmSubResultRunit tempResultsRunit : tempResultsRgroup.resultsRunits()) {
				@SuppressWarnings("StringBufferReplaceableByString") StringBuilder sb2 = new StringBuilder();
				sb2.append("      - Resource Unit id=").append(tempResultsRunit.id());
				sb2.append(", extId=").append(tempResultsRunit.externalId())
						.append(", timeIdled=").append(tempResultsRunit.timeIdled()).append(timeUnitLabel)
						.append(", timeBusy=").append(tempResultsRunit.timeBusy()).append(timeUnitLabel)
						.append(", tasksCompleted=").append(tempResultsRunit.tasksCompleted());
				defaultOutput(sb2.toString());
			}
		}

		defaultOutput("  Overview:");
		defaultOutput("    - Tasks                    " + inputRawDataForGraph.tasks.size());
		defaultOutput("    - Resource Groups          " + inputRawDataForCompute.rgroups.size());
		defaultOutput("    - Resource Units           " + inputRawDataForCompute.runits.size());
		defaultOutput("    - Minimum time requirement " + cpmResult.timePassed() + " " +
				appConfig.timeUnit().toString().toLowerCase());
	}

	// -----------------------------------------------------------------------------------------------------------------

	private <T extends BaseRawData> void readInputDataForContainerType(
				String filename,
				Set<T> outputSet,
				AbstractReadRawData<?, T> reader
			) throws IOException, InvalidInputDataException {
		if (appConfig.debugging().debugMain()) {
			debugOutput(CLASS_NAME + ": Read raw data file '" + filename + "'...");
		}
		if (! filename.startsWith("rsc:")) {
			Path path = Paths.get(filename).normalize();
			File file = path.toFile();
			try (InputStream is = new FileInputStream(file)) {  // throws FileNotFoundException
				reader.readFromStream(is, outputSet);
			} catch (FileNotFoundException e) {
				throw new IOException("file '" + filename + "' not found (absolute path '" + path.toAbsolutePath() + "')");
			}
		} else {
			reader.readFromResourcesFile(filename.substring(4), outputSet);
		}
	}

	private void readInputData() throws IOException, InvalidInputDataException {
		String currentFilename = "----";
		try {
			final boolean areIndicesNumerical = (appConfig.inputData().indicesType() == AppConfig.IndicesType.NUM);

			currentFilename = appConfig.inputData().filenameTasks();
			final AbstractReadRawDataTasks<?> rrg1 =
					(areIndicesNumerical ? new ReadRawTasksOfIdLong() : new ReadRawTasksOfIdString());
			readInputDataForContainerType(currentFilename, inputRawDataForGraph.tasks, rrg1);

			if (! appConfig.inputData().useBasicInputData()) {
				currentFilename = appConfig.inputData().inputDataAdvanced().filenameRunits();
				final AbstractReadRawDataRunits<?> rrg2 =
						(areIndicesNumerical ? new ReadRawRunitsOfIdLong() : new ReadRawRunitsOfIdString());
				readInputDataForContainerType(currentFilename, inputRawDataForCompute.runits, rrg2);

				currentFilename = appConfig.inputData().inputDataAdvanced().filenameRgroups();
				final AbstractReadRawDataRgroups<?> rrg3 =
						(areIndicesNumerical ? new ReadRawRgroupsOfIdLong() : new ReadRawRgroupsOfIdString());
				readInputDataForContainerType(currentFilename, inputRawDataForCompute.rgroups, rrg3);

				currentFilename = appConfig.inputData().inputDataAdvanced().filenameAssocRunitsWithRgroups();
				final AbstractReadRawDataAssociateRunitsWithRgroups<?> rrg4 =
						(areIndicesNumerical ? new ReadRawAssociateRunitsWithRgroupsOfIdLong() :
								new ReadRawAssociateRunitsWithRgroupsOfIdString());
				readInputDataForContainerType(currentFilename, inputRawDataForCompute.assocRunitsWithRgroups, rrg4);

				currentFilename = appConfig.inputData().inputDataAdvanced().filenameAssocRgroupsWithTasks();
				final AbstractReadRawDataAssociateRgroupsWithTasks<?> rrg5 =
						(areIndicesNumerical ? new ReadRawAssociateRgroupsWithTasksOfIdLong() :
								new ReadRawAssociateRgroupsWithTasksOfIdString());
				readInputDataForContainerType(currentFilename, inputRawDataForCompute.assocRgroupsWithTasks, rrg5);
			}
		} catch (IOException e) {
			throw new IOException("IOException while reading from '" + currentFilename + "': " + e.getMessage());
		} catch (InvalidInputDataException e) {
			throw new InvalidInputDataException("InvalidInputDataException while reading from '" +
					currentFilename + "': " + e.getMessage());
		}
	}

	// -----------------------------------------------------------------------------------------------------------------

	private @NonNull LocalDateTime determinePresentDateTime() {
		final LocalDateTime tmpNow = LocalDateTime.now();
		final boolean needMinutes = (appConfig.timeUnit() == CpmTimeUnit.MINUTES);
		final boolean needHours = (needMinutes || appConfig.timeUnit() == CpmTimeUnit.HOURS);
		return LocalDateTime.of(
				tmpNow.getYear(),
				tmpNow.getMonth(),
				tmpNow.getDayOfMonth(),
				needHours ? tmpNow.getHour() : 0,
				needMinutes ? tmpNow.getMinute() : 0,
				0
			);
	}

	private @NonNull ConvertRawToInternalDataForGraph buildConverterForGraph() {
		return new ConvertRawToInternalDataForGraph(
				appConfig.timeUnit(),
				appConfig.offDutyTimes().workDays(),
				appConfig.offDutyTimes().workHours(),
				appConfig.offDutyTimes().holidaysAsLocalDates(),
				presentDateTime
			);
	}

	private @NonNull CriticalPathGraph buildCriticalPathGraph(@NonNull RawDataForGraph inputRawDataForGraph) {
		return new CriticalPathGraph(
				appConfig.debugging().debugCpgInternals(),
				Objects.equals(true, appConfig.debugging().debugCpgVerboseInternals()),
				this::debugOutput,
				inputRawDataForGraph,
				buildConverterForGraph()
			);
	}

	private @NonNull CpmResultsToHtml buildCpmResultsToHtml(@NonNull CpmResult cpmResult, @NonNull String filenameOutput) {
		return new CpmResultsToHtml(
				appConfig,
				cpmResult,
				presentDateTime,
				filenameOutput
			);
	}

	private @NonNull ConvertRawToInternalDataForCompute buildConverterForCompute() {
		return new ConvertRawToInternalDataForCompute(
				appConfig.debugging().debugCpcInternals(),
				this::debugOutput
			);
	}

	private @NonNull CriticalPathCompute buildCriticalPathComputeBasic(@NonNull CriticalPathGraph criticalPathGraph) {
		assert (appConfig.inputData().useBasicInputData());

		return new CriticalPathCompute(
				appConfig.debugging().debugCpcInternals(),
				Objects.equals(true, appConfig.debugging().debugCpcVerboseInternals()),
				this::debugOutput,
				criticalPathGraph,
				appConfig.inputData().inputDataBasic().amountResourceUnits(),
				buildConverterForCompute()
			);
	}

	private @NonNull CriticalPathCompute buildCriticalPathComputeAdvanced(@NonNull CriticalPathGraph criticalPathGraph) {
		assert (! appConfig.inputData().useBasicInputData());

		return new CriticalPathCompute(
				criticalPathGraph,
				inputRawDataForCompute,
				buildConverterForCompute()
			);
	}
}
