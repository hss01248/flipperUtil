#!/bin/bash
set -e

# 在项目根目录下运行: sh deps/deps.sh

echo "当前目录:"$PWD


echo "release依赖更新一览表(比较耗时)"
gradlew dependencyUpdates -Drevision=release -DoutputFormatter=html -DoutputDir=../deps/release -DreportfileName=release-deps-updates
