package ui;


import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;

import com.wifidirect_datatransfer.R;

import java.io.File;
import java.util.ArrayList;

import Utils.CreateList;


/**
 * Created by xcaluser on 7/6/17.
 */

public class ImageArtifects extends Fragment {

    public ImageAdapter imageAdapter;
    ArrayList<CreateList> imageData = new ArrayList<>();



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        return inflater.inflate(R.layout.fragment_imagegallery, container, false);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imageAdapter = new ImageAdapter();
        imageAdapter.initialize();
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {



        getAllShownImagesPath(getActivity());

        GridView sdcardImages = (GridView) view.findViewById(R.id.gridView1);
        sdcardImages.setAdapter(imageAdapter);
        imageAdapter.notifyDataSetChanged();



    }

    public class ImageAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        public ArrayList<ImageItem> images = new ArrayList<ImageItem>();

        public ImageAdapter() {
            mInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void initialize() {
            images.clear();

            final String[] columns = { MediaStore.Images.Thumbnails._ID };
            final String orderBy = MediaStore.Images.Media._ID;
            Cursor imagecursor =getActivity().managedQuery(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
                    null, null, orderBy);
            if(imagecursor != null){
                int image_column_index = imagecursor
                        .getColumnIndex(MediaStore.Images.Media._ID);
                int count = imagecursor.getCount();
                for (int i = 0; i < count; i++) {
                    imagecursor.moveToPosition(i);
                    int id = imagecursor.getInt(image_column_index);
                    ImageItem imageItem = new ImageItem();
                    imageItem.id = id;

                    imageItem.img = MediaStore.Images.Thumbnails.getThumbnail(
                            getActivity().getContentResolver(), id,
                            MediaStore.Images.Thumbnails.MICRO_KIND, null);
                    images.add(imageItem);





                }
                imagecursor.close();
            }
            notifyDataSetChanged();
        }



        public int getCount() {
            return images.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.galleryitem, null);
                holder.imageview = (ImageView) convertView
                        .findViewById(R.id.thumbImage);
                holder.checkbox = (CheckBox) convertView
                        .findViewById(R.id.itemCheckBox);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            ImageItem item = images.get(position);
            holder.checkbox.setId(position);
            holder.imageview.setId(position);
            holder.checkbox.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    CheckBox cb = (CheckBox) v;

                    if (images.get(position).selection){
                        holder.checkbox.setVisibility(View.INVISIBLE);
                        cb.setChecked(false);

                        int id = v.getId();
                        ImageItem item = images.get(id);
                        String path="NA";
                        final String[] columns = { MediaStore.Images.Media.DATA };
                        Cursor imagecursor = getActivity().managedQuery(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
                                MediaStore.Images.Media._ID + " = " + item.id, null, MediaStore.Images.Media._ID);
                        if (imagecursor != null && imagecursor.getCount() > 0){
                            imagecursor.moveToPosition(0);
                            path = imagecursor.getString(imagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                            imagecursor.close();


                        }

                        ((Artifacts)getActivity()).updateSenderList(false,path);
                        images.get(position).selection = false;
                    }
                }
            });
            holder.imageview.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    int id = v.getId();
                    ImageItem item = images.get(id);
                    String path="NA";
                    final String[] columns = { MediaStore.Images.Media.DATA };
                    Cursor imagecursor = getActivity().managedQuery(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
                            MediaStore.Images.Media._ID + " = " + item.id, null, MediaStore.Images.Media._ID);
                    if (imagecursor != null && imagecursor.getCount() > 0){
                        imagecursor.moveToPosition(0);
                         path = imagecursor.getString(imagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                        File file = new File(path);
                        imagecursor.close();




                    }

                    if (images.get(position).selection){
                        holder.checkbox.setVisibility(View.INVISIBLE);
                        holder.checkbox.setChecked(false);
                        images.get(position).selection = false;
                        ((Artifacts)getActivity()).updateSenderList(false,path);
                        imageData.get(position).setselected(false);
                    } else {
                        holder.checkbox.setVisibility(View.VISIBLE);
                        holder.checkbox.setChecked(true);
                        images.get(position).selection = true;



                        ((Artifacts)getActivity()).updateSenderList(true,path);

                        Log.e("IMAGEDATA" , "storagte path " + path);
                    }


                }
            });
            holder.imageview.setImageBitmap(item.img);

            if(images.get(position).selection){
                holder.checkbox.setVisibility(View.VISIBLE);
                holder.checkbox.setChecked(images.get(position).selection);
            }
            else{

                holder.checkbox.setVisibility(View.INVISIBLE);

                holder.checkbox.setChecked(images.get(position).selection);
            }

            holder.checkbox.setChecked(item.selection);
            return convertView;
        }
    }

    class ViewHolder {
        ImageView imageview;
        CheckBox checkbox;
    }

    class ImageItem {
        boolean selection;
        int id;
        Bitmap img;
    }
    public  void getAllShownImagesPath(Activity activity) {
        Uri uri;
        Cursor cursor;
        int column_index,column_index_id;

        String absolutePathOfImage = null;


        uri = MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI;

        String[] proj = { MediaStore.Images.Media.DATA, MediaStore.Images.Thumbnails._ID };

        cursor = activity.getContentResolver().query(uri, proj, null,
                null, null);

        Log.e("IMAGEID" , "cursor count  " + cursor.getCount());
        cursor.moveToFirst();

        column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);

        column_index_id = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails._ID);

        while (cursor.moveToNext()) {
                absolutePathOfImage = cursor.getString(column_index);


            int imageID = cursor.getInt(column_index_id);

            Log.e("IMAGEID" , "id  " + imageID);
            Log.e("IMAGEID" , "path  " + absolutePathOfImage);
            CreateList createList = new CreateList();

            createList.setImage_Path(absolutePathOfImage);
            createList.setImage_ID(imageID);

            imageData.add(createList);
        }

    }

}
