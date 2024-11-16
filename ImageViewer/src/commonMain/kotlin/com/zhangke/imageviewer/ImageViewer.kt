package com.zhangke.imageviewer

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.launch

private val infinityConstraints = Constraints()

@Composable
fun ImageViewer(
    modifier: Modifier = Modifier,
    state: ImageViewerState = rememberImageViewerState(),
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    var latestSize: Size? by remember {
        mutableStateOf(null)
    }
    Layout(
        modifier = modifier
            .onGloballyPositioned { position ->
                val currentSize = position.size.toSize()
                if (currentSize != latestSize) {
                    state.updateLayoutSize(currentSize)
                    latestSize = currentSize
                }
            }
            .pointerInput(state) {
                detectTapGestures(
                    onDoubleTap = {
                        if (state.exceed) {
                            coroutineScope.launch {
                                state.animateToStandard()
                            }
                        } else {
                            coroutineScope.launch {
                                state.animateToBig(it)
                            }
                        }
                    },
                    onTap = {
                        state.startDismiss()
                    },
                )
            }
            .draggableInfinity(
                exceed = state.exceed,
                isBigVerticalImage = state.isBigVerticalImage,
                onDrag = { offset ->
                    state.drag(offset)
                },
                onDragStopped = { velocity ->
                    coroutineScope.launch {
                        state.dragStop(velocity)
                    }
                },
            )
            .pointerInput(state) {
                detectZoom { centroid, zoom ->
                    state.zoom(centroid, zoom)
                }
            },
        content = {
            Layout(
                modifier = Modifier
                    .offset(
                        x = state.currentOffsetXPixel.pxToDp(density),
                        y = state.currentOffsetYPixel.pxToDp(density),
                    )
                    .width(state.currentWidthPixel.pxToDp(density))
                    .height(state.currentHeightPixel.pxToDp(density)),
                content = {
                    content()
                },
            ) { measurables, constraints ->
                if (measurables.size > 1) {
                    throw IllegalStateException("ImageViewer is only allowed to have one children!")
                }
                val firstMeasurable = measurables.first()
                val placeable = firstMeasurable.measure(constraints)
                val minWidth = firstMeasurable.minIntrinsicWidth(100)
                val minHeight = firstMeasurable.minIntrinsicHeight(100)
                if (minWidth > 0 && minHeight > 0) {
                    state.setImageAspectRatio(minWidth / minHeight.toFloat())
                }
                layout(constraints.maxWidth, constraints.maxHeight) {
                    placeable.placeRelative(0, 0)
                }
            }
        },
    ) { measurables, constraints ->
        val placeable = measurables.first().measure(infinityConstraints)
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable.placeRelative(0, 0)
        }
    }
}

private fun Modifier.draggableInfinity(
    exceed: Boolean,
    isBigVerticalImage: Boolean,
    onDrag: (dragAmount: Offset) -> Unit,
    onDragStopped: (velocity: Velocity) -> Unit,
): Modifier {
    val velocityTracker = VelocityTracker()
    return Modifier.pointerInput(exceed || isBigVerticalImage) {
        if (exceed) {
            detectDragGestures(
                onDrag = { change, dragAmount ->
                    velocityTracker.addPointerInputChange(change)
                    onDrag(dragAmount)
                },
                onDragEnd = {
                    val velocity = velocityTracker.calculateVelocity()
                    onDragStopped(velocity)
                },
                onDragCancel = {
                    val velocity = velocityTracker.calculateVelocity()
                    onDragStopped(velocity)
                },
            )
        } else {
            detectVerticalDragGestures(
                onVerticalDrag = { change, dragAmount ->
                    velocityTracker.addPointerInputChange(change)
                    onDrag(Offset(x = 0F, y = dragAmount))
                },
                onDragEnd = {
                    val velocity = velocityTracker.calculateVelocity()
                    onDragStopped(velocity)
                },
                onDragCancel = {
                    val velocity = velocityTracker.calculateVelocity()
                    onDragStopped(velocity)
                },
            )
        }
    } then this
}

private fun Float.pxToDp(density: Density): Dp {
    val pxValue = this
    return with(density) { pxValue.toDp() }
}
