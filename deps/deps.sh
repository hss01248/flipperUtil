#!/bin/bash
set -e

# 在项目根目录下运行: sh deps/deps.sh

echo "当前目录:"$PWD

echo "生成依赖列表到release-deps-release.gradle中,便于查看新旧版本,每次建release时,以及发版后merge到master之前运行此脚本一次,便于通过git追踪各版本依赖的变化"
gradlew showDependenciesRelease
echo "生成依赖树到文件release-depesTree.txt中,每次建release时,以及发版后merge到master之前运行此脚本一次,便于通过git追踪各版本依赖的变化"
gradlew :app:dependencies --configuration releaseRuntimeClasspath > deps/release/release-depesTree.txt

echo "生成依赖列表到debug-deps.gradle中"
gradlew showDependenciesDebug
echo "生成依赖树到文件debug-depesTree.txt中"
gradlew  >> deps/debug/debug-depesTree.txt :app:dependencies --configuration debugRuntimeClasspath
