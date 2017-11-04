package postepick.mms;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

/**
 * Created by Gregoire on 04/11/2017.
 */

public class DriveConnector implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    GoogleApiClient _googleClient = null;
    Context _context = null;

    public DriveConnector(Context c){
        _context = c;
    }

    public void sendToDrive(){

    }

    public void exportToDrive() {
        if(_googleClient==null){
            Log.i(getClass().getName(),"Creating Google Drive Connection");
            _googleClient = new GoogleApiClient.Builder(_context)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(getClass().getName(),"Connected to GoogleDrive");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(getClass().getName(),"Google Drive Connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(getClass().getName(),"Google Drive Connection FAILED");
    }
}
