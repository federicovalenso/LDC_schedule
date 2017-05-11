package com.example.valera.ldc_schedule;

import android.os.AsyncTask;
import android.util.Log;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by valera on 05.05.2017.
 */

public class AsyncMySqlLoader extends AsyncTask<String, Void, ArrayList<Map<String, String>>> {

    final private String LOG_CONN = "AsynkTask mySqlLoader";
    final private int PARAM_SERVER_NAME = 0;
    final private int PARAM_BASE_NAME = 1;
    final private int PARAM_USER_NAME = 2;
    final private int PARAM_PASS = 3;

    @Override
    protected ArrayList doInBackground(String... params) {

        ArrayList<Map<String, String>> data = new ArrayList<>();
        try {
            MySqlConnector msc = new MySqlConnector("jdbc:mysql://" + params[PARAM_SERVER_NAME] + "/"
                                                                    + params[PARAM_BASE_NAME] + "?user="
                                                                    + params[PARAM_USER_NAME] +"&password="
                                                                    + params[PARAM_PASS]);


            ResultSet rs = msc.execSqlStatement("SELECT d.doc_id, " +
                                                        "d.name as "+ MySqlConnector.ATTR_DOC_NAME +", " +
                                                        "d.surname as " + MySqlConnector.ATTR_DOC_SURNAME + ", " +
                                                        "d.patronymic as " + MySqlConnector.ATTR_DOC_PATR + ", " +
                                                        "s.doc_id, " +
                                                        "s.mon as " + MySqlConnector.ATTR_SCHED_MON + ", " +
                                                        "s.tue as " + MySqlConnector.ATTR_SCHED_TUE + ", " +
                                                        "s.wed as " + MySqlConnector.ATTR_SCHED_WED + ", " +
                                                        "s.thu as " + MySqlConnector.ATTR_SCHED_THU + ", " +
                                                        "s.fri as " + MySqlConnector.ATTR_SCHED_FRI + ", " +
                                                        "s.sat as " + MySqlConnector.ATTR_SCHED_SAT + " " +
                                                        "from docs as d INNER JOIN sched as s on s.doc_id = d.doc_id");

            while (rs.next()){
                HashMap map = new HashMap<String, String>();
                map.put(MySqlConnector.ATTR_DOC_NAME, rs.getString(MySqlConnector.ATTR_DOC_NAME));
                map.put(MySqlConnector.ATTR_DOC_SURNAME, rs.getString(MySqlConnector.ATTR_DOC_SURNAME));
                map.put(MySqlConnector.ATTR_DOC_PATR, rs.getString(MySqlConnector.ATTR_DOC_PATR));
                map.put(MySqlConnector.ATTR_SCHED_MON, rs.getString(MySqlConnector.ATTR_SCHED_MON));
                map.put(MySqlConnector.ATTR_SCHED_TUE, rs.getString(MySqlConnector.ATTR_SCHED_TUE));
                map.put(MySqlConnector.ATTR_SCHED_WED, rs.getString(MySqlConnector.ATTR_SCHED_WED));
                map.put(MySqlConnector.ATTR_SCHED_THU, rs.getString(MySqlConnector.ATTR_SCHED_THU));
                map.put(MySqlConnector.ATTR_SCHED_FRI, rs.getString(MySqlConnector.ATTR_SCHED_FRI));
                map.put(MySqlConnector.ATTR_SCHED_SAT, rs.getString(MySqlConnector.ATTR_SCHED_SAT));
                data.add(map);
            }
        } catch (Exception e) {
            Log.e(LOG_CONN, "doInBackground: ", e);
        } finally {
            return data;
        }

    }
}
