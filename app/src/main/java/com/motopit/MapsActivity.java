package com.motopit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;

import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;


public class MapsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private boolean doubleBackToExitPressedOnce = false;
    Context context;
    FloatingActionButton navButton;
    public static Boolean permissionFlag = false;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    SharedPreferences pref;

    ImageView userPic;
    TextView userName;
    TextView userEmail;

    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 007;
    public static DrawerLayout drawer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        context = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        pref = getSharedPreferences("signedup", Context.MODE_PRIVATE);
        navButton = (FloatingActionButton)findViewById(R.id.fab);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);


        //show error dialog if GoolglePlayServices not available
        if (!isGooglePlayServicesAvailable()) {
            return;
        }
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if(networkInfo==null && !networkInfo.isConnected()) {

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
        }else{
            String[] PERMISSIONS = {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION};
            if(!hasPermissions(this, PERMISSIONS)){
                ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_ID_MULTIPLE_PERMISSIONS);
            }else {
                permissionFlag = true;
                if (pref.getInt(getString(R.string.signedupflag),0) == 0) {
                    getSupportActionBar().hide();
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    userName = (TextView)findViewById(R.id.GuserName);
                    userEmail = (TextView)findViewById(R.id.GuserEmail);
                    userPic = (ImageView)findViewById(R.id.Gprofileimage);
                    android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    LoginFragment fragment = new LoginFragment();
                    transaction.replace(R.id.content_frame, fragment);
                    transaction.commit();
                }else{
                    getSupportActionBar().hide();
                    navButton.show();
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    MapFragment fragment = new MapFragment();
                    transaction.replace(R.id.content_frame, fragment);
                    transaction.commit();

                }
            }
        }
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
        if(pref.getInt(getString(R.string.signedupflag), 0) == 1){      //8-11-16 AR for show user name,email and pic
            View header = navigationView.getHeaderView(0);
            userName = (TextView)header.findViewById(R.id.GuserName);
            userEmail = (TextView)header.findViewById(R.id.GuserEmail);
            userPic = (ImageView)header.findViewById(R.id.Gprofileimage);

            String strUserName = pref.getString("username", "");
            String strUserEmail = pref.getString("useremail", "");
            String strUserPic = pref.getString("photo", "");
            Log.d("UserName",strUserName);
            if(Utility.isNotNull(strUserName)){
                userName.setText(strUserName);
                userEmail.setText(strUserEmail);
                Glide.with(context).load(strUserPic).asBitmap().centerCrop().into(new BitmapImageViewTarget(userPic) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        userPic.setImageDrawable(circularBitmapDrawable);
                    }
                });
            }
        }
        navigationView.setNavigationItemSelectedListener(this);

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
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the HomeFragment/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            Intent admin = new Intent(this,AnotherActivity.class);
            admin.putExtra("item","Admin");
            startActivity(admin);
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.logout) {
           logout();
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                // .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mGoogleApiClient.connect();
        mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {

                FirebaseAuth.getInstance().signOut();
                if(mGoogleApiClient.isConnected()) {
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                                navButton.hide();
                                getSupportActionBar().hide();
                                SharedPreferences pref = getSharedPreferences("signedup",Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = pref.edit();
                                editor.putInt(getString(R.string.signedupflag), 0);
                                editor.clear();
                                editor.commit();

                                android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                                LoginFragment fragment = new LoginFragment();
                                transaction.replace(R.id.content_frame, fragment);
                                transaction.commit();
                            }
                        }
                    });
                }
            }

            @Override
            public void onConnectionSuspended(int i) {
                Log.d("Connection", "Google API Client Connection Suspended");
            }
        });

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
    public void navigatetoMapActivity(String personPhotoUrl, String personName, String email){

        if (pref.getInt(getString(R.string.signedupflag),0) == 0) {
            getSupportActionBar().hide();
            android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            LoginFragment fragment = new LoginFragment();
            transaction.replace(R.id.content_frame, fragment);
            transaction.commit();

        } else {
            userPic = (ImageView)findViewById(R.id.Gprofileimage);
            userName = (TextView)findViewById(R.id.GuserName);
            userEmail = (TextView)findViewById(R.id.GuserEmail);

            userName.setText(personName);
            userEmail.setText(email);
            Glide.with(context).load(personPhotoUrl).asBitmap().centerCrop().into(new BitmapImageViewTarget(userPic) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable =
                            RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                    circularBitmapDrawable.setCircular(true);
                    userPic.setImageDrawable(circularBitmapDrawable);
                }
            });
            navButton.show();
            android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            MapFragment fragment = new MapFragment();
            transaction.replace(R.id.content_frame, fragment);
            transaction.commit();
        }

    }
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

    @Override
    public void onResume(){
        super.onResume();
        pref = getSharedPreferences("signedup", Context.MODE_PRIVATE);
        //show error dialog if GoolglePlayServices not available
        if (!isGooglePlayServicesAvailable()) {
            return;
        }
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if(networkInfo==null && !networkInfo.isConnected()) {

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
        }else{
            String[] PERMISSIONS = {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION};
            if(!hasPermissions(this, PERMISSIONS)){
                ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_ID_MULTIPLE_PERMISSIONS);
            }else {
                permissionFlag = true;
                if (pref.getInt(getString(R.string.signedupflag),0) == 0) {
                    getSupportActionBar().hide();
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    userName = (TextView)findViewById(R.id.GuserName);
                    userEmail = (TextView)findViewById(R.id.GuserEmail);
                    userPic = (ImageView)findViewById(R.id.Gprofileimage);
                    android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    LoginFragment fragment = new LoginFragment();
                    transaction.replace(R.id.content_frame, fragment);
                    transaction.commit();
                }else{
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    getSupportActionBar().hide();
                    navButton.show();
                    android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    MapFragment fragment = new MapFragment();
                    transaction.replace(R.id.content_frame, fragment);
                    transaction.commit();

                }
            }
        }
    }
}