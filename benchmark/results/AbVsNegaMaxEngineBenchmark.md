# JMH 1.11.1 (released 77 days ago)
# VM version: JDK 1.8.0_60, VM 25.60-b23
# VM invoker: /Library/Java/JavaVirtualMachines/jdk1.8.0_60.jdk/Contents/Home/jre/bin/java
# VM options: <none>
# Warmup: 1 iterations, 1 s each
# Measurement: 5 iterations, 1 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Average time, time/op
# Benchmark: com.gordoncaleb.AbVsNegaMaxEngineBenchmark.testAlphaBeta
# Parameters: (perftNum = 0)

# Run progress: 0.00% complete, ETA 00:04:12
# Fork: 1 of 3
# Warmup Iteration   1: 711933.356 ns/op
Iteration   1: 669135.134 ns/op
Iteration   2: 734337.630 ns/op
Iteration   3: 665554.899 ns/op
Iteration   4: 654074.353 ns/op
Iteration   5: 639251.566 ns/op

# Run progress: 2.38% complete, ETA 00:04:21
# Fork: 2 of 3
# Warmup Iteration   1: 713385.466 ns/op
Iteration   1: 637066.251 ns/op
Iteration   2: 621422.430 ns/op
Iteration   3: 628891.213 ns/op
Iteration   4: 625930.973 ns/op
Iteration   5: 627107.666 ns/op

# Run progress: 4.76% complete, ETA 00:04:14
# Fork: 3 of 3
# Warmup Iteration   1: 707998.989 ns/op
Iteration   1: 626632.853 ns/op
Iteration   2: 630799.345 ns/op
Iteration   3: 640688.172 ns/op
Iteration   4: 653809.195 ns/op
Iteration   5: 636628.445 ns/op


Result "testAlphaBeta":
  646088.675 ±(99.9%) 30441.049 ns/op [Average]
  (min, avg, max) = (621422.430, 646088.675, 734337.630), stdev = 28474.576
  CI (99.9%): [615647.626, 676529.724] (assumes normal distribution)


# JMH 1.11.1 (released 77 days ago)
# VM version: JDK 1.8.0_60, VM 25.60-b23
# VM invoker: /Library/Java/JavaVirtualMachines/jdk1.8.0_60.jdk/Contents/Home/jre/bin/java
# VM options: <none>
# Warmup: 1 iterations, 1 s each
# Measurement: 5 iterations, 1 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Average time, time/op
# Benchmark: com.gordoncaleb.AbVsNegaMaxEngineBenchmark.testAlphaBeta
# Parameters: (perftNum = 1)

# Run progress: 7.14% complete, ETA 00:04:08
# Fork: 1 of 3
# Warmup Iteration   1: 29129431.657 ns/op
Iteration   1: 24837115.756 ns/op
Iteration   2: 24674107.854 ns/op
Iteration   3: 23951254.186 ns/op
Iteration   4: 24753008.366 ns/op
Iteration   5: 24588665.488 ns/op

# Run progress: 9.52% complete, ETA 00:04:03
# Fork: 2 of 3
# Warmup Iteration   1: 29030472.086 ns/op
Iteration   1: 24972575.537 ns/op
Iteration   2: 24036897.143 ns/op
Iteration   3: 24680453.415 ns/op
Iteration   4: 24491371.667 ns/op
Iteration   5: 24930021.488 ns/op

# Run progress: 11.90% complete, ETA 00:03:57
# Fork: 3 of 3
# Warmup Iteration   1: 28421595.583 ns/op
Iteration   1: 24721537.146 ns/op
Iteration   2: 24558679.293 ns/op
Iteration   3: 24392725.786 ns/op
Iteration   4: 24491256.171 ns/op
Iteration   5: 24489444.214 ns/op


Result "testAlphaBeta":
  24571274.234 ±(99.9%) 307771.054 ns/op [Average]
  (min, avg, max) = (23951254.186, 24571274.234, 24972575.537), stdev = 287889.235
  CI (99.9%): [24263503.180, 24879045.288] (assumes normal distribution)


# JMH 1.11.1 (released 77 days ago)
# VM version: JDK 1.8.0_60, VM 25.60-b23
# VM invoker: /Library/Java/JavaVirtualMachines/jdk1.8.0_60.jdk/Contents/Home/jre/bin/java
# VM options: <none>
# Warmup: 1 iterations, 1 s each
# Measurement: 5 iterations, 1 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Average time, time/op
# Benchmark: com.gordoncaleb.AbVsNegaMaxEngineBenchmark.testAlphaBeta
# Parameters: (perftNum = 2)

