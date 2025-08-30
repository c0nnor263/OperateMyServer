#!/bin/bash

### === SETTINGS ===
# Path to Java
JAVA_EXE="java"
# JVM args
JAVA_ARGS="@user_jvm_args.txt @libraries/net/minecraftforge/forge/1.20.1-47.4.0/unix_args.txt"
# File with stop cause written by OMS
CAUSE_FILE="oms/stop_cause.json"

### === FUNCTIONS ===

start_server() {
  echo "[OMS] === Starting server ==="
  "$JAVA_EXE" $JAVA_ARGS
  echo "[OMS] Server exited with code $?"
}

read_cause() {
  if [[ -f "$CAUSE_FILE" ]]; then
    LAST_STATE=$(jq -r '.state' "$CAUSE_FILE" 2>/dev/null)
    echo "[OMS] Last state: $LAST_STATE"
    rm -f "$CAUSE_FILE"
  else
    LAST_STATE=""
  fi
}

### === MAIN LOOP ===
while true; do
  start_server
  read_cause

  case "$LAST_STATE" in
    STOP)
      echo "[OMS] STOP detected. Exiting loop."
      break
      ;;
    SCHEDULED)
      echo "[OMS] Scheduled restart. Relaunch in 5 seconds..."
      sleep 5
      ;;
    MANUAL)
      echo "[OMS] Manual restart. Relaunch in 5 seconds..."
      sleep 5
      ;;
    CRASH)
      echo "[OMS] Crash restart. Relaunch in 5 seconds..."
      sleep 5
      ;;
    UNKNOWN)
      echo "[OMS] UNKNOWN state. Relaunch in 5 seconds..."
      sleep 5
      ;;
    "")
      echo "[OMS] Cause file not found. CRASH assumed. Relaunch in 5 seconds..."
      sleep 5
      ;;
    *)
      echo "[OMS] Unknown state '$LAST_STATE'. Treating as CRASH. Relaunch in 5 seconds..."
      sleep 5
      ;;
  esac
done

echo "[OMS] Done"