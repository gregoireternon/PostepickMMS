package postepick.mms;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    DriveConnector driveConnector=null;

    private static final int REQUEST_CODE_RESOLUTION = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.settings_bg_remote));
        setContentView(R.layout.activity_main);




        final Button driveExportButton = (Button) findViewById(R.id.ExportToDrive);
        final Button fileImportButton = (Button) findViewById(R.id.ImportFromFile);
        driveExportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MainActivity.this.driveConnector==null){
                    DriveConnector dc = new DriveConnector(MainActivity.this, new TaskEventHandler() {
                        @Override
                        public void onStart() {
                            driveExportButton.setEnabled(false);
                            driveExportButton.setText(R.string.drive_export_button_zipping);
                        }

                        @Override
                        public void onFinished() {
                            driveExportButton.setText(R.string.drive_export_button_exporting);
                        }
                    });
                    MainActivity.this.driveConnector = dc;
                }

                MainActivity.this.driveConnector.exportToDrive();
            }
        });
        fileImportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MainActivity.this.driveConnector==null){
                    DriveConnector dc = new DriveConnector(MainActivity.this, new TaskEventHandler() {
                        @Override
                        public void onStart() {
                            fileImportButton.setEnabled(false);
                            fileImportButton.setText("Importing data...");
                        }

                        @Override
                        public void onFinished() {
                            fileImportButton.setText("Import files");
                        }
                    });
                }

                ImportMMSTask exportTask = new ImportMMSTask(MainActivity.this);
                exportTask.launch();

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
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case DriveConnector.REQUEST_CODE_SIGN_IN:
                Log.i(getClass().getName(), "Sign in request code");
                // Called after user is signed in.
                if (resultCode == RESULT_OK) {
                    Log.i(getClass().getName(), "Signed in successfully.");
                    this.driveConnector.connected();
                }
                break;
            case DriveConnector.REQUEST_CODE_CREATOR:
                Log.i(getClass().getName(), "creator request code");
                // Called after a file is saved to Drive.
                if (resultCode == RESULT_OK) {
                    Log.i(getClass().getName(), "Image successfully saved.");
                   // Just start the camera again for another photo.
                    final Button driveExportButton = (Button) findViewById(R.id.ExportToDrive);
                    driveExportButton.setText(R.string.drive_export_button);

                    driveExportButton.setEnabled(true);
                }
                break;
        }
    }
}