# Run progress: 14.29% complete, ETA 00:03:50
# Fork: 1 of 3
# Warmup Iteration   1: 446022.041 ns/op
Iteration   1: 373475.436 ns/op
Iteration   2: 371791.921 ns/op
Iteration   3: 364316.861 ns/op
Iteration   4: 368137.117 ns/op
Iteration   5: 373488.730 ns/op

# Run progress: 16.67% complete, ETA 00:03:44
# Fork: 2 of 3
# Warmup Iteration   1: 458894.800 ns/op
Iteration   1: 386282.750 ns/op
Iteration   2: 387552.601 ns/op
Iteration   3: 390930.683 ns/op
Iteration   4: 396124.850 ns/op
Iteration   5: 380466.108 ns/op

# Run progress: 19.05% complete, ETA 00:03:37
# Fork: 3 of 3
# Warmup Iteration   1: 459365.079 ns/op
Iteration   1: 382746.029 ns/op
Iteration   2: 394221.256 ns/op
Iteration   3: 390974.743 ns/op
Iteration   4: 397830.423 ns/op
Iteration   5: 381451.079 ns/op


Result "testAlphaBeta":
  382652.706 ±(99.9%) 11284.821 ns/op [Average]
  (min, avg, max) = (364316.861, 382652.706, 397830.423), stdev = 10555.829
  CI (99.9%): [371367.885, 393937.527] (assumes normal distribution)


# JMH 1.11.1 (released 77 days ago)
# VM version: JDK 1.8.0_60, VM 25.60-b23
# VM invoker: /Library/Java/JavaVirtualMachines/jdk1.8.0_60.jdk/Contents/Home/jre/bin/java
# VM options: <none>
# Warmup: 1 iterations, 1 s each
# Measurement: 5 iterations, 1 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Average time, time/op
# Benchmark: com.gordoncaleb.AbVsNegaMaxEngineBenchmark.testAlphaBeta
# Parameters: (perftNum = 3)

# Run progress: 21.43% complete, ETA 00:03:31
# Fork: 1 of 3
# Warmup Iteration   1: 2826124.020 ns/op
Iteration   1: 2521439.075 ns/op
Iteration   2: 2469534.094 ns/op
Iteration   3: 2475436.781 ns/op
Iteration   4: 2485760.235 ns/op
Iteration   5: 2431489.058 ns/op

# Run progress: 23.81% complete, ETA 00:03:25
# Fork: 2 of 3
# Warmup Iteration   1: 2844646.314 ns/op
Iteration   1: 2536850.248 ns/op
Iteration   2: 2506528.589 ns/op
Iteration   3: 2455863.652 ns/op
Iteration   4: 2471885.629 ns/op
Iteration   5: 2487190.074 ns/op

# Run progress: 26.19% complete, ETA 00:03:18
# Fork: 3 of 3
# Warmup Iteration   1: 2776728.334 ns/op
Iteration   1: 2489036.292 ns/op
Iteration   2: 2458414.054 ns/op
Iteration   3: 2486551.822 ns/op
Iteration   4: 2438549.828 ns/op
Iteration   5: 2451342.190 ns/op


Result "testAlphaBeta":
  2477724.775 ±(99.9%) 31197.185 ns/op [Average]
  (min, avg, max) = (2431489.058, 2477724.775, 2536850.248), stdev = 29181.866
  CI (99.9%): [2446527.590, 2508921.959] (assumes normal distribution)


# JMH 1.11.1 (released 77 days ago)
# VM version: JDK 1.8.0_60, VM 25.60-b23
# VM invoker: /Library/Java/JavaVirtualMachines/jdk1.8.0_60.jdk/Contents/Home/jre/bin/java
# VM options: <none>
# Warmup: 1 iterations, 1 s each
# Measurement: 5 iterations, 1 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Average time, time/op
# Benchmark: com.gordoncaleb.AbVsNegaMaxEngineBenchmark.testAlphaBeta
# Parameters: (perftNum = 4)

