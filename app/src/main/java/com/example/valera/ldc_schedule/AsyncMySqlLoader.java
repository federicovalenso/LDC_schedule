package com.example.valera.ldc_schedule;

import android.os.AsyncTask;
import android.util.Log;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_DOC_POST;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_DOC_SNP;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_SCHED_MON;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_SCHED_TUE;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_SCHED_WED;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_SCHED_THU;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_SCHED_FRI;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_SCHED_SAT;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_SCHED_MON_END;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_SCHED_TUE_END;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_SCHED_WED_END;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_SCHED_THU_END;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_SCHED_FRI_END;

/**
 * AsyncTask.
 * Загружает данные через MySqlConnector с MySql-сервера
 */

class AsyncMySqlLoader extends AsyncTask<String, Integer, ArrayList<HashMap<String, String>>> {

    private ScheduleFullscreenActivity Activity;

    AsyncMySqlLoader(ScheduleFullscreenActivity _Activity) {
        Activity = _Activity;
    }

    @Override
    protected ArrayList doInBackground(String... params) {
        final String LOG_CONN = "AsynkTask mySqlLoader";
        final int PARAM_SERVER_NAME = 0;
        final int PARAM_BASE_NAME = 1;
        final int PARAM_USER_NAME = 2;
        final int PARAM_PASS = 3;
        ArrayList<HashMap<String, String>> data = new ArrayList<>();
        try {
            MySqlConnector msc = new MySqlConnector(
                    "jdbc:mysql://" + params[PARAM_SERVER_NAME] + "/"
                    + params[PARAM_BASE_NAME] + "?user="
                    + params[PARAM_USER_NAME] +"&password="
                    + params[PARAM_PASS]);
            ResultSet rs = msc.execSqlStatement(
                    "SELECT " +
                            "d.*," +
                            "CONCAT_WS(' ', d.surname, d.name, d.patronymic) as snp, " +
                            "s.*," +
                            "posts.name as post " +
                    "FROM docs d " +
                    "LEFT JOIN posts ON posts.id=d.post_id " +
                    "JOIN sched s ON s.doc_id=d.doc_id LIMIT 9");
            while (rs.next()){
                HashMap<String, String> map = new HashMap<>();
                map.put(ATTR_DOC_SNP, rs.getString(ATTR_DOC_SNP));
                map.put(ATTR_DOC_POST, rs.getString(ATTR_DOC_POST));
                map.put(ATTR_SCHED_MON, rs.getString(ATTR_SCHED_MON));
                map.put(ATTR_SCHED_MON_END, rs.getString(ATTR_SCHED_MON_END));
                map.put(ATTR_SCHED_TUE, rs.getString(ATTR_SCHED_TUE));
                map.put(ATTR_SCHED_TUE_END, rs.getString(ATTR_SCHED_TUE_END));
                map.put(ATTR_SCHED_WED, rs.getString(ATTR_SCHED_WED));
                map.put(ATTR_SCHED_WED_END, rs.getString(ATTR_SCHED_WED_END));
                map.put(ATTR_SCHED_THU, rs.getString(ATTR_SCHED_THU));
                map.put(ATTR_SCHED_THU_END, rs.getString(ATTR_SCHED_THU_END));
                map.put(ATTR_SCHED_FRI, rs.getString(ATTR_SCHED_FRI));
                map.put(ATTR_SCHED_FRI_END, rs.getString(ATTR_SCHED_FRI_END));
                map.put(ATTR_SCHED_SAT, rs.getString(ATTR_SCHED_SAT));
                data.add(map);
            }
        } catch (Exception e) {
            Log.e(LOG_CONN, "doInBackground: ", e);
        }
        return data;
    }

    @Override
    protected void onPostExecute(ArrayList<HashMap<String, String>> data) {
        super.onPostExecute(data);
        Activity.setSchedData(data);
    }
}
