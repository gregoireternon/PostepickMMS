package postepick.mms;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.CreateFileActivityOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import android.content.IntentSender.SendIntentException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;

/**
 * Created by Gregoire on 04/11/2017.
 */

public class DriveConnector implements
        GoogleApiClient.OnConnectionFailedListener,
        TaskEventHandler{

    GoogleSignInClient _googleSIClient = null;
    DriveClient _driveClient;
    DriveResourceClient _driveResourceClient;
    Activity _context = null;
    TaskEventHandler _taskEventHandler=null;

    public static final int REQUEST_CODE_SIGN_IN = 0;
    public static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
    public static final int REQUEST_CODE_CREATOR = 2;

    public DriveConnector(Activity c, TaskEventHandler teh){
        _context = c;
        _taskEventHandler = teh;
    }

    public void sendToDrive(){

    }

    public void exportToDrive() {

        File myFold = _context.getCacheDir();//  new File(Postepick.getStorageFolder());

        if(_googleSIClient==null){
            Log.i(getClass().getName(),"Creating Google Drive Connection");
            _googleSIClient = buildGoogleSignInClient();
        }
        _context.startActivityForResult(_googleSIClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }
    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_FILE)
                        .requestScopes(Drive.SCOPE_APPFOLDER)
                        .build();
        return GoogleSignIn.getClient(this._context, signInOptions);
    }



    public void connected() {
        Log.i(getClass().getName(),"Connected to GoogleDrive");
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            int hasWriteContactsPermission = _context.checkSelfPermission(Manifest.permission.READ_SMS);
            if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
                _context.requestPermissions(new String[] {Manifest.permission.READ_SMS},
                        DriveConnector.REQUEST_CODE_SIGN_IN);
                return;
            }
        }


        ExportMMSTaskToZip exportTask = new ExportMMSTaskToZip(_context,this);
        exportTask.launch();
    }



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
        try {
            Log.i(getClass().getName(), "Export finished, about to send to Drive");
            _driveClient = Drive.getDriveClient(_context, GoogleSignIn.getLastSignedInAccount(_context));
            _driveResourceClient = Drive.getDriveResourceClient(this._context, GoogleSignIn.getLastSignedInAccount(_context));


            _driveResourceClient
                    .createContents()
                    .continueWithTask(
                            new Continuation<DriveContents, Task<Void>>() {
                                @Override
                                public Task<Void> then(@NonNull Task<DriveContents> task) throws Exception {
                                    Log.i(getClass().getName(), "new content created");
                                    OutputStream os = task.getResult().getOutputStream();
                                    FileInputStream fis = null;
                                    try {
                                        fis = new FileInputStream(Postepick.getZipFile(_context.getCacheDir()));
                                        byte[] buffer = new byte[8 * 1024];
                                        int bytesRead;
                                        while ((bytesRead = fis.read(buffer)) != -1) {
                                            os.write(buffer, 0, bytesRead);
                                        }
                                    } catch (Exception e) {
                                        Log.e(getClass().getName(), "Error saving file", e);
                                    } finally {
                                        try {
                                            fis.close();
                                        } catch (Exception e2) {
                                        }
                                        try {
                                            os.close();
                                        } catch (Exception e2) {
                                        }
                                    }
                                    Log.i(getClass().getName(), "File sent to Drive");
                                    MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                                            .setMimeType("application/zip").setTitle("MMSExport.zip").build();
                                    // Create an intent for the file chooser, and start it.
                                    CreateFileActivityOptions createFileActivityOptions = new CreateFileActivityOptions.Builder()
                                            .setInitialMetadata(metadataChangeSet)
                                            .setInitialDriveContents(task.getResult())
                                            .build();

                                    DriveConnector.this._taskEventHandler.onFinished();
                                    return _driveClient
                                            .newCreateFileActivityIntentSender(createFileActivityOptions)
                                            .continueWith(
                                                    new Continuation<IntentSender, Void>() {
                                                        @Override
                                                        public Void then(@NonNull Task<IntentSender> task) throws Exception {
                                                            _context.startIntentSenderForResult(task.getResult(), REQUEST_CODE_CREATOR, null, 0, 0, 0);
                                                            return null;
                                                        }
                                                    });

                                }
                            })
                    .addOnSuccessListener(
                            new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.i(getClass().getName(),"SUCCESS");
                                }
                            }
                    )
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(getClass().getName(), "Failed to create new contents.", e);
                                }
                            });

        } catch (Exception eee) {
            Log.e(getClass().getName(), "Error");
        }


    }

    }