# Run progress: 28.57% complete, ETA 00:03:11
# Fork: 1 of 3
# Warmup Iteration   1: 13106938.000 ns/op
Iteration   1: 11431881.750 ns/op
Iteration   2: 11439416.625 ns/op
Iteration   3: 11681967.477 ns/op
Iteration   4: 11343066.966 ns/op
Iteration   5: 11726236.488 ns/op

# Run progress: 30.95% complete, ETA 00:03:05
# Fork: 2 of 3
# Warmup Iteration   1: 13327677.658 ns/op
Iteration   1: 11451176.989 ns/op
Iteration   2: 11760048.872 ns/op
Iteration   3: 11290518.247 ns/op
Iteration   4: 11216181.889 ns/op
Iteration   5: 11261648.090 ns/op

# Run progress: 33.33% complete, ETA 00:02:59
# Fork: 3 of 3
# Warmup Iteration   1: 13983031.653 ns/op
Iteration   1: 13439516.867 ns/op
Iteration   2: 12127401.892 ns/op
Iteration   3: 11974076.369 ns/op
Iteration   4: 12349005.915 ns/op
Iteration   5: 12102743.530 ns/op


Result "testAlphaBeta":
  11772992.531 ±(99.9%) 620399.618 ns/op [Average]
  (min, avg, max) = (11216181.889, 11772992.531, 13439516.867), stdev = 580322.188
  CI (99.9%): [11152592.913, 12393392.149] (assumes normal distribution)


# JMH 1.11.1 (released 77 days ago)
# VM version: JDK 1.8.0_60, VM 25.60-b23
# VM invoker: /Library/Java/JavaVirtualMachines/jdk1.8.0_60.jdk/Contents/Home/jre/bin/java
# VM options: <none>
# Warmup: 1 iterations, 1 s each
# Measurement: 5 iterations, 1 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Average time, time/op
# Benchmark: com.gordoncaleb.AbVsNegaMaxEngineBenchmark.testAlphaBeta
# Parameters: (perftNum = 5)

# Run progress: 35.71% complete, ETA 00:02:52
# Fork: 1 of 3
# Warmup Iteration   1: 28248952.583 ns/op
Iteration   1: 21435634.085 ns/op
Iteration   2: 20202299.060 ns/op
Iteration   3: 20391254.680 ns/op
Iteration   4: 20036059.780 ns/op
Iteration   5: 19823241.941 ns/op

# Run progress: 38.10% complete, ETA 00:02:46
# Fork: 2 of 3
# Warmup Iteration   1: 25658447.385 ns/op
Iteration   1: 20604259.102 ns/op
Iteration   2: 20726233.327 ns/op
Iteration   3: 21493450.213 ns/op
Iteration   4: 20101155.000 ns/op
Iteration   5: 20041055.140 ns/op

# Run progress: 40.48% complete, ETA 00:02:40
# Fork: 3 of 3
# Warmup Iteration   1: 26090752.128 ns/op
Iteration   1: 20480971.041 ns/op
Iteration   2: 19627120.519 ns/op
Iteration   3: 19157286.547 ns/op
Iteration   4: 19101153.660 ns/op
Iteration   5: 18767312.778 ns/op


Result "testAlphaBeta":
  20132565.792 ±(99.9%) 836273.376 ns/op [Average]
  (min, avg, max) = (18767312.778, 20132565.792, 21493450.213), stdev = 782250.635
  CI (99.9%): [19296292.415, 20968839.168] (assumes normal distribution)


# JMH 1.11.1 (released 77 days ago)
# VM version: JDK 1.8.0_60, VM 25.60-b23
# VM invoker: /Library/Java/JavaVirtualMachines/jdk1.8.0_60.jdk/Contents/Home/jre/bin/java
# VM options: <none>
# Warmup: 1 iterations, 1 s each
# Measurement: 5 iterations, 1 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Average time, time/op
# Benchmark: com.gordoncaleb.AbVsNegaMaxEngineBenchmark.testAlphaBeta
# Parameters: (perftNum = 6)

# Run progress: 42.86% complete, ETA 00:02:33
# Fork: 1 of 3
# Warmup Iteration   1: 764763.963 ns/op
Iteration   1: 676186.642 ns/op
Iteration   2: 638231.508 ns/op
Iteration   3: 636774.599 ns/op
Iteration   4: 643135.292 ns/op
Iteration   5: 632043.934 ns/op

