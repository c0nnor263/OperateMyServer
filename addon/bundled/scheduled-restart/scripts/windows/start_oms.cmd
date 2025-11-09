@echo off
setlocal enabledelayedexpansion

REM === SETTINGS ===
set "JAVA_EXE=java"
set "JAVA_ARGS=@user_jvm_args.txt @libraries/net/minecraftforge/forge/1.20.1-47.4.0/win_args.txt"
set "CAUSE_FILE=oms\oms\common\stop_cause.json"

REM === FUNCTIONS ===

:read_cause
set "LAST_REASON=UNKNOWN"
set "LAST_MESSAGE=No message provided."

if exist "%CAUSE_FILE%" (
  for /f "usebackq delims=" %%i in (`powershell -NoProfile -Command ^
    "(Get-Content -Raw '%CAUSE_FILE%') | ConvertFrom-Json | Select-Object -ExpandProperty reason"`) do (
    set "LAST_REASON=%%i"
  )
  for /f "usebackq delims=" %%i in (`powershell -NoProfile -Command ^
    "(Get-Content -Raw '%CAUSE_FILE%') | ConvertFrom-Json | Select-Object -ExpandProperty message"`) do (
    set "LAST_MESSAGE=%%i"
  )
  echo [OMS] Reason: !LAST_REASON!
  echo [OMS] Message: !LAST_MESSAGE!
  del "%CAUSE_FILE%" >nul 2>&1
) else (
  echo [OMS] No cause file found. Assuming CRASH.
)

REM === MAIN LOOP ===

:main_loop
echo [OMS] === Starting server ===
"%JAVA_EXE%" %JAVA_ARGS%
echo [OMS] Server exited with code %errorlevel%.

call :read_cause

if /I "!LAST_REASON!"=="STOP" (
  echo [OMS] STOP detected. Exiting loop.
  goto end
)

echo [OMS] Relaunching server in 5 seconds...
timeout /t 5 /nobreak >nul
goto main_loop

:end
echo [OMS] Done.
endlocal
pause