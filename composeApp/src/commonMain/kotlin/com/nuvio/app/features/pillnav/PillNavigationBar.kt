package com.nuvio.app.features.pillnav

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nuvio.app.features.profiles.ActiveProfileMiniAvatar
import com.nuvio.app.features.profiles.AvatarRepository
import com.nuvio.app.features.profiles.ProfileRepository
import kotlinx.coroutines.coroutineScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuvio.app.AppScreenTab
import com.nuvio.app.features.profiles.NuvioProfile
import com.nuvio.app.features.profiles.ProfileSwitcherTab
import dev.chrisbanes.haze.HazeState
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.compose_nav_home
import nuvio.composeapp.generated.resources.compose_nav_library
import nuvio.composeapp.generated.resources.compose_nav_search
import nuvio.composeapp.generated.resources.compose_settings_page_root
import org.jetbrains.compose.resources.stringResource

private object PillNavTokens {
    val barHeight = 48.dp
    val barTopGap = 8.dp
    val barSideMargin = 12.dp
    val barMaxWidth = 880.dp
    val innerPadding = 5.dp
    val iconItemSize = 38.dp
    val iconSize = 22.dp
    const val avatarSize = 30
    val labelSize = 15.sp
    const val unselectedAlpha = 0.84f
    const val hideScrollThreshold = 48f
}

/** Top padding tab content needs so it starts below the pill (the home tab draws under it). */
internal val pillNavContentTopPadding: Dp =
    PillNavTokens.barTopGap + PillNavTokens.barHeight + PillNavTokens.barTopGap

/** Hides the pill while home scrolls down and brings it back on the way up. Never consumes scroll. */
@Stable
internal class PillNavState {
    var hiddenByScroll by mutableStateOf(false)
        private set

    private var accumulated = 0f

    fun show() {
        hiddenByScroll = false
        accumulated = 0f
    }

    val nestedScrollConnection: NestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            val dy = available.y
            if (dy == 0f) return Offset.Zero
            if ((dy < 0f) != (accumulated < 0f)) accumulated = 0f
            accumulated += dy
            if (accumulated < -PillNavTokens.hideScrollThreshold && !hiddenByScroll) {
                hiddenByScroll = true
                accumulated = 0f
            } else if (accumulated > PillNavTokens.hideScrollThreshold && hiddenByScroll) {
                hiddenByScroll = false
                accumulated = 0f
            }
            return Offset.Zero
        }

        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
            // Overscrolling at the top always reveals the pill.
            if (available.y > 0f) show()
            return Offset.Zero
        }
    }
}

private class PillTab(val tab: AppScreenTab, val label: String)

/**
 * The selection highlight as two independently sprung edges: the leading edge races ahead and the
 * trailing edge follows, so the lens stretches and thins in flight and settles back into a pill.
 */
@Stable
private class LiquidIndicator {
    val left = Animatable(0f)
    val right = Animatable(0f)
    var placed by mutableStateOf(false)
        private set
    private var restWidth = 1f

    suspend fun moveTo(targetLeft: Float, targetRight: Float) {
        restWidth = (targetRight - targetLeft).coerceAtLeast(1f)
        if (!placed) {
            left.snapTo(targetLeft)
            right.snapTo(targetRight)
            placed = true
            return
        }
        val movingRight = targetLeft > left.value
        val lead = spring<Float>(dampingRatio = 0.72f, stiffness = 520f)
        val trail = spring<Float>(dampingRatio = 0.86f, stiffness = 210f)
        coroutineScope {
            launch { left.animateTo(targetLeft, if (movingRight) trail else lead) }
            launch { right.animateTo(targetRight, if (movingRight) lead else trail) }
        }
    }

    /** 0 at rest, towards 1 while stretched in flight. */
    fun stretch(): Float = ((right.value - left.value) / restWidth - 1f).coerceIn(0f, 1.5f) / 1.5f

    /** The lens in row coordinates: thinner while stretched in flight, a little taller and wider while pressed. */
    fun bounds(rowHeight: Float, press: Float): Rect {
        val height = rowHeight * (1f - 0.16f * stretch()) * (1f + 0.12f * press)
        val grow = rowHeight * 0.06f * press
        val top = (rowHeight - height) / 2f
        return Rect(left.value - grow, top, right.value + grow, top + height)
    }
}

private fun DrawScope.drawLiquidIndicator(indicator: LiquidIndicator, press: Float, refracts: Boolean) {
    if (!indicator.placed) return
    val bounds = indicator.bounds(size.height, press)
    val height = bounds.height
    val top = bounds.top
    val width = bounds.width
    val topLeft = bounds.topLeft
    val lensSize = Size(width, height)
    val radius = CornerRadius(height / 2f)
    // Where the glass refracts, the lens and its rim come from the shader; this only adds a whisper of frost.
    val fill = if (refracts) 0.35f else 1f
    // Glass lens: a bright top falling to a soft base, a specular rim, and a faint inner glow.
    drawRoundRect(
        brush = Brush.verticalGradient(
            0f to Color.White.copy(alpha = 0.30f * fill),
            0.55f to Color.White.copy(alpha = 0.16f * fill),
            1f to Color.White.copy(alpha = 0.22f * fill),
            startY = top,
            endY = top + height,
        ),
        topLeft = topLeft,
        size = lensSize,
        cornerRadius = radius,
    )
    if (refracts) return
    val rim = 1.dp.toPx()
    drawRoundRect(
        brush = Brush.verticalGradient(
            0f to Color.White.copy(alpha = 0.62f),
            0.5f to Color.White.copy(alpha = 0.08f),
            1f to Color.White.copy(alpha = 0.28f),
            startY = top,
            endY = top + height,
        ),
        topLeft = Offset(topLeft.x + rim / 2f, top + rim / 2f),
        size = Size(width - rim, height - rim),
        cornerRadius = CornerRadius((height - rim) / 2f),
        style = Stroke(rim),
    )
}

@Composable
private fun PillTextItem(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    horizontalPadding: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val contentAlpha = animateFloatAsState(
        targetValue = if (selected) 1f else PillNavTokens.unselectedAlpha,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "pill_nav_label_alpha",
    )
    val pressScale = pressScale(interactionSource)
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(CircleShape)
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.Tab,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .graphicsLayer {
                alpha = contentAlpha.value
                scaleX = pressScale.value
                scaleY = pressScale.value
            }
            .padding(horizontal = horizontalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = PillNavTokens.labelSize,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.sp,
            ),
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Composable
private fun PillIconItem(
    icon: ImageVector,
    contentDescription: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val contentAlpha = animateFloatAsState(
        targetValue = if (selected) 1f else PillNavTokens.unselectedAlpha,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "pill_nav_icon_alpha",
    )
    val pressScale = pressScale(interactionSource)
    Box(
        modifier = modifier
            .size(PillNavTokens.iconItemSize)
            .clip(CircleShape)
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.Tab,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .graphicsLayer {
                alpha = contentAlpha.value
                scaleX = pressScale.value
                scaleY = pressScale.value
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(PillNavTokens.iconSize),
        )
    }
}

@Composable
private fun pressScale(interactionSource: MutableInteractionSource): State<Float> {
    val pressed by interactionSource.collectIsPressedAsState()
    return animateFloatAsState(
        targetValue = if (pressed) 0.93f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "pill_nav_press",
    )
}
