package com.acme.recon;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class Config {

    private final long maxPaymentCents;
    private final int commitEvery;

    private Config(long maxPaymentCents, int commitEvery) {
        this.maxPaymentCents = maxPaymentCents;
        this.commitEvery = commitEvery;
    }

    public long maxPaymentCents() {
        return maxPaymentCents;
    }

    public int commitEvery() {
        return commitEvery;
    }

    public static Config load(Path path) throws IOException, ConfigException {
        Map<String, String> props = new HashMap<>();
        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.strip();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eq = line.indexOf('=');
                if (eq < 0) {
                    throw new ConfigException("Malformed config line: " + line);
                }
                props.put(line.substring(0, eq).strip(), line.substring(eq + 1).strip());
            }
        }

        String maxStr = props.get("MAX_PAYMENT_CENTS");
        if (maxStr == null) {
            throw new ConfigException("Missing MAX_PAYMENT_CENTS");
        }
        String commitStr = props.get("COMMIT_EVERY");
        if (commitStr == null) {
            throw new ConfigException("Missing COMMIT_EVERY");
        }

        long maxPayment;
        int commitEvery;
        try {
            maxPayment = Long.parseLong(maxStr);
        } catch (NumberFormatException e) {
            throw new ConfigException("Invalid MAX_PAYMENT_CENTS: " + maxStr);
        }
        try {
            commitEvery = Integer.parseInt(commitStr);
        } catch (NumberFormatException e) {
            throw new ConfigException("Invalid COMMIT_EVERY: " + commitStr);
        }
        if (commitEvery <= 0) {
            throw new ConfigException("COMMIT_EVERY must be positive: " + commitEvery);
        }
        return new Config(maxPayment, commitEvery);
    }
}
