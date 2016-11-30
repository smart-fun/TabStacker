package fr.arnaudguyon.tabstackerapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import fr.arnaudguyon.tabstacker.TabStacker;

/**
 * Created by aguyon on 28.11.16.
 */

public class TestFragment extends Fragment implements TabStacker.TabStackInterface {

    private static final String TAG = "TestFragment";

    private static final String ARGUMENT_TITLE = "title";
    private static final String ARGUMENT_COLOR = "randomColor";
    private static final String ARGUMENT_RANDOM_TOP = "randomTop";

    private static final String DYNAMIC_DATA_CHECKBOX = "checkBox";

    private boolean mCheckBoxValue;
    private View mView;

    public static TestFragment createInstance(String title) {
        TestFragment fragment = new TestFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARGUMENT_TITLE, title);
        bundle.putInt(ARGUMENT_COLOR, generateRandomColor());
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
        centerTitle(randomTop, 0);

        // RESTORE CHECKBOX
        CheckBox optionCheckBox = (CheckBox) mView.findViewById(R.id.optionCheckBox);
        optionCheckBox.setChecked(mCheckBoxValue);

    }

    private void centerTitle(float topValue, float leftValue) {

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

    private static int generateRandomColor() {
        int alpha = 200;
        int red = (int) (Math.random() * 128) + 127;
        int green = (int) (Math.random() * 128) + 127;
        int blue = (int) (Math.random() * 128) + 127;
        return ((alpha << 24) + (red << 16) + (green << 8) + blue);
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
    public void onSaveTabFragmentInstance(Bundle outState) {
        if (mView == null) {    // the Fragment has not been presented since the last save, so put the last saved values
            outState.putBoolean(DYNAMIC_DATA_CHECKBOX, mCheckBoxValue);
        } else {
            CheckBox optionCheckBox = (CheckBox) mView.findViewById(R.id.optionCheckBox);
            outState.putBoolean(DYNAMIC_DATA_CHECKBOX, optionCheckBox.isChecked());
        }
    }

    @Override
    public void onRestoreTabFragmentInstance(Bundle savedInstanceState) {
        mCheckBoxValue = savedInstanceState.getBoolean(DYNAMIC_DATA_CHECKBOX);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
