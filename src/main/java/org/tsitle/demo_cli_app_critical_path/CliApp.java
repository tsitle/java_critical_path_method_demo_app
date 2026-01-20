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

import java.io.*;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Command line tool for using the Critical Path Method
 */
public final class CliApp {
	private final CliArgs.AllArgs argsAll;

	private final RawDataForGraph inputRawDataForGraph = new RawDataForGraph();
	private final RawDataForCompute inputRawDataForCompute = new RawDataForCompute();

	private final LocalDateTime presentDateTime;

	public CliApp(CliArgs.AllArgs argsAll) {
		assert (argsAll != null);
		assert (argsAll.argsComputeBasic() != null || argsAll.argsComputeAdvanced() != null);

		this.argsAll = argsAll;

		presentDateTime = determinePresentDateTime();
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	public void start() throws IOException, InvalidInputDataException {
		// read input files
		readInputData();

		/*if (argsAll.argsApp().debugMain()) {
			System.out.println("M: Raw graph:");
			inputRawTasks.values().forEach((item) -> System.out.println("M:   - " + item));
		}*/

		//
		final boolean debugMainOrCpg = argsAll.argsApp().debugMain() || argsAll.argsApp().debugCpgInternals();

		// create graph
		if (debugMainOrCpg) {
			System.out.println("M: Create Critical Path graph:");
		}
		CriticalPathGraph criticalPathGraph = buildCriticalPathGraph(inputRawDataForGraph);

		if (argsAll.argsApp().debugMain()) {
			criticalPathGraph.printGraph(System.out::println, true);
			//criticalPathGraph.printMostCriticalPath();
		}

		// compute CPM results
		if (debugMainOrCpg) {
			System.out.println("M: Complete all Tasks with " +
					inputRawDataForCompute.rgroups.size() + " Resource Groups and " +
					inputRawDataForCompute.runits.size() + " Resource Units working in parallel:");
		}
		final CpmResult cpmResult = computeCpmResult(criticalPathGraph);

		// print results
		printResults(cpmResult);

		// output results as an HTML file
		if (argsAll.argsApp().filenameOutput() == null) {
			return;
		}
		System.out.println("M: Writing results to HTML file '" + argsAll.argsApp().filenameOutput() + "'");
		CpmResultsToHtml cpmResultsToHtml = buildCpmResultsToHtml(cpmResult, argsAll.argsApp().filenameOutput());
		cpmResultsToHtml.output();
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	private @NonNull CpmResult computeCpmResult(@NonNull CriticalPathGraph criticalPathGraph) {
		final CriticalPathCompute cpCompute;
		if (argsAll.argsComputeBasic() != null) {
			cpCompute = buildCriticalPathComputeBasic(criticalPathGraph);
		} else {
			cpCompute = buildCriticalPathComputeAdvanced(criticalPathGraph);
		}

		//
		return cpCompute.computeCpmResults();
	}

	private void printResults(@NonNull CpmResult cpmResult) {
		System.out.println("M: Full results:");

		final String timeUnitLabel = argsAll.argsCommon().timeUnit().getLabel();

		System.out.println("M:   Tasks:");
		for (CpmSubResultTask tempResultsTask : cpmResult.resultsTasks()) {
			@SuppressWarnings("StringBufferReplaceableByString") StringBuilder sb = new StringBuilder();
			sb.append("M:     - Task id=").append(tempResultsTask.id());
			sb.append(", extId=").append(tempResultsTask.externalId());
			sb.append(", started=").append(tempResultsTask.timeStarted()).append(timeUnitLabel)
					.append(", finished=").append(tempResultsTask.timeFinished()).append(timeUnitLabel)
					.append(", runitId=").append(tempResultsTask.idOfRunitThatFinishedTask());
			System.out.println(sb);
		}

		System.out.println("M:   Resource Groups:");
		for (CpmSubResultRgroup tempResultsRgroup : cpmResult.resultsRgroups()) {
			@SuppressWarnings("StringBufferReplaceableByString") StringBuilder sb = new StringBuilder();
			sb.append("M:     - Resource Group id=").append(tempResultsRgroup.id());
			sb.append(", extId=").append(tempResultsRgroup.externalId())
					.append(", timeIdled=").append(tempResultsRgroup.timeIdled()).append(timeUnitLabel)
					.append(", timeBusy=").append(tempResultsRgroup.timeBusy()).append(timeUnitLabel)
					.append(", maxRunitsUsed=").append(tempResultsRgroup.maxRunitsUsed());
			System.out.println(sb);
			for (CpmSubResultRunit tempResultsRunit : tempResultsRgroup.resultsRunits()) {
				@SuppressWarnings("StringBufferReplaceableByString") StringBuilder sb2 = new StringBuilder();
				sb2.append("M:       - Resource Unit id=").append(tempResultsRunit.id());
				sb2.append(", extId=").append(tempResultsRunit.externalId())
						.append(", timeIdled=").append(tempResultsRunit.timeIdled()).append(timeUnitLabel)
						.append(", timeBusy=").append(tempResultsRunit.timeBusy()).append(timeUnitLabel)
						.append(", tasksCompleted=").append(tempResultsRunit.tasksCompleted());
				System.out.println(sb2);
			}
		}

		System.out.println("M:   Overview:");
		System.out.println("M:     - Tasks                    " + inputRawDataForGraph.tasks.size());
		System.out.println("M:     - Resource Groups          " + inputRawDataForCompute.rgroups.size());
		System.out.println("M:     - Resource Units           " + inputRawDataForCompute.runits.size());
		System.out.println("M:     - Minimum time requirement " + cpmResult.timePassed() + " " +
				argsAll.argsCommon().timeUnit().toString().toLowerCase());
	}

	// -----------------------------------------------------------------------------------------------------------------

	private static <T extends BaseRawData> void readInputDataForContainerType(
				String filename,
				Set<T> outputSet,
				AbstractReadRawData<?, T> reader
			) throws IOException, InvalidInputDataException {
		if (! filename.startsWith("rsc:")) {
			File file = new File(filename);
			try (InputStream is = new FileInputStream(file)) {  // throws FileNotFoundException
				reader.readFromStream(is, outputSet);
			} catch (FileNotFoundException e) {
				throw new IOException("file '" + filename + "' not found");
			}
		} else {
			reader.readFromResourcesFile(filename.substring(4), outputSet);
		}
	}

	private void readInputData() throws IOException, InvalidInputDataException {
		String currentFilename = "----";
		try {
			currentFilename = argsAll.argsGraph().filenameRawTasks();
			final AbstractReadRawDataTasks<?> rrg1 =
					(argsAll.argsCommon().indicesNumerical() ? new ReadRawTasksOfIdLong() : new ReadRawTasksOfIdString());
			readInputDataForContainerType(currentFilename, inputRawDataForGraph.tasks, rrg1);

			if (argsAll.argsComputeAdvanced() != null) {
				currentFilename = argsAll.argsComputeAdvanced().filenameRawRunits();
				final AbstractReadRawDataRunits<?> rrg2 =
						(argsAll.argsCommon().indicesNumerical() ? new ReadRawRunitsOfIdLong() : new ReadRawRunitsOfIdString());
				readInputDataForContainerType(currentFilename, inputRawDataForCompute.runits, rrg2);

				currentFilename = argsAll.argsComputeAdvanced().filenameRawRgroups();
				final AbstractReadRawDataRgroups<?> rrg3 =
						(argsAll.argsCommon().indicesNumerical() ? new ReadRawRgroupsOfIdLong() : new ReadRawRgroupsOfIdString());
				readInputDataForContainerType(currentFilename, inputRawDataForCompute.rgroups, rrg3);

				currentFilename = argsAll.argsComputeAdvanced().filenameRawAssocRunitsWithRgroups();
				final AbstractReadRawDataAssociateRunitsWithRgroups<?> rrg4 =
						(argsAll.argsCommon().indicesNumerical() ? new ReadRawAssociateRunitsWithRgroupsOfIdLong() :
								new ReadRawAssociateRunitsWithRgroupsOfIdString());
				readInputDataForContainerType(currentFilename, inputRawDataForCompute.assocRunitsWithRgroups, rrg4);

				currentFilename = argsAll.argsComputeAdvanced().filenameRawAssocRgroupsWithTasks();
				final AbstractReadRawDataAssociateRgroupsWithTasks<?> rrg5 =
						(argsAll.argsCommon().indicesNumerical() ? new ReadRawAssociateRgroupsWithTasksOfIdLong() :
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
		final boolean needMinutes = (argsAll.argsCommon().timeUnit() == CpmTimeUnit.MINUTES);
		final boolean needHours = (needMinutes || argsAll.argsCommon().timeUnit() == CpmTimeUnit.HOURS);
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
				argsAll.argsApp().debugCpgInternals(),
				argsAll.argsApp().debugCpgVerboseInternals(),
				System.out::println,
				argsAll.argsCommon().timeUnit(),
				argsAll.argsOffDutyTimes().workDays(),
				argsAll.argsOffDutyTimes().workHours(),
				argsAll.argsOffDutyTimes().holidays(),
				presentDateTime
			);
	}

	private @NonNull CriticalPathGraph buildCriticalPathGraph(@NonNull RawDataForGraph inputRawDataForGraph) {
		return new CriticalPathGraph(
				argsAll.argsApp().debugCpgInternals(),
				argsAll.argsApp().debugCpgVerboseInternals(),
				System.out::println,
				inputRawDataForGraph,
				buildConverterForGraph()
			);
	}

	private @NonNull CpmResultsToHtml buildCpmResultsToHtml(@NonNull CpmResult cpmResult, @NonNull String filenameOutput) {
		return new CpmResultsToHtml(
				argsAll,
				cpmResult,
				presentDateTime,
				filenameOutput
			);
	}

	private @NonNull ConvertRawToInternalDataForCompute buildConverterForCompute() {
		return new ConvertRawToInternalDataForCompute(
				argsAll.argsApp().debugCpcInternals(),
				System.out::println
			);
	}

	private @NonNull CriticalPathCompute buildCriticalPathComputeBasic(@NonNull CriticalPathGraph criticalPathGraph) {
		assert (argsAll.argsComputeBasic() != null);

		return new CriticalPathCompute(
				argsAll.argsApp().debugCpcInternals(),
				argsAll.argsApp().debugCpcShowPath(),
				System.out::println,
				criticalPathGraph,
				argsAll.argsComputeBasic().amountResourceUnits(),
				buildConverterForCompute()
			);
	}

	private @NonNull CriticalPathCompute buildCriticalPathComputeAdvanced(@NonNull CriticalPathGraph criticalPathGraph) {
		return new CriticalPathCompute(
				argsAll.argsApp().debugCpcInternals(),
				argsAll.argsApp().debugCpcShowPath(),
				System.out::println,
				criticalPathGraph,
				inputRawDataForCompute,
				buildConverterForCompute()
			);
	}
}
