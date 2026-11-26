package com.omlakhera.kronoza.ui.components

import android.provider.Settings
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Centralized motion language for Kronoza.
 * Ensures calm, purposeful transitions while respecting system reduced-motion preferences.
 */
object KronozaMotion {
    const val DURATION_FAST = 150
    const val DURATION_MEDIUM = 250
    const val DURATION_SLOW = 360
    const val DURATION_CHART = 520

    val EaseStandard = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val EaseEmphasized = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

    fun <T> colorTween(reducedMotion: Boolean = false) =
        tween<T>(durationMillis = if (reducedMotion) 0 else DURATION_MEDIUM, easing = EaseStandard)

    fun <T> fastTween(reducedMotion: Boolean = false) =
        tween<T>(durationMillis = if (reducedMotion) 0 else DURATION_FAST, easing = EaseStandard)

    fun <T> mediumTween(reducedMotion: Boolean = false) =
        tween<T>(durationMillis = if (reducedMotion) 0 else DURATION_MEDIUM, easing = EaseEmphasized)

    fun <T> chartTween(reducedMotion: Boolean = false) =
        tween<T>(durationMillis = if (reducedMotion) 0 else DURATION_CHART, easing = FastOutSlowInEasing)

    fun <T> gentleSpring(reducedMotion: Boolean = false) = if (reducedMotion) {
        tween<T>(durationMillis = 0)
    } else {
        spring<T>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    }

    fun <T> tactileSpring(reducedMotion: Boolean = false) = if (reducedMotion) {
        tween<T>(durationMillis = 0)
    } else {
        spring<T>(
            dampingRatio = 0.68f,
            stiffness = Spring.StiffnessMedium
        )
    }

    fun navEnter(reducedMotion: Boolean): EnterTransition = if (reducedMotion) {
        EnterTransition.None
    } else {
        fadeIn(tween(DURATION_MEDIUM, easing = EaseStandard)) +
            slideInHorizontally(tween(DURATION_MEDIUM, easing = EaseEmphasized)) { it / 18 }
    }

    fun navExit(reducedMotion: Boolean): ExitTransition = if (reducedMotion) {
        ExitTransition.None
    } else {
        fadeOut(tween(DURATION_FAST, easing = EaseStandard)) +
            slideOutHorizontally(tween(DURATION_FAST, easing = EaseStandard)) { -it / 24 }
    }

    fun navPopEnter(reducedMotion: Boolean): EnterTransition = if (reducedMotion) {
        EnterTransition.None
    } else {
        fadeIn(tween(DURATION_MEDIUM, easing = EaseStandard)) +
            slideInHorizontally(tween(DURATION_MEDIUM, easing = EaseEmphasized)) { -it / 18 }
    }

    fun navPopExit(reducedMotion: Boolean): ExitTransition = if (reducedMotion) {
        ExitTransition.None
    } else {
        fadeOut(tween(DURATION_FAST, easing = EaseStandard)) +
            slideOutHorizontally(tween(DURATION_FAST, easing = EaseStandard)) { it / 18 }
    }

    fun directionalSlideTransform(direction: Int, reducedMotion: Boolean): ContentTransform {
        if (reducedMotion) {
            return EnterTransition.None togetherWith ExitTransition.None
        }
        val sign = if (direction >= 0) 1 else -1
        return (fadeIn(tween(DURATION_MEDIUM)) +
            slideInHorizontally(tween(DURATION_MEDIUM, easing = EaseEmphasized)) { sign * it / 6 })
            .togetherWith(
                fadeOut(tween(DURATION_FAST)) +
                    slideOutHorizontally(tween(DURATION_FAST)) { -sign * it / 8 }
            )
    }
}

val LocalReducedMotion = staticCompositionLocalOf { false }

@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            val scale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            )
            scale == 0f
        }.getOrDefault(false)
    }
}

@Composable
fun ProvideKronozaMotion(content: @Composable () -> Unit) {
    val reduced = rememberReducedMotion()
    CompositionLocalProvider(LocalReducedMotion provides reduced, content = content)
}

fun Modifier.kronozaPressScale(
    enabled: Boolean = true,
    pressedScale: Float = 0.975f
): Modifier = composed {
    val reduced = LocalReducedMotion.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && !reduced) pressedScale else 1f,
        animationSpec = KronozaMotion.tactileSpring(reduced),
        label = "press_scale"
    )
    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

@Composable
fun KronozaStaggeredReveal(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val reduced = LocalReducedMotion.current
    if (reduced) {
        Box(modifier = modifier) { content() }
        return
    }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay((index.coerceIn(0, 6) * 35L))
        progress.animateTo(1f, tween(KronozaMotion.DURATION_MEDIUM, easing = KronozaMotion.EaseEmphasized))
    }
    Box(
        modifier = modifier.graphicsLayer {
            alpha = progress.value
            translationY = (1f - progress.value) * 18f
        }
    ) {
        content()
    }
}

@Composable
fun KronozaAnimatedCheckbox(
    checked: Boolean,
    accentColor: Color,
    borderColor: Color,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    boxSize: Dp = 24.dp
) {
    val reduced = LocalReducedMotion.current
    val scale by animateFloatAsState(
        targetValue = if (checked) 1f else 0.94f,
        animationSpec = KronozaMotion.tactileSpring(reduced),
        label = "check_scale"
    )
    val fillColor by animateColorAsState(
        targetValue = if (checked) accentColor else Color.Transparent,
        animationSpec = KronozaMotion.fastTween(reduced),
        label = "check_fill"
    )
    val strokeColor by animateColorAsState(
        targetValue = if (checked) accentColor else borderColor,
        animationSpec = KronozaMotion.fastTween(reduced),
        label = "check_stroke"
    )

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clickable(role = Role.Checkbox, onClick = onToggle),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = fillColor,
            border = BorderStroke(1.8.dp, strokeColor),
            modifier = Modifier
                .size(boxSize)
                .scale(scale)
        ) {
            Box(contentAlignment = Alignment.Center) {
                AnimatedVisibility(
                    visible = checked,
                    enter = scaleIn(KronozaMotion.tactileSpring(reduced)) + fadeIn(KronozaMotion.fastTween(reduced)),
                    exit = scaleOut(KronozaMotion.fastTween(reduced)) + fadeOut(KronozaMotion.fastTween(reduced))
                ) {
                    KronozaIcon(
                        imageVector = KronozaIcons.Check,
                        contentDescription = null,
                        tint = Color.White,
                        size = boxSize * 0.58f
                    )
                }
            }
        }
    }
}
