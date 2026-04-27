#!/bin/bash

# Compile first
echo "Compiling..."
javac com/tryright/*.java
if [ $? -ne 0 ]; then
    echo "Compilation failed. Exiting."
    exit 1
fi
echo "Compilation successful."
echo ""

PASS=0
FAIL=0

# Helper: runs a command and checks stdout equals expected value
check() {
    local description="$1"
    local expected="$2"
    shift 2
    local actual
    actual=$("$@" 2>/dev/null)
    if [ "$actual" = "$expected" ]; then
        echo "PASS: $description"
        ((PASS++))
    else
        echo "FAIL: $description"
        echo "      Expected: $expected"
        echo "      Got:      $actual"
        ((FAIL++))
    fi
}

# Helper: runs a command and checks that stderr contains a keyword
check_err() {
    local description="$1"
    local keyword="$2"
    shift 2
    local err
    err=$("$@" 2>&1 >/dev/null)
    if echo "$err" | grep -qi "$keyword"; then
        echo "PASS: $description"
        ((PASS++))
    else
        echo "FAIL: $description"
        echo "      Expected stderr to contain: $keyword"
        echo "      Got: $err"
        ((FAIL++))
    fi
}

echo "=== Parameter Validation ==="
check_err "Triangles: no args prints usage"           "Usage"   java com.tryright.Triangles
check_err "ProcessTriangles: no args prints usage"    "Usage"   java com.tryright.ProcessTriangles
check_err "ThreadTriangles: no args prints usage"     "Usage"   java com.tryright.ThreadTriangles
check_err "ProcessTriangles: non-numeric proc count"  "invalid" java com.tryright.ProcessTriangles file1 file2
check_err "ThreadTriangles: non-numeric thread count" "invalid" java com.tryright.ThreadTriangles file1 file2

echo ""
echo "=== Text Files — Triangles ==="
check "testOne.txt → 4"        "4"      java com.tryright.Triangles test/testOne.txt
check "testTwo.txt → 0"        "0"      java com.tryright.Triangles test/testTwo.txt
check "testThree.txt → 1"      "1"      java com.tryright.Triangles test/testThree.txt
check "testFour.txt → 5"       "5"      java com.tryright.Triangles test/testFour.txt
check "testSix.txt → 6"        "6"      java com.tryright.Triangles test/testSix.txt
check "testProgram3.txt → 21"  "21"     java com.tryright.Triangles test/testProgram3.txt
check "scaletest.txt → 150047" "150047" java com.tryright.Triangles test/scaletest.txt
check_err "testFive.txt truncated file → error" "expected\|found" java com.tryright.Triangles test/testFive.txt

echo ""
echo "=== Text Files — ThreadTriangles ==="
check "testOne.txt 8 threads → 4"        "4"      java com.tryright.ThreadTriangles test/testOne.txt 8
check "testProgram3.txt 4 threads → 21"  "21"     java com.tryright.ThreadTriangles test/testProgram3.txt 4
check "scaletest.txt 8 threads → 150047" "150047" java com.tryright.ThreadTriangles test/scaletest.txt 8

echo ""
echo "=== Text Files — ProcessTriangles ==="
check "testOne.txt 8 procs → 4"        "4"      java com.tryright.ProcessTriangles test/testOne.txt 8
check "testProgram3.txt 4 procs → 21"  "21"     java com.tryright.ProcessTriangles test/testProgram3.txt 4
check "scaletest.txt 8 procs → 150047" "150047" java com.tryright.ProcessTriangles test/scaletest.txt 8

echo ""
echo "=== Binary Files — Triangles ==="
check "testBinOne.dat → 4"      "4" java com.tryright.Triangles test/testBinOne.dat
check "testBinTwo.dat → 1"      "1" java com.tryright.Triangles test/testBinTwo.dat
check "testBinEmpty.dat → 0"    "0" java com.tryright.Triangles test/testBinEmpty.dat
check "testBinNegative.dat → 1" "1" java com.tryright.Triangles test/testBinNegative.dat

echo ""
echo "=== Binary Files — ThreadTriangles ==="
check "testBinOne.dat 2 threads → 4" "4" java com.tryright.ThreadTriangles test/testBinOne.dat 2

echo ""
echo "=== Binary Files — ProcessTriangles ==="
check "testBinOne.dat 2 procs → 4" "4" java com.tryright.ProcessTriangles test/testBinOne.dat 2

echo ""
echo "================================"
echo "Results: $PASS passed, $FAIL failed"
if [ $FAIL -eq 0 ]; then
    echo "All tests passed!"
    exit 0
else
    exit 1
fi