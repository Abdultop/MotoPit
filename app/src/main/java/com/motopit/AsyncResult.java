package com.motopit;

import org.json.JSONObject;

/**
 * Created by abdul on 7/11/15.
 */
interface AsyncResult {
    void onResult(JSONObject object);
}
