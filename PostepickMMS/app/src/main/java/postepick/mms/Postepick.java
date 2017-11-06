package postepick.mms;

import android.os.Environment;

import java.io.File;

/**
 * Created by Gregoire on 04/11/2017.
 */

public class Postepick {
    public final static String MMS_FOLDER = "/mmsFold";

    public static String getStorageFolder(){
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + MMS_FOLDER;
    }
    public static File getZipFile(){
        return new File(getStorageFolder()+"/monZip.zip");
    }
}
