#!/usr/bin/env bash

#
# Run portable app
#
# by TS, Jan 2026
#

LCFG_APP_VERSION="1.0.1"
LCFG_BIN_EXE_TEMPLATE="build/cpm_demo-###OS###-###ARCH###-${LCFG_APP_VERSION}/bin/cpm_demo"

# ----------------------------------------------------------------------------------------------------------------------

if [ -z "${LCFG_APP_VERSION}" ]; then
	echo "$0: empty version 'LCFG_APP_VERSION'. Aborting" >&2
	exit 1
fi
if [ -z "${LCFG_BIN_EXE_TEMPLATE}" ]; then
	echo "$0: empty filename 'LCFG_BIN_EXE_TEMPLATE'. Aborting" >&2
	exit 1
fi

# ----------------------------------------------------------------------------------------------------------------------

LVAR_GRADLE_EXE="gradle"

if [ -x "./gradlew" ]; then
	LVAR_GRADLE_EXE="./gradlew"
elif ! command -v "${LVAR_GRADLE_EXE}" >/dev/null 2>&1; then
	echo "$0: could not find executable '${LVAR_GRADLE_EXE}'. Aborting" >&2
	exit 1
fi

if [ ! -x "${LVAR_BIN_EXE}" ]; then
	"${LVAR_GRADLE_EXE}" :jlink || exit 1
fi

# ----------------------------------------------------------------------------------------------------------------------

_getCpuArch() {
	case "$(uname -m)" in
		x86_64*)
				echo -n "x64"
				;;
		i686*)
				echo -n "x86"
				;;
		arm64*|aarch64*)  # macOS:arm64, linux:aarch64
				echo -n "aarch64"
				;;
		armv7*)
				echo -n "armhf"
				;;
		*)
				echo "Error: Unknown CPU architecture '$(uname -m)'" >>/dev/stderr
				return 1
				;;
	esac
	return 0
}

_getCpuArch >/dev/null || exit 1

_getOsType() {
	case "${OSTYPE}" in
		linux*) echo -n "linux";;
		darwin*) echo -n "macos";;
		*)
			echo "$0: Error: Unknown OSTYPE '$OSTYPE'" >&2
			return 1
			;;
	esac
	return 0
}

_getOsType >/dev/null || exit 1

LVAR_OS="$(_getOsType)"
LVAR_ARCH="$(_getCpuArch)"

# ----------------------------------------------------------------------------------------------------------------------

LVAR_BIN_EXE="$(echo -n "${LCFG_BIN_EXE_TEMPLATE}" | sed -e "s/###OS###/${LVAR_OS}/" -e "s/###ARCH###/${LVAR_ARCH}/")"

if [ ! -x "${LVAR_BIN_EXE}" ]; then
	echo "$0: could not find executable '${LVAR_BIN_EXE}'. Aborting" >&2
	exit 1
fi

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
#"${LVAR_BIN_EXE}" "${FN_A_CONFIG}" || exit 1

#"${LVAR_BIN_EXE}" --output-html "${FN_A_OUTPUT_HTML}" "${FN_A_CONFIG}" || exit 1

# ------------------------------------------------------------
# Sample B - NUM
# ------------------------------------------------------------
#"${LVAR_BIN_EXE}" "${FN_B_NUM_CONFIG}" || exit 1

#"${LVAR_BIN_EXE}" --output-html "${FN_B_NUM_OUTPUT_HTML}" "${FN_B_NUM_CONFIG}" || exit 1

# ------------------------------------------------------------
# Sample B - STR
# ------------------------------------------------------------
#"${LVAR_BIN_EXE}" "${FN_B_STR_CONFIG}" || exit 1

#"${LVAR_BIN_EXE}" --output-html "${FN_B_STR_OUTPUT_HTML}" "${FN_B_STR_CONFIG}" || exit 1

# ------------------------------------------------------------
# Sample C
# ------------------------------------------------------------
#"${LVAR_BIN_EXE}" --output-html "${FN_C_OUTPUT_HTML}" "${FN_C_CONFIG}" || exit 1

# ------------------------------------------------------------
# Sample D
# ------------------------------------------------------------
#"${LVAR_BIN_EXE}" --output-html "${FN_D_OUTPUT_HTML}" "${FN_D_CONFIG}" || exit 1

# ------------------------------------------------------------
# Sample E
# ------------------------------------------------------------
#"${LVAR_BIN_EXE}" --output-html "${FN_E_OUTPUT_HTML}" "${FN_E_CONFIG}" || exit 1

# ------------------------------------------------------------
# Sample F
# ------------------------------------------------------------
#"${LVAR_BIN_EXE}" --output-html "${FN_F_OUTPUT_HTML}" "${FN_F_CONFIG}" || exit 1

# ------------------------------------------------------------
# Sample G
# ------------------------------------------------------------
"${LVAR_BIN_EXE}" --output-html "${FN_G_OUTPUT_HTML}" "${FN_G_CONFIG}" || exit 1
