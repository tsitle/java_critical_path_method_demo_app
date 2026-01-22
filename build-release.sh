#!/usr/bin/env bash

#
# by TS, Jan 2026
#

LCFG_DISTPRE_DIR="distPre"
LCFG_APP_VERSION="1.0"
LCFG_ZIP_FN="cpm_demo-${LCFG_APP_VERSION}.zip"

# ----------------------------------------------------------------------------------------------------------------------

if [ -z "${LCFG_DISTPRE_DIR}" ]; then
	echo "$0: empty output dir 'LCFG_DISTPRE_DIR'. Aborting" >&2
	exit 1
fi
if [ -z "${LCFG_APP_VERSION}" ]; then
	echo "$0: empty version string 'LCFG_APP_VERSION'. Aborting" >&2
	exit 1
fi
if [ -z "${LCFG_ZIP_FN}" ]; then
	echo "$0: empty filename 'LCFG_ZIP_FN'. Aborting" >&2
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

# ----------------------------------------------------------------------------------------------------------------------

"${LVAR_GRADLE_EXE}" :jlinkZip || exit 1

if [ ! -f "build/${LCFG_ZIP_FN}" ]; then
	echo "$0: could not find launcher ZIP 'build/${LCFG_ZIP_FN}'. Aborting" >&2
	exit 1
fi

mv "build/${LCFG_ZIP_FN}" "${LCFG_DISTPRE_DIR}/" || exit 1

echo "$0: moved launcher ZIP to ${LCFG_DISTPRE_DIR}/${LCFG_ZIP_FN}"
