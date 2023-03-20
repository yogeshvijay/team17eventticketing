//package com.team17.secureticketing;
//
//import android.Manifest;
//import android.app.AlertDialog;
//
//import android.app.ProgressDialog;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.os.Environment;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.Window;
//import android.view.WindowManager;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//
//import com.google.zxing.BinaryBitmap;
//import com.google.zxing.LuminanceSource;
//import com.google.zxing.RGBLuminanceSource;
//import com.google.zxing.Reader;
//import com.google.zxing.Result;
//import com.google.zxing.common.HybridBinarizer;
//import com.google.zxing.qrcode.QRCodeReader;
//
//import java.io.BufferedReader;
//import java.io.FileInputStream;
//import java.io.InputStreamReader;
//
//import me.dm7.barcodescanner.zxing.ZXingScannerView;
//
//public class ScanQR extends AppCompatActivity implements ZXingScannerView.ResultHandler {
//
//    private static final String TAG = "BlueTest5-Controlling";
//    private int mMaxChars = 50000;//Default//change this to string..........
//
//    private ProgressDialog progressDialog;
//
//    ZXingScannerView mScannerView;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//
//        mScannerView = new ZXingScannerView(this);
//        setContentView(mScannerView);
//
//        if (ActivityCompat.checkSelfPermission(ScanQR.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(ScanQR.this, new String[]{Manifest.permission.CAMERA}, 0);
//            return;
//        }
//
//
//    }
//
//    private void msg(String s) {
//        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
//    }
//
//
//    @Override
//    protected void onStop() {
//        Log.d(TAG, "Stopped");
//        super.onStop();
//    }
//
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//// TODO Auto-generated method stub
//        super.onSaveInstanceState(outState);
//    }
//
//
//    @Override
//    protected void onDestroy() {
//        // TODO Auto-generated method stub
//        super.onDestroy();
//    }
//
//    @Override
//    public void handleResult(Result result) {
//        Log.d(TAG, "handleResult: result== $result " + result.getText());
//
////        String lastPNR = getLatestGeneratedQRCodePNR();
//        final String IMAGE_PATH = Environment.getExternalStorageDirectory().getPath() + "/SmartRailwayQR/Railway_Ticket.jpg";
//        String lastPNR = decodeQRImagePNR(IMAGE_PATH);
//        Home.mTextView.setText(result.getText());
//
//        AlertDialog.Builder ab = new AlertDialog.Builder(ScanQR.this);
//        ab.setTitle("Info");
//        ab.setMessage("Invalid PNR");
//        ab.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                Intent j = new Intent(ScanQR.this, Home.class);
//                startActivity(j);
//            }
//        });
//        if (lastPNR.equals(result.getText())) { //Hardcoded better read from file so store in local file after capturing the QR code
//            ab.setMessage("User Verified");
//        } else {
//            ab.setMessage("Invalid Input");
//        }
//        ab.show();
//
//    }
//
//    private String getLatestGeneratedQRCodePNR() {
//        try {
//            FileInputStream fileInputStream = getApplicationContext().openFileInput("latest_QR_PNR.txt");
//            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
//            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//            return bufferedReader.readLine();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "";
//        }
//    }
//
//    public static String decodeQRImagePNR(String path) {
//        Bitmap bMap = BitmapFactory.decodeFile(path);
//        String decoded = null;
//
//        try {
//            int[] intArray = new int[bMap.getWidth() * bMap.getHeight()];
//            bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());
//            LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(), bMap.getHeight(), intArray);
//            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
//
//            Reader reader = new QRCodeReader();
//
//            Result result = reader.decode(bitmap);
//            decoded = result.getText();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return decoded == null ? "" : decoded;
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        mScannerView.stopCamera();
////        if (mBTSocket != null && mIsBluetoothConnected) {
////            new ScanQR.DisConnectBT().execute();
////        }
//        Log.d(TAG, "Paused");
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
////        if (mBTSocket == null || !mIsBluetoothConnected) {
////            new ScanQR.ConnectBT().execute();
////        }
//        Log.d(TAG, "Resumed");
//        mScannerView.setResultHandler(this);
//        mScannerView.startCamera();
//    }
//}
