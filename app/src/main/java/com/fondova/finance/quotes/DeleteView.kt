package com.fondova.finance.quotes

import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.fondova.finance.R

class DeleteView(context: Context, attr: AttributeSet?): FrameLayout(context, attr) {

    constructor(context: Context): this(context, null)

    init {
        layoutParams = ViewGroup.LayoutParams(200, ViewGroup.LayoutParams.MATCH_PARENT)
        setBackgroundColor(Color.RED)

        val imageView = ImageView(context)

        val imageViewLayoutParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        imageViewLayoutParams.gravity = Gravity.CENTER
        imageViewLayoutParams.setMargins(12, 0, 12, 0)
        imageView.contentDescription = context.resources.getString(R.string.content_desc_icon_delete)
        imageView.setImageResource(R.drawable.ic_delete)
        imageView.setColorFilter(ContextCompat.getColor(context, R.color.white))

        addView(imageView, imageViewLayoutParams)
    }
}