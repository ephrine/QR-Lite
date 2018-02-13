package devesh.ephrine.qr.code.pro;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
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
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends Activity {

    public final static int WIDTH = 500;
    private static final int READ_REQUEST_CODE = 42;
    private static final String TAG = "QR Lite";
    public static int white = 0xFFFFFFFF;
    public static int black = 0xFF000000;
    public String QRcodeText;
    boolean flashmode = false;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private SurfaceView cameraView;
    private TextView barcodeInfo;

    private Camera camera = null;
    public Uri QRImgURI;

    public String OldText;

    public String QRCodeUrlText;

    public String SettingVibrator;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

OldText="0";



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        ReadQR();

        TextView URLFoundTx=(TextView)findViewById(R.id.textView11FoundURL);
        URLFoundTx.setVisibility(View.INVISIBLE);

        CardView CardViewResults=(CardView)findViewById(R.id.CardViewResults);
       // CardViewResults.setVisibility(View.INVISIBLE);

        ImageView OpenURL=(ImageView)findViewById(R.id.imageView8OpenURL);
        OpenURL.setVisibility(View.INVISIBLE);



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
                            barcodeInfo.setText(" " +    // Update the TextView
                                    barcodes.valueAt(0).displayValue);

                            ImageView GreenSq=(ImageView)findViewById(R.id.imageViewGreenSq);
                            Animation GreenAqAnim =
                                    AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);
                            GreenSq.startAnimation(GreenAqAnim);

                            CardView CardViewResults=(CardView)findViewById(R.id.CardViewResults);
                            Animation animation1 =
                                    AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slideleft);

                            CardViewResults.setVisibility(View.VISIBLE);
                            if(OldText.equals(QRcodeText)){

                            }else {
                                CardViewResults.startAnimation(animation1);
                                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                v.vibrate(70);

                            }


                            QRcodeText = barcodes.valueAt(0).displayValue;
                            OldText=QRcodeText;
                            pullLinks(QRcodeText);




                        }
                    });
                }

            }
        });


    }

    public void flash(View v) {
        flashOnButton();
    }

    private void flashOnButton() {
        camera = getCamera(cameraSource);
        if (camera != null) {
            try {
                Camera.Parameters param = camera.getParameters();
                param.setFlashMode(!flashmode ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(param);
                flashmode = !flashmode;
                if (flashmode) {
                    //  showToast("Flash Switched ON");
                } else {
                    //showToast("Flash Switched Off");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void copyQR(View v) {
        copyClip();

    }

    public void copyClip() {
        if (QRcodeText != null) {
            ClipboardManager cp = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("QR code: ", QRcodeText);
            cp.setPrimaryClip(clip);
            Toast.makeText(MainActivity.this, "Text Copied to Clipboard", Toast.LENGTH_SHORT).show();

        } else {
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

    public void info(View v) {
        View info = (View) findViewById(R.id.ViewInfo);

        Animation animation1 =
                AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slideup);
        info.startAnimation(animation1);
        info.setVisibility(View.VISIBLE);


        LinearLayout LL = (LinearLayout) findViewById(R.id.linearLayout);
        LL.setVisibility(View.GONE);
        ImageView TargetIMG = (ImageView) findViewById(R.id.imageViewGreenSq);
        TargetIMG.setVisibility(View.GONE);

//Ad1
/*
        MobileAds.initialize(this, AppID);
        NativeExpressAdView NadView = (NativeExpressAdView) findViewById(R.id.adViewNativ);
        AdRequest request = new AdRequest.Builder().build();
        NadView.loadAd(request); */
        CheckUpdate();
        //NativAd();

        //Ad2
  /*      NativeExpressAdView NadView1 = (NativeExpressAdView) findViewById(R.id.adViewNativ1);
        AdRequest request1 = new AdRequest.Builder().build();
        NadView1.loadAd(request1);
*/


    }

    public void close(View v) {
        final View info = (View) findViewById(R.id.ViewInfo);
        Animation animation1 =
                AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slidedown);
        info.startAnimation(animation1);
        animation1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                info.setVisibility(View.GONE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        Button ShareBT = (Button) findViewById(R.id.buttonShare);
        ShareBT.setVisibility(View.GONE);
        LinearLayout LL = (LinearLayout) findViewById(R.id.linearLayout);
        LL.setVisibility(View.VISIBLE);
        ImageView TargetIMG = (ImageView) findViewById(R.id.imageViewGreenSq);
        TargetIMG.setVisibility(View.VISIBLE);

    }



    public void GenerateQR(View v) {

        EditText et = (EditText) findViewById(R.id.editTextQR);
        ImageView QRimg = (ImageView) findViewById(R.id.imageViewQR);
        String Tx = et.getText().toString();
        if (Tx != null) {
            Toast.makeText(MainActivity.this, "Creating QR Code... Please Wait", Toast.LENGTH_SHORT).show();

            try {
                Bitmap bitmap = encodeAsBitmap(Tx);
                QRimg.setImageBitmap(bitmap);
                Button ShareBT = (Button) findViewById(R.id.buttonShare);
                ShareBT.setVisibility(View.VISIBLE);

            } catch (WriterException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(MainActivity.this, "Add some Text in Box to Create QR Code", Toast.LENGTH_SHORT).show();
        }
    }

    Bitmap encodeAsBitmap(String str) throws WriterException {
        BitMatrix result;
        Bitmap bitmap = null;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, WIDTH, WIDTH, null);

            int w = result.getWidth();
            int h = result.getHeight();
            int[] pixels = new int[w * h];
            for (int y = 0; y < h; y++) {
                int offset = y * w;
                for (int x = 0; x < w; x++) {
                    pixels[offset + x] = result.get(x, y) ? black : white;
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

    public void ShareQR(View v) {

        EditText et = (EditText) findViewById(R.id.editTextQR);
        ImageView QRimg = (ImageView) findViewById(R.id.imageViewQR);
        String Tx = et.getText().toString();
        if (Tx != null) {

            // save bitmap to cache directory
            try {
                Bitmap bitmap = encodeAsBitmap(Tx);
                Context context = MainActivity.this;
                File cachePath = new File(context.getCacheDir(), "images");
                cachePath.mkdirs(); // don't forget to make the directory
                FileOutputStream stream = new FileOutputStream(cachePath + "/image.png"); // overwrites this image every time
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                stream.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (WriterException e) {
                e.printStackTrace();
            }

            Context context = MainActivity.this;

            File imagePath = new File(context.getCacheDir(), "images");
            File newFile = new File(imagePath, "image.png");
            Uri contentUri = FileProvider.getUriForFile(context, "devesh.ephrine.qr.code.fileprovider", newFile);

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

    public void reset(View v) {
        barcodeInfo = (TextView) findViewById(R.id.textViewQR);
        barcodeInfo.setText("Finding QR code..");
        final CardView CardViewResults=(CardView)findViewById(R.id.CardViewResults);

        Animation animation1 =
                AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slideright);
        CardViewResults.startAnimation(animation1);
animation1.setAnimationListener(new Animation.AnimationListener() {
    @Override
    public void onAnimationStart(Animation animation) {
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        CardViewResults.setVisibility(View.INVISIBLE);


    }

    @Override
    public void onAnimationRepeat(Animation animation) {
    }
});


        ImageView OpenURL=(ImageView)findViewById(R.id.imageView8OpenURL);
        OpenURL.setVisibility(View.INVISIBLE);

        TextView URLFoundTx=(TextView)findViewById(R.id.textView11FoundURL);
        URLFoundTx.setVisibility(View.INVISIBLE);
QRCodeUrlText=null;
OldText="0";

    }






    public void CheckUpdate() {

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference Update = database.getReference("VersionCode");
        // Update.setValue("1");

        //final String AppVersionCode=getString(R.string.app_version_code);
        final String AppVersionCode=String.valueOf(BuildConfig.VERSION_CODE);

        Update.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d(TAG, "Version Code: " + value);
                if (value.equals(AppVersionCode)) {
                    Button bt = (Button) findViewById(R.id.buttonUpdate);
                    bt.setVisibility(View.INVISIBLE);
                } else {
                    Button bt = (Button) findViewById(R.id.buttonUpdate);
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

    public void UpdateApp(View v) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=devesh.ephrine.qr.code")); //Google play store
        startActivity(intent);

    }

    public void QRgallery(View v){

performFileSearch();
        Toast.makeText(MainActivity.this, "Select QR Code Image from phone", Toast.LENGTH_LONG).show();

    }

    public void performFileSearch() {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("image/*");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            if (resultData != null) {
                QRImgURI = resultData.getData();
                setimg();
            }


        }
    }

    public void setimg(){
        TextView txtView = (TextView) findViewById(R.id.textViewQRImgText1);

        ImageView QRimg=(ImageView)findViewById(R.id.imageViewQRReadImg1);
        Uri imgUri=QRImgURI;
        QRimg.setImageURI(null);
        QRimg.setImageURI(imgUri);


        BarcodeDetector detector =
                new BarcodeDetector.Builder(getApplicationContext())
                        .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                        .build();
        if(!detector.isOperational()){
            txtView.setText("Could not set up the detector!");
            return;
        }

        Bitmap myQRbitmap=((BitmapDrawable)QRimg.getDrawable()).getBitmap();
        Frame frame = new Frame.Builder().setBitmap(myQRbitmap).build();
        SparseArray<Barcode> barcodes = detector.detect(frame);

        int totalCodes = barcodes.size();
        if (totalCodes > 0) {
            Barcode thisCode = barcodes.valueAt(0);
            String QRtext=thisCode.rawValue.toString();
            if(QRtext!=null){
                QRcodeText=QRtext;
                txtView.setText("QR Code Text: "+QRtext);
            }else{
                txtView.setText("Error: Image Doesn't contain any QR Code ");

            }

        }else{
            txtView.setText("Error: Image Doesn't contain any QR Code ");

        }


    }

    public void ReadQRexit(View v){
        final View info = (View) findViewById(R.id.ViewInfo);
        Animation animation1 =
                AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slidedown);
        info.startAnimation(animation1);
        animation1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                info.setVisibility(View.INVISIBLE);
                finish();
                Intent intent = new Intent(MainActivity.this,MainActivity.class);
                startActivity(intent);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });




    }

public void NULLL(View v){}

    public ArrayList<String> pullLinks(String text)
    {
        ArrayList<String> links = new ArrayList<String>();

        //String regex = "\\(?\\b(http://|www[.])[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]";
        String regex = "\\(?\\b(https?://|www[.]|ftp://)[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]";

        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
if(m.find()){
    String urlStr = m.group();

    if (urlStr.startsWith("(") && urlStr.endsWith(")"))
    {
        urlStr = urlStr.substring(1, urlStr.length() - 1);
    }


    links.add(urlStr);
    //     Toast.makeText(MainActivity.this, "URL Found:" +urlStr, Toast.LENGTH_SHORT).show();

    TextView URLFoundTx=(TextView)findViewById(R.id.textView11FoundURL);
    ImageView OpenURL=(ImageView)findViewById(R.id.imageView8OpenURL);
    QRCodeUrlText=urlStr;
    URLFoundTx.setVisibility(View.VISIBLE);
    OpenURL.setVisibility(View.VISIBLE);
}else {
    TextView URLFoundTx=(TextView)findViewById(R.id.textView11FoundURL);
    ImageView OpenURL=(ImageView)findViewById(R.id.imageView8OpenURL);
    URLFoundTx.setVisibility(View.INVISIBLE);
    OpenURL.setVisibility(View.INVISIBLE);
}
        /*
        while(m.find())
        {
            String urlStr = m.group();

            if (urlStr.startsWith("(") && urlStr.endsWith(")"))
            {
                urlStr = urlStr.substring(1, urlStr.length() - 1);
            }


            links.add(urlStr);
       //     Toast.makeText(MainActivity.this, "URL Found:" +urlStr, Toast.LENGTH_SHORT).show();

            TextView URLFoundTx=(TextView)findViewById(R.id.textView11FoundURL);
            ImageView OpenURL=(ImageView)findViewById(R.id.imageView8OpenURL);
            QRCodeUrlText=urlStr;
            URLFoundTx.setVisibility(View.VISIBLE);
            OpenURL.setVisibility(View.VISIBLE);


        }  */


        return links;
    }

    public void web(View v){
    if(QRCodeUrlText.startsWith("http://") || QRCodeUrlText.startsWith("https://")){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(QRCodeUrlText)); //Google play store
        startActivity(intent);
    }else {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("http://"+QRCodeUrlText)); //Google play store
        startActivity(intent);
    }



    }

}
