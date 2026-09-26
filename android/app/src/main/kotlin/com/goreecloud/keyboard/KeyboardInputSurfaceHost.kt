package com.goreecloud.keyboard

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

internal class KeyboardInputSurfaceHost(context: Context) : FrameLayout(context) {
    fun showSurface(surface: View) {
        if (childCount == 1 && getChildAt(0) === surface) return
        (surface.parent as? ViewGroup)?.removeView(surface)
        removeAllViews()
        addView(surface, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    fun currentSurface(): View? = if (childCount == 1) getChildAt(0) else null
}
