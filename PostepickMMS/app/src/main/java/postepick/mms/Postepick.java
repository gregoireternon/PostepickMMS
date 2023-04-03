package postepick.mms;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by Gregoire on 04/11/2017.
 */

public class Postepick {
    public final static String MMS_FOLDER = "/mmsFold";

    public static String getStorageFolder(Context _context){
        //return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + MMS_FOLDER;
        return _context.getExternalFilesDir(MMS_FOLDER).getAbsolutePath();
    }
    public static File getZipFile(Context _context){
        return new File(getStorageFolder(_context)+"/monZip.zip");
    }
}
