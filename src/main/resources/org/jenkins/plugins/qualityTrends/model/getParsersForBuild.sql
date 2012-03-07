SELECT parser, COUNT(1) AS amount
FROM ENTRIES
WHERE build_number = ?
GROUP BY PARSER;