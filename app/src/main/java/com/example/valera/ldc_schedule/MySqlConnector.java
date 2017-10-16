package com.example.valera.ldc_schedule;

import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

/**
 * Установка подключения к MySql-серверу и исполнение запросов
 */

final class MySqlConnector {
    private Connection conn;
    final private String LOG_CONN = "mySql connector log";

    final static String ATTR_DOC_SNP        = "snp";
    final static String ATTR_DOC_POST       = "post";
    final static String ATTR_SCHED_MON      = "mon";
    final static String ATTR_SCHED_MON_END  = "mon_end";
    final static String ATTR_SCHED_TUE      = "tue";
    final static String ATTR_SCHED_TUE_END  = "tue_end";
    final static String ATTR_SCHED_WED      = "wed";
    final static String ATTR_SCHED_WED_END  = "wed_end";
    final static String ATTR_SCHED_THU      = "thu";
    final static String ATTR_SCHED_THU_END  = "thu_end";
    final static String ATTR_SCHED_FRI      = "fri";
    final static String ATTR_SCHED_FRI_END  = "fri_end";
    final static String ATTR_SCHED_SAT      = "sat";

    MySqlConnector(String inConnStr) throws ClassNotFoundException, SQLException, IllegalAccessException, InstantiationException{
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        conn = DriverManager.getConnection(inConnStr);
    }

    protected void finalize() {
        try {
            super.finalize();
            conn.close();
        } catch (Throwable e) {
            Log.e(LOG_CONN, "finalize: " + e.getMessage());
        }
    }

    ResultSet execSqlStatement(String query) throws SQLException {
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(query);
    }
}
