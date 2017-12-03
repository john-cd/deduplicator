DROP TABLE IF EXISTS hashes;

CREATE TABLE hashes(
    id IDENTITY PRIMARY KEY,
    filepath VARCHAR NOT NULL,
	ts TIMESTAMP NOT NULL,
    hash VARCHAR NOT NULL
);

--ALTER TABLE hashes ADD CONSTRAINT hashes_id PRIMARY KEY(id);

CREATE OR REPLACE VIEW hash_with_duplicates AS SELECT h.hash, COUNT(DISTINCT h.id) AS file_count FROM hashes AS h GROUP BY h.hash HAVING COUNT(DISTINCT h.id) >= 2;

CREATE OR REPLACE VIEW duplicates AS SELECT h.id, h.filepath, h.hash, hwd.file_count FROM hash_with_duplicates AS hwd INNER JOIN hashes AS h ON hwd.hash = h.hash ORDER BY hwd.hash, h.filepath;
