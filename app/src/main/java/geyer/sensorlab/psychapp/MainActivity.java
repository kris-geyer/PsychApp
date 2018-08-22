package geyer.sensorlab.psychapp;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Process;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    static final String TAG = "mainActivity";

    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    whichPermissionsToRequest whichPermissionsToRequest;

    TextView status;

    //handler for detecting if initializing broadcastReceiver is required.
    Handler handler;
    Boolean broadcastReceiverInitializationRequired;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * Initializations
         */
        initializeSharedPreferences();
        initializeOtherClasses();
        initializeVisualComponents();

        /**
         * Identify what permissions are required before calling service
         */

        direct();
    }


    private void initializeSharedPreferences() {
        prefs = getSharedPreferences("general prefs", MODE_PRIVATE);
        editor = prefs.edit();
        editor.apply();
    }

    private void initializeOtherClasses() {
        whichPermissionsToRequest = new whichPermissionsToRequest();
    }

    private void initializeVisualComponents() {
        Button export = findViewById(R.id.btnExport);
        export.setOnClickListener(this);

        Button resetPassword = findViewById(R.id.btnResetPassword);
        resetPassword.setOnClickListener(this);

        status = findViewById(R.id.tvStatus);

        broadcastReceiverInitializationRequired = true;
        Intent intent = new Intent("changeInService");
        intent.putExtra("assessingInitialization",true );
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        handler = new Handler();
        handler.postDelayed(accountForInitialization, 500);
    }

    Runnable accountForInitialization = new Runnable() {
        @Override
        public void run() {
            if(broadcastReceiverInitializationRequired){
                LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(dataCollectionInitiated, new IntentFilter("changeInService"));
            }
        }
    };

    //relays data sent from background operations to the user.
    private BroadcastReceiver dataCollectionInitiated = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getBooleanExtra("assessingInitialization", false)){
                Log.i("Main", "broadcast receiver is already initialized");
                broadcastReceiverInitializationRequired = false;
            }else{
                String toRelay = intent.getStringExtra("Status");
                String toShow ="Status: " + "\n" +
                        toRelay;

                status.setText(toShow);
                status.setGravity(Gravity.CENTER);
            }

        }
    };


    private void direct() {
        ArrayList<Integer> progressCode = assessIfInitializationRequired();
        if(!progressCode.contains(1)) {
            requestPassword();
        }else if(!progressCode.contains(2)) {
            requestEssentialPermissions();
        }else if(!progressCode.contains(3)) {
            requestPackagePermission(null);
        }else if (!progressCode.contains(4)){
            requestNotificationPermission();
        } else if(progressCode.contains(5)) {
            finalCheckToStartService();
        }
    }


    private ArrayList<Integer> assessIfInitializationRequired() {
        ArrayList<Integer> initializationRequired = new ArrayList<>();
        if(!prefs.getString("password", "notAppropriate").equals("notAppropriate")){
            initializationRequired.add(1);
        }
        if(essentialPermissionsGiven()){
            initializationRequired.add(2);
        }
        if(hasSpecialPermission()){
            initializationRequired.add(3);
        }
        if (notificationPermissionRequired()) {
            initializationRequired.add(4);
        }
        if(serviceIsRunning(recordingData.class)){
            initializationRequired.add(5);
        }
        return initializationRequired;
    }

    /*
    private void generateWelcomeMessage() {
        LayoutInflater inflater = this.getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(inflater.inflate(R.layout.intro_message_screen, null))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestPassword();
                    }
                });
        builder.create()
                .show();
    }
    */

    private void requestPassword() {
        LayoutInflater inflater = this.getLayoutInflater();
        //create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("password")
                .setMessage("The file that is generated with your data and later exported must be password protected. Please enter a password that is at least 8 characters in length.")
                .setView(inflater.inflate(R.layout.request_password_screen, null))
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Dialog d = (Dialog) dialogInterface;
                        EditText password = d.findViewById(R.id.etPassword);
                        if(checkPassword(password.getText())){
                            editor.putBoolean("password generated", true);
                            editor.putString("password", String.valueOf(password.getText()));
                            editor.putString("pdfPassword", String.valueOf(password.getText()));
                            editor.apply();
                            documentApps();
                        }else{
                            Toast.makeText(MainActivity.this, "The password entered was not long enough", Toast.LENGTH_SHORT).show();
                        }
                    }
                    private boolean checkPassword(Editable text) {
                        return text.length() > 7;
                    }
                });
        builder.create()
                .show();
    }

    private void resetPassword() {
        LayoutInflater inflater = this.getLayoutInflater();
        //create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("password")
                .setMessage("The file that is generated with your data and later exported must be password protected. Please enter a password that is at least 8 characters in length.")
                .setView(inflater.inflate(R.layout.request_password_screen, null))
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Dialog d = (Dialog) dialogInterface;
                        EditText password = d.findViewById(R.id.etPassword);
                        if(checkPassword(password.getText())){
                            editor.putBoolean("password generated", true);
                            editor.putString("pdfPassword", String.valueOf(password.getText()));
                            editor.apply();
                        }else{
                            Toast.makeText(MainActivity.this, "The password entered was not long enough", Toast.LENGTH_SHORT).show();
                        }
                    }
                    private boolean checkPassword(Editable text) {
                        return text.length() > 7;
                    }
                });
        builder.create()
                .show();
    }

    private boolean essentialPermissionsGiven() {
        ArrayList perms = whichPermissionsToRequest.generatePermissionToRequest(1);
        String[] permsArray = new String[perms.size()];
        permsArray = (String[]) perms.toArray(permsArray);

        Boolean requestPermissionAgain = true;

        for (int i = 0; i < perms.size(); i++){
            if(ContextCompat.checkSelfPermission(this, permsArray[i]) != PackageManager.PERMISSION_GRANTED){
                requestPermissionAgain = false;
                Log.i(TAG, "Permission not give: " + perms.get(i));
            }
        }
        return requestPermissionAgain;
    }

    private void requestEssentialPermissions() {
        ArrayList perms = whichPermissionsToRequest.generatePermissionToRequest(1);
        String[] permsArray = new String[perms.size()];
        permsArray = (String[]) perms.toArray(permsArray);

        if(!perms.isEmpty()){
            ActivityCompat.requestPermissions(this, permsArray, 101);
        }else{
            direct();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 101:
                if(essentialPermissionsGiven()){
                   // requestNonEssentialPermissions();
                }else{
                    requestEssentialPermissions();
                }
                break;
            case 102:
                finalCheckToStartService();
                break;
        }
    }

    //if participants has permission but should not be collecting data then uninstall the app
    private boolean hasSpecialPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = 0;
            if (appOpsManager != null) {
                mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), getPackageName());
            }
            Log.i("From mainActivity", "Permission given: " + String.valueOf(mode == AppOpsManager.MODE_ALLOWED));
            return mode == AppOpsManager.MODE_ALLOWED;
        } else {
            return true;
        }
    }
    //request usage statistics permission

    private void requestPackagePermission(View view) {
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle("Usage permission")
                .setMessage("To participate in this experiment you must enable usage stats permission")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), Constants.MY_PERMISSION_REQUEST_PACKAGE_USAGE_STATS);
                    }
                })
                .show();
    }

    //detects the results of requesting permission from the usage statistics

    public boolean notificationPermissionRequired() {
        ComponentName cn = new ComponentName(this, recordingData.class);
        String flat = Settings.Secure.getString(this.getContentResolver(), "enabled_notification_listeners");
        final boolean enabled = flat != null && flat.contains(cn.flattenToString());
        return enabled;
    }

    private void requestNotificationPermission() {
        Intent intent=new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
        startActivityForResult(intent, 103);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.MY_PERMISSION_REQUEST_PACKAGE_USAGE_STATS:
                direct();
                break;
            case 103:
                finalCheckToStartService();
                break;
        }
    }

    private boolean serviceIsRunning (Class<?> serviceClass){
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void finalCheckToStartService() {
        if(assessIfInitializationRequired().contains(1)&&
                assessIfInitializationRequired().contains(2)&&
                assessIfInitializationRequired().contains(3)&&
                assessIfInitializationRequired().contains(4) &&
                assessIfInitializationRequired().contains(5)
                ){
            Intent startServiceIntent = new Intent(this, recordingData.class);
            Bundle b=new Bundle();
            b.putBoolean("phone restarted", false);
            startServiceIntent.putExtras(b);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(startServiceIntent);
            }else{
                startService(startServiceIntent);
            }
        }else{
            Log.i(TAG, "Final check failed: " + assessIfInitializationRequired());
        }
    }

    /*

    private void informAboutNonEssentialPermissions(final String[] permsArray) {
        LayoutInflater inflater = this.getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(inflater.inflate(R.layout.intro_message_screen, null))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(MainActivity.this, permsArray, 102);
                    }
                });
        builder.create()
                .show();
    }

    private void requestNonEssentialPermissions() {
        ArrayList perms = whichPermissionsToRequest.generatePermissionToRequest(2);
        String[] permsArray = new String[perms.size()];
        permsArray = (String[]) perms.toArray(permsArray);
        if(permsArray.length > 0){
            informAboutNonEssentialPermissions(permsArray);
        }else{
            finalCheckToStartService();
        }
    }

    */

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnExport:
                startActivity(new Intent(this, uploadProgress.class));
                break;
            case R.id.btnResetPassword:
                resetPassword();
                break;
        }
    }

    private void documentApps() {
        SharedPreferences preferences = getSharedPreferences("app collection", Context.MODE_PRIVATE);
        if(!preferences.getBoolean("apps documented", false)){
            Log.i("main", "documentAppsMain - initiated");
            try{
                securelyDocumentApps(surveyApps());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("apps documented", true);
                editor.apply();
            }catch (DocumentException e) {
                Log.e(TAG, "Document exception: " + e);
            } catch (IOException e) {
                Log.e(TAG, "IOexception: " + e);
            }
        }

    }

    private ArrayList<String> surveyApps() {
        ArrayList<String> builder = new ArrayList<>();
        List pkgAppList;
        try{
            SharedPreferences appPrefs = getSharedPreferences("Apps", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = appPrefs.edit();

            Intent mainIntent = new Intent(Intent.ACTION_MAIN,null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            pkgAppList = getPackageManager().queryIntentActivities(mainIntent,0);
            int appNum = 0;
            for (Object object: pkgAppList){
                ResolveInfo info = (ResolveInfo) object;
                    builder.add("" + info.activityInfo.applicationInfo);
                    builder.add("" + info.activityInfo.packageName);
                appNum++;
                editor.putString("App number " + appNum, ""+ getBaseContext().getPackageManager().getApplicationLabel(info.activityInfo.applicationInfo));
            }
            editor.putInt("Number of apps", appNum);
            editor.apply();
            Log.i(TAG, "APPS: " + builder);

        }catch (Exception e){
            Log.e("document apps", "Error: " + e);
            status.setText("Error detected, please inform researcher that: " + e);
        }
        return builder;
    }

    private void securelyDocumentApps(ArrayList<String> recordedApps) throws IOException, DocumentException{
            Document errorDocument = new Document();
            //getting destination
            File path = this.getFilesDir();
            File file = new File(path, Constants.APP_FILE);
            // Location to save
            PdfWriter writer =PdfWriter.getInstance(errorDocument, new FileOutputStream(file));
            writer.setEncryption("concretepage".getBytes(), prefs.getString("pdfPassword", "hufusm1234123").getBytes(), PdfWriter.ALLOW_COPY, PdfWriter.STANDARD_ENCRYPTION_128);
            writer.createXmpMetadata();
            // Open to write
            errorDocument.open();

            //add to document
            errorDocument.setPageSize(PageSize.A4);
            errorDocument.addCreationDate();

            //generating table with two columns
            PdfPTable table = new PdfPTable(2);

            try{
                for (int i = 0; i < recordedApps.size(); i++){
                    table.addCell(recordedApps.get(i));
                }
            }catch(Exception e){
                Log.e("file construct", "error " + e);
            }finally{
                errorDocument.add(table);
                errorDocument.addAuthor("Lancaster sensor lab");
                errorDocument.close();
                direct();
            }
        }

}
