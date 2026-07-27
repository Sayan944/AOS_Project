import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    private static PrintWriter writer;

    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    /**
     * Initializes the logger.
     * Creates logs/siteX_log.txt
     */
    public static void initialize(int siteId) {

        try {

            File folder = new File("logs");

            if (!folder.exists()) {
                folder.mkdir();
            }

            writer = new PrintWriter(
                    new FileWriter(
                            "logs/site" + siteId + "_log.txt",
                            true));

        } catch (IOException e) {

            System.out.println("Unable to create log file.");

            e.printStackTrace();
        }
    }

    /**
     * Prints a normal log message.
     */
    public static synchronized void log(int siteId, String message) {

        String time =
                LocalDateTime.now().format(formatter);

        String output =
                "[" + time + "] "
                        + "[University " + siteId + "] "
                        + message;

        System.out.println(output);

        if (writer != null) {

            writer.println(output);

            writer.flush();
        }
    }

    /**
     * Prints a separator.
     */
    public static synchronized void separator() {

        String line =
                "------------------------------------------------------------";

        System.out.println(line);

        if (writer != null) {

            writer.println(line);

            writer.flush();
        }
    }

    /**
     * Prints a section heading.
     */
    public static synchronized void heading(String title) {

        separator();

        System.out.println(title);

        separator();

        if (writer != null) {

            writer.println(title);

            writer.println(
                    "------------------------------------------------------------");

            writer.flush();
        }
    }

    /**
     * Prints incoming messages.
     */
    public static synchronized void received(
            int siteId,
            Message message) {

        log(siteId,
                "RECEIVED "
                        + message.getType()
                        + " FROM University "
                        + message.getSenderId());
    }

    /**
     * Prints outgoing messages.
     */
    public static synchronized void sent(
            int siteId,
            Message message) {

        log(siteId,
                "SENT "
                        + message.getType()
                        + " TO University "
                        + message.getReceiverId());
    }

    /**
     * Raymond request.
     */
    public static synchronized void request(int siteId) {

        log(siteId,
                "REQUESTING AI ACCELERATOR");
    }

    /**
     * Enter CS.
     */
    public static synchronized void enterCS(int siteId) {

        log(siteId,
                "ENTERING CRITICAL SECTION");
    }

    /**
     * Exit CS.
     */
    public static synchronized void exitCS(int siteId) {

        log(siteId,
                "EXITING CRITICAL SECTION");
    }

    /**
     * Snapshot started.
     */
    public static synchronized void snapshotStarted(
            int siteId,
            int snapshotId) {

        log(siteId,
                "STARTED SNAPSHOT #" + snapshotId);
    }

    /**
     * Snapshot completed.
     */
    public static synchronized void snapshotCompleted(
            int siteId,
            int snapshotId) {

        log(siteId,
                "COMPLETED SNAPSHOT #" + snapshotId);
    }

    /**
     * Close logger.
     */
    public static void close() {

        if (writer != null) {

            writer.close();
        }
    }

}