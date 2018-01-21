package postepick.mms;

import android.os.Environment;

import java.io.File;

/**
 * Created by Gregoire on 04/11/2017.
 */

public class Postepick {
    public final static String MMS_FOLDER = "/mmsFold";


    public static File getZipFile(File targetdir){
        return new File(targetdir+"/monZip.zip");
    }
}