# Run progress: 45.24% complete, ETA 00:02:27
# Fork: 2 of 3
# Warmup Iteration   1: 759627.187 ns/op
Iteration   1: 671027.112 ns/op
Iteration   2: 641646.474 ns/op
Iteration   3: 658713.554 ns/op
Iteration   4: 649285.861 ns/op
Iteration   5: 632671.007 ns/op

# Run progress: 47.62% complete, ETA 00:02:20
# Fork: 3 of 3
# Warmup Iteration   1: 730564.326 ns/op
Iteration   1: 645513.823 ns/op
Iteration   2: 630156.294 ns/op
Iteration   3: 631109.555 ns/op
Iteration   4: 629511.802 ns/op
Iteration   5: 631672.639 ns/op


Result "testAlphaBeta":
  643178.673 ±(99.9%) 15871.636 ns/op [Average]
  (min, avg, max) = (629511.802, 643178.673, 676186.642), stdev = 14846.338
  CI (99.9%): [627307.037, 659050.309] (assumes normal distribution)


# JMH 1.11.1 (released 77 days ago)
# VM version: JDK 1.8.0_60, VM 25.60-b23
# VM invoker: /Library/Java/JavaVirtualMachines/jdk1.8.0_60.jdk/Contents/Home/jre/bin/java
# VM options: <none>
# Warmup: 1 iterations, 1 s each
# Measurement: 5 iterations, 1 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Average time, time/op
# Benchmark: com.gordoncaleb.AbVsNegaMaxEngineBenchmark.testNegaMax
# Parameters: (perftNum = 0)

# Run progress: 50.00% complete, ETA 00:02:14
# Fork: 1 of 3
# Warmup Iteration   1: 6550538.656 ns/op
Iteration   1: 5680119.610 ns/op
Iteration   2: 5739172.143 ns/op
Iteration   3: 5698842.655 ns/op
Iteration   4: 5747474.691 ns/op
Iteration   5: 5560292.691 ns/op

# Run progress: 52.38% complete, ETA 00:02:08
# Fork: 2 of 3
# Warmup Iteration   1: 6506406.587 ns/op
Iteration   1: 5882119.877 ns/op
Iteration   2: 5752391.749 ns/op
Iteration   3: 5768198.552 ns/op
Iteration   4: 5784547.787 ns/op
Iteration   5: 5618487.050 ns/op

# Run progress: 54.76% complete, ETA 00:02:01
# Fork: 3 of 3
# Warmup Iteration   1: 6531992.357 ns/op
Iteration   1: 5420660.146 ns/op
Iteration   2: 5653753.771 ns/op
Iteration   3: 5552412.729 ns/op
Iteration   4: 5641502.697 ns/op
Iteration   5: 5683013.904 ns/op


Result "testNegaMax":
  5678866.003 ±(99.9%) 120212.982 ns/op [Average]
  (min, avg, max) = (5420660.146, 5678866.003, 5882119.877), stdev = 112447.298
  CI (99.9%): [5558653.022, 5799078.985] (assumes normal distribution)


# JMH 1.11.1 (released 77 days ago)
# VM version: JDK 1.8.0_60, VM 25.60-b23
# VM invoker: /Library/Java/JavaVirtualMachines/jdk1.8.0_60.jdk/Contents/Home/jre/bin/java
# VM options: <none>
# Warmup: 1 iterations, 1 s each
# Measurement: 5 iterations, 1 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Average time, time/op
# Benchmark: com.gordoncaleb.AbVsNegaMaxEngineBenchmark.testNegaMax
# Parameters: (perftNum = 1)

# Run progress: 57.14% complete, ETA 00:01:55
# Fork: 1 of 3
# Warmup Iteration   1: 75285697.786 ns/op
Iteration   1: 65332950.125 ns/op
Iteration   2: 63748549.813 ns/op
Iteration   3: 65776481.875 ns/op
Iteration   4: 65074131.313 ns/op
Iteration   5: 63328404.563 ns/op

# Run progress: 59.52% complete, ETA 00:01:48
# Fork: 2 of 3
# Warmup Iteration   1: 74029789.571 ns/op
Iteration   1: 64273245.938 ns/op
Iteration   2: 63627687.563 ns/op
Iteration   3: 65790450.875 ns/op
Iteration   4: 64494861.563 ns/op
Iteration   5: 64444336.688 ns/op

