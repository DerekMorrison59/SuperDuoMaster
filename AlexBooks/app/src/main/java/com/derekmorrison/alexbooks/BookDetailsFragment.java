package com.derekmorrison.alexbooks;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.derekmorrison.alexbooks.data.AlexandriaContract;
import com.derekmorrison.alexbooks.services.BookService;
import com.squareup.picasso.Picasso;

/**
 * Barcode Scanner icons from
 * http://www.iconarchive.com/show/plex-android-icons-by-cornmanthe3rd/barcode-scanner-icon.html
 */
public class BookDetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = BookDetailsFragment.class.getSimpleName();

    public static final String EAN_KEY = "EAN";
    private final int LOADER_ID = 10;
    private View rootView;
    private String eanString;
    private String bookTitle;
    private ShareActionProvider mShareActionProvider;
    private Context mContext;

    public BookDetailsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mContext = getContext();

        // the phone mode replaces the main Activity with the Book Details Activity
        // and passes the ISBN via the Intent Extras
        Bundle extras = getActivity().getIntent().getExtras();
        if (extras != null) {
            eanString = extras.getString(BookDetailsFragment.EAN_KEY);
            //Log.v(LOG_TAG, "INTENT -> onCreateView = book ISBN: " + eanString);
        }

        // the tablet mode places this fragment into the right-hand side of the screen
        // and passes the ISBN via the Arguments bundle
        Bundle arguments = getArguments();
        if (arguments != null) {
            eanString = arguments.getString(BookDetailsFragment.EAN_KEY);
            //Log.v(LOG_TAG, "ARGUMENTS -> onCreateView = book ISBN: " + eanString);
        }

        getLoaderManager().restartLoader(LOADER_ID, null, this);

        if (MainActivity.IS_TABLET) {
            rootView = inflater.inflate(R.layout.fragment_book_details_tablet, container, false);
        } else {
            rootView = inflater.inflate(R.layout.fragment_book_details, container, false);
        }

        rootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, eanString);
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);

                getActivity().onBackPressed();
            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        final String intentType = "text/plain";

        inflater.inflate(R.menu.menu_book_details, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // moved here to ensure that this code is executed - onLoadFinished was being called before this method
        // so the share intent was not being created
        if (null != mShareActionProvider) {

            //Log.v(LOG_TAG, "onCreateOptionsMenu = bookTitle: " + bookTitle);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            shareIntent.setType(intentType);
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + " " + bookTitle);
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (null == eanString || eanString.isEmpty()) return null;

        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(eanString)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {

        //Log.v(LOG_TAG, "onLoadFinished at the TOP: ");

        if (!data.moveToFirst()) {
            return;
        }

        bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        ((TextView) rootView.findViewById(R.id.fullBookTitle)).setText(bookTitle);

        // removed because there is seldom a subTitle and the blank space does not look good
        //String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        //((TextView) rootView.findViewById(R.id.fullBookSubTitle)).setText(bookSubTitle);

        String desc = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.DESC));
        ((TextView) rootView.findViewById(R.id.fullBookDesc)).setText(desc);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        String[] authorsArr = authors.split(",");
        ((TextView) rootView.findViewById(R.id.authors)).setLines(authorsArr.length);
        ((TextView) rootView.findViewById(R.id.authors)).setText(authors.replace(",","\n"));

        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if(Patterns.WEB_URL.matcher(imgUrl).matches()){
            ImageView iView = (ImageView) rootView.findViewById(R.id.fullBookCover);
            Picasso
                    .with(getContext())
                    .load(imgUrl)
                    .error(R.drawable.alex_logo)
                    .into(iView);

            iView.setVisibility(View.VISIBLE);
        }

        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        ((TextView) rootView.findViewById(R.id.categories)).setText(categories);

    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

        // clear all the text fields and the book cover
        ((TextView) rootView.findViewById(R.id.fullBookTitle)).setText("");
        ((TextView) rootView.findViewById(R.id.authors)).setText("");
        ((TextView) rootView.findViewById(R.id.categories)).setText("");
        ((TextView) rootView.findViewById(R.id.fullBookDesc)).setText("");

        ImageView iView = (ImageView) rootView.findViewById(R.id.fullBookCover);
        Picasso
                .with(getContext())
                .load(R.drawable.alex_logo)
                .error(R.drawable.alex_logo)
                .into(iView);
    }
}
