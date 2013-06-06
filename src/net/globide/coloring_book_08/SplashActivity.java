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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

/**
 * Displays a Splash Screen on application launch.
 */

public class SplashActivity extends Activity {

    private int mSplashTime = 5000; // length of time (in ms) to display the
                                    // splash screen

    // The thread running the splash screen wait process
    Thread splashThread = null;
    // Boolean to determine whether a thread should continue processing or if it
    // should stop.
    public boolean isThreadBroken = false;

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

        // Add default content.
        setContentView(R.layout.activity_splash);

        isThreadBroken = false;

        // Create a thread to handle our splash screen behavior. This is needed
        // for pausing the splash screen with sleep().
        splashThread = new Thread() {
            public void run() {
                try {

                    // TODO: Time elapsed code is to allow for future touch
                    // event handling that allows users to skip the splash
                    // screen.

                    // Set the running total of time elapsed
                    int waited = 0;

                    // While the time elapsed is less than [_splashTime],
                    // continue executing sleep() in intervals of 100 ms.
                    while (waited < mSplashTime) {
                        
                        if (!isThreadBroken) {
                            sleep(100);

                            // Add to the running total of time elapsed.
                            waited += 100;
                        }
                        else {
                            // A thread break signal has been sent. Kill the loop.
                            break;
                        }
                    }

                }
                catch (InterruptedException e) {
                    // Reinterrupt the current thread so other code may know the
                    // interrupt happened.
                    Thread.currentThread().interrupt();
                }
                finally {
                    if (!isThreadBroken) {

                        // Create a new intent setting the target action to be
                        // the
                        // MAINACTIVITY activity
                        Intent mainActivityIntent = new Intent(
                                "net.globide.coloring_book_08.MAINACTIVITY");

                        // Start MAINACTIVITY activity via our intent.
                        startActivity(mainActivityIntent);
                    }
                    else {
                        // The user signaled exit. End the activity without
                        // launching a new one.
                    }
                }
            }
        };

        // Run the thread.
        splashThread.start();
    }

    /**
     * Implements onBackPressed().
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // Break the thread and consequently signal a no-launch. Android OS will
        // take care of the application exit.
        isThreadBroken = true;
    }
}
