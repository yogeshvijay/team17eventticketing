package com.team17.secureticketing;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class QRScanner extends AppCompatActivity {

    FirebaseFirestore firestore;

    QRData customerData;

    private String cipherTextFinal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scanCode();

    }

    private void scanCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {

            //Getting the data from Firebase

            final String cipherText = result.getContents();

            firestore = FirebaseFirestore.getInstance();

            firestore.collection("QRDetails").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {

                @Override
                public void onSuccess(QuerySnapshot documentSnapshots) {

                    if (documentSnapshots.isEmpty()) {
                        Log.d(TAG, "onSuccess: LIST EMPTY");
                        return;
                    } else {

                        List<QRData> qrDataList = documentSnapshots.toObjects(QRData.class);

                        System.out.println("inside on success list full " + qrDataList.size());

                        String data;

                        for (QRData qrData : qrDataList) {
                            if (cipherText.equals(qrData.getCipherText())) {
                                System.out.println(qrData.getUserName());
                                customerData = qrData;

                                System.out.println("++++++++++++=++++++++++++=++++++++++++=++++++++++++=++++++++++++= inside success");

                                try {
                                     data = decryptRSA(customerData.getCipherText());
                                } catch (NoSuchPaddingException | NoSuchAlgorithmException |
                                         IllegalBlockSizeException | BadPaddingException |
                                         InvalidKeyException | InvalidKeySpecException e) {
                                    throw new RuntimeException(e);
                                }

                                System.out.println(" This is the decrypted text +++++++++++++++++++++++ " + data);

                                AlertDialog.Builder builder = new AlertDialog.Builder(QRScanner.this);
                                builder.setTitle("Ticket Details");
                                builder.setMessage("PNR Number :  " + data);
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                }).show();
                            }
                        }

                    }
                }
            });


        } else {

            AlertDialog.Builder builder = new AlertDialog.Builder(QRScanner.this);
            builder.setTitle("Ticket Details");
            builder.setMessage("Not a valid QR Code");
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }).show();

        }
    });


//    public static String decrypt(String algorithm, String cipherText, SecretKey key, IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
//
//        Cipher cipher = Cipher.getInstance(algorithm);
//        cipher.init(Cipher.DECRYPT_MODE, key, iv);
//        byte[] plainText = new byte[0];
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText));
//        }
//        return new String(plainText);
//    }

    public String decryptRSA(String cipherTextFinal) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {

        Cipher decryptCipher = Cipher.getInstance("RSA");

        //Read the File Private Key
        File readFile = new File(getFilesDir(), "private.key");

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

        String privateKeyText = sb.toString();

        byte[] privateKeyBytes = new byte[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            privateKeyBytes = Base64.getDecoder().decode(privateKeyText);
        }

        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));

        decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] decryptedMessageBytes = new byte[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            decryptedMessageBytes = decryptCipher.doFinal(Base64.getDecoder().decode(cipherTextFinal));
        }
        String decryptedMessage = new String(decryptedMessageBytes, StandardCharsets.UTF_8);

        return decryptedMessage;
    }

}
