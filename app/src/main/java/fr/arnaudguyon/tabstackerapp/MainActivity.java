package fr.arnaudguyon.tabstackerapp;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import fr.arnaudguyon.tabstacker.AnimationSet;
import fr.arnaudguyon.tabstacker.TabStacker;

public class MainActivity extends FragmentActivity {

    private static final String TAG = "MainActivity";
    private static final String MULTISTACK_SAVED_INSTANCE = "MultiStackSavedInstance";

    private TabStacker mTabStacker;
    private AnimationSet mAddAnimation = new Anims.SlideFromBottom();
    private AnimationSet mReplaceAnimation = new Anims.SlideFromRight();

    private enum Tab {
        TAB_A(R.id.tabA),
        TAB_B(R.id.tabB),
        TAB_C(R.id.tabC);

        private int mButtonResId;

        Tab(@IdRes int buttonResId) {
            mButtonResId = buttonResId;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.mainactivity);

        if (savedInstanceState == null) {
            mTabStacker = new TabStacker(getSupportFragmentManager(), R.id.fragmentHolder);
            onClickOnTab(Tab.TAB_A);
        } else {
            mTabStacker = new TabStacker(getSupportFragmentManager(), R.id.fragmentHolder);
            Bundle bundle = savedInstanceState.getBundle(MULTISTACK_SAVED_INSTANCE);
            mTabStacker.restoreInstance(bundle);
            Tab selectedTab = Tab.valueOf(mTabStacker.getCurrentTabName());
            onClickOnTab(selectedTab);
        }

        for (final Tab tab : Tab.values()) {
            final Button button = (Button) findViewById(tab.mButtonResId);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickOnTab(tab);
                }
            });
        }

        // BACK BUTTON
        View backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // REPLACE BUTTON
        View replaceButton = findViewById(R.id.replaceButton);
        replaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TestFragment fragment = createFragment();
                mTabStacker.replaceFragment(fragment, mReplaceAnimation);
            }
        });

        // ADD BUTTON
        View addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TestFragment fragment = createFragment();
                mTabStacker.addFragment(fragment, mAddAnimation);
            }
        });

    }

    private void onClickOnTab(Tab clickedTab) {

        Log.i(TAG, "switch to tab " + clickedTab.name());

        // Update Button background (green=selected, red=not selected)
        int colorON = getResources().getColor(R.color.tabON);
        int colorOFF = getResources().getColor(R.color.tabOFF);
        for(final Tab tab : Tab.values()) {
            final Button button = (Button) findViewById(tab.mButtonResId);
            if (tab == clickedTab) {
                button.setBackgroundColor(colorON);
            } else {
                button.setBackgroundColor(colorOFF);
            }
        }

        // switch to Tab Stack
        String tabName = clickedTab.name();
        if (!mTabStacker.switchToTab(tabName)) {    // Try to switch to the TAB STACK
            // Fails = no fragment yet on this stack -> push the 1st fragment of the stack
            TestFragment fragment = createFragment();
            mTabStacker.replaceFragment(fragment, null);
        }

    }

    private TestFragment createFragment() {
        String tabName = mTabStacker.getCurrentTabName();
        int number = mTabStacker.getCurrentTabSize() + 1;
        String title = "Fragment " + tabName + " " + number;
        TestFragment fragment = TestFragment.createInstance(title);
        return fragment;
    }

    @Override
    public void onBackPressed() {
        if (!mTabStacker.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO: simplify this (no bundle creation). IMPORTANT TO CALL BEFORE onSaveInstanceState
        Bundle bundle = mTabStacker.saveInstance();
        outState.putBundle(MULTISTACK_SAVED_INSTANCE, bundle);

        super.onSaveInstanceState(outState);
    }
}
