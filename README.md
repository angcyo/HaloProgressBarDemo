# HaloProgressBarDemo
Kotlin-->模仿QQ发送图片进度效果

效果图:

![这里写图片描述](https://img-blog.csdnimg.cn/img_convert/b2786eeb16efd20ff5abe614c01e3817.gif)


**效果分析**
1. 带圆角的布局.(Canvas的clipPath方法实现, 不在本文介绍)
2. 蒙层(绘制一个有透明度的黑色)
3. 进度百分比(Canvas的drawText, 难点就是控制绘制的x和y坐标)
4. 白色进度圆圈(本文介绍,难点1)
5. 最外层是具有一定透明度的白色进度圆圈(实现方法和4一致)


----------

在Android中, 要绘制 圆柱形圆圈 , 及其不容易.
有同学可能会说用`drawArc`实现, 效果可以达到, 但是控制起来想当蓝瘦. 

所以, 我这里采用android的`Xfermode`模式绘制.
相信下面的图, 你应该很熟悉.
![这里写图片描述](https://img-blog.csdnimg.cn/img_convert/3bfca8818abe8b9469ad0cd65ef98697.png)
这就是`Xfermode`模式图, 不同的模式,绘制出来的效果不一样.

我这里使用的就是`SRC_OUT`模式.
思路就是:
1. 先绘制一个半径比较大的圆. 对应图中`Src`
2. 再绘制一个半径略小的圆, 对应图中`Dst` , 同时设置`Paint`的`SrcOut`模式, 就可以达到效果

需要注意的是, 使用`Xfermode`模式时, 尽量在新的Canvas上绘制, 不要在`View.onDraw`方法中的`canvas`绘制, 因为可能会无效.

```
circleCanvas?.let {
     it.save()
     it.translate((measuredWidth / 2).toFloat(), (measuredHeight / 2).toFloat())

     paint.xfermode = clearXF  //清理之前的图像
     it.drawPaint(paint)
     paint.xfermode = null

     //绘制圆圈
     paint.color = circleColor
     it.drawCircle(0f, 0f, circleOuterRadius + (circleOuterAnimMaxRadius - circleOuterRadius) * animatorValue, paint)  //绘制外圈圆

     paint.xfermode = srcOutXF
     it.drawCircle(0f, 0f, circleInnerRadius, paint) //绘制略小的圆
     paint.xfermode = null

     it.restore()

     canvas.drawBitmap(circleBitmap, 0f, 0f, null) //绘制好的bitmap, 通过此方法绘制在View上

, 0f, 0f, null)

}
```

