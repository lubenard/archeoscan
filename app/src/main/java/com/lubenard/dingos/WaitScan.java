package com.lubenard.dingos;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class WaitScan extends Fragment {
    private static ArrayList<Integer> elementDiscoveredArray = new ArrayList<>();
    private View curView;

    private static int itemIndexChoice;
    private static int videoPathChoice;

    private static Boolean isConnectionAlive;
    private static BluetoothSocket socket;
    private static ReceiveBtDatas bluetoothDataReceiver;

    private static final int[] resArray = new int[] {R.raw.intro, R.raw.avant_bras, R.raw.coxaux,
            R.raw.crane, R.raw.femur, R.raw.humerus, R.raw.objet, R.raw.reduction, R.raw.tibia,
            R.raw.photo};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.waiting_for_scan_fragment, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_wait_scan, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ListVideo fragment = new ListVideo();
        fragmentTransaction.replace(android.R.id.content, fragment).addToBackStack(null);
        fragmentTransaction.commit();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        curView = view;

        TextView textView = (TextView) curView.findViewById(R.id.element_discovered);
        textView.setText(elementDiscoveredArray.size() + "/10");

        Bundle bundle = getArguments();

        if (bundle.getBoolean("launchThread")) {
            bluetoothDataReceiver = (ReceiveBtDatas) bundle.getSerializable("dataReceiver");
            Log.d("BLUETOOTH", "Is connection still valid after transition :" + bluetoothDataReceiver.getConnectionStatus());
            threadReadData(bluetoothDataReceiver);
        }
        else {
            Log.d("BLUETOOTH", "isConnectionAlive = " + isConnectionAlive + " setting it to true");
            isConnectionAlive = true;
            threadReadData(bluetoothDataReceiver);
        }
    }

    private void commitTransition() {
        // Disable isConnectionAlive to avoid being able to scan during quizz or video
        isConnectionAlive = false;

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        VideoPlayerFragment fragment = new VideoPlayerFragment();
        fragmentTransaction.replace(android.R.id.content, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public static int getItemChoice() {
        return itemIndexChoice;
    }

    public static int getVideoPathChoice() {
        return videoPathChoice;
    }

    public static ArrayList<Integer> getElementDiscoveredArray() {
        return elementDiscoveredArray;
    }

    private void setItemChoice(int itemIndex, int videoPath){
        itemIndexChoice = itemIndex;
        videoPathChoice = videoPath;
    }

    private void threadReadData(final ReceiveBtDatas bluetoothDataReceiver) {
        new Thread()
        {
            public void run()
            {
                socket = bluetoothDataReceiver.getSocket();
                isConnectionAlive = bluetoothDataReceiver.getConnectionStatus();
                InputStream inputStream = null;
                try {
                    inputStream = socket.getInputStream();
                    while (isConnectionAlive) {
                        int dataRead = inputStream.read();
                        Log.d("BLUETOOTH", "Looking for datas");
                        Log.d("BLUETOOTH", "Datas available: " + String.format("%c", dataRead));
                        if (dataRead >= 48 && dataRead <= 57) {
                            Log.d("BLUETOOTH","Valid card!");
                            int elementRead = dataRead - 48;
                            elementDiscoveredArray.add(elementRead);

                            TextView textView = (TextView) curView.findViewById(R.id.element_discovered);
                            textView.setText(elementDiscoveredArray.size() + "/10");

                            setItemChoice(elementRead, resArray[elementRead]);
                            commitTransition();
                        } else {
                            Log.d("BLUETOOTH", "This card is not between 48 and 57. It's code actually is " + dataRead);
                            Toast.makeText(getContext(), getContext().getString(R.string.bad_card_code), Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}