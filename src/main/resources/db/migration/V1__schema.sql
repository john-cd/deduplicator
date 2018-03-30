DROP TABLE IF EXISTS hashes;

CREATE TABLE hashes(
    id IDENTITY PRIMARY KEY,
    ts TIMESTAMP NOT NULL,
    filepath VARCHAR NOT NULL,
    hash BINARY NOT NULL
);

CREATE HASH INDEX IX_hash ON hashes(hash);

CREATE INDEX IX_filepath ON hashes(filepath);
