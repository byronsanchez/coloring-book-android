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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Handles all database requests made by the application.
 */

public class NodeDatabase extends SQLiteOpenHelper {

    // Define the SQLite database location.
    private static final String DATABASE_PATH = "/data/data/net.globide.coloring_book_08/databases/";
    private static final String DATABASE_NAME = "coloring_book.db";

    // Define the tables used in the application.
    private static final String DATABASE_TABLE_NODE = "node";
    private static final String DATABASE_TABLE_CATEGORIES = "categories";

    // Define the "node" table SQLite columns
    private static final String KEY_NODE_ROWID = "_id";
    private static final String KEY_NODE_CATEGORYID = "cid";
    private static final String KEY_NODE_TITLE = "title";
    private static final String KEY_NODE_BODY = "body";

    // Define the "categories" table SQLite columns
    private static final String KEY_CATEGORIES_ROWID = "_id";
    private static final String KEY_CATEGORIES_CATEGORY = "category";
    private static final String KEY_CATEGORIES_DESCRIPTION = "description";
    private static final String KEY_CATEGORIES_ISAVAILABLE = "isAvailable";
    private static final String KEY_CATEGORIES_SKU = "sku";

    // Define the current schema version.
    private static final int SCHEMA_VERSION = 1;

    // Define the context and the actual database property.
    private final Context mOurContext;
    private SQLiteDatabase mOurDatabase;

    // Define query builder properties.
    private String mLeftOperand;
    private String mRightOperand;
    private String mOperator;

