package geyer.sensorlab.psychapp;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

public class recordingData extends NotificationListenerService {

    private static final String TAG = "recordingData";

    Handler handler;

    BroadcastReceiver broadcastReceiver;
    BroadcastReceiver appBroadcastReceiver;

    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    String currentlyRunningApp;
    String runningApp;

    @SuppressLint("OverrideAbstract")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startForegroundOperations();
        initializingSharedPreferences();
        detectingDeterminateOfServiceCall(intent.getExtras());
        initializeSQLCipherLibrary();
        initializeHandlerRelatedComponents();
        registerBroadcastReceivers();

        return START_STICKY;
    }


    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.i(TAG, "got notification");
        documentSecurely("note added: " +sbn.getPackageName());
        super.onNotificationRemoved(sbn);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(TAG, "removed notification");
        documentSecurely("note removed: " + sbn.getPackageName());
        super.onNotificationRemoved(sbn);
    }

    //This method across SDK versions calls for a foreground service

    private void startForegroundOperations() {
        if (Build.VERSION.SDK_INT >= 26) {
            if (Build.VERSION.SDK_INT > 26) {
                String CHANNEL_ONE_ID = "sensor.example. geyerk1.inspect.screenservice";
                String CHANNEL_ONE_NAME = "Screen service";
                NotificationChannel notificationChannel = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                            CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_MIN);
                    notificationChannel.setShowBadge(true);
                    notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                    NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    manager.createNotificationChannel(notificationChannel);
                }

                Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_recording);
                Notification notification = new Notification.Builder(getApplicationContext())
                        .setChannelId(CHANNEL_ONE_ID)
                        .setContentTitle("Recording data")
                        .setContentText("PsychApps is logging data")
                        .setSmallIcon(R.drawable.ic_recording)
                        .setLargeIcon(icon)
                        .build();

                Intent notificationIntent = new Intent(getApplicationContext(),MainActivity.class);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                notification.contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);

                startForeground(101, notification);
            } else {
                startForeground(101, updateNotification());
            }
        } else {
            Intent notificationIntent = new Intent(this, MainActivity.class);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);

            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("Recording data")
                    .setContentText("PsychApp is logging data")
                    .setContentIntent(pendingIntent).build();

            startForeground(101, notification);
        }
    }

    private Notification updateNotification() {

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        return new NotificationCompat.Builder(this)
                .setContentTitle("Activity log")
                .setTicker("Ticker")
                .setContentText("data recording a is on going")
                .setSmallIcon(R.drawable.ic_recording)
                .setContentIntent(pendingIntent)
                .setOngoing(true).build();
    }


    private void detectingDeterminateOfServiceCall(Bundle b) {
        if(b != null){
            Log.i("screenService", "bundle not null");
            if(b.getBoolean("phone restarted")){
                documentInsecurely("Phone restarted");
            }
        }else{
            Log.i("screenService", " bundle equals null");
        }
        documentServiceStart();
    }

    //important for giving reminders later
    private void documentServiceStart() {
        if(!prefs.getBoolean("started recording", false)){
            Log.i("screenService", "started running");
            editor.putBoolean("started recording", true);
            editor.apply();

            documentSecurely("Documenting start of recording");
        }
    }


    private void initializeSQLCipherLibrary() {
        SQLiteDatabase.loadLibs(this);
    }

    private void initializeHandlerRelatedComponents() {
        handler = new Handler();
    }

    /**
     * Runnables
     */

    final Runnable printForegroundTask = new Runnable() {
        @Override
        public void run() {
            documentForegroundTask();
        }
    };

    private  void documentForegroundTask() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String currentApp = "NULL";

            @SuppressLint("WrongConstant") UsageStatsManager usm = (UsageStatsManager) this.getSystemService("usagestats");
            long time = System.currentTimeMillis();
            List<UsageStats> appList;

            try {

                appList = Objects.requireNonNull(usm).queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);


                if (appList != null && appList.size() > 0) {
                    SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
                    for (UsageStats usageStats : appList) {
                        mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                    }
                    if (!mySortedMap.isEmpty()) {
                        currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                        PackageManager packageManager = getApplicationContext().getPackageManager();
                        try {
                            currentApp = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(currentApp, PackageManager.GET_META_DATA));
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                            sendToMain("Error: name not found");
                        }
                    }
                }
                handler.postDelayed(printForegroundTask, Constants.FREQUENCY_TO_IDENTIFY_FOREGROUND_APP_MILLISECONDS);
                if (Objects.equals(currentApp, "Inspect")) {
                    sendToMain("Data collection ongoing");
                }
                if (!Objects.equals(currentApp, currentlyRunningApp)) {
                    documentSecurely("App: " + currentApp);
                    currentlyRunningApp = currentApp;
                }
            } catch (Error error) {
                sendToMain("Error in collectingData service: " + error);
                Log.e(TAG, "Error in printingForegroundService: " + error);
                handler.postDelayed(printForegroundTask, 60000);
            }
        }else{
            ActivityManager am = (ActivityManager)this.getSystemService(getApplicationContext().ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
            String currentApp = tasks.get(0).processName;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if(!Objects.equals(runningApp, currentApp)) {
                    runningApp = currentApp;
                    documentSecurely(currentApp);
                    sendToMain("Data collection ongoing");
                }
            }
            else{
                Character firstLetterNew = runningApp.charAt(0);
                Character firstLetterOld = currentApp.charAt(0);
                int newC = (int) firstLetterNew;
                int oldC = (int) firstLetterOld;

                Character lastLetterNew = runningApp.charAt(runningApp.length()-1);
                Character lastLetterOld = currentApp.charAt(currentApp.length()-1);
                int newC1 = (int) lastLetterNew;
                int oldC1 = (int) lastLetterOld;

                if (newC != oldC && newC1 != oldC1) {
                    runningApp = currentApp;
                    documentSecurely(currentApp);
                    sendToMain("Data collection ongoing");
                }
            }
            handler.postDelayed(printForegroundTask, Constants.FREQUENCY_TO_IDENTIFY_FOREGROUND_APP_MILLISECONDS);
        }
    }

    /**
     * BroadcastReceiver related methods
     */

    private void registerBroadcastReceivers() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() != null) {
                    switch (intent.getAction()){
                        case Intent.ACTION_SCREEN_ON:
                            documentSecurely("Screen on");
                            handler.postDelayed(printForegroundTask, 500);
                            break;
                        case Intent.ACTION_SCREEN_OFF:
                            documentSecurely("Screen off");
                            handler.removeCallbacks(printForegroundTask);
                            break;
                        case Intent.ACTION_USER_PRESENT:
                            documentSecurely("Password entered");
                            break;
                        case Intent.ACTION_SHUTDOWN:
                            documentSecurely("Shutdown called");
                            break;
                    }
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        intentFilter.addAction(Intent.ACTION_SHUTDOWN);

        registerReceiver(broadcastReceiver, intentFilter);

        appBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction()!= null){
                    switch (intent.getAction()) {
                        case Intent.ACTION_PACKAGE_ADDED:
                            Collection<String> entry = getNewApp();
                            documentSecurely("App added: " + entry);
                            updatePrefs();
                            break;
                        case Intent.ACTION_PACKAGE_REMOVED:
                            Collection<String> entry1 = getOldApp();
                            documentSecurely("App deleted: " + entry1);
                            updatePrefs();
                            break;
                    }
                }

            }
        };

        IntentFilter appIntentFilter = new IntentFilter();
        appIntentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        appIntentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        appIntentFilter.addDataScheme("package");

        registerReceiver(appBroadcastReceiver, appIntentFilter);
    }

    private void updatePrefs() {

        SharedPreferences preferences = getSharedPreferences("Apps", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN,null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List pkgAppList = getPackageManager().queryIntentActivities(mainIntent,0);

        int appNum = 0;
        for (Object object: pkgAppList){
            ResolveInfo info = (ResolveInfo) object;
            appNum++;
            editor.putString("App number " + appNum, ""+ getBaseContext().getPackageManager().getApplicationLabel(info.activityInfo.applicationInfo));
        }
        editor.putInt("Number of apps", appNum);
        editor.apply();
    }

    private Collection<String> getNewApp() {
        SharedPreferences preferences = getSharedPreferences("Apps", Context.MODE_PRIVATE);
        int numOfApps = preferences.getInt("Number of apps", 0);
        Collection<String> appNames = new ArrayList<>();
        for (int i = 0; i < numOfApps; i++) {
            appNames.add(preferences.getString("App number " + (i + 1), "false"));
        }

        Collection<String> newApps = new ArrayList<>();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN,null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List pkgAppList = getPackageManager().queryIntentActivities(mainIntent,0);

        for (Object object: pkgAppList){
            ResolveInfo info = (ResolveInfo) object;
            newApps.add(""+getBaseContext().getPackageManager().getApplicationLabel(info.activityInfo.applicationInfo));
        }

        newApps.removeAll(appNames);
        return newApps;
    }


    private Collection<String> getOldApp() {
        SharedPreferences preferences = getSharedPreferences("Apps", Context.MODE_PRIVATE);
        int numOfApps = preferences.getInt("Number of apps", 0);
        Collection<String> appNames = new ArrayList<>();
        for (int i = 0; i < numOfApps; i++) {
            appNames.add(preferences.getString("App number " + (i + 1), "false"));
        }

        Collection<String> newApps = new ArrayList<>();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN,null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List pkgAppList = getPackageManager().queryIntentActivities(mainIntent,0);

        for (Object object: pkgAppList){
            ResolveInfo info = (ResolveInfo) object;
            newApps.add(""+getBaseContext().getPackageManager().getApplicationLabel(info.activityInfo.applicationInfo));
        }
        appNames.removeAll(newApps);
        return appNames;
    }

    private void initializingSharedPreferences() {
        prefs = getSharedPreferences("general prefs", MODE_PRIVATE);
        editor = prefs.edit();
        editor.apply();
    }

    private void documentInsecurely(String toDocument) {
        String messageToRelay = toDocument.replace(" ", "-");
        insecureDatabase iDB= new insecureDatabase(this);
        iDB.open();
        iDB.addEntry(messageToRelay, System.currentTimeMillis());
        iDB.close();
    }


    //stores the data into the internal database
    private void documentSecurely(String event) {

        SQLiteDatabase db = secureDatabaseDbHelper.getInstance(this).getWritableDatabase(prefs.getString("password", "not to be used"));

        final long time = System.currentTimeMillis();

        ContentValues values = new ContentValues();
        values.put(secureDatabaseContract.secureDatabase.EVENT, event);
        values.put(secureDatabaseContract.secureDatabase.TIMESTAMP, time);

        db.insert(secureDatabaseContract.secureDatabase.TABLE_NAME, null, values);

        Cursor cursor = db.rawQuery("SELECT * FROM '" + secureDatabaseContract.secureDatabase.TABLE_NAME + "';", null);
        Log.d(TAG, "Update: " + event + " " + time);
        cursor.close();
        db.close();
    }

    private void sendToMain(String message){
            Intent intent = new Intent("changeInService");
            intent.putExtra("Status", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            Log.i(TAG, "data sent to main");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(appBroadcastReceiver);
    }
}
