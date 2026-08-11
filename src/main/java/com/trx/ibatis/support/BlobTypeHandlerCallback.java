package com.trx.ibatis.support;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import com.ibatis.sqlmap.client.extensions.ParameterSetter;
import com.ibatis.sqlmap.client.extensions.ResultGetter;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;

public class BlobTypeHandlerCallback implements TypeHandlerCallback {

	@Override
	public Object getResult(ResultGetter getter) throws SQLException {
		Blob blob = getter.getBlob();
		byte[] returnValue;
		if (!getter.wasNull()) {
			returnValue = blob.getBytes(1, (int) blob.length());
		} else {
			returnValue = null;
		}
		return returnValue;
	}

	@Override
	public void setParameter(ParameterSetter setter, Object parameter) throws SQLException {
		if (null != parameter && !(parameter.equals(""))) {
			byte[] bytes = (byte[]) parameter;
			Blob blob = null;
			PreparedStatement preparedStatement = setter.getPreparedStatement();
			Connection connection = preparedStatement.getConnection();
			blob = connection.createBlob();
			blob.setBytes(1, bytes);
			setter.setBlob(blob);
		} else {
			setter.setNull(Types.BLOB);
		}
	}

	@Override
	public Object valueOf(String s) {
		return s;
	}

}
