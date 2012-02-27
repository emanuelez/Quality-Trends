SELECT COUNT(1)
FROM entries
WHERE build_number = ?
  AND (
      file_sha1 IS NULL
      OR warning_sha1 IS NULL
      );