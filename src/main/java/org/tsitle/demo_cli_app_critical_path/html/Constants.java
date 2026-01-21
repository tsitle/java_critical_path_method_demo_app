package org.tsitle.demo_cli_app_critical_path.html;

import java.util.List;

class Constants {
	static final String CSS_ID_SECT_GANTT_CHART = "section-gantt-chart";
	static final String CSS_ID_GANTT_CHART_JS = "gantt-chart-js";
	static final String CSS_ID_SECT_OVERVIEW_TASKS = "section-overview-tasks";
	static final String CSS_ID_SECT_STATISTICS_MAIN = "section-statistics-main";
	static final String CSS_ID_SUBSECT_STATISTICS_SUB_RG = "statistics-rg";
	static final String CSS_ID_SUBSECT_STATISTICS_SUB_RU = "statistics-ru";
	static final String CSS_ID_SECT_INPUT_ARGS = "section-input-args";
	static final String CSS_ID_SECT_GENERATION_TIME = "section-generation-time";

	static final String CSS_CLASS_TABLE = "cpm-table";
	static final String CSS_CLASS_TABLE_HEAD_ROW = "cpm-table-head-row";
	static final String CSS_CLASS_TABLE_BODY_ROW = "cpm-table-body-row";
	static final String CSS_CLASS_CELL = "cpm-table-row-cell";

	static final String CSS_ROW_BG_COLOR_ODD = "#ccc";
	static final String CSS_ROW_BG_COLOR_EVEN = "#ddd";

	static final String CSS_TABLE_BG_COLOR = "#d4e5ff";

	static final List<String> COLUMN_HEADERS_OVERVIEW_TASKS = List.of(
			"ID",
			"Name",
			"Duration",
			"Time Started",
			"Time Finished",
			"Finished By",
			"Resource Group"
		);
	static final List<String> FIELD_IDS_FOR_CSSCLASS_OVERVIEW_TASKS = List.of(
			"id",
			"name",
			"duration",
			"timeStarted",
			"timeFinished",
			"finishedByResourceUnit",
			"resourceGroup"
		);

	static final List<String> COLUMN_HEADERS_STATS_RG = List.of(
			"ID",
			"Name",
			"Time Idled",
			"Time Busy",
			"Associated Tasks",
			"Max. Resource Units Used in Parallel"
		);
	static final List<String> FIELD_IDS_FOR_CSSCLASS_STATS_RG = List.of(
			"id",
			"name",
			"timeIdled",
			"timeBusy",
			"associatedTasks",
			"runitsUsed"
		);

	static final List<String> COLUMN_HEADERS_STATS_RU = List.of(
			"ID",
			"Name",
			"Time Idled",
			"Time Busy",
			"Associated Tasks"
		);
	static final List<String> FIELD_IDS_FOR_CSSCLASS_STATS_RU = List.of(
			"id",
			"name",
			"timeIdled",
			"timeBusy",
			"associatedTasks"
		);

	static final String TEXT_PAGE_TITLE = "Project Planning Results";
	static final String TEXT_PAGE_HEADLINE = "Results Of Project Planning Using Critical Path Method";
	static final String TEXT_NO_TASKS_TO_DISPLAY = "No Tasks to display";
	static final String TEXT_NO_RUNITS_TO_DISPLAY = "No Resource Units to display";
	static final String TEXT_NO_RGROUPS_TO_DISPLAY = "No Resource Groups to display";
}
