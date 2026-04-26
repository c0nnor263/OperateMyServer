#!/bin/bash

### === SETTINGS ===
JAVA_EXE="java"
JAVA_ARGS="@user_jvm_args.txt @libraries/net/minecraftforge/forge/1.20.1-47.4.0/unix_args.txt"
CAUSE_FILE="oms/oms/common/stop_cause.json"

### === FUNCTIONS ===

start_server() {
  echo "[OMS] === Starting server ==="
  "$JAVA_EXE" $JAVA_ARGS
  echo "[OMS] Server exited with code $?"
}

read_cause() {
  if [[ -f "$CAUSE_FILE" ]]; then
    LAST_REASON=$(jq -r '.reason // "UNKNOWN"' "$CAUSE_FILE" 2>/dev/null)
    LAST_MESSAGE=$(jq -r '.message // "No message provided."' "$CAUSE_FILE" 2>/dev/null)
    echo "[OMS] Reason: $LAST_REASON"
    echo "[OMS] Message: $LAST_MESSAGE"
    rm -f "$CAUSE_FILE"
  else
    LAST_REASON="CRASH"
    LAST_MESSAGE="Cause file not found. Possible crash or force exit."
    echo "[OMS] Reason: $LAST_REASON"
    echo "[OMS] Message: $LAST_MESSAGE"
  fi
}

### === MAIN LOOP ===

while true; do
  start_server
  read_cause

  if [[ "$LAST_REASON" == "STOP" ]]; then
    echo "[OMS] Detected STOP. Exiting loop."
    break
  fi

  echo "[OMS] Relaunching server in 5 seconds..."
  sleep 5
done

echo "[OMS] Server script finished."