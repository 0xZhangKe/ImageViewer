package com.zhangke.imageviewer

import android.util.Log
import androidx.compose.animation.core.AnimationScope
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.unit.Velocity
import kotlin.math.abs

@Composable
fun rememberImageViewerState(
    minimumScale: Float = 1f,
    maximumScale: Float = 3f,
    onDragDismissRequest: (() -> Unit)? = null,
): ImageViewerState {
    val dismissRequest by rememberUpdatedState(onDragDismissRequest)
    return rememberSaveable(
        saver = ImageViewerState.Saver,
    ) {
        ImageViewerState(
            minimumScale = minimumScale,
            maximumScale = maximumScale,
        )
    }.apply {
        this.onDragDismissRequest = dismissRequest
    }
}

@Stable
class ImageViewerState(
    private val minimumScale: Float = 1f,
    private val maximumScale: Float = 3f,
) {

    var onDragDismissRequest: (() -> Unit)? = null

    private var _currentWidthPixel = mutableFloatStateOf(0F)
    private var _currentHeightPixel = mutableFloatStateOf(0F)
    private var _currentOffsetXPixel = mutableFloatStateOf(0F)
    private var _currentOffsetYPixel = mutableFloatStateOf(0F)

    val currentWidthPixel: Float by _currentWidthPixel
    val currentHeightPixel: Float by _currentHeightPixel
    val currentOffsetXPixel: Float by _currentOffsetXPixel
    val currentOffsetYPixel: Float by _currentOffsetYPixel

    private var aspectRatio: Float = 1F

    private var layoutSize: Size = Size.Zero
    private val standardWidth: Float get() = layoutSize.width
    private val standardHeight: Float get() = standardWidth / aspectRatio

    val exceed: Boolean get() = !_currentWidthPixel.floatValue.equalsExactly(layoutSize.width)

    private var flingAnimation: AnimationScope<Offset, AnimationVector2D>? = null
    private var scaleAnimation: AnimationScope<Float, AnimationVector1D>? = null
    private var resumeOffsetYAnimation: AnimationScope<Float, AnimationVector1D>? = null

    private val draggableBounds: Bounds
        get() {
            return calculateDragBounds(
                imageWidth = _currentWidthPixel.floatValue,
                imageHeight = _currentHeightPixel.floatValue,
            )
        }

    fun updateLayoutSize(size: Size) {
        layoutSize = size
        onLayoutSizeChanged()
    }

    fun setImageAspectRatio(ratio: Float) {
        Log.d("F_TEST", "updateImageAspectRatio($ratio)")
        if (aspectRatio.equalsExactly(ratio)) return
        aspectRatio = ratio
        onLayoutSizeChanged()
    }

    private fun onLayoutSizeChanged() {
        _currentWidthPixel.floatValue = standardWidth
        _currentHeightPixel.floatValue = standardHeight
        _currentOffsetXPixel.floatValue = 0F
        _currentOffsetYPixel.floatValue = layoutSize.height / 2F - standardHeight / 2F
    }

    suspend fun animateToStandard() {
        val layoutSize = layoutSize
        if (layoutSize == Size.Zero) return
        val targetWidth = standardWidth
        val targetHeight = standardHeight
        animateToTarget(
            targetWidth = targetWidth,
            targetHeight = targetHeight,
            targetOffsetX = 0F,
            targetOffsetY = layoutSize.height / 2F - targetHeight / 2F,
        )
    }

    suspend fun animateToBig(point: Offset) {
        val layoutSize = layoutSize
        if (layoutSize == Size.Zero) return

        val targetWidth = standardWidth * maximumScale
        val targetHeight = targetWidth / aspectRatio
        var targetOffsetX = currentOffsetXPixel * maximumScale
        var targetOffsetY = layoutSize.height / 2F - targetHeight / 2F

        if (point.isSpecified && point.isValid()) {
            // tap point must be in the image bounds
            if (point.y < currentOffsetYPixel || point.y > (currentOffsetYPixel + currentHeightPixel)) return
            val xRatio = point.x / currentWidthPixel
            val yRatio = (point.y - currentOffsetYPixel) / currentHeightPixel
            targetOffsetX = -(targetWidth * xRatio - point.x)
            targetOffsetY = point.y - targetHeight * yRatio
        }
        val dragBounds = calculateDragBounds(
            imageWidth = targetWidth,
            imageHeight = targetHeight,
        )
        animateToTarget(
            targetWidth = targetWidth,
            targetHeight = targetHeight,
            targetOffsetX = dragBounds.coerceInX(targetOffsetX),
            targetOffsetY = dragBounds.coerceInY(targetOffsetY),
        )
    }

    fun drag(dragAmount: Offset) {
        cancelAnimation()
        if (exceed) {
            dragForVisit(dragAmount)
        } else {
            dragForExit(dragAmount)
        }
    }

    private fun dragForVisit(dragAmount: Offset) {
        val currentOffset = Offset(_currentOffsetXPixel.floatValue, _currentOffsetYPixel.floatValue)
        val newOffset = currentOffset + dragAmount
        val fixedOffset = draggableBounds.coerceIn(newOffset)
        _currentOffsetXPixel.floatValue = fixedOffset.x
        _currentOffsetYPixel.floatValue = fixedOffset.y
    }

    private fun dragForExit(dragAmount: Offset) {
        val dragAmountY = dragAmount.y
        if (dragAmountY <= 0F) {
            if (_currentOffsetYPixel.floatValue > 0F) {
                _currentOffsetYPixel.floatValue += dragAmountY
            }
        } else {
            _currentOffsetYPixel.floatValue += dragAmountY
        }
    }

    suspend fun dragStop(initialVelocity: Velocity) {
        cancelAnimation()
        if (!exceed) {
            dragStopForExit()
            return
        }
        val initialValue = Offset(_currentOffsetXPixel.floatValue, _currentOffsetYPixel.floatValue)
        AnimationState(
            typeConverter = Offset.VectorConverter,
            initialValue = initialValue,
            initialVelocity = initialVelocity.toOffset(),
        ).animateDecay(exponentialDecay()) {
            flingAnimation = this
            if (draggableBounds.outsideAbsolute(value) ||
                velocity.getDistance() <= 300
            ) {
                flingAnimation = null
                cancelAnimation()
                return@animateDecay
            }
            val progressOffset = draggableBounds.coerceIn(value)
            _currentOffsetXPixel.floatValue = progressOffset.x
            _currentOffsetYPixel.floatValue = progressOffset.y
        }
    }

    private suspend fun dragStopForExit() {
        cancelAnimation()
        val standardOffsetY = layoutSize.height / 2F - _currentHeightPixel.floatValue / 2F
        val totalAmount = _currentOffsetYPixel.floatValue - standardOffsetY
        val exitOffsetYThresholds = standardHeight * 0.3F
        if (onDragDismissRequest != null && totalAmount > exitOffsetYThresholds) {
            onDragDismissRequest?.invoke()
        } else {
            val anim = AnimationState(initialValue = _currentOffsetYPixel.floatValue)
            anim.animateTo(
                targetValue = standardOffsetY,
                animationSpec = tween(durationMillis = ImageViewerDefault.ANIMATION_DURATION),
            ) {
                resumeOffsetYAnimation = this
                _currentOffsetYPixel.floatValue = value
            }
        }
    }

    fun startDismiss() {
        onDragDismissRequest?.invoke()
    }

    private suspend fun animateToTarget(
        targetWidth: Float,
        targetHeight: Float,
        targetOffsetX: Float,
        targetOffsetY: Float,
    ) {
        cancelAnimation()
        val startWidth = currentWidthPixel
        val startHeight = currentHeightPixel
        val startOffsetX = currentOffsetXPixel
        val startOffsetY = currentOffsetYPixel
        if (startWidth != targetWidth || startHeight != targetHeight
            || startOffsetX != targetOffsetX || startOffsetY != targetOffsetY
        ) {
            val widthDiff = targetWidth - startWidth
            val heightDiff = targetHeight - startHeight
            val offsetXDiff = targetOffsetX - startOffsetX
            val offsetYDiff = targetOffsetY - startOffsetY
            val anim = AnimationState(initialValue = 0f)
            anim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = ImageViewerDefault.ANIMATION_DURATION),
            ) {
                scaleAnimation = this
                val progress = value
                if (widthDiff != 0F) {
                    _currentWidthPixel.floatValue = startWidth + widthDiff * progress
                }
                if (heightDiff != 0F) {
                    _currentHeightPixel.floatValue = startHeight + heightDiff * progress
                }
                if (offsetXDiff != 0F) {
                    _currentOffsetXPixel.floatValue = startOffsetX + offsetXDiff * progress
                }
                if (offsetYDiff != 0F) {
                    _currentOffsetYPixel.floatValue = startOffsetY + offsetYDiff * progress
                }
            }
        }
    }

    private fun cancelAnimation() {
        scaleAnimation?.takeIf { it.isRunning }?.cancelAnimation()
        scaleAnimation = null
        flingAnimation?.takeIf { it.isRunning }?.cancelAnimation()
        flingAnimation = null
        resumeOffsetYAnimation?.takeIf { it.isRunning }?.cancelAnimation()
        resumeOffsetYAnimation = null
    }

    internal fun zoom(centroid: Offset, zoom: Float) {
        val newWidth = (currentWidthPixel * zoom).coerceInWidth()
        val newHeight = (currentHeightPixel * zoom).coerceInHeight()
        val xRatio = (centroid.x - currentOffsetXPixel) / currentWidthPixel
        val yRatio = (centroid.y - currentOffsetYPixel) / currentHeightPixel
        _currentWidthPixel.floatValue = newWidth
        _currentHeightPixel.floatValue = newHeight
        val xOffset = -(newWidth * xRatio - centroid.x)
        val yOffset = -(newHeight * yRatio - centroid.y)
        val bounds = calculateDragBounds(newWidth, newHeight)
        _currentOffsetYPixel.floatValue = bounds.coerceInY(yOffset)
        if (newWidth == standardWidth) {
            _currentOffsetXPixel.floatValue = 0F
        } else {
            _currentOffsetXPixel.floatValue = bounds.coerceInX(xOffset)
        }
    }

    private fun calculateDragBounds(imageWidth: Float, imageHeight: Float): Bounds {
        val left: Float
        val right: Float
        if (imageWidth > layoutSize.width) {
            left = -(imageWidth - layoutSize.width)
            right = 0F
        } else {
            left = (layoutSize.width - imageWidth) / 2F
            right = left
        }
        val top: Float
        val bottom: Float
        if (imageHeight > layoutSize.height) {
            top = -(imageHeight - layoutSize.height)
            bottom = 0F
        } else {
            top = (layoutSize.height - imageHeight) / 2F
            bottom = top
        }
        return Bounds(
            left = left,
            top = top,
            right = right,
            bottom = bottom,
        )
    }

    private fun Float.coerceInWidth(): Float {
        val maxWidth = standardWidth * maximumScale
        return coerceAtLeast(standardWidth).coerceAtMost(maxWidth)
    }

    private fun Float.coerceInHeight(): Float {
        val maxHeight = standardHeight * maximumScale
        return coerceAtLeast(standardHeight).coerceAtMost(maxHeight)
    }

    internal companion object {

        val Saver: Saver<ImageViewerState, *> = listSaver(
            save = {
                listOf<Any>(
                    it.minimumScale,
                    it.maximumScale,
                )
            },
            restore = {
                ImageViewerState(
                    minimumScale = it[0] as Float,
                    maximumScale = it[1] as Float,
                )
            }
        )
    }
}

private object ImageViewerDefault {

    const val ANIMATION_DURATION = 200
}

private fun Float.equalsExactly(target: Float): Boolean {
    return abs(target - this) <= 0.000001F
}

private fun Velocity.toOffset() = Offset(x = x, y = y)
