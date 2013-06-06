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

/**
 * Defines an interface that handles touch events within the Main Activity
 * scope. This includes all fragments it contains. This interface allows
 * fragments to communicate with one another as well as with the host activity,
 * and therefore, pass input data to one another. This allows fragments to
 * behave differently based on the input data OTHER fragments may receive. This
 * interface is necessary to allow the host Main Activity to communicate with
 * multiple fragments using this same interface. The alternative would be to
 * define a local interface within each fragment, each with a different name to
 * prevent collisions. This alternative is obviously not as efficient as the
 * current implementation.
 */

// Callback interface that the host activity must implement to share events
// triggered by the various fragments it hosts.
public interface OnMainPanelTouchListener {
    public void onMainPanelTouchListener(int dbId);
}
