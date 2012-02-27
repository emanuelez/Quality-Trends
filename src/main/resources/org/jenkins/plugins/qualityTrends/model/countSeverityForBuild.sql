SELECT COUNT(DISTINCT warning_sha1)
FROM entries
WHERE build_number = ?
  AND severity = ?
  AND file_sha1 NOT IS null
  AND warning_sha1 NOT IS null;