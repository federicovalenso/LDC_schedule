package com.example.valera.ldc_schedule;

import android.os.AsyncTask;
import android.util.Log;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_DOC_NAME;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_DOC_PATR;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_DOC_SNP;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_DOC_SURNAME;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_SCHED_FRI;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_SCHED_MON;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_SCHED_SAT;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_SCHED_THU;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_SCHED_TUE;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_SCHED_WED;

/**
 * AsyncTask.
 * Загружает данные через MySqlConnector с MySql-сервера
 */

class AsyncMySqlLoader extends AsyncTask<String, Integer, ArrayList<HashMap<String, String>>> {

    private ScheduleFullscreenActivity Activity;

    public AsyncMySqlLoader(ScheduleFullscreenActivity _Activity) {
        Activity = _Activity;
    }

    @Override
    protected void onPreExecute() {
        Activity.progBar.setProgress(0);
    }

    @Override
    protected ArrayList doInBackground(String... params) {

        final String LOG_CONN = "AsynkTask mySqlLoader";
        final int PARAM_SERVER_NAME = 0;
        final int PARAM_BASE_NAME = 1;
        final int PARAM_USER_NAME = 2;
        final int PARAM_PASS = 3;
        int progress = 20;
        ArrayList<HashMap<String, String>> data = new ArrayList<>();

        try {
            MySqlConnector msc = new MySqlConnector("jdbc:mysql://" + params[PARAM_SERVER_NAME] + "/"
                                                                    + params[PARAM_BASE_NAME] + "?user="
                                                                    + params[PARAM_USER_NAME] +"&password="
                                                                    + params[PARAM_PASS]);
            publishProgress(progress);

            ResultSet rs = msc.execSqlStatement("SELECT d.doc_id, " +
                                                        "d.name as "+ ATTR_DOC_NAME +", " +
                                                        "d.surname as " + ATTR_DOC_SURNAME + ", " +
                                                        "d.patronymic as " + ATTR_DOC_PATR + ", " +
                                                        "s.doc_id, " +
                                                        "s.mon as " + ATTR_SCHED_MON + ", " +
                                                        "s.tue as " + ATTR_SCHED_TUE + ", " +
                                                        "s.wed as " + ATTR_SCHED_WED + ", " +
                                                        "s.thu as " + ATTR_SCHED_THU + ", " +
                                                        "s.fri as " + ATTR_SCHED_FRI + ", " +
                                                        "s.sat as " + ATTR_SCHED_SAT + " " +
                                                        "from docs as d INNER JOIN sched as s on s.doc_id = d.doc_id");

            while (rs.next()){
                HashMap<String, String> map = new HashMap<>();
                String snp = rs.getString(ATTR_DOC_SURNAME) + " " +
                        rs.getString(ATTR_DOC_NAME) + " " +
                        rs.getString(ATTR_DOC_PATR);
                map.put(ATTR_DOC_SNP, snp);
                map.put(ATTR_SCHED_MON, rs.getString(ATTR_SCHED_MON));
                map.put(ATTR_SCHED_TUE, rs.getString(ATTR_SCHED_TUE));
                map.put(ATTR_SCHED_WED, rs.getString(ATTR_SCHED_WED));
                map.put(ATTR_SCHED_THU, rs.getString(ATTR_SCHED_THU));
                map.put(ATTR_SCHED_FRI, rs.getString(ATTR_SCHED_FRI));
                map.put(ATTR_SCHED_SAT, rs.getString(ATTR_SCHED_SAT));
                data.add(map);
                publishProgress(progress + 5);
            }
        } catch (Exception e) {
            Log.e(LOG_CONN, "doInBackground: ", e);
        }

        return data;

    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        Activity.progBar.setProgress(values[0]);
    }

    @Override
    protected void onPostExecute(ArrayList<HashMap<String, String>> map) {
        super.onPostExecute(map);
        Activity.setSchedData(map);
        Activity.progBar.setProgress(100);
    }
}
