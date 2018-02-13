package devesh.ephrine.qr.code.pro;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScanQRActivity extends AppCompatActivity {

    private static final String TAG = "QR Lite";
    public String QRcodeText;
    public String QRcodeURLText;

     public Uri QRImgURI;

    public Intent intent;
    public Uri data;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr);
        // Get the intent that started this activity

        getSupportActionBar().setTitle("Scan Image for QR Code");  // provide compatibility to all the versions
getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        intent = getIntent();


      //  data = intent.getData();
data =(Uri) getIntent().getExtras().get(Intent.EXTRA_STREAM);
        if(data!=null){
            Log.v(TAG, "Img URI not none ");
        }else {
            Log.v(TAG, "Img URI none ");
        }


        if (intent.getType().indexOf("image/") != -1) {
          //  String B=data.toString();
            Log.v(TAG, "Img URI found: ");
            String A=intent.toString();
            Log.v(TAG, "Img URI found intent: "+A);

            QRImgURI=data;
ScanIMG();
        } else if (intent.getType().equals("text/plain")) {

        }
    }

    public void ScanIMG(){
        ImageView QRimg=(ImageView)findViewById(R.id.imageViewQRscan);
        Uri imgUri=QRImgURI;
        QRimg.setImageURI(null);
        QRimg.setImageURI(imgUri);
        TextView txtView=(TextView)findViewById(R.id.textView13);

        TextView txScan=(TextView)findViewById(R.id.textView11);

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
                txtView.setText(QRtext);
                pullLinks(QRcodeText);

                txScan.setText("QR Code: ");
            }else{
                txScan.setText("Error: Image Doesn't contain any QR Code ");
                Log.v(TAG, "QR Error 1 !!!!");


            }

        }else{
            Log.v(TAG, "QR Error 2 !!!!");
            txScan.setText("Error: Image Doesn't contain any QR Code ");

        }

    }

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

Button Browser=(Button)findViewById(R.id.button9);
Browser.setVisibility(View.VISIBLE);
            QRcodeURLText=urlStr;

        }else {

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
        if(QRcodeURLText.startsWith("http://") || QRcodeURLText.startsWith("https://")){
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(QRcodeURLText)); //Google play store
            startActivity(intent);
        }else {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("http://"+QRcodeURLText)); //Google play store
            startActivity(intent);
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
            Toast.makeText(ScanQRActivity.this, "Text Copied to Clipboard", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(ScanQRActivity.this, "Scarn QR code Before Copying it to clipboard", Toast.LENGTH_SHORT).show();

        }
    }

}
