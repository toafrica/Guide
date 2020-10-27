package com.toafrica.guideview;

/**
 * 用于接收用户的点击事件的三种模式
 * 1:PARENT模式  整体蒙层接收用户的点击
 * 1:GUIDE模式  引导图接收用户的点击
 * 1:TARGET模式   目标视图接收用户点击
 */
public enum ClickMode {
    PARENT, GUIDE,TARGET
}