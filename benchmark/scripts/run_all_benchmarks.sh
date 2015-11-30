#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

pushd $DIR/..

function runBenchmark () {
    echo "Running $1"

    java -cp target/ohword-benchmark-0.1-SNAPSHOT.jar:target/benchmarks.jar com.gordoncaleb.$1 | tee results/$1.md
}

#runBenchmark "PerftBenchmark"
runBenchmark "MoveGenerationBenchmark"
#runBenchmark "StaticScoreBenchmark"

echo "All benchmark tests complete"
popd