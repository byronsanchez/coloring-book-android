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

/**
 * Defines the class that will store a row from the node table in the database.
 */

public class Node {
    // The current row's node id.
    public int id;
    // The node's image location.
    public String body;
    // The node's name.
    public String title;

    /**
     * Constructs the Node object.
     */
    public Node(int id, String title, String body) {
        this.id = id;
        this.title = title;
        this.body = body;
    }

    /**
     * Constructs the CategoryModel object.
     */
    public Node() {
    }
}
