package com.github.shadowsocks.widget

import android.content.Context
import android.text.format.Formatter
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomappbar.BottomAppBar
import com.mrd.news.shadowsocks.R

class StatsBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.bottomAppBarStyle
) :
    BottomAppBar(context, attrs, defStyleAttr) {
    private lateinit var statusText: TextView

    private lateinit var txText: TextView
    private lateinit var rxText: TextView
    private lateinit var txRateText: TextView
    private lateinit var rxRateText: TextView
    private val behavior = object : Behavior() {
        val threshold = context.resources.getDimensionPixelSize(R.dimen.stats_bar_scroll_threshold)
        override fun onNestedScroll(
            coordinatorLayout: CoordinatorLayout, child: BottomAppBar, target: View,
            dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int
        ) {
            val dy = dyConsumed + dyUnconsumed
            super.onNestedScroll(
                coordinatorLayout, child, target, dxConsumed, if (Math.abs(dy) >= threshold) dy else 0,
                dxUnconsumed, 0, type
            )
        }
    }

    override fun getBehavior() = behavior

   /* fun changeState(state: Int) {
        val activity = context as VPNActivity
        if (state != BaseService.CONNECTED) {
            updateTraffic(0, 0, 0, 0)
            tester.status.removeObservers(activity)
            if (state != BaseService.IDLE) tester.invalidate()
            statusText.setText(
                when (state) {
                    BaseService.CONNECTING -> R.string.connecting
                    BaseService.STOPPING -> R.string.stopping
                    else -> R.string.not_connected
                }
            )
        } else {
            behavior.slideUp(this)
            tester.status.observe(activity, Observer {
                it.retrieve(statusText::setText) {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }
            })
        }
    }*/

    fun updateTraffic(txRate: Long, rxRate: Long, txTotal: Long, rxTotal: Long) {
        txText.text = "▲ ${Formatter.formatFileSize(context, txTotal)}"
        rxText.text = "▼ ${Formatter.formatFileSize(context, rxTotal)}"
        txRateText.text = context.getString(R.string.speed, Formatter.formatFileSize(context, txRate))
        rxRateText.text = context.getString(R.string.speed, Formatter.formatFileSize(context, rxRate))
    }
}
