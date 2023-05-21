#!/bin/bash

SCRIPT=$(realpath "$0")
SCRIPT_FOLDER=$(dirname "$SCRIPT")
OUTPUT_FOLDER="$SCRIPT_FOLDER/../src/"

java -jar Coco.jar "$SCRIPT_FOLDER"/Mini.atg -o "$OUTPUT_FOLDER"