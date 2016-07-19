package com.example.android.movieapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.movieapp.data.MovieContract.MovieEntry;
import com.example.android.movieapp.data.MovieContract.FavoritesEntry;

/**
 * Created by Manuel on 07/06/2016.
 *
 * Define table and column names for the movie database.
 *
 * AGREGAR UN CAMPO A LA TABLA MOVIE: POPULARITY para ordenar en la SQLite, y tal vez otros campos
 * neecsarios para otras cosas.
 *
 * Al momento de hacer la consulta en el onCreatedLoader()
 * - Para obtener por POPULAR se ordenara en DESC  por el campo POPULARITY,
 * - Para obtener por TOP_RATED se ordenara en DESC por el campo VOTE_AVERAGE
 * - Ambas consultas con un COUNT de 20 (verificar los JSON)
 *
 * Esto porque hay peliculas que son populares pero no top_rated y viceversa, y tambien puede darse
 * el caso de que sea popular y top_rated a la vez por eso no se esta poniendo ninguna UNIQUE o restriccion para admitirla.
 *
 * SE ELIMINARAN los registros de peliculas que sean anteriores a HOY.
 *
 * Se agregara un tabla FAVORITE que tendra como fk el ID de tabla MOVIE, y se tendrá el campo de fecha y una relacion
 * UNIQUE entre el pk y fk

 */
public class MovieDbHelper
     extends SQLiteOpenHelper
{
    public static final int DATABASE_VERSION=1;
    static final String DATABASE_NAME="movie.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        // don't confuse _ID="_id" with MOVIE_ID="id" which comes from JSON
        final String SQL_CREATE_MOVIE_TABLE="CREATE TABLE "+ MovieEntry.TABLE_NAME+" ("+
                MovieEntry._ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
                MovieEntry.COLUMN_ORIGINAL_TITLE+" TEXT NOT NULL, "+
                MovieEntry.COLUMN_POSTER_PATH+" TEXT NOT NULL, "+
                MovieEntry.COLUMN_OVERVIEW+" TEXT NOT NULL, "+
                MovieEntry.COLUMN_VOTE_AVERAGE+" REAL NOT NULL, "+
                MovieEntry.COLUMN_RELEASE_DATE+" TEXT NOT NULL, "+
                MovieEntry.COLUMN_POPULARITY+" REAL NOT NULL, "+
                MovieEntry.COLUMN_MOVIE_ID+" INTEGER NOT NULL UNIQUE ON CONFLICT REPLACE"+
                " );";

        // COLUMN_MOVIE_ID and COLUMN_MOVIE_KEY are the same but both table ain't linked with fk since it's easier
        // updating MOVIE TABLE with simple INSERT and avoids REFERENCE PROBLEMS on FAVORITE TABLE
        final String SQL_CREATE_FAVORITES_TABLE="CREATE TABLE "+ FavoritesEntry.TABLE_NAME+" ("+
                FavoritesEntry._ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
                FavoritesEntry.COLUMN_MOVIE_KEY+" INTEGER NOT NULL UNIQUE ON CONFLICT IGNORE,"+
                FavoritesEntry.COLUMN_DATE+" TEXT NOT NULL"+
                " );";


        db.execSQL(SQL_CREATE_MOVIE_TABLE);
        db.execSQL(SQL_CREATE_FAVORITES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+FavoritesEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+MovieEntry.TABLE_NAME);
        onCreate(db);
    }
}
