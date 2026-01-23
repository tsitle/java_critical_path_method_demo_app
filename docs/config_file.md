# Configuration File

Example configuration file:

``` json
{
	"debugging": {
		"debugMain": true,
		"debugVerboseMain": false,
		"debugCpgInternals": false,
		"debugCpgVerboseInternals": false,
		"debugCpcInternals": false,
		"debugCpcVerboseInternals": false
	},
	"timeUnit": "DAYS",
	"offDutyTimes": {
		"workDays": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"],
		"workHours": [
			{"hourStart": 8, "hourEnd": 12},
			{"hourStart": 13, "hourEnd": 16}
		],
		"holidays": [
			"2025-01-01",
			"<YEAR>-01-01",
			"<YEAR>-12-25",
			"<YEAR>-12-26",
			"<YEAR>-<MONTH>-<DAY+2>",
			"<YEAR>-<MONTH+1>-<DAY+10>"
		]
	},
	"inputData": {
		"indicesType": "NUM",
		"filenameTasks": "rsc:sample-tasks.csv",
		"useBasicInputData": false,
		"inputDataBasic": {
			"amountResourceUnits": 0
		},
		"inputDataAdvanced": {
			"filenameRunits": "rsc:sample-runits.csv",
			"filenameRgroups": "rsc:sample-rgroups.csv",
			"filenameAssocRunitsWithRgroups": "rsc:sample-assoc_runits_w_rgroups.csv",
			"filenameAssocRgroupsWithTasks": "rsc:sample-assoc_rgroups_w_tasks.csv"
		}
	}
}
```

## Section 'debugging'

- `debugMain`: Enables/disables debug output of the application itself
- `debugVerboseMain`: Optional: enables/disables verbose debug output of the application itself (requires `debugMain`)
- `debugCpgInternals`: Enables/disables debug output of the **CriticalPathGraph** methods
- `debugCpgVerboseInternals`: Optional: enables/disables verbose debug output of the **CriticalPathGraph** methods (requires `debugCpgInternals`)
- `debugCpcInternals`: Enables/disables debug output of the **CriticalPathCompute** methods
- `debugCpcVerboseInternals`: Optional: enables/disables debug output that shows the current path in the **CriticalPathCompute.computeCpmResults()** method every time a task has been completed (requires `debugCpcInternals`)

## Section 'timeUnit'

The selected unit of time will be used for all task durations in the 'Tasks' input file.

The value of `timeUnit` must be one of the following:

- `MINUTES`
- `HOURS`
- `DAYS`

## Section 'offDutyTimes'

- `workDays`: List of weekdays that are considered work days (uppercase, case-sensitive)
- `workHours`: List of work hours (start and end hour in 24-hour format). The start hour is inclusive, the end hour is exclusive
- `holidays`: List of holidays (year-month-day format)

The holidays list supports formulas that can be used to define dynamic holidays.

Examples:

- `2026-01-01` - Fixed holiday (January 1st of 2026)
- `2026-12-31` - Fixed holiday (December 31st of 2026)
- `2026-01-<DAY+7>` - Dynamic holiday: say at runtime the day of the month is the 25th. Then the holiday is on the 2nd of February 2026
- `2026-02-<DAY-1>` - Dynamic holiday: say at runtime the day of the month is the 1st. Then the holiday is on the 31st of January 2026
- `2026-<MONTH>-01` - Dynamic holiday: say at runtime the month is January. Then the holiday is on the 1st of January 2026
- `2026-<MONTH-2>-01` - Dynamic holiday: say at runtime the month is January. Then the holiday is on the 1st of November 2025
- `<YEAR>-01-01` - Dynamic holiday: say at runtime the year is 2026. Then the holiday is on the 1st of January 2026
- `<YEAR+1>-01-01` - Dynamic holiday: say at runtime the year is 2026. Then the holiday is on the 1st of January 2027

## Section 'inputData'

- `indicesType`: Type of indices used in the input files. Must be either 'NUM' (for numerical indices) or 'STR' (for alphanumerical indices, like UUIDs)
- `filenameTasks`: Path to the **Tasks** data file
- `useBasicInputData`: If true, the 'inputDataBasic' section will be used. Else the 'inputDataAdvanced' section will be used
- `inputDataBasic`:
	- `amountResourceUnits`: Amount of **Resource Units** to be used in the Critical Path computation
- `inputDataAdvanced`:
	- `filenameRunits`: Path to the **Resource Units** data file
	- `filenameRgroups`: Path to the **Resource Groups** data file
	- `filenameAssocRunitsWithRgroups`: Path to the file containing the associations between **Resource Units** and **Resource Groups**
	- `filenameAssocRgroupsWithTasks`: Path to the file containing the associations between **Resource Groups** and **Tasks**

All input filenames can be prefixed with 'rsc:' to indicate that they are located in the Java resources folder.

For more details on the input files see [csv_files.md](csv_files.md).
