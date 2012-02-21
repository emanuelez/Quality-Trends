SELECT *
FROM ENTRIES
WHERE build_number = ?
  AND file_sha1 = ?
  AND line_number = ?;