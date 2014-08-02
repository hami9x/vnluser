package rfx.server.util.sql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;

import rfx.server.configs.SqlDbConfigs;
import rfx.server.util.LogUtil;
import rfx.server.util.json.JSONArray;
import rfx.server.util.json.JSONException;
import rfx.server.util.json.JSONObject;

public abstract class DbGenericDao {

	// protected static ExecutorService dbExecutor =
	// Executors.newSingleThreadExecutor();

	protected volatile int timeToSleep = 0;

	private DataSource defaultDataSource;
	private SqlDbConfigs defautlSqlDbConfigs;

	public SqlDbConfigs getDefaultSqlDbConfigs() {
		if (defautlSqlDbConfigs == null) {
			defautlSqlDbConfigs = SqlDbConfigs.load("defaultConfig");
		}
		return defautlSqlDbConfigs;
	}

	public SqlDbConfigs getSqlDbConfigs(String configName) {
		return SqlDbConfigs.load(configName);
	}

	protected void resetDefaultDataSource() {
		defaultDataSource = null;
		defaultDataSource = getDefaultSqlDbConfigs().getDataSource();
	}

	public DataSource getDefaultDataSource() {
		if (defaultDataSource == null) {
			defaultDataSource = getDefaultSqlDbConfigs().getDataSource();
		}
		return defaultDataSource;
	}

	protected void dbExceptionHandler(Throwable exception) {
		String msg = exception.getMessage();
		LogUtil.i("DbGenericDao", msg + " " + exception.toString());
		exception.printStackTrace();
		resetDefaultDataSource();// force reset datasource ???
	}

	protected JSONArray convertResultSetToJson(ResultSet rs) {
		JSONArray json = new JSONArray();
		try {
			ResultSetMetaData rsmd = rs.getMetaData();
			while (rs.next()) {
				int numColumns = rsmd.getColumnCount();
				JSONObject obj = new JSONObject();

				for (int i = 1; i < numColumns + 1; i++) {
					String column_name = rsmd.getColumnName(i);

					switch (rsmd.getColumnType(i)) {
					case java.sql.Types.ARRAY:
						obj.put(column_name, rs.getArray(i));
						break;
					case java.sql.Types.BIGINT:
						obj.put(column_name, rs.getInt(i));
						break;
					case java.sql.Types.BOOLEAN:
						obj.put(column_name, rs.getBoolean(i));
						break;
					case java.sql.Types.BLOB:
						obj.put(column_name, rs.getBlob(i));
						break;
					case java.sql.Types.DOUBLE:
						obj.put(column_name, rs.getDouble(i));
						break;
					case java.sql.Types.FLOAT:
						obj.put(column_name, rs.getFloat(i));
						break;
					case java.sql.Types.INTEGER:
						obj.put(column_name, rs.getInt(i));
						break;
					case java.sql.Types.NVARCHAR:
						obj.put(column_name, rs.getNString(i));
						break;
					case java.sql.Types.VARCHAR:
						obj.put(column_name, rs.getString(i));
						break;
					case java.sql.Types.TINYINT:
						obj.put(column_name, rs.getInt(i));
						break;
					case java.sql.Types.SMALLINT:
						obj.put(column_name, rs.getInt(i));
						break;
					case java.sql.Types.DATE:
						obj.put(column_name, rs.getDate(i));
						break;
					case java.sql.Types.TIMESTAMP:
						obj.put(column_name, rs.getTimestamp(i));
						break;
					default:
						obj.put(column_name, rs.getObject(i));
						break;
					}
				}
				json.put(obj);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}
}
