package Utils;

import android.util.Log;

import java.io.File;

import static com.wifidirect_datatransfer.GlobalApplication.FilesFolder;
import static com.wifidirect_datatransfer.GlobalApplication.Folder;
import static com.wifidirect_datatransfer.GlobalApplication.ImageFolder;
import static com.wifidirect_datatransfer.GlobalApplication.Path;

/**
 * Created by xcaluser on 19/6/17.
 */

public class Constants {

    public static final int CONNECTION_REQUESTED = 1;
    public static final int ALREADY_CONNECTED = 0;
    public static final int UNABLE_TO_FIND_SECURITY_TYPE=2;
    public static final int SSID_NOT_FOUND=4;

    public static String RECEIVER_HOST ;
    public static int RECEIVER_PORT ;

    public static final String key = "DT";


    public static String SENDER_TYPE = "Android";  // Ios

    public static final String COMMAND_1 = "CreateSocket";
    public static final String COMMAND_2 = "SendFilePathInfo";



    public static void createDir() {


        String path = Path+"/" + Folder;

        File fileDir =  new File(path);

        if(!fileDir.exists())
        {
            Log.e("DataTransfer" , "folder not present");
            fileDir.mkdir();
        }


        String picturesPath = fileDir.getAbsolutePath()+"/" + ImageFolder;

        File filePicture =  new File(picturesPath);

        if(!filePicture.exists())
        {
            Log.e("DataTransfer" , "folder not present");
            filePicture.mkdir();
        }

        String filesPath = fileDir.getAbsolutePath()+"/" + FilesFolder;

        File files =  new File(filesPath);

        if(!files.exists())
        {
            Log.e("DataTransfer" , "folder not present");
            files.mkdir();
        }


        Log.e("DataTransfer" , "folder created");

    }
}
