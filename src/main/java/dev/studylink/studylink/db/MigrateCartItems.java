package dev.studylink.studylink.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MigrateCartItems {
    
    public static void main(String[] args) {
        System.out.println("Starting cart_items migration...");
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Step 1: Add new columns
                executeUpdate(conn, "ALTER TABLE cart_items ADD COLUMN IF NOT EXISTS session_id INTEGER");
                executeUpdate(conn, "ALTER TABLE cart_items ADD COLUMN IF NOT EXISTS item_type VARCHAR(20)");
                
                // Step 2: Set item_type for existing rows
                executeUpdate(conn, "UPDATE cart_items SET item_type = 'RESOURCE' WHERE item_type IS NULL");
                
                // Step 3: Make item_type NOT NULL
                executeUpdate(conn, "ALTER TABLE cart_items ALTER COLUMN item_type SET NOT NULL");
                
                // Step 4: Make resource_id nullable
                executeUpdate(conn, "ALTER TABLE cart_items ALTER COLUMN resource_id DROP NOT NULL");
                
                // Step 5: Add foreign key constraint for session_id
                executeUpdate(conn, "ALTER TABLE cart_items ADD CONSTRAINT fk_cart_session FOREIGN KEY (session_id) REFERENCES tutor_sessions(id) ON DELETE CASCADE");
                
                // Step 6: Add check constraint for item_type values
                executeUpdate(conn, "ALTER TABLE cart_items ADD CONSTRAINT check_item_type CHECK (item_type IN ('RESOURCE', 'SESSION'))");
                
                // Step 7: Add check constraint to ensure proper item configuration
                executeUpdate(conn, "ALTER TABLE cart_items ADD CONSTRAINT check_item CHECK ((item_type = 'RESOURCE' AND resource_id IS NOT NULL AND session_id IS NULL) OR (item_type = 'SESSION' AND session_id IS NOT NULL AND resource_id IS NULL))");
                
                // Step 8: Drop old unique constraint if it exists
                try {
                    executeUpdate(conn, "ALTER TABLE cart_items DROP CONSTRAINT IF EXISTS unique_user_resource");
                } catch (SQLException e) {
                    System.out.println("Constraint unique_user_resource doesn't exist, skipping...");
                }
                
                // Step 9: Add new unique constraint
                executeUpdate(conn, "ALTER TABLE cart_items ADD CONSTRAINT unique_user_item UNIQUE (user_id, resource_id, session_id)");
                
                // Step 10: Create index for session_id
                executeUpdate(conn, "CREATE INDEX IF NOT EXISTS idx_cart_session ON cart_items(session_id)");
                
                // Step 11: Create index for item_type
                executeUpdate(conn, "CREATE INDEX IF NOT EXISTS idx_cart_item_type ON cart_items(item_type)");
                
                conn.commit();
                System.out.println("✅ Migration completed successfully!");
                System.out.println("Cart now supports both resources and tutoring sessions.");
                
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("❌ Migration failed, rolling back...");
                e.printStackTrace();
                throw e;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed:");
            e.printStackTrace();
        }
    }
    
    private static void executeUpdate(java.sql.Connection conn, String sql) throws SQLException {
        System.out.println("Executing: " + sql.substring(0, Math.min(60, sql.length())) + "...");
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
            System.out.println("  ✓ Success");
        }
    }
}
