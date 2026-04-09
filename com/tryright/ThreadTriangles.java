package com.tryright;

import java.util.ArrayList;
import java.util.List;

/**
 * Counts right triangles using threads with shared memory.
 */
public class ThreadTriangles {

    private static class CounterThread extends Thread {
        private final PointStore store;
        private final List<Integer> vertexIndices;
        private final int[] results;
        private final int resultIndex;

        public CounterThread(PointStore store, List<Integer> vertexIndices,
                           int[] results, int resultIndex) {
            this.store = store;
            this.vertexIndices = vertexIndices;
            this.results = results;
            this.resultIndex = resultIndex;
        }

        @Override
        public void run() {
            int count = TriangleCounter.countRightTrianglesForVertices(store, vertexIndices);
            results[resultIndex] = count;
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java com.tryright.ThreadTriangles <input_file> <num_threads>");
            System.exit(2);
        }

        String filename = args[0];
        int numThreads = TriangleCounter.parseWorkerCount(args[1], "threads");

        PointStore store = TriangleCounter.createPointStore(filename);
        int n = store.numPoints();

        if (n < 3 || numThreads == 1) {
            int result = TriangleCounter.countRightTriangles(store);
            store.close();
            System.out.println(result);
            return;
        }

        try {
            List<List<Integer>> vertexAssignments = TriangleCounter.distributeVertices(n, numThreads);
            int[] results = new int[numThreads];
            List<CounterThread> threads = new ArrayList<>();

            for (int i = 0; i < numThreads; i++) {
                List<Integer> assignment = vertexAssignments.get(i);
                if (!assignment.isEmpty()) {
                    CounterThread thread = new CounterThread(store, assignment, results, i);
                    threads.add(thread);
                    thread.start();
                }
            }

            for (CounterThread thread : threads) {
                thread.join();
            }

            int totalCount = 0;
            for (int count : results) {
                totalCount += count;
            }

            store.close();
            System.out.println(totalCount);

        } catch (InterruptedException e) {
            store.close();
            System.err.println("Error: Thread execution failed - " + e.getMessage());
            System.exit(8);
        }
    }

}
