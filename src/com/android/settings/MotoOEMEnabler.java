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

import static android.Manifest.permission.READ_PHONE_STATE;
import android.app.ActivityManagerNative;
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

import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneStateIntentReceiver;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.TelephonyProperties;

public class MotoOEMEnabler implements Preference.OnPreferenceChangeListener {

    private final Context mContext;

    private final CheckBoxPreference mCheckBoxPref;

    private ContentObserver mMotoOEMObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            onMotoOEMChanged();
        }
    };

    public MotoOEMEnabler(Context context, CheckBoxPreference MotoOEMCheckBoxPreference) {
        mContext = context;
        mCheckBoxPref = MotoOEMCheckBoxPreference;
        MotoOEMCheckBoxPreference.setPersistent(true);
    }

    public void resume() {
        mCheckBoxPref.setChecked(isMotoOEMOn(mContext));
        mCheckBoxPref.setOnPreferenceChangeListener(this);
        mContext.getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.MOTO_OEM_STATE), true,
                mMotoOEMObserver);
    }
    
    public void pause() {
        mCheckBoxPref.setOnPreferenceChangeListener(null);
        mContext.getContentResolver().unregisterContentObserver(mMotoOEMObserver);
    }

    public static boolean isMotoOEMOn(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.MOTO_OEM_STATE, 0) != 0;
    }

    private void setMotoOEMOn(boolean enabling) {
        // Change the system setting
        Settings.System.putInt(mContext.getContentResolver(), Settings.System.MOTO_OEM_STATE, 
                                enabling ? 1 : 0);
        // Update the UI to reflect system setting
        mCheckBoxPref.setChecked(enabling);
        SystemProperties.set(TelephonyProperties.PROPERTY_MOTO_OEM, (enabling ? "true" : "false"));
        // Post the intent
/*
        Intent iccstate = new Intent(IccCardConstants.INTENT_VALUE_ICC_NOT_READY);
        ActivityManagerNative.broadcastStickyIntent(iccstate, null);
        Intent intent = new Intent(TelephonyIntents.ACTION_RADIO_TECHNOLOGY_CHANGED);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        intent.putExtra(PhoneConstants.PHONE_NAME_KEY, "Phone");
        ActivityManagerNative.broadcastStickyIntent(intent, null);
        Intent simstate = new Intent(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        intent.putExtra(PhoneConstants.PHONE_NAME_KEY, "Phone");
        ActivityManagerNative.broadcastStickyIntent(simstate, READ_PHONE_STATE);
*/
    }

    /**
     * Called when we've received confirmation that the Moto OEM was set.
     */
    private void onMotoOEMChanged() {
        mCheckBoxPref.setChecked(isMotoOEMOn(mContext));
    }
    
    /**
     * Called when someone clicks on the checkbox preference.
     */
    public boolean onPreferenceChange(Preference preference, Object newValue) {
	Log.e("MotoOEMEnabler", "onPreferenceChange(" + (Boolean)newValue + ")");
        setMotoOEMOn((Boolean) newValue);
        return true;
    }
}
