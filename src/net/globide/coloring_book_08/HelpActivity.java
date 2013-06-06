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
import android.os.Bundle;
import android.text.Html;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * This activity presents the user with application usage instructions.
 */

public class HelpActivity extends Activity {

    // Define whether or not this activity continues music from the
    // MusicManager.
    private boolean mContinueMusic = true;

    private TextView mTvHelpBody;

    /**
     * Implements onCreate().
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // We are using the Theme.Dialog theme in this activity's entry in the
        // manifest, thus, all we need to do is inflate the layout.
        super.onCreate(savedInstanceState);

        // Set the activity to full screen mode.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Add default content.
        setContentView(R.layout.activity_help);

        // Attach views to their corresponding resource ids.
        mTvHelpBody = (TextView) findViewById(R.id.tvHelpBody);

        // Parse the help string html.
        mTvHelpBody.setText(Html.fromHtml(getResources().getString(R.string.tv_help), null, null));
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
}
