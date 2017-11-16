package postepick.mms;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Gregoire on 03/11/2017.
 */

public class ExportMMSTask extends AsyncTask<Void, Integer, Boolean> {

    Context _context;

    TaskEventHandler _localEventHandler;

    List<MessageEntity> _messages;


    public ExportMMSTask(Context c, TaskEventHandler eHandler){
        _context = c;
        _localEventHandler = eHandler;
    }

    public void launch(){
        _localEventHandler.onStart();
        //_button.setText(R.string.launch_button_launched);
        this.execute();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {

        _messages = new ArrayList<>();
        try {
            Cursor c =  _context.getContentResolver().query(Uri.parse("content://mms/inbox"),null,null,null,null);
            while(c.moveToNext()){
                System.out.println("sms entry");
                int mmsId = c.getInt(0);
                String selectionPart = "mid=" + mmsId;
                Uri uri = Uri.parse("content://mms/part");
                Cursor cursor = _context.getContentResolver().query(uri, null,
                        selectionPart, null, null);
                if (cursor.moveToFirst()) {
                    do {
                        String partId = cursor.getString(cursor.getColumnIndex("_id"));
                        String type = cursor.getString(cursor.getColumnIndex("ct"));
                        if ("text/plain".equals(type)) {
                            String data = cursor.getString(cursor.getColumnIndex("_data"));
                            String body;
                            if (data != null) {
                                // implementation of this method below
                                body = getMmsText(partId);
                            } else {
                                body = cursor.getString(cursor.getColumnIndex("text"));
                            }
                        }
                        if ("image/jpeg".equals(type) || "image/bmp".equals(type) ||
                                "image/gif".equals(type) || "image/jpg".equals(type) ||
                                "image/png".equals(type)) {
                            getMmsImage(partId, type);
                        }
                    } while (cursor.moveToNext());
                }
            }
            Log.i(null, "FINISHED");

        }catch(Exception e){
            Log.e(this.getClass().getName(),"Error exporting mms", e);
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        _localEventHandler.onFinished();
        //_button.setText(R.string.launch_button);
        super.onPostExecute(aBoolean);

    }

    private void getMmsImage(String _id, String type) {
        Uri partURI = Uri.parse("content://mms/part/" + _id);
        InputStream is = null;
        Bitmap bitmap = null;
        try {
            boolean hasPermission = (ContextCompat.checkSelfPermission(_context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            String secStore = System.getenv("SECONDARY_STORAGE");

            is = _context.getContentResolver().openInputStream(partURI);
            writeMMS(is, type);

        } catch (IOException e) {
            Log.e(null, "getMmsImage: ",e );
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {}
            }
        }
    }

    protected void writeMMS(InputStream is,String type) throws IOException {

        File myFold = new File(Postepick.getStorageFolder());
        if(!myFold.exists()){
            if(!myFold.mkdirs()){
                Log.w(null, "pas de repertoire créé: ", null);
            }
        }
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
        String fileName=myFold.getAbsolutePath()+"/"+ UUID.randomUUID().toString()+fileExtension;
        FileOutputStream fos = new FileOutputStream(new File(fileName));
        try{
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }catch(Exception e){
            Log.e(getClass().getName(),"Error saving file",e);
        }finally{
            try {
                is.close();
            }catch(Exception e2){}
            try {
                fos.close();
            }catch(Exception e2){}
        }
        Log.i(null, "getMmsImage: file written:"+fileName);
    }

    private String getMmsText(String id)  {
        Uri partURI = Uri.parse("content://mms/part/" + id);
        InputStream is = null;
        StringBuilder sb = new StringBuilder();
        try {
            is = _context.getContentResolver().openInputStream(partURI);
            if (is != null) {
                InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                BufferedReader reader = new BufferedReader(isr);
                String temp = reader.readLine();
                while (temp != null) {
                    sb.append(temp);
                    temp = reader.readLine();
                }
            }
        } catch (IOException e) {}
        finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {}
            }
        }
        return sb.toString();
    }

}
