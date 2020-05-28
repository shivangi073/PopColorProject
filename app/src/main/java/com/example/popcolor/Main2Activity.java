package com.example.popcolor;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class Main2Activity extends Activity implements Callback,OnClickListener , Camera.PreviewCallback{

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;

    private Camera camera=null;
    private Button flipCamera;
    private Button flashCameraButton;
    private Button captureImageButton;
    private int cameraId;
    private boolean flashmode = false;
    private int rotation;
    private Camera.Size prevsize;

    TextView redtv,greentv, bluetv , hexcodetv ,tvcolor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        /*
         camera surface view created
        surface view helps camera to be displayed on screen
        */
        cameraId = CameraInfo.CAMERA_FACING_BACK;
        flipCamera = (Button) findViewById(R.id.flipCamera);
        flashCameraButton = (Button) findViewById(R.id.flash);
        captureImageButton=(Button)findViewById(R.id.button);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();//surface holder objects
        surfaceHolder.addCallback(this);

        /* listener buttons */
        flipCamera.setOnClickListener(this);
        flashCameraButton.setOnClickListener(this);
        captureImageButton.setOnClickListener(this);

        redtv=findViewById(R.id.redtextView);
        greentv=findViewById(R.id.greentextView);
        bluetv=findViewById(R.id.bluetextView);
        hexcodetv=findViewById(R.id.hexcodeTV);

        tvcolor=findViewById(R.id.tv);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        /* to keep screen on until activity is going on */

        if (Camera.getNumberOfCameras() > 1) {
            flipCamera.setVisibility(View.VISIBLE);
        }
        if (!getBaseContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA_FLASH)) {
            flashCameraButton.setVisibility(View.GONE);
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!openCamera(CameraInfo.CAMERA_FACING_BACK)) {/*
            open the camera
            if camera permission not granted ask for permission
            */
            Intent intent = new Intent(Main2Activity.this,
                    MainScreenActivity.class);
            startActivity(intent);
        }

        camera.setPreviewCallback(Main2Activity.this);
        Camera.Parameters parameters=camera.getParameters();
        prevsize=parameters.getPreviewSize();

        parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        camera.setParameters(parameters);
    }

    /* open the camera */
    private boolean openCamera(int id) {
        boolean result = false;
        cameraId = id;
        releaseCamera();
        try {
            camera = Camera.open(cameraId);
            /* camera.setPreviewCallback(Main2Activity.this); */
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (camera != null) {
            try {
                setUpCamera(camera);
                camera.setErrorCallback(new ErrorCallback() {

                    @Override
                    public void onError(int error, Camera camera) {

                    }
                });


                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
                camera.setPreviewCallback(Main2Activity.this);

                result = true;
            } catch (IOException e) {
                e.printStackTrace();
                result = false;
                releaseCamera();
                camera.stopPreview();
                camera=null;
            }
        }
        return result;
    }

    /* this method manage rotation of camera */
    private void setUpCamera(Camera c) {
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degree = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degree = 0;
                break;
            case Surface.ROTATION_90:
                degree = 90;
                break;
            case Surface.ROTATION_180:
                degree = 180;
                break;
            case Surface.ROTATION_270:
                degree = 270;
                break;

            default:
                break;
        }

        if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
            // frontFacing
            rotation = (info.orientation + degree) % 330;
            rotation = (360 - rotation) % 360;
        } else {
            // Back-facing
            rotation = (info.orientation - degree + 360) % 360;
        }
        c.setDisplayOrientation(rotation);
        Parameters params = c.getParameters();

        showFlashButton(params);/* this manages flash */

        List<String> focusModes = params.getSupportedFlashModes();
        if (focusModes != null) {
            if (focusModes
                    .contains(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                params.setFlashMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
        }

        params.setRotation(rotation);
    }


    /* this method manages flash */
    private void showFlashButton(Parameters params) {
        boolean showFlash = (getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA_FLASH) && params.getFlashMode() != null)
                && params.getSupportedFlashModes() != null
                && params.getSupportedFocusModes().size() > 1;

        flashCameraButton.setVisibility(showFlash ? View.VISIBLE
                : View.INVISIBLE);

    }


    /* this will stop whichever camera is running (back or front)
        and make the camera object null for further use. */
    private void releaseCamera() {
        try {
            if (camera != null) {
                camera.setPreviewCallback(null);
                camera.setErrorCallback(null);
                camera.release();
                camera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("error", e.toString());
            camera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        camera.startPreview();
        try {
            if(captureImageButton.isSelected())
            {
                camera.setPreviewCallback(null);
            }else{
                camera.setPreviewCallback(Main2Activity.this);
                camera.setPreviewDisplay(holder);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        releaseCamera();
        camera=null;
    }

    @Override
    /* this method responses when button is clicked */
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.flash:
                flashOnButton();
                break;
            case R.id.flipCamera:
                flipCamera();
                break;
            case R.id.button:
                Intent i=new Intent(this, MainActivity.class);
                startActivity(i);
            default:
                break;
        }
    }


    //this method flip the camera
    private void flipCamera() {
        int id = (cameraId == CameraInfo.CAMERA_FACING_BACK ? CameraInfo.CAMERA_FACING_FRONT
                : CameraInfo.CAMERA_FACING_BACK);

        if (!openCamera(id)) {
            openCamera(id);
            camera.setPreviewCallback(Main2Activity.this);
            alertCameraDialog();
        }
    }

    //display an error if any occurred while opening front or back camera
    private void alertCameraDialog() {

        Builder dialog = createAlert(Main2Activity.this,"Camera info", "error to open camera");
        dialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

            }
        });
        dialog.show();
    }

    private Builder createAlert(Context context, String title, String message) {

        Builder dialog = new Builder(
                new ContextThemeWrapper(context,
                        android.R.style.Theme_Holo_Light_Dialog));
        dialog.setIcon(R.mipmap.ic_launcher);
        if (title != null)
            dialog.setTitle(title);
        else
            dialog.setTitle("Information");
        dialog.setMessage(message);
        dialog.setCancelable(false);
        return dialog;

    }

    private void flashOnButton() {
        if (camera != null) {
            try {
                Parameters param = camera.getParameters();

                if(flashmode==false )
                {
                    param.setFlashMode(Parameters.FLASH_MODE_TORCH);
                    flashmode=true;
                }
                else{
                    param.setFlashMode(Parameters.FLASH_MODE_OFF);
                    flashmode=false;
                }
                camera.setParameters(param);

            } catch (Exception e) {
                // TODO: handle exception
            }

        }
    }


    /* for color detection */
    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {

        Camera.Parameters parameters = camera.getParameters();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        YuvImage yuvImage = new YuvImage(bytes, parameters.getPreviewFormat(), parameters.getPreviewSize().width, parameters.getPreviewSize().height, null);
        yuvImage.compressToJpeg(new Rect(0, 0, parameters.getPreviewSize().width, parameters.getPreviewSize().height), 90, out);
        byte[] imageBytes = out.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

        int width=bitmap.getWidth() /2;
        int height=bitmap.getHeight() /2;

        int pixel=bitmap.getPixel(width,height);


        int redValue = Color.red(pixel);
        int blueValue = Color.blue(pixel);
        int greenValue = Color.green(pixel);


        if(redValue <100  && greenValue<100 && blueValue<100  && flashmode==false){

            Toast.makeText(this, "Turn on Flash" , Toast.LENGTH_SHORT).show();
        }

        redtv.setText(Integer.toString(redValue));
        greentv.setText(Integer.toString(greenValue));
        bluetv.setText(Integer.toString(blueValue));


        String hexColor = String.format( "#%02x%02x%02x", redValue, greenValue, greenValue );
        hexcodetv.setText(hexColor);

        tvcolor.setBackgroundColor(Color.parseColor(hexColor));


        try {
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}