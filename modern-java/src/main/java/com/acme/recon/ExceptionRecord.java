package com.acme.recon;

public record ExceptionRecord(int lineNo, ExceptionReason reason, String rawLine) {

    public String toCsvLine() {
        return lineNo + "," + reason.name() + ",\"" + rawLine + "\"";
    }
}
