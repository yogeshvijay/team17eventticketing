package com.team17.secureticketing;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

public class Home extends AppCompatActivity {

    public static  String res="";

    public static TextView mTextView;


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.activity_home);

        mTextView=findViewById(R.id.result);


        CardView generate_qr,scan_qr;

        generate_qr=findViewById(R.id.gen_qr);
        scan_qr=findViewById(R.id.scan_qr);


        generate_qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED&&ActivityCompat.checkSelfPermission(Home.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(Home.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,},0);
                    return;
                }
                Intent i=new Intent(Home.this,GenerateQR.class);
                startActivity(i);
            }
        });

        scan_qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent i=new Intent(Home.this,Verify_Fingerprint.class);
//                startActivity(i);

                Intent intent = new Intent(getApplicationContext(), QRScanner.class);
//                intent.putExtra(DEVICE_EXTRA, device);
//                intent.putExtra(DEVICE_UUID, mDeviceUUID.toString());
//                intent.putExtra(BUFFER_SIZE, mBufferSize);
                startActivity(intent);
            }
        });



    }

}
