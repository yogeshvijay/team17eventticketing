package com.team17.secureticketing;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Environment;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.WriterException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import androidmads.library.qrgenearator.QRGSaver;

public class QRCode extends AppCompatActivity {

    private static final String IMAGE_DIRECTORY =  Environment.getExternalStorageDirectory().getPath() + "/EventQR/";

    String TAG = "GenerateQrCode";

    ImageView qrimg;
    Bitmap mBitmap;

    QRGEncoder mQRGEncoder;

    Button mm, exit,save;

    //FirebaseDatabase database=FirebaseDatabase.getInstance();
    //DatabaseReference reference= database.getReference("users");

    RequestQueue mRequestQueue;

    FirebaseFirestore firestore;

    SecretKey key;

    IvParameterSpec ivParameterSpec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_qrcode);

        final User mUser = (User) getIntent().getSerializableExtra("user");
        final String input = mUser.getPnr();

        qrimg = findViewById(R.id.qr);
        mm = findViewById(R.id.main_menu);
        exit = findViewById(R.id.close);
        save = findViewById(R.id.save_img);


        storeLatestQRCodePNR(input);

        String cipherText;

        try {
            cipherText = this.encryptString(mUser.getPnr());
        } catch (NoSuchAlgorithmException | IllegalBlockSizeException | InvalidKeyException |
                 BadPaddingException | InvalidAlgorithmParameterException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }

        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerdimen = width < height ? width : height;
        smallerdimen = smallerdimen * 3 / 4;
        mQRGEncoder = new QRGEncoder(cipherText, null, QRGContents.Type.TEXT, smallerdimen);

        try {
            mBitmap = mQRGEncoder.encodeAsBitmap();
            qrimg.setImageBitmap(mBitmap);
        } catch (WriterException e) {
            Log.v(TAG, e.toString());
        }

        firestore = FirebaseFirestore.getInstance();

        QRData dataStore = new QRData();

        dataStore.setCipherText(cipherText);
        dataStore.setUserName(mUser.getName());
        dataStore.setKey(String.valueOf(this.key.getEncoded()));
        dataStore.setIvParameterSpec(String.valueOf(this.ivParameterSpec.getIV()));

        firestore.collection("QRDetails").add(dataStore).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_LONG).show();
            }
        });

        mRequestQueue = Volley.newRequestQueue(this);



//        Toast.makeText(this, input, Toast.LENGTH_LONG).show();

        final ProgressDialog pd = new ProgressDialog(QRCode.this);
        pd.setMessage("Please Wait...");
        pd.setCanceledOnTouchOutside(false);
      //  pd.show();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
//                    QRGSaver.save(IMAGE_DIRECTORY,input.trim(),mBitmap,QRGContents.ImageType.IMAGE_JPEG);
                    QRGSaver.save(IMAGE_DIRECTORY,"Team 17 Event",mBitmap,QRGContents.ImageType.IMAGE_JPEG);
                }

                catch (Exception e){
                    e.printStackTrace();
                }

                AlertDialog.Builder ab=new AlertDialog.Builder(QRCode.this);
                ab.setTitle("Info");
                ab.setMessage("File Saved to "+IMAGE_DIRECTORY);
                ab.setPositiveButton("OK",null);
                ab.show();
            }
        });

        mm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(QRCode.this, Home.class);
                startActivity(i);
                finish();
            }
        });

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAffinity();
            }
        });

    }

    //Start of AES Encryption

    public static SecretKey generateKey(int n) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(n);
        SecretKey key = keyGenerator.generateKey();
        return key;
    }

    public static IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    @SuppressLint("NewApi")
    public static String encrypt(String algorithm, String input, SecretKey key,
                                 IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] cipherText = cipher.doFinal(input.getBytes());
        return Base64.getEncoder()
                .encodeToString(cipherText);
    }

    private String encryptString(String input)
            throws NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException,
            BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException {

        this.key = this.generateKey(128);
        this.ivParameterSpec = this.generateIv();
        String algorithm = "AES/CBC/PKCS5Padding";
        String cipherText = this.encrypt(algorithm, input, key, ivParameterSpec);

        return cipherText;

    }

    // End of AES Encryption

    private void storeLatestQRCodePNR(String input) {
        try {
            File file = new File(getFilesDir(), "latest_QR_PNR.txt");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
            bufferedWriter.write(input);
        } catch (Exception e) {
            Log.e(TAG, "storeLatestQRCodePNR: ", e );
            e.printStackTrace();
        }
    }


}
