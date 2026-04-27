**Belmont University x TEC Operating Systems Project 3**
# TryRight - Right Triangle Counter

**Group:** Polina Iaremchuk, Kenneth Chau, Jayden Cruz

---

## Overview

This program counts how many right triangles can be formed from a set of 2D integer points. It uses an O(n^2) slope-grouping algorithm instead of the brute-force O(n^3) approach. For each point treated as the right-angle vertex, it groups all other points by slope using a HashMap, then counts pairs of perpendicular slope groups.

Program 3 adds the `PointStore` interface to abstract how points are loaded. Files ending in `.dat` use `BinPointStore` (binary, memory-mapped I/O). All other files use `TextPointStore` (plain text).

---

## File Formats

**Text format (.txt)**
```
<number of points>
<x1> <y1>
<x2> <y2>
...
```

**Binary format (.dat)**

Each point is 8 bytes: two 4-byte big-endian integers [x][y]. File size must be a multiple of 8. A 0-byte file means 0 points and is valid.

Example: point (1, 2) in hex: `00 00 00 01 00 00 00 02`

---

## Building

From the repo root (the directory containing `com/`):

```bash
javac com/tryright/*.java
```

---

## Running

All three programs take a filename and a worker count. If the filename ends in `.dat`, the binary reader is used automatically.

```bash
java com.tryright.Triangles <input_file>
java com.tryright.ProcessTriangles <input_file> <num_processes>
java com.tryright.ThreadTriangles <input_file> <num_threads>
```

---

## Implementing Suggestions

### Automated Test Script

A bash script is included to compile the project and run all tests from the test plan automatically, printing PASS/FAIL for each case and a final summary.

To run it, from the repo root:

```bash
chmod +x run_tests.sh
./run_tests.sh
```

Example output:

```
Compiling...
Compilation successful.

=== Parameter Validation ===
PASS: Triangles: no args prints usage
PASS: ProcessTriangles: no args prints usage
...

Results: 24 passed, 0 failed
All tests passed!
```

### Merging the two duplicate methods

The two `countRightTrianglesForVertices` methods in `TriangleCounter.java` had identical logic but different parameter types. A private `ListPointStore` adapter was added to wrap a `List<Point>` as a `PointStore`, so the list version now delegates to the single store-based implementation. This removes the duplicate code and means any future changes only need to be made once.

---

## Tests and Expected Output

Run all commands from the repo root after compiling.

### Parameter validation

```bash
java com.tryright.Triangles
# stderr: Usage: java com.tryright.Triangles <input_file>
# exit code: 2

java com.tryright.ProcessTriangles
# stderr: Usage: java com.tryright.ProcessTriangles <input_file> <num_processes>
# exit code: 2

java com.tryright.ThreadTriangles
# stderr: Usage: java com.tryright.ThreadTriangles <input_file> <num_threads>
# exit code: 2

java com.tryright.ProcessTriangles test/testOne.txt notanumber
# stderr: Error: Invalid number of processes - For input string: "notanumber"
# exit code: 7
```

### Text file tests

```bash
# testOne.txt - 5 points, spec example
java com.tryright.Triangles test/testOne.txt
# Expected: 4

java com.tryright.ProcessTriangles test/testOne.txt 8
# Expected: 4

java com.tryright.ThreadTriangles test/testOne.txt 8
# Expected: 4

# testTwo.txt - 8 collinear points, no triangles possible
java com.tryright.Triangles test/testTwo.txt
# Expected: 0

# testThree.txt - 3 points forming one right triangle
java com.tryright.Triangles test/testThree.txt
# Expected: 1

# testFour.txt - 15 random points
java com.tryright.Triangles test/testFour.txt
# Expected: 5

# testFive.txt - malformed file (declares 10 points, only has 2)
java com.tryright.Triangles test/testFive.txt
# stderr: Error: Expected 10 points but only found 2
# exit code: 5

# testSix.txt - 25 random points
java com.tryright.Triangles test/testSix.txt
# Expected: 6

# testProgram3.txt - 7 points
java com.tryright.Triangles test/testProgram3.txt
# Expected: 21

java com.tryright.ProcessTriangles test/testProgram3.txt 4
# Expected: 21

java com.tryright.ThreadTriangles test/testProgram3.txt 4
# Expected: 21

# scaletest - 8245 points
java com.tryright.Triangles test/scaletest
# Expected: 150047

java com.tryright.ThreadTriangles test/scaletest 8
# Expected: 150047

java com.tryright.ProcessTriangles test/scaletest 8
# Expected: 150047
```

### Binary file tests

```bash
# testBinOne.dat - binary version of testOne.txt
java com.tryright.Triangles test/testBinOne.dat
# Expected: 4

java com.tryright.ProcessTriangles test/testBinOne.dat 2
# Expected: 4

java com.tryright.ThreadTriangles test/testBinOne.dat 2
# Expected: 4

# testBinTwo.dat - points (0,0) (3,0) (0,4)
java com.tryright.Triangles test/testBinTwo.dat
# Expected: 1

# testBinEmpty.dat - 0 bytes, 0 points
java com.tryright.Triangles test/testBinEmpty.dat
# Expected: 0

# testBinNegative.dat - points (-5,-3) (-5,2) (0,-3)
java com.tryright.Triangles test/testBinNegative.dat
# Expected: 1
```

### Grader test cases

```bash
# test_spec_list.dat
java com.tryright.Triangles test_spec_list.dat 4
# Expected: 4

# test_long_list.dat (must finish under 2 minutes)
time java com.tryright.Triangles test_long_list.dat 4
# Expected: 32909

# 10000.txt with ThreadTriangles (must finish under 2 minutes)
time java com.tryright.ThreadTriangles 10000.txt 10
# Expected: 72533
```

---

## Error Codes

| Code | Cause |
|------|-------|
| 2 | Wrong number of arguments |
| 3 | File not found |
| 4 | I/O error reading file |
| 5 | Invalid file format |
| 6 | Worker count less than 1 |
| 7 | Worker count is not a valid integer |
| 8 | Thread or process interrupted |
| 9 | Child process failed |
| 10 | No output from child process |

---

## Project Structure

```
com/tryright/
    PointStore.java         - interface
    TextPointStore.java     - text file implementation
    BinPointStore.java      - binary MMIO implementation
    TriangleCounter.java    - algorithm and shared utilities
    ComputeTask.java        - child worker process
    Triangles.java          - single-threaded entry point
    ProcessTriangles.java   - multi-process entry point
    ThreadTriangles.java    - multi-threaded entry point
test/
    testplan.pdf
    performance.pdf
    scaletest
    testOne.txt / testBinOne.dat
    testTwo.txt
    testThree.txt
    testFour.txt
    testFive.txt
    testSix.txt
    testProgram3.txt
    testBinTwo.dat
    testBinEmpty.dat
    testBinNegative.dat
```