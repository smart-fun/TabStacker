/*
    Copyright 2016 Arnaud Guyon

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
package fr.arnaudguyon.tabstackerapp;

import fr.arnaudguyon.tabstacker.AnimationSet;

/**
 * Default AnimationSets for the App
 */

class Anims {

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
