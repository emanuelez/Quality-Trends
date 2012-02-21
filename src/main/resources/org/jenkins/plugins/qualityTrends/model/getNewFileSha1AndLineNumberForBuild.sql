SELECT DISTINCT file_sha1, line_number
FROM entries e1
WHERE
   build_number = ?
   AND NOT EXISTS (
      SELECT DISTINCT file_sha1, line_number
      FROM entries e2
         WHERE build_number < ?
         AND e1.file_sha1 = e2.file_sha1
         AND e1.line_number = e2.line_number
   )