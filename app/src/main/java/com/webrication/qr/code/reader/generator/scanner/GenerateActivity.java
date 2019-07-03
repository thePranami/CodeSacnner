package com.webrication.qr.code.reader.generator.scanner;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;

public class GenerateActivity extends AppCompatActivity {

    Button generate, save2photo;
    ImageView Qrcode_image,back;
    EditText text;

    private static final int REQUEST_APP_SETTINGS = 168;
    private static final int REQUEST_WRITE_STORAGE = 112;

    private static final String[] requiredPermissions = new String[]{
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate);
        generate = (Button) findViewById(R.id.generate);
        save2photo = (Button) findViewById(R.id.save);
        Qrcode_image = (ImageView) findViewById(R.id.generate_image);
        text = (EditText) findViewById(R.id.generate_text);
        back = (ImageView) findViewById(R.id.back);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                GenerateActivity.super.onBackPressed();
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);

            }
        });


        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    String qrCodeData = text.getText().toString();
                    if (!qrCodeData.equals("")) {
                        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                        BitMatrix bitMatrix = multiFormatWriter.encode(qrCodeData, BarcodeFormat.QR_CODE, 200, 200);
                        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                        Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                        Qrcode_image.setImageBitmap(bitmap);
                        generate.setVisibility(View.GONE);
                        save2photo.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(GenerateActivity.this, "Enter Text", Toast.LENGTH_SHORT).show();
                    }


                } catch (WriterException e) {
                    e.printStackTrace();
                }


            }
        });


        save2photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT > 22 && !hasPermissions(requiredPermissions)) {
                    ActivityCompat.requestPermissions(GenerateActivity.this,
                            requiredPermissions, REQUEST_WRITE_STORAGE);
                }
                else
                {
                    save2gallery();
                }
            }
        });

    }

    public void save2gallery() {

        BitmapDrawable draw = (BitmapDrawable) Qrcode_image.getDrawable();
        Bitmap bitmap = draw.getBitmap();
        String savedImageURL = MediaStore.Images.Media.insertImage(getContentResolver(),
                bitmap, "Qrcode", "Image of Qrcode");

        Uri savedImageURI = Uri.parse(savedImageURL);
        Toast.makeText(GenerateActivity.this, "save image in :" + savedImageURI, Toast.LENGTH_SHORT).show();
        save2photo.setVisibility(View.GONE);
        generate.setVisibility(View.VISIBLE);
        text.setText("");
        Qrcode_image.setImageDrawable(null);

    }
    private void createDirectoryAndSaveFile(Bitmap imageToSave, String fileName)
    {

        File direct = new File(Environment.getExternalStorageDirectory() + "/QrCode");

        if (!direct.exists()) {
            File wallpaperDirectory = new File("/sdcard/QrCode/");
            wallpaperDirectory.mkdirs();
        }

        File file = new File(new File("/sdcard/QrCode/"), fileName);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            imageToSave.compress(Bitmap.CompressFormat.JPEG, 100, out);
            Log.e("bitmap", String.valueOf(imageToSave));
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean hasPermissions(@NonNull String... permissions) {
        for (String permission : permissions)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(permission))
                    return false;
            }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
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

        new AlertDialog.Builder(GenerateActivity.this)
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
