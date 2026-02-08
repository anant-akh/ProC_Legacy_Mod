package com.acme.recon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class CustomerDao {

    private final Connection conn;

    public CustomerDao(Connection conn) {
        this.conn = conn;
    }

    public CustomerInfo lookup(String customerId) throws SQLException {
        String sql = "SELECT status, credit_limit FROM customers WHERE customer_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new CustomerInfo(rs.getString("status"), rs.getLong("credit_limit"));
            }
        }
    }

    public record CustomerInfo(String status, long creditLimit) {
    }
}
