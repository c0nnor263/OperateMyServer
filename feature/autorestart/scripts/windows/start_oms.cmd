@echo off
setlocal enabledelayedexpansion

REM === SETTINGS ===
set "JAVA_EXE=java"
set "JAVA_ARGS=@user_jvm_args.txt @libraries/net/minecraftforge/forge/1.20.1-47.4.0/win_args.txt"
set "CAUSE_FILE=oms\stop_cause.json"

REM === FUNCTIONS ===

:read_cause
if exist "%CAUSE_FILE%" (
  for /f "usebackq delims=" %%i in (`powershell -NoProfile -Command ^
    "(Get-Content -Raw '%CAUSE_FILE%') | ConvertFrom-Json | Select-Object -ExpandProperty state"`) do (
    set "LAST_STATE=%%i"
  )
  echo [OMS] Last state: !LAST_STATE!
  del "%CAUSE_FILE%" >nul 2>&1
) else (
  set "LAST_STATE="
)

REM === MAIN LOOP ===

:main_loop
echo [OMS] === Starting server ===
"%JAVA_EXE%" %JAVA_ARGS%
echo [OMS] Server exited with code %errorlevel%.

call :read_cause

if /I "!LAST_STATE!"=="STOP" (
  echo [OMS] STOP detected. Exiting loop.
  goto end
)

if /I "!LAST_STATE!"=="SCHEDULED" (
  echo [OMS] Scheduled restart. Relaunch in 5 seconds...
  timeout /t 5 /nobreak >nul
  goto main_loop
)

if /I "!LAST_STATE!"=="MANUAL" (
  echo [OMS] Manual restart. Relaunch in 5 seconds...
  timeout /t 5 /nobreak >nul
  goto main_loop
)

if /I "!LAST_STATE!"=="CRASH" (
  echo [OMS] Crash restart. Relaunch in 5 seconds...
  timeout /t 5 /nobreak >nul
  goto main_loop
)

if /I "!LAST_STATE!"=="UNKNOWN" (
  echo [OMS] UNKNOWN state. Relaunch in 5 seconds...
  timeout /t 5 /nobreak >nul
  goto main_loop
)

if "!LAST_STATE!"=="" (
  echo [OMS] Cause file not found. CRASH assumed. Relaunch in 5 seconds...
  timeout /t 5 /nobreak >nul
  goto main_loop
)

echo [OMS] Unknown state "!LAST_STATE!". Treating as CRASH. Relaunch in 5 seconds...
timeout /t 5 /nobreak >nul
goto main_loop

:end
echo [OMS] Done.
endlocal
pause