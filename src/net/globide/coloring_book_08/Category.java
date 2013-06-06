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
 * Defines the class that will store a row from the category table in the
 * database.
 */
public class Category {

    // The current row's category id.
    public int id;
    // The current category name.
    public String category;
    // The category description.
    public String description;
    // Whether or not the category is enabled.
    public int isAvailable;
    // The SKU id of the current coloring book
    public String sku;

    /**
     * Constructs the CategoryModel object.
     */
    public Category(int id, String category, String description, int isAvailable, String sku) {
        this.id = id;
        this.category = category;
        this.description = description;
        this.isAvailable = isAvailable;
        this.sku = sku;
    }

    /**
     * Constructs the CategoryModel object.
     */
    public Category() {
    }
}
