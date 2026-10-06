#!/usr/bin/env bash
# Runs Gradle with JDK 25 (Arch: sudo pacman -S jdk25-openjdk).
# Loom needs Java >= 25 and Gradle 9.7 can't run on 27, so the default
# Java on this machine won't do. Usage:
#   ./build.sh              # build, jar lands in build/libs/
#   ./build.sh runClient    # launch a dev client with the mod
#   ./build.sh <tasks...>   # any other Gradle tasks
set -euo pipefail
cd "$(dirname "$0")"

export JAVA_HOME="${JAVA_HOME_25:-/usr/lib/jvm/java-25-openjdk}"
if [[ ! -x "$JAVA_HOME/bin/java" ]]; then
	echo "No JDK 25 at $JAVA_HOME. Install jdk25-openjdk or set JAVA_HOME_25." >&2
	exit 1
fi

if [[ $# -eq 0 ]]; then
	set -- build
fi
exec ./gradlew "$@"
