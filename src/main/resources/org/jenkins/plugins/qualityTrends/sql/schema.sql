DROP TABLE IF EXISTS builds;

CREATE TABLE builds
(
    build_id INT IDENTITY,
    build_number INT,
    commit_sha1 VARCHAR(40)
);

DROP TABLE IF EXISTS entries;

CREATE TABLE entries
(
    entry_id INT IDENTITY,
    build_id INT,
    file_name VARCHAR(4096),
    line_number INT,
    parser VARCHAR(32),
    severity VARCHAR(32),
    issue_id VARCHAR(16),
    message VARCHAR(255),
    link VARCHAR(255),
    file_sha1 VARCHAR(40),
    warning_id INT
);

DROP TABLE IF EXISTS warnings;

CREATE TABLE warnings
(
    warning_id INT IDENTITY,
    warning_sha1 VARCHAR(40)
);