package com.app.ray.testbluetooth;

/**
 * Created by ray on 2017/8/28.
 */

public class ConnectThread {
//    private final BluetoothSocket mmSocket;
//    private final BluetoothDevice mmDevice;
//
//    public ConnectThread(BluetoothDevice device) {
//        // Use a temporary object that is later assigned to mmSocket
//        // because mmSocket is final.
//        BluetoothSocket tmp = null;
//        mmDevice = device;
//
//        try {
//            // Get a BluetoothSocket to connect with the given BluetoothDevice.
//            // MY_UUID is the app's UUID string, also used in the server code.
//            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
//        } catch (IOException e) {
//            Log.e(TAG, "Socket's create() method failed", e);
//        }
//        mmSocket = tmp;
//    }
//
//    public void run() {
//        // Cancel discovery because it otherwise slows down the connection.
//        mBluetoothAdapter.cancelDiscovery();
//
//        try {
//            // Connect to the remote device through the socket. This call blocks
//            // until it succeeds or throws an exception.
//            mmSocket.connect();
//        } catch (IOException connectException) {
//            // Unable to connect; close the socket and return.
//            try {
//                mmSocket.close();
//            } catch (IOException closeException) {
//                Log.e(TAG, "Could not close the client socket", closeException);
//            }
//            return;
//        }
//
//        // The connection attempt succeeded. Perform work associated with
//        // the connection in a separate thread.
//        manageMyConnectedSocket(mmSocket);
//    }
//
////    private void manageMyConnectedSocket(BluetoothSocket mmSocket) {
////        if (mmSocket.isConnected()) {
////            MyBluetoothService mConnectedThread = new MyBluetoothService();
////            mConnectedThread.start();
////        }
////    }
////
//    // Closes the client socket and causes the thread to finish.
//    public void cancel() {
//        try {
//            mmSocket.close();
//        } catch (IOException e) {
//            Log.e(TAG, "Could not close the client socket", e);
//        }
//    }
}
