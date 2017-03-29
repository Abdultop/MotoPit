package com.motopit;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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
 * Created by abdul on 2/11/16.
 */

public class AdminFragment extends Fragment {
    AutoCompleteTextView name;
    static EditText latitudes;
    static EditText longitudes;
    static EditText shop;
    static EditText owner;
    static EditText mobile;
    static EditText landline;
    static EditText hours;
    static EditText days;
    static EditText tubeless;
    static EditText tube;
    static EditText service;
    static EditText water;
    static RadioGroup mobility;
    static RadioButton mobilityYes;
    static RadioButton mobilityNo;
    static EditText mobilityAvailable;
    static EditText howlong;
    static CheckBox normal;
    static CheckBox superBike;
    static CheckBox bullet;

    MenuItem save;
    MenuItem sync;
    MenuItem referesh;
    MenuItem next;
    LocationManager locationManager;
    // GPSTracker class
    static GPSTracker gps;
    Context context;


    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.admin, container,
                false);
        context = getActivity();
        setHasOptionsMenu(true);
        gps = new GPSTracker(getActivity());
        name = (AutoCompleteTextView)view.findViewById(R.id.nameText);
        latitudes = (EditText)view.findViewById(R.id.latitude);
        longitudes = (EditText)view.findViewById(R.id.longitude);
        shop = (EditText)view.findViewById(R.id.shop);
        owner = (EditText)view.findViewById(R.id.owner);
        mobile = (EditText)view.findViewById(R.id.mobile);
        landline = (EditText)view.findViewById(R.id.landline);
        hours = (EditText)view.findViewById(R.id.workinghours);
        days = (EditText)view.findViewById(R.id.workingdays);
        tubeless = (EditText)view.findViewById(R.id.tubeless);
        tube = (EditText)view.findViewById(R.id.tube);
        service = (EditText)view.findViewById(R.id.service);
        water = (EditText)view.findViewById(R.id.water);
        mobility = (RadioGroup) view.findViewById(R.id.mobility);
        mobilityYes = (RadioButton) view.findViewById(R.id.mobilityY);
        mobilityNo = (RadioButton) view.findViewById(R.id.mobilityN);
        mobilityAvailable = (EditText)view.findViewById(R.id.dayNight);
        howlong = (EditText)view.findViewById(R.id.since);
        normal = (CheckBox)view.findViewById(R.id.normal);
        superBike = (CheckBox)view.findViewById(R.id.superBike);
        bullet = (CheckBox)view.findViewById(R.id.bullet);


        // Get the string array
        final String[] names = getResources().getStringArray(R.array.name);
        // Create the adapter and set it to the AutoCompleteTextView
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, names);
        name.setAdapter(adapter);

        SharedPreferences receiverShar = context.getSharedPreferences("Details", Context.MODE_PRIVATE);
        String Urname = receiverShar.getString("name","");
        name.setText(Urname);


        mobilityYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mobilityAvailable.setVisibility(View.VISIBLE);
                mobilityAvailable.setText("");
            }
        });

        mobilityNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mobilityAvailable.setVisibility(View.GONE);
                mobilityAvailable.setText("No");
            }
        });

        ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if(networkInfo!=null && networkInfo.isConnected()) {
            if (gps.isGPSEnabled && gps.isNetworkEnabled) {
                latitudes.setText(String.valueOf(gps.latitude));
                longitudes.setText(String.valueOf(gps.longitude));

            }else{
                gps.showSettingsAlert();
            }
        }else{
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
            // Setting Dialog Title
            alertDialog.setTitle("Connectivity");
            // Setting Dialog Message
            alertDialog.setMessage("Internet not connected... Check your Connection");
            // On pressing Settings button
            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int which) {
                    getActivity().finish();
                }
            });
            // Showing Alert Message
            alertDialog.show();
        }



        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        inflater.inflate(R.menu.control, menu);
        super.onCreateOptionsMenu(menu, inflater);
        next = menu.findItem(R.id.action_next);
        save = menu.findItem(R.id.action_save);
        save.setVisible(false);
        sync = menu.findItem(R.id.action_sync);
        referesh = menu.findItem(R.id.action_refresh);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
            case R.id.action_next:
                validation();
                break;

            case R.id.action_sync:
                if (gps.isGPSEnabled && gps.isNetworkEnabled) {
                    latitudes.setText(String.valueOf(gps.latitude));
                    longitudes.setText(String.valueOf(gps.longitude));

                }else{
                    gps.showSettingsAlert();
                }
                break;

            case R.id.action_refresh:
                reset();
                break;

            case android.R.id.home:
                getActivity().finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void validation() {
        final String latitudeStr = latitudes.getText().toString();
        final String longitudeStr = longitudes.getText().toString();
        final String shopStr = shop.getText().toString();
        final String ownerStr = owner.getText().toString();
        final String mobileStr = mobile.getText().toString();
        final String landlineStr = landline.getText().toString();
        final String hoursStr = hours.getText().toString();
        final String daysStr = days.getText().toString();
        final String tubelessStr = tubeless.getText().toString();
        final String tubeStr = tube.getText().toString();
        final String serviceStr = service.getText().toString();
        final String waterStr = water.getText().toString();
        final String mobilityStr;
        int selectRadio = mobility.getCheckedRadioButtonId();
        if(selectRadio == R.id.mobilityY)
        {
            mobilityStr = "Yes";
        }
        else{
            mobilityStr = "No";
        }
        final String mobilityAvailableStr = mobilityAvailable.getText().toString();
        final String howlongStr = howlong.getText().toString();
        final String normalStr;
        if(normal.isChecked()){normalStr = "Yes";}else {normalStr = "No";}
        final String superBikeStr;
        if(superBike.isChecked()){superBikeStr="Yes";}else {superBikeStr = "No";}
        final String bulletStr;
        if(bullet.isChecked()){bulletStr="Yes";}else {bulletStr = "No";}

        if (Utility.isNotNull(latitudeStr) && Utility.isNotNull(longitudeStr) && Utility.isNotNull(shopStr) &&
                Utility.isNotNull(ownerStr) && Utility.isNotNull(mobileStr) && Utility.isNotNull(landlineStr) &&
                Utility.isNotNull(hoursStr) && Utility.isNotNull(daysStr) && Utility.isNotNull(tubelessStr) &&
                Utility.isNotNull(tubeStr) && Utility.isNotNull(serviceStr) && Utility.isNotNull(waterStr) &&
                Utility.isNotNull(mobilityStr) && Utility.isNotNull(mobilityAvailableStr) && Utility.isNotNull(howlongStr) &&
                Utility.isNotNull(name.getText().toString())){

            SharedPreferences shared = context.getSharedPreferences("Details", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = shared.edit();
            editor.putString("latitude", latitudeStr);
            editor.putString("longitude",longitudeStr);
            editor.putString("shop",shopStr);
            editor.putString("owner",ownerStr);
            editor.putString("mobile",mobileStr);
            editor.putString("landline",landlineStr);
            editor.putString("hour",hoursStr);
            editor.putString("days",daysStr);
            editor.putString("tubeless",tubelessStr);
            editor.putString("tube",tubeStr);
            editor.putString("service",serviceStr);
            editor.putString("water",waterStr);
            editor.putString("mobility",mobilityStr);
            editor.putString("mobilityAvail",mobilityAvailableStr);
            editor.putString("howlong",howlongStr);
            editor.putString("normal",normalStr);
            editor.putString("superBike",superBikeStr);
            editor.putString("bullet",bulletStr);
            editor.putString("name",name.getText().toString());
            editor.commit();

            Intent adminImage = new Intent(context,ImageActivity.class);
            adminImage.putExtra("item","Image");
            startActivity(adminImage);

        }else {
            Toast.makeText(getActivity().getApplicationContext(), "Please Fill all Fields", Toast.LENGTH_LONG).show();
        }
    }

    public static void reset() {
        if (gps.isGPSEnabled && gps.isNetworkEnabled) {
            latitudes.setText(String.valueOf(gps.latitude));
            longitudes.setText(String.valueOf(gps.longitude));

        }else{
            gps.showSettingsAlert();
        }
        shop.setText("");
        owner.setText("");
        mobile.setText("");
        landline.setText("");
        hours.setText("");
        days.setText("");
        tubeless.setText("");
        tube.setText("");
        service.setText("");
        water.setText("");
        mobility.clearCheck();
        mobilityAvailable.setText("");
        mobilityAvailable.setVisibility(View.GONE);
        howlong.setText("");
        normal.setChecked(true);
        superBike.setChecked(false);
        bullet.setChecked(false);
    }


}
