package com.team17.secureticketing;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
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

    private static final String IMAGE_DIRECTORY = Environment.getExternalStorageDirectory().getPath() + "/EventQR/";

    String TAG = "GenerateQrCode";

    ImageView qrimg;
    Bitmap mBitmap;

    QRGEncoder mQRGEncoder;

    Button mm, exit, save;

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

        //RSA Algorithm Check

        KeyPairGenerator generator = null;
        try {
            generator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();


        PrivateKey privateKey = pair.getPrivate();
        PublicKey publicKey = pair.getPublic();

        // Save the file
        File file = new File(getFilesDir(), "public.key");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        OutputStreamWriter osw = new OutputStreamWriter(fos);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                osw.write(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            osw.flush();
            osw.close();
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Save the file Private Key
        File privateKeyFile = new File(getFilesDir(), "private.key");

        FileOutputStream fos1 = null;
        try {
            fos1 = new FileOutputStream(privateKeyFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        OutputStreamWriter osw1 = new OutputStreamWriter(fos1);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                osw1.write(Base64.getEncoder().encodeToString(privateKey.getEncoded()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            osw1.flush();
            osw1.close();
            fos1.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Read the File
        File readFile = new File(getFilesDir(), "public.key");

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(readFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);

        StringBuilder sb = new StringBuilder();
        String line;
        while (true) {
            try {
                if (!((line = br.readLine()) != null)) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            sb.append(line);
        }

        String publicKeyText = sb.toString();

        System.out.println("+++++++++++++++++++++++++++++++++++= " + publicKeyText);

        try {
            br.close();
            isr.close();
            fis.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        byte[] publicKeyBytes = new byte[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            publicKeyBytes = Base64.getDecoder().decode(publicKeyText);
        }


        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (Base64.getEncoder().encodeToString(publicKey.getEncoded()).equals(Base64.getEncoder().encodeToString(publicKeyBytes))) {
                System.out.println("its the same");
            }
            else {
                System.out.println("not the same");
            }
        }

        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            System.out.println("this is the public key +++ " + Base64.getEncoder().encodeToString(publicKey.getEncoded()));
        }

        try {
            keyFactory.generatePublic(publicKeySpec);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }

        Cipher encryptCipher = null;
        try {
            encryptCipher = Cipher.getInstance("RSA");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
        try {
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }

        byte[] secretMessageBytes = mUser.getPnr().getBytes(StandardCharsets.UTF_8);
        byte[] encryptedMessageBytes;
        try {
            encryptedMessageBytes = encryptCipher.doFinal(secretMessageBytes);
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        }

        String encodedMessage = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            encodedMessage = Base64.getEncoder().encodeToString(encryptedMessageBytes);
        }

        //End RSA

        try {
            cipherText = this.encryptString(mUser.getPnr());
        } catch (NoSuchAlgorithmException | IllegalBlockSizeException | InvalidKeyException |
                 BadPaddingException | InvalidAlgorithmParameterException |
                 NoSuchPaddingException e) {
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
        mQRGEncoder = new QRGEncoder(encodedMessage, null, QRGContents.Type.TEXT, smallerdimen);

        try {
            mBitmap = mQRGEncoder.encodeAsBitmap();
            qrimg.setImageBitmap(mBitmap);
        } catch (WriterException e) {
            Log.v(TAG, e.toString());
        }

        firestore = FirebaseFirestore.getInstance();

        QRData dataStore = new QRData();

        dataStore.setCipherText(encodedMessage);
        dataStore.setUserName(mUser.getName());

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
                try {
//                    QRGSaver.save(IMAGE_DIRECTORY,input.trim(),mBitmap,QRGContents.ImageType.IMAGE_JPEG);
                    QRGSaver.save(IMAGE_DIRECTORY, "Team 17 Event", mBitmap, QRGContents.ImageType.IMAGE_JPEG);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                AlertDialog.Builder ab = new AlertDialog.Builder(QRCode.this);
                ab.setTitle("Info");
                ab.setMessage("File Saved to " + IMAGE_DIRECTORY);
                ab.setPositiveButton("OK", null);
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
            Log.e(TAG, "storeLatestQRCodePNR: ", e);
            e.printStackTrace();
        }
    }


}