# Run progress: 61.90% complete, ETA 00:01:42
# Fork: 3 of 3
# Warmup Iteration   1: 75229163.857 ns/op
Iteration   1: 64087668.438 ns/op
Iteration   2: 63568224.813 ns/op
Iteration   3: 65548561.875 ns/op
Iteration   4: 64545531.188 ns/op
Iteration   5: 64900392.625 ns/op


Result "testNegaMax":
  64569431.950 ±(99.9%) 871261.773 ns/op [Average]
  (min, avg, max) = (63328404.563, 64569431.950, 65790450.875), stdev = 814978.803
  CI (99.9%): [63698170.177, 65440693.723] (assumes normal distribution)


# JMH 1.11.1 (released 77 days ago)
# VM version: JDK 1.8.0_60, VM 25.60-b23
# VM invoker: /Library/Java/JavaVirtualMachines/jdk1.8.0_60.jdk/Contents/Home/jre/bin/java
# VM options: <none>
# Warmup: 1 iterations, 1 s each
# Measurement: 5 iterations, 1 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Average time, time/op
# Benchmark: com.gordoncaleb.AbVsNegaMaxEngineBenchmark.testNegaMax
# Parameters: (perftNum = 2)

# Run progress: 64.29% complete, ETA 00:01:36
# Fork: 1 of 3
# Warmup Iteration   1: 1261790.749 ns/op
Iteration   1: 1041714.970 ns/op
Iteration   2: 1091992.894 ns/op
Iteration   3: 1044345.228 ns/op
Iteration   4: 1059031.308 ns/op
Iteration   5: 1072380.887 ns/op

# Run progress: 66.67% complete, ETA 00:01:29
# Fork: 2 of 3
# Warmup Iteration   1: 1343194.839 ns/op
Iteration   1: 1075879.759 ns/op
Iteration   2: 1131290.014 ns/op
Iteration   3: 1138648.216 ns/op
Iteration   4: 1116613.739 ns/op
Iteration   5: 1104528.231 ns/op

# Run progress: 69.05% complete, ETA 00:01:23
# Fork: 3 of 3
# Warmup Iteration   1: 1246022.362 ns/op
Iteration   1: 1042819.774 ns/op
Iteration   2: 1057570.746 ns/op
Iteration   3: 1064353.072 ns/op
Iteration   4: 1044197.656 ns/op
Iteration   5: 1026103.117 ns/op


Result "testNegaMax":
  1074097.974 ±(99.9%) 37474.316 ns/op [Average]
  (min, avg, max) = (1026103.117, 1074097.974, 1138648.216), stdev = 35053.498
  CI (99.9%): [1036623.659, 1111572.290] (assumes normal distribution)


# JMH 1.11.1 (released 77 days ago)
# VM version: JDK 1.8.0_60, VM 25.60-b23
# VM invoker: /Library/Java/JavaVirtualMachines/jdk1.8.0_60.jdk/Contents/Home/jre/bin/java
# VM options: <none>
# Warmup: 1 iterations, 1 s each
# Measurement: 5 iterations, 1 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Average time, time/op
# Benchmark: com.gordoncaleb.AbVsNegaMaxEngineBenchmark.testNegaMax
# Parameters: (perftNum = 3)

# Run progress: 71.43% complete, ETA 00:01:16
# Fork: 1 of 3
# Warmup Iteration   1: 6021916.263 ns/op
Iteration   1: 5434995.432 ns/op
Iteration   2: 5497500.803 ns/op
Iteration   3: 5330665.354 ns/op
Iteration   4: 5541888.434 ns/op
Iteration   5: 5403370.172 ns/op

# Run progress: 73.81% complete, ETA 00:01:10
# Fork: 2 of 3
# Warmup Iteration   1: 6007185.583 ns/op
Iteration   1: 5478111.446 ns/op
Iteration   2: 5471906.623 ns/op
Iteration   3: 5386125.882 ns/op
Iteration   4: 5454843.838 ns/op
Iteration   5: 5368390.578 ns/op

# Run progress: 76.19% complete, ETA 00:01:04
# Fork: 3 of 3
# Warmup Iteration   1: 6229515.673 ns/op
Iteration   1: 5315548.799 ns/op
Iteration   2: 5273757.791 ns/op
Iteration   3: 5325704.794 ns/op
Iteration   4: 5471478.152 ns/op
Iteration   5: 5383993.979 ns/op


