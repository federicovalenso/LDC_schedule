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

public class AsyncMySqlLoader extends AsyncTask<Void, Void, ArrayList<Map<String, String>>> {

    final private String LOG_CONN = "AsynkTask mySqlLoader";
    final private String ATTR_DOC_NAME      = "name";
    final private String ATTR_DOC_SURNAME   = "surname";
    final private String ATTR_DOC_PATR      = "patronymic";
    final private String ATTR_SCHED_MON      = "mon";
    final private String ATTR_SCHED_TUE      = "tue";
    final private String ATTR_SCHED_WED      = "wed";
    final private String ATTR_SCHED_THU      = "thu";
    final private String ATTR_SCHED_FRI      = "fri";
    final private String ATTR_SCHED_SAT      = "sat";
    final private String SERVER_ADDR        = "37.140.192.64";
    final private String BASE_NAME          = "u0178389_u10393";
    final private String USER_NAME          = "u0178389_u10393";
    final private String PASS               = "adm2916";

    @Override
    protected ArrayList doInBackground(Void... params) {

        ArrayList<Map<String, String>> data = new ArrayList<>();
        try {
            //jdbc:mysql://37.140.192.64/u0178389_u10393?user=u0178389_u10393&password=adm2916
            MySqlConnector msc = new MySqlConnector("jdbc:mysql://" + SERVER_ADDR + "/"
                                                                    + BASE_NAME + "?user="
                                                                    + USER_NAME +"&password="
                                                                    + PASS);


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
                map.put(ATTR_DOC_NAME, rs.getString(ATTR_DOC_NAME));
                map.put(ATTR_DOC_SURNAME, rs.getString(ATTR_DOC_SURNAME));
                map.put(ATTR_DOC_PATR, rs.getString(ATTR_DOC_PATR));
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
        } finally {
            return data;
        }

    }
}
