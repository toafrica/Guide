[![](https://api.bintray.com/packages/wildme/maven/guide/images/download.svg)](https://bintray.com/wildme/maven/guide)


# Guide
Android 新手蒙层引导图,链式调用,一链到底,就是这么爽

支持圆角与背景设置,支持百分比适配引导图,妈妈再也不用担心分辨率的问题了!
支持xml及View的添加方式,位置使用方便,调整灵活



## 导入

```
implementation 'com.toafrica:guide:1.0.3'
```

## 使用

```
new Guide.Builder(Orientation.PORTRAIT)//设置当前横竖屏模式
    .setTarget(target_1)//设置第一个引导图目标
    .setCorner(5, 5)//设置圆角
    .setBackgroundColor(0xee000000)//背景色
    .location(Location.BOTTOM)//引导图相对目标位置
    .setHighLightPadding(10, 0, 8, 10)//设置高亮目标区域padding
    .setOffset(20, 20)//设置引导图的 x,y偏移量
    .setGuide(R.layout.guide_1)//引导图
    .setRatio(0.58f)//设置引导图的屏占比,方便多分辨率尺寸适配
    .setClickMode(ClickMode.GUIDE)//点击事件触发的模式
    .next()//下一个引导图
    .setTarget(target_2)
    .location(Location.BOTTOM)
    .setClickMode(ClickMode.TARGET)
    .setOffset(20, 0)
    .setGuide(R.layout.guide_2)
    .setRatio(0.58f)
    .show();
```

