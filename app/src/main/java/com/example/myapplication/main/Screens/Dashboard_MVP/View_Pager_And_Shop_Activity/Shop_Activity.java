package com.example.myapplication.main.Screens.Dashboard_MVP.View_Pager_And_Shop_Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.myapplication.R;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class Shop_Activity extends AppCompatActivity {
    private final String[] addresses = { "VarangianMinsk@yandex.ru"};
    private String EmailText;
    private Uri attachment;
    private TextView OrderTextView, StaffTextView, quantityTextView,priceTextView, onestafprice ;

    private static final DecimalFormat df = new DecimalFormat("0.00");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shop_activity);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setTitle("ArmourCart");

        initialization();

        getMainIntent();
    }

    private void initialization(){
        OrderTextView=findViewById(R.id.OrderTextName);
        StaffTextView=findViewById(R.id.OrderTextNameStaff);
        quantityTextView=findViewById(R.id.quantityTextNameStaff);
        priceTextView=findViewById(R.id.PriceTextNameStaff);
        onestafprice=findViewById(R.id.OneStafPrice);
    }

    void getMainIntent(){
        Intent intent = getIntent();

        String userName=intent.getStringExtra("userName");
        String staffName=intent.getStringExtra("typeOfStaff");
        int quantity=intent.getIntExtra("numberOfStaff",1);
        int onePrice=intent.getIntExtra("priceForOne",0);

        int price = quantity * onePrice;

        OrderTextView.setText(userName);
        StaffTextView.setText(staffName);
        quantityTextView.setText(String.valueOf(quantity));
        df.setRoundingMode(RoundingMode.UP);
        priceTextView.setText(String.valueOf(df.format(price) +" $"));
        onestafprice.setText(String.valueOf(onePrice+" $"));

        if(userName != null){
            EmailText=("For " + userName + " ordered " + quantity +" units " + staffName + " for the amount of " + price +" $" + " (Price for one: " + onePrice+" $" + ").");
        }
        EmailText=("For " + "name unknow" + " ordered " + quantity +" units " + staffName + " for the amount of " + price +" $" + " (Price for one: " + onePrice+" $" + ").");
    }



    public void Email(View view) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        String subject = "Model_Order From Armour Shop";
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, EmailText);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void Messenger(View view) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT,EmailText );
        intent.putExtra(Intent.EXTRA_STREAM, attachment);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void CallToSupport (View view) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        String phoneNumber = "+375447546898";
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }


}