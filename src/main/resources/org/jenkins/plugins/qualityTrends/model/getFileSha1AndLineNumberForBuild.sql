SELECT file_sha1, line_number
FROM entries
WHERE build_number = ?
  AND NOT file_sha1 IS null;