package com.geaden.android.gsana.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

/**
 * Task create activity
 */
public class TaskCreateActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_new);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container_task_new, new TaskCreateFragment())
                    .commit();
        }

    }

    public static class TaskCreateFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_task_new, container, false);
            Button button = (Button) rootView.findViewById(R.id.save_task_button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TextView taskName = (TextView) rootView.findViewById(R.id.task_name);
                    TextView taskDesc = (TextView) rootView.findViewById(R.id.task_description);
                    Toast.makeText(getActivity(), taskName.getText() + ":" + taskDesc.getText(),
                            Toast.LENGTH_LONG).show();
                }
            });
            return rootView;
        }
    }
}
