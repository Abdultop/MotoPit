package com.motopit;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

/**
 * Created by abdul on 23/03/17.
 */

public class ImageActivity extends AppCompatActivity {

    static Context context;
    ImageView imageView;
    ImageButton cameraButton;
    private final int CAMERA_REQUEST = 20;
    Bitmap bitmap;

    MenuItem save;
    MenuItem sync;
    MenuItem referesh;
    MenuItem next;

    private Uri uri;

    static String latitude;
    static String longitude;
    static String shop;
    String owner;
    String mobile;
    String landline;
    String hour;
    String days;
    String tubeless;
    String tube;
    String service;
    String water;
    String mobility;
    String mobilityAvail;
    String howlong;
    String normal;
    String superBike;
    String bullet;
    String name;
    String imageName;

    ProgressDialog prgDialog;
    private Handler responseHandler=null;
    SharedPreferences receiverShar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.image_layout);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        context = this;

        imageView = (ImageView)findViewById(R.id.images);
        cameraButton = (ImageButton)findViewById(R.id.camButton);

        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);

        receiverShar = context.getSharedPreferences("Details", Context.MODE_PRIVATE);
        latitude = receiverShar.getString("latitude","");
        longitude = receiverShar.getString("longitude","");
        shop = receiverShar.getString("shop","");
        owner = receiverShar.getString("owner","");
        mobile = receiverShar.getString("mobile","");
        landline = receiverShar.getString("landline","");
        hour = receiverShar.getString("hour","");
        days = receiverShar.getString("days","");
        tubeless = receiverShar.getString("tubeless","");
        tube = receiverShar.getString("tube","");
        service = receiverShar.getString("service","");
        water = receiverShar.getString("water","");
        mobility = receiverShar.getString("mobility","");
        mobilityAvail = receiverShar.getString("mobilityAvail","");
        howlong = receiverShar.getString("howlong","");
        normal = receiverShar.getString("normal","");
        superBike = receiverShar.getString("superBike","");
        bullet = receiverShar.getString("bullet","");
        name = receiverShar.getString("name","");


        // camera butoon event listener
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mScannerView.stopCamera();
                String[] PERMISSIONS = {android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                if (!MapsActivity.hasPermissions(context,PERMISSIONS)){
                    ActivityCompat.requestPermissions((Activity) context, PERMISSIONS, 1);
                } else {
                    Intent photoCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    uri = FileProvider.getUriForFile(ImageActivity.this,BuildConfig.APPLICATION_ID + ".provider",getOutputMediaFile());
                    photoCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    startActivityForResult(photoCaptureIntent, CAMERA_REQUEST);
                }
            }
        });

        setTitle("Image Loader");
    }

    private static File getOutputMediaFile(){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Motopit");

        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                Log.d("Motopit", "failed to create directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageName = "IMG_"+shop.trim()+"_"+latitude+"&"+longitude+"_"+timeStamp + ".jpg";
        SharedPreferences shared = context.getSharedPreferences("Details", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putString("imgName", imageName.trim());
        editor.commit();
        return new File(mediaStorageDir.getPath() + File.separator +imageName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.control, menu);
        next = menu.findItem(R.id.action_next);
        next.setVisible(false);
        save = menu.findItem(R.id.action_save);
        sync = menu.findItem(R.id.action_sync);
        sync.setVisible(false);
        referesh = menu.findItem(R.id.action_refresh);
        referesh.setVisible(false);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the HomeFragment/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
            case R.id.action_save:
                //validation() ;
                /*String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                if(ImageStorage.saveToSdCard(bitmap,"IMG_"+timeStamp+".JPEG",context)){
                    Toast.makeText(context,"Saved Successfully",Toast.LENGTH_SHORT).show();
                }*/
                if(Utility.isNetworkAvailable(context)){
                    callWebServer();
                }else {
                    showAlertDialog();
                }

                break;
            case android.R.id.home:
                Intent in = new Intent(this,AnotherActivity.class);
                in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(in);
                finish();
                return true;


        }
        return super.onOptionsItemSelected(item);

    }

    private void callWebServer() {


        imageName = receiverShar.getString("imgName","");

        if(Utility.isNotNull(imageName)){
            prgDialog.show();
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    postData(latitude, longitude, shop, owner, mobile, landline, hour,days,tubeless,
                            tube,service,water,mobility,mobilityAvail,howlong,normal,superBike,
                            bullet,name,imageName);
                    responseHandler.sendEmptyMessage(0);
                }
            });
            t.start();

            responseHandler = new Handler(){
                public void handleMessage(Message msg)
                {
                    super.handleMessage(msg);
                    try
                    {
                        prgDialog.dismiss();
                        Toast.makeText(context.getApplicationContext(), "Successfully Saved", Toast.LENGTH_LONG).show();
                        AdminFragment.reset();
                        finish();
                        Intent in = new Intent(context,AnotherActivity.class);
                        in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(in);

                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            };
        }else{
            Toast.makeText(context.getApplicationContext(), "Please Take Picture", Toast.LENGTH_LONG).show();
        }
    }

    private void postData(String latitudeStr, String longitudeStr, String shopStr, String ownerStr, String mobileStr,
                          String landlineStr, String hoursStr, String daysStr, String tubelessStr, String tubeStr,
                          String serviceStr, String waterStr, String mobilityStr, String mobilityAvailableStr,
                          String howlongStr, String normalStr, String superBikeStr, String bulletStr,String name,String imgName) {
        HttpClient httpClient = new DefaultHttpClient();
        // replace with your url
        HttpPost httpPost = new HttpPost("https://docs.google.com/forms/d/18HksFXHY9rcoMeR0Nzyo249xYgg6nFoDuHHl_qD1KC0/formResponse");


        //Post Data
        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
        nameValuePair.add(new BasicNameValuePair("entry_1334476336", latitudeStr));
        nameValuePair.add(new BasicNameValuePair("entry_1959995806", longitudeStr));
        nameValuePair.add(new BasicNameValuePair("entry_1295177422", shopStr));
        nameValuePair.add(new BasicNameValuePair("entry_1273251043", ownerStr));
        nameValuePair.add(new BasicNameValuePair("entry_328484333", mobileStr));
        nameValuePair.add(new BasicNameValuePair("entry_367027045", landlineStr));
        nameValuePair.add(new BasicNameValuePair("entry_616810873", hoursStr));
        nameValuePair.add(new BasicNameValuePair("entry_1548603419", daysStr));
        nameValuePair.add(new BasicNameValuePair("entry_460536563", tubelessStr));
        nameValuePair.add(new BasicNameValuePair("entry_1693165769", tubeStr));
        nameValuePair.add(new BasicNameValuePair("entry_1637289014", serviceStr));
        nameValuePair.add(new BasicNameValuePair("entry_1255947466", waterStr));
        nameValuePair.add(new BasicNameValuePair("entry_1629656933", mobilityStr));
        nameValuePair.add(new BasicNameValuePair("entry_1655267250", mobilityAvailableStr));
        nameValuePair.add(new BasicNameValuePair("entry_61468702", howlongStr));
        nameValuePair.add(new BasicNameValuePair("entry_1509053437", normalStr));
        nameValuePair.add(new BasicNameValuePair("entry_1857739354", superBikeStr));
        nameValuePair.add(new BasicNameValuePair("entry_1216948728", bulletStr));
        nameValuePair.add(new BasicNameValuePair("entry_1986388724", imgName));
        nameValuePair.add(new BasicNameValuePair("entry_1252657813", name));
        //Encoding POST data
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
        } catch (UnsupportedEncodingException e) {
            // log exception
            e.printStackTrace();
        }

        //making POST request.
        try {

            HttpResponse response = httpClient.execute(httpPost);
            // write response to log
            Log.d("Http Post Response:", response.toString());
            /*int responseCode = response.getStatusLine().getStatusCode();
            Log.d("Response Code",String.valueOf(responseCode));*/

        } catch (ClientProtocolException e) {
            // Log exception
            e.printStackTrace();
        } catch (IOException e) {
            // Log exception
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("requestCode", String.valueOf(requestCode)+","+"resultCode"+resultCode);
       // Toast.makeText(context,requestCode+"--"+resultCode,Toast.LENGTH_SHORT).show();
        if(CAMERA_REQUEST == requestCode && resultCode == RESULT_OK){
            imageView.setImageURI(uri);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        Intent in = new Intent(this,AnotherActivity.class);
        in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(in);

    }

    public void showAlertDialog(){


        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        // Setting Dialog Title
        alertDialog.setTitle("Connectivity");
        // Setting Dialog Message
        alertDialog.setMessage("Internet not connected... Check your Connection");
        // On pressing Settings button
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                //finish();
                dialog.dismiss();
            }
        });
        // Showing Alert Message
        alertDialog.show();


    }


    @Override
    public void onResume() {
        super.onResume();
    }

}
