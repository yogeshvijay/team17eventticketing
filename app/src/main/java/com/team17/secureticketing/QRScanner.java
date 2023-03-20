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

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result ->
    {
        if (result.getContents() != null) {

            //Getting the data from Firebase

            final String cipherText = result.getContents();


            firestore = FirebaseFirestore.getInstance();

            System.out.println("before firebase call");

            firestore.collection("QRDetails").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {

                @Override
                public void onSuccess(QuerySnapshot documentSnapshots) {

                    if (documentSnapshots.isEmpty()) {
                        Log.d(TAG, "onSuccess: LIST EMPTY");
                        return;
                    } else {

                        List<QRData> qrDataList = documentSnapshots.toObjects(QRData.class);

                        System.out.println("inside on success list full " + qrDataList.size());

                        System.out.println(qrDataList.get(0).toString());

                        for (QRData qrData : qrDataList) {
                            if (cipherText.equals(qrData.getCipherText())) {
                                System.out.println(qrData.getUserName());
                                customerData = qrData;

                                AlertDialog.Builder builder = new AlertDialog.Builder(QRScanner.this);
                                builder.setTitle("Ticket Details");
                                builder.setMessage(customerData.toString());
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

            //Decryption

            //String refNo = decrypt(algorithm, cipherText, customerData.getKey(), customerData.getIvParameterSpec());


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


    public static String decrypt(String algorithm, String cipherText, SecretKey key,
                                 IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] plainText = new byte[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            plainText = cipher.doFinal(Base64.getDecoder()
                    .decode(cipherText));
        }
        return new String(plainText);
    }

}
