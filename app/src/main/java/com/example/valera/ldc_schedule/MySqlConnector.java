package com.example.valera.ldc_schedule;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Установка подключения к MySql-серверу и исполнение запросов
 */

final class MySqlConnector {
    private Connection conn;
    final private String LOG_CONN = "mySql connector log";

    MySqlConnector(final String serverName,
                   final String baseName,
                   final String userName,
                   final String password)
            throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        this("jdbc:mysql://" + serverName + "/"
                    + baseName + "?user="
                    + userName +"&password="
                    + password);
    }

    MySqlConnector(final String inConnStr) throws ClassNotFoundException, SQLException, IllegalAccessException, InstantiationException {
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