package com.derekmorrison.alexbooks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.derekmorrison.alexbooks.api.BookListAdapter;
import com.derekmorrison.alexbooks.data.AlexandriaContract;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    Drawer mDrawer;
    ActionBar mSupportActionBar;
    private AccountHeader mAccountHeader = null;

    private BookListAdapter mBookListAdapter;
    private ListView mBookListView;
    //private int mListposition = ListView.INVALID_POSITION;
    private final int LOADER_ID = 10;
    private String mSearchString = "";

    public static final String MESSAGE_EVENT = "MESSAGE_EVENT";
    public static final String MESSAGE_KEY = "MESSAGE_EXTRA";
    private BroadcastReceiver mMessageReciever;

    public static boolean IS_TABLET = false;
    Fragment mFragmentBookDetails = null;

    private static final String listState = "listViewState";
    private Parcelable mListState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IS_TABLET = isTablet();

        // load the appropriate layout
        if (IS_TABLET){
            setContentView(R.layout.activity_main_tablet);
        } else {
            setContentView(R.layout.activity_main);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSupportActionBar = getSupportActionBar();

        mMessageReciever = new MessageReciever();
        IntentFilter filter = new IntentFilter(MESSAGE_EVENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReciever,filter);

        FloatingActionButton fab_add_book = (FloatingActionButton) findViewById(R.id.fab_add_book);
        fab_add_book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, BookAdd.class);
                MainActivity.this.startActivity(intent);
            }
        });

        FloatingActionButton fab_search_book = (FloatingActionButton) findViewById(R.id.fab_search_book);
        fab_search_book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSearchTerm();
                //Log.v(LOG_TAG, "FAB Click Search Term: " + mSearchString);
            }
        });

        final IProfile profile = new ProfileDrawerItem().withName(getString(R.string.drawer_header_text)).withIcon(R.drawable.alex_logo);

        // Create the AccountHeader for the Material Drawer
        // free image for the header from pixabay
        // https://pixabay.com/static/uploads/photo/2015/10/22/22/56/books-1002123_960_720.jpg
        mAccountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.books_on_shelf)
                .addProfiles(profile)
                .withSelectionListEnabled(false)
                .withSavedInstance(savedInstanceState)
                .build();

        // Create the drawer
        // https://github.com/mikepenz/MaterialDrawer
        mDrawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withHasStableIds(true)
                .withAccountHeader(mAccountHeader)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.menu_home).withIcon(R.drawable.ic_home_black_24dp).withDescription(R.string.sub_menu_home).withIdentifier(1).withSelectable(false),
                        new PrimaryDrawerItem().withName(R.string.menu_add_book).withIcon(R.drawable.ic_add_black_24dp).withDescription(R.string.sub_menu_add_book).withIdentifier(2).withSelectable(false),
                        new PrimaryDrawerItem().withName(R.string.menu_about).withIcon(R.drawable.ic_info_black_24dp).withDescription("").withIdentifier(3).withSelectable(false)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem != null) {
                            Intent intent = null;
                            if (drawerItem.getIdentifier() == 1) {
                                mSearchString = "";
                                restartLoader();
                            } else if (drawerItem.getIdentifier() == 2) {
                                intent = new Intent(MainActivity.this, BookAdd.class);
                            } else if (drawerItem.getIdentifier() == 3) {
                                intent = new Intent(MainActivity.this, AlexAbout.class);
                            }

                            // if there is an intent then launch to new activity
                            if (intent != null) {
                                MainActivity.this.startActivity(intent);
                            }
                        }

                        return false;
                    }
                })
                .withSavedInstance(savedInstanceState)
                .withShowDrawerOnFirstLaunch(false)
                .build();


        mBookListAdapter = new BookListAdapter(this, null, 0);
        mBookListView = (ListView) findViewById(R.id.listOfBooks);
        mBookListView.setAdapter(mBookListAdapter);

        mBookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //Log.v(LOG_TAG, "ListView - onItemClick position: " + position);
                Cursor cursor = mBookListAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    String selectedItem = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry._ID));
                    MainActivity.this.onItemSelected(selectedItem);
                }
            }
        });

        // warn the user if there is no network connection
        if (false == Utility.isNetworkAvailable(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), R.string.no_internet_connection, Toast.LENGTH_LONG).show();
        }

        restartLoader();
    }

    // copied the basic idea from MaterialDrawer by Mike Penz
    @Override
    public void onBackPressed() {
        //handle the back press :D close the drawer first
        if (mDrawer != null && mDrawer.isDrawerOpen()) {
            mDrawer.closeDrawer();
        } else {
            // when using a tablet close the details panel first. If it's not there then exit the app via call to super
            if (IS_TABLET) {
                if (findViewById(R.id.right_container) != null && mFragmentBookDetails != null){
                    getSupportFragmentManager().beginTransaction()
                            .remove(mFragmentBookDetails)
                            .commit();

                    mFragmentBookDetails = null;
                }

            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public void onStart(){
        mSearchString = "";
        restartLoader();
        super.onStart();
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (savedInstanceState != null) {

            // Restore last state for the listView - this data gets used after the Loader is finished
            // during "onLoadFinished"  That's when the ListView will be populated
            mListState = savedInstanceState.getParcelable(listState);
            //Log.v(LOG_TAG, "onPostCreate: getting the saved state of the ListView ");
        }
    }

    private boolean isTablet() {
        return (getApplicationContext().getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    // found a good solution to the save / restore issue here
    // http://stackoverflow.com/questions/3014089/maintain-save-restore-scroll-position-when-returning-to-a-listview/5688490#5688490
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save position of ListView
        mListState = mBookListView.onSaveInstanceState();
        outState.putParcelable(listState, mListState);
    }

    // this provides a way for the BookService to send messages to the UI
    private class MessageReciever extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getStringExtra(MESSAGE_KEY)!=null){
                Toast.makeText(MainActivity.this, intent.getStringExtra(MESSAGE_KEY), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReciever);
        super.onDestroy();
    }

    // pop up a dialog box so the user can enter a search term
    // Based on: http://stackoverflow.com/questions/10903754/input-text-dialog-android
    private void getSearchTerm() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_search_title));

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mSearchString = input.getText().toString();
                //Log.v(LOG_TAG, "getSearchTerm  Search Term: " + mSearchString);
                restartLoader();
            }
        });
        builder.setNegativeButton(getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    // this method launches the Book Details Activity
    public void onItemSelected(String ean) {

        // either launch the BookDetails activity via an Intent or show the details fragment in the 'right_container'
        Bundle args = new Bundle();
        args.putString(BookDetailsFragment.EAN_KEY, ean);

        if (IS_TABLET){
            if(findViewById(R.id.right_container) != null){
                mFragmentBookDetails = new BookDetailsFragment();
                mFragmentBookDetails.setArguments(args);
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.right_container, mFragmentBookDetails)
                    .commit();
            }
        } else {
            Intent intent = new Intent(MainActivity.this, BookDetails.class);
            intent.putExtras(args);
            MainActivity.this.startActivity(intent);
        }
    }

    private void restartLoader(){
        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        //Log.v(LOG_TAG, "onCreateLoader: mSearchString " + mSearchString);

        // if the user has entered a search term then use a different query
        if(mSearchString.length() > 0){

            // look for the search term in both the TITLE and the SUBTITLE of the book
            final String selection = AlexandriaContract.BookEntry.TITLE +" LIKE ? OR " + AlexandriaContract.BookEntry.SUBTITLE + " LIKE ? ";
            String searchTerm = "%" + mSearchString + "%";

            // reset the search string now that it has been used
            mSearchString = "";

            return new CursorLoader(
                    this,
                    AlexandriaContract.BookEntry.CONTENT_URI,
                    null,
                    selection,
                    new String[]{searchTerm, searchTerm},
                    null
            );
        }

        return new CursorLoader(
                this,
                AlexandriaContract.BookEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        mBookListAdapter.changeCursor(data);

        // there is no point in continuing if there is no data
        if (null == data) { return; }

        TextView emptyMessage = (TextView) findViewById(R.id.no_books_message);

        //Log.v(LOG_TAG, "onLoadFinished: cursor count " + data.getCount());

        if (data.getCount() == 0){
            emptyMessage.setVisibility(View.VISIBLE);
        } else {
            emptyMessage.setVisibility(View.GONE);
            String msg = getString(R.string.books_showing) + data.getCount();
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
        }

        //Log.v(LOG_TAG, "onLoadFinished: now the ListView 'state' will be restored ");

        // try to restore the listview to the way it was before the data update
        if (mListState != null){
            mBookListView.onRestoreInstanceState(mListState);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mBookListAdapter.changeCursor(null);
    }

}
