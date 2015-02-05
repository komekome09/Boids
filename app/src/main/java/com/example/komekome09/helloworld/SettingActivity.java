package com.example.komekome09.helloworld;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by komekome09 on 2015/02/05.
 */
public class SettingActivity extends Activity{
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                            .replace(android.R.id.content, new SettingFragment())
                            .commit();
    }


}
