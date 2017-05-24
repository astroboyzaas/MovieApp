package com.example.android.movieapp;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;



public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewAdapterViewHolder>{

    private Cursor mCursor;

    @Override
    public ReviewAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(parent instanceof RecyclerView){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
            return  new ReviewAdapterViewHolder(view);
        }
        else {
            throw new RuntimeException("Not bound to RecyclerViewSelection");
        }
    }

    @Override
    public void onBindViewHolder(ReviewAdapterViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        holder.mAuthorTextView.setText(mCursor.getString(DetailFragment.COL_REVIEW_AUTHOR));
        holder.mContentTextView.setText(mCursor.getString(DetailFragment.COL_REVIEW_CONTENT));
    }

    // called from DetailFragment
    public void swapCursor(Cursor newCursor){
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (null == mCursor)
            return 0;
        return mCursor.getCount();
    }

    public class ReviewAdapterViewHolder extends RecyclerView.ViewHolder {

        private TextView mAuthorTextView;
        private TextView mContentTextView;

        public ReviewAdapterViewHolder(View itemView) {
            super(itemView);

            mAuthorTextView = (TextView) itemView.findViewById(R.id.author_textview);
            mContentTextView = (TextView) itemView.findViewById(R.id.content_textview);

        }
    }
}
