/*
 * Copyright (c) 2013 Byron Sanchez
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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ToggleButton;

/**
 * Outputs a list of user-configurable settings to affect the user experience of
 * the application. Also provides buttons to access other parts of the
 * application, such as the shop or to view production credits (this is placed
 * here to prevent negative UX due to placing shop and credits somewhere where
 * the user can always see them. The settings place might be a good candidate as
 * it is not-interfering and consequently, more of an opt-in if you want to
 * access the credits or the shop).
 */

public class SettingsActivity extends Activity implements OnClickListener {

    // Define whether or not this activity continues music from the
    // MusicManager.
    private boolean mContinueMusic = true;

    // SharedPreferences persistent data storage properties
    private SharedPreferences mSharedPreferences;
    private Editor mEditor;

    // SharedPreferences storage properties
    private static String sFilename = "coloring_book_settings";

    // Define views used for event handling.
    private ToggleButton mTbSettingsMusic;
    private ImageButton mIbSettingsShop;
    private ImageButton mIbSettingsCredits;

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
        setContentView(R.layout.activity_settings);

        // Get stored preferences, if any.
        mSharedPreferences = getSharedPreferences(sFilename, 0);
        // Setup the editor in this function, so it can be used anywhere else if
        // needed.
        mEditor = mSharedPreferences.edit();

        // Attach the defined views to their corresponding resource ids.
        mTbSettingsMusic = (ToggleButton) findViewById(R.id.tbSettingsMusic);
        mIbSettingsShop = (ImageButton) findViewById(R.id.ibSettingsShop);
        mIbSettingsCredits = (ImageButton) findViewById(R.id.ibSettingsCredits);

        // Set listeners for our input views.
        mTbSettingsMusic.setOnClickListener(this);
        mIbSettingsShop.setOnClickListener(this);
        mIbSettingsCredits.setOnClickListener(this);

        // Update output based on stored preferences, if any.
        updateView();
    }

    /**
     * Updates the text preview based on text configuration preferences.
     */
    private void updateView() {
        // Get the stored preferences.
        mSharedPreferences = getSharedPreferences(sFilename, 0);

        // Store the preference values in local variables.
        boolean tbSettingsMusicIsChecked = mSharedPreferences.getBoolean(
                "tbSettingsMusicIsChecked", false);

        // Update the tbSettingsMusic Toggle Button.
        // This is needed for the initial loading of this view. In manual
        // selections, it's redundant.
        mTbSettingsMusic.setChecked(tbSettingsMusicIsChecked);

        // Update actual status
        MusicManager.updateVolume();
        MusicManager.updateStatusFromPrefs(this);

        // Set whether music is on or not in the Music Manager
        if (tbSettingsMusicIsChecked) {
            MusicManager.start(this, MusicManager.MUSIC_A);
        }
        else {
            MusicManager.release();
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
    }

    /**
     * Implements onPause().
     */
    @Override
    protected void onPause() {
        super.onPause();

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

        // If this activity continues playing the music, start the mediaplayer.
        mContinueMusic = false;
        MusicManager.start(this, MusicManager.MUSIC_A);
    }

    /**
     * Implements onClick().
     */
    @Override
    public void onClick(View v) {
        // Setup an intent variable for potential new activity intents.
        Intent i;
        // Setup the string to contain the action name for an activity, should
        // the user trigger a new activity event.
        String intentClass = "";
        // Create a Class variable which will contain the Activity class we
        // intend to trigger.
        Class<?> selectedClass = null;

        switch (v.getId()) {
        // The music toggle button was clicked.
            case R.id.tbSettingsMusic:

                // Set the music toggle preference to be the opposite of what it
                // currently
                // is.
                mEditor
                        .putBoolean("tbSettingsMusicIsChecked", mTbSettingsMusic.isChecked());
                mEditor.commit();

                // Update the preview based on the new preference.
                updateView();

                break;
            // Shop Activity button.
            case R.id.ibSettingsShop:
                intentClass = "ShopActivity";
                break;
            // Credits Activity button.
            case R.id.ibSettingsCredits:
                intentClass = "CreditsActivity";
                break;
        }
        // If intentClass is NOT empty due to an activity button being
        // clicked...
        if (intentClass != "") {
            try {
                // Create a Class variable containing the Activity class we
                // intend to trigger.
                selectedClass = Class.forName("net.globide.coloring_book_08."
                        + intentClass);
            } catch (ClassNotFoundException e) {
                // If, for some STRANGE reason, the class is one that does not
                // exist, launch the ShopActivity. This will be considered the
                // default case.
                intentClass = "ShopActivity";
                // Create a Class variable containing the Activity class we
                // intend to trigger.
                try {
                    selectedClass = Class.forName("net.globide.coloring_book_08." + intentClass);
                } catch (ClassNotFoundException e2) {
                    // If the default class does not exist, something is wrong
                    // with the installation of the program. Crash.
                    throw new RuntimeException(
                            "Application installation is corrupt. Please reinstall the application.");
                }
            } finally {
                if (selectedClass != null) {
                    // Create a new intent based on the selected class.
                    i = new Intent(this, selectedClass);
                    // Start the activity.
                    startActivity(i);
                }
            }
        }
    }
}
