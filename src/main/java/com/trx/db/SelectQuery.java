package com.trx.db;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.ibatis.sqlmap.client.SqlMapClient;

public class SelectQuery {

	@Autowired
	SqlMapClient sqlMapClient;

	public void select() throws SQLException {

		System.out.println("111");

		StringBuffer buffer = new StringBuffer();

		List result = sqlMapClient.queryForList("TM_TRACE.selectTrace");

		System.out.println(result);


	}
}