Result "testNegaMax":
  5409218.805 ±(99.9%) 82726.686 ns/op [Average]
  (min, avg, max) = (5273757.791, 5409218.805, 5541888.434), stdev = 77382.594
  CI (99.9%): [5326492.119, 5491945.491] (assumes normal distribution)


# JMH 1.11.1 (released 77 days ago)
# VM version: JDK 1.8.0_60, VM 25.60-b23
# VM invoker: /Library/Java/JavaVirtualMachines/jdk1.8.0_60.jdk/Contents/Home/jre/bin/java
# VM options: <none>
# Warmup: 1 iterations, 1 s each
# Measurement: 5 iterations, 1 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Average time, time/op
# Benchmark: com.gordoncaleb.AbVsNegaMaxEngineBenchmark.testNegaMax
# Parameters: (perftNum = 4)

# Run progress: 78.57% complete, ETA 00:00:57
# Fork: 1 of 3
# Warmup Iteration   1: 37951543.111 ns/op
Iteration   1: 31785742.594 ns/op
Iteration   2: 31826653.938 ns/op
Iteration   3: 32890490.806 ns/op
Iteration   4: 32803105.935 ns/op
Iteration   5: 32387442.806 ns/op

# Run progress: 80.95% complete, ETA 00:00:51
# Fork: 2 of 3
# Warmup Iteration   1: 37814868.889 ns/op
Iteration   1: 32707979.774 ns/op
Iteration   2: 33033810.032 ns/op
Iteration   3: 33287784.452 ns/op
Iteration   4: 32875282.645 ns/op
Iteration   5: 33345945.710 ns/op

# Run progress: 83.33% complete, ETA 00:00:44
# Fork: 3 of 3
# Warmup Iteration   1: 37251805.630 ns/op
Iteration   1: 32385269.613 ns/op
Iteration   2: 32195015.375 ns/op
Iteration   3: 32094153.031 ns/op
Iteration   4: 32209893.406 ns/op
Iteration   5: 32204071.125 ns/op


Result "testNegaMax":
  32535509.416 ±(99.9%) 530665.496 ns/op [Average]
  (min, avg, max) = (31785742.594, 32535509.416, 33345945.710), stdev = 496384.834
  CI (99.9%): [32004843.920, 33066174.912] (assumes normal distribution)


# JMH 1.11.1 (released 77 days ago)
# VM version: JDK 1.8.0_60, VM 25.60-b23
# VM invoker: /Library/Java/JavaVirtualMachines/jdk1.8.0_60.jdk/Contents/Home/jre/bin/java
# VM options: <none>
# Warmup: 1 iterations, 1 s each
# Measurement: 5 iterations, 1 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Average time, time/op
# Benchmark: com.gordoncaleb.AbVsNegaMaxEngineBenchmark.testNegaMax
# Parameters: (perftNum = 5)

# Run progress: 85.71% complete, ETA 00:00:38
# Fork: 1 of 3
# Warmup Iteration   1: 65591943.938 ns/op
Iteration   1: 58294406.444 ns/op
Iteration   2: 58485102.611 ns/op
Iteration   3: 57698973.944 ns/op
Iteration   4: 56818633.500 ns/op
Iteration   5: 57369691.056 ns/op

# Run progress: 88.10% complete, ETA 00:00:32
# Fork: 2 of 3
# Warmup Iteration   1: 63250295.938 ns/op
Iteration   1: 58504943.611 ns/op
Iteration   2: 56188053.167 ns/op
Iteration   3: 57877156.278 ns/op
Iteration   4: 57355483.889 ns/op
Iteration   5: 57168449.722 ns/op

# Run progress: 90.48% complete, ETA 00:00:25
# Fork: 3 of 3
# Warmup Iteration   1: 62793782.688 ns/op
Iteration   1: 57165072.111 ns/op
Iteration   2: 57956900.000 ns/op
Iteration   3: 55978380.056 ns/op
Iteration   4: 56344754.222 ns/op
Iteration   5: 55225243.947 ns/op


Result "testNegaMax":
  57228749.637 ±(99.9%) 1036816.877 ns/op [Average]
  (min, avg, max) = (55225243.947, 57228749.637, 58504943.611), stdev = 969839.151
  CI (99.9%): [56191932.760, 58265566.515] (assumes normal distribution)


