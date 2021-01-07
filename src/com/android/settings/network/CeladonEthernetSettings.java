/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) 2020 Intel Corporation
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

package com.android.settings.network;

import android.app.Dialog;
import android.content.Context;
import android.net.EthernetManager;
import android.net.ProxyInfo;
import android.net.IpConfiguration;
import android.net.IpConfiguration.ProxySettings;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class CeladonEthernetSettings extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener, OnKeyListener {

    private static final String TAG = "CeladonEthernetSettings";

    private EditTextPreference mProxyPreference;
    private EditTextPreference mPortPreference;

    private String mProxyHost = "";
    private int mProxyPort = -1;

    private EthernetManager mEthernetManager;
    private IpConfiguration mIpConfiguration;
    private String mInterfaceName;

    private final static String KEY_PROXY = "proxy";
    private final static String KEY_PORT = "port";

    private static final int MENU_SAVE = Menu.FIRST;
    private static final int MENU_CANCEL = Menu.FIRST + 1;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.ethernet_proxy_settings);

	mProxyPreference = (EditTextPreference) findPreference("proxy");
        mPortPreference = (EditTextPreference) findPreference("port");

        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
            getPreferenceScreen().getPreference(i).setOnPreferenceChangeListener(this);
        }
        mEthernetManager = (EthernetManager) getSystemService(Context.ETHERNET_SERVICE);

        String[] ifaces = mEthernetManager.getAvailableInterfaces();
        if (ifaces.length > 0) {
            mInterfaceName = ifaces[0];
            mIpConfiguration = mEthernetManager.getConfiguration(mInterfaceName);
        }
        Log.d(TAG, "Interface Name: " + mInterfaceName);
        if (mIpConfiguration != null && mIpConfiguration.httpProxy != null) {
            mProxyHost = mIpConfiguration.httpProxy.getHost();
            mProxyPort = mIpConfiguration.httpProxy.getPort();
            if (mProxyHost != null && !mProxyHost.equals("")) {
                mProxyPreference.setText(mProxyHost);
            }
            if (mProxyPort > -1) {
                mPortPreference.setText(String.valueOf(mProxyPort));
            }
	}
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.VIEW_UNKNOWN;
    }

    public void configureProxy() {
        if (!mProxyHost.equals("") && mProxyPort > -1) {
            mIpConfiguration.setProxySettings(ProxySettings.STATIC);
            ProxyInfo mHttpProxy = new ProxyInfo(mProxyHost, mProxyPort, null);
            mIpConfiguration.setHttpProxy(mHttpProxy);
            mEthernetManager.setConfiguration(mInterfaceName, mIpConfiguration);

            Log.d(TAG, "IpConfiguration being updated: " + mIpConfiguration.toString());
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (KEY_PROXY.equals(key)) {
	    if (newValue != null) {
	        mProxyHost = String.valueOf(newValue);
                Log.d(TAG, "ProxyHost: " + mProxyHost);
	        preference.setSummary(mProxyHost);
            }
        } else if (KEY_PORT.equals(key)) {
	    if (newValue != null) {
                String port = String.valueOf(newValue);
                try {
	            mProxyPort = Integer.parseInt(port);
                } catch (NumberFormatException ex) {
                    Log.e(TAG, "Exception in port: " + ex);
		    mProxyPort = -1;
                }
                Log.d(TAG, "Port: " + port);
	        preference.setSummary(port);
            }
        }
        return true;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_DOWN) return false;
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK: {
		configureProxy();
                finish();
                return true;
            }
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

	menu.add(0, MENU_SAVE, 0, R.string.menu_save)
            .setIcon(android.R.drawable.ic_menu_save);
        menu.add(0, MENU_CANCEL, 0, R.string.menu_cancel)
            .setIcon(android.R.drawable.ic_menu_close_clear_cancel);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_SAVE:
		configureProxy();
		finish();
                return true;
            case MENU_CANCEL:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setOnKeyListener(this);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
    }
}
