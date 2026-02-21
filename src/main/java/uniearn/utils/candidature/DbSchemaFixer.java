package uniearn.utils.candidature;

import uniearn.database.MyConnection;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class DbSchemaFixer {
    public static void main(String[] args) {
        Connection conn = MyConnection.getInstance().getCnx();
        if (conn == null) {
            System.err.println("Failed to connect to database!");
            return;
        }

        try (Statement stmt = conn.createStatement()) {
            System.out.println("Starting aggressive schema fix...");

            // 1. Fix APPLICATION Table
            String[] commonAppIndices = { "freelance_id", "freelancer_id", "unique_freelancer",
                    "uk_freelancer_project" };
            for (String indexName : commonAppIndices) {
                try {
                    stmt.execute("DROP INDEX " + indexName + " ON application");
                    System.out.println("Dropped index on application: " + indexName);
                } catch (SQLException e) {
                    /* ignore */ }
            }
            try {
                stmt.execute("CREATE UNIQUE INDEX uk_freelancer_project ON application (freelancer_id, project_id)");
                System.out.println("Added composite UNIQUE (freelancer_id, project_id) to application.");
            } catch (SQLException e) {
                System.err.println("Note: Could not add uk_freelancer_project (maybe valid duplicates exist).");
            }

            // 2. Fix EVALUATION Table
            // Removing ALL unique constraints that might block multiple reviews.
            String[] evalIndices = {
                    "evaluator_id", "evaluated_id", "project_id",
                    "uk_evaluator_evaluated", "uk_project", "uk_evaluator_project",
                    "unique_eval", "evaluation_unique", "eval_uk"
            };
            for (String indexName : evalIndices) {
                try {
                    stmt.execute("DROP INDEX " + indexName + " ON evaluation");
                    System.out.println("Dropped restrictive index on evaluation: " + indexName);
                } catch (SQLException e) {
                    /* ignore */ }
            }

            System.out.println("Aggressive schema fix completed.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
