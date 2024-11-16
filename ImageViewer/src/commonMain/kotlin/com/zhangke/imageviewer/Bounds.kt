package com.zhangke.imageviewer

import androidx.compose.ui.geometry.Offset
import kotlin.ranges.coerceAtLeast
import kotlin.ranges.coerceAtMost
import kotlin.ranges.rangeTo

internal data class Bounds(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {

    val isEmpty = (right - left) * (bottom - top) == 0F

    fun inside(x: Float, y: Float): Boolean {
        return xInside(x) && yInside(y)
    }

    fun outside(x: Float, y: Float) = !inside(x, y)

    fun inside(offset: Offset) = inside(x = offset.x, y = offset.y)

    fun outside(offset: Offset) = outside(x = offset.x, y = offset.y)

    fun xInside(x: Float) = x in left..right

    fun yInside(y: Float) = y in top..bottom

    fun xOutside(x: Float) = !xInside(x)

    fun yOutside(y: Float) = !yInside(y)

    fun outsideAbsolute(offset: Offset) = xOutside(offset.x) && yOutside(offset.y)

    fun coerceInY(y: Float): Float {
        return y.coerceAtLeast(top).coerceAtMost(bottom)
    }

    fun coerceInX(x: Float): Float{
        return x.coerceAtLeast(left).coerceAtMost(right)
    }

    fun coerceIn(offset: Offset): Offset {
        return Offset(
            x = offset.x.coerceIn(left..right),
            y = offset.y.coerceIn(top..bottom),
        )
    }

    companion object {

        val EMPTY = Bounds(0F, 0F, 0F, 0F)

    }
}
