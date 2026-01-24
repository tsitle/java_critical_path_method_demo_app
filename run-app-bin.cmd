@REM ----------------------------------------------------------------------------
@REM Run portable app
@REM
@REM by TS, Jan 2026
@REM ----------------------------------------------------------------------------

@echo off

@setlocal

set LCFG_APP_VERSION=1.0.1
set LCFG_BIN_EXE=build\cpm_demo-win-x64-%LCFG_APP_VERSION%\bin\cpm_demo

@REM ------------------------------------------------------------

set LVAR_INT_ERROR_CODE=0
SET LVAR_PWD=%cd%

@REM ------------------------------------------------------------
@REM Build app if necessary
@REM ------------------------------------------------------------

if exist "%LCFG_BIN_EXE%" goto init

if [%JAVA_HOME%] == [] goto TryDefaultJavaHome
goto GradleBuild

:TryDefaultJavaHome

set JAVA_HOME=C:\java\openjdk-22.0.2-win_x64
if exist "%JAVA_HOME%" goto GradleBuild

echo You need to set the environment variable JAVA_HOME in the Windows settings first >&2
goto HaveError

:GradleBuild

call gradlew jlink
if ERRORLEVEL 1 goto HaveError

:init

echo.
echo Run "%LCFG_BIN_EXE%"
echo.

@REM ------------------------------------------------------------
@REM Filenames
@REM ------------------------------------------------------------

set FN_A_CONFIG=rsc:config-a.json
set FN_A_OUTPUT_HTML=output-sample-a.html

set FN_G_CONFIG=rsc:config-g.json
set FN_G_OUTPUT_HTML=output-sample-g.html

@REM ------------------------------------------------------------
@REM Sample A
@REM ------------------------------------------------------------

@REM call "%LCFG_BIN_EXE%" --output-html "%LVAR_PWD%\%FN_A_OUTPUT_HTML%" "%FN_A_CONFIG%"
@REM if ERRORLEVEL 1 goto HaveError

@REM ------------------------------------------------------------
@REM Sample G
@REM ------------------------------------------------------------

call "%LCFG_BIN_EXE%" --output-html "%LVAR_PWD%\%FN_G_OUTPUT_HTML%" "%FN_G_CONFIG%"
if ERRORLEVEL 1 goto HaveError

@REM ------------------------------------------------------------

goto TheEnd

:HaveError

echo. >&2
echo Aborting >&2
echo. >&2

set LVAR_INT_ERROR_CODE=1

:TheEnd

pause
exit /B %LVAR_INT_ERROR_CODE%
