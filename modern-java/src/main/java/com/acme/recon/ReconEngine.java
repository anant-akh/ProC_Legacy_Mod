package com.acme.recon;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public final class ReconEngine {

    private final Config config;
    private final Connection conn;
    private final CustomerDao dao;

    public ReconEngine(Config config, Connection conn) {
        this.config = config;
        this.conn = conn;
        this.dao = new CustomerDao(conn);
    }

    public BatchResult run(Path paymentsFile) throws IOException, SQLException {
        BatchResult result = new BatchResult();
        int pendingCommit = 0;

        try (BufferedReader br = Files.newBufferedReader(paymentsFile)) {
            String line;
            int dataLineNo = 0;
            boolean headerSkipped = false;

            while ((line = br.readLine()) != null) {
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }

                dataLineNo++;

                String raw = line.strip();
                if (raw.isEmpty()) {
                    continue;
                }

                List<String> fields = CsvParser.splitLine(raw);

                if (fields.size() != 3) {
                    result.addException(new ExceptionRecord(dataLineNo, ExceptionReason.MALFORMED_ROW, raw));
                    continue;
                }

                String paymentId = fields.get(0);
                String customerId = fields.get(1);
                String amountStr = fields.get(2);

                long amountCents;
                try {
                    amountCents = Long.parseLong(amountStr);
                } catch (NumberFormatException e) {
                    result.addException(new ExceptionRecord(dataLineNo, ExceptionReason.MALFORMED_ROW, raw));
                    continue;
                }

                CustomerDao.CustomerInfo cust;
                try {
                    cust = dao.lookup(customerId);
                } catch (SQLException e) {
                    throw new SQLException("DB error looking up customer " + customerId, e);
                }

                if (cust == null) {
                    result.addException(new ExceptionRecord(dataLineNo, ExceptionReason.UNKNOWN_CUSTOMER, raw));
                    continue;
                }

                if (!"ACTIVE".equals(cust.status())) {
                    result.addException(new ExceptionRecord(dataLineNo, ExceptionReason.CUSTOMER_NOT_ACTIVE, raw));
                    continue;
                }

                if (amountCents > config.maxPaymentCents()) {
                    result.addException(new ExceptionRecord(dataLineNo, ExceptionReason.AMOUNT_EXCEEDS_MAX, raw));
                    continue;
                }

                result.addSuccess(amountCents);
                pendingCommit++;

                if (pendingCommit >= config.commitEvery()) {
                    conn.commit();
                    result.incrementCommits();
                    pendingCommit = 0;
                }
            }

            if (pendingCommit > 0) {
                conn.commit();
                result.incrementCommits();
            }
        }

        return result;
    }
}
