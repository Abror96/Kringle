package com.example.kringle.kringle;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.io.ByteArrayOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;

public class LoadQrCode extends AppCompatActivity implements ZXingScannerView.ResultHandler, View.OnClickListener {

    private static final int REQUEST_CAMERA = 1;
    public static final int REQUEST_CODE_FOR_INTENT = 5432;

    @BindView(R.id.scanner_view)
    ZXingScannerView scannerView;

    @BindView(R.id.load_qr_mainView)
    ConstraintLayout mainLayout;

    @BindView(R.id.tv_upload_qr_code)
    TextView upload_qr_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_qr_code);
        ButterKnife.bind(this);

        //setting the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_load);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setTitle("Load QR-Code");

        // checking permission for camera
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkPermission()) requestPermission();
        }

        upload_qr_code.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_upload_qr_code:
                pickImageFromGallery();
                break;
        }
    }

    /* --------- Methods for checking permissions --------- */

    private boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(this, CAMERA)
                == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (!cameraAccepted) Snackbar.make(mainLayout, "Permission is denied", Snackbar.LENGTH_LONG).show();
                }
        }
    }
    /* ------------------------------------------------------ */


    /* --------- Methods for scanning qr-code from camera --------- */
    @Override
    public void handleResult(Result result) {
        final String scanResult = result.getText();
        backToPrevActivity(scanResult);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkPermission()) {
            if (scannerView != null) {
                scannerView.setResultHandler(this);
                scannerView.startCamera();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scannerView.stopCamera();
    }

    /* -------------------------------------------------------------- */


    /* --------- Methods for scanning qr-code from gallery --------- */

    private void pickImageFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_CAMERA);
    }

    @Override
    protected final void onActivityResult(final int requestCode,
                                          final int resultCode, final Intent i) {
        super.onActivityResult(requestCode, resultCode, i);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CAMERA:
                    manageImageFromUri(i.getData());
                    break;
            }
        }

    }

    public void manageImageFromUri(Uri imageUri) {
        Bitmap bitmap = null;

        try {
            bitmap = MediaStore.Images.Media.getBitmap(
                    this.getContentResolver(), imageUri);
        } catch (Exception e) {
            Snackbar.make(mainLayout, e.getMessage(), Snackbar.LENGTH_LONG).show();
        }

        if (bitmap != null) {

            detectBarCode(bitmap);

        }
    }

    void detectBarCode(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        int[] intArray = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(intArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        LuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), intArray);
        Reader reader = new QRCodeReader();
        try {
            Result result = reader.decode(new BinaryBitmap(new HybridBinarizer(source)));
            backToPrevActivity(result.getText());
        } catch (NotFoundException | ChecksumException | FormatException e) {
            Snackbar.make(mainLayout, "" +
                    "Unable to upload Qr-Code, please try again with screened/downloaded version", Snackbar.LENGTH_LONG).show();
        }
    }

    /* ------------------------------------------------------------- */

    /* Back to the main activity with response */

    private void backToPrevActivity(String response) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("response", response);
        setResult(REQUEST_CODE_FOR_INTENT, intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
