package com.team17.secureticketing;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Random;

public class GenerateQR extends AppCompatActivity {

    EditText name_o_p,ac_no,sta,ct,str,post,doj,adh_no,mob_no;
    Spinner ac_name;
    int d,m,y;

    String name,activityname,activityno,state,city,street,postcode,date,emergency,mobile;
    Button sc_fg;
    String[] eventName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_generate_qr);


        sc_fg=findViewById(R.id.fingerp);


        name_o_p=findViewById(R.id.name);
        ac_name=findViewById(R.id.activity_name);
//        ac_no=findViewById(R.id.activity_no);
        sta=findViewById(R.id.st_name);
        ct=findViewById(R.id.city_name);
        str=findViewById(R.id.street);
        post=findViewById(R.id.postcode);
        doj=findViewById(R.id.journey_date);
        adh_no=findViewById(R.id.emergency);
        mob_no=findViewById(R.id.mobile);

        Calendar calendar=Calendar.getInstance();
        d= calendar.get(Calendar.DAY_OF_MONTH);
        m=calendar.get(Calendar.MONTH);
        y=calendar.get(Calendar.YEAR);
        ArrayAdapter<CharSequence>adapter= ArrayAdapter.createFromResource(this, R.array.stars, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        ac_name.setAdapter(adapter);
        eventName = getResources().getStringArray(R.array.stars);
        doj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(GenerateQR.this,listener,y,m,d).show();
            }
        });
        ac_name.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView parent, View view, int position, long id) {
                activityname = eventName[position];
            }
            @Override
            public void onNothingSelected(AdapterView parent) {
            }
        });
        sc_fg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                name=name_o_p.getText().toString();
              //  activityname=ac_name.getText().toString();
       /*         activityno=ac_no.getText().toString();*/

                state=sta.getText().toString();
                city=ct.getText().toString();
                street=str.getText().toString();
                postcode=post.getText().toString();
                date=doj.getText().toString();
                emergency=adh_no.getText().toString();
                mobile=mob_no.getText().toString();

                if(name.isEmpty()||activityname.isEmpty()||state.isEmpty()||city.isEmpty() ||street.isEmpty()||postcode.isEmpty()||date.isEmpty()||emergency.isEmpty()||mobile.isEmpty()){
                    Toast.makeText(GenerateQR.this, "Please fill all the Details!", Toast.LENGTH_SHORT).show();
                }
                else {
                    User user = new User();

                    int random = new Random().nextInt(100000000) + 999999999;
                    String pnr = String.valueOf(random).trim();
                    user.setPnr(pnr);
                    user.setName(name);
                   user.setActivityname(activityname);
     /*               user.setActivityno(activityno);*/
                    user.setState(state);
                    user.setCity(city);
                    user.setStreet(street);
                    user.setPostCode(postcode);
                    user.setDate(date);
                    user.setEmergency(emergency);
                    user.setMobile(mobile);


//                    Intent g = new Intent(GenerateQR.this, ScanFingerprint.class);
//                    g.putExtra("user", user);
//                    startActivity(g);
//                    finish();

                    Intent i = new Intent(GenerateQR.this, QRCode.class);

                    i.putExtra("user", user);
                    startActivity(i);
                    finish();
                }

            }
        });



    }

    DatePickerDialog.OnDateSetListener listener=new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
            doj.setText(i2+"/"+(i1+1)+"/"+i);
            d=i2;
            m=i1;
            y=i;
        }
    };
}
