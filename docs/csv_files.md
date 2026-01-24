# CSV Files For Input Data

All input files must be CSV files with specific column names.  
The order of the columns does not matter, and they are case-insensitive.

Column separator: ',' (comma)  
String separator: '"' (double quote)  
Encoding: UTF-8

All IDs must be in the same format as the `indicesType` specified in the configuration file.

A **Resource Unit** may be associated with multiple **Resource Groups**.  
But every **Task** must be associated with a **Resource Group**.  
Every **Resource Group** that is associated with a **Task** must be associated with at least one **Resource Unit**.

IDs only have to be unique within their respective data type.  
So for instance, a **Resource Unit** with ID `100` and a **Resource Group** with ID `100`
can coexist without a conflict.

## Input File Formats

### for Tasks

Required columns:

- `TaskId`: Unique identifier of the **Task**. E.g. `10` or `"ABC-10"`
- `Name`: Optional name of the **Task**. E.g. `Task A`
- `Duration`: Duration of the **Task** in the time unit defined in the configuration. E.g. `60`
- `DependenciesTaskIdList`: Optional list of **Task** IDs that this **Task** depends on. E.g. `20`, `20:30`, `"ABC-10":"DEF-20"` or `none`
- `StartedAtUTC`: Optional start date (and time) of the **Task** in the UTC time zone. E.g. `2026-03-20T13:52:00Z`, `2026-03-20 13:52:00` or `2026-03-20`
- `FinishedAtUTC`: Optional finish date (and time) of the **Task** in the UTC time zone

The date/times are expected to be in the UTC time zone and will automatically
be converted to the local time zone.

**Example file**:

``` csv
TaskId,Name,Duration,DependenciesTaskIdList,StartedAtUTC,FinishedAtUTC
10,"Task A",3,none,,
20,"Task B",2,none,,
30,"Task C",5,10:20,,
```

### for Resource Units

Required columns:

- `RunitId`: Unique identifier of the **Resource Unit**. E.g. `201` or `"RU-201"`
- `Name`: Optional name of the **Resource Unit**. E.g. `Printer 1st Floor` or `John Doe`

**Example file**:

``` csv
RunitId,Name
101,"Printer 1st Floor"
102,"Printer 2nd Floor"
201,"Copier Foyer"
```

### for Resource Groups

Required columns:

- `RgroupId`: Unique identifier of the **Resource Group**. E.g. `3000` or `"RG-3000"`
- `Name`: Optional name of the **Resource Group**. E.g. `Printers` or `Backend Developers`

**Example file**:

``` csv
RgroupId,Name
1000,"Printers"
2000,"Copiers"
```

### for Associations between Resource Units and Resource Groups

Required columns:

- `RgroupId`: Unique identifier of the **Resource Group**
- `RunitsIdList`: Optional list of IDs of **Resource Units** belonging to the **Resource Group**. E.g. `201`, `101:201` or `"RU-101":"RU-201"`

**Example file**:

``` csv
RgroupId,RunitsIdList
1000,101:102
2000,201
```

### for Associations between Resource Groups and Tasks

Required columns:

- `TaskId`: Unique identifier of the **Task**
- `RgroupId`: Unique identifier of the **Resource Group** that is responsible for the **Task**

**Example file**:

``` csv
TaskId,RgroupId
10,1000
20,1000
30,2000
```
