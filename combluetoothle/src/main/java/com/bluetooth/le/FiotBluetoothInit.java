package com.bluetooth.le;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bluetooth.le.exception.NotFromActivity;
import com.bluetooth.le.exception.NotSupportBleException;
import com.example.com.bluetooth.le.R;

/**
 * Created by caoxuanphong on 4/5/17.
 */

public class FiotBluetoothInit {
    private static final String TAG = "FiotBluetoothInit";
    private static Context context;
    private static FiotBluetoothInitListener listener;

    /**
     * Callback when enable completed
     */
    public interface FiotBluetoothInitListener {
        void completed();
    }

    /**
     * Check bluetooth state, ble support and permission
     *
     * @param c
     * @param l
     * @throws NotSupportBleException
     * @throws NotFromActivity
     */
    public static void enable(Context c, FiotBluetoothInitListener l) throws NotSupportBleException, NotFromActivity {
        if (!(c instanceof Activity)) {
            throw new NotFromActivity("Given Context must be an Activity");
        }

        context = c;
        listener = l;
        ((Activity) context).getApplication().registerActivityLifecycleCallbacks(activityLifecycleCallbacks);

        checkSupportBle();
        checkBluetoothAndEnablePermission();
    }

    private static void checkSupportBle() throws NotSupportBleException {
        if (!isBleHardwareAvailable()) {
            throw new NotSupportBleException(context.getResources().getString(R.string.exception_not_support_ble));
        }
    }

    /**
     * Check if phone supports BLE
     *
     * @return true on support, false on not.
     */
    private static boolean isBleHardwareAvailable() {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    private static void checkBluetoothAndEnablePermission() {
        if (!FiotBluetoothUtils.isBluetoothEnabled(context)) {
            Log.d(TAG, "checkBluetoothAndEnablePermission: ");
            FiotBluetoothUtils.enableBluetooth(context);
        } else if (!isLocationServiceEnabled()) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    new MaterialDialog.Builder(context)
                            .title("Permission")
                            .content("Please turn on location service")
                            .positiveText("Turn On")
                            .cancelable(false)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    context.startActivity(intent);
                                }
                            })
                            .show();
                }
            });
        } else {
            if (hasPermission()) {
                listener.completed();
            } else {
                FiotBluetoothUtils.requestPermission(context);
            }
        }
    }

    private static boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    static Application.ActivityLifecycleCallbacks activityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            Log.d(TAG, "onActivityCreated: ");
        }

        @Override
        public void onActivityStarted(Activity activity) {
            Log.d(TAG, "onActivityStarted: ");
        }

        @Override
        public void onActivityResumed(Activity activity) {
            Log.d(TAG, "onActivityResumed: ");
            checkBluetoothAndEnablePermission();
        }

        @Override
        public void onActivityPaused(Activity activity) {
            Log.d(TAG, "onActivityPaused: ");
        }

        @Override
        public void onActivityStopped(Activity activity) {
            Log.d(TAG, "onActivityStopped: ");
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            Log.d(TAG, "onActivitySaveInstanceState: ");
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            Log.d(TAG, "onActivityDestroyed: ");
        }

    };

    public static boolean isLocationServiceEnabled() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }

}
