package com.example.valera.ldc_schedule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by valera on 13.10.2017.
 */

public final class SchedBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(intent.ACTION_BOOT_COMPLETED)) {
            Intent sfaIntent = new Intent(context, ScheduleFullscreenActivity.class);
            sfaIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(sfaIntent);
        }

    }
}
