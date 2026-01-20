#!/usr/bin/env bash

LVAR_GRADLE_EXE="gradle"

if [ -x "./gradlew" ]; then
	LVAR_GRADLE_EXE="./gradlew"
elif ! command -v "${LVAR_GRADLE_EXE}" >/dev/null 2>&1; then
	echo "$0: could not find executable '${LVAR_GRADLE_EXE}'. Aborting" >&2
	exit 1
fi

"${LVAR_GRADLE_EXE}" :compileJava || exit 1

# ------------------------------------------------------------
# Filenames
# ------------------------------------------------------------
FN_A_TASKS="rsc:sample-a-tasks-w03-m065-num.csv"
FN_A_OUTPUT_HTML="output-sample-a.html"
#
RSC_DIR="src/main/resources"
FN_B_TASKS="$(pwd)/${RSC_DIR}/sample-b-tasks-w04-m013-num.csv"
FN_B_OUTPUT_HTML="output-sample-b.html"
#
FN_C_TASKS="rsc:sample-c-tasks-num.csv"
FN_C_RUNITS="rsc:sample-c-runits-num.csv"
FN_C_RGROUPS="rsc:sample-c-rgroups-num.csv"
FN_C_ASSOC_R2R="rsc:sample-c-assoc_runits_w_rgroups-num.csv"
FN_C_ASSOC_R2T="rsc:sample-c-assoc_rgroups_w_tasks-num.csv"
FN_C_OUTPUT_HTML="output-sample-c.html"
#
FN_D_TASKS="rsc:sample-d-tasks-str.csv"
FN_D_RUNITS="rsc:sample-d-runits-str.csv"
FN_D_RGROUPS="rsc:sample-d-rgroups-str.csv"
FN_D_ASSOC_R2R="rsc:sample-d-assoc_runits_w_rgroups-str.csv"
FN_D_ASSOC_R2T="rsc:sample-d-assoc_rgroups_w_tasks-str.csv"
FN_D_OUTPUT_HTML="output-sample-d.html"
#
FN_E_TASKS="rsc:sample-e-tasks-empty.csv"
FN_E_RUNITS="rsc:sample-e-runits-empty.csv"
FN_E_RGROUPS="rsc:sample-e-rgroups-empty.csv"
FN_E_ASSOC_R2R="rsc:sample-e-assoc_runits_w_rgroups-empty.csv"
FN_E_ASSOC_R2T="rsc:sample-e-assoc_rgroups_w_tasks-empty.csv"
FN_E_OUTPUT_HTML="output-sample-e.html"

# ------------------------------------------------------------
# Sample A
# ------------------------------------------------------------
#"${LVAR_GRADLE_EXE}" :run --args="NUM '${FN_A_TASKS}' 3" || exit 1

#"${LVAR_GRADLE_EXE}" :run --args="NUM '${FN_A_TASKS}' 3 'of=${FN_A_OUTPUT_HTML}'" || exit 1

# ------------------------------------------------------------
# Sample B
# ------------------------------------------------------------
#"${LVAR_GRADLE_EXE}" :run --args="NUM '${FN_B_TASKS}' 4" || exit 1

#"${LVAR_GRADLE_EXE}" :run --args="NUM '${FN_B_TASKS}' 4 'of=${FN_B_OUTPUT_HTML}'" || exit 1

# ------------------------------------------------------------
# Sample C
# ------------------------------------------------------------
#"${LVAR_GRADLE_EXE}" :run --args="NUM '${FN_C_TASKS}' '${FN_C_RUNITS}' '${FN_C_RGROUPS}' '${FN_C_ASSOC_R2R}' '${FN_C_ASSOC_R2T}'" || exit 1

"${LVAR_GRADLE_EXE}" :run --args="NUM '${FN_C_TASKS}' '${FN_C_RUNITS}' '${FN_C_RGROUPS}' '${FN_C_ASSOC_R2R}' '${FN_C_ASSOC_R2T}' 'of=${FN_C_OUTPUT_HTML}'" || exit 1

# ------------------------------------------------------------
# Sample D
# ------------------------------------------------------------
#"${LVAR_GRADLE_EXE}" :run --args="STR '${FN_D_TASKS}' '${FN_D_RUNITS}' '${FN_D_RGROUPS}' '${FN_D_ASSOC_R2R}' '${FN_D_ASSOC_R2T}'" || exit 1

#"${LVAR_GRADLE_EXE}" :run --args="STR '${FN_D_TASKS}' '${FN_D_RUNITS}' '${FN_D_RGROUPS}' '${FN_D_ASSOC_R2R}' '${FN_D_ASSOC_R2T}' 'of=${FN_D_OUTPUT_HTML}'" || exit 1

# ------------------------------------------------------------
# Sample E
# ------------------------------------------------------------
#"${LVAR_GRADLE_EXE}" :run --args="STR '${FN_E_TASKS}' '${FN_E_RUNITS}' '${FN_E_RGROUPS}' '${FN_E_ASSOC_R2R}' '${FN_E_ASSOC_R2T}'" || exit 1

#"${LVAR_GRADLE_EXE}" :run --args="STR '${FN_E_TASKS}' '${FN_E_RUNITS}' '${FN_E_RGROUPS}' '${FN_E_ASSOC_R2R}' '${FN_E_ASSOC_R2T}' 'of=${FN_E_OUTPUT_HTML}'" || exit 1
