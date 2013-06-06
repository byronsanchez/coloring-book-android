/*
 * Copyright (c) 2013 Byron Sanchez (hackbytes.com)
 * www.chompix.com
 *
 * This file is part of "Coloring Book for Android."
 *
 * "Coloring Book for Android" is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, version 2 of the
 * license.
 *
 * "Coloring Book for Android" is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with "Coloring Book for Android."  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package net.globide.coloring_book_08;

import java.util.HashMap;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ToggleButton;

/**
 * Displays the currently selected image for coloring. Also displays color
 * palettes, and coloring tools such as bucket, eraser, next image and previous
 * image. Loops around if the bound of the currently selected category is
 * reached.
 */

public class ColorActivity extends Activity implements OnClickListener {

    // TODO: Move device info into it's own bootstrap static class

    // Define whether or not this activity continues music from the
    // MusicManager.
    private boolean mContinueMusic = true;

    // Define our small db API object for database interaction.
    private NodeDatabase mDbNodeHelper = null;
    // Define a long number that we will receive from Main Activity when a user
    // selects a coloring book.
    private long mCid;
    // Define a node object that will store the node data from the database.
    private Node[] mNodeData;

    // The saved state data
    private boolean isSavedState = false;
    private ColorGfxData savedData = null;

    // Define image properties.
    private int sCurrentImageId = 0;
    private int mMinImageId;
    private int mMaxImageId;
    public boolean isDirectionRight = true;

    // Define views.
    private FrameLayout mFlColorBody;
    private LinearLayout mLlColorPaletteLeft;
    private LinearLayout mLlColorPaletteLeft2;
    private LinearLayout mLlColorPaletteRight;
    private LinearLayout mLlColorPaletteRight2;
    private ImageButton mIbLeft;
    private ImageButton mIbRight;
    private ToggleButton mTbFillMode;
    private ToggleButton mTbEraseMode;
    public ProgressBar pbFloodFill;

    // The render engine.
    public ColorGFX colorGFX;

    // Define a container for the palettes
    public HashMap<String, ColorPalette> hmPalette = new HashMap<String, ColorPalette>();

    // Tablet vs. phone boolean. Defaults to phone.
    public static boolean sIsTablet = false;
    public static boolean sIsSmall = false;
    public static boolean sIsNormal = false;
    public static boolean sIsLarge = false;
    public static boolean sIsExtraLarge = false;

    // TODO: Check for memory leak in bitmap allocation when changing views.
    // This might require a thorough study of the activity lifestyle and how
    // bitmaps are allocated in an activity.

    // TODO: Make the surfaceview a fragment and use retainstate instead of the
    // nonconfiginstance.

