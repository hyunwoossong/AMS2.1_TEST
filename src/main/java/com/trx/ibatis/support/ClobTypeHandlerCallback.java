package com.trx.ibatis.support;



import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;



import com.ibatis.sqlmap.client.extensions.ParameterSetter;
import com.ibatis.sqlmap.client.extensions.ResultGetter;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;

public class ClobTypeHandlerCallback implements TypeHandlerCallback {

	@Override
	public Object getResult(ResultGetter getter) throws SQLException {
		Clob clob = getter.getClob();
		String returnValue;
		if (clob != null) {
			returnValue = clob.getSubString(1, (int) clob.length());
		} else {
			returnValue = null;
		}
		return returnValue;
	}

	@Override
	public void setParameter(ParameterSetter setter, Object parameter) throws SQLException {
		if (null != parameter && !(parameter.equals(""))) {
			String str = (String) parameter;
			Clob clob = null;
			PreparedStatement preparedStatement = setter.getPreparedStatement();
			Connection connection = preparedStatement.getConnection();
			clob = connection.createClob();
			clob.setString(1, str);
			setter.setClob(clob);
		} else {
			setter.setNull(Types.CLOB);
		}
	}

	@Override
	public Object valueOf(String s) {
		return s;
	}

}
