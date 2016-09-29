package com.example.android.movieapp.sync;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.example.android.movieapp.MovieFragment;
import com.example.android.movieapp.R;
import com.squareup.picasso.Picasso;

/**
 * Created by Manuel on 07/09/2016.
 */
public class MovieAdapter extends CursorAdapter {

    public MovieAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return view;
    }
    // inflar el elemento de la lista y retornar como view
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
    // llenar cada dato del elemento de la lista (view) con cada elemento del cursor
        ImageView thumbnail=(ImageView)view.findViewById(R.id.item_thumb);
        String posterPathUrl=context.getString(R.string.base_url)+cursor.getString(MovieFragment.COL_POSTER_PATH);
        // Trigger the download of the URL asynchronously into the image view.
        Picasso.with(context) //
                .load(posterPathUrl)
                .into(thumbnail);
    }
}
