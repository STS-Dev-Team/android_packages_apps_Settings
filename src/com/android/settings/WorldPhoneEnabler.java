/*
 * Copyright (C) 2012 STS-Dev-Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.provider.Settings;
import android.util.Log;

public class WorldPhoneEnabler implements Preference.OnPreferenceChangeListener {

    private final Context mContext;

    private final CheckBoxPreference mCheckBoxPref;

    private ContentObserver mWorldPhoneObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            onWorldPhoneChanged();
        }
    };

    public WorldPhoneEnabler(Context context, CheckBoxPreference WorldPhoneCheckBoxPreference) {
        mContext = context;
        mCheckBoxPref = WorldPhoneCheckBoxPreference;
        WorldPhoneCheckBoxPreference.setPersistent(true);
    }

    public void resume() {
        mCheckBoxPref.setChecked(isWorldPhoneOn(mContext));
        mCheckBoxPref.setOnPreferenceChangeListener(this);
        mContext.getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.WORLD_PHONE_STATE), true,
                mWorldPhoneObserver);
    }
    
    public void pause() {
        mCheckBoxPref.setOnPreferenceChangeListener(null);
        mContext.getContentResolver().unregisterContentObserver(mWorldPhoneObserver);
    }

    public static boolean isWorldPhoneOn(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.WORLD_PHONE_STATE, 0) != 0;
    }

    private void setWorldPhoneOn(boolean enabling) {
        // Change the system setting
        Settings.System.putInt(mContext.getContentResolver(), Settings.System.WORLD_PHONE_STATE, 
                                enabling ? 1 : 0);
        // Update the UI to reflect system setting
        mCheckBoxPref.setChecked(enabling);
    }

    /**
     * Called when we've received confirmation that the World Phone was set.
     */
    private void onWorldPhoneChanged() {
        mCheckBoxPref.setChecked(isWorldPhoneOn(mContext));
    }
    
    /**
     * Called when someone clicks on the checkbox preference.
     */
    public boolean onPreferenceChange(Preference preference, Object newValue) {
	Log.e("WorldPhoneEnabler", "onPreferenceChange(" + (Boolean)newValue + ")");
        setWorldPhoneOn((Boolean) newValue);
        return true;
    }
}
