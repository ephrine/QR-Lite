package devesh.ephrine.qr.code.pro;


import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

public class MainActivity extends Activity {

    private static final int READ_REQUEST_CODE = 42;

    public String QRcodeText;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private SurfaceView cameraView;
    private TextView barcodeInfo;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


  /*      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
                Log.d("QR","Cam Not Permission ");
            } else if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                ReadQR();
                Log.d("QR","Cam Permission ");
            }
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            ReadQR();
        }
*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        ReadQR();


    }


    public void ReadQR() {
        cameraView = (SurfaceView) findViewById(R.id.surfaceViewQR);
        barcodeInfo = (TextView) findViewById(R.id.textViewQR);

        barcodeDetector =
                new BarcodeDetector.Builder(this)
                        .setBarcodeFormats(Barcode.ALL_FORMATS)  //QR code
                        .build();

        cameraSource = new CameraSource
                .Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1024, 764)
                .setAutoFocusEnabled(true)
                .build();



        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    cameraSource.start(cameraView.getHolder());

                } catch (IOException ie) {
                    Log.e("CAMERA SOURCE", ie.getMessage());
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() != 0) {
                    // Toast.makeText(MainActivity.this, "QR Code Found", Toast.LENGTH_SHORT).show();

                    barcodeInfo.post(new Runnable() {    // Use the post method of the TextView
                        public void run() {
                            barcodeInfo.setText("QR Code: "+    // Update the TextView
                                    barcodes.valueAt(0).displayValue

                            );

                            QRcodeText = barcodes.valueAt(0).displayValue;

                        }
                    });
                }

            }
        });


    }

    public void flash(View v){
        flashOnButton();
    }

    private Camera camera = null;
    boolean flashmode=false;
    private void flashOnButton() {
        camera=getCamera(cameraSource);
        if (camera != null) {
            try {
                Camera.Parameters param = camera.getParameters();
                param.setFlashMode(!flashmode?Camera.Parameters.FLASH_MODE_TORCH :Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(param);
                flashmode = !flashmode;
                if(flashmode){
                    //  showToast("Flash Switched ON");
                }
                else {
                    //showToast("Flash Switched Off");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
    private static Camera getCamera(@NonNull CameraSource cameraSource) {
        Field[] declaredFields = CameraSource.class.getDeclaredFields();

        for (Field field : declaredFields) {
            if (field.getType() == Camera.class) {
                field.setAccessible(true);
                try {
                    Camera camera = (Camera) field.get(cameraSource);
                    if (camera != null) {
                        return camera;
                    }
                    return null;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        return null;
    }


    public void copyQR(View v){
        copyClip();

    }
    public void copyClip() {
        if(QRcodeText!=null) {
            ClipboardManager cp = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("QR code: ", QRcodeText);
            cp.setPrimaryClip(clip);
            Toast.makeText(MainActivity.this, "Text Copied to Clipboard", Toast.LENGTH_SHORT).show();

        }else {
            Toast.makeText(MainActivity.this, "Scarn QR code Before Copying it to clipboard", Toast.LENGTH_SHORT).show();

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

            cameraSource.release();
            barcodeDetector.release();


    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    finish();
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);

                    ReadQR();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    public void info(View v){
        View info=(View)findViewById(R.id.ViewInfo);
        info.setVisibility(View.VISIBLE);
        LinearLayout LL =(LinearLayout)findViewById(R.id.linearLayout);
        LL.setVisibility(View.GONE);
        ImageView TargetIMG=(ImageView)findViewById(R.id.imageView);
        TargetIMG.setVisibility(View.GONE);
        CheckUpdate();




    }
    public void close(View v){
        View info=(View)findViewById(R.id.ViewInfo);
        info.setVisibility(View.GONE);
        Button ShareBT=(Button)findViewById(R.id.buttonShare);
        ShareBT.setVisibility(View.GONE);
        LinearLayout LL =(LinearLayout)findViewById(R.id.linearLayout);
        LL.setVisibility(View.VISIBLE);
        ImageView TargetIMG=(ImageView)findViewById(R.id.imageView);
        TargetIMG.setVisibility(View.VISIBLE);
    }

    public void NativAd(){

    }
    public void GenerateQR(View v){

        EditText et=(EditText)findViewById(R.id.editTextQR);
        ImageView QRimg=(ImageView)findViewById(R.id.imageViewQR);
        String Tx=et.getText().toString();
        if(Tx!=null){
            Toast.makeText(MainActivity.this, "Creating QR Code... Please Wait", Toast.LENGTH_SHORT).show();

            try {
                Bitmap bitmap = encodeAsBitmap(Tx);
                QRimg.setImageBitmap(bitmap);
                Button ShareBT=(Button)findViewById(R.id.buttonShare);
                ShareBT.setVisibility(View.VISIBLE);

            } catch (WriterException e) {
                e.printStackTrace();
            }}else {
            Toast.makeText(MainActivity.this, "Add some Text in Box to Create QR Code", Toast.LENGTH_SHORT).show();
        }
    }

    public final static int WIDTH=500;

    public static int white = 0xFFFFFFFF;
    public static int black = 0xFF000000;
    Bitmap encodeAsBitmap(String str) throws WriterException {
        BitMatrix result;
        Bitmap bitmap=null;
        try
        {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, WIDTH, WIDTH, null);

            int w = result.getWidth();
            int h = result.getHeight();
            int[] pixels = new int[w * h];
            for (int y = 0; y < h; y++) {
                int offset = y * w;
                for (int x = 0; x < w; x++) {
                    pixels[offset + x] = result.get(x, y) ? black:white;
                }
            }
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, 500, 0, 0, w, h);
        } catch (Exception iae) {
            iae.printStackTrace();
            return null;
        }
        return bitmap;
    }
    public void ShareQR(View v){

        EditText et=(EditText)findViewById(R.id.editTextQR);
        ImageView QRimg=(ImageView)findViewById(R.id.imageViewQR);
        String Tx=et.getText().toString();
        if(Tx!=null){

            // save bitmap to cache directory
            try {
                Bitmap bitmap = encodeAsBitmap(Tx);
                Context context=MainActivity.this;
                File cachePath = new File(context.getCacheDir(), "images");
                cachePath.mkdirs(); // don't forget to make the directory
                FileOutputStream stream = new FileOutputStream(cachePath + "/image.png"); // overwrites this image every time
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                stream.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }catch (WriterException e) {
                e.printStackTrace();
            }

            Context context=MainActivity.this;

            File imagePath = new File(context.getCacheDir(), "images");
            File newFile = new File(imagePath, "image.png");
            Uri contentUri = FileProvider.getUriForFile(context, "devesh.ephrine.qr.code.pro.fileprovider", newFile);

            if (contentUri != null) {

                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
                shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                startActivity(Intent.createChooser(shareIntent, "Choose an app"));

            }


        }

    }

    public void reset(View v){
        barcodeInfo = (TextView) findViewById(R.id.textViewQR);
        barcodeInfo.setText("Finding QR code..");
    }



    public void IMGQR(){


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //   cameraSource.release();
            // barcodeDetector.release();

           finish();




            return true;
        }

        return super.onKeyDown(keyCode, event);
    }


    public void CheckUpdate(){

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference Update = database.getReference("VersionCode");
        // Update.setValue("1");
        // Read from the database
        Update.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d(TAG, "Version Code: " + value);
                if(value.equals("1")){
                    Button bt=(Button)findViewById(R.id.buttonUpdate);
                    bt.setVisibility(View.INVISIBLE);
                }else {

                    Button bt=(Button)findViewById(R.id.buttonUpdate);
                    bt.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });


    }

    public void UpdateApp(View v){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=devesh.ephrine.qr.code.pro")); //Google play store
        startActivity(intent);

    }


}
