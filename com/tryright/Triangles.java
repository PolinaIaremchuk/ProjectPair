package com.tryright;

/**
 * Counts right triangles in a point set using single-threaded processing.
 */
public class Triangles {

    /**
     * Entry point for triangle counting.
     *
     * @param args input filename
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java com.tryright.Triangles <input_file>");
            System.exit(2);
        }

        String filename = args[0];

        PointStore store = TriangleCounter.createPointStore(filename);
        int result = TriangleCounter.countRightTriangles(store);
        store.close();
        System.out.println(result);
    }
}