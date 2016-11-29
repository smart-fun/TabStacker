package fr.arnaudguyon.tabstacker;

import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.v4.app.FragmentTransaction;

/**
 * Created by aguyon on 28.11.16.
 */

public class AnimationSet {
    private int mPushInAnim;
    private int mPushOutAnim;
    private int mPopInAnim;
    private int mPopOutAnim;

    public AnimationSet(@AnimRes int pushInAnim, @AnimRes int pushOutAnim, @AnimRes int popInAnim, @AnimRes int popOutAnim) {
        mPushInAnim = pushInAnim;
        mPushOutAnim = pushOutAnim;
        mPopInAnim = popInAnim;
        mPopOutAnim = popOutAnim;
    }

    void addToTransaction(FragmentTransaction transaction) {
        if ((mPushInAnim != 0) && (mPushOutAnim != 0)) {
            if ((mPopInAnim != 0) && (mPopOutAnim != 0)) {
                transaction.setCustomAnimations(mPushInAnim, mPushOutAnim, mPopInAnim, mPopOutAnim);
            } else {
                transaction.setCustomAnimations(mPushInAnim, mPushOutAnim);
            }
        }
    }

    @AnimRes int getPopInAnim() {
        return mPopInAnim;
    }

    @AnimRes int getPopOutAnim() {
        return mPopOutAnim;
    }

    @AnimRes int getPushInAnim() {
        return mPushInAnim;
    }

    @AnimRes int getPushOutAnim() {
        return mPushOutAnim;
    }


    // *** SAVE / RESTORE ***

    private static final String BUNDLE_PUSH_IN = "pushin";
    private static final String BUNDLE_PUSH_OUT = "pushout";
    private static final String BUNDLE_POP_IN = "popin";
    private static final String BUNDLE_POP_OUT = "popout";

    Bundle saveInstance() {
        Bundle bundle = new Bundle();
        bundle.putInt(BUNDLE_PUSH_IN, mPushInAnim);
        bundle.putInt(BUNDLE_PUSH_OUT, mPushOutAnim);
        bundle.putInt(BUNDLE_POP_IN, mPopInAnim);
        bundle.putInt(BUNDLE_POP_OUT, mPopOutAnim);
        return bundle;
    }

    static AnimationSet restoreInstance (Bundle bundle) {
        if (bundle != null) {
            int pushInAnim = bundle.getInt(BUNDLE_PUSH_IN);
            int pushOutAnim = bundle.getInt(BUNDLE_PUSH_OUT);
            int popInAnim = bundle.getInt(BUNDLE_POP_IN);
            int popOutAnim = bundle.getInt(BUNDLE_POP_OUT);
            return new AnimationSet(pushInAnim, pushOutAnim, popInAnim, popOutAnim);
        }
        return null;
    }

}
