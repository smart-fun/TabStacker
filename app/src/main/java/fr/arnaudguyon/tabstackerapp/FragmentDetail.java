package fr.arnaudguyon.tabstackerapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.Fade;
import android.transition.TransitionSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fr.arnaudguyon.tabstacker.TabStacker;

/**
 * Created by aguyon on 16.05.17.
 */

public class FragmentDetail extends Fragment implements TabStacker.TabStackInterface {

    private View mView;

    public static FragmentDetail createInstance() {
        FragmentDetail fragment = new FragmentDetail();
        fragment.setSharedElementEnterTransition(new DetailsTransition());
//        fragment.setEnterTransition(new Fade());
//        fragment.setExitTransition(new Fade());
        fragment.setSharedElementReturnTransition(new DetailsTransition());
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragmentdetail, container, false);
        return mView;
    }

    @Override
    public void onTabFragmentPresented(TabStacker.PresentReason reason) {

    }

    @Override
    public void onTabFragmentDismissed(TabStacker.DismissReason reason) {

    }

    @Override
    public View onSaveTabFragmentInstance(Bundle outState) {
        return mView;
    }

    @Override
    public void onRestoreTabFragmentInstance(Bundle savedInstanceState) {

    }

    public static class DetailsTransition extends TransitionSet {
        public DetailsTransition() {
            setOrdering(ORDERING_TOGETHER);
            addTransition(new ChangeBounds()).
                    addTransition(new ChangeTransform()).
                    addTransition(new ChangeImageTransform());
        }
    }
}
