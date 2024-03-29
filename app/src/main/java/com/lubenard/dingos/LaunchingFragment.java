package com.lubenard.dingos;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.Locale;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class LaunchingFragment extends Fragment {

    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private ReceiveBtDatas bluetoothDataReceiver;
    private String currentLocale;
    private int switchToLanguage;
    private TextView isBackupFoundTextView;

    private void connectToBluetooth() {
        // Get SharedPreference to see if Bluetooth address is already registered
        String bluetooth_addr = getActivity().getPreferences(Context.MODE_PRIVATE).getString("BLUETOOTH_ADDR", null);
        if (bluetooth_addr != null) {
            // Bluetooth address already registered, try to connect to it
            Log.d("BLUETOOTH", "Connecting to " + bluetooth_addr);
            Toast.makeText(getContext(), getContext().getString(R.string.auto_connect_toast), Toast.LENGTH_LONG).show();
            bluetoothDataReceiver = new ReceiveBtDatas();
            if (bluetoothDataReceiver.connect(bluetooth_addr) == 1) {
                Toast.makeText(getContext(), getContext().getString(R.string.bluetooth_toast_error), Toast.LENGTH_LONG).show();
            } else {
                BluetoothFragment.setFragmentManager(getFragmentManager());
                BluetoothFragment.setBluetoothDataReceiver(bluetoothDataReceiver);
                BluetoothFragment.changeForWaitScan();
            }
        } else {
            Log.d("BLUETOOTH", "Device not registered, displaying Bluetooth Page...");
            // Bluetooth address not registered, display bluetooth devices
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            BluetoothFragment fragment = new BluetoothFragment();
            fragmentTransaction.replace(android.R.id.content, fragment).addToBackStack(null);
            fragmentTransaction.commit();
        }
    }

    private void setAppLocale(String localeCode){
        Locale myLocale = new Locale(localeCode);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        LaunchingFragment fragment = new LaunchingFragment();
        fragmentTransaction.replace(android.R.id.content, fragment);
        fragmentTransaction.commit();
    }

    private int isBluetoothTurnedOn() {
        if (mBluetoothAdapter == null) {
            // Bluetooth does not exist on this device
            Log.e("BLUETOOTH","Error. It seems bluetooth does not exist on this device");
            return -1;
        } else if (!mBluetoothAdapter.isEnabled()) {
            // Bluetooth is not activated
            return 1;
        } else {
            // Bluetooth is activated
            return 0;
        }
    }

    private boolean isBluetoothConnected() {
        switch (isBluetoothTurnedOn()) {
            case -1:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                alertDialogBuilder.setTitle(getContext().getString(R.string.error));
                alertDialogBuilder.setMessage(getContext().getString(R.string.bluetooth_does_not_exist));
                alertDialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getActivity().finish();
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                break;
            case 0:
                connectToBluetooth();
                break;
            case 1:
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
                connectToBluetooth();
                break;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                connectToBluetooth();
            }
            if (resultCode == RESULT_CANCELED) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                alertDialogBuilder.setPositiveButton(this.getString(R.string.bluetooth_required), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getActivity().finish();
                    }
                });
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.lauching_fragment, container, false);
    }


    Locale getCurrentLocale(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return context.getResources().getConfiguration().getLocales().get(0);
        } else {
            return context.getResources().getConfiguration().locale;
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentLocale = getCurrentLocale(getContext()).toString();

        Log.d("LANGUAGE", "Current locale is " + currentLocale);

        Toolbar toolbar = view.findViewById(R.id.main_toolbar);
        if (!currentLocale.equals("fr_FR") && !currentLocale.equals("fr")) {
            toolbar.getMenu().getItem(0).setIcon(getResources().getDrawable(R.drawable.fr));
            switchToLanguage = 0; // Switch to fr language
        }
        else {
            toolbar.getMenu().getItem(0).setIcon(getResources().getDrawable(R.drawable.en));
            switchToLanguage = 1; // switch to en language
        }
        toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.about:
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    AboutFragment fragment = new AboutFragment();
                    fragmentTransaction.replace(android.R.id.content, fragment).addToBackStack(null);
                    fragmentTransaction.commit();
                    break;
                case R.id.set_language:

                    if (switchToLanguage == 1)
                        setAppLocale("en-us");
                    else if (switchToLanguage == 0)
                        setAppLocale("fr");
                    break;
                case R.id.reset_bt:
                    SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
                    preferences.edit().remove("BLUETOOTH_ADDR").apply();
                    break;
                case R.id.reset_user_progress:
                    SharedPreferences preferences2 = getActivity().getPreferences(Context.MODE_PRIVATE);
                    preferences2.edit().remove("DISCOVERED_PROGRESS").apply();
                    WaitScan.resetDiscoveryArray();
                    isBackupFoundTextView.setTextColor(ResourcesCompat.getColor(getResources(), R.color.red, null));
                    isBackupFoundTextView.setText(getContext().getString(R.string.saveNotFound));
                    break;
                default:
                    break;
            }
            return false;
        });

        Button startSession = view.findViewById(R.id.startSessionButton);

         isBackupFoundTextView = view.findViewById(R.id.isThereBackup);

        String userProgress = getActivity().getPreferences(Context.MODE_PRIVATE).getString("DISCOVERED_PROGRESS", null);
        if (userProgress != null) {
            isBackupFoundTextView.setTextColor(ResourcesCompat.getColor(getResources(), R.color.green, null));
            isBackupFoundTextView.setText(getContext().getString(R.string.saveFound));
        } else {
            isBackupFoundTextView.setTextColor(ResourcesCompat.getColor(getResources(), R.color.red, null));
            isBackupFoundTextView.setText(getContext().getString(R.string.saveNotFound));
        }

        startSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WaitScan.setDebugMode(0);
                isBluetoothConnected();
            }
        });
    }
}
