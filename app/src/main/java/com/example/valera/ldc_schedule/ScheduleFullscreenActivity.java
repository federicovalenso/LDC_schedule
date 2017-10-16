package com.example.valera.ldc_schedule;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_DOC_SNP;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_DOC_POST;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_SCHED_MON;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_SCHED_THU;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_SCHED_TUE;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_SCHED_WED;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_SCHED_FRI;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_SCHED_MON_END;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_SCHED_TUE_END;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_SCHED_WED_END;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_SCHED_THU_END;
import static com.example.valera.ldc_schedule.MySqlConnector.ATTR_SCHED_FRI_END;

public class ScheduleFullscreenActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */

    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private ListView mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    private final View.OnTouchListener mRefreshDataListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            refreshData();
            return false;
        }
    };

    final private String LOG_SCHED_ACT      = "Schedule activity";
    final static String SERVER_ADDR         = "37.140.192.64";
    final static String BASE_NAME           = "u0178389_u10393";
    final static String USER_NAME           = "u0178389_u10393";
    final static String PASS                = "adm2916";
    private TextView tvDateTime;
    private ArrayList<HashMap<String, String>> alSchedule;
    private schedAdapter lvAdapter;
    Timer timerDataRefresher;
    Timer timerDateTimeRefresher;
    TimerTask ttDataRefresher;
    TimerTask ttDateTimeRefresher;
    final private long lDataRefresherTimerInterval = 60*1000;
    final private long lDateTimeRefresherTimerInterval = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_fullscreen);
        tvDateTime = (TextView) findViewById(R.id.tvDateTime);
        timerDateTimeRefresher = new Timer();
        refreshDateTimeByTimer();
        mVisible = true;
        mContentView = (ListView) findViewById(R.id.fullscreen_content);
        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                toggle();
            }
        });
        String[] from = {
                ATTR_DOC_SNP,
                ATTR_DOC_POST,
                ATTR_SCHED_MON,
                ATTR_SCHED_MON_END,
                ATTR_SCHED_TUE,
                ATTR_SCHED_TUE_END,
                ATTR_SCHED_WED,
                ATTR_SCHED_WED_END,
                ATTR_SCHED_THU,
                ATTR_SCHED_THU_END,
                ATTR_SCHED_FRI,
                ATTR_SCHED_FRI_END};
        int[] to = {
                R.id.tvDoc,
                R.id.tvPost,
                R.id.tvMon,
                R.id.tvMonEnd,
                R.id.tvTue,
                R.id.tvTueEnd,
                R.id.tvWed,
                R.id.tvWedEnd,
                R.id.tvThu,
                R.id.tvThuEnd,
                R.id.tvFri,
                R.id.tvFriEnd};
        int firstRowColor = ContextCompat.getColor(this, R.color.sched_row_first_color);
        int secondRowColor = ContextCompat.getColor(this, R.color.sched_row_second_color);
        int[] colors = {
                firstRowColor,
                secondRowColor,
                firstRowColor,
                secondRowColor,
                firstRowColor,
                secondRowColor,
                firstRowColor,
                secondRowColor,
                firstRowColor};
        alSchedule = new ArrayList<>();
        timerDataRefresher = new Timer();
        refreshDataByTimer();
        lvAdapter = new schedAdapter(this, alSchedule, R.layout.doc_row, from, to, colors);
        mContentView.setAdapter(lvAdapter);
    }

    void refreshDateTimeByTimer() {
        if (ttDateTimeRefresher != null) {
            ttDateTimeRefresher.cancel();
        }
        ttDateTimeRefresher = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvDateTime.setText(DateFormat.getDateTimeInstance().format(new Date()));
                    }
                });
            }
        };
        timerDateTimeRefresher.schedule(ttDateTimeRefresher, 0, lDateTimeRefresherTimerInterval);
    }

    void refreshDataByTimer() {
        if (ttDataRefresher != null) {
            ttDataRefresher.cancel();
        }
        if (lDataRefresherTimerInterval > 0) {
            ttDataRefresher = new TimerTask() {
                @Override
                public void run() {
                    Log.d(LOG_SCHED_ACT, "Don't sleep");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshData();
                        }
                    });
                }
            };
            timerDataRefresher.schedule(ttDataRefresher, 0, lDataRefresherTimerInterval);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.schedule_act__menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.mi_refresh:
                refreshData();
                return true;
        }
        return false;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    public void setSchedData(ArrayList<HashMap<String, String>> inAlData){
        alSchedule.clear();
        for (HashMap<String, String> item : inAlData){
            alSchedule.add(item);
        }
        lvAdapter.notifyDataSetChanged();
    }

    private void refreshData(){

        if (checkNetworkConnection()) {
            AsyncMySqlLoader asyncMSL = new AsyncMySqlLoader(this);
            asyncMSL.execute(SERVER_ADDR, BASE_NAME, USER_NAME, PASS);
        }
        else {
            Toast.makeText(getApplicationContext(),"Check your internet connection",Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Check whether the device is connected
     */
    private boolean checkNetworkConnection() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        return (activeInfo != null && activeInfo.isConnected());
    }

    protected void onDestroy() {
        super.onDestroy();
    }

}
