package fr.arnaudguyon.tabstackerapp;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import fr.arnaudguyon.tabstacker.AnimationSet;
import fr.arnaudguyon.tabstacker.TabStacker;

public class MainActivity extends FragmentActivity {

    private static final String TAG = "MainActivity";

    private TabStacker mTabStacker;
    private AnimationSet mAddAnimation = new Anims.SlideFromBottom();
    private AnimationSet mReplaceAnimation = new Anims.SlideFromRight();

    private enum Tab {
        TAB_A(R.id.tabA, R.color.tabA),
        TAB_B(R.id.tabB, R.color.tabB),
        TAB_C(R.id.tabC, R.color.tabC);

        private int mButtonResId;
        private int mColor;

        Tab(@IdRes int buttonResId, @ColorRes int color) {
            mButtonResId = buttonResId;
            mColor = color;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.mainactivity);

        mTabStacker = new TabStacker(getSupportFragmentManager(), R.id.fragmentHolder);
        if (savedInstanceState == null) {
            onClickOnTab(Tab.TAB_A);
        } else {
            mTabStacker = new TabStacker(getSupportFragmentManager(), R.id.fragmentHolder);
            mTabStacker.restoreInstance(savedInstanceState);
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
                TabFragment fragment = createFragment();
                mTabStacker.replaceFragment(fragment, mReplaceAnimation);
            }
        });

        // ADD BUTTON
        View addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TabFragment fragment = createFragment();
                mTabStacker.addFragment(fragment, mAddAnimation);
            }
        });

    }

    private void onClickOnTab(Tab clickedTab) {

        Log.i(TAG, "switch to tab " + clickedTab.name());

        // Update Button state
        for(final Tab tab : Tab.values()) {
            View button = findViewById(tab.mButtonResId);
            button.setSelected((tab == clickedTab));
        }

        // switch to Tab Stack
        String tabName = clickedTab.name();
        if (!mTabStacker.switchToTab(tabName)) {    // Try to switch to the TAB STACK
            // Fails = no fragment yet on this stack -> push the 1st fragment of the stack
            TabFragment fragment = createFragment();
            mTabStacker.replaceFragment(fragment, null);
        }

    }

    private TabFragment createFragment() {
        String tabName = mTabStacker.getCurrentTabName();
        int number = mTabStacker.getCurrentTabSize() + 1;
        String title = "Fragment " + tabName + " " + number;
        int color = getResources().getColor(Tab.valueOf(tabName).mColor);
        color &= 0x00FFFFFF;
        color |= 0x7F000000;
        TabFragment fragment = TabFragment.createInstance(title, color);
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
        // Keep this first
        mTabStacker.saveInstance(outState);

        super.onSaveInstanceState(outState);
    }

    void restoreView(Fragment fragment, View view) {
        mTabStacker.restoreView(fragment, view);
    }
}
