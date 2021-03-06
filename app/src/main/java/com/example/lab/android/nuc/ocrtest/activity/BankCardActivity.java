 package com.example.lab.android.nuc.ocrtest.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.BankCardParams;
import com.baidu.ocr.sdk.model.BankCardResult;
import com.baidu.ocr.ui.camera.CameraActivity;
import com.example.lab.android.nuc.ocrtest.util.FileUtil;
import com.example.lab.android.nuc.ocrtest.R;

import java.io.File;

 public class BankCardActivity extends AppCompatActivity {

     private static final int REQUEST_CODE_PICK_IMAGE = 101;
     private static final int REQUEST_CODE_CAMERA = 102;
     private TextView infoTextView;

     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate( savedInstanceState );
         setContentView( R.layout.activity_accurate );
         //jsjsjsjsjk的骄傲理解的
         infoTextView = (TextView) findViewById(R.id.info_text_view);
         infoTextView.setTextIsSelectable(true);
         findViewById(R.id.gallery_button).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                     int ret = ActivityCompat.checkSelfPermission(BankCardActivity.this, Manifest.permission
                             .READ_EXTERNAL_STORAGE);
                     if (ret != PackageManager.PERMISSION_GRANTED) {
                         ActivityCompat.requestPermissions(BankCardActivity.this,
                                 new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                                 1000);
                         return;
                     }
                 }
                 Intent intent = new Intent(Intent.ACTION_PICK);
                 intent.setType("image/*");
                 startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
             }
         });

         findViewById(R.id.camera_button).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {

                 Intent intent = new Intent(BankCardActivity.this, CameraActivity.class);
                 intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                         FileUtil.getSaveFile(getApplication()).getAbsolutePath());
                 intent.putExtra(CameraActivity.KEY_CONTENT_TYPE,
                         CameraActivity.CONTENT_TYPE_BANK_CARD);
                 startActivityForResult(intent, REQUEST_CODE_CAMERA);
             }
         });


         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
             int ret = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
             if (ret == PackageManager.PERMISSION_DENIED) {
                 ActivityCompat.requestPermissions(BankCardActivity.this,
                         new String[] {Manifest.permission.READ_PHONE_STATE},
                         100);
             }
         }
         //添加返回按钮
         ActionBar actionBar = getSupportActionBar();
         if (actionBar != null) {
             // Show the Up button in the action bar.
             actionBar.setDisplayHomeAsUpEnabled(true);
             actionBar.setTitle( "银行卡识别" );
         }
     }

     private void recGeneral(String filePath) {
         BankCardParams param = new BankCardParams();
         param.setImageFile(new File(filePath));
         OCR.getInstance(this).recognizeBankCard(param, new OnResultListener<BankCardResult>() {

             @Override
             public void onResult(BankCardResult bankCardResult) {
                 StringBuilder sb = new StringBuilder();
                 sb.append( bankCardResult.getBankName() + "\n");
                 sb.append("银行卡类型 " +  bankCardResult.getBankCardType() + "\n");
                 sb.append( "银行卡卡号 " + bankCardResult.getBankCardNumber() + "\n" );
                 infoTextView.setText(sb.toString());
             }

             @Override
             public void onError(OCRError error) {
                 infoTextView.setText(error.getMessage());
             }
         });
     }

     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
             Uri uri = data.getData();
             String filePath = getRealPathFromURI(uri);
             recGeneral(filePath);
         }

         if (requestCode == REQUEST_CODE_CAMERA && resultCode == Activity.RESULT_OK) {
             recGeneral(FileUtil.getSaveFile(getApplicationContext()).getAbsolutePath());
         }
     }

     private String getRealPathFromURI(Uri contentURI) {
         String result;
         Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
         if (cursor == null) { // Source is Dropbox or other similar local file path
             result = contentURI.getPath();
         } else {
             cursor.moveToFirst();
             int idx = cursor.getColumnIndex( MediaStore.Images.ImageColumns.DATA);
             result = cursor.getString(idx);
             cursor.close();
         }
         return result;
     }

     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()){
             case android.R.id.home:
                 this.finish();
                 return true;
         }
         return super.onOptionsItemSelected( item );
     }
}
