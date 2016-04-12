ALTER TABLE sub_commands ALTER COLUMN default_responses SET DEFAULT '{}';

UPDATE sub_commands
  SET default_responses = '{}'
WHERE default_responses IS NULL;
