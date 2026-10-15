package utils;

import config.TestConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TicketDatabaseClient {

    public TicketDatabaseClient() {
    }

    public TicketDatabaseRecord findByIncidentNumber(String incidentNumber) {
        long id = parseIncidentId(incidentNumber);
        String sql = "select id, short_description, description, category, priority, status, assigned_group "
                + "from tickets where id = ?";
        try (Connection connection = DriverManager.getConnection(
                TestConfig.dbUrl(), TestConfig.dbUsername(), TestConfig.dbPassword());
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new AssertionError("Ticket not found in database: " + incidentNumber);
                }
                return new TicketDatabaseRecord(
                        "INC%06d".formatted(resultSet.getLong("id")),
                        resultSet.getString("short_description"),
                        resultSet.getString("description"),
                        resultSet.getString("category"),
                        resultSet.getString("priority"),
                        resultSet.getString("status"),
                        resultSet.getString("assigned_group"));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to query TicketFlow database", exception);
        }
    }

    private long parseIncidentId(String incidentNumber) {
        if (incidentNumber == null || !incidentNumber.matches("INC\\d{6}")) {
            throw new IllegalArgumentException("Invalid incident number: " + incidentNumber);
        }
        return Long.parseLong(incidentNumber.substring(3));
    }

    public record TicketDatabaseRecord(
            String incidentNumber,
            String shortDescription,
            String description,
            String category,
            String priority,
            String status,
            String assignedGroup) {
    }
}
