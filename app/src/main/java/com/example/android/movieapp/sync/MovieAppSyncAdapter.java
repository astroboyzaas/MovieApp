package com.example.android.movieapp.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.example.android.movieapp.BuildConfig;
import com.example.android.movieapp.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Manuel on 08/06/2016.
 */
public class MovieAppSyncAdapter extends AbstractThreadedSyncAdapter {

    String LOG_TAG=MovieAppSyncAdapter.class.getSimpleName();
    public MovieAppSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        HttpURLConnection urlConnection=null;
        BufferedReader reader=null;
        String moviesJsonStr=null;

        try {
            final String MOVIES_BASE_URL=
                    "http://api.themoviedb.org/3/movie";
            final String option="popular";
            final String API_KEY_PARAM="api_key";

            Uri builtUri= Uri.parse(MOVIES_BASE_URL).buildUpon()
                    .appendPath(option)
                    .appendQueryParameter(API_KEY_PARAM,BuildConfig.THE_MOVIE_DB_API_KEY)
                    .build();

            URL url= new URL(builtUri.toString());
            urlConnection= (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream=urlConnection.getInputStream();
            StringBuffer buffer= new StringBuffer();
            if(inputStream==null){
                return;
            }
            reader=new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while((line=reader.readLine())!=null){
                buffer.append(line+"\n");
            }
            moviesJsonStr=buffer.toString();
            Log.v(LOG_TAG,moviesJsonStr);

        }catch (IOException e){
            Log.e(LOG_TAG,"Error",e);
        }finally {
            if(urlConnection!=null){
                urlConnection.disconnect();
            }
            if(reader!=null){
                try {
                    reader.close();
                }catch (final IOException e){
                    Log.e(LOG_TAG,"Error closing stream",e);
                }
            }
        }
    }

    public static void syncInmmediatly(Context context){
        Bundle bundle= new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED,true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL,true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority),bundle);
    }

    public static Account getSyncAccount(Context context){
        // account type in form of domain
        String account_type = context.getString(R.string.sync_account_type);
        // account name
        String account = context.getString(R.string.app_name);

        AccountManager accountManager=
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        Account newAccount = new Account(account,account_type);

        // when an account is created always should have a password
        if(null== accountManager.getPassword(newAccount)){
            // adding account with password ""
            if(!accountManager.addAccountExplicitly(newAccount,"",null)){
                return null;
            }
        }
        return newAccount;
    }
}
