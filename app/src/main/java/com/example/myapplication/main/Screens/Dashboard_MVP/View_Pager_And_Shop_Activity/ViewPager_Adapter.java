package com.example.myapplication.main.Screens.Dashboard_MVP.View_Pager_And_Shop_Activity;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.example.myapplication.R;
import com.example.myapplication.main.Models.Model_ViewPager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class ViewPager_Adapter extends PagerAdapter {

    private final Context context;
    private final ArrayList<Model_ViewPager> shopList;
    private int number = 1;
    private int allPriceSum = 0;
    private String userName;

    private int mPos = 0;

    public ViewPager_Adapter(Context context, ArrayList<Model_ViewPager> shopList) {
        this.context = context;
        this.shopList = shopList;
    }


    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }


    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        number = 1;
        allPriceSum = 0;

        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_shop_viewpager, container, false);

        //init views
        ImageView bannerIv = view.findViewById(R.id.bannerIv);
        TextView titleTv = view.findViewById(R.id.titleViewPager);
        TextView descriptionTv = view.findViewById(R.id.descriptionViewPager);

        ImageButton plusBtn = view.findViewById(R.id.plusStaff);
        ImageButton minusBtn = view.findViewById(R.id.minusStaff);
        TextView numberOfStaff = view.findViewById(R.id.numberOfStaff);
        TextView priceOfStaff = view.findViewById(R.id.priceOfStaff);

        //get data
        Model_ViewPager model = shopList.get(mPos);
        int image = model.getImage();
        int price = model.getPrice();
        String titleData = model.getTitle();
        String descriptionData = model.getDescription();

        //set Data
        bannerIv.setImageResource(image);
        titleTv.setText(titleData);
        descriptionTv.setText(descriptionData);


        //methods
        numberOfStaff.setText(String.valueOf(number));
        allPriceSum = number * price;
        priceOfStaff.setText(String.valueOf(allPriceSum + " $"));

        //get info about user
        String currentUser = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        DatabaseReference usersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        Query userQuery = usersDatabaseReference.orderByChild("firebaseId").equalTo(currentUser);
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    userName = Objects.requireNonNull(ds.child("name").getValue()).toString();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //set Btns
        plusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(number >= 99){
                    Toast.makeText(context, "No more", Toast.LENGTH_SHORT).show();
                }
                else{
                    number++;
                    allPriceSum = number * price;
                    numberOfStaff.setText(String.valueOf(number));
                    priceOfStaff.setText(String.valueOf(allPriceSum + " $"));
                }
            }
        });

        minusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(number >=1){
                    number--;
                    allPriceSum = number * price;
                    numberOfStaff.setText(String.valueOf(number));
                    priceOfStaff.setText(String.valueOf(allPriceSum + " $"));
                }
            }
        });

        //handel card click
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //method of shop
                Intent intent = new Intent(context, Shop_Activity.class);
                intent.putExtra("userName", userName);
                intent.putExtra("typeOfStaff", titleData);
                intent.putExtra("numberOfStaff", number);
                intent.putExtra("priceForOne", price);
                context.startActivity(intent);
            }
        });



        //add view to container
        container.addView(view);

        if(mPos >= shopList.size() - 1){
            mPos = 0;
        }else if(mPos < 8) {
            mPos++;
        }


        return view;
    }


    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }
}
