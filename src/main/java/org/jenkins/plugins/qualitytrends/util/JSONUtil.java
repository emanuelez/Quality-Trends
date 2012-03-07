package org.jenkins.plugins.qualitytrends.util;

import com.google.common.base.Throwables;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class JSONUtil {

    public static final JSONObject ResultSet2JSONObject(ResultSet resultSet) {
        JSONObject element;
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        int totalLength = 0;
        ResultSetMetaData resultSetMetaData;
        String columnName;
        try {
            resultSetMetaData = resultSet.getMetaData();
            while (resultSet.next()) {
                element = new JSONObject();
                for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
                    columnName = resultSetMetaData.getColumnName(i);

                    if (resultSetMetaData.getColumnType(i) == java.sql.Types.ARRAY) {
                        element.accumulate(columnName, resultSet.getArray(columnName));
                    } else if (resultSetMetaData.getColumnType(i) == java.sql.Types.BIGINT) {
                        element.accumulate(columnName, resultSet.getInt(columnName));
                    } else if (resultSetMetaData.getColumnType(i) == java.sql.Types.BOOLEAN) {
                        element.accumulate(columnName, resultSet.getBoolean(columnName));
                    } else if (resultSetMetaData.getColumnType(i) == java.sql.Types.BLOB) {
                        element.accumulate(columnName, resultSet.getBlob(columnName));
                    } else if (resultSetMetaData.getColumnType(i) == java.sql.Types.DOUBLE) {
                        element.accumulate(columnName, resultSet.getDouble(columnName));
                    } else if (resultSetMetaData.getColumnType(i) == java.sql.Types.FLOAT) {
                        element.accumulate(columnName, resultSet.getFloat(columnName));
                    } else if (resultSetMetaData.getColumnType(i) == java.sql.Types.INTEGER) {
                        element.accumulate(columnName, resultSet.getInt(columnName));
                    } else if (resultSetMetaData.getColumnType(i) == java.sql.Types.NVARCHAR) {
                        element.accumulate(columnName, resultSet.getNString(columnName));
                    } else if (resultSetMetaData.getColumnType(i) == java.sql.Types.VARCHAR) {
                        element.accumulate(columnName, resultSet.getString(columnName));
                    } else if (resultSetMetaData.getColumnType(i) == java.sql.Types.TINYINT) {
                        element.accumulate(columnName, resultSet.getInt(columnName));
                    } else if (resultSetMetaData.getColumnType(i) == java.sql.Types.SMALLINT) {
                        element.accumulate(columnName, resultSet.getInt(columnName));
                    } else if (resultSetMetaData.getColumnType(i) == java.sql.Types.DATE) {
                        element.accumulate(columnName, resultSet.getDate(columnName));
                    } else if (resultSetMetaData.getColumnType(i) == java.sql.Types.TIMESTAMP) {
                        element.accumulate(columnName, resultSet.getTimestamp(columnName));
                    } else {
                        element.accumulate(columnName, resultSet.getObject(columnName));
                    }
                }
                jsonArray.add(element);
                totalLength++;
            }
            jsonObject.accumulate("result", "success");
            jsonObject.accumulate("rows", totalLength);
            jsonObject.accumulate("data", jsonArray);
        } catch (SQLException e) {
            jsonObject.accumulate("result", "failure");
            jsonObject.accumulate("error", e.getMessage());
            e.printStackTrace();
            Throwables.propagate(e);
        }
        return jsonObject;
    }

    public static final String ResultSet2JSONString(ResultSet rs) {
        return ResultSet2JSONObject(rs).toString();
    }

}
