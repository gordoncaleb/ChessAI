#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

pushd $DIR/..
mvn exec:java -Dexec.mainClass=com.gordoncaleb.chess.io.MoveBook
popd