SELECT
   COUNT(1) as total,
   COUNT(CASEWHEN(file_sha1 IS NULL OR warning_sha1 IS NULL, 1, 0)) as orphans
FROM entries
WHERE build_number = ?;