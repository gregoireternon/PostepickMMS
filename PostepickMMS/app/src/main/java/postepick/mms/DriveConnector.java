package postepick.mms;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.MetadataChangeSet;
import android.content.IntentSender.SendIntentException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;

/**
 * Created by Gregoire on 04/11/2017.
 */

public class DriveConnector implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        TaskEventHandler{

    GoogleApiClient _googleClient = null;
    Activity _context = null;
    TaskEventHandler _taskEventHandler=null;

    public DriveConnector(Activity c, TaskEventHandler teh){
        _context = c;
        _taskEventHandler = teh;
    }

    public void sendToDrive(){

    }

    public void exportToDrive() {

        File myFold = new File(Postepick.getStorageFolder());
        myFold.delete();
        Log.i(getClass().getName(),"mmsFold deleted");
        if(_googleClient==null){
            Log.i(getClass().getName(),"Creating Google Drive Connection");
            _googleClient = new GoogleApiClient.Builder(_context)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        _googleClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(getClass().getName(),"Connected to GoogleDrive");
        ExportMMSTaskToZip exportTask = new ExportMMSTaskToZip(_context,this);
        exportTask.launch();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(getClass().getName(),"Google Drive Connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult res) {
        Log.e(getClass().getName(),"Google Drive Connection FAILED");
        Log.i(getClass().getName(), "GoogleApiClient connection failed: " + res.toString());
        if (!res.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this._context, res.getErrorCode(), 0).show();
            return;
        }
        // The failure has a resolution. Resolve it.
        // Called typically when the app is not yet authorized, and an
        // authorization
        // dialog is displayed to the user.
        try {
            res.startResolutionForResult(this._context, 3);
        } catch (Exception e) {
            Log.e(getClass().getName(), "Exception while starting resolution activity", e);
        }
    }

    @Override
    public void onStart() {
        _taskEventHandler.onStart();
    }

    @Override
    public void onFinished() {
        Log.i(getClass().getName(),"Export finished, about to send to Drive");
        Drive.DriveApi.newDriveContents(_googleClient).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override
            public void onResult(@NonNull DriveApi.DriveContentsResult res) {
                if(!res.getStatus().isSuccess()){
                    Log.e(getClass().getName(),"Error creating new content");
                    return;
                }
                Log.i(getClass().getName(),"new content created");
                OutputStream os = res.getDriveContents().getOutputStream();
                FileInputStream fis=null;
                try{
                    fis = new FileInputStream(Postepick.getZipFile());
                    byte[] buffer = new byte[8 * 1024];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }catch(Exception e){
                    Log.e(getClass().getName(),"Error saving file",e);
                }finally{
                    try {
                        fis.close();
                    }catch(Exception e2){}
                    try {
                        os.close();
                    }catch(Exception e2){}
                }
                Log.i(getClass().getName(),"File sent to Drive");
                MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                        .setMimeType("application/zip").setTitle("MMSExport.zip").build();
                // Create an intent for the file chooser, and start it.
                IntentSender intentSender = Drive.DriveApi
                        .newCreateFileActivityBuilder()
                        .setInitialMetadata(metadataChangeSet)
                        .setInitialDriveContents(res.getDriveContents())
                        .build(_googleClient);

                try {
                    DriveConnector.this._context.startIntentSenderForResult(
                            intentSender, 0, null, 0, 0, 0);
                } catch (SendIntentException e) {
                    Log.i(getClass().getName(), "Failed to launch file chooser.");
                }
                DriveConnector.this._taskEventHandler.onFinished();
            }
        });
    }
}
