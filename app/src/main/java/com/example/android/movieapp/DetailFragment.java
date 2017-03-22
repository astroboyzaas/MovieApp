package com.example.android.movieapp;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.movieapp.data.MovieContract;
import com.squareup.picasso.Picasso;

/**
 * Created by Manuel on 12/10/2016.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    static final String DETAIL_URI = "URI";
    private Uri mUri;

    private static final int DETAIL_LOADER = 0;
    private static final String[] DETAIL_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieEntry.COLUMN_POPULARITY,
            MovieContract.MovieEntry.COLUMN_OVERVIEW
    };

    public static final int COL_MOVIE_ID = 0;
    public static final int COL_ORIGINAL_TITLE = 1;
    public static final int COL_POSTER_PATH = 2;
    public static final int COL_RELEASE_DATE = 3;
    public static final int COL_VOTE_AVERAGE = 4;
    public static final int COL_POPULARITY = 5;
    public static final int COL_OVERVIEW = 6;

    private TextView mTitleView;
    private ImageView mThumbnail;
    private TextView mReleaseDate;
    private TextView mVoteAverage;
    private TextView mPopularity;
    private TextView mOverview;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }
        //// TODO: 09/03/2017 ADD BACKDROP PATH FOR TRAILER IMAGE
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mTitleView = (TextView) rootView.findViewById(R.id.detail_title_textview);
        mThumbnail = (ImageView) rootView.findViewById(R.id.detail_thumbnail_imageview);
        mReleaseDate = (TextView) rootView.findViewById(R.id.detail_release_date_textview);
        mVoteAverage = (TextView) rootView.findViewById(R.id.detail_vote_average_textview);
        mPopularity = (TextView) rootView.findViewById(R.id.detail_popularity_textview);
        mOverview = (TextView) rootView.findViewById(R.id.detail_overview_textview);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {

            mTitleView.setText(data.getString(COL_ORIGINAL_TITLE));

            String posterPathUrl =getString(R.string.base_url)+ data.getString(COL_POSTER_PATH);
            Picasso.with(getActivity())
                    .load(posterPathUrl)
                    .into(mThumbnail);
            mReleaseDate.setText(data.getString(COL_RELEASE_DATE));

            double voteAverage=data.getDouble(COL_VOTE_AVERAGE);
            mVoteAverage.setText(String.format("%2f",voteAverage));

            double popularity=data.getDouble(COL_POPULARITY);
            mPopularity.setText(String.format("%2f",popularity));

            mOverview.setText(data.getString(COL_OVERVIEW));

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
