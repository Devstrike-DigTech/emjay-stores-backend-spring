-- V8__add_profile_image_to_staff_profiles.sql
-- Add profile image support to staff profiles

ALTER TABLE staff_profiles
ADD COLUMN profile_image_url VARCHAR(500);

-- Add index for faster queries
CREATE INDEX idx_staff_profile_image ON staff_profiles(profile_image_url);

-- Comment
COMMENT ON COLUMN staff_profiles.profile_image_url IS 'Path to staff member profile photo';