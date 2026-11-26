package com.omlakhera.kronoza.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.omlakhera.kronoza.data.local.EventEntity
import com.omlakhera.kronoza.model.AccentPreset
import com.omlakhera.kronoza.model.EventCategory
import com.omlakhera.kronoza.model.ThemeMode
import com.omlakhera.kronoza.ui.components.KronozaMotion
import com.omlakhera.kronoza.ui.components.LocalReducedMotion
import com.omlakhera.kronoza.ui.components.ProvideKronozaMotion
import com.omlakhera.kronoza.util.CalendarEngine

@Immutable
data class KronozaExtendedColors(
    val isDark: Boolean,
    val canvas: Color,
    val card: Color,
    val elevatedCard: Color,
    val cardBorder: Color,
    val mutedSurface: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val lavenderAccent: Color,
    val lavenderContainer: Color,
    val onLavenderContainer: Color,
    val skyBlueAccent: Color,
    val skyBlueContainer: Color,
    val onSkyBlueContainer: Color,
    val mintAccent: Color,
    val mintContainer: Color,
    val onMintContainer: Color,
    val coralAccent: Color,
    val coralContainer: Color,
    val onCoralContainer: Color,
) {
    fun categoryAccent(category: EventCategory): Color = when (category) {
        EventCategory.WORK -> lavenderAccent
        EventCategory.DEEP_WORK -> skyBlueAccent
        EventCategory.PERSONAL -> mintAccent
        EventCategory.DEADLINE -> coralAccent
    }

    fun categoryContainer(category: EventCategory): Color = when (category) {
        EventCategory.WORK -> lavenderContainer
        EventCategory.DEEP_WORK -> skyBlueContainer
        EventCategory.PERSONAL -> mintContainer
        EventCategory.DEADLINE -> coralContainer
    }

    fun onCategoryContainer(category: EventCategory): Color = when (category) {
        EventCategory.WORK -> onLavenderContainer
        EventCategory.DEEP_WORK -> onSkyBlueContainer
        EventCategory.PERSONAL -> onMintContainer
        EventCategory.DEADLINE -> onCoralContainer
    }

    fun eventAccent(event: EventEntity): Color {
        val custom = CalendarEngine.parseHexColorOrNull(event.customColorHex)
        return custom ?: categoryAccent(event.category)
    }

    fun eventContainer(event: EventEntity): Color {
        val custom = CalendarEngine.parseHexColorOrNull(event.customColorHex)
        return if (custom != null) {
            custom.copy(alpha = if (isDark) 0.22f else 0.14f)
        } else {
            categoryContainer(event.category)
        }
    }

    fun onEventContainer(event: EventEntity): Color {
        val custom = CalendarEngine.parseHexColorOrNull(event.customColorHex)
        return if (custom != null) {
            if (isDark) custom.copy(alpha = 0.95f) else custom
        } else {
            onCategoryContainer(event.category)
        }
    }
}

private val LightExtendedColors = KronozaExtendedColors(
    isDark = false,
    canvas = CanvasLight,
    card = SurfaceWhite,
    elevatedCard = SurfaceWhite,
    cardBorder = BorderSubtleLight,
    mutedSurface = SurfaceContainerLowLight,
    textPrimary = TextPrimaryLight,
    textSecondary = TextSecondaryLight,
    lavenderAccent = LavenderAccent,
    lavenderContainer = LavenderContainer,
    onLavenderContainer = OnLavenderContainer,
    skyBlueAccent = SkyBlueAccent,
    skyBlueContainer = SkyBlueContainer,
    onSkyBlueContainer = OnSkyBlueContainer,
    mintAccent = MintAccent,
    mintContainer = MintContainer,
    onMintContainer = OnMintContainer,
    coralAccent = CoralAccent,
    coralContainer = CoralContainer,
    onCoralContainer = OnCoralContainer
)

private val DarkExtendedColors = KronozaExtendedColors(
    isDark = true,
    canvas = CanvasDark,
    card = SurfaceDark,
    elevatedCard = SurfaceContainerDark,
    cardBorder = BorderSubtleDark,
    mutedSurface = SurfaceContainerLowDark,
    textPrimary = TextPrimaryDark,
    textSecondary = TextSecondaryDark,
    lavenderAccent = LavenderAccentDark,
    lavenderContainer = LavenderContainerDark,
    onLavenderContainer = LavenderAccentDark,
    skyBlueAccent = SkyBlueAccentDark,
    skyBlueContainer = SkyBlueContainerDark,
    onSkyBlueContainer = SkyBlueAccentDark,
    mintAccent = MintAccentDark,
    mintContainer = MintContainerDark,
    onMintContainer = MintAccentDark,
    coralAccent = CoralAccentDark,
    coralContainer = CoralContainerDark,
    onCoralContainer = CoralAccentDark
)

val LocalKronozaColors = staticCompositionLocalOf { LightExtendedColors }

val KronozaShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun KronozaTheme(
    themeMode: ThemeMode = ThemeMode.LIGHT,
    accentPreset: AccentPreset = AccentPreset.LAVENDER,
    content: @Composable () -> Unit
) {
    ProvideKronozaMotion {
        val reducedMotion = LocalReducedMotion.current
        val isDark = when (themeMode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> isSystemInDarkTheme()
        }

        val targetPrimary = when (accentPreset) {
            AccentPreset.LAVENDER -> if (isDark) PrimaryBrandDark else PrimaryBrand
            AccentPreset.SKY_BLUE -> if (isDark) SkyBlueAccentDark else SkyBlueAccent
            AccentPreset.MINT -> if (isDark) MintAccentDark else MintAccent
            AccentPreset.CORAL -> if (isDark) CoralAccentDark else CoralAccent
        }

        val targetPrimaryContainer = when (accentPreset) {
            AccentPreset.LAVENDER -> if (isDark) LavenderContainerDark else LavenderContainer
            AccentPreset.SKY_BLUE -> if (isDark) SkyBlueContainerDark else SkyBlueContainer
            AccentPreset.MINT -> if (isDark) MintContainerDark else MintContainer
            AccentPreset.CORAL -> if (isDark) CoralContainerDark else CoralContainer
        }

        val spec = KronozaMotion.colorTween<Color>(reducedMotion)
        val primaryColor by animateColorAsState(targetPrimary, spec, label = "theme_primary")
        val primaryContainer by animateColorAsState(targetPrimaryContainer, spec, label = "theme_primary_container")

        val baseExt = if (isDark) DarkExtendedColors else LightExtendedColors
        val animCanvas by animateColorAsState(baseExt.canvas, spec, label = "theme_canvas")
        val animCard by animateColorAsState(baseExt.card, spec, label = "theme_card")
        val animElevated by animateColorAsState(baseExt.elevatedCard, spec, label = "theme_elevated")
        val animBorder by animateColorAsState(baseExt.cardBorder, spec, label = "theme_border")
        val animMuted by animateColorAsState(baseExt.mutedSurface, spec, label = "theme_muted")
        val animTextPrimary by animateColorAsState(baseExt.textPrimary, spec, label = "theme_text_primary")
        val animTextSecondary by animateColorAsState(baseExt.textSecondary, spec, label = "theme_text_secondary")

        val extended = baseExt.copy(
            canvas = animCanvas,
            card = animCard,
            elevatedCard = animElevated,
            cardBorder = animBorder,
            mutedSurface = animMuted,
            textPrimary = animTextPrimary,
            textSecondary = animTextSecondary
        )

        val onPrimary = if (isDark) Color(0xFF101326) else Color.White

        val colorScheme = if (isDark) {
            darkColorScheme(
                primary = primaryColor,
                onPrimary = onPrimary,
                primaryContainer = primaryContainer,
                onPrimaryContainer = primaryColor,
                secondary = SkyBlueAccentDark,
                onSecondary = Color(0xFF0A1F38),
                secondaryContainer = SkyBlueContainerDark,
                onSecondaryContainer = SkyBlueAccentDark,
                tertiary = MintAccentDark,
                onTertiary = Color(0xFF07281C),
                tertiaryContainer = MintContainerDark,
                onTertiaryContainer = MintAccentDark,
                background = animCanvas,
                onBackground = animTextPrimary,
                surface = animCard,
                onSurface = animTextPrimary,
                surfaceVariant = animElevated,
                onSurfaceVariant = animTextSecondary,
                surfaceContainerLowest = animCard,
                surfaceContainerLow = animMuted,
                surfaceContainer = animElevated,
                surfaceContainerHigh = SurfaceContainerHighDark,
                outline = OutlineDark,
                outlineVariant = animBorder,
                error = CoralAccentDark,
                errorContainer = CoralContainerDark
            )
        } else {
            lightColorScheme(
                primary = primaryColor,
                onPrimary = onPrimary,
                primaryContainer = primaryContainer,
                onPrimaryContainer = if (accentPreset == AccentPreset.LAVENDER) OnLavenderContainer else primaryColor,
                secondary = SkyBlueAccent,
                onSecondary = Color.White,
                secondaryContainer = SkyBlueContainer,
                onSecondaryContainer = OnSkyBlueContainer,
                tertiary = MintAccent,
                onTertiary = Color.White,
                tertiaryContainer = MintContainer,
                onTertiaryContainer = OnMintContainer,
                background = animCanvas,
                onBackground = animTextPrimary,
                surface = animCard,
                onSurface = animTextPrimary,
                surfaceVariant = SurfaceContainerLight,
                onSurfaceVariant = animTextSecondary,
                surfaceContainerLowest = animCard,
                surfaceContainerLow = animMuted,
                surfaceContainer = SurfaceContainerLight,
                surfaceContainerHigh = SurfaceContainerHighLight,
                outline = OutlineLight,
                outlineVariant = animBorder,
                error = CoralAccent,
                errorContainer = CoralContainer
            )
        }

        CompositionLocalProvider(LocalKronozaColors provides extended) {
            MaterialTheme(
                colorScheme = colorScheme,
                typography = KronozaTypography,
                shapes = KronozaShapes,
                content = content
            )
        }
    }
}
