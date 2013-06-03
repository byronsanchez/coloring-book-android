/*
 * Copyright (c) 2013 Byron Sanchez
 * www.chompix.com
 *
 * This file is part of "Coloring Book for Android."
 *
 * "Coloring Book for Android" is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version
 * 3 of the License, or (at your option) any later version.
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

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

/**
 * Displays an animated activity screen with scrolling credits.
 */

public class CreditsActivity extends Activity {

    // Define whether or not this activity continues music from the
    // MusicManager.
    private boolean mContinueMusic = true;

    // Define views.
    private RelativeLayout mLlCreditsBody;
    private ScrollView mSvCreditsBody;

    // Define display metric properties
    private float mScreenWidth;
    private float mScreenHeight;

    // Define scroll animation properties.
    private float mVerticalScrollMax;
    private float mVerticalScrollMin;
    private Timer mScrollTimer = null;
    private TimerTask mScrollerSchedule;
    private int mScrollPos = 0;

    // Use for solo bitmap.
    public Bitmap image;

    // Define the credits display arrays.
    private String[] mImageNameArray = {
            "coloring_book_credits"
    };

    // TODO: Make this pauseable on touch.

    /**
     * Implements onCreate().
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the activity to full screen mode.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_credits);

        // Calculate screen metrics
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;

        // Attach the views to their corresponding resource ids.
        mLlCreditsBody = (RelativeLayout) findViewById(R.id.llCreditsBody);
        mSvCreditsBody = (ScrollView) findViewById(R.id.svCreditsBody);

        // Attach the images to the LinearLayout that the ScrollView contains.
        addImagesToView();

        // Get a VTO to watch for layout changes on llCredits body which is with
        // the
        // ScrollView.
        ViewTreeObserver vto = mLlCreditsBody.getViewTreeObserver();
        // This listener is triggered when changes occur to the layout. We use
        // it to
        // determine when the layout is setup so we can determine the height.
        vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                // TODO: Add a reflection for this deprecation.
                
                // Remove the listener so this code isn't executed again.
                mLlCreditsBody.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                // Get the max scroll value.
                getScrollMaxAmount();
                // Set the initial scroll value to min + 1
                mSvCreditsBody.scrollBy(0, 1);
                // Start the scroll animation.
                startAutoScrolling();
            }
        });
    }

    /**
     * Adds images to the view. This is extendible to multiple images, but we
     * are currently only using one very large credits image.
     */
    public void addImagesToView() {
        // Loop through the credits image array.
        for (int i = 0; i < mImageNameArray.length; i++) {
            
            /*
             * ImageView
             */
            
            // Get the resource name from the array and through that, find the
            // id.
            final ImageView ibCredits = new ImageView(this);
            int imageResourceId = getResources().getIdentifier(mImageNameArray[i],
                    "drawable", getPackageName());

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(getResources(),
                    imageResourceId, options);
            
            int inSampleSize = 1;
            int imageWidth = options.outWidth;
            
            // The failed scale image bounds
            int newWidth;
            int newHeight;
            
            // The layout bounds.
            int dstWidth;
            int dstHeight;
            
            // If the scale fails, we will need to use more memory to perform
            // scaling for the layout to work on all size screens.
            boolean scaleFailed = false;
            float resizeRatioWidth = 1;
            
            // Scale down only if necessary.
            if (imageWidth != mScreenWidth) {
                resizeRatioWidth =  (float) imageWidth / mScreenWidth;
                
                inSampleSize = (int) resizeRatioWidth;
                
                if (inSampleSize <= 1) {
                    scaleFailed = true;
                }
            }
            
            // Decode Bitmap with inSampleSize set
            options.inSampleSize = inSampleSize;
            options.inJustDecodeBounds = false;
            Bitmap picture = BitmapFactory.decodeResource(getResources(), imageResourceId, options);
            
            // If the scale failed, that means a scale was needed but didn't happen.
            // We need to create a scaled copy of the image by allocating more
            // memory.
            if (scaleFailed) {
                newWidth = (int) Math.ceil((picture.getWidth() / resizeRatioWidth));
                newHeight = (int) Math.ceil((picture.getHeight() / resizeRatioWidth));
                
                image = Bitmap.createScaledBitmap(picture, newWidth, newHeight, true);
                
                // Recycle the picture bitmap.
                picture.recycle();
                
            } else {
                // No scaling was needed in the first place!
                image = picture;
                newWidth = picture.getWidth();
                newHeight = picture.getHeight();
            }
            
            // With the final decoded image info, define the layout bounds (2 *
            // the screen width + the image).
            dstWidth = (int) mScreenWidth;
            dstHeight = (int) (mScreenHeight * 2) + newHeight;

            // Create layout params with the calculated sizes.
            ScrollView.LayoutParams layoutParams = new ScrollView.LayoutParams((int) dstWidth,
                    (int) dstHeight);
            // Set the layout params to the relativelayout.
            mLlCreditsBody.setLayoutParams(layoutParams);

            // Set the image.
            ibCredits.setImageBitmap(image);
            // Add a tag so we can reference it later.
            ibCredits.setTag(i);

            // Give the LinearLayout containing the credits some parameters.
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            ibCredits.setLayoutParams(params);

            // Add the image view to the layout.
            mLlCreditsBody.addView(ibCredits);
        }
    }

