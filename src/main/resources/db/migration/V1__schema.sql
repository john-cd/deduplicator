DROP TABLE IF EXISTS hashes;

CREATE TABLE hashes(
    id IDENTITY,
    filepath VARCHAR NOT NULL,
	ts TIMESTAMP NOT NULL,
    hash VARCHAR NOT NULL
);

ALTER TABLE hashes ADD CONSTRAINT hashes_id PRIMARY KEY(id);

CREATE OR REPLACE VIEW hash_with_duplicates AS SELECT hash, COUNT(DISTINCT id) AS file_count FROM hashes GROUP BY hash HAVING COUNT(DISTINCT id) >= 2;

CREATE OR REPLACE VIEW duplicates AS SELECT h.id, h.filepath, h.hash, hwd.file_count FROM hash_with_duplicates AS hwd INNER JOIN hashes AS h ON hwd.hash = hashes.hash ORDER BY hash, filepath;
