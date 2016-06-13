package diy.uiimage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class CropImageActivity extends AppCompatActivity {

    private static final String TAG = "CropImageActivity";

    private CropImageFragment fragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.show();
        }

        fragment = (CropImageFragment) getFragmentManager().findFragmentById(R.id.fragment_crop_image);
        if (fragment.getBitmap() == null) {
            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();
            Bitmap bitmap = null;
            Uri uri = (Uri) bundle.get("uri");
            try {
                InputStream stream = getContentResolver().openInputStream(uri);
                bitmap = BitmapFactory.decodeStream(stream);
                if (bundle.containsKey("temp") && bundle.getBoolean("temp")) {
                    File file = new File(uri.getPath());
                    file.delete();
                }
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            fragment.setBitmap(bitmap);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_crop_image, menu);

        // inits radio menu items
        MenuItem item;
        switch (fragment.getCropType()) {
            default:
            case CropImageFragment.CROP_TYPE_CIRCLE:
                item = menu.findItem(R.id.action_crop_type_circle);
                break;

            case CropImageFragment.CROP_TYPE_ROUND_RECTANGLE:
                item = menu.findItem(R.id.action_crop_type_round_rectangle);
                break;

            case CropImageFragment.CROP_TYPE_RECTANGLE:
                item = menu.findItem(R.id.action_crop_type_rectangle);
                break;
        }
        item.setChecked(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.action_crop:
                Bitmap bitmap = fragment.save(256, 256);
                Intent resultIntent = new Intent();
                resultIntent.putExtra("image_result", bitmap);
                setResult(RESULT_OK, resultIntent);
                finish();
                return true;

            case R.id.action_crop_type_circle:
                item.setChecked(true);
                fragment.setCropType(CropImageFragment.CROP_TYPE_CIRCLE);
                return true;

            case R.id.action_crop_type_round_rectangle:
                item.setChecked(true);
                fragment.setCropType(CropImageFragment.CROP_TYPE_ROUND_RECTANGLE);
                return true;

            case R.id.action_crop_type_rectangle:
                item.setChecked(true);
                fragment.setCropType(CropImageFragment.CROP_TYPE_RECTANGLE);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
