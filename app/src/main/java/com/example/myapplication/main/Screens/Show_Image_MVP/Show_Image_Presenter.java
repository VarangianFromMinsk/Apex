package com.example.myapplication.main.Screens.Show_Image_MVP;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;

public class Show_Image_Presenter {

    private final Show_Image_view view;

    public Show_Image_Presenter(Show_Image_view view) {
        this.view = view;
    }

    public void saveImageInGallery(ImageView showImage, Show_Image_Activity activity){

        Drawable drawable = null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            drawable = showImage.getDrawable();

            //todo: Get the bitmap from drawable object
            Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();

            //todo: Save image to gallery
            String savedImageURL = MediaStore.Images.Media.insertImage(
                    activity.getContentResolver(),
                    bitmap,
                    "Apex",
                    String.valueOf(System.currentTimeMillis())
            );

            // Parse the gallery image url to uri
            Uri savedImageURI = Uri.parse(savedImageURL);
            view.saved();
        }
    }
}
