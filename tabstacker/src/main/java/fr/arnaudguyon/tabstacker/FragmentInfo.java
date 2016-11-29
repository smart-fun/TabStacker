package fr.arnaudguyon.tabstacker;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by aguyon on 28.11.16.
 */

class FragmentInfo {

    private static final String BUNDLE_FRAGMENT_CLASS = "fragment_class";
    private static final String BUNDLE_FRAGMENT_ARGUMENTS = "fragment_arguments";
    private static final String BUNDLE_FRAGMENT_DATA = "fragment_data";
    private static final String BUNDLE_TYPE = "type";
    private static final String BUNDLE_ANIMATION = "animation";

    Fragment mFragment;
    AnimationSet mAnimationSet;
    TabStacker.Type mType;

    FragmentInfo(Fragment fragment, AnimationSet animationSet, TabStacker.Type type) {
        mFragment = fragment;
        mAnimationSet = animationSet;
        mType = type;
    }

    static FragmentInfo restoreInstance(Bundle bundle) {

        String className = bundle.getString(BUNDLE_FRAGMENT_CLASS);
        Fragment fragment;
        try {
            Class<Fragment> fragmentClass = (Class<Fragment>) Class.forName(className);
            fragment = fragmentClass.newInstance();
            Bundle arguments = bundle.getBundle(BUNDLE_FRAGMENT_ARGUMENTS);
            fragment.setArguments(arguments);
            Bundle fragmentData = bundle.getBundle(BUNDLE_FRAGMENT_DATA);
            if ((fragmentData != null) && (fragment instanceof TabStacker.TabStackInterface)) {
                ((TabStacker.TabStackInterface) fragment).onRestoreTabFragmentInstance(fragmentData);
            }

            TabStacker.Type type = TabStacker.Type.valueOf(bundle.getString(BUNDLE_TYPE));
            Bundle animation = bundle.getBundle(BUNDLE_ANIMATION);
            AnimationSet animationSet = AnimationSet.restoreInstance(animation);
            return new FragmentInfo(fragment, animationSet, type);

        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        } catch (InstantiationException e2) {
            e2.printStackTrace();
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
        }
        return null;
    }

    Bundle saveInstance() {
        Bundle bundle = new Bundle();

        String fragmentClassName = mFragment.getClass().getName();
        Bundle fragmentArguments = mFragment.getArguments();
        Bundle fragmentData = (mFragment instanceof TabStacker.TabStackInterface) ? ((TabStacker.TabStackInterface) mFragment).onSaveTabFragmentInstance() : null;
        String type = mType.name();
        Bundle animation = (mAnimationSet != null) ? mAnimationSet.saveInstance() : null;

        bundle.putString(BUNDLE_FRAGMENT_CLASS, fragmentClassName);
        bundle.putBundle(BUNDLE_FRAGMENT_ARGUMENTS, fragmentArguments);
        bundle.putBundle(BUNDLE_FRAGMENT_DATA, fragmentData);
        bundle.putString(BUNDLE_TYPE, type);
        bundle.putBundle(BUNDLE_ANIMATION, animation);

        return bundle;
    }

}
