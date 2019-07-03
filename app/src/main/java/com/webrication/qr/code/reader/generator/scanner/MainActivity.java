package com.webrication.qr.code.reader.generator.scanner;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;



import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.os.Build.VERSION_CODES.M;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class MainActivity extends AppCompatActivity {
    Dialog dialog;
    Button camera_dialog, album_dialog;
    ImageView scan, generate, custom, identify;

    private Uri imgUri;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 3;
    File file;
    private static final int REQUEST_APP_SETTINGS = 168;
    private static final int REQUEST_WRITE_STORAGE = 112;
    private static final int REQUEST_CAMERA = 120;

    private static final String[] requiredPermissions = new String[]{
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA,

    };
    private static final String[] requiredPermissionscamera = new String[]{
            android.Manifest.permission.CAMERA,

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         scan=(ImageView) findViewById(R.id.Scan_QR);
        generate=(ImageView) findViewById(R.id.generate_qr);
        custom=(ImageView) findViewById(R.id.custom_code);
        identify=(ImageView) findViewById(R.id.identify_code);
        dialog=new Dialog(this);
        dialog.setTitle("Please select a picture");
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_dialog);
        camera_dialog=(Button)dialog.findViewById(R.id.camera);
        album_dialog=(Button)dialog.findViewById(R.id.album);

        ButtonClick();

        if (Build.VERSION.SDK_INT > 22 && !hasPermissions(requiredPermissions)) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    requiredPermissions, REQUEST_WRITE_STORAGE);
        }
    }

    public void ButtonClick(){
        camera_dialog.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
               // imgUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                //imgUri = ImageServices.getOutputImageFileUri(cont);
                //getContentResolver().notifyChange(imgUri, null);
                if (intent.resolveActivity(getPackageManager()) != null) {
//            fileTemp = ImageUtils.getOutputMediaFile();
                    ContentValues values = new ContentValues(1);
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
                    imgUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//            if (fileTemp != null) {
//            fileUri = Uri.fromFile(fileTemp);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                    //intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
                    startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
                    dialog.dismiss();
                }
            }
        });
        album_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryintent=new Intent();
                galleryintent.setAction(Intent.ACTION_PICK);
                galleryintent.setType("image/*");
                startActivityForResult(Intent.createChooser(galleryintent,"Select One App"),120);
                dialog.dismiss();
            }
        });

        identify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT > 22 && !hasPermissions(requiredPermissions)) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            requiredPermissions, REQUEST_WRITE_STORAGE);
                }
                else
                {
                    dialog.show();
                }
           }
        });

        custom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    Intent intent=new Intent(MainActivity.this,CustomActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            }
        });

        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(MainActivity.this,GenerateActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            }
        });

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT > 22 && !hasPermissions(requiredPermissionscamera))
                {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            requiredPermissionscamera, REQUEST_CAMERA);
                }
                else
                {
                    Intent intent=new Intent(MainActivity.this,ScanActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                }
            }
        });
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
        if (requestCode ==REQUEST_WRITE_STORAGE)
        {
            for (int i = 0, len = permissions.length; i < len; i++) {
                String permission = permissions[i];
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    boolean showRationale = shouldShowRequestPermissionRationale(permission);
                    if (!showRationale) {

                        if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permission)) {
                            openDialogForSetting(R.string.permission_Storage, R.string.permission_Storage_msg);
                        } else if (android.Manifest.permission.CAMERA.equals(permission)) {
                            openDialogForSetting(R.string.permission_camera, R.string.permission_camera_msg);
                        }
                    }
                }
            }
        }

        if (requestCode==REQUEST_CAMERA)
        {
            for (int i = 0, len = permissions.length; i < len; i++) {
                String permission = permissions[i];
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    boolean showRationale = shouldShowRequestPermissionRationale(permission);
                    if (!showRationale) {

                        if (android.Manifest.permission.CAMERA.equals(permission)) {
                            openDialogForSetting(R.string.permission_camera, R.string.permission_camera_msg);
                        }
                    }
                }
            }
        }
    }

    private void openDialogForSetting(int title, int msg) {

        new AlertDialog.Builder(MainActivity.this)
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("file_uri", imgUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        imgUri = savedInstanceState.getParcelable("file_uri");
    }

    public Uri getOutputMediaFileUri(int type) {

        if (Build.VERSION.SDK_INT > M)
        {
            return FileProvider.getUriForFile(MainActivity.this,
                    BuildConfig.APPLICATION_ID + ".provider", getOutputMediaFile(type));
        }
        else
        {
            return Uri.fromFile(getOutputMediaFile(type));
        }
    }

    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                " Hello camera");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(" Hello camera", "Oops! Failed create "
                        + " Hello camera" + " directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                Intent intent=new Intent(this,IdentifyActivity.class);
                intent.putExtra("imageUri", imgUri.toString());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);

            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        }
        if (requestCode==120&&resultCode==RESULT_OK)
        {
            Uri contentUri = data.getData();
            Intent intent=new Intent(this,IdentifyActivity.class);
            intent.putExtra("imageUri", contentUri.toString());
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);

            //.setData(contentUri));

        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "Camera");
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
       // mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }
}
