package com.example.android.movieapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.example.android.movieapp.data.MovieContract;
import com.example.android.movieapp.sync.MovieAppSyncAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

//    private GridView mGridView;
    private RecyclerView mRecyclerView;
    private MovieAdapter mMovieAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    private static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH
            //DEPENDE DEL URI
    };

    public static final int COL_ID = 0;
    public static final int COL_MOVIE_ID = 1;
    public static final int COL_POSTER_PATH = 2;

    private static final int MOVIES_LOADER = 0;


    public MovieFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // inflate  recyclerview
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_movies);
        // tells the adapter that item sizes will not change
        mRecyclerView.setHasFixedSize(true);


        // Set the layout manager
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));

        // create and set the adapter
        mMovieAdapter = new MovieAdapter(getActivity(), new MovieAdapter.OnItemClickListener() {
            @Override
            public void onClick(RecyclerView.ViewHolder holder, long idMovie) {
                Intent intent =new Intent(getActivity(),DetailActivity.class)
                        .setData(MovieContract.MovieEntry.buildMovieUri(idMovie));
                startActivity(intent);
            }
        });
        mRecyclerView.setAdapter(mMovieAdapter);

        return rootView;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MovieAppSyncAdapter.syncImmediately(getActivity());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIES_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.moviefragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id=item.getItemId();
        String orderValue=getString(R.string.pref_order_default);
        switch (id){
            case R.id.popular_order:
                orderValue=getString(R.string.popular_order);
                break;
            case R.id.top_order:
                orderValue=getString(R.string.top_order);
                break;
            case R.id.favorites_order:
                orderValue=getString(R.string.favorites_order);
                break;
        }
        Utility.setMoviesOrder(getActivity(),orderValue);
        MovieAppSyncAdapter.syncImmediately(getActivity());
        getLoaderManager().restartLoader(MOVIES_LOADER, null, this);
        return true;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String orderSelected=Utility.getMoviesOrder(getContext());

        Uri moviesUri;
        String sortOrder;

        if(orderSelected.equals(getString(R.string.favorites_order))){
            sortOrder= MovieContract.FavoritesEntry.COLUMN_DATE + " ASC";
            moviesUri = MovieContract.FavoritesEntry.CONTENT_URI;
        }else{
            if(orderSelected.equals(getString(R.string.popular_order))){
                sortOrder = MovieContract.MovieEntry.COLUMN_POPULARITY + " DESC";
            }else{
                sortOrder = MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE + " DESC";
            }
            moviesUri = MovieContract.MovieEntry.buildSortOrderMovie(orderSelected);
        }
        /////////////////////////////////// VERIFICAR CON EL JSON DEL API SI SON LOS RESULTADOS CORRECTOS
        return new CursorLoader(getActivity(),
                moviesUri,
                MOVIE_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMovieAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieAdapter.swapCursor(null);
    }

}