    // Upon construction, pass the database information and set the context.
    public NodeDatabase(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA_VERSION);
        mOurContext = context;
    }

    /**
     * Wrapper method for creating the database. Intended for external access.
     */
    public void createDatabase() {
        createDB();
    }

    /**
     * Creates the database or establishes the database connection.
     */
    private void createDB() {
        // Check to see if the database exists. (Typically, on first run, it
        // should
        // not exist yet).
        boolean dbExist = DBExists();

        // If a database does not exist, create one.
        if (!dbExist) {
            // Create an empty database in the default system location.
            mOurDatabase = getReadableDatabase();

            // Copy our pre-populated database in /assets/ to the emty database
            // we
            // just created.
            copyDBFromResource();
        }
        else {
            // If the database exists, just call it for use.
            mOurDatabase = getReadableDatabase();
        }
    }

    /**
     * Checks to see if a database file exists in the default system location.
     */
    private boolean DBExists() {
        // Create a local database accessor variable for our check.
        SQLiteDatabase db = null;

        try {
            // Define a string containing the default system database file path
            // for
            // our application's database.
            String databasePath = DATABASE_PATH + DATABASE_NAME;
            // Open the database.
            db = SQLiteDatabase.openDatabase(databasePath, null,
                    SQLiteDatabase.OPEN_READWRITE);
            // Set the database locale.
            db.setLocale(Locale.getDefault());
            // Set the database version.
            db.setVersion(SCHEMA_VERSION);
        } catch (SQLiteException e) {
            // DO NOT HANDLE THIS ERROR.

            // This catch block is used as part of a boolean check. If the
            // database does NOT exist, db will be null and a database will be
            // created from the assets folder.
        }

        // If the database is not null.
        if (db != null) {
            // Close this local connection. We know it exists now...
            db.close();
        }

        // If db is not null, database exists, return true.
        // Else, db does NOT exist, return false.
        return db != null ? true : false;
    }

    /**
     * Copies our pre-populated database to the default system database location
     * for our application.
     */
    private void copyDBFromResource() {
        // Define the I/O streams.
        InputStream inputStream = null;
        OutputStream outputStream = null;
        // Define the default system location for our application's database.
        String dbFilePath = DATABASE_PATH + DATABASE_NAME;

        try {
            // Based on the context, get it's assets and open the pre-populated
            // database file.
            inputStream = mOurContext.getAssets().open(DATABASE_NAME);
            // Define an output stream set to the default system location for
            // our
            // application's database file.
            outputStream = new FileOutputStream(dbFilePath);

            // Define a byte buffer in chunks of 1024 bytes.
            byte[] buffer = new byte[1024];
            // Integer variable to use in our file looper.
            int length;

            // While the length of the buffer is greater than 0 (there is
            // still more to read)...
            while ((length = inputStream.read(buffer)) > 0) {
                // Write the next chunk from the buffer to the output stream.
                outputStream.write(buffer, 0, length);
            }

            // Flush and close the I/O strams.
            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } catch (FileNotFoundException fileNotFoundException) {

            // The system database storage location does not exist! Crash the
            // application.
            throw new RuntimeException(
                    "Failed to load default database storage location.");

        } catch (IOException ioException) {

            // The database file does not exist! This is a critical error as
            // there are no stories to load and therefore no content! Crash the
            // application.
            throw new RuntimeException(
                    "Failed to copy database from assets.");

        } finally {
            try {
                outputStream.close();
                inputStream.close();
            } catch (IOException ioException) {
                // If for some reason, closing the streams fails...
                throw new RuntimeException(
                        "Failed to load default database storage location.");
            }
        }
    }

    /**
     * Returns a full list of categories from the categories table.
     */
    public Category[] getCategoryListData() {

        // Initialize a where string to contain a potential WHERE clause.
        String where = "";

        // If the WHERE clause properties were set...
        if (this.mLeftOperand != null && this.mRightOperand != null
                && this.mOperator != null) {
            // Define the WHERE clause.
            where = this.mLeftOperand + this.mOperator + this.mRightOperand;
        }

        // Define an array of columns to SELECT.
        String[] columns = new String[] {
                KEY_CATEGORIES_ROWID,
                KEY_CATEGORIES_CATEGORY,
                KEY_CATEGORIES_DESCRIPTION,
                KEY_CATEGORIES_ISAVAILABLE,
                KEY_CATEGORIES_SKU
        };
        // Define the cursor to contain our query results, and execute the
        // query.
        Cursor c;

        // If a WHERE clause was defined.
        if (where != "") {
            // Execute the query with the WHERE clause.
            c = mOurDatabase.query(DATABASE_TABLE_CATEGORIES, columns, where, null, null,
                    null, KEY_CATEGORIES_ROWID + " ASC");
        }
        else {
            // Execute the query.
            c = mOurDatabase.query(DATABASE_TABLE_CATEGORIES, columns, null, null, null,
                    null, KEY_CATEGORIES_ROWID + " ASC");
        }

        // Define an array per column, for dynamic results.
        ArrayList<Integer> resultId = new ArrayList<Integer>();
        ArrayList<String> resultCategory = new ArrayList<String>();
        ArrayList<String> resultDescription = new ArrayList<String>();
        ArrayList<Integer> resultIsAvailable = new ArrayList<Integer>();
        ArrayList<String> resultSku = new ArrayList<String>();

        // Define indices for each column, for iteration.
        int id = c.getColumnIndex(KEY_CATEGORIES_ROWID);
        int category = c.getColumnIndex(KEY_CATEGORIES_CATEGORY);
        int description = c.getColumnIndex(KEY_CATEGORIES_DESCRIPTION);
        int isAvailable = c.getColumnIndex(KEY_CATEGORIES_ISAVAILABLE);
        int sku = c.getColumnIndex(KEY_CATEGORIES_SKU);

        // Loop through the results in the Cursor.
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            // Add each id to the id ArrayList.
            resultId.add(c.getInt(id));
            // Add each category to the category ArrayList.
            resultCategory.add(c.getString(category));
            // Add each description to the description ArrayList
            resultDescription.add(c.getString(description));
            // Add each availability to the availability ArrayList
            resultIsAvailable.add(c.getInt(isAvailable));
            // Add each sku id to the sku ArrayList
            resultSku.add(c.getString(sku));
        }

        // Close the cursor.
        c.close();

        // Define an array in which to store the results.
        Category[] resultArray = new Category[resultId.size()];

        // Convert the ArrayList to an array.
        for (int i = 0; i < resultId.size(); i++) {

            resultArray[i] = new Category();
            resultArray[i].id = resultId.get(i);
            resultArray[i].category = resultCategory.get(i);
            resultArray[i].description = resultDescription.get(i);
            resultArray[i].isAvailable = resultIsAvailable.get(i);
            resultArray[i].sku = resultSku.get(i);
        }

        // Return the array.
        return resultArray;
    }

    /**
     * Returns a list of node titles filtered by a category id.
     */
    public Node[] getNodeListData(long cid) {

        // Define an array of columns to SELECT.
        String[] columns = new String[] {
                KEY_NODE_ROWID, KEY_NODE_TITLE,
                KEY_NODE_BODY
        };
        // Define the cursor to contain our query results, and execute the
        // query.
        Cursor c = mOurDatabase.query(DATABASE_TABLE_NODE, columns,
                KEY_NODE_CATEGORYID + "=" + cid, null, null, null, null);

        // Define an array per column, for dynamic results.
        ArrayList<Integer> resultId = new ArrayList<Integer>();
        ArrayList<String> resultTitle = new ArrayList<String>();
        ArrayList<String> resultBody = new ArrayList<String>();

        // Define indices for each column, for iteration.
        int id = c.getColumnIndex(KEY_NODE_ROWID);
        int title = c.getColumnIndex(KEY_NODE_TITLE);
        int body = c.getColumnIndex(KEY_NODE_BODY);

        // Loop through the results in the Cursor.
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            // Add each id to the id ArrayList.
            resultId.add(c.getInt(id));
            // Add each node title to the node title ArrayList.
            resultTitle.add(c.getString(title));
            // Add each body to the body ArrayList.
            resultBody.add(c.getString(body));
        }

        // Close the cursor.
        c.close();

        // Define an array in which to store the results.
        Node[] resultArray = new Node[resultId.size()];

        // Convert the ArrayList to an array.
        for (int i = 0; i < resultId.size(); i++) {
            resultArray[i] = new Node();
            resultArray[i].id = resultId.get(i);
            resultArray[i].title = resultTitle.get(i);
            resultArray[i].body = resultBody.get(i);
        }

        // Return the array.
        return resultArray;
    }

    /**
     * Returns a full list of node titles.
     */
    public Node[] getNodeListData() {

        // Initialize a where string to contain a potential WHERE clause.
        String where = "";

        // If the WHERE clause properties were set...
        if (this.mLeftOperand != null && this.mRightOperand != null
                && this.mOperator != null) {
            // Define the WHERE clause.
            where = this.mLeftOperand + this.mOperator + this.mRightOperand;
        }

        // Define an array of columns to SELECT.
        String[] columns = new String[] {
                KEY_NODE_ROWID, KEY_NODE_TITLE, KEY_NODE_BODY
        };
        // Create a cursor to contain our query results;
        Cursor c;

        // If a WHERE clause was defined.
        if (where != "") {
            // Execute the query with the WHERE clause.
            c = mOurDatabase.query(DATABASE_TABLE_NODE, columns, where, null, null,
                    null, null);
        }
        else {
            // Execute the query.
            c = mOurDatabase.query(DATABASE_TABLE_NODE, columns, null, null, null,
                    null, null);
        }

        // Define an array per column, for dynamic results.
        ArrayList<Integer> resultId = new ArrayList<Integer>();
        ArrayList<String> resultTitle = new ArrayList<String>();
        ArrayList<String> resultBody = new ArrayList<String>();

        // Define indices for each column, for iteration.
        int id = c.getColumnIndex(KEY_NODE_ROWID);
        int title = c.getColumnIndex(KEY_NODE_TITLE);
        int body = c.getColumnIndex(KEY_NODE_BODY);

        // Loop through the results in the Cursor.
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            // Add each id to the id ArrayList.
            resultId.add(c.getInt(id));
            // Add each node title to the node title ArrayList.
            resultTitle.add(c.getString(title));
            // Add each node body to the node body ArrayList
            resultBody.add(c.getString(body));
        }

        // Close the cursor.
        c.close();

        // Define an array in which to store the results.
        Node[] resultArray = new Node[resultId.size()];

        // Convert the ArrayList to an array.
        for (int i = 0; i < resultId.size(); i++) {
            resultArray[i] = new Node();
            resultArray[i].id = resultId.get(i);
            resultArray[i].title = resultTitle.get(i);
            resultArray[i].body = resultBody.get(i);
        }

        // Return the array.
        return resultArray;
    }

    /**
     * Returns a single node row containing all column data.
     */
    public Node getNodeData(long l) throws SQLException {
        // Define an array of columns to SELECT.
        String[] columns = new String[] {
                KEY_NODE_ROWID, KEY_NODE_TITLE,
                KEY_NODE_BODY
        };
        // Define the cursor to contain our query results, and execute the
        // query.
        Cursor c = mOurDatabase.query(DATABASE_TABLE_NODE, columns, KEY_NODE_ROWID
                + "=" + l, null, null, null, null);

        // If there is a result...
        if (c != null) {
            // Position the iterator to the start of the result set.
            c.moveToFirst();

            // Define a node to store each column result.
            // Store the results in local variables.
            Node result = new Node();
            result.id = Integer.parseInt(c.getString(0));
            result.title = c.getString(1);
            result.body = c.getString(2);

            // Close the cursor.
            c.close();

            // Return the array.
            return result;
        }

        // No result? Return null.
        return null;
    }

    /**
     * Updates a node column.
     */
    public void updateNode(long nid, String column, int value) {

        // Create the new values
        ContentValues cv;

        // Store the arguments passed as the column and value in ContentValues.
        cv = new ContentValues();
        cv.put(column, value);

        // Execute a simple update query using the nid as the WHERE clause
        mOurDatabase.update(DATABASE_TABLE_NODE, cv, "_id=" + nid, null);
    }
    
    /**
     * Updates a category column.
     */
    public void updateCategory(long cid, String column, int value) {

        // Create the new values
        ContentValues cv;

        // Store the arguments passed as the column and value in ContentValues.
        cv = new ContentValues();
        cv.put(column, value);

        // Execute a simple update query using the nid as the WHERE clause
        mOurDatabase.update(DATABASE_TABLE_CATEGORIES, cv, "_id=" + cid, null);
    }

    /**
     * Sets conditions for a potential WHERE clause.
     */
    public void setConditions(String leftOperand, String rightOperand) {
        this.mLeftOperand = leftOperand;
        this.mRightOperand = rightOperand;
        this.mOperator = "=";
    }

    /**
     * Sets condition for a potential WHERE clause.
     */
    public void setConditions(String leftOperand, String rightOperand,
            String operator) {
        this.mLeftOperand = leftOperand;
        this.mRightOperand = rightOperand;
        this.mOperator = operator;
    }

    /**
     * Flushes any query builder properties.
     */
    public void flushQuery() {
        this.mLeftOperand = null;
        this.mRightOperand = null;
        this.mOperator = null;
    }

    /**
     * Implements onCreate().
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    /**
     * Implements onUpgrade().
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
