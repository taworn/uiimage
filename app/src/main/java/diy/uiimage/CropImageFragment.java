package diy.uiimage;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

/**
 * Crop image fragment for your convenient.
 * It provides 3 type: circle, round rectangle and plain rectangle.
 */
public class CropImageFragment extends Fragment {

    // Crop type constants
    public static final int CROP_TYPE_CIRCLE = 0;
    public static final int CROP_TYPE_ROUND_RECTANGLE = 1;
    public static final int CROP_TYPE_RECTANGLE = 2;

    private ImageView imageCrop = null;
    private ImageView imageMask = null;
    private Bitmap bitmapCrop = null;
    private Bitmap bitmapMask = null;

    private int cropType = 0;
    private DisplayMetrics metrics = null;

    public CropImageFragment() {
        // Required empty public constructor
    }

    public static CropImageFragment newInstance() {
        CropImageFragment fragment = new CropImageFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crop_image, container, false);
        imageCrop = (ImageView) view.findViewById(R.id.image_crop);
        imageMask = (ImageView) view.findViewById(R.id.image_mask);
        imageMask.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Since getWidth() and getHeight() returns 0 here, so, we have to postpone.
                if (bitmapMask == null)
                    drawMask();
            }
        });
        metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("bitmap"))
                bitmapCrop = savedInstanceState.getParcelable("bitmap");
            cropType = savedInstanceState.getInt("crop_type");
        }
        if (bitmapCrop != null)
            imageCrop.setImageBitmap(bitmapCrop);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (bitmapCrop != null)
            savedInstanceState.putParcelable("bitmap", bitmapCrop);
        savedInstanceState.putInt("crop_type", cropType);
    }

    /**
     * Saves the cropped bitmap.
     *
     * @param width  Bitmap width.
     * @param height Bitmap height.
     * @return Returns the bitmap.
     */
    public Bitmap save(int width, int height) {
        // creates crop bitmap
        Bitmap bitmap = drawCrop();

        // compresses output
        return Bitmap.createScaledBitmap(bitmap, width, height, false);
    }

    /**
     * Gets the bitmap to crop.
     */
    public Bitmap getBitmap() {
        return bitmapCrop;
    }

    /**
     * Sets the bitmap to crop.
     */
    public void setBitmap(Bitmap value) {
        bitmapCrop = value;
        if (bitmapCrop != null)
            imageCrop.setImageBitmap(bitmapCrop);
    }

    /**
     * Gets the crop type.
     */
    public int getCropType() {
        return cropType;
    }

    /**
     * Sets the crop type.
     */
    public void setCropType(int value) {
        cropType = value;
        drawMask();
    }

    /**
     * Draws output for return.
     */
    private Bitmap drawCrop() {
        // computes width and height
        int width = imageMask.getWidth();
        int height = imageMask.getHeight();

        // computes output bitmap
        Rect rectSrc = new Rect();
        int border = 16 * Math.round(metrics.density);
        if (cropType == CROP_TYPE_ROUND_RECTANGLE || cropType == CROP_TYPE_RECTANGLE) {
            // computes quadrangle with smaller side
            int left, top, right, bottom;
            if (width < height) {
                bottom = height / 2 + width / 2;
                top = bottom - width;
                left = 0;
                right = width;
            }
            else /*(height <= width)*/ {
                right = width / 2 + height / 2;
                left = right - height;
                top = 0;
                bottom = height;
            }

            // adds with border
            rectSrc.set(left + border, top + border, right - border, bottom - border);
        }
        else {
            // computes circle
            int r = Math.min(width, height) / 2;
            int x = width / 2 - r;
            int y = height / 2 - r;
            rectSrc.set(x + border, y + border, x + r + r - border, y + r + r - border);
        }

        // creates bitmap for result
        Bitmap bitmap = Bitmap.createBitmap(rectSrc.width(), rectSrc.height(), Bitmap.Config.ARGB_8888);

        // creates canvas
        Canvas canvas = new Canvas(bitmap);

        // creates paint
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawColor(0xFF000000, PorterDuff.Mode.XOR);

        // draws crop image from cache
        Rect rectDst = new Rect(0, 0, rectSrc.width(), rectSrc.height());
        imageCrop.setDrawingCacheEnabled(true);
        Bitmap bitmapCrop = imageCrop.getDrawingCache();
        canvas.drawBitmap(bitmapCrop, rectSrc, rectDst, paint);
        imageCrop.setDrawingCacheEnabled(false);

        // draws output as transparent
        if (cropType == CROP_TYPE_ROUND_RECTANGLE || cropType == CROP_TYPE_RECTANGLE) {
            // clip path for reverse rectangle
            RectF rectF = new RectF(rectDst);
            Path rectPath = new Path();
            if (cropType == CropImageFragment.CROP_TYPE_ROUND_RECTANGLE)
                rectPath.addRoundRect(rectF, border, border, Path.Direction.CW);
            else
                rectPath.addRect(rectF, Path.Direction.CW);
            canvas.clipRect(rectDst);
            canvas.clipPath(rectPath, Region.Op.XOR);
        }
        else {
            // clip path for reverse circle
            Path circlePath = new Path();
            circlePath.addCircle(rectDst.width() / 2, rectDst.height() / 2, Math.min(rectDst.width(), rectDst.height()) / 2, Path.Direction.CW);
            canvas.clipRect(rectDst);
            canvas.clipPath(circlePath, Region.Op.XOR);
        }
        canvas.drawColor(0xFF000000, PorterDuff.Mode.XOR);

        // returns the result
        return bitmap;
    }

    /**
     * Draws bitmap mask for used to show.
     */
    private void drawMask() {
        // computes width and height
        int width = imageMask.getWidth();
        int height = imageMask.getHeight();

        // creates bitmap for mask
        bitmapMask = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8);

        // creates canvas
        Canvas canvas = new Canvas(bitmapMask);

        // creates paint
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);

        // draws bitmap mask
        int border = 16 * Math.round(metrics.density);
        Rect rect = new Rect(0, 0, width, height);
        if (cropType == CROP_TYPE_ROUND_RECTANGLE || cropType == CROP_TYPE_RECTANGLE) {
            // computes quadrangle with smaller side
            int left, top, right, bottom;
            if (width < height) {
                bottom = height / 2 + width / 2;
                top = bottom - width;
                left = 0;
                right = width;
            }
            else /*(height <= width)*/ {
                right = width / 2 + height / 2;
                left = right - height;
                top = 0;
                bottom = height;
            }

            // adds with border
            RectF rectF = new RectF(left + border, top + border, right - border, bottom - border);
            Path rectPath = new Path();
            if (cropType == CropImageFragment.CROP_TYPE_ROUND_RECTANGLE)
                rectPath.addRoundRect(rectF, border, border, Path.Direction.CW);
            else
                rectPath.addRect(rectF, Path.Direction.CW);

            // clip path for reverse rectangle
            canvas.clipRect(rect);
            canvas.clipPath(rectPath, Region.Op.XOR);
        }
        else {
            // computes circle
            Path circlePath = new Path();
            circlePath.addCircle(width / 2, height / 2, Math.min(width, height) / 2 - border, Path.Direction.CW);

            // clip path for reverse circle
            canvas.clipRect(rect);
            canvas.clipPath(circlePath, Region.Op.XOR);
        }
        canvas.drawARGB(0x80, 0, 0, 0);

        // sets bitmap mask
        imageMask.setImageBitmap(bitmapMask);
    }

}
