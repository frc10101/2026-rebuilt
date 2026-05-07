package frc.robot.util;

import edu.wpi.first.util.datalog.DataLogReader;
import edu.wpi.first.util.datalog.DataLogRecord;
import java.io.IOException;
import java.util.TreeMap;

/** Utility to list all available keys in a WPI log file. */
public class ListKeys {
  public static void main(String[] args) throws IOException {
    if (args.length == 0) {
      printUsage();
      return;
    }

    String logPath = args[0];

    try {
      DataLogReader reader = new DataLogReader(logPath);
      System.out.println("Available keys in: " + logPath);
      System.out.println("==========================================");

      // Collect all unique keys and their types
      TreeMap<String, String> keys = new TreeMap<>();
      for (DataLogRecord record : reader) {
        try {
          if (record.isStart()) {
            String name = record.getStartData().name;
            String type = record.getStartData().type;
            keys.put(name, type);
          }
        } catch (Exception e) {
          // Skip records that can't be parsed
          continue;
        }
      }

      for (var entry : keys.entrySet()) {
        System.out.println(entry.getKey() + " [" + entry.getValue() + "]");
      }

      System.out.println("==========================================");
      System.out.println("Total entries listed above.");
    } catch (IOException e) {
      System.err.println("Error reading log file: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private static void printUsage() {
    System.out.println("Usage: java ListKeys <log_file>");
    System.out.println("  <log_file>: Path to the .wpilog file to analyze");
    System.out.println("\nExample:");
    System.out.println("  java ListKeys match.wpilog");
  }
}
