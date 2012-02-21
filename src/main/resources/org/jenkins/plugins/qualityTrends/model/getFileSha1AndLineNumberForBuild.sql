SELECT file_sha1, line_number
FROM entries
WHERE build_number = ?
  AND file_sha1 != null;