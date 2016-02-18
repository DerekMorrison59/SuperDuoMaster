package com.derekmorrison.alexbooks;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.derekmorrison.alexbooks.data.AlexandriaContract;
import com.derekmorrison.alexbooks.services.BookService;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;

public class BookAdd extends AppCompatActivity  implements LoaderManager.LoaderCallbacks<Cursor>{

    private final String LOG_TAG = BookAdd.class.getSimpleName();
    private EditText mIsbnEditText;
    private final int LOADER_ID = 1;

    private final int ISBN10 = 10;
    private final int ISBN13 = 13;
    private final String ISBN_PREFIX = "978";

    private final String EAN_CONTENT="eanContent";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_add);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (null != actionBar) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // set up the FAB to allow the user to trigger the scanner function
        FloatingActionButton fab_search_book = (FloatingActionButton) findViewById(R.id.fab_scan_book);
        fab_search_book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanNow();
            }
        });

        mIsbnEditText = (EditText) findViewById(R.id.ean);

        if (null != savedInstanceState) {
            mIsbnEditText.setText(savedInstanceState.getString(EAN_CONTENT));
            mIsbnEditText.setHint("");
        }

        mIsbnEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //no need
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //no need
            }

            @Override
            public void afterTextChanged(Editable s) {
                String isbnString = s.toString();

                //catch isbn10 numbers
                if (isbnString.length() == ISBN10 && !isbnString.startsWith(ISBN_PREFIX)) {
                    isbnString = ISBN_PREFIX + isbnString;
                }

                if (isbnString.length() < ISBN13) {
                    clearFields();
                    return;
                }

                //Once we have an ISBN, start the book service via intent to fetch book data from Google
                if (Utility.isNetworkAvailable(getApplicationContext())) {
                    Intent bookIntent = new Intent(getApplicationContext(), BookService.class);
                    bookIntent.putExtra(BookService.EAN, isbnString);
                    bookIntent.setAction(BookService.FETCH_BOOK);
                    startService(bookIntent);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.no_internet_connection, Toast.LENGTH_LONG).show();
                }

                BookAdd.this.restartLoader();
            }
        });

        // the save button just clears current ISBN - the book has already been saved to the database
        findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            mIsbnEditText.setText("");
            }
        });

        // the delete button sends an Intent to the BookService requesting that the specified ISBN be deleted
        findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getApplicationContext(), BookService.class);
                bookIntent.putExtra(BookService.EAN, mIsbnEditText.getText().toString());
                bookIntent.setAction(BookService.DELETE_BOOK);
                getApplicationContext().startService(bookIntent);
                mIsbnEditText.setText("");
            }});

        // warn the user if there is no network connection
        if (false == Utility.isNetworkAvailable(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), R.string.no_internet_connection, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if( mIsbnEditText != null) {
            outState.putString(EAN_CONTENT, mIsbnEditText.getText().toString());
            //Log.v(LOG_TAG, "onSaveInstanceState: mIsbnEditText: " + mIsbnEditText.getText().toString());
        }
    }

    private void restartLoader(){
        getSupportLoaderManager().restartLoader(LOADER_ID, null, BookAdd.this);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // the user must have entered at least 10 digits
        if(mIsbnEditText.getText().length() < ISBN10){
            return null;
        }
        String eanStr = mIsbnEditText.getText().toString();

        if(eanStr.length()==ISBN10 && !eanStr.startsWith(ISBN_PREFIX)){
            eanStr = ISBN_PREFIX + eanStr;
        }
        return new CursorLoader(
                this,
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(eanStr)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        ((TextView) findViewById(R.id.bookTitle)).setText(bookTitle);

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        ((TextView) findViewById(R.id.bookSubTitle)).setText(bookSubTitle);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));

        // Bug Fix: Check to make sure the 'authors' field returned from google is not null
        if (null != authors) {
            String[] authorsArr = authors.split(",");
            ((TextView) findViewById(R.id.authors)).setLines(authorsArr.length);
            ((TextView) findViewById(R.id.authors)).setText(authors.replace(",", "\n"));
        } else {
            ((TextView) findViewById(R.id.authors)).setText(R.string.no_author_provided);
        }

        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));

        // use Picasso to handle the book cover images
        if(Patterns.WEB_URL.matcher(imgUrl).matches()){
            ImageView iView = (ImageView) findViewById(R.id.bookCover);
            Picasso
                    .with(getApplicationContext())
                    .load(imgUrl)
                    .error(R.drawable.alex_logo)
                    .into(iView);

            iView.setVisibility(View.VISIBLE);
        }

        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        ((TextView) findViewById(R.id.categories)).setText(categories);

        findViewById(R.id.save_button).setVisibility(View.VISIBLE);
        findViewById(R.id.delete_button).setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        clearFields();
    }

    private void clearFields(){
        ((TextView) findViewById(R.id.bookTitle)).setText("");
        ((TextView) findViewById(R.id.bookSubTitle)).setText("");
        ((TextView) findViewById(R.id.authors)).setText("");
        ((TextView) findViewById(R.id.categories)).setText("");
        findViewById(R.id.bookCover).setVisibility(View.INVISIBLE);
        findViewById(R.id.save_button).setVisibility(View.INVISIBLE);
        findViewById(R.id.delete_button).setVisibility(View.INVISIBLE);
    }

    // the ZXing library has been integrated into this project for convenience
    // https://github.com/journeyapps/zxing-android-embedded/
    private void scanNow() {

        // use IntentIntegrator to invoke the ZXing scan library
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
        integrator.setPrompt(getString(R.string.scan_instruction));
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
    }

    // this is the method that gets called by the ZXing library to provide the results of the scan
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        String newISBN = "";

        switch (requestCode) {
            case IntentIntegrator.REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {

                    // Parsing bar code reader result
                    IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

                    // get the ISBN from the ZXing scan results
                    newISBN = result.getContents();

                    // put the ISBN into the EditText for normal processing
                    mIsbnEditText = (EditText) findViewById(R.id.ean);
                    mIsbnEditText.setText(newISBN);
                }
                break;
        }
    }
}
