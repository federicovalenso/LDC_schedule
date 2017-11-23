package com.example.valera.ldc_schedule;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

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
    private boolean mVisible = true;
    private final Handler mHideHandler = new Handler();
    private ListView mContentView;
    private View mFullscreenView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mVisible = false;
            mFullscreenView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
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
            mVisible = true;
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
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

    final private String LOG_SCHED_ACT = "Schedule activity";
    private ImageView imgSync;
    private SharedPreferences prefs;
    private TextView tvDateTime;
    private Schedule schedule;
    final private int[] viewsToShowSchedule = {
            R.id.tvCab,
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
    private schedAdapter lvAdapter;
    private Timer timerDataRefresher;
    private Timer timerDateTimeRefresher;
    private TimerTask ttDataRefresher;
    private TimerTask ttDateTimeRefresher;
    private long lDataRefresherTimerInterval;
    final private long lDateTimeRefresherTimerInterval = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_fullscreen);
        mFullscreenView = findViewById(R.id.fullscreen_widget);
        tvDateTime = findViewById(R.id.tvDateTime);
        imgSync = findViewById(R.id.imgSync);
        imgSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });
        mContentView = findViewById(R.id.schedule_content);
        mContentView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                toggle();
            }
        });
        schedule = new Schedule();
        lvAdapter = new schedAdapter(this, schedule, R.layout.doc_row, Schedule.columnHeaders, viewsToShowSchedule);
        mContentView.setAdapter(lvAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.setDefaultValues(this, R.xml.pref_data_sync, false);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        refreshDateTimeByTimer();
        refreshDataByTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        timerDateTimeRefresher.cancel();
        timerDataRefresher.cancel();
    }

    void refreshDateTimeByTimer() {
        timerDateTimeRefresher = new Timer();
        if (ttDateTimeRefresher != null) {
            ttDateTimeRefresher.cancel();
        }
        ttDateTimeRefresher = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvDateTime.setText(new SimpleDateFormat("d MMM y EEEE H:mm:ss").format(new Date()));
                    }
                });
            }
        };
        timerDateTimeRefresher.schedule(ttDateTimeRefresher, 0, lDateTimeRefresherTimerInterval);
    }

    void refreshDataByTimer() {
        timerDataRefresher = new Timer();
        lDataRefresherTimerInterval = Integer.parseInt(prefs.getString((String) this.getText(R.string.pref_sync_frequency_key), "")) * 40 * 1000;
        if (ttDataRefresher != null) {
            ttDataRefresher.cancel();
        }
        if (lDataRefresherTimerInterval > 0) {
            ttDataRefresher = new TimerTask() {
                @Override
                public void run() {
                    Log.d(LOG_SCHED_ACT, "Starting refreshing data");
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
        switch (item.getItemId()) {
            case R.id.mi_refresh:
                refreshData();
                break;
            case R.id.mi_options:
                showAppOptions();
        }
        return false;
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        mFullscreenView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, 2*UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    public void setSchedule(Schedule schedule) {
        if (schedule != null) {
            imgSync.setVisibility(View.GONE);
            this.schedule.clear();
            for (Schedule.ScheduleRow row : schedule) {
                this.schedule.add(row);
            }
            lvAdapter.notifyDataSetChanged();
            if (mVisible == true) {
                hide();
            }
        } else {
            Toast.makeText(this.getApplicationContext(),
                    "Данные с сервера не были получены",
                    Toast.LENGTH_LONG).show();
            if (mVisible == false) {
                show();
            }
        }
    }

    private void refreshData() {

        if (checkNetworkConnection()) {
            AsyncMySqlLoader asyncMSL = new AsyncMySqlLoader(this);
            asyncMSL.execute();
        } else {
            Toast.makeText(getApplicationContext(),
                    "Проверьте подключение к сети",
                    Toast.LENGTH_LONG).show();
            if (mVisible == false) {
                show();
            }
        }
    }

    private void showAppOptions() {
        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
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
