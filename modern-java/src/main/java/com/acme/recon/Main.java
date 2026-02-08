package com.acme.recon;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Main {

    public static final int EXIT_SUCCESS = 0;
    public static final int EXIT_SUCCESS_WITH_EXCEPTIONS = 1;
    public static final int EXIT_BAD_ARGS = 2;
    public static final int EXIT_IO_DB_ERROR = 3;
    public static final int EXIT_CONFIG_ERROR = 4;

    private Main() {
    }

    public static void main(String[] args) {
        int code = run(args);
        System.exit(code);
    }

    public static int run(String[] args) {
        if (args.length != 5) {
            usage();
            return EXIT_BAD_ARGS;
        }

        Path configPath = Path.of(args[0]);
        Path paymentsPath = Path.of(args[1]);
        Path summaryPath = Path.of(args[2]);
        Path exceptionsPath = Path.of(args[3]);
        String jdbcUrl = args[4];

        Config config;
        try {
            config = Config.load(configPath);
        } catch (IOException e) {
            System.err.println("ERROR: Cannot read config: " + e.getMessage());
            return EXIT_IO_DB_ERROR;
        } catch (ConfigException e) {
            System.err.println("ERROR: Bad config: " + e.getMessage());
            return EXIT_CONFIG_ERROR;
        }

        try (Connection conn = DriverManager.getConnection(jdbcUrl)) {
            conn.setAutoCommit(false);

            ReconEngine engine = new ReconEngine(config, conn);
            BatchResult result = engine.run(paymentsPath);

            Files.writeString(summaryPath, result.renderSummary());
            Files.writeString(exceptionsPath, result.renderExceptionsCsv());

            if (result.exceptionCount() > 0) {
                return EXIT_SUCCESS_WITH_EXCEPTIONS;
            }
            return EXIT_SUCCESS;

        } catch (SQLException e) {
            System.err.println("ERROR: Database error: " + e.getMessage());
            return EXIT_IO_DB_ERROR;
        } catch (IOException e) {
            System.err.println("ERROR: IO error: " + e.getMessage());
            return EXIT_IO_DB_ERROR;
        }
    }

    private static void usage() {
        System.err.println("Usage: recon <config> <payments.csv> <out_summary> <out_exceptions> <jdbc_url>");
    }
}
