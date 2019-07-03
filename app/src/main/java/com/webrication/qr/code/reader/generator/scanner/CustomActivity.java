package com.webrication.qr.code.reader.generator.scanner;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static android.graphics.Color.WHITE;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;

public class CustomActivity extends AppCompatActivity {

    EditText editText;
    ImageView Qr_image, logo_image, back;
    TextView sel_logo;
    Button generate,save2photo;
    private final int RESULT_CROP = 400;

    private static final int REQUEST_APP_SETTINGS = 168;
    private static final int REQUEST_WRITE_STORAGE = 112;

    private static final String[] requiredPermissions = new String[]{
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,

    };

    String picturePath="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom);
        generate=(Button)findViewById(R.id.generate);
        save2photo=(Button)findViewById(R.id.save);
        editText=(EditText)findViewById(R.id.generate_text);
        sel_logo=(TextView)findViewById(R.id.slect_logo);
        Qr_image=(ImageView)findViewById(R.id.generate_image);
        logo_image=(ImageView)findViewById(R.id.image_logo);
        back=(ImageView)findViewById(R.id.back);

        if (Build.VERSION.SDK_INT > 22 && !hasPermissions(requiredPermissions)) {
            ActivityCompat.requestPermissions(CustomActivity.this,
                    requiredPermissions, REQUEST_WRITE_STORAGE);
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CustomActivity.super.onBackPressed();
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
            }
        });

        sel_logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT > 22 && !hasPermissions(requiredPermissions)) {
                    ActivityCompat.requestPermissions(CustomActivity.this,
                            requiredPermissions, REQUEST_WRITE_STORAGE);
                }
                else {
                    opengallery();
                }
            }
        });
        logo_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT > 22 && !hasPermissions(requiredPermissions)) {
                    ActivityCompat.requestPermissions(CustomActivity.this,
                            requiredPermissions, REQUEST_WRITE_STORAGE);
                }
                else {
                    opengallery();
                }
            }
        });

        save2photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                save2gallery();
            }
        });

        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String qrCodeData = editText.getText().toString();

                if (!qrCodeData.equals("")&& logo_image.getDrawable()!=null)
                {
                    try {

                        int width =300;
                        int height = 300;
                        int smallestDimension = width < height ? width : height;

                        String charset = "UTF-8";
                        Map<EncodeHintType, ErrorCorrectionLevel> hintMap =new HashMap<EncodeHintType, ErrorCorrectionLevel>();
                        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
                        CreateQRCode(qrCodeData, charset, hintMap, smallestDimension, smallestDimension);

                        generate.setVisibility(View.GONE);
                        save2photo.setVisibility(View.VISIBLE);

                    } catch (Exception ex) {
                        Log.e("QrGenerate",ex.getMessage());
                    }
                }
                else
                {
                    Toast.makeText(CustomActivity.this, "Enter Text & Logo", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public  void CreateQRCode(String qrCodeData, String charset, Map hintMap, int qrCodeheight, int qrCodewidth){

        try {

            BitMatrix matrix = new MultiFormatWriter().encode(new String(qrCodeData.getBytes(charset), charset),
                    BarcodeFormat.QR_CODE, qrCodewidth, qrCodeheight, hintMap);

            int width = matrix.getWidth();
            int height = matrix.getHeight();
            int[] pixels = new int[width * height];

            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {

                    pixels[offset + x] = matrix.get(x, y) ?
                            ResourcesCompat.getColor(getResources(),android.R.color.black,null) :WHITE;
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

            BitmapDrawable draw = (BitmapDrawable)logo_image.getDrawable();
            Bitmap overlay = draw.getBitmap();

            Qr_image.setImageBitmap(mergeBitmaps(getResizedBitmap(overlay,70),bitmap));

        }catch (Exception er){
            Log.e("QrGenerate",er.getMessage());
        }
    }

    public Bitmap mergeBitmaps(Bitmap overlay, Bitmap bitmap) {

        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int height_o=overlay.getHeight();
        int width_o=overlay.getWidth();

        Bitmap combined = Bitmap.createBitmap(width, height, bitmap.getConfig());
        Canvas canvas = new Canvas(combined);
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        canvas.drawBitmap(bitmap, new Matrix(), null);

        int centreX = (canvasWidth  - overlay.getWidth())/2;
        int centreY = (canvasHeight - overlay.getHeight())/2;
        canvas.drawBitmap(overlay, centreX, centreY, null);

        return combined;
    }

    public void opengallery(){
        Intent galleryintent=new Intent();
        galleryintent.setAction(Intent.ACTION_PICK);
        galleryintent.setType("image/*");
        startActivityForResult(Intent.createChooser(galleryintent,"Select One App"),120);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
          if (requestCode==120 && resultCode == RESULT_OK) {
              try {
                  Uri imageUri = data.getData();
                  String[] filePathColumn = {MediaStore.Images.Media.DATA};
                  Cursor cursor = getContentResolver().query(imageUri,
                          filePathColumn, null, null, null);
                  cursor.moveToFirst();
                  int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                  picturePath = cursor.getString(columnIndex);
//   uncomment below line
                  logo_image.setImageURI(Uri.parse(picturePath));
                  performCrop(picturePath);
                  cursor.close();
              } catch (Exception e) {
                  e.printStackTrace();
              }
          }

          if (requestCode == RESULT_CROP) {
              if (resultCode == RESULT_OK) {
                  Bundle extras = data.getExtras();

                  if (extras != null) {
                      Bitmap selectedBitmap = extras.getParcelable("data");
                      logo_image.setImageBitmap(selectedBitmap);
                      logo_image.setScaleType(ImageView.ScaleType.FIT_XY);
                  } else {
                      logo_image.setImageURI(Uri.parse(picturePath));
                      logo_image.setScaleType(ImageView.ScaleType.FIT_XY);
                  }
              }
          }
    }

    private void performCrop(String picUri) {
        try {

            final int width  = 280;
            final int height = 280;

            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            File f = new File(picUri);
            Uri contentUri;
            if (Build.VERSION.SDK_INT >M)
            {
                contentUri = FileProvider.getUriForFile(CustomActivity.this,
                        BuildConfig.APPLICATION_ID + ".provider", f);

                cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            else
            {
                contentUri = Uri.fromFile(f);
            }

            cropIntent.setDataAndType(contentUri, "image/*");
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            cropIntent.putExtra("outputX", width);
            cropIntent.putExtra("outputY", height);
            cropIntent.putExtra("return-data", true);
            startActivityForResult(cropIntent,RESULT_CROP);
        }

        catch (ActivityNotFoundException e) {
            String errorMessage = "your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();

            logo_image.setImageURI(Uri.parse(picturePath));
            logo_image.setScaleType(ImageView.ScaleType.FIT_XY);
        }
    }

    public void save2gallery() {

        BitmapDrawable draw = (BitmapDrawable)Qr_image.getDrawable();
        Bitmap bitmap = draw.getBitmap();
        String savedImageURL = MediaStore.Images.Media.insertImage(
                getContentResolver(),
                bitmap,
                "Qrcode",
                "Image of Qrcode"
        );

        Uri savedImageURI = Uri.parse(savedImageURL);
        Toast.makeText(CustomActivity.this, "save image in :" + savedImageURI, Toast.LENGTH_SHORT).show();

        save2photo.setVisibility(View.GONE);
        generate.setVisibility(View.VISIBLE);
        editText.setText("");
        logo_image.setImageResource(0);
        Qr_image.setImageResource(0);
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public boolean hasPermissions(@NonNull String... permissions) {
        for (String permission : permissions)
            if (Build.VERSION.SDK_INT >= M) {
                if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(permission))
                    return false;
            }
        return true;
    }

    @TargetApi(M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode ==REQUEST_WRITE_STORAGE) {
            for (int i = 0, len = permissions.length; i < len; i++) {
                String permission = permissions[i];
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    boolean showRationale = shouldShowRequestPermissionRationale(permission);
                    if (!showRationale) {
                        if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permission)) {
                            openDialogForSetting(R.string.permission_Storage, R.string.permission_Storage_msg);
                        }
                    }
                }
            }
        }
    }

    private void openDialogForSetting(int title, int msg) {

        new AlertDialog.Builder(CustomActivity.this)
                .setTitle(title)
                .setMessage(msg)
                .setCancelable(true)
                .setPositiveButton("Setting",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //do something...
                                goToSettings();
                            }
                        }
                )
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }
                ).show();

    }

    private void goToSettings() {
        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(myAppSettings, REQUEST_APP_SETTINGS);
    }
}
