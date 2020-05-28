package com.example.popcolor;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Collections;
import java.util.HashSet;

public class ImageColours extends AppCompatActivity {

    TextView domColor;
    ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_colours);
        domColor = findViewById(R.id.textView);
        image = findViewById(R.id.imageView);

        String imgPath = null;
        Bitmap bmp = null;

        // Getting ImageURI from Gallery from Main Activity
        Uri selectedImgUri = getIntent().getData();
        if (selectedImgUri != null) {
            Log.e("Gallery ImageURI", "" + selectedImgUri);
            String[] selectedImgPath = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImgUri,
                    selectedImgPath, null, null, null);
            cursor.moveToFirst();
            int indexCol = cursor.getColumnIndex(selectedImgPath[0]);
            imgPath = cursor.getString(indexCol);
            image.setImageBitmap(BitmapFactory.decodeFile(imgPath));
            bmp = BitmapFactory.decodeFile(imgPath);
            cursor.close();
        }

        // Getting ImageBitmap from Camera from Main Activity
        Intent intent_camera = getIntent();
        if(intent_camera.hasExtra("BitmapImage")) {
            Bitmap camera_img_bitmap = (Bitmap) intent_camera.getParcelableExtra("BitmapImage");
            if (camera_img_bitmap != null) {
                image.setImageBitmap(camera_img_bitmap);
                bmp = camera_img_bitmap;
            }
        }

        new ColorFinder(new ColorFinder.CallbackInterface() {
            @Override
            public void onCompleted(final String color) {
                domColor.setBackgroundColor(Color.parseColor(color));
                domColor.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bitmap bm = ((BitmapDrawable)image.getDrawable()).getBitmap();
                        Bitmap newImg = createNewImage(bm, Color.parseColor(color));
                        image.setImageBitmap(newImg);
                    }
                });
                Toast.makeText(ImageColours.this, "Your Color : " + color, Toast.LENGTH_LONG).show();
                TableLayout tv = findViewById(R.id.tableLayout);
                tv.removeAllViewsInLayout();

                Toast.makeText(ImageColours.this, "Your Size : " + ColorFinder.allColorList.size(), Toast.LENGTH_LONG).show();
                Collections.sort(ColorFinder.allColorList);
                HashSet<Integer> set = new HashSet<Integer>();
                int count = 0;
                for(PixelObject colorp: ColorFinder.allColorList){
                    if(set.contains(colorp.pixel)){
                        continue;
                    }
                    set.add(colorp.pixel);
                    if(count > 25){
                        break;
                    }
                    TableRow tr = new TableRow(ImageColours.this);
                    tr.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                            TableLayout.LayoutParams.WRAP_CONTENT));
                    TextView hexvalue = new TextView(ImageColours.this);
                    hexvalue.setPadding(20, 20, 40,20);
                    String str = colorToHex(colorp.pixel);
                    hexvalue.setText(str);
                    hexvalue.setTextSize(20);
                    hexvalue.setTextColor(Color.BLACK);
                    tr.addView(hexvalue);

                    TextView colorbox = new TextView(ImageColours.this);
                    colorbox.setPadding(20, 20, 20, 20);
                    colorbox.setBackgroundColor(colorp.pixel);
                    colorbox.setWidth(500);
                    tr.addView(colorbox);

                    tv.addView(tr);
                    final View view = new View(ImageColours.this);
                    view.setLayoutParams(new
                            TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1));
                    view.setBackgroundColor(Color.WHITE);
                    tv.addView(view);  // add line below each row
                    count++;
                }
            }
        }).findDominantColor(bmp);
    }
    private String colorToHex(int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }

    private Bitmap createNewImage(Bitmap bmp, Integer color){
        Bitmap nImg = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
        float[] hsvm = new float[3];
        float[] blackhsv = new float[3];
        Color.colorToHSV(color, hsvm);
        Color.colorToHSV(Color.BLACK, blackhsv);
        int setcolor = Color.BLACK;
        if ((int) hsvm[0] == (int) blackhsv[0] && (int) hsvm[1] == (int) blackhsv[1] && (int) hsvm[2] == (int) blackhsv[2]) {
            setcolor = Color.WHITE;
        }

        for (int i = 0; i < bmp.getWidth(); i++) {
            for (int j = 0; j < bmp.getHeight(); j++) {
                int pixel = bmp.getPixel(i, j);
                float[] hsv = new float[3];
                Color.colorToHSV(pixel, hsv);
                if ((int) hsv[0] == (int) hsvm[0] && (int) hsv[1] == (int) hsvm[1] && (int) hsv[2] == (int) hsvm[2]) {
                    nImg.setPixel(i, j, color);
                } else {
                    nImg.setPixel(i, j, setcolor);
                }
            }
        }
        return nImg;
    }
}







/*
        im = findViewById(R.id.imageView);

        Intent intent = getIntent();
        Uri imageUri = Uri.parse(intent.getStringExtra("IMAGE"));

        Glide.with(ImageColours.this)
                .load(imageUri)
                .into(im);
        //Bitmap bitmap = (Bitmap) intent.getParcelableExtra("IMAGE");
        //im.setImageBitmap(bitmap);

        /*
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            Bitmap image = (Bitmap) bundle.get("IMAGE");
            Glide.with(ImageColours.this)
                    .load(image)
                    .into(im);
        }

         */