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

import android.graphics.Bitmap;
import android.graphics.Paint;

public class ColorGfxData {

    // The id of the currently selected color.
    public int selectedColor;
    
    // Define whether or not fill mode is enabled (if not, then the mode is
    // considered draw mode).
    public boolean isFillModeEnabled = false;
    // Define whether ot not erase mode is enabled (this check is necessary to
    // take precedence over fillMode.
    public boolean isEraseModeEnabled = false;
    
    // The path bitmap.
    public Bitmap bitmap;
    
    // The color of the path.
    public Paint paint;
    
    public int currentImageId;
    
    ColorGfxData() {}
    
}
