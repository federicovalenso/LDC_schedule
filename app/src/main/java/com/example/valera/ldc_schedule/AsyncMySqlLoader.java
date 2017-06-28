package com.example.valera.ldc_schedule;

import android.os.AsyncTask;
import android.util.Log;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_DOC_NAME;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_DOC_SNP;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_DOC_PATR;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_DOC_SURNAME;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_SCHED_FRI;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_SCHED_MON;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_SCHED_THU;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_SCHED_TUE;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_SCHED_WED;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_SCHED_SAT;

/**
 * Created by valera on 05.05.2017.
 */

public class AsyncMySqlLoader extends AsyncTask<String, Void, ArrayList<Map<String, String>>> {

    final private String LOG_CONN = "AsynkTask mySqlLoader";
    final private int PARAM_SERVER_NAME = 0;
    final private int PARAM_BASE_NAME = 1;
    final private int PARAM_USER_NAME = 2;
    final private int PARAM_PASS = 3;
    private ScheduleFullscreenActivity Activity;

    void link(ScheduleFullscreenActivity inActivity){
        Activity = inActivity;
    }

    void unLink(){
        Activity = null;
    }

    @Override
    protected ArrayList doInBackground(String... params) {

        ArrayList<Map<String, String>> data = new ArrayList<>();

        try {
            MySqlConnector msc = new MySqlConnector("jdbc:mysql://" + params[PARAM_SERVER_NAME] + "/"
                                                                    + params[PARAM_BASE_NAME] + "?user="
                                                                    + params[PARAM_USER_NAME] +"&password="
                                                                    + params[PARAM_PASS]);


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
                HashMap map = new HashMap<String, String>();
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
            }
        } catch (Exception e) {
            Log.e(LOG_CONN, "doInBackground: ", e);
        }

        return data;

    }

    @Override
    protected void onPostExecute(ArrayList<Map<String, String>> map) {
        super.onPostExecute(map);
        Activity.setSchedData(map);
    }
}
