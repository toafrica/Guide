package com.toafrica.guideview;

import android.view.View;

/**
 * 一些引导图每次展示的配置
 */
class Configuration {
    ClickMode mode = ClickMode.PARENT;
    Location location = Location.BOTTOM;
    View target;
    View guideView;
    int tipRes;
    int backgroundColor = 0x7f000000;
    int offsetX;
    int offsetY;
    int targetPaddingLeft;
    int targetPaddingTop;
    int targetPaddingRight;
    int targetPaddingBottom;
    float rx;
    float ry;
    float ratio;
    Configuration before;
    Configuration next;

    boolean hasNext() {
        return next != null;
    }

    private boolean hasBefore() {
        return before != null;
    }

    Configuration findRoot() {
        Configuration root = this;
        while (root != null && root.hasBefore()) {
            root = root.before;
        }
        return root;
    }
}