# JMH 1.11.1 (released 77 days ago)
# VM version: JDK 1.8.0_60, VM 25.60-b23
# VM invoker: /Library/Java/JavaVirtualMachines/jdk1.8.0_60.jdk/Contents/Home/jre/bin/java
# VM options: <none>
# Warmup: 1 iterations, 1 s each
# Measurement: 5 iterations, 1 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Average time, time/op
# Benchmark: com.gordoncaleb.AbVsNegaMaxEngineBenchmark.testNegaMax
# Parameters: (perftNum = 6)

# Run progress: 92.86% complete, ETA 00:00:19
# Fork: 1 of 3
# Warmup Iteration   1: 1673835.473 ns/op
Iteration   1: 1522885.700 ns/op
Iteration   2: 1511265.896 ns/op
Iteration   3: 1465317.119 ns/op
Iteration   4: 1450053.022 ns/op
Iteration   5: 1438964.941 ns/op

# Run progress: 95.24% complete, ETA 00:00:12
# Fork: 2 of 3
# Warmup Iteration   1: 1676599.644 ns/op
Iteration   1: 1462191.978 ns/op
Iteration   2: 1472433.322 ns/op
Iteration   3: 1457887.933 ns/op
Iteration   4: 1540420.054 ns/op
Iteration   5: 1517070.149 ns/op

# Run progress: 97.62% complete, ETA 00:00:06
# Fork: 3 of 3
# Warmup Iteration   1: 1669250.562 ns/op
Iteration   1: 1467359.585 ns/op
Iteration   2: 1452301.799 ns/op
Iteration   3: 1455680.486 ns/op
Iteration   4: 1510100.768 ns/op
Iteration   5: 1532158.023 ns/op


Result "testNegaMax":
  1483739.385 ±(99.9%) 36630.297 ns/op [Average]
  (min, avg, max) = (1438964.941, 1483739.385, 1540420.054), stdev = 34264.003
  CI (99.9%): [1447109.088, 1520369.682] (assumes normal distribution)


# Run complete. Total time: 00:04:29

Benchmark                      (perftNum)  Mode  Cnt         Score         Error  Units
EngineBenchmark.testAlphaBeta           0  avgt   15    646088.675 ±   30441.049  ns/op
EngineBenchmark.testAlphaBeta           1  avgt   15  24571274.234 ±  307771.054  ns/op
EngineBenchmark.testAlphaBeta           2  avgt   15    382652.706 ±   11284.821  ns/op
EngineBenchmark.testAlphaBeta           3  avgt   15   2477724.775 ±   31197.185  ns/op
EngineBenchmark.testAlphaBeta           4  avgt   15  11772992.531 ±  620399.618  ns/op
EngineBenchmark.testAlphaBeta           5  avgt   15  20132565.792 ±  836273.376  ns/op
EngineBenchmark.testAlphaBeta           6  avgt   15    643178.673 ±   15871.636  ns/op
EngineBenchmark.testNegaMax             0  avgt   15   5678866.003 ±  120212.982  ns/op
EngineBenchmark.testNegaMax             1  avgt   15  64569431.950 ±  871261.773  ns/op
EngineBenchmark.testNegaMax             2  avgt   15   1074097.974 ±   37474.316  ns/op
EngineBenchmark.testNegaMax             3  avgt   15   5409218.805 ±   82726.686  ns/op
EngineBenchmark.testNegaMax             4  avgt   15  32535509.416 ±  530665.496  ns/op
EngineBenchmark.testNegaMax             5  avgt   15  57228749.637 ± 1036816.877  ns/op
EngineBenchmark.testNegaMax             6  avgt   15   1483739.385 ±   36630.297  ns/op

Benchmark result is saved to EngineBenchmark.jmh.json
Param 0: {Mean=+778.96%, Stdev=+294.90%, Min=+772.30%, Max=+701.01%}
Param 1: {Mean=+162.78%, Stdev=+183.09%, Min=+164.41%, Max=+163.45%}
Param 2: {Mean=+180.70%, Stdev=+232.08%, Min=+181.65%, Max=+186.21%}
Param 3: {Mean=+118.31%, Stdev=+165.17%, Min=+116.89%, Max=+118.46%}
Param 4: {Mean=+176.36%, Stdev=+-14.46%, Min=+183.39%, Max=+148.12%}
Param 5: {Mean=+184.26%, Stdev=+23.98%, Min=+194.26%, Max=+172.20%}
Param 6: {Mean=+130.69%, Stdev=+130.79%, Min=+128.58%, Max=+127.81%}
