package com.geaden.android.gsana.app;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Adapter for mapping projects to the view.
 */
public class GsanaProjectsAdapter extends CursorAdapter {
    private final String LOG_TAG = getClass().getSimpleName();

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
    public class AsanaProjectColor {
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

    /**
     * Gets color id from provided color string
     * @param context the context of application
     * @param colorString the color string representation
     * @return color resource id
     */
    public static int getColor(Context context, String colorString) {
        int color;
        Resources res = context.getResources();
        switch (colorString) {
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
            case AsanaProjectColor.LIGHT_TEAL:
                color = res.getColor(R.color.light_teal);
                break;
            case AsanaProjectColor.LIGHT_YELLOW:
                color = res.getColor(R.color.light_yellow);
                break;
            default:
                color = context.getResources().getColor(R.color.dark_warm_gray);
        }
        return color;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        // Read project color from cursor
        String projectColor = cursor.getString(LoadersColumns.COL_PROJECT_COLOR);
        // Find TextView and set project name on it.
//        int color = 0;
//        Resources res = context.getResources();
//        switch (projectColor) {
//            case AsanaProjectColor.DARK_BLUE:
//                color = res.getColor(R.color.dark_blue);
//                break;
//            case AsanaProjectColor.DARK_GREEN:
//                color = res.getColor(R.color.dark_green);
//                break;
//            case AsanaProjectColor.DARK_BROWN:
//                color = res.getColor(R.color.dark_brown);
//                break;
//            case AsanaProjectColor.DARK_ORANGE:
//                color = res.getColor(R.color.dark_orange);
//                break;
//            case AsanaProjectColor.DARK_PINK:
//                color = res.getColor(R.color.dark_pink);
//                break;
//            case AsanaProjectColor.DARK_PURPLE:
//                color = res.getColor(R.color.dark_purple);
//                break;
//            case AsanaProjectColor.DARK_RED:
//                color = res.getColor(R.color.dark_red);
//                break;
//            case AsanaProjectColor.DARK_TEAL:
//                color = res.getColor(R.color.dark_teal);
//                break;
//            case AsanaProjectColor.DARK_WARM_GRAY:
//                color = res.getColor(R.color.dark_warm_gray);
//                break;
//            case AsanaProjectColor.LIGHT_BLUE:
//                color = context.getResources().getColor(R.color.light_blue);
//                break;
//            case AsanaProjectColor.LIGHT_GREEN:
//                color = res.getColor(R.color.light_green);
//                break;
//            case AsanaProjectColor.LIGHT_ORANGE:
//                color = res.getColor(R.color.light_orange);
//                break;
//            case AsanaProjectColor.LIGHT_PINK:
//                color = res.getColor(R.color.light_pink);
//                break;
//            case AsanaProjectColor.LIGHT_RED:
//                color = res.getColor(R.color.light_red);
//                break;
//            case AsanaProjectColor.LIGHT_WARM_GRAY:
//                color = res.getColor(R.color.light_warm_gray);
//                break;
//            case AsanaProjectColor.LIGHT_TEAL:
//                color = res.getColor(R.color.light_teal);
//                break;
//            case AsanaProjectColor.LIGHT_YELLOW:
//                color = res.getColor(R.color.light_yellow);
//                break;
//            default:
//                color = context.getResources().getColor(R.color.dark_warm_gray);
//        }
        // Project color
        viewHolder.projectColorView.setBackgroundColor(getColor(context, projectColor));

        // Project name
        viewHolder.projectNameView.setText(cursor.getString(LoadersColumns.COL_PROJECT_NAME));
    }
}
