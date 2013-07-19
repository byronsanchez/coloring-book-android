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
/*
 * NOTE: The Android Open Source Project holds the copyright to the
 * code ranging from lines 451-486. I hold the copyright to the rest
 * of the code.
 *
 * For all code copyrighted by The Android Open Source Project, the
 * subsequent license notice applies. The license notice remains
 * untouched and intact as per the license agreement. For the rest of
 * the code, the COPYING file in the root directory of this application
 * applies.
 */
/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.globide.coloring_book_08;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * Handles all canvas rendering actions.
 */

public class ColorGFX extends SurfaceView implements Runnable {

    // A thread, that will be used to execute code in parallel with the UI
    // thread.
    public Thread mThread;
    // Create a thread handler to queue code execution on a thread.
    private Handler mHandler;
    // Boolean to determine whether a thread should continue processing or if it
    // should stop.
    public boolean isThreadBroken = false;
    // Runnable counter for progress bar.
    private int mRunnableCounter = 0;

    // The id of the currently selected color.
    public int selectedColor = Color.BLACK;

    // Define whether or not fill is occuring.
    public boolean isFillEnabled = false;

    // Define whether or not fill mode is enabled (if not, then the mode is
    // considered draw mode).
    public boolean isFillModeEnabled = false;
    // Define whether ot not erase mode is enabled (this check is necessary to
    // take precedence over fillMode.
    public boolean isEraseModeEnabled = false;

    // Set the SurfaceView Thread properties.
    private SurfaceHolder mOurHolder;
    private Thread mOurThread = null;

    // Image metrics
    public int imageWidth;
    public int imageHeight;

    // Set the GFX properties.

    // Boolean for determining whether or not the canvas should be cleared for
    // the next image load.
    public boolean isNextImage = false;
    // The picture bitmap.
    public Bitmap pictureBitmap;

    public String paintBitmapName;

    // Bitmap buffers used when loading a new bitmap.
    // The picture bitmap.
    public Bitmap pictureBitmapBuffer;

    // The path bitmap.
    public Bitmap bitmap;
    // The canvas to draw on.
    public Canvas pathCanvas;
    // The canvas to fill on.
    public Canvas fillCanvas;
    // The canvas for all draws.
    public Canvas canvas;

    // The path buffers.
    private boolean mIsDrawn = false;

    // Set brush properties
    // Set a brush emboss.
    public MaskFilter emboss;
    // Set a brush blur.
    public MaskFilter blur;
    // The color of the path.
    public Paint paint;
    public Paint bitmapPaint;
    // The path to draw in draw mode.
    public Path mPath;

    // Cehck to see if data is being restored.
    public boolean isSavedData;

    // A list of points to floodfill whenever this tool is used.
    private boolean[][] mFloodfillList;
    // A list of points to floodfill whenever this tool is used.
    private boolean[][] mStrokefillList;

    private Context mContext;

    /**
     * Constructor.
     */
    public ColorGFX(Context context, int width, int height) {
        super(context);
        this.mContext = context;
        this.imageWidth = width;
        this.imageHeight = height;

        // Create the stroke path object.
        mPath = new Path();

        // Set a clear dithered background.
        bitmapPaint = new Paint(Paint.DITHER_FLAG);

        mOurHolder = getHolder();
    }

    /**
     * Constructor for restoring data.
     */
    public ColorGFX(Context context, int width, int height, boolean isSavedData,
            Bitmap savedPathBitmap) {
        super(context);
        this.mContext = context;
        this.imageWidth = width;
        this.imageHeight = height;
        this.isSavedData = isSavedData;
        this.bitmap = savedPathBitmap;

        // Create the stroke path object.
        mPath = new Path();

        // Set a clear dithered background.
        bitmapPaint = new Paint(Paint.DITHER_FLAG);

        mOurHolder = getHolder();
    }

    /**
     * Constructor.
     */
    public ColorGFX(Context context) {
        super(context);
        this.mContext = context;

        // Create the stroke path object.
        mPath = new Path();

        // Set a clear dithered background.
        bitmapPaint = new Paint(Paint.DITHER_FLAG);

        mOurHolder = getHolder();
    }

