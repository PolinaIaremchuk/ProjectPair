package com.tryright;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * A class for reading points from binary files using memory mapped IO
 * Points are stored as pairs of 4 byte big endian ints.
 */
public class BinPointStore implements PointStore {
    private static final int INTEGER_SIZE = 4;
    private static final int POINT_SIZE = 2 * INTEGER_SIZE;

    private final RandomAccessFile file;
    private final FileChannel channel;
    private final MappedByteBuffer buffer;
    private final int numPoints;

    /**
     * Opens a binary file and maps it to memory
     *
     * @param filename path to binary file
     * @throws IOException if file access error
     * @throws IllegalArgumentException if invalid file size
     */
    public BinPointStore(String filename) throws IOException, IllegalArgumentException {
        File f = new File(filename);
        if (!f.exists()) {
            throw new IOException("File not found: " + filename);
        }

        long fileSize = f.length();

        //size must be multiple of 8 bytes
        if (fileSize % POINT_SIZE != 0) {
            throw new IllegalArgumentException(
                "Invalid binary file format: file size must be a multiple of 8 bytes");
        }

        this.numPoints = (int) (fileSize / POINT_SIZE);

        this.file = new RandomAccessFile(filename, "r");
        this.channel = file.getChannel();
        if (fileSize > 0) {
            this.buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);
        } else {
            this.buffer = null;
        }
    }

    @Override
    public int getX(int idx) {
        if (idx < 0 || idx >= numPoints) {
            throw new IndexOutOfBoundsException("Index " + idx + " out of bounds for " + numPoints + " points");
        }

        if (buffer == null) {
            throw new IllegalStateException("No data in file");
        }

        int position = idx * POINT_SIZE;
        return buffer.getInt(position);
    }

    @Override
    public int getY(int idx) {
        if (idx < 0 || idx >= numPoints) {
            throw new IndexOutOfBoundsException("Index " + idx + " out of bounds for " + numPoints + " points");
        }

        if (buffer == null) {
            throw new IllegalStateException("No data in file");
        }

        int position = idx * POINT_SIZE + INTEGER_SIZE;
        return buffer.getInt(position);
    }

    @Override
    public int numPoints() {
        return numPoints;
    }

    @Override
    public void close() {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
            if (file != null) {
                file.close();
            }
        } catch (IOException e) {
            System.err.println("Warning: Error closing file - " + e.getMessage());
        }
    }
}
