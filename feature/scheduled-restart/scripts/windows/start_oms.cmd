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
set "LAST_SHOULD_RESTART=false"

if exist "%CAUSE_FILE%" (
  for /f "usebackq delims=" %%i in (`powershell -NoProfile -Command ^
    "$json = Get-Content -Raw '%CAUSE_FILE%' | ConvertFrom-Json; if ($null -ne $json.reason) { $json.reason } else { 'UNKNOWN' }"`) do (
    set "LAST_REASON=%%i"
  )
  for /f "usebackq delims=" %%i in (`powershell -NoProfile -Command ^
    "$json = Get-Content -Raw '%CAUSE_FILE%' | ConvertFrom-Json; if ($null -ne $json.message) { $json.message } else { 'No message provided.' }"`) do (
    set "LAST_MESSAGE=%%i"
  )
  for /f "usebackq delims=" %%i in (`powershell -NoProfile -Command ^
    "$json = Get-Content -Raw '%CAUSE_FILE%' | ConvertFrom-Json; if ($null -ne $json.shouldRestart) { $json.shouldRestart } else { $false }"`) do (
    set "LAST_SHOULD_RESTART=%%i"
  )
  echo [OMS] Reason: !LAST_REASON!
  echo [OMS] Message: !LAST_MESSAGE!
  echo [OMS] Should restart: !LAST_SHOULD_RESTART!
  del "%CAUSE_FILE%" >nul 2>&1
) else (
  set "LAST_REASON=CRASH"
  set "LAST_MESSAGE=Cause file not found. Possible crash or force exit."
  set "LAST_SHOULD_RESTART=true"

  echo [OMS] Reason: !LAST_REASON!
  echo [OMS] Message: !LAST_MESSAGE!
  echo [OMS] Should restart: !LAST_SHOULD_RESTART!
)
goto :eof

REM === MAIN LOOP ===

:main_loop
echo [OMS] === Starting server ===
"%JAVA_EXE%" %JAVA_ARGS%
set "LAST_EXIT_CODE=%errorlevel%"
echo [OMS] Server exited with code !LAST_EXIT_CODE!.

call :read_cause

if /I not "!LAST_SHOULD_RESTART!"=="true" (
  echo [OMS] Restart not requested. Exiting loop.
  goto end
)

echo [OMS] Relaunching server in 5 seconds...
timeout /t 5 /nobreak >nul
goto main_loop

:end
echo [OMS] Done.
endlocal
pause