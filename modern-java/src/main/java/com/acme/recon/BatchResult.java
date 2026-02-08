package com.acme.recon;

import java.util.ArrayList;
import java.util.List;

public final class BatchResult {

    private int processedOk;
    private long totalAppliedCents;
    private int commits;
    private final List<ExceptionRecord> exceptions = new ArrayList<>();

    public void addSuccess(long amountCents) {
        processedOk++;
        totalAppliedCents += amountCents;
    }

    public void addException(ExceptionRecord rec) {
        exceptions.add(rec);
    }

    public void incrementCommits() {
        commits++;
    }

    public int processedOk() {
        return processedOk;
    }

    public int exceptionCount() {
        return exceptions.size();
    }

    public long totalAppliedCents() {
        return totalAppliedCents;
    }

    public int commits() {
        return commits;
    }

    public List<ExceptionRecord> exceptions() {
        return List.copyOf(exceptions);
    }

    public String renderSummary() {
        return "PROCESSED_OK=" + processedOk + "\n"
                + "EXCEPTIONS=" + exceptions.size() + "\n"
                + "TOTAL_APPLIED_CENTS=" + totalAppliedCents + "\n"
                + "COMMITS=" + commits + "\n";
    }

    public String renderExceptionsCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("line_no,reason,raw\n");
        for (ExceptionRecord rec : exceptions) {
            sb.append(rec.toCsvLine()).append('\n');
        }
        return sb.toString();
    }
}
