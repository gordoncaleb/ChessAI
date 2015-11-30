# JMH 1.11.1 (released 66 days ago)
# VM version: JDK 1.8.0_60, VM 25.60-b23
# VM invoker: /Library/Java/JavaVirtualMachines/jdk1.8.0_60.jdk/Contents/Home/jre/bin/java
# VM options: <none>
# Warmup: 1 iterations, 1 s each, 5000 calls per op
# Measurement: 5 iterations, 1 s each, 5000 calls per op
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Average time, time/op
# Benchmark: com.gordoncaleb.MoveGenerationBenchmark.testMoveGeneration1

# Run progress: 0.00% complete, ETA 00:01:00
# Fork: 1 of 10
# Warmup Iteration   1: 2803961.601 ns/op
Iteration   1: 2673016.269 ns/op
Iteration   2: 2706191.445 ns/op
Iteration   3: 2663471.452 ns/op
Iteration   4: 2671248.093 ns/op
Iteration   5: 2672431.320 ns/op

# Run progress: 10.00% complete, ETA 00:00:57
# Fork: 2 of 10
# Warmup Iteration   1: 2797064.721 ns/op
Iteration   1: 2649562.198 ns/op
Iteration   2: 2661604.963 ns/op
Iteration   3: 2658361.233 ns/op
Iteration   4: 2632167.654 ns/op
Iteration   5: 2680190.747 ns/op

# Run progress: 20.00% complete, ETA 00:00:51
# Fork: 3 of 10
# Warmup Iteration   1: 2258590.269 ns/op
Iteration   1: 2165619.856 ns/op
Iteration   2: 2359270.242 ns/op
Iteration   3: 2589931.457 ns/op
Iteration   4: 2518489.328 ns/op
Iteration   5: 2389974.535 ns/op

# Run progress: 30.00% complete, ETA 00:00:44
# Fork: 4 of 10
# Warmup Iteration   1: 2518629.150 ns/op
Iteration   1: 2377881.720 ns/op
Iteration   2: 2226138.630 ns/op
Iteration   3: 2244255.879 ns/op
Iteration   4: 2234014.384 ns/op
Iteration   5: 2199726.174 ns/op

# Run progress: 40.00% complete, ETA 00:00:38
# Fork: 5 of 10
# Warmup Iteration   1: 2292219.320 ns/op
Iteration   1: 2169888.244 ns/op
Iteration   2: 2252439.113 ns/op
Iteration   3: 2622969.896 ns/op
Iteration   4: 2378967.107 ns/op
Iteration   5: 2445389.577 ns/op

# Run progress: 50.00% complete, ETA 00:00:31
# Fork: 6 of 10
# Warmup Iteration   1: 2280084.624 ns/op
Iteration   1: 2298478.252 ns/op
Iteration   2: 2330908.930 ns/op
Iteration   3: 2414947.050 ns/op
Iteration   4: 2290824.409 ns/op
Iteration   5: 2315099.624 ns/op

# Run progress: 60.00% complete, ETA 00:00:25
# Fork: 7 of 10
# Warmup Iteration   1: 2511875.628 ns/op
Iteration   1: 2482388.725 ns/op
Iteration   2: 2326564.813 ns/op
Iteration   3: 2556792.485 ns/op
Iteration   4: 2443322.780 ns/op
Iteration   5: 2323782.703 ns/op

# Run progress: 70.00% complete, ETA 00:00:19
# Fork: 8 of 10
# Warmup Iteration   1: 2399025.303 ns/op
Iteration   1: 2438197.056 ns/op
Iteration   2: 2295744.133 ns/op
Iteration   3: 2188330.488 ns/op
Iteration   4: 2208969.040 ns/op
Iteration   5: 2206597.262 ns/op

# Run progress: 80.00% complete, ETA 00:00:13
# Fork: 9 of 10
# Warmup Iteration   1: 2224266.451 ns/op
Iteration   1: 2189792.664 ns/op
Iteration   2: 2248492.060 ns/op
Iteration   3: 2263674.791 ns/op
Iteration   4: 2183353.983 ns/op
Iteration   5: 2199058.993 ns/op

# Run progress: 90.00% complete, ETA 00:00:06
# Fork: 10 of 10
# Warmup Iteration   1: 2749439.981 ns/op
Iteration   1: 2674133.695 ns/op
Iteration   2: 2709058.690 ns/op
Iteration   3: 2641266.446 ns/op
Iteration   4: 2684979.874 ns/op
Iteration   5: 2682659.219 ns/op


Result "testMoveGeneration1":
  2428812.394 ±(99.9%) 94964.471 ns/op [Average]
  (min, avg, max) = (2165619.856, 2428812.394, 2709058.690), stdev = 191832.930
  CI (99.9%): [2333847.922, 2523776.865] (assumes normal distribution)


# Run complete. Total time: 00:01:05

Benchmark                                    Mode  Cnt        Score       Error  Units
MoveGenerationBenchmark.testMoveGeneration1  avgt   50  2428812.394 ± 94964.471  ns/op
