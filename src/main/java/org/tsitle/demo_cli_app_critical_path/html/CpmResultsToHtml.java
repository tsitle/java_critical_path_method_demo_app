package org.tsitle.demo_cli_app_critical_path.html;

import com.google.common.html.HtmlEscapers;
import io.github.tsitle.criticalpath.cpmresults.*;
import io.github.tsitle.criticalpath.ResultsPostProcessing;
import io.github.tsitle.criticalpath.dateformatters.DateFormatters;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.tsitle.demo_cli_app_critical_path.json.AppConfig;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public final class CpmResultsToHtml {
	private final @NonNull AppConfig appConfig;
	private final @NonNull CpmResult cpmResult;
	private final List<@NonNull CpmPostProcessedTask> postProcessedTasks;
	private final long projectTotalTimeSpent;
	private final long projectTotalOffDutyTime;
	private final long projectTotalWorkingTime;
	private final long projectTimeUntilEnd;
	private final long projectOffDutyTimeUntilEnd;
	private final @NonNull String outputFilename;
	private @Nullable OutputStream outputStream = null;

	public CpmResultsToHtml(
				@NonNull AppConfig appConfig,
				@NonNull CpmResult cpmResult,
				@NonNull LocalDateTime presentDateTime,
				@NonNull String outputFilename
			) {
		this.appConfig = appConfig;
		this.cpmResult = cpmResult;
		this.outputFilename = outputFilename;

		//
		final ResultsPostProcessing rppObj = new ResultsPostProcessing(
				appConfig.timeUnit,
				appConfig.offDutyTimes.workDays(),
				appConfig.offDutyTimes.workHours(),
				appConfig.offDutyTimes.holidaysAsLocalDates(),
				cpmResult,
				presentDateTime
			);
		this.postProcessedTasks = rppObj.processTasks();

		projectTotalTimeSpent = rppObj.computeTotalTimeSpent(this.postProcessedTasks);
		projectTotalOffDutyTime = rppObj.computeTotalOffDutyTimeDuringProjectsTimeSpan(this.postProcessedTasks);
		projectTotalWorkingTime = projectTotalTimeSpent - projectTotalOffDutyTime;
		projectTimeUntilEnd = rppObj.computeTimeLeftUntilProjectsEnd(this.postProcessedTasks);
		projectOffDutyTimeUntilEnd = rppObj.computeOffDutyTimeLeftUntilProjectsEnd(this.postProcessedTasks);
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	public void output() throws IOException {
		try (OutputStream os = new FileOutputStream(outputFilename)) {
			outputStream = os;

			internalOutput();
		} catch (FileNotFoundException e) {
			throw new IOException("file '" + outputFilename + "' could not be created or overwritten");
		} catch (IOException e) {
			throw new IOException("IOException while writing to '" + outputFilename + "': " + e.getMessage());
		}
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	private void internalOutput() throws IOException {
		writeln(0, "<!DOCTYPE html>");
		writeln(0, "<html lang=\"en\">");
		writeln(0, "<head>");
		writeln(1, "<meta charset=\"utf-8\">");
		writeln(1, "<title>" + Constants.TEXT_PAGE_TITLE + "</title>");
		writeln(1, "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
		writeHeadStyles();
		writeln(0, "</head>");
		writeln(0, "<body>");

		writeHeadline(1, "h1", Constants.TEXT_PAGE_HEADLINE);

		//
		writeSectionGanttChart();

		//
		writeSectionOverviewTasks();

		//
		writeSectionStatistics();

		//
		writeSectionInputArgs();

		//
		writeSectionGenerationTime();

		//
		writeBodyScripts();

		//
		writeln(0, "</body>");
		writeln(0, "</html>");
	}

	private void writeHeadStyles() throws IOException {
		final String CSS_OVERFLOW_HIDDEN_WS_NOWRAP = "overflow:hidden;white-space:nowrap";

		writeln(1, "<link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/frappe-gantt/dist/frappe-gantt.css\" />");

		writeln(1, "<style>");

		writeln(2, "/* for Gantt Chart */");
		writeln(2, ":root {");
		writeln(3, "--g-progress-color:" + Constants.CSS_GANTT_CHART_JS_PROGRESS_COLOR + ";");
		writeln(3, "--g-weekend-highlight-color:" + Constants.CSS_GANTT_CHART_JS_WEEKEND_COLOR + ";");
		writeln(2, "}");
		writeln(2, ".gantt-container .popup-wrapper { background:" + Constants.CSS_GANTT_CHART_JS_POPUP_BG_COLOR + "; }");

		writeln(2, "/* for everything else */");
		writeln(2, "body { padding-bottom:50px; }");

		writeln(2, ".headline-wrapper { display:block; }");
		writeln(2, "h1,h2,h3 { display:inline-block; }");
		writeln(2, "h1 { border-bottom:2px solid black; }");
		writeln(2, "h2 { border-bottom:1px solid black; }");

		writeln(2, "ul { margin-top:0; }");
		writeln(2, "li { margin-bottom:5px; }");

		writeln(2, "section { max-width:100%;width:1280px;margin-bottom:15px;margin-bottom:15px; }");

		writeln(2, "." + Constants.CSS_CLASS_TABLE + " { display:table;background-color:" + Constants.CSS_TABLE_BG_COLOR + "; }");
		writeln(2, "." + Constants.CSS_CLASS_TABLE_HEAD_ROW + ", ." + Constants.CSS_CLASS_TABLE_BODY_ROW + " { display:table-row; }");
		writeln(2, "." + Constants.CSS_CLASS_TABLE_HEAD_ROW + " span { font-weight:bold; }");
		writeln(2, "." + Constants.CSS_CLASS_TABLE_BODY_ROW + ":nth-child(2n+1) { background-color:" + Constants.CSS_ROW_BG_COLOR_ODD + "; }");
		writeln(2, "." + Constants.CSS_CLASS_TABLE_BODY_ROW + ":nth-child(2n+2) { background-color:" + Constants.CSS_ROW_BG_COLOR_EVEN + "; }");
		writeln(2, "." + Constants.CSS_CLASS_CELL + " { display:table-cell;padding:3px 5px;border-left:1px solid black;" +
				CSS_OVERFLOW_HIDDEN_WS_NOWRAP + ";min-width:15px;max-width:250px;text-overflow:ellipsis; }");
		writeln(2, "." + Constants.CSS_CLASS_CELL + ":nth-child(1) { border-left:0px solid black; }");

		writeln(2, "#" + Constants.CSS_ID_SECT_GENERATION_TIME + " { border-top:1px solid black; }");

		writeln(1, "</style>");
	}

	private void writeHeadline(int level, String hTag, String title) throws IOException {
		writeln(level, "<div class=\"headline-wrapper\">" +
				"<" + hTag + ">" + title + "</" + hTag + ">" +
				"</div>");
	}

	private CpmPostProcessedTask createEmptyPpTask() {
		CpmSubResultTask srTask = new CpmSubResultTask(
				0,
				"-",
				Constants.TEXT_NO_TASKS_TO_DISPLAY,
				0,
				0,
				null,
				null,
				new HashSet<>(),
				new HashSet<>(),
				false,
				null,
				null
			);
		return new CpmPostProcessedTask(srTask.id(), srTask);
	}

	private CpmSubResultRgroup createEmptySrRgroup() {
		return new CpmSubResultRgroup(
				0,
				"-",
				Constants.TEXT_NO_RGROUPS_TO_DISPLAY,
				0L,
				0L,
				0,
				new ArrayList<>()
			);
	}

	private CpmSubResultRunit createEmptySrRunit() {
		return new CpmSubResultRunit(
				0,
				"-",
				Constants.TEXT_NO_RUNITS_TO_DISPLAY,
				0L,
				0L,
				new HashSet<>(),
				new HashSet<>()
			);
	}

	// -----------------------------------------------------------------------------------------------------------------

	private void writeSectionGanttChart() throws IOException {
		writeln(1, "<section id=\"" + Constants.CSS_ID_SECT_GANTT_CHART + "\">");
		writeHeadline(2, "h2", "Gantt Chart For Optimum Order Of Task Completion");

		if (cpmResult.resultsTasks().isEmpty()) {
			writeln(2, "<p>" + Constants.TEXT_NO_TASKS_TO_DISPLAY + "</p>");
		} else {
			writeln(2, "<div id=\"" + Constants.CSS_ID_GANTT_CHART_JS + "\"></div>");
		}

		writeln(1, "</section>");
	}

	// -----------------------------------------------------------------------------------------------------------------

	private void writeSectionOverviewTasks() throws IOException {
		writeln(1, "<section id=\"" + Constants.CSS_ID_SECT_OVERVIEW_TASKS + "\">");
		writeHeadline(2, "h2", "Overview Of Tasks In Execution Order");
		writeln(2, "<div class=\"" + Constants.CSS_CLASS_TABLE + "\">");

		writeOverviewTasksEntryHeader();

		if (postProcessedTasks.isEmpty()) {
			writeOverviewTasksEntryTask(createEmptyPpTask(), true);
		}
		for (CpmPostProcessedTask ppTask : postProcessedTasks) {
			writeOverviewTasksEntryTask(ppTask, false);
		}

		writeln(2, "</div>");
		writeln(1, "</section>");
	}

	private void writeOverviewTasksEntryHeader() throws IOException {
		writeTableRow(
				3,
				Constants.CSS_CLASS_TABLE_HEAD_ROW + " overview-tasks-head",
				"overview-tasks-head-content",
				Constants.FIELD_IDS_FOR_CSSCLASS_OVERVIEW_TASKS,
				Constants.COLUMN_HEADERS_OVERVIEW_TASKS
			);
	}

	private void writeOverviewTasksEntryTask(CpmPostProcessedTask ppTask, boolean isEmptyEntry) throws IOException {
		String runitStr = "-";
		String rgroupStr = "-";
		String durationStr = "-";
		String timeStartedStr = "-";
		String timeFinishedStr = "-";
		if (! isEmptyEntry && ppTask.srTask().idOfRgroupThatFinishedTask() != null &&
				ppTask.srTask().idOfRunitThatFinishedTask() != null) {
			CpmSubResultRgroup rgroupObj = cpmResult.resultsRgroups().stream()
					.filter(x ->
							x.id() == ppTask.srTask().idOfRgroupThatFinishedTask()
						)
					.findFirst()
					.orElseThrow(() -> new IllegalStateException("Could not find resource group for task id=" + ppTask.id()));
			CpmSubResultRunit runitObj = rgroupObj.resultsRunits().stream()
					.filter(x -> x.id() == ppTask.srTask().idOfRunitThatFinishedTask())
					.findFirst()
					.orElseThrow(() -> new IllegalStateException("Could not find resource unit for task id=" + ppTask.id()));
			runitStr = runitObj.name() + " (ID " + getExternalIdForOutput(runitObj.externalId()) + ")";
			rgroupStr = rgroupObj.name() + " (ID " + getExternalIdForOutput(rgroupObj.externalId()) + ")";

			final String timeUnitLabel = getTimeUnitLabel();
			durationStr = ppTask.durationOrg() + timeUnitLabel +
					" (+ " + ppTask.durationDelta() + timeUnitLabel + ")";

			final DateTimeFormatter formatter = DateFormatters.getFormatterForTimeUnit(appConfig.timeUnit);

			timeStartedStr = ppTask.taskStartedAdjustedDateTime().format(formatter);
			timeFinishedStr = ppTask.taskFinishedAdjustedDateTime().format(formatter);
		}

		List<String> cellValues = List.of(
				ppTask.srTask().externalId() != null ? getExternalIdForOutput(ppTask.srTask().externalId(), false) : "",
				ppTask.srTask().name(),
				durationStr,
				timeStartedStr,
				timeFinishedStr,
				runitStr,
				rgroupStr
			);
		writeTableRow(
				3,
				Constants.CSS_CLASS_TABLE_BODY_ROW + " overview-tasks-entry",
				"overview-tasks-entry-content",
				Constants.FIELD_IDS_FOR_CSSCLASS_OVERVIEW_TASKS,
				cellValues
			);
	}

	// -----------------------------------------------------------------------------------------------------------------

	private void writeSectionStatistics() throws IOException {
		writeln(1, "<section id=\"" + Constants.CSS_ID_SECT_STATISTICS_MAIN + "\">");
		writeHeadline(2, "h2", "Statistics");
		writeln(2, "<div>");

		final String timeUnitStr = appConfig.timeUnit.toString().toLowerCase();
		writeln(3, "<ul>");
		writeln(4, "<li><span>Minimum time required to complete all tasks: " +
				projectTotalTimeSpent + " " + timeUnitStr + "</span><br />" +
				"<span>(including " + projectTotalOffDutyTime + " off-duty " + timeUnitStr + ")</span></li>");
		final String idealWorkingTimeStr = "<br /><span>(the ideal would be " + cpmResult.timePassed() +
				" working " + timeUnitStr + ")</span>";
		final String maybeProblemStr = " <span><strong>There might be a problem in the input data</strong></span>";
		writeln(4, "<li><span>Minimum working time required to complete all tasks: " +
				projectTotalWorkingTime + " working " + timeUnitStr + "</span>" +
				(projectTotalWorkingTime == cpmResult.timePassed() ? "" : idealWorkingTimeStr) +
				(projectTotalWorkingTime >= cpmResult.timePassed() ? "" : maybeProblemStr) +
				"</li>");
		writeln(4, "<li><span>Minimum time left until all tasks have been completed: " +
				projectTimeUntilEnd + " " + timeUnitStr + "</span><br />" +
				"<span>(including " + projectOffDutyTimeUntilEnd + " off-duty " + timeUnitStr + ")</span></li>");
		writeln(4, "<li><span>Accumulated time that Resource Units idled: " +
				cpmResult.timeIdled() + " working " + timeUnitStr + "</span><br />" +
				"<span>(when there were pending Tasks but no new Task could be started<br />" +
				"because there were no available Resource Units or the Task still had pending dependencies)</span></li>");
		writeln(4, "<li><span>Maximum Resource Units used in parallel: " +
				cpmResult.maxRunitsUsed() + "</span></li>");
		writeln(3, "</ul>");

		writeHeadline(3, "h3", "Per Resource Group");
		writeln(3, "<div id=\"" + Constants.CSS_ID_SUBSECT_STATISTICS_SUB_RG + "\" class=\"" + Constants.CSS_CLASS_TABLE + "\">");
		writeStatisticsSubRgEntryHeader();
		if (cpmResult.resultsRgroups().isEmpty()) {
			writeStatisticsSubRgEntryRgroup(createEmptySrRgroup(), true);
		}
		for (CpmSubResultRgroup srRgroup : cpmResult.resultsRgroups().stream().sorted(Comparator.comparing(CpmSubResultRgroup::name)).toList()) {
			writeStatisticsSubRgEntryRgroup(srRgroup, false);
		}
		writeln(3, "</div>");

		writeHeadline(3, "h3", "Per Resource Unit");
		writeln(3, "<div id=\"" + Constants.CSS_ID_SUBSECT_STATISTICS_SUB_RU + "\" class=\"" + Constants.CSS_CLASS_TABLE + "\">");
		writeStatisticsSubRuEntryHeader();
		if (cpmResult.resultsRgroups().isEmpty()) {
			writeStatisticsSubRuEntryRunit(createEmptySrRunit(), true);
		}
		List<CpmSubResultRunit> runitsForOutput = cpmResult.resultsRunits();
		for (CpmSubResultRunit srRunit : runitsForOutput.stream().sorted(Comparator.comparing(CpmSubResultRunit::name)).toList()) {
			writeStatisticsSubRuEntryRunit(srRunit, false);
		}
		writeln(3, "</div>");

		writeln(2, "</div>");
		writeln(1, "</section>");
	}

	private void writeStatisticsSubRgEntryHeader() throws IOException {
		writeTableRow(
				4,
				Constants.CSS_CLASS_TABLE_HEAD_ROW + " statistics-rg-head",
				"statistics-rg-head-content",
				Constants.FIELD_IDS_FOR_CSSCLASS_STATS_RG,
				Constants.COLUMN_HEADERS_STATS_RG
			);
	}

	private static String getHtmlForPercentage(long value, long total, String unit) {
		double percentDbl = (total > 0L ? ((double)value / (double)total) * 100.0 : 0.0);
		return String.format("%.0f%% (%d%s / %d%s)", percentDbl, value, unit, total, unit);
	}

	private @NonNull Set<CpmSubResultRgroup> getAssociatedRgroupsForRunit(@NonNull CpmSubResultRunit srRunit) {
		Set<CpmSubResultRgroup> resSet = new LinkedHashSet<>();
		for (Long rgroupId : srRunit.associatedRgroupIds()) {
			CpmSubResultRgroup tmpSrRgroup = cpmResult.resultsRgroups().stream()
					.filter(x -> x.id() == rgroupId)
					.findFirst()
					.orElseThrow(() -> new IllegalStateException("Could not find resource group with id=" + rgroupId));
			resSet.add(tmpSrRgroup);
		}
		return resSet;
	}

	private long getTasksCompletedCountForRgroup(CpmSubResultRgroup srRgroup) {
		return cpmResult.resultsTasks().stream()
				.filter(x ->
						x.idOfRgroupThatFinishedTask() != null &&
						x.idOfRgroupThatFinishedTask() == srRgroup.id()
					)
				.count();
	}

	private void writeStatisticsSubRgEntryRgroup(CpmSubResultRgroup srRgroup, boolean isEmptyEntry) throws IOException {
		String timeIdledStr = "-";
		String timeBusyStr = "-";
		String associatedTasksStr = "-";
		String runitsUsedStr = "-";
		if (! isEmptyEntry) {
			timeIdledStr = getHtmlForPercentage(srRgroup.timeIdled(), cpmResult.timePassed(), getTimeUnitLabel());

			timeBusyStr = getHtmlForPercentage(srRgroup.timeBusy(), cpmResult.timePassed(), getTimeUnitLabel());

			long associatedTasksLong = getTasksCompletedCountForRgroup(srRgroup);
			associatedTasksStr = getHtmlForPercentage(associatedTasksLong, cpmResult.resultsTasks().size(), "");

			runitsUsedStr = getHtmlForPercentage(srRgroup.maxRunitsUsed(), srRgroup.resultsRunits().size(), "");
		}

		List<String> cellValues = List.of(
				getExternalIdForOutput(srRgroup.externalId(), false),
				srRgroup.name(),
				timeIdledStr,
				timeBusyStr,
				associatedTasksStr,
				runitsUsedStr
			);
		writeTableRow(
				4,
				Constants.CSS_CLASS_TABLE_BODY_ROW + " statistics-rg-entry",
				"statistics-rg-entry-content",
				Constants.FIELD_IDS_FOR_CSSCLASS_STATS_RG,
				cellValues
			);
	}

	private void writeStatisticsSubRuEntryHeader() throws IOException {
		writeTableRow(
				4,
				Constants.CSS_CLASS_TABLE_HEAD_ROW + " statistics-ru-head",
				"statistics-ru-head-content",
				Constants.FIELD_IDS_FOR_CSSCLASS_STATS_RU,
				Constants.COLUMN_HEADERS_STATS_RU
			);
	}

	private void writeStatisticsSubRuEntryRunit(CpmSubResultRunit srRunit, boolean isEmptyEntry)
			throws IOException {
		String timeIdledStr = "-";
		String timeBusyStr = "-";
		String associatedTasksStr = "-";
		if (! isEmptyEntry) {
			timeIdledStr = getHtmlForPercentage(srRunit.timeIdled(), cpmResult.timePassed(), getTimeUnitLabel());

			timeBusyStr = getHtmlForPercentage(srRunit.timeBusy(), cpmResult.timePassed(), getTimeUnitLabel());

			long associatedTasksRuLong = srRunit.tasksCompleted().size();
			Set<CpmSubResultRgroup> tmpRgroups = getAssociatedRgroupsForRunit(srRunit);
			long associatedTasksRgLong = tmpRgroups.stream().mapToLong(this::getTasksCompletedCountForRgroup).sum();
			associatedTasksStr = getHtmlForPercentage(associatedTasksRuLong, associatedTasksRgLong, "");
		}

		List<String> cellValues = List.of(
				getExternalIdForOutput(srRunit.externalId(), false),
				srRunit.name(),
				timeIdledStr,
				timeBusyStr,
				associatedTasksStr
			);
		writeTableRow(
				4,
				Constants.CSS_CLASS_TABLE_BODY_ROW + " statistics-ru-entry",
				"statistics-ru-entry-content",
				Constants.FIELD_IDS_FOR_CSSCLASS_STATS_RU,
				cellValues
			);
	}

	// -----------------------------------------------------------------------------------------------------------------

	private void writeSectionInputArgs() throws IOException {
		writeln(1, "<section id=\"" + Constants.CSS_ID_SECT_INPUT_ARGS + "\">");
		writeHeadline(2, "h2", "Input Arguments");
		writeln(2, "<div>");

		writeln(3, "<ul>");

		for (Map.Entry<@NonNull String, @NonNull Map<@NonNull String, @NonNull String>> entry : appConfig.asMap(false).entrySet()) {
			writeSectionInputArgsArgList(entry.getKey(), entry.getValue());
		}

		writeln(3, "</ul>");

		writeln(2, "</div>");
		writeln(1, "</section>");
	}

	private void writeSectionInputArgsArgList(String title, Map<@NonNull String, @NonNull String> keyValuePairs) throws IOException {
		writeln(4, "<li><span>" + escapeHtml(title) + ":</span><br />");
		writeln(5, "<ul>");
		for (Map.Entry<@NonNull String, @NonNull String> entry : keyValuePairs.entrySet()) {
			writeln(6, "<li><span>" + escapeHtml(entry.getKey()) + ":</span> " +
					"<span>" + escapeHtml(entry.getValue()) + "</span></li>");
		}
		writeln(5, "</ul>");
		writeln(4, "</li>");
	}

	// -----------------------------------------------------------------------------------------------------------------

	private void writeSectionGenerationTime() throws IOException {
		writeln(1, "<section id=\"" + Constants.CSS_ID_SECT_GENERATION_TIME + "\">");
		writeln(2, "<div>");

		writeln(3, "<p>Generated: " + escapeHtml(LocalDateTime.now().toString()) + "</p>");

		writeln(2, "</div>");
		writeln(1, "</section>");
	}

	// -----------------------------------------------------------------------------------------------------------------

	private void writeBodyScripts() throws IOException {
		if (cpmResult.resultsTasks().isEmpty()) {
			return;
		}
		writeln(1, "<script src=\"https://cdn.jsdelivr.net/npm/frappe-gantt/dist/frappe-gantt.umd.js\"></script>");

		writeln(1, "<script type=\"text/javascript\">");

		writeln(2, "const LOC_HOLIDAYS = [");
		for (LocalDate holiday : appConfig.offDutyTimes.holidaysAsLocalDates()) {
			writeln(3, "{");
			writeln(4, "name: 'Holiday " + holiday.format(DateFormatters.onlyDate) + "',");
			writeln(4, "date: '" + holiday.format(DateFormatters.onlyDate) + "'");
			writeln(3, "},");
		}
		writeln(2, "];");

		writeln(2, "const LOC_TASKS = [");
		for (CpmPostProcessedTask ppTask : postProcessedTasks) {
			final String dependenciesStr = ppTask.srTask().dependenciesIds().stream()
					.map(String::valueOf)
					.collect(Collectors.joining(","));
			writeln(3, "{");
			writeln(4, "id: '" + ppTask.id() + "',");
			writeln(4, "name: '" + ppTask.srTask().name() + "',");
			writeln(4, "start: '" + ppTask.taskStartedAdjustedDateTime().format(DateFormatters.dateAndTime) + "',");
			writeln(4, "end: '" + ppTask.taskFinishedAdjustedDateTime().format(DateFormatters.dateAndTime) + "',");
			writeln(4, "progress: " + ppTask.progressPercent() + ",");
			writeln(4, "dependencies: '" + dependenciesStr + "'");
			writeln(3, "},");
		}
		writeln(2, "];");

		/*
		 * Available view modes: Hour, Quarter Day, Half Day, Day, Week, Month, Year
		 */
		final String viewMode;
		switch (appConfig.timeUnit) {
			case MINUTES -> viewMode = "Hour";
			case HOURS -> viewMode = "Quarter Day";
			case DAYS -> viewMode = "Week";
			default -> throw new IllegalStateException("Unexpected value: " + appConfig.timeUnit);
		}
		writeln(2, "new Gantt(\"#" + Constants.CSS_ID_GANTT_CHART_JS + "\", LOC_TASKS, " +
				"{view_mode: '" + viewMode + "', view_mode_select: true, " +
					"readonly_progress: true, readonly_dates: true, readonly: true, " +
					"holidays: { " +
						"'var(--g-weekend-highlight-color)': 'weekend', " +
						"'" + Constants.CSS_GANTT_CHART_JS_HOLIDAY_COLOR + "': LOC_HOLIDAYS " +
					"}" +
				"});");

		writeln(1, "</script>");
	}

	// -----------------------------------------------------------------------------------------------------------------

	private String getTimeUnitLabel() {
		return appConfig.timeUnit.getLabel();
	}

	private static String escapeHtml(@Nullable String str) {
		if (str == null) {
			return "";
		}
		return HtmlEscapers.htmlEscaper().escape(str);
	}

	private String getExternalIdForOutput(@Nullable Comparable<?> externalId) {
		return getExternalIdForOutput(externalId, true);
	}

	private String getExternalIdForOutput(@Nullable Comparable<?> externalId, boolean withQuotes) {
		return switch (externalId) {
				case null -> "none";
				case Long idLong -> String.valueOf(idLong);
				case String idStr -> (withQuotes ? "\"" : "") + idStr + (withQuotes ? "\"" : "");
				default -> "-invalid ID-";
			};
	}

	private void writeTableRow(
				int level,
				String cssClassesRow,
				String cssClassCells,
				List<String> fieldIds,
				List<String> cellValues
			) throws IOException {
		if (fieldIds.size() != cellValues.size()) {
			throw new IllegalArgumentException("Invalid cell count: " + fieldIds.size() + " != " + cellValues.size());
		}
		writeln(level, "<div class=\"" + cssClassesRow + "\">");

		for (int i = 0; i < fieldIds.size(); i++) {
			writeTableCell(level + 1, cssClassCells, fieldIds.get(i), cellValues.get(i));
		}

		writeln(level, "</div>");
	}

	private void writeTableCell(int level, String cssClassCells, String fieldId, String cellValue) throws IOException {
		final String htmlCellValue = escapeHtml(cellValue);
		writeln(level, "<div class=\"" + Constants.CSS_CLASS_CELL + " " + cssClassCells + " " + cssClassCells + "-" + fieldId + "\">" +
				"<span title=\"" + htmlCellValue + "\">" + htmlCellValue + "</span>" +
				"</div>");
	}

	private void writeln(int level, String line) throws IOException {
		if (outputStream == null) {
			return;
		}
		String tabs = String.join("", Collections.nCopies(level, "\t"));
		outputStream.write((tabs + line + "\n").getBytes());
	}
}
