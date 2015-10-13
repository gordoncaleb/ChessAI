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
Iteration   1: 96462898.273 ns/op
Iteration   2: 84650550.000 ns/op
Iteration   3: 77083667.357 ns/op

# Run progress: 10.00% complete, ETA 00:00:32
# Fork: 2 of 10
Iteration   1: 94425147.545 ns/op
Iteration   2: 76226091.929 ns/op
Iteration   3: 77971377.154 ns/op

# Run progress: 20.00% complete, ETA 00:00:28
# Fork: 3 of 10
Iteration   1: 97281154.909 ns/op
Iteration   2: 77887321.231 ns/op
Iteration   3: 78858198.154 ns/op

# Run progress: 30.00% complete, ETA 00:00:24
# Fork: 4 of 10
Iteration   1: 110522682.100 ns/op
Iteration   2: 99741077.455 ns/op
Iteration   3: 78164667.385 ns/op

# Run progress: 40.00% complete, ETA 00:00:21
# Fork: 5 of 10
Iteration   1: 106775052.600 ns/op
Iteration   2: 87849036.167 ns/op
Iteration   3: 78977815.154 ns/op

# Run progress: 50.00% complete, ETA 00:00:17
# Fork: 6 of 10
Iteration   1: 93464623.909 ns/op
Iteration   2: 84748259.000 ns/op
Iteration   3: 76711852.786 ns/op

# Run progress: 60.00% complete, ETA 00:00:14
# Fork: 7 of 10
Iteration   1: 94162097.000 ns/op
Iteration   2: 84634984.250 ns/op
Iteration   3: 76132933.571 ns/op

# Run progress: 70.00% complete, ETA 00:00:10
# Fork: 8 of 10
Iteration   1: 105684835.000 ns/op
Iteration   2: 78333269.692 ns/op
Iteration   3: 77600845.769 ns/op

# Run progress: 80.00% complete, ETA 00:00:07
# Fork: 9 of 10
Iteration   1: 98603476.455 ns/op
Iteration   2: 77374044.692 ns/op
Iteration   3: 78100395.077 ns/op

# Run progress: 90.00% complete, ETA 00:00:03
# Fork: 10 of 10
Iteration   1: 93963665.545 ns/op
Iteration   2: 86854687.167 ns/op
Iteration   3: 77527643.923 ns/op


Result "testMoveGeneration1":
  86892478.375 ±(99.9%) 7057204.303 ns/op [Average]
  (min, avg, max) = (76132933.571, 86892478.375, 110522682.100), stdev = 10562891.971
  CI (99.9%): [79835274.071, 93949682.678] (assumes normal distribution)


# Run complete. Total time: 00:00:35

Benchmark                                    Mode  Cnt         Score         Error  Units
MoveGenerationBenchmark.testMoveGeneration1  avgt   30  86892478.375 ± 7057204.303  ns/op
