#!/bin/bash
set -e
echo "生成依赖树到文件dependencies.txt中,每次建release时,以及发版后merge到master之前运行此脚本一次,便于通过git追踪各版本依赖的变化"
./gradlew :app:dependencies --configuration releaseRuntimeClasspath > dependencies.txt