package com.example.android.movieapp;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
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
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    private Cursor mCursor;
    final private Context mContext;

    /////////////////////////////////////  VIEWHOLDER CLASS
    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder{

        public final ImageView mItemThumb;
        public MovieAdapterViewHolder(View view){
            super(view);
            mItemThumb=(ImageView)view.findViewById(R.id.item_thumb);
        }
    }
    /////////////////////////////////////  VIEWHOLDER CLASS


    public MovieAdapter(Context context) {
        mContext=context;
    }

    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if(viewGroup instanceof RecyclerView){
            View view =LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_movie,viewGroup,false);
            view.setFocusable(true);
            return new MovieAdapterViewHolder(view);
        }else{
            throw new RuntimeException("Not bound to RecyclerViewSelection");
        }
    }

    @Override
    public void onBindViewHolder(MovieAdapterViewHolder movieAdapterViewHolder, int position) {
        mCursor.moveToPosition(position);
        String posterPathUrl=mContext.getString(R.string.base_url)+mCursor.getString(MovieFragment.COL_POSTER_PATH);
        // Trigger the download of the URL asynchronously into the image view.
        Picasso.with(mContext) //
                .load(posterPathUrl)
                .into(movieAdapterViewHolder.mItemThumb);
    }

    // we need to make our own swapCursor method since RecyclerView.Adapter is general
    // and flexible for each kind of Adapter (CursorAdapter, ArrayAdapter, etc)
    public void swapCursor(Cursor newCursor){
        mCursor=newCursor;

        // Notify any registered observers that the data set has changed.
        // For our case keeps the behavior of notifying the observers that something has changed intact
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (null==mCursor)
            return 0;
        return mCursor.getCount();
    }

}
