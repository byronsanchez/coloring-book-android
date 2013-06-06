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

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * Scroller class derivative used to control the scroller animation speed in
 * MainActivity.java.
 */

public class FixedSpeedScroller extends Scroller {

    // Define the scroller animation duration.
    private int mDuration = 500;

    /**
     * Constructor.
     */
    public FixedSpeedScroller(Context context) {
        super(context);
    }

    /**
     * Constructor.
     */
    public FixedSpeedScroller(Context context, Interpolator interpolator) {
        super(context, interpolator);
    }

    /**
     * Implements startScroll().
     */
    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        // Override the default duration and use our own.
        super.startScroll(startX, startY, dx, dy, mDuration);
    }

    /**
     * Implements startScroll().
     */
    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
        // Override the default duration and use our own.
        super.startScroll(startX, startY, dx, dy, mDuration);
    }
}
