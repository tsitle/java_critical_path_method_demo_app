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
FN_A_CONFIG="rsc:config-a.json"
FN_A_OUTPUT_HTML="output-sample-a.html"
#
RSC_DIR="src/main/resources"
FN_B_NUM_CONFIG="$(pwd)/${RSC_DIR}/config-b-num.json"
FN_B_NUM_OUTPUT_HTML="output-sample-b-num.html"
#
FN_B_STR_CONFIG="rsc:config-b-str.json"
FN_B_STR_OUTPUT_HTML="output-sample-b-str.html"
#
FN_C_CONFIG="rsc:config-c.json"
FN_C_OUTPUT_HTML="output-sample-c.html"
#
FN_D_CONFIG="rsc:config-d.json"
FN_D_OUTPUT_HTML="output-sample-d.html"
#
FN_E_CONFIG="rsc:config-e-empty.json"
FN_E_OUTPUT_HTML="output-sample-e-empty.html"
#
FN_F_CONFIG="rsc:config-f.json"
FN_F_OUTPUT_HTML="output-sample-f.html"
#
FN_G_CONFIG="rsc:config-g.json"
FN_G_OUTPUT_HTML="output-sample-g.html"

# ------------------------------------------------------------
# Sample A
# ------------------------------------------------------------
#"${LVAR_GRADLE_EXE}" :run --args="'${FN_A_CONFIG}'" || exit 1

#"${LVAR_GRADLE_EXE}" :run --args="--output-html '${FN_A_OUTPUT_HTML}' '${FN_A_CONFIG}'" || exit 1

# ------------------------------------------------------------
# Sample B - NUM
# ------------------------------------------------------------
#"${LVAR_GRADLE_EXE}" :run --args="'${FN_B_NUM_CONFIG}'" || exit 1

#"${LVAR_GRADLE_EXE}" :run --args="--output-html '${FN_B_NUM_OUTPUT_HTML}' '${FN_B_NUM_CONFIG}'" || exit 1

# ------------------------------------------------------------
# Sample B - STR
# ------------------------------------------------------------
#"${LVAR_GRADLE_EXE}" :run --args="'${FN_B_STR_CONFIG}'" || exit 1

#"${LVAR_GRADLE_EXE}" :run --args="--output-html '${FN_B_STR_OUTPUT_HTML}' '${FN_B_STR_CONFIG}'" || exit 1

# ------------------------------------------------------------
# Sample C
# ------------------------------------------------------------
#"${LVAR_GRADLE_EXE}" :run --args="--output-html '${FN_C_OUTPUT_HTML}' '${FN_C_CONFIG}'" || exit 1

# ------------------------------------------------------------
# Sample D
# ------------------------------------------------------------
#"${LVAR_GRADLE_EXE}" :run --args="--output-html '${FN_D_OUTPUT_HTML}' '${FN_D_CONFIG}'" || exit 1

# ------------------------------------------------------------
# Sample E
# ------------------------------------------------------------
#"${LVAR_GRADLE_EXE}" :run --args="--output-html '${FN_E_OUTPUT_HTML}' '${FN_E_CONFIG}'" || exit 1

# ------------------------------------------------------------
# Sample F
# ------------------------------------------------------------
#"${LVAR_GRADLE_EXE}" :run --args="--output-html '${FN_F_OUTPUT_HTML}' '${FN_F_CONFIG}'" || exit 1

# ------------------------------------------------------------
# Sample G
# ------------------------------------------------------------
"${LVAR_GRADLE_EXE}" :run --args="--output-html '${FN_G_OUTPUT_HTML}' '${FN_G_CONFIG}'" || exit 1
