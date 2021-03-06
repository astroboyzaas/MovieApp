package com.example.android.movieapp;

import android.content.ContentValues;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.movieapp.data.MovieContract;
import com.example.android.movieapp.sync.MovieAppSyncAdapter;
import com.google.android.youtube.player.YouTubeIntents;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;


public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    String LOG_TAG = DetailFragment.class.getSimpleName();
    static final String DETAIL_URI = "URI";
    static final String FAVORITE = "FAVORITE";
    private Uri mDetailUri;
    private Uri mReviewsUri;
    private Uri mVideoUri;
    private boolean mIsFavorite;

    private static final int DETAIL_LOADER = 0;
    private static final int REVIEWS_LOADER = 1;
    private static final int VIDEO_LOADER = 2;

    private static final String[] DETAIL_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieEntry.COLUMN_POPULARITY,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_BACKDROP_PATH,
            MovieContract.MovieEntry.COLUMN_MOVIE_ID
    };

    private static final String[] REVIEW_COLUMNS = {
            MovieContract.ReviewsEntry.TABLE_NAME + "." + MovieContract.ReviewsEntry._ID,
            MovieContract.ReviewsEntry.COLUMN_AUTHOR,
            MovieContract.ReviewsEntry.COLUMN_CONTENT
    };

    private static final String[] VIDEO_COLUMNS = {
            MovieContract.VideosEntry.TABLE_NAME + "." + MovieContract.VideosEntry._ID,
            MovieContract.VideosEntry.TABLE_NAME + "." + MovieContract.VideosEntry.COLUMN_VIDEO_KEY
    };

    // DETAIL COLUMNS INDEX
    public static final int COL_ROW_MOVIE_ID = 0;
    public static final int COL_ORIGINAL_TITLE = 1;
    public static final int COL_POSTER_PATH = 2;
    public static final int COL_RELEASE_DATE = 3;
    public static final int COL_VOTE_AVERAGE = 4;
    public static final int COL_POPULARITY = 5;
    public static final int COL_OVERVIEW = 6;
    public static final int COL_BACKDROP_PATH = 7;
    public static final int COL_MOVIE_ID = 8;

    // REVIEW COLUMNS INDEX
    public static final int COL_ROW_REVIEW_ID = 0;
    public static final int COL_REVIEW_AUTHOR = 1;
    public static final int COL_REVIEW_CONTENT = 2;

    // VIDEO COLUMNS INDEX
    public static final int COL_ROW_VIDEO_ID = 0;
    public static final int COL_VIDEO_KEY = 1;

    private TextView mTitleView;
    private TextView mReleaseDate;
    private TextView mOverview;
    private TextView mVoteAverage;
    private RatingBar mRatingBar;
    private TextView mPopularity;
    private ImageView mImgToolbar;
    private ImageView mPlayButton;
    private FloatingActionButton mFavoriteButton;
    //private Button mReviewsButton;

    private RecyclerView mReviewRecyclerView;
    private ReviewAdapter mReviewAdapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mDetailUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
            mIsFavorite = arguments.getBoolean(DetailFragment.FAVORITE);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mImgToolbar= (ImageView) container.findViewById(R.id.imgToolbar);
        mPlayButton= (ImageView) container.findViewById(R.id.play_imageview);
        mFavoriteButton= (FloatingActionButton) container.findViewById(R.id.btnFab);

        if (mIsFavorite) mFavoriteButton.setImageResource(R.mipmap.ic_star);
        else mFavoriteButton.setImageResource(R.mipmap.ic_add_star);

        mTitleView = (TextView) rootView.findViewById(R.id.detail_title_textview);
        mReleaseDate = (TextView) rootView.findViewById(R.id.detail_release_date_textview);
        mOverview = (TextView) rootView.findViewById(R.id.detail_overview_textview);
        mPopularity = (TextView) rootView.findViewById(R.id.popularity_textview);
        mVoteAverage = (TextView) rootView.findViewById(R.id.vote_average_texview);
        mRatingBar = (RatingBar) rootView.findViewById(R.id.movie_ratingbar);
        //mReviewsButton = (Button) rootView.findViewById(R.id.reviews_button);

        mReviewRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_review);
        mReviewRecyclerView.setHasFixedSize(true);
        mReviewRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mReviewAdapter = new ReviewAdapter();
        mReviewRecyclerView.setAdapter(mReviewAdapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == DETAIL_LOADER) {
            if (null != mDetailUri) {
                return new CursorLoader(
                        getActivity(),
                        mDetailUri,
                        DETAIL_COLUMNS,
                        null,
                        null,
                        null
                );
            }
        } else if (id == REVIEWS_LOADER) {
            if (null != mReviewsUri) {
                return new CursorLoader(
                        getActivity(),
                        mReviewsUri,
                        REVIEW_COLUMNS,
                        null,
                        null,
                        null
                );
            }
        } else if (id == VIDEO_LOADER) {
            if (null != mVideoUri) {
                return new CursorLoader(
                        getActivity(),
                        mVideoUri,
                        VIDEO_COLUMNS,
                        null,
                        null,
                        null
                );
            }
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int loaderId = loader.getId();
        if (loaderId == DETAIL_LOADER) {
            if (data != null && data.moveToFirst()) {

                String backdropPathUrl = getString(R.string.base_url_backdrop) + data.getString(COL_BACKDROP_PATH);

                Picasso.with(getActivity())
                        .load(backdropPathUrl)
                        .into(mImgToolbar);


                mTitleView.setText(data.getString(COL_ORIGINAL_TITLE));

                mReleaseDate.setText(Utility.getFriendlyDateString(data.getString(COL_RELEASE_DATE)));

                double voteAverage = data.getDouble(COL_VOTE_AVERAGE);
                String voteAverageStr = String.format("%.1f", voteAverage);
                mVoteAverage.setText(voteAverageStr + "/10");
                float rating = ((float)(voteAverage*0.5));
                mRatingBar.setRating(rating);

                mOverview.setText(data.getString(COL_OVERVIEW));

                double popularity = data.getDouble(COL_POPULARITY);
                String popularityStr = String.format("%.1f", popularity);
                mPopularity.setText("Views: " + popularityStr);

                final int idMovie = data.getInt(COL_MOVIE_ID);

                mFavoriteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (!mIsFavorite) {
                            ContentValues favoriteValues = new ContentValues();
                            String todayString = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                            favoriteValues.put(MovieContract.FavoritesEntry.COLUMN_MOVIE_KEY, idMovie);
                            favoriteValues.put(MovieContract.FavoritesEntry.COLUMN_DATE, todayString);
                            MovieAppSyncAdapter.insertFavorite(getActivity(), favoriteValues);
                            Toast.makeText(getActivity(), "Agregado a Favoritos", Toast.LENGTH_SHORT).show();
                            mFavoriteButton.setImageResource(R.mipmap.ic_star);
                        }else {
                            String movie_id = String.valueOf(idMovie);
                            MovieAppSyncAdapter.deleteFavorite(getActivity(), movie_id);
                            Toast.makeText(getActivity(), "Quitado de Favoritos", Toast.LENGTH_SHORT).show();
                            mFavoriteButton.setImageResource(R.mipmap.ic_add_star);
                        }

                        mIsFavorite = !mIsFavorite;
                    }
                });

                // loading video
                mVideoUri = MovieContract.VideosEntry.buildVideoMovieUri(idMovie);
                getLoaderManager().initLoader(VIDEO_LOADER, null, this);

                // loading reviews
                mReviewsUri = MovieContract.ReviewsEntry.buildReviewMovieUri(idMovie);
                getLoaderManager().initLoader(REVIEWS_LOADER, null, this);

                // TODO: 18/05/2017  VERIFICAR CUANDO NO HAY REVIEWS QUE NO QUEDE UN ESPACIO EN BLANCO 
            }
        } else if (loaderId == REVIEWS_LOADER) {
            if(data.getCount()>0){
                mReviewAdapter.swapCursor(data);
            }
        }else if (loaderId == VIDEO_LOADER) {
            if (data != null && data.moveToFirst()) {
            final String videoKey = data.getString(COL_VIDEO_KEY);
            mPlayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Intent intent = null;
                        if (YouTubeIntents.canResolvePlayVideoIntent(getActivity())){
                            intent = YouTubeIntents.createPlayVideoIntentWithOptions(getActivity(), videoKey, true, false);

                        }else{
                            intent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse( getActivity().getString(R.string.base_youtube_url) + videoKey));
                        }
                        startActivity(intent);

                    } catch(Exception e)
                    {
                        Log.e(LOG_TAG,e.getMessage());
                    }

                }
            });
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