    /**
     * Implements onCreate().
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the activity to full screen mode.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Add the default content.
        setContentView(R.layout.activity_color);

        // Determine whether or not the current device is a tablet.
        ColorActivity.sIsTablet = getResources().getBoolean(R.bool.isTablet);
        ColorActivity.sIsSmall = getResources().getBoolean(R.bool.isSmall);
        ColorActivity.sIsNormal = getResources().getBoolean(R.bool.isNormal);
        ColorActivity.sIsLarge = getResources().getBoolean(R.bool.isLarge);
        ColorActivity.sIsExtraLarge = getResources().getBoolean(R.bool.isExtraLarge);

        final Object colorGfxData = getLastNonConfigurationInstance();

        if (colorGfxData != null) {

            isSavedState = true;
            savedData = ((ColorGfxData) colorGfxData);

            // Restore the previous data.
            sCurrentImageId = savedData.currentImageId;
        }
        else {
            isSavedState = false;
        }

        // Create our database object so we can communicate with the db.
        mDbNodeHelper = new NodeDatabase(this);

        // Call the createDatabase() function...just in case for some weird
        // reason the database does not yet exist. Otherwise, it will load
        // our
        // database for use.
        mDbNodeHelper.createDatabase();

        // Setup a bundle and store any data passed from the activity
        // (BrowseActivity) that invoked this NodeActivity. The data passed
        // should be the id of the story the user wants to read.
        Bundle extras = getIntent().getExtras();
        mCid = (long) extras.getInt("id");

        // Query the database for a node containing the id which was passed
        // from BrowseActivity.
        mNodeData = mDbNodeHelper.getNodeListData(mCid);
        mMinImageId = 0;
        mMaxImageId = mNodeData.length - 1;

        // Close the database.
        mDbNodeHelper.close();

        // Attach views to their corresponding resource ids.
        mIbLeft = (ImageButton) findViewById(R.id.ibLeft);
        mLlColorPaletteLeft = (LinearLayout) findViewById(R.id.llColorPaletteLeft);
        mLlColorPaletteLeft2 = (LinearLayout) findViewById(R.id.llColorPaletteLeft2);
        mTbFillMode = (ToggleButton) findViewById(R.id.tbFillMode);
        mIbRight = (ImageButton) findViewById(R.id.ibRight);
        mTbEraseMode = (ToggleButton) findViewById(R.id.tbEraseMode);
        mLlColorPaletteRight = (LinearLayout) findViewById(R.id.llColorPaletteRight);
        mLlColorPaletteRight2 = (LinearLayout) findViewById(R.id.llColorPaletteRight2);
        mFlColorBody = (FrameLayout) findViewById(R.id.flColorBody);

        // Create the progressbar view
        pbFloodFill = new ProgressBar(this);
        // Give the progressbar some parameters.
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.RIGHT;

        if (ColorActivity.sIsTablet) {
            if (ColorActivity.sIsSmall) {
                params.setMargins(16, 16, 16, 16);
            }
            else if (ColorActivity.sIsNormal) {
                params.setMargins(20, 20, 20, 20);
            }
            else if (ColorActivity.sIsLarge) {
                params.setMargins(24, 24, 24, 24);
            }
            else if (ColorActivity.sIsExtraLarge) {
                params.setMargins(28, 28, 28, 28);
            }
        } else {
            params.setMargins(10, 10, 10, 10);
        }

        pbFloodFill.setLayoutParams(params);
        pbFloodFill.setIndeterminate(true);
        pbFloodFill.setVisibility(View.GONE);

        // Request focus to the FrameLayout containing the paint view.
        mFlColorBody.requestFocus();

        // Load the canvas.
        loadColorCanvas();

        // Load the color palettes.
        loadColorPalettes();

        // Create the palette buttons.
        loadPaletteButtons();

        // Load brushes
        loadBrushes();

        // Set any event listeners.
        mIbLeft.setOnClickListener(this);
        mIbRight.setOnClickListener(this);
        mTbFillMode.setOnClickListener(this);
        mTbEraseMode.setOnClickListener(this);

        if (colorGfxData != null) {

            if (savedData != null) {
                // Restore the previous data.
                colorGFX.selectedColor = savedData.selectedColor;
                colorGFX.isFillModeEnabled = savedData.isFillModeEnabled;
                colorGFX.isEraseModeEnabled = savedData.isEraseModeEnabled;
                colorGFX.paint = savedData.paint;
                // Set the disabled image resources for the brush and fill
                // buttons.
                if (savedData.isEraseModeEnabled) {
                    mTbFillMode.setBackgroundResource(R.drawable.bucket_button_disabled);
                }
                else {
                    mTbFillMode.setBackgroundResource(R.drawable.bucket_button);
                }
            }
        }
    }

    /*
     * Implements onRetainNonConfigurationInstance(). Saves current Gfx data for
     * resumes.
     */
    @Override
    public Object onRetainNonConfigurationInstance() {

        ColorGfxData colorGfxData = new ColorGfxData();

        colorGfxData.selectedColor = colorGFX.selectedColor;
        colorGfxData.isFillModeEnabled = colorGFX.isFillModeEnabled;
        colorGfxData.isEraseModeEnabled = colorGFX.isEraseModeEnabled;
        colorGfxData.bitmap = colorGFX.bitmap;
        colorGfxData.paint = colorGFX.paint;
        colorGfxData.currentImageId = sCurrentImageId;

        return colorGfxData;
    }

    private MaskFilter mBlur;

