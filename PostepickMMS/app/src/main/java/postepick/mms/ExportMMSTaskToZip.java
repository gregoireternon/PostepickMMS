package postepick.mms;

import android.content.Context;
import android.os.Environment;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
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
        File myFold = _context.getCacheDir();// new File(Postepick.getStorageFolder());
        File zipResult = Postepick.getZipFile(myFold);

        try {
            myFold.mkdirs();
            zous = new ZipOutputStream(new FileOutputStream(zipResult));
            Boolean res =super.doInBackground(voids);
            writeJson();
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

    private void writeJson() {
        ZipEntry e = new ZipEntry("mmscontent.json");
        try{
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            zous.putNextEntry(e);
            OutputStreamWriter writer = new OutputStreamWriter(zous);
            Gson gson = new Gson();
            writer.write(gson.toJson(_messages));
            writer.flush();
            zous.closeEntry();
        }catch(Exception ess){
            Log.e(getClass().getName(),"Error saving json",ess);

        }finally{

        }
    }

    @Override
    protected String writeMMS(InputStream is, String type, String filePrefix) throws IOException {

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
        String fileName=filePrefix+fileExtension;
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
            return null;
        }finally{
            try {
                is.close();
            }catch(Exception e2){}
        }
        Log.i(null, "getMmsImage: file written:"+fileName);
        return fileName;
    }

}
