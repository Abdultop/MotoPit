package com.motopit;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by abdul on 13/04/17.
 */

public class ImageGridAdapter extends BaseAdapter {

    Context context;
    List<ImageItem> imageList = new ArrayList<ImageItem>();

    public ImageGridAdapter(Context context,List<ImageItem> imageList){
        this.context = context;
        this.imageList = imageList;
    }
    @Override
    public int getCount() {
        return imageList.size();
    }

    @Override
    public Object getItem(int position) {
        return imageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) ((Activity)context).getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.image_item,parent,false);
            holder = new ViewHolder();

            holder.image = (ImageView) view.findViewById(R.id.images);

            view.setTag(holder);
        }else{
            holder=(ViewHolder)view.getTag();
        }
        File fs = null;
        Bitmap bm;
        fs = new File(imageList.get(position).getImageUri());

        if(fs!=null) {
            //bm= BitmapFactory.decodeFileDescriptor(fs.getFD(), null, bfOptions);
            bm = BitmapFactory.decodeFile(fs.getAbsolutePath());
            holder.image.setImageBitmap(bm);
            /*imageView.setId(position);
            imageView.setLayoutParams(new GridView.LayoutParams(200, 160));*/
        }
        //holder.image.setImageURI(imageList.get(position).getImageUri());

        return view;
    }

    public static class ViewHolder{

        public ImageView image;


    }
}
