package com.example.popcolor;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button Upload;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Upload = findViewById(R.id.uploadButton);
        Upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionListener permissionListener = new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        selectImage();
                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {
                        Toast.makeText(MainActivity.this, "Permission Not Granted ", Toast.LENGTH_SHORT).show();

                    }
                };
                TedPermission.with(MainActivity.this)
                        .setPermissionListener(permissionListener)
                        .setPermissions(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .check();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu this adds options to the action bar if it is present. */
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    private void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery","Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo"))
                {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    if(cameraIntent.resolveActivity(getPackageManager()) != null){
                        startActivityForResult(cameraIntent, 1);
                    }
                }
                else if (options[item].equals("Choose from Gallery"))
                {
                    /*
                     Intent imageFromGallery = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    Intent imageFromGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(imageFromGallery, 2);
                    */

                    Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    getIntent.setType("image/*");

                    Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    pickIntent.setType("image/*");

                    Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

                    startActivityForResult(chooserIntent, 2);

                }
                else if (options[item].equals("Cancel")) {
                    Toast.makeText(MainActivity.this, "You cancelled the operation", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                if (data != null) {
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    /* Passing BITMAP to the Second Activity */
                    Intent IntentCamera = new Intent(this, ImageColours.class);
                    IntentCamera.putExtra("BitmapImage", bitmap);
                    startActivity(IntentCamera);
                }
            } else if (requestCode == 2) {
                if (data != null) {
                    Uri selectedImgUri = data.getData();
                    /* Passing ImageURI to the Second Activity */
                    Intent IntentGallery = new Intent(this, ImageColours.class);
                    IntentGallery.setData(selectedImgUri);
                    startActivity(IntentGallery);
                }
            }
        }
    }
}
