package postepick.mms;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.google.android.gms.common.api.GoogleApiClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    DriveConnector driveConnector=null;
    private static final int REQUEST_CODE_RESOLUTION = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        final Button extractButton = (Button) findViewById(R.id.LaunchExtract);
        extractButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ExportMMSTask exporter = new ExportMMSTaskToZip(MainActivity.this, new TaskEventHandler() {
                    @Override
                    public void onStart() {
                        extractButton.setText(R.string.launch_button_launched);
                    }

                    @Override
                    public void onFinished() {
                        extractButton.setText(R.string.launch_button);
                    }
                });
                exporter.launch();
            }
        });
        final Button driveExportButton = (Button) findViewById(R.id.ExportToDrive);
        driveExportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MainActivity.this.driveConnector==null){
                    DriveConnector dc = new DriveConnector(MainActivity.this, new TaskEventHandler() {
                        @Override
                        public void onStart() {
                            extractButton.setText(R.string.export_button_launched);
                        }

                        @Override
                        public void onFinished() {
                            extractButton.setText(R.string.launch_button);
                        }
                    });
                    MainActivity.this.driveConnector = dc;
                }

                MainActivity.this.driveConnector.exportToDrive();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==REQUEST_CODE_RESOLUTION){
            if(resultCode==RESULT_OK) {
                driveConnector.exportToDrive();
            }
        }
    }
}
