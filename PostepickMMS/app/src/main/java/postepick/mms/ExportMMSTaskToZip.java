package postepick.mms;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by Gregoire on 04/11/2017.
 */

public class ExportMMSTaskToZip extends ExportMMSTask {
    public ExportMMSTaskToZip(Context c, TaskEventHandler eHandler) {
        super(c, eHandler);
    }

    ZipOutputStream zous=null;


    @Override
    protected Boolean doInBackground(Void... voids) {
        File myFold = new File(Postepick.getStorageFolder());
        File zipResult = Postepick.getZipFile();

        try {
            myFold.mkdirs();
            zous = new ZipOutputStream(new FileOutputStream(zipResult));
            Boolean res =super.doInBackground(voids);
            try{
                zous.close();
            }catch(Exception e){
                Log.e(getClass().getName(),"error closing zip output");
                return false;
            }
            Log.i(getClass().getName(),"Zip SUCCESSFULLY created");
            return res;
        } catch (FileNotFoundException e) {
            Log.e(getClass().getName(),"FileNotFoundException", e);
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void writeMMS(InputStream is, String type) throws IOException {

        String fileExtension = ".jpg";
        if ("image/bmp".equals(type)){
            fileExtension = ".bmp";
        }
        else if("image/gif".equals(type)){
            fileExtension = ".gif";
        }
        else if("image/png".equals(type)){
            fileExtension = ".png";
        }
        String fileName=UUID.randomUUID().toString()+fileExtension;
        ZipEntry e = new ZipEntry(fileName);
        try{
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            zous.putNextEntry(e);
            while ((bytesRead = is.read(buffer)) != -1) {
                zous.write(buffer, 0, bytesRead);
            }
            zous.closeEntry();
        }catch(Exception ess){
            Log.e(getClass().getName(),"Error saving file",ess);
        }finally{
            try {
                is.close();
            }catch(Exception e2){}
        }
        Log.i(null, "getMmsImage: file written:"+fileName);
    }

}
