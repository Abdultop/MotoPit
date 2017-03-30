package com.motopit;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.support.v4.app.Fragment;
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
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by abdul on 2/11/16.
 */

public class MapFragment extends Fragment implements OnMapReadyCallback,LocationListener{

    private SupportMapFragment mapFragment;
    private GoogleMap mMap;
    public static Boolean permissionFlag = false;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    LocationManager locationManager;
    RelativeLayout mainCoordinatorLayout;

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
    private boolean isPanel;
    Context context;

    private ViewGroup infoWindow;
    private TextView infoTitle;
    private ImageView infoButton;

    DBHelper mydb;
    ArrayList<String> strArr;

    public Boolean navigationFlag = false;
    Circle shape;
    private static View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.content, container, false);
        } catch (InflateException e) {
        /* map is already there, just return view as it is */
        }
        gps = new GPSTracker(getActivity());
        context =getActivity();
        isPanel = false;
        mydb = new DBHelper(getActivity());
        //mydb.clearTable("vendor");
        optionLayout = (LinearLayout)view.findViewById(R.id.option);
        transLayout = (RelativeLayout) view.findViewById(R.id.trans);
        radioLayout = (LinearLayout)view. findViewById(R.id.optionHeader);
        infinity = (ImageView) view.findViewById(R.id.infinity);
        infinityText = (TextView)view.findViewById(R.id.infinityText);
        puncture = (ImageView)view. findViewById(R.id.puncture);
        punctureText = (TextView) view.findViewById(R.id.punctureText);
        breakdown = (ImageView) view.findViewById(R.id.breakdown);
        breakText = (TextView)view.findViewById(R.id.breakdownText);
        petrol = (ImageView) view.findViewById(R.id.petrol);
        petrolText = (TextView) view.findViewById(R.id.petrolText);
        general = (ImageView) view.findViewById(R.id.general);
        generalText = (TextView) view.findViewById(R.id.generalText);

        tube = (TextView) view.findViewById(R.id.tube);
        tubeLess = (TextView) view.findViewById(R.id.tubeLess);

        String[] PERMISSIONS = {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION};
        if(!hasPermissions(getActivity(), PERMISSIONS)){
            ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, REQUEST_ID_MULTIPLE_PERMISSIONS);
        }else {
            permissionFlag = true;
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            FragmentManager fm = getChildFragmentManager();
            mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
            if (mapFragment == null) {
                mapFragment = SupportMapFragment.newInstance();
                fm.beginTransaction().replace(R.id.map, mapFragment).commit();
            }
           // mMap = mapFragment.getMap();
            mapFragment.getMapAsync(this);
            mainCoordinatorLayout = (RelativeLayout) view.findViewById(R.id.mainLayout);
            locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);

            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                gps.showSettingsAlert();
            }

        }

        this.infoWindow = (ViewGroup) getActivity().getLayoutInflater().inflate(R.layout.custominfowindow, null);
        this.infoTitle = (TextView) infoWindow.findViewById(R.id.title);
        this.infoButton = (ImageView) infoWindow.findViewById(R.id.button);

        strArr = mydb.getAllDetails();

        if(Utility.isNetworkAvailable(context)){
            getDataFromServer();
        }else {
            showAlertDialog(Utility.isNetworkAvailable(context));
        }

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
                navigationFlag = false;

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
                navigationFlag = false;
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
                navigationFlag = false;
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
                navigationFlag = true;
                loadNearByPlaces(gps.getLatitude(),gps.getLongitude());
            }
        });

        general.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                general.setImageResource(R.drawable.general_on);
                generalText.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                infinity.setImageResource(R.drawable.infin_off);
                infinityText.setTextColor(ContextCompat.getColor(context, R.color.hint));
                puncture.setImageResource(R.drawable.punc_off);
                punctureText.setTextColor(ContextCompat.getColor(context, R.color.hint));
                breakdown.setImageResource(R.drawable.break_off);
                breakText.setTextColor(ContextCompat.getColor(context, R.color.hint));
                petrol.setImageResource(R.drawable.petrol_off);
                petrolText.setTextColor(ContextCompat.getColor(context, R.color.hint));
                transLayout.setVisibility(View.GONE);
                if(isPanel){
                    slideUpDown(view,false);
                }
                navigationFlag = false;
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
        return view;
    }

    //AR 1-NOV-16 Within KM Method
    private Boolean within10km(double mylatitude, double mylongitude, double testLatitude, double testLongitude, int km){
        float[] results = new float[1];
        Location.distanceBetween(mylatitude, mylongitude, testLatitude, testLongitude, results);
        float distanceInMeters = results[0];
        return distanceInMeters < km; //10km(10000m)
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
            Animation bottomUp = AnimationUtils.loadAnimation(getActivity(),R.anim.bottom_up);
            radioLayout.startAnimation(bottomUp);
            radioLayout.setVisibility(View.VISIBLE);
            isPanel = true;
        }
        else {
            // Hide the Panel
            Animation bottomDown = AnimationUtils.loadAnimation(getActivity(),R.anim.bottom_down);
            radioLayout.startAnimation(bottomDown);
            radioLayout.setVisibility(View.GONE);
            isPanel = false;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        LatLng latLng = new LatLng(latitude, longitude);
        MarkerOptions mp = new MarkerOptions();
        mp.position(new LatLng(location.getLatitude(), location.getLongitude()));
        mp.title("my position");
        mMap.addMarker(mp);
        shape = drawCircle(latLng);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
        locationManager.removeUpdates(this);

       // String addresses = getCompleteAddressString(location.getLatitude(),location.getLongitude());
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

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        //mMap.getUiSettings().setZoomControlsEnabled(true);


        showCurrentLocation();

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition pos) {

            }
        });

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
                        if(navigationFlag){
                            final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?" +
                                    "saddr="+ gps.latitude + "," + gps.longitude + "&daddr=" + marker.getPosition().latitude + "," +
                                    marker.getPosition().longitude+ "&sensor=false&units=metric&mode=driving"));
                            intent.setClassName("com.google.android.apps.maps","com.google.android.maps.MapsActivity");
                            startActivity(intent);
                        }else {
                            Intent admin = new Intent(getActivity(),ShopActivity.class);
                            admin.putExtra("Shopname",marker.getTitle());
                            startActivity(admin);
                        }
                    }
                });

                return infoWindow;
            }
        });
        /*View v = getSupportFragmentManager().findFragmentById(R.id.map).getView();
        v.setAlpha(0.15f); // Change this value to set the desired alpha*/

    /*    // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/



    }

    //AR 1-Nov-16 For Place Marker on search result palces
    private void placeMarkersOnSearchPlace(ArrayList<String> strArr) {
        mMap.clear();
        LatLng l1 = new LatLng(gps.getLatitude(), gps.getLongitude());
        shape = drawCircle(l1);
        Log.d("Array Size", String.valueOf(strArr.size()));
        for(int i=0; i<strArr.size();i++) {

            MarkerOptions mp = new MarkerOptions();
            mp.position(l1);
            mp.title("my position");
            mp.icon(getMarkerIcon("#2579BE"));
            mMap.addMarker(mp);

            String split[] = strArr.get(i).split("\\,");
            if(within10km(gps.getLatitude(),gps.getLongitude(),Double.parseDouble(split[0]), Double.parseDouble(split[1]),5000)){
                LatLng latLng = new LatLng(Double.parseDouble(split[0]), Double.parseDouble(split[1]));
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(split[2]);
                markerOptions.snippet("and snippet")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.pus));
                mMap.addMarker(markerOptions);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
            }

        }
    }

    private Circle drawCircle(LatLng latLng) {
        CircleOptions option = new CircleOptions()
                .center(latLng)
                .radius(AppConfig.PROXIMITY_RADIUS)
                .strokeWidth(2)
                .strokeColor(Color.RED)
        // Fill color of the circle
        // 0x represents, this is an hexadecimal code
        // 55 represents percentage of transparency. For 100% transparency, specify 00.
        // For 0% transparency ( ie, opaque ) , specify ff
        // The remaining 6 characters(00ff00) specify the fill color
                .fillColor(ContextCompat.getColor(context,R.color.radius));

        return mMap.addCircle(option);
    }

    private void showCurrentLocation() {
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
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
                shape = drawCircle(new LatLng(gps.getLatitude(), gps.getLongitude()));
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
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.mapicon));

                    mMap.addMarker(markerOptions);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(12));

                }

                Toast.makeText(getActivity().getBaseContext(), jsonArray.length() + "location found!",
                        Toast.LENGTH_LONG).show();
            } else if (result.getString(AppConfig.STATUS).equalsIgnoreCase(AppConfig.ZERO_RESULTS)) {
                Toast.makeText(getActivity().getBaseContext(), "No location found in 5KM radius!!!",
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

    private String getCompleteAddressString(double latitude, double longitude) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
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

    public void showAlertDialog(boolean isConnected){

        if(!isConnected){
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
            // Setting Dialog Title
            alertDialog.setTitle("Connectivity");
            // Setting Dialog Message
            alertDialog.setMessage("Internet not connected... Check your Connection");
            // On pressing Settings button
            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int which) {
                    getActivity().finish();
                    dialog.dismiss();
                }
            });
            // Showing Alert Message
            alertDialog.show();
        }

    }
}
