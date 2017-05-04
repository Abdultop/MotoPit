package com.motopit;

import android.net.Uri;

/**
 * Created by abdul on 13/04/17.
 */

public class ImageItem {

    private String imageUri;

    public ImageItem(String imageUri){
        this.imageUri = imageUri;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
}
