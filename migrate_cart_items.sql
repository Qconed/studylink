-- Migration script to update cart_items table to support both resources and sessions

-- Step 1: Add new columns
ALTER TABLE cart_items 
ADD COLUMN IF NOT EXISTS session_id INTEGER,
ADD COLUMN IF NOT EXISTS item_type VARCHAR(20);

-- Step 2: Set item_type for existing rows (all are resources)
UPDATE cart_items 
SET item_type = 'RESOURCE' 
WHERE item_type IS NULL;

-- Step 3: Make item_type NOT NULL
ALTER TABLE cart_items 
ALTER COLUMN item_type SET NOT NULL;

-- Step 4: Make resource_id nullable (for sessions)
ALTER TABLE cart_items 
ALTER COLUMN resource_id DROP NOT NULL;

-- Step 5: Add foreign key constraint for session_id
ALTER TABLE cart_items
ADD CONSTRAINT fk_cart_session 
FOREIGN KEY (session_id) REFERENCES tutor_sessions(id) ON DELETE CASCADE;

-- Step 6: Add check constraint for item_type values
ALTER TABLE cart_items
ADD CONSTRAINT check_item_type CHECK (item_type IN ('RESOURCE', 'SESSION'));

-- Step 7: Add check constraint to ensure proper item configuration
ALTER TABLE cart_items
ADD CONSTRAINT check_item CHECK (
    (item_type = 'RESOURCE' AND resource_id IS NOT NULL AND session_id IS NULL) OR
    (item_type = 'SESSION' AND session_id IS NOT NULL AND resource_id IS NULL)
);

-- Step 8: Drop old unique constraint if it exists
ALTER TABLE cart_items DROP CONSTRAINT IF EXISTS unique_user_resource;

-- Step 9: Add new unique constraint
ALTER TABLE cart_items
ADD CONSTRAINT unique_user_item UNIQUE (user_id, resource_id, session_id);

-- Step 10: Create index for session_id
CREATE INDEX IF NOT EXISTS idx_cart_session ON cart_items(session_id);

-- Step 11: Create index for item_type
CREATE INDEX IF NOT EXISTS idx_cart_item_type ON cart_items(item_type);

-- Verification
SELECT 'Migration completed successfully!' as status;
