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

import java.util.ArrayList;
import java.util.HashMap;

import net.globide.coloring_book_08.util.IabHelper;
import net.globide.coloring_book_08.util.IabResult;
import net.globide.coloring_book_08.util.Inventory;
import net.globide.coloring_book_08.util.Purchase;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Outputs a list of items that can be bought via the marketplace as well as
 * corresponding buy buttons.
 */

public class ShopActivity extends Activity implements OnClickListener {

    // Define whether or not this activity continues music from the
    // MusicManager.
    private boolean mContinueMusic = true;

    // Define our small db API object for database interaction.
    private NodeDatabase mDbNodeHelper = null;
    // Define a category object that will store the category data from the
    // database.
    private Category[] mCategoryData;

    // (arbitrary) request code for the purchase flow
    private static final int RC_REQUEST = 10001;

    // The helper object
    private IabHelper mHelper;

    // Boolean check for whether or not the user is currently online.
    // TODO: private boolean mIsOnline;

    // Does the user have the second coloring book?
    private HashMap<String, Boolean> mHas = new HashMap<String, Boolean>();
    
    // Declare hashmaps for the dynamically generated data.
    private HashMap<String, RelativeLayoutHolder> mRlHashMap = new HashMap<String, RelativeLayoutHolder>();
    private HashMap<String, TextViewHolder> mTvHashMap = new HashMap<String, TextViewHolder>();
    private HashMap<String, ImageButtonHolder> mIbHashMap = new HashMap<String, ImageButtonHolder>();
    
    // Define the necessary views.
    private LinearLayout mLlShopScrollView;
    private RelativeLayout mRlShopProgress;
    public ProgressBar pbShopQuery;
    private RelativeLayout mRlTemplate;
    private TextView mTvTemplate;
    private ImageButton mIbTemplate;
    
    // Tablet vs. phone boolean. Defaults to phone.
    public static boolean sIsTablet = false;
    public static boolean sIsSmall = false;
    public static boolean sIsNormal = false;
    public static boolean sIsLarge = false;
    public static boolean sIsExtraLarge = false;

    /**
     *  Holders for dynamically generated relative layouts.
     */
    private class RelativeLayoutHolder {

        // Tag so we can access each view when updating the UI.
        private String tag;
        private String sku;

    }

    /**
     *  Holders for dynamically generated textviews.
     */
    private class TextViewHolder {

        // Tag so we can access each view when updating the UI.
        private String tag;
        private String sku;

    }

    /**
     * Holders for dynamically generated image buttons.
     */
    private class ImageButtonHolder {

        // Tag so we can access each view when updating the UI.
        private String tag;
        private String sku;

    }

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
        setContentView(R.layout.activity_shop);
        
        // Determine whether or not the current device is a tablet.
        ShopActivity.sIsTablet = getResources().getBoolean(R.bool.isTablet);
        ShopActivity.sIsSmall = getResources().getBoolean(R.bool.isSmall);
        ShopActivity.sIsNormal = getResources().getBoolean(R.bool.isNormal);
        ShopActivity.sIsLarge = getResources().getBoolean(R.bool.isLarge);
        ShopActivity.sIsExtraLarge = getResources().getBoolean(R.bool.isExtraLarge);

        // Attach views to their corresponding resource ids.
        mLlShopScrollView = (LinearLayout) findViewById(R.id.llShopScrollView);
        mRlShopProgress = (RelativeLayout) findViewById(R.id.rlShopProgress);

        // Create the progressbar view
        pbShopQuery = new ProgressBar(this, null, android.R.attr.progressBarStyleLargeInverse);