    /**
     * Pauses the thread.
     */
    public void pause() {

        // If a fill op is happening, kill it.
        if (mThread != null && mThread.isAlive()) {
            isThreadBroken = true;
        }

        // Kill the main canvas thread.
        mOurThread.interrupt();
    }

    /**
     * Resumes a paused thread. This is called as soon as the view is created.
     */
    public void resume() {
        // If a bitmap has been restored from a saved state, don't create a new
        // bitmap.
        if (bitmap == null && !isSavedData) {
            bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
        }
        pathCanvas = new Canvas(bitmap);
        mOurThread = new Thread(this);
        mOurThread.start();
    }

    /**
     * Executes while the thread is running.
     */
    public void run() {

        // While the thread is not paused...
        while ( !mOurThread.isInterrupted() ) {

            // If we don't have access to the surface (as another application is
            // currently using it), don't do anything until we have access.
            if (!mOurHolder.getSurface().isValid()) {
                continue;
            }
            // Get the canvas and lock it so other applications cannot use it.
            canvas = mOurHolder.lockCanvas();

            // If a new image hasn't been requested via the previous/next
            // buttons...

            // mBitmapCanvas.drawColor(0xFFAAAAAA);
            canvas.drawColor(Color.WHITE);

            // Draw the backup eraser bitmap.
            if (pictureBitmap != null) {
                canvas.drawBitmap(pictureBitmap, 0, 0, null);
            }

            if (bitmap != null) {
                if (!bitmap.isRecycled()) {
                    // draw the paint bitmap on the canvas
                    canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);
                }
            }

            // Only reset the path once it has been drawn to the pathCanvas.
            if (mIsDrawn) {
                mPath.reset();
                mIsDrawn = false;
            }

            canvas.drawPath(mPath, paint);

            // Draw the strokes bitmap.
            if (pictureBitmap != null) {
                canvas.drawBitmap(pictureBitmap, 0, 0, null);
            }

            if (isNextImage) {
                // If new images have been set in the buffer, load them before
                // the next draw.
                loadNewImages();

                isNextImage = false;
            }

            // Once we are done, unlock the canvas and update the display.
            mOurHolder.unlockCanvasAndPost(canvas);
        }

