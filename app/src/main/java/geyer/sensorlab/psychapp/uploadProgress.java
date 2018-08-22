package geyer.sensorlab.psychapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class uploadProgress extends Activity {

        //UI components
        ProgressBar progressBar;
        TextView progressStatus;

        //components for handling data which is not deleted with activity related to the operation of the app
        SharedPreferences upPrefs;

        static final String TAG = "uploadProgress";

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.upload_screen);
            initializeVisibleComponents();
            initializeInvisibleComponents();
            writePDFTable();
        }

        private void initializeVisibleComponents() {
            progressBar = findViewById(R.id.pbUpload);
            progressBar.setProgress(1);
            progressStatus = findViewById(R.id.tvProgress);
        }

        private void initializeInvisibleComponents() {
            upPrefs = getSharedPreferences("general prefs", Context.MODE_PRIVATE);

            //load the native libraries for the SQL cipher
            SQLiteDatabase.loadLibs(this);
        }

        //determines flow of activity
        //handles any errors and relays them to the user.
        //if upload went smoothly then the app selection interface should appear
        private void writePDFTable() {
            boolean okToUpload = true;
            try {
                makeSecurePdf();
            } catch (IOException e) {
                okToUpload = false;
                e.printStackTrace();
                final String toRelay = "IO Error: " + e.getLocalizedMessage();
                progressStatus.setText(toRelay);
            } catch (DocumentException e) {
                okToUpload = false;
                e.printStackTrace();
                final String toRelay = "Document Error: " + e.getLocalizedMessage();
                progressStatus.setText(toRelay);
            }finally {
                if(okToUpload){
                    email();
                }else{
                    Toast.makeText(this, "Please inform the researcher about the above error", Toast.LENGTH_LONG).show();
                }

            }
        }

    private void documentProgress(float currentProgress) {
            Log.i("progress", "" + currentProgress);
            progressBar.setProgress((int) currentProgress);
            final String toRelay = currentProgress +"%";
            progressStatus.setText(toRelay);
        }

        private void makeEventPdf() throws IOException, DocumentException {
            Document errorDocument = new Document();
            //getting destination
            File path = this.getFilesDir();
            File file = new File(path, Constants.EVENTS_FILE);
            // Location to save
            PdfWriter writer =PdfWriter.getInstance(errorDocument, new FileOutputStream(file));
            writer.setEncryption("concretepage".getBytes(), upPrefs.getString("pdfPassword", "hufusm1234123").getBytes(), PdfWriter.ALLOW_COPY, PdfWriter.STANDARD_ENCRYPTION_40);
            writer.createXmpMetadata();
            // Open to write
            errorDocument.open();

            //add to document
            errorDocument.setPageSize(PageSize.A4);
            errorDocument.addCreationDate();
            //retrieving error data from SQLite database
            insecureDatabase iDB = new insecureDatabase(this);
            iDB.open();

            ArrayList<String> entries = iDB.getLoggedEntries();
            ArrayList<Long> TimeStamp = iDB.getTimes();

            iDB.close();

            //generating table with two columns
            PdfPTable table = new PdfPTable(2);

            try{
                for (int i = 0; i < entries.size(); i++){
                    table.addCell(entries.get(i));
                    table.addCell("" + TimeStamp.get(i));
                    if(i!=0){
                        //documents current progress
                        float currentProgress = (float) i/entries.size();
                        currentProgress = currentProgress*100;
                        documentProgress(currentProgress);
                    }
                }
            }catch(Exception e){
                Log.e("file construct", "error " + e);
            }finally{
                documentProgress(100);
                errorDocument.add(table);
                errorDocument.addAuthor("Lancaster sensor lab");
                errorDocument.close();
            }
        }

        //this is from the SQL cipher database
    private void makeSecurePdf() throws IOException, DocumentException {
        //creates document
        Document document = new Document();
        //getting destination
        File path = this.getFilesDir();
        File file = new File(path, Constants.EVENTS_FILE);
        // Location to save
        PdfWriter writer =PdfWriter.getInstance(document, new FileOutputStream(file));
        writer.setEncryption("concretepage".getBytes(), upPrefs.getString("pdfPassword", "hufusm1234123").getBytes(), PdfWriter.ALLOW_COPY, PdfWriter.ENCRYPTION_AES_128);
        writer.createXmpMetadata();
        // Open to write
        document.open();

        //add to document
        document.setPageSize(PageSize.A4);
        document.addCreationDate();

        String selectQuery = "SELECT * FROM " + secureDatabaseContract.secureDatabase.TABLE_NAME;
        SQLiteDatabase db = secureDatabaseDbHelper.getInstance(this).getReadableDatabase(upPrefs.getString("password", "not to be used"));

        Cursor c = db.rawQuery(selectQuery, null);

        int iEvent = c.getColumnIndex(secureDatabaseContract.secureDatabase.EVENT);
        int iTime = c.getColumnIndex(secureDatabaseContract.secureDatabase.TIMESTAMP);

        ArrayList<String> databaseEvent = new ArrayList<>();
        ArrayList<Long> databaseTimestamp = new ArrayList<>();


        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
            databaseEvent.add(c.getString(iEvent));
            databaseTimestamp.add(c.getLong(iTime));
        }

        c.close();
        db.close();

        //makes a table with four columns
        PdfPTable table = new PdfPTable(2);

        //attempts to add the columns
        try{
            for (int i = 0; i < databaseEvent.size(); i++){
                table.addCell("" + databaseEvent.get(i));
                table.addCell("" + databaseTimestamp.get(i));
                if(i!=0){
                    float currentProgress = (float) i/databaseEvent.size();
                    currentProgress = currentProgress*100;
                    documentProgress(currentProgress);
                }
            }
        }catch(Exception e){
            Log.e("file construct", "error " + e);
        }finally{
            documentProgress(100);
            document.add(table);
            document.addAuthor("Kris");
            document.close();

            databaseEvent.clear();
            databaseTimestamp.clear();
        }
    }


        //relays the email
        private void email() {

            //documenting intent to send multiple
            Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            intent.setType("text/plain");

            //getting directory for internal files
            String directory = (String.valueOf(this.getFilesDir()) + File.separator);
            Log.i("Directory", directory);

            //initializing files reference
            File eventsFile = new File(directory + File.separator + Constants.EVENTS_FILE);
            File appFile = new File(directory + File.separator + Constants.APP_FILE);

            //list of files to be uploaded
            ArrayList<Uri> files = new ArrayList<>();

            //if target files are identified to exist then they are packages into the attachments of the email
            try {
                if(eventsFile.exists()){
                    files.add(FileProvider.getUriForFile(this, "geyer.sensorlab.psychapp.fileprovider", eventsFile));
                }else{
                    Log.i(TAG, "events file doesn't exist");
                }

                if(appFile.exists()){
                    Log.i("toExportFileError", "true");
                    files.add(FileProvider.getUriForFile(this, "geyer.sensorlab.psychapp.fileprovider", appFile));
                }else{
                    Log.i(TAG, "app file doesn't exist");
                }

                //adds the file to the intent to send multiple data points
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                this.startActivity(intent);
            }
            catch (Exception e){
                Log.e("File upload error1", "Error:" + e);
            }
        }

}
