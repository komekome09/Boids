package com.example.komekome09.helloworld;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

/**
 * Created by komekome09 on 2015/02/05.
 */
public class SettingFragment extends PreferenceFragment{
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        // Load the preference from an XML resources.
        addPreferencesFromResource(R.xml.setting_preference);

        CharSequence cs = getText(R.string.pref_edittext_key);
        EditTextPreference etp = (EditTextPreference)findPreference(cs);
        Integer str_num = getResources().getInteger(R.integer.init_num);
        String str = str_num.toString();

        etp.setText(str);
        etp.setOnPreferenceChangeListener(editTextChangeListener);
    }

    private Preference.OnPreferenceChangeListener editTextChangeListener =
            new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    return editTextChange(preference, newValue);
                }
            };

    // Reflect a value.
    private boolean editTextChange(Preference preference, Object newValue){
        String input = newValue.toString();
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        Activity ac = getActivity();

        // Send input data for MainActivity.
        if(input != null){
            String str = getResources().getString(R.string.app_name, Integer.parseInt(input));
            int num = Integer.parseInt(input), max = getResources().getInteger(R.integer.max_num);

            if(num > max) {
                Toast.makeText(ac, "Input value is too big!!", Toast.LENGTH_LONG).show();
                num = max;
            }

            bundle.putInt("key.CircNum", num);
            intent.putExtras(bundle);
            ac.setResult(Activity.RESULT_OK, intent);
            preference.setSummary(input);
            ac.finish();

            return true;
        }else{
            bundle.putInt("key.CircNum", -1);
            intent.putExtras(bundle);
            ac.setResult(Activity.RESULT_CANCELED, intent);
            ac.finish();

            return false;
        }
    }
}
