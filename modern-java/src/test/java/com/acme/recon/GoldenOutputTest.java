package com.acme.recon;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GoldenOutputTest {

    private static final String JDBC_URL = "jdbc:h2:mem:recontest;DB_CLOSE_DELAY=-1";

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws SQLException {
        try (Connection conn = DriverManager.getConnection(JDBC_URL);
             Statement st = conn.createStatement()) {
            st.execute("DROP TABLE IF EXISTS customers");
            st.execute("CREATE TABLE customers ("
                    + "customer_id VARCHAR(20), "
                    + "status VARCHAR(10), "
                    + "credit_limit BIGINT)");
            st.execute("INSERT INTO customers VALUES ('c001','ACTIVE',100000)");
            st.execute("INSERT INTO customers VALUES ('c002','ACTIVE',50000)");
            st.execute("INSERT INTO customers VALUES ('c003','SUSPENDED',5000)");
            conn.commit();
        }
    }

    @Test
    void matchesGoldenSummary() throws IOException {
        Path configPath = copyResource("recon.cfg");
        Path paymentsPath = copyResource("payments.csv");
        Path summaryPath = tempDir.resolve("out_summary.txt");
        Path exceptionsPath = tempDir.resolve("out_exceptions.csv");

        int exitCode = Main.run(new String[]{
                configPath.toString(),
                paymentsPath.toString(),
                summaryPath.toString(),
                exceptionsPath.toString(),
                JDBC_URL
        });

        String expectedSummary = readResource("expected_summary.txt");
        String actualSummary = Files.readString(summaryPath);
        assertEquals(expectedSummary, actualSummary, "Summary output mismatch");

        assertEquals(Main.EXIT_SUCCESS_WITH_EXCEPTIONS, exitCode, "Exit code mismatch");
    }

    @Test
    void matchesGoldenExceptions() throws IOException {
        Path configPath = copyResource("recon.cfg");
        Path paymentsPath = copyResource("payments.csv");
        Path summaryPath = tempDir.resolve("out_summary.txt");
        Path exceptionsPath = tempDir.resolve("out_exceptions.csv");

        Main.run(new String[]{
                configPath.toString(),
                paymentsPath.toString(),
                summaryPath.toString(),
                exceptionsPath.toString(),
                JDBC_URL
        });

        String expectedExceptions = readResource("expected_exceptions.csv");
        String actualExceptions = Files.readString(exceptionsPath);
        assertEquals(expectedExceptions, actualExceptions, "Exceptions output mismatch");
    }

    @Test
    void badArgsReturnsExitCode2() {
        int exitCode = Main.run(new String[]{});
        assertEquals(Main.EXIT_BAD_ARGS, exitCode);
    }

    @Test
    void missingFileReturnsExitCode3() {
        Path summaryPath = tempDir.resolve("out_summary.txt");
        Path exceptionsPath = tempDir.resolve("out_exceptions.csv");

        int exitCode = Main.run(new String[]{
                tempDir.resolve("nonexistent.cfg").toString(),
                tempDir.resolve("nonexistent.csv").toString(),
                summaryPath.toString(),
                exceptionsPath.toString(),
                JDBC_URL
        });
        assertEquals(Main.EXIT_IO_DB_ERROR, exitCode);
    }

    @Test
    void badConfigReturnsExitCode4() throws IOException {
        Path badConfig = tempDir.resolve("bad.cfg");
        Files.writeString(badConfig, "GARBAGE_ONLY\n");
        Path paymentsPath = copyResource("payments.csv");
        Path summaryPath = tempDir.resolve("out_summary.txt");
        Path exceptionsPath = tempDir.resolve("out_exceptions.csv");

        int exitCode = Main.run(new String[]{
                badConfig.toString(),
                paymentsPath.toString(),
                summaryPath.toString(),
                exceptionsPath.toString(),
                JDBC_URL
        });
        assertEquals(Main.EXIT_CONFIG_ERROR, exitCode);
    }

    private Path copyResource(String name) throws IOException {
        Path dest = tempDir.resolve(name);
        try (var in = getClass().getClassLoader().getResourceAsStream(name)) {
            if (in == null) {
                throw new IOException("Resource not found: " + name);
            }
            Files.copy(in, dest);
        }
        return dest;
    }

    private String readResource(String name) throws IOException {
        try (var in = getClass().getClassLoader().getResourceAsStream(name)) {
            if (in == null) {
                throw new IOException("Resource not found: " + name);
            }
            return new String(in.readAllBytes());
        }
    }
}
