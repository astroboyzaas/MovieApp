package com.example.android.movieapp;

import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import com.example.android.movieapp.data.MovieContract;

import static android.R.attr.data;

public class ReviewsDialogFragment extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private Uri mReviewsUri;
    static final String REVIEWS_URI = "URI";
    private static final int REVIEWS_LOADER = 1;
    private static final String[] REVIEW_COLUMNS = {
            MovieContract.ReviewsEntry.TABLE_NAME + "." + MovieContract.ReviewsEntry._ID,
            MovieContract.ReviewsEntry.COLUMN_AUTHOR,
            MovieContract.ReviewsEntry.COLUMN_CONTENT
    };

    // REVIEW COLUMNS INDEX
    public static final int COL_ROW_REVIEW_ID = 0;
    public static final int COL_REVIEW_AUTHOR = 1;

 

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    public static final int COL_REVIEW_CONTENT = 2;

    private RecyclerView mRecyclerView;

    private ReviewAdapter mReviewAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.dialog_reviews,container,false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_review);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mReviewAdapter = new ReviewAdapter();
        mRecyclerView.setAdapter(mReviewAdapter);

        Bundle arguments = getArguments();
        if (arguments != null) {
            mReviewsUri = arguments.getParcelable(ReviewsDialogFragment.REVIEWS_URI);
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(REVIEWS_LOADER,null,this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == REVIEWS_LOADER && null != mReviewsUri) {
                return new CursorLoader(
                        getActivity(),
                        mReviewsUri,
                        REVIEW_COLUMNS,
                        null,
                        null,
                        null
                );
            }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.getCount()>0){
            mReviewAdapter.swapCursor(data);
        }else{
            Toast.makeText(getActivity(),"No hay Reviews",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
