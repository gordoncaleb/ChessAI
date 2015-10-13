# JMH 1.11.1 (released 18 days ago)
# VM version: JDK 1.8.0_60, VM 25.60-b23
# VM invoker: /Library/Java/JavaVirtualMachines/jdk1.8.0_60.jdk/Contents/Home/jre/bin/java
# VM options: <none>
# Warmup: <none>
# Measurement: 3 iterations, 1 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Average time, time/op
# Benchmark: com.gordoncaleb.MoveGenerationBenchmark.testMoveGeneration1

# Run progress: 0.00% complete, ETA 00:00:30
# Fork: 1 of 10
Iteration   1: 75389861.643 ns/op
Iteration   2: 69476808.667 ns/op
Iteration   3: 70204817.133 ns/op

# Run progress: 10.00% complete, ETA 00:00:31
# Fork: 2 of 10
Iteration   1: 74889902.929 ns/op
Iteration   2: 68444109.867 ns/op
Iteration   3: 68182265.333 ns/op

# Run progress: 20.00% complete, ETA 00:00:27
# Fork: 3 of 10
Iteration   1: 74259897.643 ns/op
Iteration   2: 68135109.733 ns/op
Iteration   3: 68933987.000 ns/op

# Run progress: 30.00% complete, ETA 00:00:24
# Fork: 4 of 10
Iteration   1: 74581236.286 ns/op
Iteration   2: 68645275.400 ns/op
Iteration   3: 66758389.333 ns/op

# Run progress: 40.00% complete, ETA 00:00:20
# Fork: 5 of 10
Iteration   1: 73812523.643 ns/op
Iteration   2: 68344951.867 ns/op
Iteration   3: 67089725.467 ns/op

# Run progress: 50.00% complete, ETA 00:00:17
# Fork: 6 of 10
Iteration   1: 74180014.143 ns/op
Iteration   2: 70524737.267 ns/op
Iteration   3: 69400004.400 ns/op

# Run progress: 60.00% complete, ETA 00:00:13
# Fork: 7 of 10
Iteration   1: 79985505.000 ns/op
Iteration   2: 70163486.067 ns/op
Iteration   3: 67411502.400 ns/op

# Run progress: 70.00% complete, ETA 00:00:10
# Fork: 8 of 10
Iteration   1: 76009863.786 ns/op
Iteration   2: 68646756.600 ns/op
Iteration   3: 67840976.267 ns/op

# Run progress: 80.00% complete, ETA 00:00:06
# Fork: 9 of 10
Iteration   1: 74635435.000 ns/op
Iteration   2: 68902741.333 ns/op
Iteration   3: 67703070.067 ns/op

# Run progress: 90.00% complete, ETA 00:00:03
# Fork: 10 of 10
Iteration   1: 73693673.214 ns/op
Iteration   2: 67895519.133 ns/op
Iteration   3: 67011744.438 ns/op


Result "testMoveGeneration1":
  70705129.702 ±(99.9%) 2314569.005 ns/op [Average]
  (min, avg, max) = (66758389.333, 70705129.702, 79985505.000), stdev = 3464338.187
  CI (99.9%): [68390560.696, 73019698.707] (assumes normal distribution)


# Run complete. Total time: 00:00:34

Benchmark                                    Mode  Cnt         Score         Error  Units
MoveGenerationBenchmark.testMoveGeneration1  avgt   30  70705129.702 ± 2314569.005  ns/op