        if (mOurThread.isInterrupted()) {
          mOurThread = null;
        }
    }

    /**
     * Loads new images into the main bitmap caches.
     */
    public void loadNewImages() {

        if (pictureBitmap != null) {
            // Recycle the old bitmap.
            pictureBitmap.recycle();
        }

        int counter = 0;

        // Use a loop to ensure all bitmaps get loaded and prevent any potential
        // race conditions.
        while (true) {

            pictureBitmap = pictureBitmapBuffer.copy(Bitmap.Config.ARGB_8888, true);

            // If all bitmaps are loaded, break out of the loop.
            if (pictureBitmap != null) {
                break;
            }
            else if (counter > 1000) {
                // TODO: throw a timeout exception. Resource is not loading or
                // something is hanging. Right now we'll just break so we don't
                // over consume resources. The error might simply crash the
                // program for the user if this ever happens.
                break;
            }

            counter++;
        }

        // Clear the buffers for future use.
        pictureBitmapBuffer.recycle();
    }

    /**
     * Clears the path canvas of any color.
     */
    public void clear() {
        pathCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }

    /**
     * Clears all canvases of any color.
     */
    public void clearAllCanvases() {
        pathCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        fillCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }

    float lastX = -1;
    float lastY = -1;

    /**
     * Handles all user input events to manipulate the screen rendering.
     */
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();

        // Set defaults as per the bounds hit. This way, the draw limit is
        // AT the bounds and any path draws aren't interrupted just because
        // a user tries to go out of bounds. This will allow for erases and
        // draws at edges to not be interrupted producing artifacts or give
        // them a hard time trying to erase/draw near the bounds.

        // If the user exceeds any bound.
        if (x < 0) {
            // Check if a draw has previously occured within bounds.
            if (lastX == -1) {
                // If not, then use the default bound limit.
                x = 0;
            } else {
                // Else, use the last acceptable inbound position.
                x = lastX;
            }
        }
        // Do the same for the upper bound limit.
        else if (x > pathCanvas.getWidth()) {
            if (lastX == -1) {
                x = pathCanvas.getWidth();
            } else {
                x = lastX;
            }
        }
        // If a user is in bound...
        else {
            // Locally cache the bound for future out of bound handling.
            lastX = x;
        }

        // Rinse and repeat for y.
        if (y < 0) {
            if (lastY == -1) {
                y = 0;
            } else {
                y = lastY;
            }
        }
        else if (y > pathCanvas.getHeight()) {
            if (lastY == -1) {
                y = pathCanvas.getHeight();
            } else {
                y = lastY;
            }
        }
        else {
            lastY = y;
        }

        /*
         * After bounds have been processed, start drawing the path with the
         * constrained coordinates.
         */

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!isFillModeEnabled || isEraseModeEnabled) {
                    touch_start(x, y);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isFillModeEnabled || isEraseModeEnabled) {
                    touch_move(x, y);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!isFillModeEnabled || isEraseModeEnabled) {
                    touch_up();
                    invalidate();
                }
                else {
                    this.isFillEnabled = true;
                    fillHandler(x, y);
                    invalidate();
                }
                break;
        }
        return true;
    }

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    /**
     * Starts a new path.
     */
    private void touch_start(float x, float y) {

        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    /**
     * Moves the path.
     */
    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    /**
     * Completes the path.
     */
    private void touch_up() {
        mPath.lineTo(mX, mY);
        // commit the path to our offscreen
        pathCanvas.drawPath(mPath, paint);
        mIsDrawn = true;
    }

    /**
     * Handles fill operations when a fill event occurs.
     */
    private void fillHandler(float x, float y) {
        // If the user has set fill mode on...
        if (isFillModeEnabled) {
            // Check for the fill signal and ensure that a point has been set to
            // begin the flood fill algorithm.
            if (isFillEnabled) {

                // If a fill op is currently happening, do not register a new
                // op!
                if (mRunnableCounter >= 1) {
                    // Signal end of op.
                    isFillEnabled = false;
                    return;
                }

                // Get the corresponding fill map file resource.
                int resPaintId = mContext.getResources().getIdentifier(paintBitmapName,
                        "drawable", mContext.getPackageName());

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeResource(getResources(),
                        resPaintId, options);

                int inSampleSize = 1;
                int fillImageWidth = options.outWidth;
                int fillImageHeight = options.outHeight;
                // If the scale fails, we will need to use more memory to
                // perform scaling for the layout to work on all size screens.
                boolean scaleFailed = false;
                Bitmap paintMap = null;
                float resizeRatioHeight = 1;

                // THIS IS DESIGNED FOR FITTING ON THE SCREEN WITH NO SCROLLBAR

                // Scale down if the image width exceeds the screen width.
                if (fillImageWidth > imageWidth || fillImageHeight > imageHeight) {

                    // If we need to resize the image because the width or
                    // height is too
                    // big, get the resize ratios for width and height.
                    resizeRatioHeight = (float) fillImageHeight / (float) imageHeight;

                    // Get the smaller ratio.
                    inSampleSize = (int) resizeRatioHeight;

                    if (inSampleSize <= 1) {
                        scaleFailed = true;
                    }
                }

                // Decode Bitmap with inSampleSize set
                options.inSampleSize = inSampleSize;
                options.inJustDecodeBounds = false;

                // colorGFX.isScaled = true;
                // colorGFX.inSampleSize = inSampleSize;
                Bitmap picture = BitmapFactory.decodeResource(getResources(), resPaintId, options);

                // If the scale failed, that means a scale was needed but didn't
                // happen. We need to create a scaled copy of the image by allocating
                // more memory.
                if (scaleFailed) {
                    int newWidth = (int) (picture.getWidth() / resizeRatioHeight);
                    int newHeight = (int) (picture.getHeight() / resizeRatioHeight);

                    paintMap = Bitmap.createScaledBitmap(picture, newWidth, newHeight, true);

                    // Recycle the picture bitmap.
                    picture.recycle();
                }
                else {
                    // No scaling was needed in the first place!
                    paintMap = picture;
                }

                // Convert the bitmap to a mutable bitmap.
                Bitmap mutablePaintMap = paintMap.copy(Config.ARGB_8888, true);

                // Recycle the immutable bitmap
                paintMap.recycle();

                // Get the color of the selected point.
                int targetColor = mutablePaintMap.getPixel((int) x, (int) y);

                // If the target color is not black (black means it is the
                // stroke color).
                if (targetColor == Color.TRANSPARENT) {

                    // Fill all unbounded pixels of that color.
                    Point node = new Point((int) x, (int) y);

                    mRunnableCounter++;

                    ((ColorActivity) mContext).pbFloodFill.setVisibility(View.VISIBLE);

                    // Runnable Flood fill
                    LoadViewTask floodfillTask = new LoadViewTask();
                    floodfillTask.image = mutablePaintMap;
                    floodfillTask.point = node;
                    floodfillTask.target = targetColor;
                    floodfillTask.replacementColor = selectedColor;

                    // Initialize the handler
                    mHandler = new Handler();
                    // Initialize the thread
                    mThread = new Thread(floodfillTask, "FloodFillThread");
                    // start the thread.
                    mThread.start();
                }
            }
        }
    }

    /**
     * Colors all pixels from the flood fill algorithm.
     */
    public void colorPixels(Bitmap picture, int replacementColor) {
        // Both arrays are the same size, so just choose one to control the
        // iteration.
        for (int i=0; i < mFloodfillList.length; i++) {
            for (int j=0; j < mFloodfillList[i].length; j++) {
                if (mFloodfillList[i][j] != false) {
                    picture.setPixel(i, j, replacementColor);
                }
                if (mStrokefillList[i][j] != false) {
                    picture.setPixel(i, j, replacementColor);
                }
            }
        }
    }

    /**
     * Clears the stroke and floodfill pixel lists.
     */
    public void clearPixelLists() {
        mStrokefillList = null;
        mFloodfillList = null;
    }

    /**
     * Class encapsulating the floodfill algorithm task.
     */

    private class LoadViewTask implements Runnable {

        public Point point;
        public int target;
        public int replacementColor;
        public Bitmap image;

        // Define the array primitives. This is faster and will ultimately use
        // less memory than array lists that can dynamically allocate memory.
        public boolean[][] list;
        public boolean[][] strokeList;

        /**
         * Implements run().
         */
        @Override
        public void run() {

            // Get the current thread's token
            synchronized (mThread) {

                floodFill(point, target, replacementColor, image);

                // Update the changes to the UI thread
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Set the current Progress
                    }
                });
            }

            // Similar to onPostExecute
            mHandler.post(new Runnable() {
                @Override
                public void run() {

                    if (!isThreadBroken) {

                        // Now that the lists have been generated, we can
                        // garbage collect the paint bitmap.
                        image.recycle();

                        // Pass the generated fill and stroke list data back to
                        // the UI thread class.
                        mFloodfillList = list;
                        mStrokefillList = strokeList;

                        // Create the bitmap that will be added to the paint
                        // layer.
                        Bitmap fillPicture = Bitmap.createBitmap(imageWidth, imageHeight,
                                Bitmap.Config.ARGB_8888);

                        // Color the list of pixels generated from the flood
                        // fill algorithm.
                        colorPixels(fillPicture, replacementColor);

                        // Paint filter to draw the paint over what is already
                        // on the paint layer.
                        Paint addFilter = new Paint();
                        addFilter
                                .setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

                        // Save the flood filled image so it can persist in
                        // future flood fills and path draws.
                        // mPaintBitmap = picture;

                        // Draw the newly filled image onto the path canvas.
                        pathCanvas.drawBitmap(fillPicture, 0, 0, addFilter);

                    }

                    clearPixelLists();
                    // Close the progress dialog
                    // progressDialog.dismiss();
                    mRunnableCounter--;

                    // If there are no more runnables running a flood fill job,
                    // remove the progress bar view from the display.
                    if (mRunnableCounter == 0) {
                        ((ColorActivity) mContext).pbFloodFill.setVisibility(View.GONE);
                        // Once all threads have ended, we can set the boolean
                        // check for threads back to false so they work again.
                        isThreadBroken = false;
                    }
                }
            });

            // Try to kill the thread, by interrupting its execution
            synchronized (mThread) {
                mThread.interrupt();
            }
        }

        /**
         * Runs a typical floodfill algorithm, replacing all pixels of one color
         * with another.
         */
        public void floodFill(Point node, int targetColor,
                int replacementColor, Bitmap picture) {

            // Define the bitmap width and height.
            int width = picture.getWidth();
            int height = picture.getHeight();

            // Initialize the arrays according to the image metrics.
            list = new boolean[width][height];
            strokeList = new boolean[width][height];

            // Define the target and replacement color.
            int target = targetColor;
            int replacement = replacementColor;

            // Start the flood fill algorithm.
            if (target != replacement) {
                // Set the empty queue and run the algorithm at least once (or
                // alternatively, set the point to the end of queue and run a
                // while loop that performs this algorithm so long as the Queue is not
                // empty).
                Queue<Point> queue = new LinkedList<Point>();

                // Run the loop at least once for the selected pixel.
                do {

                    if (isThreadBroken) {
                        break;
                    }

                    // Store the current pixel in local variables.
                    int x = node.x;
                    int y = node.y;

                    // while x is not at the origin AND the color of it's West
                    // neighboring pixel is changeable.
                    while (x > 0 && picture.getPixel(x - 1, y) == target) {
                        // Continuously decrement x (AKA bring x as far to the
                        // west as possible given the color constraints).
                        x--;
                    }

                    // Given the above while loop, we are now as far West as we
                    // can be and are currently at a pixel we will need to replace.

                    // Set directional booleans.
                    boolean spanUp = false;
                    boolean spanDown = false;

                    // While x has not reached as far East as it can in the
                    // bitmap (AKA hasn't hit the end of the image and hasn't
                    // reached a color different than the replacement color)...
                    while (x < width && picture.getPixel(x, y) == target) {

                        // Replace the current pixel color.
                        picture.setPixel(x, y, replacement);
                        // Add the pixel to the flood fill list.
                        list[x][y] = true;

                        // If we don't take the stroke paths into consideration
                        // and color them where necessary, we WILL have reduced
                        // aliasing, but not perfect anti-aliasing. By coloring
                        // the paths, we have virtually ZERO aliasing.

                        // TOP

                        if (y + 1 < height - 1 && picture.getPixel(x, y + 1) != target) {
                            strokeList[x][y + 1] = true;
                        }

                        // RIGHT
                        if (x + 1 < width - 1 && picture.getPixel(x + 1, y) != target) {
                            strokeList[x + 1][y] = true;
                        }

                        // LEFT
                        if (x - 1 > 0 && picture.getPixel(x - 1, y) != target) {
                            strokeList[x - 1][y] = true;
                        }

                        // BOTTOM
                        if (y - 1 > 0 && picture.getPixel(x, y - 1) != target) {
                            strokeList[x][y - 1] = true;
                        }

                        // Add one SOUTH point to the queue if it is replaceable
                        // (this will be the next relative point to check from)
                        // and we have not previously moved down.
                        if (!spanUp && y > 0 && picture.getPixel(x, y - 1) == target) {
                            queue.add(new Point(x, y - 1));
                            spanUp = true;
                        }
                        // If the SOUTH point is unreplaceable or we have
                        // previously moved up set the boolean to false.
                        else if (spanUp && y > 0 && picture.getPixel(x, y - 1) != target) {
                            spanUp = false;
                        }

                        // Add one NORTH point to the queue if it is replaceable
                        // (this will be the next relative point to check from)
                        // and we have not previously moved up.
                        if (!spanDown && y < height - 1
                                && picture.getPixel(x, y + 1) == target) {
                            queue.add(new Point(x, y + 1));
                            spanDown = true;
                        }
                        // If the NORTH point is unreplaceable or we have
                        // previously moved up set the boolean to false.
                        else if (spanDown && y < height - 1
                                && picture.getPixel(x, y + 1) != target) {
                            spanDown = false;
                        }

                        // Increment the x-position, 1 to the east.
                        x++;
                    }
                }
                // Remove the head of this queue. Keep looping until no pixels
                // remain.
                while ((node = queue.poll()) != null);
            }

            // Once the Flood Fill Algorithm has completed, turn the action flag
            // off. We're done.
            isFillEnabled = false;
        }
    }
}
