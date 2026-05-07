// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package frc.robot.util;

import edu.wpi.first.util.datalog.DataLogReader;
import edu.wpi.first.util.datalog.DataLogRecord;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * LogDumper is a utility class that parses `.wpilog` files and outputs data in various formats.
 *
 * <p>It supports: - Console output (tabular or raw) - CSV file export - Statistics and correlation
 * analysis - Key filtering and time range selection
 *
 * <p>Usage: java LogDumper --log <path> [--keys <k1,k2,...>] [--start <seconds>] [--end <seconds>]
 * [--out <csv>] [--stats]
 */
public class LogDumper {
  private String logPath;
  private List<String> filterKeys = new ArrayList<>();
  private double startTime = Double.NEGATIVE_INFINITY;
  private double endTime = Double.POSITIVE_INFINITY;
  private String outputFile = null;
  private boolean statsMode = false;
  private Map<String, List<Double>> keyValues = new LinkedHashMap<>();
  private Map<String, List<Double>> timestamps = new LinkedHashMap<>();
  private List<Double> userCodeMSValues = new ArrayList<>();
  private Map<String, List<Double>> latencyValues = new HashMap<>();

  /**
   * Parse command-line arguments and execute log dumping.
   *
   * @param args Command-line arguments
   */
  public static void main(String[] args) {
    LogDumper dumper = new LogDumper();
    if (!dumper.parseArgs(args)) {
      printUsage();
      System.exit(1);
    }

    try {
      dumper.readLog();
      if (dumper.statsMode) {
        dumper.printStats();
      } else {
        dumper.printData();
      }
    } catch (IOException e) {
      System.err.println("Error reading log file: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * Parse command-line arguments.
   *
   * @param args Command-line arguments
   * @return true if arguments are valid
   */
  private boolean parseArgs(String[] args) {
    for (int i = 0; i < args.length; i++) {
      switch (args[i]) {
        case "--log":
          if (i + 1 < args.length) {
            logPath = args[++i];
          } else {
            System.err.println("--log requires an argument");
            return false;
          }
          break;

        case "--keys":
          if (i + 1 < args.length) {
            String[] keys = args[++i].split(",");
            for (String key : keys) {
              filterKeys.add(key.trim());
            }
          } else {
            System.err.println("--keys requires an argument");
            return false;
          }
          break;

        case "--start":
          if (i + 1 < args.length) {
            try {
              startTime = Double.parseDouble(args[++i]);
            } catch (NumberFormatException e) {
              System.err.println("--start requires a numeric argument");
              return false;
            }
          } else {
            System.err.println("--start requires an argument");
            return false;
          }
          break;

        case "--end":
          if (i + 1 < args.length) {
            try {
              endTime = Double.parseDouble(args[++i]);
            } catch (NumberFormatException e) {
              System.err.println("--end requires a numeric argument");
              return false;
            }
          } else {
            System.err.println("--end requires an argument");
            return false;
          }
          break;

        case "--out":
          if (i + 1 < args.length) {
            outputFile = args[++i];
          } else {
            System.err.println("--out requires an argument");
            return false;
          }
          break;

        case "--stats":
          statsMode = true;
          break;

        default:
          System.err.println("Unknown argument: " + args[i]);
          return false;
      }
    }

    if (logPath == null) {
      System.err.println("--log is required");
      return false;
    }

    return true;
  }

  /** Read and parse the log file. */
  private void readLog() throws IOException {
    DataLogReader reader = new DataLogReader(logPath);
    Map<Integer, String> entryNames = new HashMap<>();
    Map<Integer, String> entryTypes = new HashMap<>();

    // First pass: collect metadata
    for (DataLogRecord record : reader) {
      if (record.isStart()) {
        int entryId = record.getStartData().entry;
        String name = record.getStartData().name;
        String type = record.getStartData().type;
        entryNames.put(entryId, name);
        entryTypes.put(entryId, type);
      }
    }

    // Second pass: collect data
    reader = new DataLogReader(logPath);
    for (DataLogRecord record : reader) {
      if (record.isControl() || record.isFinish()) {
        continue;
      }

      int entryId = record.getEntry();
      String name = entryNames.get(entryId);
      String type = entryTypes.get(entryId);
      double timestamp = record.getTimestamp() / 1e6; // Convert µs to seconds

      // Apply time filter
      if (timestamp < startTime || timestamp > endTime) {
        continue;
      }

      // Track UserCodeMS for stats
      if (name != null && name.equals("/UserCodeMS")) {
        try {
          userCodeMSValues.add(record.getDouble());
        } catch (Exception e) {
          // Ignore type mismatches
        }
      }

      // Track latency values for correlation
      if (name != null && name.contains("LatencyPeriodicSec")) {
        try {
          latencyValues.computeIfAbsent(name, k -> new ArrayList<>()).add(record.getDouble());
        } catch (Exception e) {
          // Ignore type mismatches
        }
      }

      // Skip if key filtering is active and this key is not in the filter
      if (!filterKeys.isEmpty() && !filterKeys.contains(name)) {
        continue;
      }

      // Store the value
      if (name != null) {
        keyValues.computeIfAbsent(name, k -> new ArrayList<>());
        timestamps.computeIfAbsent(name, k -> new ArrayList<>());

        try {
          if ("double".equals(type)) {
            keyValues.get(name).add(record.getDouble());
          } else if ("int64".equals(type)) {
            keyValues.get(name).add((double) record.getInteger());
          } else if ("boolean".equals(type)) {
            keyValues.get(name).add(record.getBoolean() ? 1.0 : 0.0);
          } else if ("string".equals(type)) {
            // Store as string representation
            keyValues.get(name).add(0.0); // Placeholder for strings
          }
          timestamps.get(name).add(timestamp);
        } catch (Exception e) {
          // Skip entries that don't match expected types
          keyValues.get(name).remove(keyValues.get(name).size() - 1);
          timestamps.get(name).remove(timestamps.get(name).size() - 1);
        }
      }
    }
  }

  /** Print data to console or CSV file. */
  private void printData() throws IOException {
    if (outputFile != null) {
      printCSV();
    } else {
      printConsole();
    }
  }

  /** Print data to console in tabular format. */
  private void printConsole() {
    if (keyValues.isEmpty()) {
      System.out.println("No data found matching the specified filters.");
      return;
    }

    // Print header
    System.out.print("Timestamp");
    for (String key : keyValues.keySet()) {
      System.out.print("\t" + key);
    }
    System.out.println();

    // Print data rows
    Set<Double> allTimestamps = new TreeSet<>();
    for (List<Double> times : timestamps.values()) {
      allTimestamps.addAll(times);
    }

    for (double timestamp : allTimestamps) {
      System.out.printf("%.3f", timestamp);
      for (String key : keyValues.keySet()) {
        List<Double> values = keyValues.get(key);
        List<Double> times = timestamps.get(key);
        int index = times.indexOf(timestamp);
        if (index >= 0) {
          System.out.printf("\t%.2f", values.get(index));
        } else {
          System.out.print("\t-");
        }
      }
      System.out.println();
    }
  }

  /** Export data to CSV file. */
  private void printCSV() throws IOException {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
      if (keyValues.isEmpty()) {
        System.out.println("No data found matching the specified filters.");
        return;
      }

      // Write header
      writer.write("Timestamp");
      for (String key : keyValues.keySet()) {
        writer.write("," + key);
      }
      writer.newLine();

      // Write data rows
      Set<Double> allTimestamps = new TreeSet<>();
      for (List<Double> times : timestamps.values()) {
        allTimestamps.addAll(times);
      }

      for (double timestamp : allTimestamps) {
        writer.write(String.format("%.3f", timestamp));
        for (String key : keyValues.keySet()) {
          List<Double> values = keyValues.get(key);
          List<Double> times = timestamps.get(key);
          int index = times.indexOf(timestamp);
          if (index >= 0) {
            writer.write(String.format(",%.2f", values.get(index)));
          } else {
            writer.write(",");
          }
        }
        writer.newLine();
      }

      System.out.println("Data exported to " + outputFile);
    }
  }

  /** Print statistics and correlation analysis. */
  private void printStats() {
    if (userCodeMSValues.isEmpty()) {
      System.out.println("No UserCodeMS data found in the specified time range.");
      return;
    }

    // Calculate UserCodeMS statistics
    double mean = userCodeMSValues.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    double max = userCodeMSValues.stream().mapToDouble(Double::doubleValue).max().orElse(0);
    double min = userCodeMSValues.stream().mapToDouble(Double::doubleValue).min().orElse(0);
    double stdDev = calculateStdDev(userCodeMSValues, mean);

    System.out.println("UserCodeMS Statistics:");
    System.out.printf("  Mean:             %.2f ms%n", mean);
    System.out.printf("  Max:              %.2f ms%n", max);
    System.out.printf("  Min:              %.2f ms%n", min);
    System.out.printf("  Std Dev:          %.2f ms%n%n", stdDev);

    // Calculate correlations
    if (!latencyValues.isEmpty()) {
      System.out.println("Correlations with UserCodeMS:");
      for (String latencyKey : latencyValues.keySet()) {
        List<Double> latencies = latencyValues.get(latencyKey);
        double correlation = calculateCorrelation(userCodeMSValues, latencies);
        System.out.printf("  %s: %.2f%n", latencyKey, correlation);
      }
    }
  }

  /**
   * Calculate standard deviation.
   *
   * @param values Data values
   * @param mean Mean of the data
   * @return Standard deviation
   */
  private double calculateStdDev(List<Double> values, double mean) {
    double sumSquaredDiff = values.stream().mapToDouble(v -> Math.pow(v - mean, 2)).sum();
    return Math.sqrt(sumSquaredDiff / values.size());
  }

  /**
   * Calculate Pearson correlation coefficient between two lists.
   *
   * @param x First data list
   * @param y Second data list
   * @return Correlation coefficient (-1 to 1)
   */
  private double calculateCorrelation(List<Double> x, List<Double> y) {
    int n = Math.min(x.size(), y.size());
    if (n < 2) return 0.0;

    double xMean = x.stream().limit(n).mapToDouble(Double::doubleValue).average().orElse(0);
    double yMean = y.stream().limit(n).mapToDouble(Double::doubleValue).average().orElse(0);

    double covariance = 0;
    double xStdDev = 0;
    double yStdDev = 0;

    for (int i = 0; i < n; i++) {
      double dx = x.get(i) - xMean;
      double dy = y.get(i) - yMean;
      covariance += dx * dy;
      xStdDev += dx * dx;
      yStdDev += dy * dy;
    }

    if (xStdDev == 0 || yStdDev == 0) return 0.0;

    return covariance / Math.sqrt(xStdDev * yStdDev);
  }

  /** Print usage information. */
  private static void printUsage() {
    System.out.println("Usage: java LogDumper --log <path> [options]");
    System.out.println();
    System.out.println("Options:");
    System.out.println("  --log <path>          Path to the .wpilog file (REQUIRED)");
    System.out.println("  --keys <k1,k2,...>   Comma-separated list of keys to filter");
    System.out.println("  --start <seconds>     Start time in seconds");
    System.out.println("  --end <seconds>       End time in seconds");
    System.out.println("  --out <csv_file>      Output path for CSV file");
    System.out.println("  --stats               Run in statistics mode");
    System.out.println();
    System.out.println("Examples:");
    System.out.println("  java LogDumper --log match.wpilog");
    System.out.println("  java LogDumper --log match.wpilog --keys /RealOutputs/Launcher/RPM");
    System.out.println("  java LogDumper --log match.wpilog --out data.csv --start 0 --end 15");
    System.out.println("  java LogDumper --log match.wpilog --stats");
  }
}
