package com.motopit;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by abdul on 29/3/16.
 */
public class AnotherActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.another_main);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String item = getIntent().getStringExtra("item");
        Fragment fragment = null;

        switch (item) {
            case "Admin":  //MR 4-8-16 @Rahman make space between words
                fragment = new AdminFragment();
                break;
            case "Shop":
                fragment = new ShopFragment();
                break;
        }
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.another_frame, fragment);
            transaction.commit();
        }
        setTitle(" "+item);
    }



    @Override
    public void onResume() {
        super.onResume();
    }



}
