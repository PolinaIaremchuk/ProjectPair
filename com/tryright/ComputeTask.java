package com.tryright;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Worker process that computes partial triangle counts.
 */
public class ComputeTask {

    /**
     * Reads data from stdin and outputs triangle count.
     *
     * @param args unused
     */
    public static void main(String[] args) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

            List<TriangleCounter.Point> points = TriangleCounter.readPointsFromStream(in);
            List<Integer> vertexIndices = TriangleCounter.readIntListFromStream(in);
            int partialCount = TriangleCounter.countRightTrianglesForVertices(points, vertexIndices);
            System.out.println(partialCount);

        } catch (Exception e) {
            System.err.println("Task error: " + e.getMessage());
            System.exit(1);
        }
    }
}
