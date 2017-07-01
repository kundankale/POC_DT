package services;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

import static com.wifidirect_datatransfer.GlobalApplication.Folder;
import static com.wifidirect_datatransfer.GlobalApplication.ImageFolder;
import static com.wifidirect_datatransfer.GlobalApplication.Path;

/**
 * Created by xcaluser on 1/7/17.
 */

public class GalleryRefreshService extends Service {


    public static final String TAG = "GALLERYREFRESHSERVICE";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)  {


        Log.e(TAG,"Service on startcommand");


        if(chkImageDirectory()){

            new UpdateAndroidGallery().execute();
        }



        return START_STICKY;
    }

    public class UpdateAndroidGallery extends AsyncTask<Void,Void,Void>{


        @Override
        protected Void doInBackground(Void... params) {

            try {
                String path = Path + "/" + Folder;

                File fileDir = new File(path);
                String picturesPath = fileDir.getAbsolutePath() + "/" + ImageFolder;

                File filePicDir = new File(picturesPath);
                ArrayList<String> list_path = getList(filePicDir, picturesPath);

                Log.e(TAG,"doinback list path size " + list_path.size());

                for (int i = 0; i < list_path.size(); i++) {
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri contentUri = Uri.fromFile(new File(list_path.get(i)));
                    mediaScanIntent.setData(contentUri);
                    GalleryRefreshService.this.sendBroadcast(mediaScanIntent);

                }

            }catch(Exception e){

                Log.e(TAG,"Exception while refreshing gallery " + e.getMessage());
            }



            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Log.e(TAG,"on post of gallery update");

            stopSelf();
        }
    }




    public boolean chkImageDirectory(){

        String path = Path+"/" + Folder;

        File fileDir =  new File(path);

        if(!fileDir.exists())
        {
            Log.e("DataTransfer" , "folder not present");
            fileDir.mkdir();
        }


        String picturesPath = fileDir.getAbsolutePath()+"/" + ImageFolder;

        File filePicture =  new File(picturesPath);

        if(filePicture.exists())
        {
            Log.e(TAG , "image folder present");

            return true;

        }
        else{
            return false;
        }


    }
    private ArrayList<String> getList(File parentDir, String pathToParentDir) {

        ArrayList<String> inFiles = new ArrayList<String>();
        String[] fileNames = parentDir.list();

        for (String fileName : fileNames) {
            if ((fileName.endsWith(".jpg") || fileName.endsWith(".png") ||
                    fileName.endsWith(".JPG") || fileName.endsWith(".PNG"))) {
                inFiles.add(pathToParentDir+ "/"+ fileName);
            }
        }

        return inFiles;
    }

}