    /**
     * Calculates and sets the maximum scroll value.
     */
    public void getScrollMaxAmount() {

        // Get the raw measured height of the LinearLayout (the total height of
        // all the combined credit image elements). We can also set a max scroll
        // limit by subtracting from this width.
        int actualHeight = (mLlCreditsBody.getMeasuredHeight());
        // Set the maximum scroll value.
        mVerticalScrollMax = actualHeight - mScreenHeight;
        mVerticalScrollMin = 1;
    }

    /**
     * Initiates the scrolling animation using a Timer.
     */
    public void startAutoScrolling() {
        // If the timer has not been previously created.
        if (mScrollTimer == null) {
            // Create a new timer.
            mScrollTimer = new Timer();
            // Define a Timer Tick, to be run repeatedly by the timer.
            final Runnable Timer_Tick = new Runnable() {
                public void run() {
                    // Move the scroll view.
                    moveScrollView();
                }
            };

            // If the Timer Scheduler has been previously created...
            if (mScrollerSchedule != null) {
                // Cancel the scheduler and set it to null
                mScrollerSchedule.cancel();
                mScrollerSchedule = null;
            }
            // Create a new scheduler.
            mScrollerSchedule = new TimerTask() {
                @Override
                public void run() {
                    // Whenever this scheduler gets executed, run the Timer
                    // Tick.
                    runOnUiThread(Timer_Tick);
                }
            };

            // Schedule the scheduler task in the timer.
            // The first int is how long to wait before initiating the task.
            // The second int is how long to wait between every subsequent task.
            mScrollTimer.schedule(mScrollerSchedule, 30, 30);
        }
    }

    /**
     * Moves the scroll view. This is called by the Timer Tick.
     */
    public void moveScrollView() {

        // Get the current scroll position and make sure it is int casted.
        // Add 1 to the position.
        mScrollPos = (int) (mSvCreditsBody.getScrollY() + 1.0);
        // If the new scrollPos is greater than or equal to the max vertical
        // scroll.

        // Loop around at the start of the credits screen.
        if (mScrollPos <= mVerticalScrollMin) {
            mScrollPos = (int) (mVerticalScrollMax - 1.0f);
        }

        // Loop around at the end of the credits screen.
        if (mScrollPos >= mVerticalScrollMax) {
            // Reset the scroll position.
            mScrollPos = (int) (mVerticalScrollMin + 1.0f);
        }
        // Set our calculated scroll position to the scrollview.
        mSvCreditsBody.scrollTo(0, mScrollPos);
    }

    /**
     * Stops the scrolling animation.
     */
    public void stopAutoScrolling() {
        // If the scroll timer has been previously created.
        if (mScrollTimer != null) {
            // Cancel the timer, thus killing the task.
            mScrollTimer.cancel();
            // Set the timer to null.
            mScrollTimer = null;
        }
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

        // Kill the activity.
        finish();
    }

    /**
     * Implements onPause().
     */
    @Override
    protected void onPause() {
        super.onPause();
        
        // TODO: Make the credits screen resizeable by recalculating screen
        // bounds and reloading the credits resource.

        // If the music should not continue playing when leaving this activity,
        // pause it.
        if (!mContinueMusic) {
            MusicManager.pause();
        }

        // Kill the activity.
        finish();
    }

    /**
     * Implements onResume().
     */
    @Override
    protected void onResume() {
        super.onResume();

        // If this activity continues playing the music, start the mediaplayer.
        mContinueMusic = false;
        MusicManager.start(this, MusicManager.MUSIC_A);
    }

    /**
     * Implements onDestroy().
     */
    @Override
    public void onDestroy() {
        // When this activity is killed, make sure to implement proper garbage
        // collection.

        // Clean up the timers.
        clearTimerTask(mScrollerSchedule);
        clearTimers(mScrollTimer);
        mScrollerSchedule = null;
        mScrollTimer = null;
        
        // Garbage collection for the bitmap.
        image.recycle();

        super.onDestroy();
    }

    /**
     * Garbage collection for timers.
     */
    private void clearTimers(Timer timer) {
        // If the timers are not already cleaned up...
        if (timer != null) {
            // Cancel the timer and clean up.
            timer.cancel();
            timer = null;
        }
    }

    /**
     * Garbage collection for timer tasks.
     */
    private void clearTimerTask(TimerTask timerTask) {
        // If the timer tasks are not already cleaned up...
        if (timerTask != null) {
            // Cancel the timer and clean up.
            timerTask.cancel();
            timerTask = null;
        }
    }
}
