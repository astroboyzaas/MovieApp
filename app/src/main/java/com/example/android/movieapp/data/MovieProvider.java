package com.example.android.movieapp.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.example.android.movieapp.sync.MovieAppSyncAdapter;

import static com.example.android.movieapp.data.MovieContract.PATH_VIDEOS;

/**
 * Created by Manuel on 07/06/2016.
 */
public class MovieProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mOpenHelper;
    String LOG_TAG = MovieProvider.class.getSimpleName();

    static final int MOVIES = 100; // it can be popular or top rated depending sort order
    static final int DETAIL_MOVIE = 101;
    static final int VIDEOS_MOVIE = 102;
    static final int REVIEWS_MOVIE = 103;
    static final int FAVORITES = 200;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        // trying from the most specific to general

        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/#", DETAIL_MOVIE);

        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/#/"+MovieContract.PATH_VIDEOS, VIDEOS_MOVIE);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/#/"+MovieContract.PATH_REVIEWS, REVIEWS_MOVIE);

        matcher.addURI(authority, MovieContract.PATH_MOVIE +"/"+MovieContract.PATH_FAVORITES, FAVORITES);

        /////////// SE PUEDE PRESCINDIR DEL /*  Y QUE SOLO SEA PATH_MOVIE, PORQUE AL FINAL SOLO CUENTA EL SORT ORDER
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/*", MOVIES);

        return matcher;
    }

    private static final SQLiteQueryBuilder sFavoriteMoviesQueryBuilder;

    static {
        sFavoriteMoviesQueryBuilder = new SQLiteQueryBuilder();
        // favorites INNER JOIN movie ON favorites.movie_id=movie.id
        sFavoriteMoviesQueryBuilder.setTables(
                MovieContract.FavoritesEntry.TABLE_NAME + " INNER JOIN " +
                        MovieContract.MovieEntry.TABLE_NAME +
                        " ON " + MovieContract.FavoritesEntry.TABLE_NAME +
                        "." + MovieContract.FavoritesEntry.COLUMN_MOVIE_KEY +
                        "=" + MovieContract.MovieEntry.TABLE_NAME +
                        "." + MovieContract.MovieEntry.COLUMN_MOVIE_ID
        );
    }

    private static final SQLiteQueryBuilder sMoviesQueryBuilder;

    static {
        sMoviesQueryBuilder = new SQLiteQueryBuilder();
        // movie LEFT JOIN favorites ON movie.id=favorites.movie_id
        sMoviesQueryBuilder.setTables(
                MovieContract.MovieEntry.TABLE_NAME  + " LEFT JOIN " +
                        MovieContract.FavoritesEntry.TABLE_NAME +
                        " ON " + MovieContract.MovieEntry.TABLE_NAME +
                        "." + MovieContract.MovieEntry.COLUMN_MOVIE_ID +
                        "=" +  MovieContract.FavoritesEntry.TABLE_NAME +
                        "." + MovieContract.FavoritesEntry.COLUMN_MOVIE_KEY
        );
    }

    private static final SQLiteQueryBuilder sVideosMovieQueryBuilder;

    static {
        sVideosMovieQueryBuilder = new SQLiteQueryBuilder();
        // videos INNER JOIN movie ON videos.movie_id=movie.id
        sVideosMovieQueryBuilder.setTables(
                MovieContract.VideosEntry.TABLE_NAME + " INNER JOIN " +
                        MovieContract.MovieEntry.TABLE_NAME +
                        " ON " + MovieContract.VideosEntry.TABLE_NAME +
                        "." + MovieContract.VideosEntry.COLUMN_MOVIE_KEY +
                        "=" + MovieContract.MovieEntry.TABLE_NAME +
                        "." + MovieContract.MovieEntry.COLUMN_MOVIE_ID
        );
    }

    private static final SQLiteQueryBuilder sReviewsMovieQueryBuilder;

    static {
        sReviewsMovieQueryBuilder = new SQLiteQueryBuilder();
        // reviews INNER JOIN movie ON reviews.movie_id=movie.id
        sReviewsMovieQueryBuilder.setTables(
                MovieContract.ReviewsEntry.TABLE_NAME + " INNER JOIN " +
                        MovieContract.MovieEntry.TABLE_NAME +
                        " ON " + MovieContract.ReviewsEntry.TABLE_NAME +
                        "." + MovieContract.ReviewsEntry.COLUMN_MOVIE_KEY +
                        "=" + MovieContract.MovieEntry.TABLE_NAME +
                        "." + MovieContract.MovieEntry.COLUMN_MOVIE_ID
        );
    }





    // movie.id=?
    private static final String sIdSelection = MovieContract.MovieEntry.TABLE_NAME + "." +
            MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ? ";

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case MOVIES: {
//                // 1 table
//                retCursor = mOpenHelper.getReadableDatabase().query(
//                        MovieContract.MovieEntry.TABLE_NAME,
//                        projection,
//                        selection,
//                        selectionArgs,
//                        null,
//                        null,
//                        sortOrder+" LIMIT 20");
                // 2 joined tables
                retCursor=sMoviesQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                Log.d(LOG_TAG,"Nro de Registros: " + retCursor.getCount());
                break;
            }
            case DETAIL_MOVIE: {
                String[] selectionById=new String[]{MovieContract.MovieEntry.getMovieIdFromUri(uri)};
                // 1 table
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        sIdSelection,
                        selectionById,
                        null,
                        null,
                        sortOrder);// instead of sortOrder can be null
                break;
            }
            case FAVORITES: {
                // 2 joined tables
                retCursor=sFavoriteMoviesQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                Log.d(LOG_TAG,"Nro de Registros: " + retCursor.getCount());
                break;
            }
            case VIDEOS_MOVIE: {
                String[] selectionById=new String[]{MovieContract.MovieEntry.getMovieIdFromUri(uri)};
                // 2 joined tables
                retCursor=sVideosMovieQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        sIdSelection,
                        selectionById,
                        null,
                        null,
                        sortOrder);
                Log.d(LOG_TAG,"Nro de Registros: " + retCursor.getCount());
                break;
            }
            case REVIEWS_MOVIE: {
                String[] selectionById=new String[]{MovieContract.MovieEntry.getMovieIdFromUri(uri)};
                // 2 joined tables
                retCursor=sReviewsMovieQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        sIdSelection,
                        selectionById,
                        null,
                        null,
                        sortOrder);

                Log.d(LOG_TAG,"Nro de Registros: " + retCursor.getCount());
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Register the uri to watch for changes, so data in the cursor can be updated automatically.
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db=mOpenHelper.getWritableDatabase();
        Uri returnUri=null;
        long _id;
        switch (sUriMatcher.match(uri)){
            case MOVIES:{
                _id=db.insert(MovieContract.MovieEntry.TABLE_NAME,null,values);
                if(_id>0)
                    returnUri= MovieContract.MovieEntry.buildMovieUri(_id);
                break;
            }
            case FAVORITES:{
                _id=db.insert(MovieContract.FavoritesEntry.TABLE_NAME,null,values);
                if(_id>0)
                    returnUri= MovieContract.FavoritesEntry.buildFavoriteMovieUri(_id);
                break;
            }
            case VIDEOS_MOVIE:{
                _id=db.insert(MovieContract.VideosEntry.TABLE_NAME,null,values);
                if(_id>0)
                    returnUri= MovieContract.VideosEntry.buildVideoMovieUri(_id);
                break;
            }
            case REVIEWS_MOVIE:{
                _id=db.insert(MovieContract.ReviewsEntry.TABLE_NAME,null,values);
                if(_id>0)
                    returnUri= MovieContract.ReviewsEntry.buildReviewMovieUri(_id);
                break;
            }
            default: throw new UnsupportedOperationException("Unknown uri: "+uri);
        }
        if (_id<=0)
            // this kind of exception only for insert operations
            throw new android.database.SQLException("Failed to insert row into "+uri);
        // If this change affects other uris, notify the change
        getContext().getContentResolver().notifyChange(uri,null);
        return returnUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {

        final SQLiteDatabase db=mOpenHelper.getWritableDatabase();
        int returnCount=0;
        db.beginTransaction();

        try {
            long _id;
            for (ContentValues value: values) {

                switch (sUriMatcher.match(uri)) {
                    case MOVIES:
                        _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, value);
                        break;
                    case VIDEOS_MOVIE:
                        _id = db.insert(MovieContract.VideosEntry.TABLE_NAME, null, value);
                        break;
                    case REVIEWS_MOVIE:
                        _id = db.insert(MovieContract.ReviewsEntry.TABLE_NAME, null, value);
                        break;
                    default:
                        return super.bulkInsert(uri, values);
                }

                if (_id != -1) {
                    returnCount++;
                }

            }
            db.setTransactionSuccessful();

        }finally {
            db.endTransaction();
        }

        // If this change affects other uris, notify the change
        getContext().getContentResolver().notifyChange(uri,null);

        return returnCount;

        /**
        final SQLiteDatabase db=mOpenHelper.getWritableDatabase();
        int returnCount=0;
        switch(sUriMatcher.match(uri)){
            case MOVIES:
                db.beginTransaction();
                try{
                    for (ContentValues value: values){
                        long _id=db.insert(MovieContract.MovieEntry.TABLE_NAME,null,value);
                        if(_id!=-1){
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                }finally {
                    db.endTransaction();
                }
                // If this change affects other uris, notify the change
                getContext().getContentResolver().notifyChange(uri,null);
                return returnCount;
            case VIDEOS_MOVIE:
                db.beginTransaction();
                try{
                    for (ContentValues value: values){
                        long _id=db.insert(MovieContract.VideosEntry.TABLE_NAME,null,value);
                        if(_id!=-1){
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                }finally {
                    db.endTransaction();
                }
                // If this change affects other uris, notify the change
                getContext().getContentResolver().notifyChange(uri,null);
                return returnCount;
            case REVIEWS_MOVIE:
                db.beginTransaction();
                try{
                    for (ContentValues value: values){
                        long _id=db.insert(MovieContract.ReviewsEntry.TABLE_NAME,null,value);
                        if(_id!=-1){
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                }finally {
                    db.endTransaction();
                }
                // If this change affects other uris, notify the change
                getContext().getContentResolver().notifyChange(uri,null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }*/
    }

    // avoid deleting linked rows
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db=mOpenHelper.getWritableDatabase();
        int rowsDeleted;

        // To remove all rows and get a count, pass "1" as the whereClause.
        // see this on: https://developer.android.com/reference/android/database/sqlite/SQLiteDatabase.html#delete(java.lang.String, java.lang.String, java.lang.String[])
        if(null==selection) selection="1";

        switch (sUriMatcher.match(uri)){
            // for MOVIES table, deleting joined registers is handled in the delete's call
            case MOVIES:{
                rowsDeleted=db.delete(MovieContract.MovieEntry.TABLE_NAME,selection,selectionArgs);//new String[]{""}
                db.delete(MovieContract.VideosEntry.TABLE_NAME,"NOT EXISTS (SELECT 1 FROM movie WHERE videos.movie_id=movie.id)",null);
                db.delete(MovieContract.ReviewsEntry.TABLE_NAME,"NOT EXISTS (SELECT 1 FROM movie WHERE reviews.movie_id=movie.id)",null);
                break;
            }
            case FAVORITES:{
                rowsDeleted=db.delete(MovieContract.FavoritesEntry.TABLE_NAME,selection,selectionArgs);
                break;
            }
//            case VIDEOS_MOVIE:
//                rowsDeleted=db.delete(MovieContract.VideosEntry.TABLE_NAME,selection,selectionArgs);
//                break;
//            case REVIEWS_MOVIE:
//                rowsDeleted= db.delete(MovieContract.ReviewsEntry.TABLE_NAME,selection,selectionArgs);
//                break;
            default: throw new UnsupportedOperationException("Unknown uri "+uri);
        }

        if(rowsDeleted!=0){
            /// If this change affects other uris, notify the change
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
