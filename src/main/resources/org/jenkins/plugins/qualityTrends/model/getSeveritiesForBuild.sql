SELECT 
   SUM(CASEWHEN(SEVERITY = 'INFO', 1, 0)) AS info, 
   SUM(CASEWHEN(SEVERITY = 'WARNING', 1, 0)) as warnings, 
   SUM(CASEWHEN(SEVERITY = 'ERROR', 1, 0)) as errors 
FROM ENTRIES
WHERE build_number = ?;