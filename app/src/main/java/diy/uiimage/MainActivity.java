package diy.uiimage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.ortiz.touch.TouchImageView;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 101;
    private static final int REQUEST_FILE = 102;
    private static final int REQUEST_CROP_IMAGE = 111;

    private TouchImageView imageResult = null;
    private Bitmap bitmapResult = null;
    private Uri tempUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button buttonCamera = (Button) findViewById(R.id.button_camera);
        Button buttonFile = (Button) findViewById(R.id.button_file);

        if (buttonCamera != null)
            buttonCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String external = Environment.getExternalStorageState();
                    if (external.equals(Environment.MEDIA_MOUNTED)) {
                        try {
                            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                            File temp = File.createTempFile("temp-", ".jpg", path);
                            tempUri = Uri.fromFile(temp);
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
                            startActivityForResult(intent, REQUEST_CAMERA);
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

        if (buttonFile != null)
            buttonFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, REQUEST_FILE);
                }
            });

        imageResult = (TouchImageView) findViewById(R.id.image_result);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelable("tempUri", tempUri);
        savedInstanceState.putParcelable("bitmap_result", bitmapResult);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        tempUri = savedInstanceState.getParcelable("tempUri");
        bitmapResult = savedInstanceState.getParcelable("bitmap_result");
        imageResult.setImageBitmap(bitmapResult);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        super.onActivityResult(requestCode, resultCode, resultIntent);
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (resultCode == RESULT_OK)
                    cropImage(tempUri, true);
                else {
                    File file = new File(tempUri.getPath());
                    file.delete();
                }
                break;

            case REQUEST_FILE:
                if (resultCode == RESULT_OK)
                    cropImage(resultIntent.getData(), false);
                break;

            case REQUEST_CROP_IMAGE:
                if (resultCode == RESULT_OK) {
                    bitmapResult = resultIntent.getParcelableExtra("image_result");
                    imageResult.setImageBitmap(bitmapResult);
                    imageResult.postInvalidate();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_about) {
            new AlertDialog.Builder(MainActivity.this)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle("About")
                    .setMessage("Cropman v1.0\nImage cropping")
                    .setNegativeButton("OK", null)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void cropImage(Uri uri, boolean deleteOnTemp) {
        Intent intent = new Intent(this, CropImageActivity.class);
        intent.putExtra("uri", uri);
        intent.putExtra("temp", deleteOnTemp);
        startActivityForResult(intent, REQUEST_CROP_IMAGE);
    }

}
