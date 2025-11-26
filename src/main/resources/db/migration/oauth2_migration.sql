-- SQL script to update the users table for OAuth2 support
-- Run this script on your brandsnap database

-- Make password column nullable
ALTER TABLE users MODIFY COLUMN password VARCHAR(255) NULL;

-- Add provider column if it doesn't exist
ALTER TABLE users ADD COLUMN IF NOT EXISTS provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL';

-- Add provider_id column if it doesn't exist
ALTER TABLE users ADD COLUMN IF NOT EXISTS provider_id VARCHAR(255) NULL;

-- Update existing users to have LOCAL provider
UPDATE users SET provider = 'LOCAL' WHERE provider IS NULL OR provider = '';
