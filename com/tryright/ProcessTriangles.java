package com.tryright;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Counts right triangles using multiple processes that run in parallel
 */
public class ProcessTriangles {

    /**
     * Entry point for multiprocess triangle counting
     *
     * @param args input filename and process count
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java com.tryright.ProcessTriangles <input_file> <num_processes>");
            System.err.println("  input_file: Path to file containing points");
            System.err.println("  num_processes: Number of processes to use");
            System.exit(2);
        }

        String filename = args[0];
        int numProcesses = TriangleCounter.parseWorkerCount(args[1], "processes");

        PointStore store = TriangleCounter.createPointStore(filename);

        try {
            int n = store.numPoints();

            // If only few points or only 1 process, run sequentially
            if (n < 3 || numProcesses == 1) {
                int result = TriangleCounter.countRightTriangles(store);
                store.close();
                System.out.println(result);
                return;
            }

            List<List<Integer>> vertexAssignments = TriangleCounter.distributeVertices(n, numProcesses);

            // create task processes
            List<Process> processes = new ArrayList<>();
            List<PrintWriter> inputs = new ArrayList<>();
            List<BufferedReader> outputs = new ArrayList<>();

            String classpath = System.getProperty("java.class.path");
            String javaHome = System.getProperty("java.home");
            String javaBin = javaHome + File.separator + "bin" + File.separator + "java";

            // start all processes first
            for (int i = 0; i < numProcesses; i++) {
                List<Integer> assignedVertices = vertexAssignments.get(i);

                // skip if no vertices assigned
                if (assignedVertices.isEmpty()) {
                    continue;
                }

                ProcessBuilder pb = new ProcessBuilder(
                    javaBin,
                    "-cp", classpath,
                    "com.tryright.ComputeTask"
                );

                pb.redirectErrorStream(false);
                Process process = pb.start();
                processes.add(process);

                //larger buffers for faster I/O
                inputs.add(new PrintWriter(new BufferedOutputStream(process.getOutputStream(), 8192)));
                outputs.add(new BufferedReader(new InputStreamReader(process.getInputStream()), 8192));
            }

            // Send data to all tasks in parallel to track which process gets which assignment
            int processIdx = 0;
            for (int i = 0; i < numProcesses; i++) {
                List<Integer> assignedVertices = vertexAssignments.get(i);

                if (assignedVertices.isEmpty()) {
                    continue;
                }

                PrintWriter out = inputs.get(processIdx);

                //send points
                out.println(n);
                for (int idx = 0; idx < n; idx++) {
                    out.println(store.getX(idx) + " " + store.getY(idx));
                }

                //send assigned vertex indices
                out.println(assignedVertices.size());
                for (int vertex : assignedVertices) {
                    out.println(vertex);
                }

                out.flush();
                out.close();
                processIdx++;
            }

            // Get results from all
            int totalCount = 0;
            for (int i = 0; i < processes.size(); i++) {
                Process process = processes.get(i);
                BufferedReader reader = outputs.get(i);

                // wait for process to complete
                int exitCode = process.waitFor();

                if (exitCode != 0) {
                    BufferedReader errorReader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream())
                    );
                    String errorLine;
                    StringBuilder errorMsg = new StringBuilder();
                    while ((errorLine = errorReader.readLine()) != null) {
                        errorMsg.append(errorLine).append("\n");
                    }
                    System.err.println("Error: Task process " + i + " failed with exit code " + exitCode + ": " + errorMsg);
                    System.exit(9);
                }

                // read result
                String resultLine = reader.readLine();
                if (resultLine == null) {
                    System.err.println("Error: No output from task process " + i);
                    System.exit(10);
                }

                int partialCount = Integer.parseInt(resultLine.trim());
                totalCount += partialCount;
            }

            // output final result
            store.close();
            System.out.println(totalCount);

        } catch (IOException e) {
            store.close();
            System.err.println("Error: I/O error - " + e.getMessage());
            System.exit(4);
        } catch (InterruptedException e) {
            store.close();
            System.err.println("Error: Process interrupted - " + e.getMessage());
            System.exit(8);
        }
    }

}
