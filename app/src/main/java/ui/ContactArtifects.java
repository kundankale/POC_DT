package ui;

import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.Toast;

import com.wifidirect_datatransfer.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static com.wifidirect_datatransfer.GlobalApplication.Folder;
import static com.wifidirect_datatransfer.GlobalApplication.Path;

/**
 * Created by xcaluser on 7/6/17.
 */

public class ContactArtifects extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       return inflater.inflate(R.layout.fragment_contact, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

       final CheckedTextView chkTextView  = (CheckedTextView)view.findViewById(R.id.checkedTextView);

        chkTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chkTextView.isChecked()) {

                    final String vfile = "Contacts.vcf";


                    File f = new File(Path+"/"+Folder+"/"+vfile);
                    String path = Path+"/"+Folder+"/"+vfile;

                    ((Artifacts)getActivity()).updateSenderList(false,path);
                    chkTextView.setChecked(false);
                }
                else {
                    createVcf();
                    chkTextView.setChecked(true);
                }

            }
        });


    }

    public void createVcf(){



        ArrayList<String> arrList = new ArrayList<>();
        final String vfile = "Contacts.vcf";


        File f = new File(Path+"/"+Folder+"/"+vfile);
        String path = Path+"/"+Folder+"/"+vfile;
        if(f.exists()){
            f.delete();
        }


        Cursor phones = getActivity().getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                null, null, null);
        phones.moveToFirst();

        if(phones.getCount()>0) {
            Log.e("CONTACTS", "cursor count " + phones.getCount());
            for (int i = 0; i < phones.getCount(); i++) {

                String lookupKey = phones.getString(phones
                        .getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));

                Log.e("CONTACTS", "lookupkey Db" + lookupKey);

                    Log.e("CONTACTS", "adding lookupkey " + lookupKey);
                    //arrList.add(lookupKey);

                    Uri uri = Uri.withAppendedPath(
                            ContactsContract.Contacts.CONTENT_VCARD_URI,
                            lookupKey);
                    AssetFileDescriptor fd;
                    try {
                        fd = getActivity().getContentResolver().openAssetFileDescriptor(uri, "r");
                        FileInputStream fis = fd.createInputStream();
                        Log.e("CONTACTS", "------------------ 1 ------------");
                        byte[] buf = readBytes(fis);  //new byte[(int) fd.getDeclaredLength()];
                        fis.read(buf);
                        Log.e("CONTACTS", "------------------ 2 ------------");
                        String VCard = new String(buf);

                        if(!arrList.contains(VCard)){
                            arrList.add(VCard);
                            FileOutputStream mFileOutputStream = new FileOutputStream(path,
                                    true);


                            mFileOutputStream.write(VCard.toString().getBytes());
                        }
                        else{

                            Log.e("CONTACTS", "Vcard  already present");
                        }


                        phones.moveToNext();

                        Log.e("CONTACTS", "------------------ 3 ------------");
                        Log.e("CONTACTS", VCard);
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        phones.moveToNext();
                        e1.printStackTrace();
                        Log.e("CONTACTS", "error" + e1.getMessage());
                    }
                }

            phones.close();

            ((Artifacts)getActivity()).updateSenderList(true,path);
        }
        else{

            Toast.makeText(getActivity(), "No Contacts found to Transfer", Toast.LENGTH_SHORT).show();
        }






    }
    public byte[] readBytes(InputStream inputStream) throws IOException {
        // this dynamically extends to take the bytes you read
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        // this is storage overwritten on each iteration with bytes
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        // we need to know how may bytes were read to write them to the byteBuffer
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        // and then we can return your byte array.
        return byteBuffer.toByteArray();
    }

}
