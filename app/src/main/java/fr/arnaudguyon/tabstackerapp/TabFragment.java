package fr.arnaudguyon.tabstackerapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import fr.arnaudguyon.tabstacker.TabStacker;

/**
 * Created by aguyon on 28.11.16.
 */

public class TabFragment extends Fragment implements TabStacker.TabStackInterface {

    private static final String TAG = "TabFragment";

    private static final String ARGUMENT_TITLE = "title";
    private static final String ARGUMENT_COLOR = "color";
    private static final String ARGUMENT_RANDOM_TOP = "randomTop";

    private View mView;

    public static TabFragment createInstance(String title, int color) {
        TabFragment fragment = new TabFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARGUMENT_TITLE, title);
        bundle.putInt(ARGUMENT_COLOR, color);
        bundle.putFloat(ARGUMENT_RANDOM_TOP, (float) (Math.random() * 0.6 + 0.2));
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.testfragment, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle arguments = getArguments();

        String title = arguments.getString(ARGUMENT_TITLE);
        int color = arguments.getInt(ARGUMENT_COLOR);
        float randomTop = arguments.getFloat(ARGUMENT_RANDOM_TOP);

        // SET BACKGROUND RANDOM COLOR
        view.setBackgroundColor(color);

        // SET FRAGMENT TITLE
        TextView titleView = (TextView) view.findViewById(R.id.titleView);
        titleView.setText(title);
        centerTitle(randomTop);

        MainActivity activity = (MainActivity) getActivity();
        activity.restoreView(this, view);
    }

    private void centerTitle(float topValue) {

        {
            View topView = getView().findViewById(R.id.topView);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.weight = topValue;
            topView.setLayoutParams(params);
        }

        {
            float bottomValue = 1 - topValue;
            View bottomView = getView().findViewById(R.id.bottomView);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.weight = bottomValue;
            bottomView.setLayoutParams(params);
        }

    }

    @Override
    public void onTabFragmentPresented(TabStacker.PresentReason reason) {
        // Logs the Reason and Fragment name
        Bundle arguments = getArguments();
        String title = arguments.getString(ARGUMENT_TITLE);
        Log.i(TAG, "PRESENT " + title + " (" + reason.name() + ")");
    }

    @Override
    public void onTabFragmentDismissed(TabStacker.DismissReason reason) {
        // Logs the Reason and Fragment name
        Bundle arguments = getArguments();
        String title = arguments.getString(ARGUMENT_TITLE);
        Log.i(TAG, "DISMISS " + title + " (" + reason.name() + ")");
    }

    @Override
    public View onSaveTabFragmentInstance(Bundle outState) {
        // You can add here some precious data to be saved in the bundle
        // getView() is always null, so needs to keep a reference so that the view values can be restored correctly later
        return mView;
    }

    @Override
    public void onRestoreTabFragmentInstance(Bundle savedInstanceState) {
        // You could retrieve here some precious data that has been saved with onSaveTabFragmentInstance
    }

}
