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

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Manages the fragments used in MainActivity's ViewPager.
 */

public class MainPagerAdapter extends FragmentPagerAdapter {

    // Define a list to contain the fragments associated with this pager.
    private List<Fragment> mFragments;

    /**
     * Constructor to set the fragment manager and the list of fragments to
     * associate with this pager.
     */
    public MainPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        this.mFragments = fragments;
    }

    /**
     * Returns a fragment from the list of pager fragments.
     */
    public Fragment getItem(int position) {
        return this.mFragments.get(position);
    }

    /**
     * Returns the number of panels contained within this Pager.
     */
    public int getCount() {
        return this.mFragments.size();
    }
}
