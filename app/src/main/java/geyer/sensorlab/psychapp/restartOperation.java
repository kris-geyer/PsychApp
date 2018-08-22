package geyer.sensorlab.psychapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

public class restartOperation extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent activityIntent = new Intent(context, recordingData.class);
            Bundle b=new Bundle();
            b.putBoolean("phone restarted", true);
            activityIntent.putExtras(b);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                context.startService(activityIntent);
            }else {
                context.startForegroundService(activityIntent);
            }
        }
    }
}
