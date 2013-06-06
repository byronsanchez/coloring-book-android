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
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

// TODO: For v2, add a blank page to the main menu. This will require a stroke based fill algorithm.

/**
 * Creates the views used in MainActivity's ViewPager's children.
 */

public class MainPanelFragment extends Fragment implements
        OnMainPanelTouchListener, OnClickListener {

    // The current category id
    private int mCid;
    // The current category name
    private String mCategory;
    // The cover size
    public int coverSize;

    // Define a class listener property so we can fire back events to the host
    // activity.
    private OnMainPanelTouchListener mListener;

    /**
     * Implements onCreateView().
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the view for the search fragment.
        View view = inflater.inflate(R.layout.panel_main, container, false);

        // Get the host activity as an object.
        Activity activity = getActivity();

        // If the host activity exists...
        if (activity != null) {

            // Get the selected category data.
            Bundle extras = this.getArguments();
            mCid = extras.getInt("id");
            mCategory = extras.getString("category");

            /*
             * ImageView.
             */

            // Set the current palette cover.
            final ImageButton ibMainColor = (ImageButton) view.findViewById(R.id.ibMainColor);
            // Generate the resource name based on the current category id.
            int imageResourceId = getResources().getIdentifier("cover_" + mCid,
                    "drawable", activity.getPackageName());
            // Create a new drawable object to so we can draw our credits image.
            Drawable image = this.getResources().getDrawable(imageResourceId);

            ibMainColor.setImageDrawable(image);

            ibMainColor.setBackgroundColor(Color.TRANSPARENT);

            ibMainColor.setTag("ibMainColor");
            ibMainColor.setOnClickListener(this);
            
            /*
             * TextView.
             */
            
            // Set the category name as the title of this coloring book.
            final TextView tvMainTitle = (TextView) view.findViewById(R.id.tvPanelMain);
            tvMainTitle.setText(mCategory);

            /*
             * This screen needs to be dynamically positioned to fit each screen
             * size fluidly.
             */
            RelativeLayout mRlPanelMain = (RelativeLayout) view.findViewById(R.id.rlPanelMain);

            // Determine the floor section size
            Drawable imageFloor = this.getResources().getDrawable(R.drawable.floor);
            // Store the height locally
            int floorHeight = imageFloor.getIntrinsicHeight();

            // Resize the layout views to be centered in their proper positions
            // based on the sizes calculated.
            ViewGroup.LayoutParams paramsTop = mRlPanelMain.getLayoutParams();
            int resultHeight = floorHeight;
            paramsTop.height = resultHeight;
            mRlPanelMain.setLayoutParams(paramsTop);
        }

        return view;
    }

    /**
     * Implements onClick().
     */
    @Override
    public void onClick(View view) {
        String tag = view.getTag().toString();
        if (tag.equals("ibMainColor")) {
            // Fire an onMainPanelTouch event, passing the associated class name
            // to the host activity.
            mListener.onMainPanelTouchListener(mCid);
        }
    }

    /**
     * Implements onAttach().
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            // When the fragment is added to the display screen, have this
            // class's mListener property reference the host activity's
            // OnMainPanelTouchListener implementation. This allows this
            // fragment to both fire events to the host activity AND forces
            // activities using this fragment to implement
            // OnMainPanelTouchListener.
            mListener = (OnMainPanelTouchListener) activity;
        } catch (ClassCastException e) {
            // If the class using this fragment is NOT implementing
            // OnBrowseTouchListener, throw an exception.
            throw new ClassCastException(activity.toString()
                    + " must implement OnBrowseListTouchListener");
        }
    }

    /**
     * Implements onMainPanelTouchListener().
     */
    @Override
    public void onMainPanelTouchListener(int dbId) {
        // do nothing...we're just using the listener as a conduit for firing
        // events to the host activity.
    }
}
