package com.example.android.movieapp.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Manuel on 07/06/2016.
 */
public class MovieContract {

    public static final String CONTENT_AUTHORITY= "com.example.android.movieapp";
    public static final Uri BASE_CONTENT_URI= Uri.parse("content://"+CONTENT_AUTHORITY);
    public static final String PATH_MOVIE="movie";
    public static final String PATH_FAVORITES="favorites";

    public static final class MovieEntry implements BaseColumns{
        /**
         *  CONTENT_URI="content://com.example.android.movieapp/movie"
         */
        public static final Uri CONTENT_URI =BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MOVIE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE+"/"+CONTENT_AUTHORITY+"/"+PATH_MOVIE;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE+"/"+CONTENT_AUTHORITY+"/"+PATH_MOVIE;

        public static final String TABLE_NAME="movie";

        public static final String COLUMN_ORIGINAL_TITLE="original_title";
        public static final String COLUMN_POSTER_PATH="poster_path";
        public static final String COLUMN_OVERVIEW="overview";
        public static final String COLUMN_VOTE_AVERAGE="vote_average";
        public static final String COLUMN_RELEASE_DATE="release_date";
        public static final String COLUMN_POPULARITY="popularity";
        public static final String COLUMN_MOVIE_ID="id";
        // COLUMN_DATE doesn't come with the JSON, and it's for registration date in the DB
        public static final String COLUMN_DATE="date";

        // differences:
        // - ContentUris.withAppendedId(CONTENT_URI, id) ----> appends the given id to the end of the path CONTENT_URI
        // - CONTENT_URI.buildUpon().appendPath(criteria).build()----> Encodes the given segment(criteria) and appends it to the path CONTENT_URI

        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
        /** criteria could be "popular", "top_rated"
         *  content://com.example.android.movieapp/movie/popular
         *  content://com.example.android.movieapp/movie/top_rated
         */
        public static Uri buildSortOrderMovie(String criteria){
            return CONTENT_URI.buildUpon().appendPath(criteria).build();
        }
        // example of path:  com.example.android.movieapp/movie/52212  then it'd return 52212
        public static String getMovieIdFromUri(Uri uri){
            return uri.getPathSegments().get(1);
        }

    }

    public static final class FavoritesEntry implements BaseColumns{
        /**
         *  CONTENT_URI="content://com.example.android.movieapp/movie/favorites"
         */
        public static final Uri CONTENT_URI =BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MOVIE).appendPath(PATH_FAVORITES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE+"/"+CONTENT_AUTHORITY+"/"+PATH_MOVIE+"/"+PATH_FAVORITES;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE+"/"+CONTENT_AUTHORITY+"/"+PATH_MOVIE+"/"+PATH_FAVORITES;

        public static final String TABLE_NAME="favorites";
        public static final String COLUMN_MOVIE_KEY="movie_id";
        public static final String COLUMN_DATE="date";

        public static Uri buildFavoriteMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }

}
