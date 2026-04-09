package com.tryright;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads points from text files with integer coordinates.
 */
public class TextPointStore implements PointStore {
    private final List<Integer> xCoords;
    private final List<Integer> yCoords;

    /**
     * Loads points from a text file.
     *
     * @param filename path to text file
     * @throws FileNotFoundException if file doesn't exist
     * @throws IOException if read error
     * @throws IllegalArgumentException if invalid format
     */
    public TextPointStore(String filename) throws IOException, IllegalArgumentException {
        xCoords = new ArrayList<>();
        yCoords = new ArrayList<>();
        loadFromFile(filename);
    }

    private void loadFromFile(String filename) throws IOException, IllegalArgumentException {
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
                    xCoords.add(x);
                    yCoords.add(y);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                        "Invalid coordinates at line " + (i + 2) + ": " + line);
                }
            }

        } finally {
            br.close();
        }
    }

    @Override
    public int getX(int idx) {
        return xCoords.get(idx);
    }

    @Override
    public int getY(int idx) {
        return yCoords.get(idx);
    }

    @Override
    public int numPoints() {
        return xCoords.size();
    }

    @Override
    public void close() {
        // no resources to close for in-memory storage
    }
}
