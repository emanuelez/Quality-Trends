SELECT COUNT(*) FROM entries
JOIN builds ON entries.build_id = builds.build_id AND builds.build_number = ?;