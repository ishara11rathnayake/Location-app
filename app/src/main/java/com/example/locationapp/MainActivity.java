package com.example.locationapp;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    Button requestLocation, removeLocation;

    BackgroundService mService = null;
    boolean mBound = false;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BackgroundService.LocalBinder binder = (BackgroundService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Dexter.withContext(this)
                .withPermissions(Arrays.asList(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ))
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        requestLocation = findViewById(R.id.request_location_update_btn);
                        removeLocation = findViewById(R.id.remove_location_updates_btn);

                        requestLocation.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mService.requestLocationUpdates();
                            }
                        });

                        removeLocation.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mService.removeLocationUpdates();
                            }
                        });

                        setButtonState(Common.requestingLocationUpdates(MainActivity.this));
                        bindService(new Intent(MainActivity.this, BackgroundService.class),
                                mServiceConnection,
                                Context.BIND_AUTO_CREATE);
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

                    }
                }).check();
    }

    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        if(mBound){
            unbindService(mServiceConnection);
            mBound=false;
        }
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(Common.KEY_REQUESTING_LOCATION_UPDATES)){
            setButtonState(sharedPreferences.getBoolean(Common.KEY_REQUESTING_LOCATION_UPDATES, false));
        }
    }

    private void setButtonState(boolean isRequestEnable) {
        if(isRequestEnable){
            requestLocation.setEnabled(false);
            removeLocation.setEnabled(true);
        } else {
            requestLocation.setEnabled(true);
            removeLocation.setEnabled(false);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onListenLocation(SendLocationToActivity event){
        if(event != null){
            String data = new StringBuilder()
                    .append(event.getLocation().getLatitude())
                    .append("/")
                    .append(event.getLocation().getLongitude())
                    .toString();
            Toast.makeText(mService, data, Toast.LENGTH_SHORT).show();
            Gson gson = new Gson();
            mService.mSocket.emit("GPS_DATA",gson.toJson(new NewLocation(event.getLocation().getLatitude(), event.getLocation().getLongitude(),
                    event.getLocation().getAltitude(), new Date(), event.getLocation())));
        }
    }
}
