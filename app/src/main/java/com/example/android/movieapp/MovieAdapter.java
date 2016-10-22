package com.example.android.movieapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.movieapp.data.MovieContract;
import com.example.android.movieapp.sync.MovieAppSyncAdapter;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Manuel on 07/09/2016.
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    private Cursor mCursor;
    final private Context mContext;
    private OnItemClickListener mItemClickListener;
    String LOG_TAG = MovieAdapter.class.getSimpleName();

    public MovieAdapter(Context context, OnItemClickListener itemClickListener) {
        mContext = context;
        mItemClickListener = itemClickListener;
    }

    /////////////////////////////////////////////CALLBACK /////////////
    public interface OnItemClickListener {
        void onClick(RecyclerView.ViewHolder holder, long idMovie);
    }
    /////////////////////////////////////////////CALLBACK /////////////

    /////////////////////////////////////  VIEWHOLDER CLASS
    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        public final ImageView mItemThumbView;
        public final ImageView mYearImageView;
        public final TextView mYearTextView;
        public final ImageView mCategoryImageView;
        public final TextView mCategoryTextView;
        public final ImageButton mImageButtonView;

        public MovieAdapterViewHolder(View view) {
            super(view);
            mItemThumbView = (ImageView) view.findViewById(R.id.item_thumb);

            mYearImageView = (ImageView) view.findViewById(R.id.release_year_icon);
            mYearTextView = (TextView) view.findViewById(R.id.release_year_textview);

            mCategoryImageView = (ImageView) view.findViewById(R.id.category_icon);
            mCategoryTextView = (TextView) view.findViewById(R.id.category_textview);

            mImageButtonView = (ImageButton) view.findViewById(R.id.favorite_imagebutton);
            mImageButtonView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int adapterPosition = getAdapterPosition();
                    mCursor.moveToPosition(adapterPosition);
                    if (mCursor.isNull(MovieFragment.COL_MOVIE_KEY)) {
                        mImageButtonView.setBackgroundResource(R.mipmap.ic_star);
                        ContentValues favoriteValues = new ContentValues();
                        int movie_id = mCursor.getInt(MovieFragment.COL_MOVIE_ID);
                        String todayString = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                        favoriteValues.put(MovieContract.FavoritesEntry.COLUMN_MOVIE_KEY, movie_id);
                        favoriteValues.put(MovieContract.FavoritesEntry.COLUMN_DATE, todayString);
                        MovieAppSyncAdapter.insertFavorite(mContext, favoriteValues);
                        Toast.makeText(mContext, "Agregado a Favoritos", Toast.LENGTH_SHORT).show();
                    } else {
                        String orderSelected = Utility.getMoviesOrder(mContext);
                        if(!orderSelected.equals(mContext.getString(R.string.favorites_order))){
                            mImageButtonView.setBackgroundResource(R.mipmap.ic_add_star);
                        }
                        String movie_id = String.valueOf(mCursor.getInt(MovieFragment.COL_MOVIE_ID));
                        MovieAppSyncAdapter.deleteFavorite(mContext, movie_id);
                        Toast.makeText(mContext, "Quitado de Favoritos", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);

            mItemClickListener.onClick(this, mCursor.getLong(MovieFragment.COL_MOVIE_ID));
        }
    }
    /////////////////////////////////////  VIEWHOLDER CLASS


    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (viewGroup instanceof RecyclerView) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_movie, viewGroup, false);
            view.setFocusable(true);
            return new MovieAdapterViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerViewSelection");
        }
    }

    @Override
    public void onBindViewHolder(MovieAdapterViewHolder movieAdapterViewHolder, int position) {
        mCursor.moveToPosition(position);
        String posterPathUrl = mContext.getString(R.string.base_url) + mCursor.getString(MovieFragment.COL_POSTER_PATH);
        // Trigger the download of the URL asynchronously into the image view.
        Picasso.with(mContext) //
                .load(posterPathUrl)
                .into(movieAdapterViewHolder.mItemThumbView);

        String releaseYear = Utility.getFriendlyYearString(mCursor.getString(MovieFragment.COL_RELEASE_DATE));
        movieAdapterViewHolder.mYearTextView.setText(releaseYear);
        movieAdapterViewHolder.mYearImageView.setImageResource(R.mipmap.ic_calendar);

        String orderSelected = Utility.getMoviesOrder(mContext);

        if (orderSelected.equals(mContext.getString(R.string.popular_order))) { 
            double popularity = mCursor.getDouble(MovieFragment.COL_POPULARITY);
            String popularityStr = String.format("%.2f", popularity);
            movieAdapterViewHolder.mCategoryTextView.setText(popularityStr);
            movieAdapterViewHolder.mCategoryImageView.setImageResource(R.mipmap.ic_eye);
        } else
//        if (orderSelected.equals(mContext.getString(R.string.top_order)))
        {
            double voteAverage = mCursor.getDouble(MovieFragment.COL_VOTE_AVERAGE);
            String voteAverageStr = String.format("%.2f", voteAverage);
            movieAdapterViewHolder.mCategoryTextView.setText(voteAverageStr);
            movieAdapterViewHolder.mCategoryImageView.setImageResource(R.mipmap.ic_heart);
        }

        if(orderSelected.equals(mContext.getString(R.string.favorites_order))){
            movieAdapterViewHolder.mImageButtonView.setBackgroundResource(R.mipmap.ic_delete);
        }else
        if (mCursor.isNull(MovieFragment.COL_MOVIE_KEY)) {
            movieAdapterViewHolder.mImageButtonView.setBackgroundResource(R.mipmap.ic_add_star);
        } else {
            movieAdapterViewHolder.mImageButtonView.setBackgroundResource(R.mipmap.ic_star);
        }




    }

    // we need to make our own swapCursor method since RecyclerView.Adapter is general
    // and flexible for each kind of Adapter (CursorAdapter, ArrayAdapter, etc)
    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;

        // Notify any registered observers that the data set has changed.
        // For our case keeps the behavior of notifying the observers that something has changed intact
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (null == mCursor)
            return 0;
        return mCursor.getCount();
    }

}
