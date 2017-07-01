package ui;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.wifidirect_datatransfer.R;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import pojos.DocumentPojo;


/**
 * Created by xcaluser on 7/6/17.
 */

public class DocumentArtifect extends Fragment {

    DocAdapter adapter;
    ArrayList<DocumentPojo> list_doc_pojo = new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        return inflater.inflate(R.layout.fragment_document, container, false);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {


        RecyclerView list;
        list = (RecyclerView) view.findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        list.setHasFixedSize(true);




        list.setAdapter(adapter);

        adapter.notifyDataSetChanged();

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDocFiles();
        adapter = new DocAdapter(list_doc_pojo);
    }
    private void getDocFiles() {

        Log.e("DOCUMENT" , "getDocFiles");
        String root_sd = Environment.getExternalStorageDirectory().getAbsolutePath().toString();
        File file = new File(root_sd);


        File list[] = file.listFiles();


        for (int i = 0; i < list.length; i++) {

            //check the contents of each folder before adding to list

            Log.e("DOCUMENT" , "root path ");

            File mFile = new File(file, list[i].getName());


            if(mFile.isDirectory()){


                File dirList[] = mFile.listFiles();
                if(dirList == null) continue;
                for (int j = 0; j < dirList.length; j++) {

                    Log.e("DOCUMENT" , "folder path ");

                    if((dirList[j].getName().toLowerCase(Locale.getDefault()).endsWith(".txt"))
                            ||(dirList[j].getName().toLowerCase(Locale.getDefault()).endsWith(".pdf"))
                            ||(dirList[j].getName().toLowerCase(Locale.getDefault()).endsWith(".docx"))
                            || (dirList[j].getName().toLowerCase(Locale.getDefault()).endsWith(".xlsx"))
                            || (dirList[j].getName().toLowerCase(Locale.getDefault()).endsWith(".html"))
                            || (dirList[j].getName().toLowerCase(Locale.getDefault()).endsWith(".xml"))){


                        if(dirList[j].length()>0) {
                            DocumentPojo docpojo = new DocumentPojo();
                            docpojo.setFilename(dirList[j].getName());
                            docpojo.setFilePath(dirList[j].getAbsolutePath().toString());
                            docpojo.setFilesize(getStringSizeLengthFile(dirList[j].length()));
                            docpojo.setSelected(false);


                            Log.e("DOCUMENT", "file found " + dirList[j].getName());
                            list_doc_pojo.add(docpojo);
                        }

                    }
                }








            }
            else{


                if((mFile.getName().toLowerCase(Locale.getDefault()).endsWith(".txt"))
                        ||(mFile.getName().toLowerCase(Locale.getDefault()).endsWith(".pdf"))
                        ||(mFile.getName().toLowerCase(Locale.getDefault()).endsWith(".docx"))
                        || (mFile.getName().toLowerCase(Locale.getDefault()).endsWith(".xlsx"))
                        || (mFile.getName().toLowerCase(Locale.getDefault()).endsWith(".html"))
                        || (mFile.getName().toLowerCase(Locale.getDefault()).endsWith(".xml"))){

                    if(mFile.length()>0) {
                        DocumentPojo docpojo = new DocumentPojo();
                        docpojo.setFilename(mFile.getName());
                        docpojo.setFilePath(mFile.getAbsolutePath().toString());
                        docpojo.setFilesize(getStringSizeLengthFile(mFile.length()));
                        docpojo.setSelected(false);


                        Log.e("DOCUMENT", "file found " + mFile.getName());
                        list_doc_pojo.add(docpojo);
                    }

                }

            }




        }



    }

    public class DocAdapter extends RecyclerView.Adapter<DocAdapter.ViewHolder> {

        ArrayList<DocumentPojo> numbers;

        public DocAdapter(List<DocumentPojo> numbers) {
            Log.e("DOCUMENT" , "doc adapter ");
            this.numbers = new ArrayList<>(numbers);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.client_list_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            holder.bindData(numbers.get(position));

            holder.checkbox.setOnCheckedChangeListener(null);


            holder.checkbox.setChecked(numbers.get(position).isSelected());

            holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    numbers.get(holder.getAdapterPosition()).setSelected(isChecked);

                    ((Artifacts)getActivity()).updateSenderList(isChecked,numbers.get(position).getFilePath());

                }
            });
        }

        @Override
        public int getItemCount() {
            return numbers.size();
        }


        public class ViewHolder extends RecyclerView.ViewHolder {

            private TextView ONEs;
            private TextView textONEs;
            private CheckBox checkbox;

            public ViewHolder(View v) {
                super(v);
                ONEs = (TextView) v.findViewById(R.id.ONEs);
                textONEs = (TextView) v.findViewById(R.id.textONEs);
                checkbox = (CheckBox) v.findViewById(R.id.checkbox);
            }

            public void bindData(DocumentPojo number) {

                Log.e("DOCUMENT" , "binddata name " + number.getfilename());
                Log.e("DOCUMENT" , "binddata path " + number.getFilePath());

                ONEs.setText(number.getfilename());
                textONEs.setText(number.getfilesize());
            }
        }
    }

    public String getStringSizeLengthFile(long size) {

        DecimalFormat df = new DecimalFormat("0.00");

        float sizeKb = 1024.0f;
        float sizeMo = sizeKb * sizeKb;
        float sizeGo = sizeMo * sizeKb;
        float sizeTerra = sizeGo * sizeKb;


        if(size < sizeMo)
            return df.format(size / sizeKb)+ " KB";
        else if(size < sizeGo)
            return df.format(size / sizeMo) + " MB";
        else if(size < sizeTerra)
            return df.format(size / sizeGo) + " GB";

        return "";
    }
}
