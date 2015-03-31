package com.geaden.android.gsana.app.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.geaden.android.gsana.app.R;
import com.geaden.android.gsana.app.Utility;
import com.geaden.android.gsana.app.models.AsanaStory;


/**
 * Stories for the task adapter.
 *
 * @author Gennady Denisov
 */
public class GsanaStoriesAdapater extends ArrayAdapter<AsanaStory> {
    private final String LOG_TAG = getClass().getSimpleName();

    private int mLayout;
    private Context mContext;

    public GsanaStoriesAdapater(Context context, int layout) {
        super(context, layout);
        mContext = context;
        mLayout = layout;
    }

    private class ViewHolder {
        private TextView mTaskStoryText;
        private TextView mTaskStoryInfo;

        public ViewHolder(View view) {
            mTaskStoryText = (TextView) view.findViewById(R.id.task_story_text);
            mTaskStoryInfo = (TextView) view.findViewById(R.id.task_story_info);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mLayout, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        AsanaStory asanaStory = getItem(position);
        if (null != asanaStory) {
            viewHolder.mTaskStoryText.setText(asanaStory.getText());
            viewHolder.mTaskStoryInfo.setText(asanaStory.getCreatedBy().getName() +
                " at " + Utility.dateReformat(asanaStory.getCreatedAt(), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                    "MMM d, yyyy HH:mm:ss"));
        }
        return convertView;
    }
}
