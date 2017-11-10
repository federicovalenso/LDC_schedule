package com.example.valera.ldc_schedule;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * AsyncTask.
 * Загружает данные через MySqlConnector с MySql-сервера
 */

class AsyncMySqlLoader extends AsyncTask<String, Integer, Schedule> {

    private ScheduleFullscreenActivity Activity;

    AsyncMySqlLoader(ScheduleFullscreenActivity _Activity) {
        Activity = _Activity;
    }

    @Override
    protected Schedule doInBackground(String... params) {
        final String LOG_CONN = "AsynkTask mySqlLoader";
        Schedule data = null;
        try {
            MySqlConnector msc = getConnector();
            if (msc != null) {
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
                int evenRowColor = ContextCompat.getColor(Activity.getApplicationContext(), R.color.sched_row_even_color);
                int oddRowColor = ContextCompat.getColor(Activity.getApplicationContext(), R.color.sched_row_odd_color);
                HashMap<Schedule.rowPosition, Integer> colors = new HashMap<>();
                colors.put(Schedule.rowPosition.EVEN, evenRowColor);
                colors.put(Schedule.rowPosition.ODD, oddRowColor);
                data = new Schedule(rs, colors);
            }
        } catch (Exception e) {
            Log.e(LOG_CONN, "doInBackground: ", e);
        }
        return data;
    }

    @Override
    protected void onPostExecute(Schedule data) {
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