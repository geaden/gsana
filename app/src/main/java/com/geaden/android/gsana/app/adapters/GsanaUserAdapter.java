package com.geaden.android.gsana.app.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.geaden.android.gsana.app.LoadersColumns;
import com.geaden.android.gsana.app.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


/**
 * Exposes user info from database
 */
public class GsanaUserAdapter extends CursorAdapter{
    private final static String LOG_TAG = GsanaUserAdapter.class.getSimpleName();

    private class GsanaUserViewHolder {
        public final TextView userNameTextView;
        public final ImageView userPicImageView;

        public GsanaUserViewHolder(View view) {
            userNameTextView = (TextView) view.findViewById(R.id.left_drawer_user_name);
            userPicImageView = (ImageView) view.findViewById(R.id.left_drawer_user_pic);

        };
    }

    public GsanaUserAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        Log.v(LOG_TAG, "Gsana User Adapter");
        View view = LayoutInflater.from(context).inflate(R.layout.list_asana_task_item, parent, false);
        GsanaUserViewHolder viewHolder = new GsanaUserViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        GsanaUserViewHolder viewHolder = (GsanaUserViewHolder) view.getTag();
        viewHolder.userNameTextView.setText(cursor.getString(LoadersColumns.COL_USER_NAME));
        FetchUserPicTask userPicTask = new FetchUserPicTask();
        userPicTask.execute(cursor.getString(LoadersColumns.COL_USER_PHOTO_60), viewHolder);
    }

    private class FetchUserPicTask extends AsyncTask<Object, Void, Bitmap> {
        private GsanaUserViewHolder mUserViewHolder;

        @Override
        protected Bitmap doInBackground(Object... params) {
            mUserViewHolder = (GsanaUserViewHolder) params[1];
            Bitmap userPic = null;
            try {
                URL url = new URL((String) params[0]);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                userPic = BitmapFactory.decodeStream(input);
            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
            return userPic;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mUserViewHolder.userPicImageView.setImageBitmap(bitmap);
        }
    }
}