    /**
     * Loads the brush and it's stylings.
     */
    public void loadBrushes() {
        colorGFX.paint = new Paint();
        colorGFX.paint.setAntiAlias(true);
        colorGFX.paint.setDither(true);
        colorGFX.paint.setColor(colorGFX.selectedColor);
        colorGFX.paint.setStyle(Paint.Style.STROKE);
        colorGFX.paint.setStrokeJoin(Paint.Join.ROUND);
        colorGFX.paint.setStrokeCap(Paint.Cap.ROUND);
        colorGFX.paint.setStrokeWidth(12);
        mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);
        colorGFX.paint.setMaskFilter(mBlur);
    }

    /**
     * Sets up the color palettes.
     */
    private void loadColorPalettes() {

        /*
         * Pallete 1
         */

        // Create a tag and a HashMap of colors to assign to Palette1
        String tag = "Palette1";

        HashMap<String, Integer> colors = new HashMap<String, Integer>();
        colors.put("1_lightRed", Color.rgb(255, 106, 106));
        colors.put("2_red", Color.rgb(220, 20, 60));
        colors.put("3_orange", Color.rgb(255, 140, 0));
        colors.put("4_yellow", Color.rgb(255, 255, 0));
        colors.put("5_gold", Color.rgb(255, 185, 15));

        // Create a new palette based on this information.
        ColorPalette Palette1;

        if (isSavedState) {
            Palette1 = new ColorPalette(this, colors, isSavedState,
                    savedData.selectedColor);
        } else {
            Palette1 = new ColorPalette(this, colors);
        }

        // Add the palette to the HashMap.
        hmPalette.put(tag, Palette1);

        /*
         * Palette 2
         */

        // Create a tag and a HashMap of colors to assign to Palette1
        tag = "Palette2";

        HashMap<String, Integer> colors2 = new HashMap<String, Integer>();
        colors2.put("1_green", Color.rgb(0, 205, 0));
        colors2.put("2_darkGreen", Color.rgb(0, 128, 0));
        colors2.put("3_lightBlue", Color.rgb(99, 184, 255));
        colors2.put("4_blue", Color.rgb(0, 0, 255));
        colors2.put("5_darkBlue", Color.rgb(39, 64, 139));

        // Create a new palette based on this information.
        ColorPalette Palette2;

        if (isSavedState) {
            Palette2 = new ColorPalette(this, colors2, isSavedState,
                    savedData.selectedColor);
        } else {
            Palette2 = new ColorPalette(this, colors2);
        }

        // Add the palette to the HashMap.
        hmPalette.put(tag, Palette2);

        /*
         * Palette 3
         */

        // Create a tag and a HashMap of colors to assign to Palette1
        tag = "Palette3";

        HashMap<String, Integer> colors3 = new HashMap<String, Integer>();
        colors3.put("1_indigo", Color.rgb(75, 0, 130));
        colors3.put("2_violet", Color.rgb(148, 0, 211));
        colors3.put("3_pink", Color.rgb(255, 105, 180));
        colors3.put("4_peach", Color.rgb(255, 215, 164));
        colors3.put("5_lightBrown", Color.rgb(205, 133, 63));

        // Create a new palette based on this information.
        ColorPalette Palette3;

        if (isSavedState) {
            Palette3 = new ColorPalette(this, colors3, isSavedState,
                    savedData.selectedColor);
        } else {
            Palette3 = new ColorPalette(this, colors3);
        }

        // Add the palette to the HashMap.
        hmPalette.put(tag, Palette3);

        /*
         * Palette 4
         */

        // Create a tag and a HashMap of colors to assign to Palette1
        tag = "Palette4";

        HashMap<String, Integer> colors4 = new HashMap<String, Integer>();
        colors4.put("1_black", Color.rgb(0, 0, 0));
        colors4.put("2_grey", Color.rgb(128, 128, 128));
        colors4.put("3_white", Color.rgb(255, 255, 255));
        colors4.put("4_lightgrey", Color.rgb(183, 183, 183));
        colors4.put("5_brown", Color.rgb(139, 69, 19));

        // Create a new palette based on this information.
        ColorPalette Palette4;

        if (isSavedState) {
            Palette4 = new ColorPalette(this, colors4, isSavedState, savedData.selectedColor);
        } else {
            Palette4 = new ColorPalette(this, colors4);
        }

        // Add the palette to the HashMap.
        hmPalette.put(tag, Palette4);
    }

    /**
     * Creates Image Buttons for each color defined in each palette.
     */
    private void loadPaletteButtons() {
        // Iterate through the palettes
        for (String key : hmPalette.keySet()) {
            // Load the button size.
            hmPalette.get(key).calculateButtonSize();
            // Get the palette object and create ImageButtons for the color set
            // that corresponds to that palette object.
            hmPalette.get(key).createButtons();
            // Add the palettes to a view.
        }

        // Set the left Palette buttons on the screen.
        hmPalette.get("Palette1").addToView(mLlColorPaletteLeft);

        // Set the left Palette buttons on the screen.
        hmPalette.get("Palette2").addToView(mLlColorPaletteLeft2);

        // Set the right Palette buttons on the screen.
        hmPalette.get("Palette3").addToView(mLlColorPaletteRight);

        // Set the right Palette buttons on the screen.
        hmPalette.get("Palette4").addToView(mLlColorPaletteRight2);
    }

    /**
     * Sets up the coloring canvas. Loads the bitmap and draws it to the screen
     * on the canvas.
     */
    private void loadColorCanvas() {

        // Load the first image in the currently selected coloring book.
        loadImage();

        // Add a tag so we can reference it later. Usually ids gen'd in a loop.
        colorGFX.setTag(0);

        // Give the ImageButton some parameters.
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(colorGFX.imageWidth,
                colorGFX.imageHeight);
        params.gravity = Gravity.CENTER;
        colorGFX.setLayoutParams(params);

        // Add the image view to the layout.
        mFlColorBody.addView(colorGFX);
        mFlColorBody.addView(pbFloodFill);
    }

    /**
     * Sets the image to display on the canvas.
     */
    private void loadImage() {

        // If the user goes below the minimum possible image, we cycle around
        // back
        // to the max.
        if (sCurrentImageId < mMinImageId) {
            sCurrentImageId = mMaxImageId;
        }

        // If the user goes above the maximum possible image, we cycle around
        // back
        // to the min.
        if (sCurrentImageId > mMaxImageId) {
            sCurrentImageId = mMinImageId;
        }

        // Get the node resource based on the currently selected image id.
        Node node = mNodeData[sCurrentImageId];
        // Find the image resource based on the coloring book id and the image
        // id.
        String mResourceName = node.body;
        // Find the image resource based on the coloring book id and the image
        // id.
        String mResourcePaint = mResourceName + "_map";

        // Get the resource id based on the image's file name.
        int resId = getResources().getIdentifier(mResourceName, "drawable",
                getPackageName());

        // Ensure that the image is sized to fit to screen.
        Bitmap picture = decodeImage(resId);

        // Instantiate the renderer if it doesn't yet exist.
        if (colorGFX == null) {
            // If a saved state is being restored...restore the path bitmap
            if (isSavedState && savedData != null) {

                colorGFX = new ColorGFX(this, picture.getWidth(), picture.getHeight(),
                        isSavedState, savedData.bitmap);
            }
            else {
                colorGFX = new ColorGFX(this, picture.getWidth(), picture.getHeight());
            }
        }
        else {
            // Clear the previous image and colors from the canvas.
            if (colorGFX.pathCanvas != null) {

                colorGFX.clear();
            }
        }

        // Clear the bitmaps from the screen.
        colorGFX.isNextImage = true;

        // Set the canvas's bitmap image so it can be drawn on canvas's run
        // method.
        colorGFX.pictureBitmapBuffer = picture;

        // Set the canvas's paint map.
        colorGFX.paintBitmapName = mResourcePaint;
    }

    /**
     * Resizes the image if it is too big for the screen. This should almost
     * never really be needed if the proper images are supplied to the drawable
     * folders. However, in practice this may not be the case and therefore,
     * this is used as a protection against these bad cases.
     */
    private Bitmap decodeImage(int resId) {

        // Get the screen width and height.
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        float screenWidth = dm.widthPixels;
        float screenHeight = dm.heightPixels;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(),
                resId, options);

        int inSampleSize = 1;
        int imageWidth = options.outWidth;
        int imageHeight = options.outHeight;

        // If the scale fails, we will need to use more memory to perform
        // scaling for the layout to work on all size screens.
        boolean scaleFailed = false;
        Bitmap scaledBitmap = null;
        float resizeRatioHeight = 1;

        // THIS IS DESIGNED FOR FITTING ON THE SCREEN WITH NO SCROLLBAR

        // Scale down if the image width exceeds the screen width.
        if (imageWidth > screenWidth || imageHeight > screenHeight) {

            // If we need to resize the image because the width or height is too
            // big, get the resize ratios for width and height.
            resizeRatioHeight = (float) imageHeight / (float) screenHeight;

            // Get the smaller ratio.
            inSampleSize = (int) resizeRatioHeight;

            if (inSampleSize <= 1) {
                scaleFailed = true;
            }
        }

        // Decode Bitmap with inSampleSize set
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;

        Bitmap picture = BitmapFactory.decodeResource(getResources(), resId, options);

        // If the scale failed, that means a scale was needed but didn't happen.
        // We need to create a scaled copy of the image by allocating more
        // memory.
        if (scaleFailed) {
            int newWidth = (int) (picture.getWidth() / resizeRatioHeight);
            int newHeight = (int) (picture.getHeight() / resizeRatioHeight);

            scaledBitmap = Bitmap.createScaledBitmap(picture, newWidth, newHeight, true);

            // Recycle the picture bitmap.
            picture.recycle();
        }
        else {
            // No scaling was needed in the first place!
            scaledBitmap = picture;
        }

        return scaledBitmap;
    }

    /**
     * Implements onBackPressed().
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        
        // Ensure that the music will continue to play when the user returns to
        // another activity.
        mContinueMusic = true;
    }

    /**
     * Implements onPause().
     */
    @Override
    protected void onPause() {
        super.onPause();

        // Pause the thread.
        colorGFX.pause();

        // If the music should not continue playing when leaving this activity,
        // pause it.
        if (!mContinueMusic) {
            MusicManager.pause();
        }

    }

    /**
     * Implements onResume().
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Resume the thread.
        colorGFX.resume();

        // If this activity continues playing the music, start the mediaplayer.
        mContinueMusic = false;
        MusicManager.start(this, MusicManager.MUSIC_A);
    }

    /**
     * Implements onClick().
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ibLeft:
                // Switch to the previous image.
                sCurrentImageId--;
                // Set the direction boolean for table row skipping if an id
                // doesn't
                // exist.
                isDirectionRight = false;

                // If the user goes below the minimum possible image, we cycle
                // around back
                // to the max.
                if (sCurrentImageId < mMinImageId) {
                    sCurrentImageId = mMaxImageId;
                }

                // If a fill op is happening, kill it.
                if (colorGFX.mThread != null && colorGFX.mThread.isAlive()) {
                    colorGFX.isThreadBroken = true;
                }

                // Set the savedState flag to false.
                isSavedState = false;
                // Load the image.
                loadImage();
                break;
            case R.id.ibRight:

                // Switch to the next image.
                sCurrentImageId++;
                // Set the direction boolean for table row skipping if an id
                // doesn't
                // exist.
                isDirectionRight = true;

                // If the user goes above the maximum possible image, we cycle
                // around back
                // to the min.
                if (sCurrentImageId > mMaxImageId) {
                    sCurrentImageId = mMinImageId;
                }

                // If a fill op is happening, kill it.
                if (colorGFX.mThread != null && colorGFX.mThread.isAlive()) {
                    colorGFX.isThreadBroken = true;
                }

                // Set the savedState flag to false.
                isSavedState = false;

                // Load the image.
                loadImage();

                break;

            case R.id.tbFillMode:

                // Check to see if erase mode is enabled.
                if (mTbEraseMode.isChecked()) {
                    // If it is, simply set this button as the enabled button.

                    // Prevent toggle.
                    mTbFillMode.setChecked(!mTbFillMode.isChecked());
                    colorGFX.isFillModeEnabled = mTbFillMode.isChecked();

                    // Disable erase mode
                    colorGFX.paint.setXfermode(null);
                    // Set the blur mode on again for path drawing.
                    colorGFX.paint.setMaskFilter(mBlur);
                    // Set the isEraseModeEnabled boolean
                    colorGFX.isEraseModeEnabled = false;

                    // Turn the eraser button off.
                    mTbEraseMode.setChecked(false);

                    // Replace the drawable with the color versions.
                    mTbFillMode.setBackgroundResource(R.drawable.bucket_button);
                }
                else {
                    colorGFX.isFillModeEnabled = mTbFillMode.isChecked();
                }

                break;

            case R.id.tbEraseMode:

                boolean isEraseModeEnabled = mTbEraseMode.isChecked();

                if (isEraseModeEnabled) {

                    // Set the disabled image resources for the brush and fill
                    // buttons.
                    mTbFillMode.setBackgroundResource(R.drawable.bucket_button_disabled);

                    // Set the current brush mode to erase.
                    colorGFX.paint.setXfermode(new PorterDuffXfermode(
                            PorterDuff.Mode.CLEAR));
                    // Take the blur mode off for the eraser.
                    colorGFX.paint.setMaskFilter(null);
                    // Set the colorGFX isEraseModeEnabled Boolean
                    colorGFX.isEraseModeEnabled = true;
                }
                else {
                    // Set the enabled image resources for the brush and fill
                    // buttons.
                    mTbFillMode.setBackgroundResource(R.drawable.bucket_button);

                    colorGFX.paint.setXfermode(null);
                    // Set the blur mode on again for path drawing.
                    colorGFX.paint.setMaskFilter(mBlur);
                    // Set the isEraseModeEnabled boolean
                    colorGFX.isEraseModeEnabled = false;
                }

                break;
        }
    }
}
