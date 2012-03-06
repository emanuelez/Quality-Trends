DROP TABLE IF EXISTS entries;

CREATE TABLE entries
(
    entry_id INT IDENTITY,
    build_number INT,
    file_name CLOB,
    line_number INT,
    parser VARCHAR(32),
    severity VARCHAR(32),
    issue_id VARCHAR(16),
    message CLOB,
    link CLOB,
    file_sha1 VARCHAR(40),
    warning_sha1 VARCHAR(40)
);