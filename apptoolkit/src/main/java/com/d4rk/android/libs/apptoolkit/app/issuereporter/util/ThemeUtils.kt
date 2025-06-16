package com.heinrichreimersoftware.androidissuereporter.util

import android.content.Context
import android.content.res.TypedArray
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.NonNull
import androidx.annotation.StyleableRes
import com.google.android.material.R as MaterialR

object ThemeUtils {
    @ColorInt
    private fun resolveThemeColor(context: Context, @AttrRes attr: Int): Int {
        val typedValue = TypedValue()
        val a: TypedArray = context.obtainStyledAttributes(typedValue.data, intArrayOf(attr))
        val color = a.getColor(0, 0)
        a.recycle()
        return color
    }

    @ColorInt
    fun getColorAccent(@NonNull context: Context): Int {
        return resolveThemeColor(context, MaterialR.attr.colorSecondary)
    }
}
