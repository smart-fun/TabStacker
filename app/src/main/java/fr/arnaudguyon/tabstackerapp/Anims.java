package fr.arnaudguyon.tabstackerapp;

import fr.arnaudguyon.tabstacker.AnimationSet;

/**
 * Created by aguyon on 28.11.16.
 */

public class Anims {

    static class SlideFromRight extends AnimationSet {
        SlideFromRight() {
            super(R.anim.slide_from_right, R.anim.slide_to_left, R.anim.slide_from_left, R.anim.slide_to_right);
        }
    }
    static class SlideFromBottom extends AnimationSet {
        SlideFromBottom() {
            super(R.anim.slide_from_bottom, R.anim.slide_to_top, R.anim.slide_from_top, R.anim.slide_to_bottom);
        }
    }
}
