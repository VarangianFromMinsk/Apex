package com.example.myapplication.main.Screens.Settings;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.example.myapplication.R;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
      addPreferencesFromResource(R.xml.app_settings);

        SharedPreferences sharedPreferences=getPreferenceScreen().getSharedPreferences();
        PreferenceScreen preferenceScreen=getPreferenceScreen();
        int count=preferenceScreen.getPreferenceCount();

        for (int i =0; i<count; i++){
            Preference preference=preferenceScreen.getPreference(i);
            if(!(preference instanceof SwitchPreference)){
                String value=sharedPreferences.getString(preference.getKey(),"bell");
                setPreferenceLabel(preference,value);
            }

        }

    }

    private void setPreferenceLabel(Preference preference,String value){
        if(preference instanceof ListPreference){
            ListPreference listPreference=(ListPreference) preference;
            int index=listPreference.findIndexOfValue(value);
            if(index >= 0){
                listPreference.setSummary(listPreference.getEntries()[index]);
            }
        }else if(preference instanceof EditTextPreference) {
            preference.setSummary(value);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference=findPreference(key);
        if(!(preference instanceof SwitchPreference)) {
            String value=sharedPreferences.getString(preference.getKey(),"");
            setPreferenceLabel(preference,value);
        }

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }
}
