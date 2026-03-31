#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
mvn -q compile
exec java -cp target/classes com.flipperdemo.sse.SseHttpServer "$@"
