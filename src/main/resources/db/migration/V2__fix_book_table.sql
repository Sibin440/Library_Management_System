-- V2: fix book table - remove extra columns that don't match entity
ALTER TABLE book DROP COLUMN IF EXISTS published_date;
ALTER TABLE book DROP COLUMN IF EXISTS available;
DROP INDEX IF EXISTS ux_book_isbn;
