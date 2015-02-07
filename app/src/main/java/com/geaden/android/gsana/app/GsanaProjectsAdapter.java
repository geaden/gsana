package com.geaden.android.gsana.app;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

/**
 * Adapter for mapping projects to the view.
 */
public class GsanaProjectsAdapter extends CursorAdapter {
    /**
     * Cache of the children views for an Asana project list item
     */
    public static class ViewHolder {
        public final View projectColorView;
        public final TextView projectNameView;

        public ViewHolder(View view) {
            projectColorView = view.findViewById(R.id.list_item_asana_project_color);
            projectNameView = (TextView) view.findViewById(R.id.list_item_asana_project_name);
        }
    }

    public GsanaProjectsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_asana_project_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    // Asana Color
    class AsanaProjectColor {
        static final String DARK_PINK = "dark-pink";
        static final String DARK_GREEN = "dark-green";
        static final String DARK_BLUE = "dark-blue";
        static final String DARK_RED = "dark-red";
        static final String DARK_TEAL = "dark-teal";
        static final String DARK_BROWN ="dark-brown";
        static final String DARK_ORANGE = "dark-orange";
        static final String DARK_PURPLE = "dark-purple";
        static final String DARK_WARM_GRAY = "dark-warm-gray";
        static final String LIGHT_PINK = "light-pink";
        static final String LIGHT_GREEN = "light-green";
        static final String LIGHT_BLUE = "ligth-blue";
        static final String LIGHT_RED = "light-red";
        static final String LIGHT_TEAL = "light-teal";
        static final String LIGHT_YELLOW = "light-yellow";
        static final String LIGHT_ORANGE = "light-orange";
        static final String LIGHT_WARM_GRAY = "light-warm-gray";
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        // Read project color from cursor
        String projectColor = cursor.getString(TaskListFragment.COL_PROJECT_COLOR);
        // Find TextView and set task name on it.
        int color = 0;
        Resources res = context.getResources();
        switch (projectColor) {
            case AsanaProjectColor.DARK_BLUE:
                color = res.getColor(R.color.dark_blue);
                break;
            case AsanaProjectColor.DARK_GREEN:
                color = res.getColor(R.color.dark_green);
                break;
            case AsanaProjectColor.DARK_BROWN:
                color = res.getColor(R.color.dark_brown);
                break;
            case AsanaProjectColor.DARK_ORANGE:
                color = res.getColor(R.color.dark_orange);
                break;
            case AsanaProjectColor.DARK_PINK:
                color = res.getColor(R.color.dark_pink);
                break;
            case AsanaProjectColor.DARK_PURPLE:
                color = res.getColor(R.color.dark_purple);
                break;
            case AsanaProjectColor.DARK_RED:
                color = res.getColor(R.color.dark_red);
                break;
            case AsanaProjectColor.DARK_TEAL:
                color = res.getColor(R.color.dark_teal);
                break;
            case AsanaProjectColor.DARK_WARM_GRAY:
                color = res.getColor(R.color.dark_warm_gray);
                break;
            case AsanaProjectColor.LIGHT_BLUE:
                color = context.getResources().getColor(R.color.light_blue);
                break;
            case AsanaProjectColor.LIGHT_GREEN:
                color = res.getColor(R.color.light_green);
                break;
            case AsanaProjectColor.LIGHT_ORANGE:
                color = res.getColor(R.color.light_orange);
                break;
            case AsanaProjectColor.LIGHT_PINK:
                color = res.getColor(R.color.light_pink);
                break;
            case AsanaProjectColor.LIGHT_RED:
                color = res.getColor(R.color.light_red);
                break;
            case AsanaProjectColor.LIGHT_WARM_GRAY:
                color = res.getColor(R.color.light_warm_gray);
                break;
            default:
                color = context.getResources().getColor(R.color.dark_green);
        }
        // Project color
        viewHolder.projectColorView.setBackgroundColor(color);

        // Project name
        viewHolder.projectNameView.setText(cursor.getString(TaskListFragment.COL_PROJECT_NAME));
    }
}
