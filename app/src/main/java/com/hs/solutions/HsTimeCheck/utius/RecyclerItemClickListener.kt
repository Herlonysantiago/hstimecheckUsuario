package com.hs.solutions.Hstimecheck.utils

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewConfiguration

class RecyclerItemClickListener(
    context: Context,
    private val recyclerView: RecyclerView,
    private val callback: Callback
) : RecyclerView.OnItemTouchListener {

    interface Callback {
        fun onClick(view: View, position: Int)
        fun onLongClick(view: View, position: Int)
        fun onDoubleClick(view: View, position: Int)
    }

    private val gestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean = true
            override fun onDoubleTap(e: MotionEvent): Boolean = true
        })

    private var lastTap = 0L
    private val DOUBLE = 300L

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        val child = rv.findChildViewUnder(e.x, e.y) ?: return false
        val pos = rv.getChildAdapterPosition(child)

        if (gestureDetector.onTouchEvent(e)) {
            val now = System.currentTimeMillis()
            if (now - lastTap < DOUBLE) {
                callback.onDoubleClick(child, pos)
                lastTap = 0
            } else {
                lastTap = now
                child.postDelayed({
                    if (System.currentTimeMillis() - lastTap >= DOUBLE) {
                        callback.onClick(child, pos)
                        lastTap = 0
                    }
                }, DOUBLE)
            }
            return true
        }

        // long press
        if (e.action == MotionEvent.ACTION_DOWN) {
            child.postDelayed({
                callback.onLongClick(child, pos)
            }, ViewConfiguration.getLongPressTimeout().toLong())
        }

        return false
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
    override fun onRequestDisallowInterceptTouchEvent(d: Boolean) {}
}
