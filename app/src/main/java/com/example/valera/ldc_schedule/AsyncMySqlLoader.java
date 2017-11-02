package com.example.valera.ldc_schedule;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_CAB;
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
        ArrayList<HashMap<String, String>> data = null;
        try {
            MySqlConnector msc = getConnector();
            if (msc != null) {
                data = new ArrayList<>();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Activity.getApplicationContext());
                String screen = prefs.getString((String) Activity.getText(R.string.pref_screen_ids_key), "");
                ResultSet rs = msc.execSqlStatement(
                        "SELECT " +
                        "d.*," +
                        "CONCAT_WS(' ', d.surname, d.name, d.patronymic) as snp, " +
                        "s.*," +
                        "posts.name as post " +
                        "FROM docs d " +
                        "LEFT JOIN posts ON posts.id=d.post_id " +
                        "JOIN sched s ON s.doc_id=d.doc_id " +
                        "WHERE fl_display=1 AND " +
                        "screen_id=" + screen + " " +
                        "ORDER BY screen_position ASC " +
                        "LIMIT 9");
                while (rs.next()){
                    HashMap<String, String> map = new HashMap<>();
                    map.put(ATTR_CAB,rs.getString(ATTR_CAB));
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

    private MySqlConnector getConnector() throws InterruptedException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Activity.getApplicationContext());
        String serverAddress = prefs.getString((String) Activity.getText(R.string.pref_server_address_key), "");
        String baseName = prefs.getString((String) Activity.getText(R.string.pref_base_name_key), "");
        String userName = prefs.getString((String) Activity.getText(R.string.pref_user_name_key), "");
        String password = prefs.getString((String) Activity.getText(R.string.pref_password_key), "");
        MySqlConnectionGetter getter = new MySqlConnectionGetter(serverAddress, baseName, userName, password);
        getter.start();
        TimeUnit.SECONDS.sleep(5);
        getter.interrupt();
        return getter.getConnector();
    }

    private class MySqlConnectionGetter extends Thread {

        private MySqlConnector connector;
        String serverAddress;
        String baseName;
        String userName;
        String password;

        MySqlConnectionGetter(final String serverAddress,
                              final String baseName,
                              final String userName,
                              final String password) {
            super();
            connector = null;
            this.serverAddress = serverAddress;
            this.baseName = baseName;
            this.userName = userName;
            this.password = password;
        }

        @Override
        public void run() {
            try {
                connector = new MySqlConnector(serverAddress, baseName, userName, password);
            } catch (Exception e) {
                Toast.makeText(Activity.getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        public MySqlConnector getConnector() {
            return connector;
        }
    }
}