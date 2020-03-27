package com.fondova.finance.charts

import android.graphics.PointF
import android.view.View
import com.shinobicontrols.charts.ShinobiChart

class DoubleClickListener(val onClickListener: View.OnClickListener): ShinobiChart.OnGestureListener {



    override fun onDoubleTapDown(p0: ShinobiChart?, p1: PointF?) {
        onClickListener.onClick(null)
    }

    override fun onPinch(p0: ShinobiChart?, p1: PointF?, p2: PointF?, p3: PointF?) {

    }

    override fun onSecondTouchDown(p0: ShinobiChart?, p1: PointF?, p2: PointF?) {

    }

    override fun onPinchEnd(p0: ShinobiChart?, p1: PointF?, p2: Boolean, p3: PointF?) {

    }

    override fun onSingleTouchUp(p0: ShinobiChart?, p1: PointF?) {

    }

    override fun onSwipeEnd(p0: ShinobiChart?, p1: PointF?, p2: Boolean, p3: PointF?) {

    }

    override fun onSecondTouchUp(p0: ShinobiChart?, p1: PointF?, p2: PointF?) {

    }

    override fun onLongTouchDown(p0: ShinobiChart?, p1: PointF?) {

    }

    override fun onSwipe(p0: ShinobiChart?, p1: PointF?, p2: PointF?) {

    }

    override fun onLongTouchUp(p0: ShinobiChart?, p1: PointF?) {

    }

    override fun onDoubleTapUp(p0: ShinobiChart?, p1: PointF?) {

    }

    override fun onSingleTouchDown(p0: ShinobiChart?, p1: PointF?) {

    }


}