        // Give the progressbar some parameters.
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);

        pbShopQuery.setLayoutParams(params);
        pbShopQuery.setIndeterminate(true);

        // Add the view to the layout.
        mRlShopProgress.addView(pbShopQuery);

        // load state data
        loadData();

        // Create all buttons based on the data in the categories table in the
        // db.
        createButtons();

        // Find out whether or not the user is currently online.
        // TODO: mIsOnline = isOnline();

        /*
         * base64EncodedPublicKey should be YOUR APPLICATION'S PUBLIC KEY (that
         * you got from the Google Play developer console). This is not your
         * developer public key, it's the *app-specific* public key.
         * Instead of just storing the entire literal string here embedded in
         * the program, construct the key at runtime from pieces or use bit
         * manipulation (for example, XOR with some other string) to hide the
         * actual key. The key itself is not secret information, but we don't
         * want to make it easy for an attacker to replace the public key with
         * one of their own and then fake messages from the server.
         */
        // TODO: Construct the key at runtime. Implement one of the techniques
        // described in the preceeding comment.

        // Create the helper, passing it our context and the public key to
        // verify signatures with
        mHelper = new IabHelper(this, base64EncodedPublicKey);

        // enable debug logging (for a production application, you should
        // set this to false).
        mHelper.enableDebugLogging(false);

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    return;
                }

                // Hooray, IAB is fully set up. Now, let's get an inventory of
                // stuff we own.
                mHelper.queryInventoryAsync(mGotInventoryListener);
            }
        });
    }
    
    /**
     * Listener that's called when we finish querying the items we own
     */
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (result.isFailure()) {
                return;
            }

            // Do we have the items.
            for (Category category : mCategoryData) {
                mHas.put(category.sku, inventory.hasPurchase(category.sku));
            }

            // If we can't find it via the inventory, we might not have an
            // internet connection, so check the database.

            // Ensure that the database contains the purchase information.
            checkDatabase();

            updateUi();
            setWaitScreen(false);
        }
    };

    /**
     * Callback for when a purchase is finished
     */
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (result.isFailure()) {
                // Oh noes!
                setWaitScreen(false);
                return;
            }

            for (Category category : mCategoryData) {
                if (purchase.getSku().equals(category.sku)) {
                    // bought a coloring book!
                    alert(category.category + " pack has been added! Access it via the main menu.");
                    mHas.put(category.sku, true);

                    // Ensure that the database contains the purchase
                    // information.
                    checkDatabase();

                    updateUi();
                    setWaitScreen(false);
                }
            }
        }
    };

    /**
     * Updates UI to reflect model
     */
    public void updateUi() {

        // update the shop buttons to reflect whether or not the user has bought
        // the item.
        for (String sku : mIbHashMap.keySet()) {
            
            // Store a reference to the current iteration's Image Holder.
            ImageButtonHolder ibHolder = mIbHashMap.get(sku);
            
            ImageButton currentImageButton = (ImageButton) mLlShopScrollView.findViewWithTag(ibHolder.tag);

            // The buy button should only work when this product has not yet
            // been purchased.
            currentImageButton.setBackgroundResource(mHas.get(sku) ? R.drawable.button_shop_disabled
                    : R.drawable.button_shop);

            currentImageButton.setEnabled(mHas.get(sku) ? false : true);
        }

        // Redraw the view.
        mLlShopScrollView.invalidate();
    }

    /**
     * Enables or disables the "please wait" screen.
     */
    void setWaitScreen(boolean set) {
        // Update the views.
        findViewById(R.id.svShop).setVisibility(set ? View.GONE : View.VISIBLE);
        findViewById(R.id.rlShopProgress).setVisibility(set ? View.VISIBLE : View.GONE);

        // Invalidate to redraw them
        findViewById(R.id.svShop).invalidate();
        findViewById(R.id.rlShopProgress).invalidate();
    }

    /**
     * Creates buttons for all coloring books in the categories table.
     */
    private void createButtons() {

        // Attach views to their corresponding resource ids.
        mRlTemplate = (RelativeLayout) findViewById(R.id.rlTemplate);
        mTvTemplate = (TextView) findViewById(R.id.tvTemplate);
        mIbTemplate = (ImageButton) findViewById(R.id.ibTemplate);

        // Retrieve the layout parameters for each view.
        LinearLayout.LayoutParams rlTemplateParams = (LinearLayout.LayoutParams) mRlTemplate
                .getLayoutParams();
        RelativeLayout.LayoutParams tvTemplateParams = (RelativeLayout.LayoutParams) mTvTemplate
                .getLayoutParams();
        RelativeLayout.LayoutParams ibTemplateParams = (RelativeLayout.LayoutParams) mIbTemplate
                .getLayoutParams();

        // Iterate through each category row.
        for (Category category : mCategoryData) {
            
            /*
             * RelativeLayout
             */
            // Create a new relative layout and add it to the main ScrollView.
            RelativeLayout newRelativeLayout = new RelativeLayout(this);
            newRelativeLayout.setLayoutParams(rlTemplateParams);
            
            // Set view styles
            if (ShopActivity.sIsTablet) {
                if (ShopActivity.sIsSmall) {
                    newRelativeLayout.setPadding(0, 12, 0, 12);
                }
                else if (ShopActivity.sIsNormal) {
                    newRelativeLayout.setPadding(0, 15, 0, 15);
                }
                else if (ShopActivity.sIsLarge) {
                    newRelativeLayout.setPadding(0, 18, 0, 18);
                }
                else if (ShopActivity.sIsExtraLarge) {
                    newRelativeLayout.setPadding(0, 20, 0, 20);
                }
            } else {
                newRelativeLayout.setPadding(0, 8, 0, 8);
            }

            // Set tag as the viewtype _ sku
            newRelativeLayout.setTag("RelativeLayout_" + category.sku);
            
            // Store this data in the corresponding holder hash map
            RelativeLayoutHolder rlHolder = new RelativeLayoutHolder();
            rlHolder.sku = category.sku;
            rlHolder.tag = "RelativeLayout_" + category.sku;
            mRlHashMap.put(category.sku, rlHolder);

            mLlShopScrollView.addView(newRelativeLayout);
            
            /*
             * TextView
             */

            // Create a new textview and add it to the relative layout.
            TextView newTextView = new TextView(this);
            newTextView.setLayoutParams(tvTemplateParams);
            newTextView.setText(category.category + " Pack");
            newTextView.setTextColor(Color.WHITE);
            
            // Set view styles
            if (ShopActivity.sIsTablet) {
                if (ShopActivity.sIsSmall) {
                    newTextView.setTextSize(24);
                }
                else if (ShopActivity.sIsNormal) {
                    newTextView.setTextSize(30);
                }
                else if (ShopActivity.sIsLarge) {
                    newTextView.setTextSize(36);
                }
                else if (ShopActivity.sIsExtraLarge) {
                    newTextView.setTextSize(40);
                }
            } else {
                newTextView.setTextSize(16);
            }

            // Set tag as the viewtype _ sku
            newTextView.setTag("TextView_" + category.sku);
            
            // Store this data in the corresponding holder hash map
            TextViewHolder tvHolder = new TextViewHolder();
            tvHolder.sku = category.sku;
            tvHolder.tag = "TextView_" + category.sku;
            mTvHashMap.put(category.sku, tvHolder);

            newRelativeLayout.addView(newTextView);
            
            /*
             * ImageButton.
             */

            // Create a new imagebutton and add it to the relative layout.
            ImageButton newImageButton = new ImageButton(this);
            newImageButton.setLayoutParams(ibTemplateParams);
            newImageButton.setOnClickListener(this);

            // Set tag as the viewtype _ sku
            newImageButton.setTag("ImageButton_" + category.sku);
            
            // Store this data in the corresponding holder hash map
            ImageButtonHolder ibHolder = new ImageButtonHolder();
            ibHolder.sku = category.sku;
            ibHolder.tag = "ImageButton_" + category.sku;
            mIbHashMap.put(category.sku, ibHolder);

            newRelativeLayout.addView(newImageButton);
        }

        // Remove the template views from the layout.
        mLlShopScrollView.removeView(mRlTemplate);
        
        // Redraw the views.
        mLlShopScrollView.invalidate();
    }

    /**
     * Checks whether or not the user currently has internet access.
     */
    //public boolean isOnline() {
     //   ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

     //   NetworkInfo netInfo = cm.getActiveNetworkInfo();

     //   if (netInfo != null && netInfo.isConnected()) {
     //       return true;
     //   }

    //   return false;
    //}

    /**
     * Renders a message to the screen for user confirmation.
     */
    private void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        bld.create().show();
    }

    /**
     * Checks to see if the database contains the purchase information.
     */
    private void checkDatabase() {
        // Check to see if the corresponding row exists in the database
        // and is enabled. If not, we need to enable it.
        for (Category category : mCategoryData) {

            // If the user doesn't have it in the data base but has it in
            // the inventory, the product hasn't yet been locally registered
            // so register it.
            if (category.isAvailable == 0) {
                if (mHas.get(category.sku)) {
                    // Database check!

                    // Create our database access object.
                    mDbNodeHelper = new NodeDatabase(this);

                    // Call the create method right just in case the user
                    // has
                    // never run the app before. If a database does not
                    // exist,
                    // the prepopulated one will be copied from the assets
                    // folder. Else, a connection is established.
                    mDbNodeHelper.createDatabase();
                    // Enable the coloring book in the database.
                    mDbNodeHelper.updateCategory(category.id, "isAvailable", 1);
                    // Update the local cache for consistency
                    category.isAvailable = 1;

                    // Flush the buffer.
                    mDbNodeHelper.flushQuery();

                    // This activity no longer needs the connection, so
                    // close
                    // it.
                    mDbNodeHelper.close();
                    
                    // A quick toast to confirm updated content in cases where a new purchase has just occurred or a previous purchase has been reinstated.
                    // TODO: Decouple both cases and display an alert dialog for each case so its more obvious.
                    Toast.makeText(this, "Your content has been updated.", Toast.LENGTH_LONG).show();
                }
            }
            // Else the product has been locally registered, so just set the
            // local boolean to match correspondingly.
            else if (category.isAvailable == 1) {
                mHas.put(category.sku, true);
            }
        }
    }

    /**
     * Loads current category status from the database.
     */
    private void loadData() {
        // Database check!

        // Create our database access object.
        mDbNodeHelper = new NodeDatabase(this);

        // Call the create method right just in case the user has never run the
        // app before. If a database does not exist, the prepopulated one will
        // be copied from the assets folder. Else, a connection is established.
        mDbNodeHelper.createDatabase();

        // Query the database for all purchased categories.

        // Set a conditional buffer. Internally, the orderby is set to _id ASC
        // (NodeDatabase.java).
        mDbNodeHelper.setConditions("sku", "'000_default'", "!=");
        // Execute the query.
        mCategoryData = mDbNodeHelper.getCategoryListData();
        // Flush the buffer.
        mDbNodeHelper.flushQuery();

        // This activity no longer needs the connection, so close it.
        mDbNodeHelper.close();
    }

    /**
     * Implements onActivityResult();
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        }
        else {
            //Log.d(TAG, "onActivityResult handled by IABUtil.");
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
     * Implements onDestroyed().
     */
    @Override
    public void onDestroy() {
        // very important:
        if (mHelper != null) {
            mHelper.dispose();
        }
        mHelper = null;

        super.onDestroy();
    }

    /**
     * Implements onClick().
     */
    @Override
    public void onClick(View view) {
        
        // Iterate through the views.
        // update the shop buttons to reflect whether or not the user has bought
        // the item.
        for (String sku : mIbHashMap.keySet()) {
            
            // Store a reference to the current iteration's Image Holder.
            ImageButtonHolder ibHolder = mIbHashMap.get(sku);

            // If the clicked view tag matches this iteration's view tag, launch the purchase flow for this item.
            if (view.getTag().equals(ibHolder.tag)) {
                setWaitScreen(true);
                mHelper.launchPurchaseFlow(this, ibHolder.sku, RC_REQUEST,
                        mPurchaseFinishedListener);
            }
        }
    }
}
