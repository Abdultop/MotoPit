package com.MotoPit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,LocationListener,NavigationView.OnNavigationItemSelectedListener {

    private GoogleMap mMap;
    public static Boolean permissionFlag = false;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    LocationManager locationManager;
    RelativeLayout mainCoordinatorLayout;
    FloatingActionButton navButton;
    // GPSTracker class
    GPSTracker gps;

    LinearLayout optionLayout;
    RelativeLayout transLayout;
    LinearLayout radioLayout;
    ImageView infinity;
    TextView infinityText;
    ImageView puncture;
    TextView punctureText;
    ImageView breakdown;
    TextView breakText;
    ImageView petrol;
    TextView petrolText;
    ImageView general;
    TextView generalText;

    TextView tube;
    TextView tubeLess;
    private boolean doubleBackToExitPressedOnce = false;
    private boolean isPanel;
    Context context;

    private ViewGroup infoWindow;
    private TextView infoTitle;
    private ImageView infoButton;

    DBHelper mydb;
    ArrayList<String> strArr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        //show error dialog if GoolglePlayServices not available
        if (!isGooglePlayServicesAvailable()) {
            return;
        }
        setContentView(R.layout.activity_maps);
        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/
        gps = new GPSTracker(MapsActivity.this);

        isPanel = false;
        mydb = new DBHelper(this);
        //mydb.clearTable("vendor");
        optionLayout = (LinearLayout)findViewById(R.id.option);
        transLayout = (RelativeLayout) findViewById(R.id.trans);
        radioLayout = (LinearLayout) findViewById(R.id.optionHeader);
        infinity = (ImageView) findViewById(R.id.infinity);
        infinityText = (TextView) findViewById(R.id.infinityText);
        puncture = (ImageView) findViewById(R.id.puncture);
        punctureText = (TextView) findViewById(R.id.punctureText);
        breakdown = (ImageView) findViewById(R.id.breakdown);
        breakText = (TextView) findViewById(R.id.breakdownText);
        petrol = (ImageView) findViewById(R.id.petrol);
        petrolText = (TextView) findViewById(R.id.petrolText);
        general = (ImageView) findViewById(R.id.general);
        generalText = (TextView) findViewById(R.id.generalText);

        tube = (TextView) findViewById(R.id.tube);
        tubeLess = (TextView) findViewById(R.id.tubeLess);
        navButton = (FloatingActionButton)findViewById(R.id.fab);
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if(networkInfo!=null && networkInfo.isConnected()) {
            getDataFromServer();
        }else{
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            // Setting Dialog Title
            alertDialog.setTitle("Connectivity");
            // Setting Dialog Message
            alertDialog.setMessage("Internet not connected... Check your Connection");
            // On pressing Settings button
            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int which) {
                    finish();
                }
            });
            // Showing Alert Message
            alertDialog.show();
        }

        String[] PERMISSIONS = {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION};
        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_ID_MULTIPLE_PERMISSIONS);
        }else {
            permissionFlag = true;
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mMap = mapFragment.getMap();
            mapFragment.getMapAsync(this);
            mainCoordinatorLayout = (RelativeLayout) findViewById(R.id.mainLayout);
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                gps.showSettingsAlert();
            }

        }

        this.infoWindow = (ViewGroup) getLayoutInflater().inflate(R.layout.custominfowindow, null);
        this.infoTitle = (TextView) infoWindow.findViewById(R.id.title);
        this.infoButton = (ImageView) infoWindow.findViewById(R.id.button);



        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                infoTitle.setText(marker.getTitle());
                //infoSnippet.setText(marker.getSnippet());
                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?" +
                "saddr="+ gps.latitude + "," + gps.longitude + "&daddr=" + marker.getPosition().latitude + "," +
                marker.getPosition().longitude+ "&sensor=false&units=metric&mode=driving"));
                intent.setClassName("com.google.android.apps.maps","com.google.android.maps.MapsActivity");
                startActivity(intent);
                    }
                });

                return infoWindow;
            }
        });

        strArr = mydb.getAllDetails();

        infinity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                infinity.setImageResource(R.drawable.infin_on);
                infinityText.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                puncture.setImageResource(R.drawable.punc_off);
                punctureText.setTextColor(ContextCompat.getColor(context, R.color.hint));
                breakdown.setImageResource(R.drawable.break_off);
                breakText.setTextColor(ContextCompat.getColor(context, R.color.hint));
                petrol.setImageResource(R.drawable.petrol_off);
                petrolText.setTextColor(ContextCompat.getColor(context, R.color.hint));
                general.setImageResource(R.drawable.general_off);
                generalText.setTextColor(ContextCompat.getColor(context, R.color.hint));
                transLayout.setVisibility(View.GONE);
                if(isPanel){
                    slideUpDown(view,false);
                }
                placeMarkersOnSearchPlace(strArr);

            }
        });

        puncture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                puncture.setImageResource(R.drawable.punc_on);
                punctureText.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                infinity.setImageResource(R.drawable.infin_off);
                infinityText.setTextColor(ContextCompat.getColor(context, R.color.hint));
                breakdown.setImageResource(R.drawable.break_off);
                breakText.setTextColor(ContextCompat.getColor(context, R.color.hint));
                petrol.setImageResource(R.drawable.petrol_off);
                petrolText.setTextColor(ContextCompat.getColor(context, R.color.hint));
                general.setImageResource(R.drawable.general_off);
                generalText.setTextColor(ContextCompat.getColor(context, R.color.hint));
                transLayout.setVisibility(View.VISIBLE);
                radioLayout.setVisibility(View.VISIBLE);
                slideUpDown(view,true);
            }
        });

        breakdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                breakdown.setImageResource(R.drawable.break_on);
                breakText.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                infinity.setImageResource(R.drawable.infin_off);
                infinityText.setTextColor(ContextCompat.getColor(context, R.color.hint));
                puncture.setImageResource(R.drawable.punc_off);
                punctureText.setTextColor(ContextCompat.getColor(context, R.color.hint));
                petrol.setImageResource(R.drawable.petrol_off);
                petrolText.setTextColor(ContextCompat.getColor(context, R.color.hint));
                general.setImageResource(R.drawable.general_off);
                generalText.setTextColor(ContextCompat.getColor(context, R.color.hint));
                transLayout.setVisibility(View.GONE);
                if(isPanel){
                    slideUpDown(view,false);
                }
            }
        });

        petrol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                petrol.setImageResource(R.drawable.petrol_on);
                petrolText.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                infinity.setImageResource(R.drawable.infin_off);
                infinityText.setTextColor(ContextCompat.getColor(context, R.color.hint));
                puncture.setImageResource(R.drawable.punc_off);
                punctureText.setTextColor(ContextCompat.getColor(context, R.color.hint));
                breakdown.setImageResource(R.drawable.break_off);
                breakText.setTextColor(ContextCompat.getColor(context, R.color.hint));
                general.setImageResource(R.drawable.general_off);
                generalText.setTextColor(ContextCompat.getColor(context, R.color.hint));
                transLayout.setVisibility(View.GONE);
                if(isPanel){
                    slideUpDown(view,false);
                }
                loadNearByPlaces(gps.getLatitude(),gps.getLongitude());
            }
        });

        general.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                general.setImageResource(R.drawable.general_on);
                generalText.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                infinity.setImageResource(R.drawable.infin_off);
                puncture.setImageResource(R.drawable.punc_off);
                breakdown.setImageResource(R.drawable.break_off);
                petrol.setImageResource(R.drawable.petrol_off);
                transLayout.setVisibility(View.GONE);
                if(isPanel){
                    slideUpDown(view,false);
                }
            }
        });

        tube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tube.setBackground(ContextCompat.getDrawable(context,R.drawable.button_shape));
                tube.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
                tubeLess.setBackground(ContextCompat.getDrawable(context,R.drawable.border));
                tubeLess.setTextColor(ContextCompat.getColor(context, R.color.hint));
            }
        });

        tubeLess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tubeLess.setBackground(ContextCompat.getDrawable(context,R.drawable.button_shape));
                tubeLess.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
                tube.setBackground(ContextCompat.getDrawable(context,R.drawable.border));
                tube.setTextColor(ContextCompat.getColor(context, R.color.hint));
            }
        });

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.openDrawer(Gravity.LEFT);
            }
        });
        /*ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();*/

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


    }

    //AR 1-Nov-16 For Place Marker on search result palces
    private void placeMarkersOnSearchPlace(ArrayList<String> strArr) {
        mMap.clear();
        for(int i=0; i<strArr.size();i++) {

            MarkerOptions mp = new MarkerOptions();
            mp.position(new LatLng(gps.getLatitude(), gps.getLongitude()));
            mp.title("my position");
            mp.icon(getMarkerIcon("#2579BE"));
            mMap.addMarker(mp);

            String split[] = strArr.get(i).split("\\,");
            if(within10km(gps.getLatitude(),gps.getLongitude(),Double.parseDouble(split[0]), Double.parseDouble(split[1]))){
                LatLng latLng = new LatLng(Double.parseDouble(split[0]), Double.parseDouble(split[1]));
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(split[2]);
                markerOptions.snippet("and snippet")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));

                mMap.addMarker(markerOptions);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
            }

        }
    }
    //AR 1-NOV-16 Within KM Method
    private Boolean within10km(double mylatitude, double mylongitude, double testLatitude, double testLongitude){
        float[] results = new float[1];
        Location.distanceBetween(mylatitude, mylongitude, testLatitude, testLongitude, results);
        float distanceInMeters = results[0];
        return distanceInMeters < 10000;
    }

    //AR 1-Nov-16 Get Data From Server DB
    public void getDataFromServer() {
        new DownloadWebpageTask(new AsyncResult() {
            @Override
            public void onResult(JSONObject object) {
                processJson(object);
            }
        }).execute("https://spreadsheets.google.com/tq?key=1ChEcvQaVxbXhBFY3AkKpbiwW05iQds8BYa816DNcVQg");

    }

    private void processJson(JSONObject object) {
        try {
            JSONArray rows = object.getJSONArray("rows");

            for (int r = 0; r < rows.length(); r++) {
                JSONObject row = rows.getJSONObject(r);
                JSONArray columns = row.getJSONArray("c");

                String areaName = columns.getJSONObject(2).getString("v");
                String latitude = columns.getJSONObject(3).getString("f");
                String longitude = columns.getJSONObject(4).getString("f");
                String activeFlag = columns.getJSONObject(16).getString("v");
                if(mydb.insertVendor(areaName,latitude,longitude,activeFlag)){
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Method for Hide/Show Tube(or)Tubless layout
    public void slideUpDown(View view, Boolean isPanelShown) {
        if (isPanelShown) {
            // Show the panel
            Animation bottomUp = AnimationUtils.loadAnimation(this,R.anim.bottom_up);
            radioLayout.startAnimation(bottomUp);
            radioLayout.setVisibility(View.VISIBLE);
            isPanel = true;
        }
        else {
            // Hide the Panel
            Animation bottomDown = AnimationUtils.loadAnimation(this,R.anim.bottom_down);
            radioLayout.startAnimation(bottomDown);
            radioLayout.setVisibility(View.GONE);
            isPanel = false;
        }
    }

    private void showLocationSettings() {
        Snackbar snackbar = Snackbar
                .make(mainCoordinatorLayout, "Location Error: GPS Disabled!",
                        Snackbar.LENGTH_LONG)
                .setAction("Enable", new View.OnClickListener() {
                    @Override                    public void onClick(View v) {

                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                });
        snackbar.setActionTextColor(Color.RED);
        snackbar.setDuration(Snackbar.LENGTH_INDEFINITE);

        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView
                .findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);

        snackbar.show();
    }



    /*private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Goto Settings Page To Enable GPS",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                        finish();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();

    }*/



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        showCurrentLocation();

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition pos) {

            }
        });


        /*View v = getSupportFragmentManager().findFragmentById(R.id.map).getView();
        v.setAlpha(0.15f); // Change this value to set the desired alpha*/

    /*    // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/


    }


    private void showCurrentLocation() {
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = gps.getLocation();
        if (location != null) {
            onLocationChanged(location);
        }
        /*if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 20000, 0, this);
        }
       locationManager.requestLocationUpdates(bestProvider, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);*/

    }

    private void loadNearByPlaces(double latitude, double longitude) {
//YOU Can change this type at your own will, e.g hospital, cafe, restaurant.... and see how it all works
        String type = "gas_station";
        StringBuilder googlePlacesUrl =
                new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=").append(latitude).append(",").append(longitude);
        googlePlacesUrl.append("&radius=").append(AppConfig.PROXIMITY_RADIUS);
        googlePlacesUrl.append("&types=").append(type);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + AppConfig.GOOGLE_BROWSER_API_KEY);
        JsonObjectRequest request = new JsonObjectRequest(googlePlacesUrl.toString(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject result) {
                        parseLocationResult(result);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(AppConfig.TAG, "onErrorResponse: Error= " + error);
                        Log.e(AppConfig.TAG, "onErrorResponse: Error= " + error.getMessage());
                    }
                });

        AppController.getInstance().addToRequestQueue(request);
    }


    private void parseLocationResult(JSONObject result) {

        String id, place_id, placeName = null, reference, icon, vicinity = null;
        double latitude, longitude;

        try {
            JSONArray jsonArray = result.getJSONArray("results");

            if (result.getString(AppConfig.STATUS).equalsIgnoreCase(AppConfig.OK)) {

                mMap.clear();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject place = jsonArray.getJSONObject(i);

                    id = place.getString(AppConfig.TYPE_ID);
                    place_id = place.getString(AppConfig.PLACE_ID);
                    if (!place.isNull(AppConfig.NAME)) {
                        placeName = place.getString(AppConfig.NAME);
                    }
                    if (!place.isNull(AppConfig.VICINITY)) {
                        vicinity = place.getString(AppConfig.VICINITY);
                    }
                    latitude = place.getJSONObject(AppConfig.GEOMETRY).getJSONObject(AppConfig.LOCATION)
                            .getDouble(AppConfig.LATITUDE);
                    longitude = place.getJSONObject(AppConfig.GEOMETRY).getJSONObject(AppConfig.LOCATION)
                            .getDouble(AppConfig.LONGITUDE);
                    reference = place.getString(AppConfig.REFERENCE);
                    icon = place.getString(AppConfig.ICON);

                    //BitmapDescriptor bitmapDescriptor  = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);


                    MarkerOptions mp = new MarkerOptions();
                    mp.position(new LatLng(gps.getLatitude(), gps.getLongitude()));
                    mp.title("my position");
                    mp.icon(getMarkerIcon("#2579BE"));
                    mMap.addMarker(mp);

                    MarkerOptions markerOptions = new MarkerOptions();
                    LatLng latLng = new LatLng(latitude, longitude);
                    markerOptions.position(latLng);
                    markerOptions.title(placeName + " : " + vicinity);
                    markerOptions.snippet("and snippet")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.pumps));

                    mMap.addMarker(markerOptions);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(13));

                }

                Toast.makeText(getBaseContext(), jsonArray.length() + "location found!",
                        Toast.LENGTH_LONG).show();
            } else if (result.getString(AppConfig.STATUS).equalsIgnoreCase(AppConfig.ZERO_RESULTS)) {
                Toast.makeText(getBaseContext(), "No location found in 5KM radius!!!",
                        Toast.LENGTH_LONG).show();
            }

        } catch (JSONException e) {

            e.printStackTrace();
            Log.e(AppConfig.TAG, "parseLocationResult: Error=" + e.getMessage());
        }
    }

    private BitmapDescriptor getMarkerIcon(String color) {
        float[] hsv = new float[3];
        Color.colorToHSV(Color.parseColor(color), hsv);
        return BitmapDescriptorFactory.defaultMarker(hsv[0]);
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        LatLng latLng = new LatLng(latitude, longitude);
        MarkerOptions mp = new MarkerOptions();
        mp.position(new LatLng(location.getLatitude(), location.getLongitude()));
        mp.title("my position");
        mMap.addMarker(mp);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        String addresses = getCompleteAddressString(location.getLatitude(),location.getLongitude());
      /*  locationTv.setText("Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude()+
                            System.getProperty("line.separator")+
                            addresses);*/
                /*TextView locationTv = (TextView) findViewById(R.id.latlongLocation);
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(latLng));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        locationTv.setText("Latitude:" + latitude + ", Longitude:" + longitude);*/
    }


    private String getCompleteAddressString(double latitude, double longitude) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
               // Log.w("My Current loction addr", strReturnedAddress.toString());
            } else {
                Log.w("My Current loction addr", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("My Current loction addr", "Canont get Address!");
        }
        return strAdd;
    }


    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, AppConfig.PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(AppConfig.TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /*public void onResume(){
        super.onResume();
        String[] PERMISSIONS = {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION};
        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_ID_MULTIPLE_PERMISSIONS);
        }else {
            permissionFlag = true;

            // Spinner spinner = (Spinner) findViewById(R.id.spinner);
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            //  mapFragment.getView().setBackgroundResource(R.color.transparent);
            //   mapFragment.getView().setAlpha(0.10f);
            mMap = mapFragment.getMap();
            // mapFragment.getMapAsync(this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                mMap.setMyLocationEnabled(true);
                return;
            }
            mMap.setMyLocationEnabled(true);
            if(gps.isGPSEnabled && gps.isNetworkEnabled){
                if(gps.canGetLocation()){
                    Location location = gps.getLocation();
                    if (location != null) {
                        onLocationChanged(location);
                    }
                }
            }else {
                gps.showSettingsAlert();
            }
           *//* LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String bestProvider = locationManager.getBestProvider(criteria, true);
            Location location = locationManager.getLastKnownLocation(bestProvider);
            Log.d("location", String.valueOf(location));
            if (location != null) {
                onLocationChanged(location);
            }
        *//**//*if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 20000, 0, this);
        }else*//**//* if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 20000, 0, this);
            }else{
                showGPSDisabledAlertToUser();
            }*//*

       *//* List<String> categories = new ArrayList<String>();
        categories.add("Automobile/Bike Service");
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);*//*


        }
        //show error dialog if GoolglePlayServices not available
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
    }*/

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }else if(doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;

        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

    }
}