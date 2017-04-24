package com.example.android.movieapp.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.example.android.movieapp.BuildConfig;
import com.example.android.movieapp.MovieFragment;
import com.example.android.movieapp.R;
import com.example.android.movieapp.Utility;
import com.example.android.movieapp.data.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import static android.media.CamcorderProfile.get;
import static android.os.Build.VERSION_CODES.BASE;

/**
 * Created by Manuel on 08/06/2016.
 */
public class MovieAppSyncAdapter extends AbstractThreadedSyncAdapter {

    String LOG_TAG = MovieAppSyncAdapter.class.getSimpleName();

    public MovieAppSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        final String option = extras.getString(MovieFragment.ARG_TAB_NAME);
        getData(option, -1);
    }

    private void getData(String option, int movieId){
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String jsonStr;

        // http://api.themoviedb.org/3/movie/top_rated?api_key=1b3d52c94b0b86238bb624987007b0de
        // https://api.themoviedb.org/3/movie/244786/reviews?api_key=1b3d52c94b0b86238bb624987007b0de
        try {
            final String MOVIES_BASE_URL =
                    "http://api.themoviedb.org/3/movie";

            final String API_KEY_PARAM = "api_key";
            final String LANGUAGE_PARAM = "language";
            final String LANGUAGE_VALUE = "es";

            Uri builtUri;

            if (movieId!=-1) // for reviews and videos
                builtUri =Uri.parse(MOVIES_BASE_URL).buildUpon()
                            .appendPath(String.valueOf(movieId))
                            .appendPath(option)
                            .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                            .build();
            else
                builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                            .appendPath(option)
                            .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
//                          .appendQueryParameter(LANGUAGE_PARAM, LANGUAGE_VALUE)
                            .build();

            URL url = new URL(builtUri.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }
            jsonStr = buffer.toString();
            Log.v(LOG_TAG, jsonStr);

            if(option.equals(getContext().getString(R.string.popular_order)) ||
                    option.equals(getContext().getString(R.string.top_order)))
                    getMoviesDataFromJson(jsonStr, option);
            else
                if (option.equals(getContext().getString(R.string.videos_order)))
                    getVideosDataFromJson(jsonStr, movieId);
                else
                    getReviewsDataFromJson(jsonStr, movieId);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error", e);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    private void getMoviesDataFromJson(String moviesJsonStr, String option) throws JSONException {

        final String TMDB_LIST = "results";
        final String TMDB_ORIGINAL_TITLE = "original_title";
        final String TMDB_POSTER_PATH = "poster_path";
        final String TMDB_OVERVIEW = "overview";
        final String TMDB_VOTE_AVERAGE = "vote_average";
        final String TMDB_RELEASE_DATE = "release_date";
        final String TMDB_POPULARITY = "popularity";
        final String TMDB_MOVIE_ID = "id";
        final String TMDB_BACKDROP_PATH = "backdrop_path";


        
        try {

            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray movieArray = moviesJson.getJSONArray(TMDB_LIST);
            // movieArray.length() returns number of its elements
            // could be ArrayList instead Vector. Try later.
            Vector<ContentValues> cvVector = new Vector<ContentValues>(movieArray.length());

            String originalTitle;
            String posterPath;
            String overview;
            String releaseDate;
            double voteAverage;
            double popularity;
            int movieId;
            String todayString=new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String backdropPath;

            for (int i = 0; i < movieArray.length(); i++) {

                JSONObject movieItem = movieArray.getJSONObject(i);
                originalTitle = movieItem.getString(TMDB_ORIGINAL_TITLE);
                posterPath = movieItem.getString(TMDB_POSTER_PATH);
                overview = movieItem.getString(TMDB_OVERVIEW);
                releaseDate = movieItem.getString(TMDB_RELEASE_DATE);
                voteAverage = movieItem.getDouble(TMDB_VOTE_AVERAGE);
                popularity = movieItem.getDouble(TMDB_POPULARITY);
                movieId = movieItem.getInt(TMDB_MOVIE_ID);
                backdropPath = movieItem.getString(TMDB_BACKDROP_PATH);

                ContentValues movieValues = new ContentValues();

                movieValues.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE, originalTitle);
                movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, posterPath);
                movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, overview);
                movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, releaseDate);
                movieValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, voteAverage);
                movieValues.put(MovieContract.MovieEntry.COLUMN_POPULARITY, popularity);
                movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movieId);

                // Once we have movieId we use it for getting Videos and Reviews
                getData(getContext().getString(R.string.videos_order), movieId);
                getData(getContext().getString(R.string.reviews_order), movieId);

                // new SimpleDateFormat("yyyy-MM-dd").format(new Date()) return today in string format
                movieValues.put(MovieContract.MovieEntry.COLUMN_DATE, todayString);
                movieValues.put(MovieContract.MovieEntry.COLUMN_BACKDROP_PATH, backdropPath);

                cvVector.add(movieValues);

            }

            int inserted=0;
            // cvVector.size() returns number of its elements
            if(cvVector.size()>0){
                ContentValues[] cvArray=new ContentValues[cvVector.size()];
                cvVector.toArray(cvArray);

                //String orderSelected=Utility.getMoviesOrder(getContext());

                // CAUTION ---> insert operation will replace old movies with same id of new ones
                inserted=getContext().getContentResolver().bulkInsert(MovieContract.MovieEntry.buildSortOrderMovie(option),cvArray);

                // delete old data before today and not favorite movies, so we don't build up an endless history
                // movie.date<? AND (NOT EXISTS (SELECT 1 FROM favorites WHERE favorites.movie_id=movie.id))      // could be left join too, but not exists is faster
                String selection= MovieContract.MovieEntry.TABLE_NAME+"."+ MovieContract.MovieEntry.COLUMN_DATE+"<?"+
                        " AND (NOT EXISTS (SELECT 1 FROM " +MovieContract.FavoritesEntry.TABLE_NAME +
                        " WHERE "+MovieContract.FavoritesEntry.TABLE_NAME+"."+ MovieContract.FavoritesEntry.COLUMN_MOVIE_KEY+
                        "="+MovieContract.MovieEntry.TABLE_NAME+"."+ MovieContract.MovieEntry.COLUMN_MOVIE_ID+"))";

                getContext().getContentResolver().delete(MovieContract.MovieEntry.buildSortOrderMovie(option), selection, new String[]{todayString});

                // If a movie is deleted also should delete their videos and reviews
//                getContext().getContentResolver().delete(MovieContract.VideosEntry.buildVideoMovieUri(), selection, new String[]{todayString});
//                getContext().getContentResolver().delete(MovieContract.MovieEntry.buildSortOrderMovie(option), selection, new String[]{todayString});

            }
            Log.d(LOG_TAG, "Sync Complete. " + inserted + " Movies Inserted");
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

    }

    private void getVideosDataFromJson(String videosJsonStr, int movieId) {
        final String TMDB_LIST = "results";
        final String TMDB_VIDEO_KEY = "key";
        final String TMDB_VIDEO_NAME = "name";

        try {

            JSONObject videosJson = new JSONObject(videosJsonStr);
            JSONArray videoArray = videosJson.getJSONArray(TMDB_LIST);
            Vector<ContentValues> cvVector = new Vector<ContentValues>(videoArray.length());

            String videoKey;
            String videoName;

            for (int i = 0; i < videoArray.length(); i++) {

                JSONObject videoItem = videoArray.getJSONObject(i);
                videoKey = videoItem.getString(TMDB_VIDEO_KEY);
                videoName = videoItem.getString(TMDB_VIDEO_NAME);

                // Agregar 2 tablas de Videos y Reviews por cada Pelicula
                ContentValues videoValues = new ContentValues();

                videoValues.put(MovieContract.VideosEntry.COLUMN_MOVIE_KEY, movieId);
                videoValues.put(MovieContract.VideosEntry.COLUMN_VIDEO_KEY, videoKey);
                videoValues.put(MovieContract.VideosEntry.COLUMN_VIDEO_NAME, videoName);

                cvVector.add(videoValues);

            }

            int inserted=0;
            // cvVector.size() returns number of its elements
            if(cvVector.size()>0){
                ContentValues[] cvArray=new ContentValues[cvVector.size()];
                cvVector.toArray(cvArray);

                //String orderSelected=Utility.getMoviesOrder(getContext());

                inserted=getContext().getContentResolver().bulkInsert(MovieContract.VideosEntry.buildVideoMovieUri(movieId),cvArray);

            }
            Log.d(LOG_TAG, "Sync Complete. " + inserted + " Videos Inserted");
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private void getReviewsDataFromJson(String reviewsJsonStr, int movieId) {
        final String TMDB_LIST = "results";
        final String TMDB_REVIEW_ID = "id";
        final String TMDB_AUTHOR = "author";
        final String TMDB_CONTENT = "content";

        try {

            JSONObject reviewsJson = new JSONObject(reviewsJsonStr);
            JSONArray reviewArray = reviewsJson.getJSONArray(TMDB_LIST);
            Vector<ContentValues> cvVector = new Vector<ContentValues>(reviewArray.length());

            String reviewId;
            String author;
            String content;

            for (int i = 0; i < reviewArray.length(); i++) {

                JSONObject reviewItem = reviewArray.getJSONObject(i);
                reviewId = reviewItem.getString(TMDB_REVIEW_ID);
                author = reviewItem.getString(TMDB_AUTHOR);
                content = reviewItem.getString(TMDB_CONTENT);


                // Agregar 2 tablas de Videos y Reviews por cada Pelicula
                ContentValues reviewValues = new ContentValues();

                reviewValues.put(MovieContract.ReviewsEntry.COLUMN_MOVIE_KEY, movieId);
                reviewValues.put(MovieContract.ReviewsEntry.COLUMN_REVIEW_ID, reviewId);
                reviewValues.put(MovieContract.ReviewsEntry.COLUMN_AUTHOR, author);
                reviewValues.put(MovieContract.ReviewsEntry.COLUMN_CONTENT, content);

                cvVector.add(reviewValues);

            }

            int inserted=0;
            // cvVector.size() returns number of its elements
            if(cvVector.size()>0){
                ContentValues[] cvArray=new ContentValues[cvVector.size()];
                cvVector.toArray(cvArray);

                //String orderSelected=Utility.getMoviesOrder(getContext());

                inserted=getContext().getContentResolver().bulkInsert(MovieContract.ReviewsEntry.buildReviewMovieUri(movieId),cvArray);

            }
            Log.d(LOG_TAG, "Sync Complete. " + inserted + " Reviews Inserted");
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    public static void syncImmediately(Context context, String tabName) {
        Bundle bundle = new Bundle();
        // SYNC_EXTRAS_EXPEDITED for give it priority and SYNC_EXTRAS_MANUAL for avoiding automatic sync
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putString(MovieFragment.ARG_TAB_NAME,tabName);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    public static void insertFavorite(Context context, ContentValues cv){
        int inserted=0;
        context.getContentResolver().insert(MovieContract.FavoritesEntry.CONTENT_URI,cv);
    }

    public static void deleteFavorite(Context context, String movieKey){
        int deleted=0;
        String selection= MovieContract.FavoritesEntry.COLUMN_MOVIE_KEY+"=?";
        deleted = context.getContentResolver().delete(MovieContract.FavoritesEntry.CONTENT_URI,selection,new String[]{movieKey});
    }

    public static Account getSyncAccount(Context context) {
        // account type in form of domain
        String account_type = context.getString(R.string.sync_account_type);
        // account name
        String account = context.getString(R.string.app_name);

        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        Account newAccount = new Account(account, account_type);

        // when an account is created always should have a password
        if (null == accountManager.getPassword(newAccount)) {
            // adding account with password "", if there was an error return null
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
        }
        return newAccount;
    }
}
