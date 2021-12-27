#!/bin/bash
set -e

# 在项目根目录下运行: sh deps/deps.sh

echo "当前目录:"$PWD

echo "debug依赖更新一览表(比较耗时)"
gradlew dependencyUpdates -Drevision=debug -DoutputFormatter=html -DoutputDir=../deps/debug -DreportfileName=debug-deps-updates