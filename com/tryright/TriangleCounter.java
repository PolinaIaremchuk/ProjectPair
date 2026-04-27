package com.tryright;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Algorithms for counting right triangles using slope grouping.
 */
public class TriangleCounter {

    /**
     * Represents a 2 dimentional point.
     */
    public static class Point {
        public int x, y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return x + " " + y;
        }
    }

    /**
     * Counts all right triangles in the point list
     *
     * @param points list of points
     * @return triangle count
     */
    public static int countRightTriangles(List<Point> points) {
        return countRightTrianglesForVertices(points, null);
    }

    /**
     * Counts all right triangles using a PointStore.
     *
     * @param store point store
     * @return triangle count
     */
    public static int countRightTriangles(PointStore store) {
        return countRightTrianglesForVertices(store, null);
    }

    /**
     * Adapts a List&lt;Point&gt; to the PointStore interface.
     */
    private static class ListPointStore implements PointStore {
        private final List<Point> points;

        ListPointStore(List<Point> points) {
            this.points = points;
        }

        @Override public int getX(int idx) { return points.get(idx).x; }
        @Override public int getY(int idx) { return points.get(idx).y; }
        @Override public int numPoints() { return points.size(); }
        @Override public void close() {}
    }

    /**
     * Counts right triangles with grouping points by their slope and finds perpendicular pairs.
     * Delegates to the PointStore overload via a lightweight adapter.
     *
     * @param points list of all points
     * @param vertexIndices vertices to check, or null to check all
     * @return number of right triangles
     */
    public static int countRightTrianglesForVertices(List<Point> points, List<Integer> vertexIndices) {
        return countRightTrianglesForVertices(new ListPointStore(points), vertexIndices);
    }

    /**
     * Counts right triangles using slope grouping algorithm with PointStore.
     *
     * @param store point store containing all points
     * @param vertexIndices vertices to check, or null to check all
     * @return number of right triangles
     */
    public static int countRightTrianglesForVertices(PointStore store, List<Integer> vertexIndices) {
        int count = 0;
        int n = store.numPoints();

        List<Integer> verticesToProcess;
        if (vertexIndices == null) {
            verticesToProcess = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                verticesToProcess.add(i);
            }
        } else {
            verticesToProcess = vertexIndices;
        }

        for (int i : verticesToProcess) {
            int vertexX = store.getX(i);
            int vertexY = store.getY(i);
            java.util.Map<Long, Integer> slopeCount = new java.util.HashMap<>();

            for (int j = 0; j < n; j++) {
                if (i == j) continue;
                long dx = store.getX(j) - vertexX;
                long dy = store.getY(j) - vertexY;

                long slope = computeSlope(dx, dy);
                slopeCount.put(slope, slopeCount.getOrDefault(slope, 0) + 1);
            }

            for (java.util.Map.Entry<Long, Integer> entry : slopeCount.entrySet()) {
                long slope = entry.getKey();
                int cnt = entry.getValue();

                long perpSlope = getPerpendicularSlope(slope);
                Integer perpCount = slopeCount.get(perpSlope);

                if (perpCount != null && slope <= perpSlope) {
                    count += cnt * perpCount;
                }
            }
        }

        return count;
    }

    private static long computeSlope(long dx, long dy) {
        if (dx == 0 && dy == 0) return 0;
        if (dx == 0) return Long.MAX_VALUE;
        if (dy == 0) return Long.MAX_VALUE - 1;

        long gcd = gcd(Math.abs(dx), Math.abs(dy));
        dx /= gcd;
        dy /= gcd;

        if (dx < 0) {
            dx = -dx;
            dy = -dy;
        }

        return (dy + 1000000) * 2000000L + (dx + 1000000);
    }

    private static long getPerpendicularSlope(long slope) {
        if (slope == Long.MAX_VALUE) return Long.MAX_VALUE - 1;
        if (slope == Long.MAX_VALUE - 1) return Long.MAX_VALUE;

        long dy = (slope / 2000000L) - 1000000;
        long dx = (slope % 2000000L) - 1000000;

        long perpDx = -dy;
        long perpDy = dx;

        if (perpDx < 0) {
            perpDx = -perpDx;
            perpDy = -perpDy;
        }

        return (perpDy + 1000000) * 2000000L + (perpDx + 1000000);
    }

    private static long gcd(long a, long b) {
        while (b != 0) {
            long temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    /**
     * Distributes vertices among workers using round robin.
     *
     * @param numVertices total vertices
     * @param numWorkers number of workers
     * @return vertex assignments per worker
     */
    public static List<List<Integer>> distributeVertices(int numVertices, int numWorkers) {
        List<List<Integer>> assignments = new ArrayList<>();

        for (int i = 0; i < numWorkers; i++) {
            assignments.add(new ArrayList<>());
        }

        for (int v = 0; v < numVertices; v++) {
            int workerId = v % numWorkers;
            assignments.get(workerId).add(v);
        }

        return assignments;
    }

    /**
     * Validates and parses number of threads or processes parameter.
     *
     * @param arg command line argument
     * @param paramName parameter name for errors
     * @return parsed number
     */
    public static int parseWorkerCount(String arg, String paramName) {
        try {
            int count = Integer.parseInt(arg);
            if (count < 1) {
                System.err.println("Error: Number of " + paramName + " must be at least 1");
                System.exit(6);
            }
            return count;
        } catch (NumberFormatException e) {
            System.err.println("Error: Invalid number of " + paramName + " - " + e.getMessage());
            System.exit(7);
            return -1;
        }
    }

    /**
     * Creates a PointStore from file with error handling
     *
     * @param filename the file to read
     * @return PointStore instance
     */
    public static PointStore createPointStore(String filename) {
        try {
            if (filename.endsWith(".dat")) {
                return new BinPointStore(filename);
            } else {
                return new TextPointStore(filename);
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error: File not found - " + e.getMessage());
            System.exit(3);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(4);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(5);
        }
        return null;
    }

    /**
     * Reads points from input file with error handling
     *
     * @param filename the file to read
     * @return list of points
     */
    public static List<Point> readPointsWithErrorHandling(String filename) {
        try {
            return readPointsFromFile(filename);
        } catch (FileNotFoundException e) {
            System.err.println("Error: File not found - " + e.getMessage());
            System.exit(3);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(4);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(5);
        }
        return null;
    }

    /**
     * Reads points from input file
     *
     * @param filename the file to read
     * @return list of points
     * @throws IOException if error reading file
     * @throws IllegalArgumentException if invalid format
     */
    public static List<Point> readPointsFromFile(String filename)
            throws IOException, IllegalArgumentException {

        List<Point> points = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(filename));

        try {
            String firstLine = br.readLine();
            if (firstLine == null) {
                throw new IllegalArgumentException("Empty file");
            }

            int n;
            try {
                n = Integer.parseInt(firstLine.trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid number format: " + firstLine);
            }

            if (n < 0) {
                throw new IllegalArgumentException("Number of points cannot be negative");
            }

            for (int i = 0; i < n; i++) {
                String line = br.readLine();
                if (line == null) {
                    throw new IllegalArgumentException(
                        "Expected " + n + " points but only found " + i);
                }

                String[] parts = line.trim().split("\\s+");
                if (parts.length < 2) {
                    throw new IllegalArgumentException(
                        "Invalid point format at line " + (i + 2) + ": " + line);
                }

                try {
                    int x = Integer.parseInt(parts[0]);
                    int y = Integer.parseInt(parts[1]);
                    points.add(new Point(x, y));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                        "Invalid coordinates at line " + (i + 2) + ": " + line);
                }
            }

        } finally {
            br.close();
        }

        return points;
    }

    /**
     * Reads points from an input stream for inter process communication
     *
     * @param in input stream to read from
     * @return list of points
     * @throws IOException if error reading
     */
    public static List<Point> readPointsFromStream(BufferedReader in) throws IOException {
        List<Point> points = new ArrayList<>();

        String line = in.readLine();
        if (line == null) {
            throw new IOException("Empty input stream");
        }

        int n = Integer.parseInt(line.trim());

        for (int i = 0; i < n; i++) {
            line = in.readLine();
            if (line == null) {
                throw new IOException("Unexpected end of stream");
            }

            String[] parts = line.trim().split("\\s+");
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            points.add(new Point(x, y));
        }

        return points;
    }

    /**
     * Reads a list of integers from a stream
     *
     * @param in input stream
     * @return list of integers
     * @throws IOException if error reading
     */
    public static List<Integer> readIntListFromStream(BufferedReader in) throws IOException {
        List<Integer> result = new ArrayList<>();

        String line = in.readLine();
        if (line == null) {
            throw new IOException("Unexpected end of stream");
        }

        int count = Integer.parseInt(line.trim());

        for (int i = 0; i < count; i++) {
            line = in.readLine();
            if (line == null) {
                throw new IOException("Unexpected end of stream");
            }
            result.add(Integer.parseInt(line.trim()));
        }

        return result;
    }
}
