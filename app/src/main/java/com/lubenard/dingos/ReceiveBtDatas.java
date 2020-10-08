package com.lubenard.dingos;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.UUID;

public class ReceiveBtDatas extends Thread implements Serializable {
    private Boolean isConnectionAlive = false;
    private BluetoothSocket socket = null;

    private static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public int connect(String macAddr) {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice btdevice = mBluetoothAdapter.getRemoteDevice(macAddr);

        int counter = 0;

        while (counter < 3) {
            try {
                socket = btdevice.createRfcommSocketToServiceRecord(myUUID);
                Log.d("BLUETOOTH", "socket is " + socket);
                socket.connect();
            } catch (IOException e) {
                Log.d("BLUETOOTH", "Failed to connect" + socket);
                e.printStackTrace();
            }
            Log.d("BLUETOOTH", "Is bluetooth connected " + socket.isConnected());
            counter++;
        }
        if (!socket.isConnected()){
            return 1;
        }
        isConnectionAlive = true;
        return 0;
    }

    public void listenForDatas() throws IOException {
        InputStream inputStream = socket.getInputStream();

        try {
            Log.d("BLUETOOTH", "Listening for datas. IsConnection alive: " + isConnectionAlive);
            while (isConnectionAlive) {
                Log.d("BLUETOOTH", "Datas available: " + String.format("%c", inputStream.read()));
            }
        } catch( Exception exception ) {
            Log.e( "DEBUG", "Cannot read data", exception );
            //closeConnection();
        }
    }

    public boolean getConnectionStatus() {
        return isConnectionAlive;
    }

    public BluetoothSocket getSocket() {
        return socket;
    }

    public void closeConnection() {
        isConnectionAlive = false;
    }
}