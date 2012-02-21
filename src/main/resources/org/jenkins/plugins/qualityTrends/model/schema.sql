DROP TABLE IF EXISTS entries;

CREATE TABLE entries
(
    entry_id INT IDENTITY,
    build_number INT,
    file_name VARCHAR(4096),
    line_number INT,
    parser VARCHAR(32),
    severity VARCHAR(32),
    issue_id VARCHAR(16),
    message VARCHAR(255),
    link VARCHAR(255),
    file_sha1 VARCHAR(40),
    warning_sha1 VARCHAR(40)
);