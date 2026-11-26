package com.omlakhera.kronoza.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Centralized geometric UI icon pack for Kronoza.
 * Built with uniform 24x24 vector grid, 1.85dp rounded strokes, and paired filled/outline states
 * for navigation, calendar views, events, tasks, insights, and settings.
 */
object KronozaIcons {

    private fun strokeIcon(
        name: String,
        block: ImageVector.Builder.() -> Unit
    ): ImageVector = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply(block).build()

    private fun ImageVector.Builder.strokePath(
        strokeWidth: Float = 1.85f,
        pathBuilder: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit
    ) {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = strokeWidth,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathBuilder = pathBuilder
        )
    }

    private fun ImageVector.Builder.fillPath(
        pathBuilder: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit
    ) {
        path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.NonZero,
            pathBuilder = pathBuilder
        )
    }

    // ─── Navigation Icons ────────────────────────────────────────────────────────
    val HomeOutline: ImageVector by lazy {
        strokeIcon("KronozaHomeOutline") {
            strokePath {
                moveTo(3.5f, 10.5f)
                lineTo(12f, 3.5f)
                lineTo(20.5f, 10.5f)
                verticalLineTo(19f)
                curveTo(20.5f, 20.1f, 19.6f, 21f, 18.5f, 21f)
                horizontalLineTo(5.5f)
                curveTo(4.4f, 21f, 3.5f, 20.1f, 3.5f, 19f)
                close()
                moveTo(9.5f, 21f)
                verticalLineTo(14.5f)
                curveTo(9.5f, 13.7f, 10.2f, 13f, 11f, 13f)
                horizontalLineTo(13f)
                curveTo(13.8f, 13f, 14.5f, 13.7f, 14.5f, 14.5f)
                verticalLineTo(21f)
            }
        }
    }

    val HomeFilled: ImageVector by lazy {
        strokeIcon("KronozaHomeFilled") {
            fillPath {
                moveTo(11.37f, 2.74f)
                curveTo(11.74f, 2.43f, 12.26f, 2.43f, 12.63f, 2.74f)
                lineTo(20.63f, 9.34f)
                curveTo(21.18f, 9.8f, 21.5f, 10.48f, 21.5f, 11.19f)
                verticalLineTo(19f)
                curveTo(21.5f, 20.66f, 20.16f, 22f, 18.5f, 22f)
                horizontalLineTo(15f)
                curveTo(14.45f, 22f, 14f, 21.55f, 14f, 21f)
                verticalLineTo(15.5f)
                curveTo(14f, 14.95f, 13.55f, 14.5f, 13f, 14.5f)
                horizontalLineTo(11f)
                curveTo(10.45f, 14.5f, 10f, 14.95f, 10f, 15.5f)
                verticalLineTo(21f)
                curveTo(10f, 21.55f, 9.55f, 22f, 9f, 22f)
                horizontalLineTo(5.5f)
                curveTo(3.84f, 22f, 2.5f, 20.66f, 2.5f, 19f)
                verticalLineTo(11.19f)
                curveTo(2.5f, 10.48f, 2.82f, 9.8f, 3.37f, 9.34f)
                close()
            }
        }
    }

    val CalendarOutline: ImageVector by lazy {
        strokeIcon("KronozaCalendarOutline") {
            strokePath {
                moveTo(5.5f, 4.5f)
                horizontalLineTo(18.5f)
                curveTo(19.6f, 4.5f, 20.5f, 5.4f, 20.5f, 6.5f)
                verticalLineTo(18.5f)
                curveTo(20.5f, 19.6f, 19.6f, 20.5f, 18.5f, 20.5f)
                horizontalLineTo(5.5f)
                curveTo(4.4f, 20.5f, 3.5f, 19.6f, 3.5f, 18.5f)
                verticalLineTo(6.5f)
                curveTo(3.5f, 5.4f, 4.4f, 4.5f, 5.5f, 4.5f)
                close()
                moveTo(3.5f, 9.5f)
                horizontalLineTo(20.5f)
                moveTo(8f, 2.5f)
                verticalLineTo(5.5f)
                moveTo(16f, 2.5f)
                verticalLineTo(5.5f)
                moveTo(8f, 13.5f)
                horizontalLineTo(10f)
                moveTo(14f, 13.5f)
                horizontalLineTo(16f)
                moveTo(8f, 17f)
                horizontalLineTo(10f)
            }
        }
    }

    val CalendarFilled: ImageVector by lazy {
        strokeIcon("KronozaCalendarFilled") {
            fillPath {
                moveTo(8f, 2f)
                curveTo(8.55f, 2f, 9f, 2.45f, 9f, 3f)
                verticalLineTo(4f)
                horizontalLineTo(15f)
                verticalLineTo(3f)
                curveTo(15f, 2.45f, 15.45f, 2f, 16f, 2f)
                curveTo(16.55f, 2f, 17f, 2.45f, 17f, 3f)
                verticalLineTo(4f)
                horizontalLineTo(18.5f)
                curveTo(20.16f, 4f, 21.5f, 5.34f, 21.5f, 7f)
                verticalLineTo(9f)
                horizontalLineTo(2.5f)
                verticalLineTo(7f)
                curveTo(2.5f, 5.34f, 3.84f, 4f, 5.5f, 4f)
                horizontalLineTo(7f)
                verticalLineTo(3f)
                curveTo(7f, 2.45f, 7.45f, 2f, 8f, 2f)
                close()
                moveTo(2.5f, 10.5f)
                horizontalLineTo(21.5f)
                verticalLineTo(18.5f)
                curveTo(21.5f, 20.16f, 20.16f, 21.5f, 18.5f, 21.5f)
                horizontalLineTo(5.5f)
                curveTo(3.84f, 21.5f, 2.5f, 20.16f, 2.5f, 18.5f)
                close()
            }
        }
    }

    val TasksOutline: ImageVector by lazy {
        strokeIcon("KronozaTasksOutline") {
            strokePath {
                moveTo(12f, 21f)
                curveTo(16.97f, 21f, 21f, 16.97f, 21f, 12f)
                curveTo(21f, 7.03f, 16.97f, 3f, 12f, 3f)
                curveTo(7.03f, 3f, 3f, 7.03f, 3f, 12f)
                curveTo(3f, 16.97f, 7.03f, 21f, 12f, 21f)
                close()
                moveTo(8.5f, 12.2f)
                lineTo(10.9f, 14.6f)
                lineTo(15.8f, 9.6f)
            }
        }
    }

    val TasksFilled: ImageVector by lazy {
        strokeIcon("KronozaTasksFilled") {
            fillPath {
                moveTo(12f, 2f)
                curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
                curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
                curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
                curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
                close()
                moveTo(16.71f, 9.71f)
                lineTo(11.41f, 15.01f)
                curveTo(11.02f, 15.4f, 10.39f, 15.4f, 10f, 15.01f)
                lineTo(7.29f, 12.3f)
                curveTo(6.9f, 11.91f, 6.9f, 11.28f, 7.29f, 10.89f)
                curveTo(7.68f, 10.5f, 8.31f, 10.5f, 8.7f, 10.89f)
                lineTo(10.71f, 12.9f)
                lineTo(15.3f, 8.3f)
                curveTo(15.69f, 7.91f, 16.32f, 7.91f, 16.71f, 8.3f)
                curveTo(17.1f, 8.69f, 17.1f, 9.32f, 16.71f, 9.71f)
                close()
            }
        }
    }

    val InsightsOutline: ImageVector by lazy {
        strokeIcon("KronozaInsightsOutline") {
            strokePath {
                moveTo(4f, 19.5f)
                horizontalLineTo(20f)
                moveTo(6.5f, 16f)
                verticalLineTo(12f)
                moveTo(12f, 16f)
                verticalLineTo(6.5f)
                moveTo(17.5f, 16f)
                verticalLineTo(9.5f)
            }
        }
    }

    val InsightsFilled: ImageVector by lazy {
        strokeIcon("KronozaInsightsFilled") {
            fillPath {
                moveTo(5f, 11f)
                horizontalLineTo(8f)
                curveTo(8.55f, 11f, 9f, 11.45f, 9f, 12f)
                verticalLineTo(19f)
                curveTo(9f, 19.55f, 8.55f, 20f, 8f, 20f)
                horizontalLineTo(5f)
                curveTo(4.45f, 20f, 4f, 19.55f, 4f, 19f)
                verticalLineTo(12f)
                curveTo(4f, 11.45f, 4.45f, 11f, 5f, 11f)
                close()
                moveTo(10.5f, 5f)
                horizontalLineTo(13.5f)
                curveTo(14.05f, 5f, 14.5f, 5.45f, 14.5f, 6f)
                verticalLineTo(19f)
                curveTo(14.5f, 19.55f, 14.05f, 20f, 13.5f, 20f)
                horizontalLineTo(10.5f)
                curveTo(9.95f, 20f, 9.5f, 19.55f, 9.5f, 19f)
                verticalLineTo(6f)
                curveTo(9.5f, 5.45f, 9.95f, 5f, 10.5f, 5f)
                close()
                moveTo(16f, 8.5f)
                horizontalLineTo(19f)
                curveTo(19.55f, 8.5f, 20f, 8.95f, 20f, 9.5f)
                verticalLineTo(19f)
                curveTo(20f, 19.55f, 19.55f, 20f, 19f, 20f)
                horizontalLineTo(16f)
                curveTo(15.45f, 20f, 15f, 19.55f, 15f, 19f)
                verticalLineTo(9.5f)
                curveTo(15f, 8.95f, 15.45f, 8.5f, 16f, 8.5f)
                close()
            }
        }
    }

    val SettingsOutline: ImageVector by lazy {
        strokeIcon("KronozaSettingsOutline") {
            strokePath {
                moveTo(12f, 15f)
                curveTo(13.66f, 15f, 15f, 13.66f, 15f, 12f)
                curveTo(15f, 10.34f, 13.66f, 9f, 12f, 9f)
                curveTo(10.34f, 9f, 9f, 10.34f, 9f, 12f)
                curveTo(9f, 13.66f, 10.34f, 15f, 12f, 15f)
                close()
                moveTo(19.4f, 15f)
                lineTo(20.5f, 13.5f)
                lineTo(20.5f, 10.5f)
                lineTo(19.4f, 9f)
                lineTo(18f, 7.6f)
                lineTo(16.5f, 5.5f)
                lineTo(13.5f, 4.5f)
                lineTo(12f, 3.5f)
                lineTo(10.5f, 4.5f)
                lineTo(7.5f, 5.5f)
                lineTo(6f, 7.6f)
                lineTo(4.6f, 9f)
                lineTo(3.5f, 10.5f)
                lineTo(3.5f, 13.5f)
                lineTo(4.6f, 15f)
                lineTo(6f, 16.4f)
                lineTo(7.5f, 18.5f)
                lineTo(10.5f, 19.5f)
                lineTo(12f, 20.5f)
                lineTo(13.5f, 19.5f)
                lineTo(16.5f, 18.5f)
                lineTo(18f, 16.4f)
                close()
            }
        }
    }

    val SettingsFilled: ImageVector by lazy {
        strokeIcon("KronozaSettingsFilled") {
            fillPath {
                moveTo(19.43f, 12.98f)
                curveTo(19.47f, 12.66f, 19.5f, 12.34f, 19.5f, 12f)
                curveTo(19.5f, 11.66f, 19.47f, 11.34f, 19.43f, 11.02f)
                lineTo(21.54f, 9.37f)
                curveTo(21.73f, 9.22f, 21.78f, 8.95f, 21.66f, 8.73f)
                lineTo(19.66f, 5.27f)
                curveTo(19.54f, 5.05f, 19.27f, 4.97f, 19.05f, 5.05f)
                lineTo(16.56f, 6.05f)
                curveTo(16.04f, 5.65f, 15.48f, 5.32f, 14.87f, 5.07f)
                lineTo(14.49f, 2.42f)
                curveTo(14.46f, 2.18f, 14.25f, 2f, 14f, 2f)
                horizontalLineTo(10f)
                curveTo(9.75f, 2f, 9.54f, 2.18f, 9.51f, 2.42f)
                lineTo(9.13f, 5.07f)
                curveTo(8.52f, 5.32f, 7.96f, 5.66f, 7.44f, 6.05f)
                lineTo(4.95f, 5.05f)
                curveTo(4.72f, 4.96f, 4.46f, 5.05f, 4.34f, 5.27f)
                lineTo(2.34f, 8.73f)
                curveTo(2.21f, 8.95f, 2.27f, 9.22f, 2.46f, 9.37f)
                lineTo(4.57f, 11.02f)
                curveTo(4.53f, 11.34f, 4.5f, 11.67f, 4.5f, 12f)
                curveTo(4.5f, 12.33f, 4.53f, 12.66f, 4.57f, 12.98f)
                lineTo(2.46f, 14.63f)
                curveTo(2.27f, 14.78f, 2.22f, 15.05f, 2.34f, 15.27f)
                lineTo(4.34f, 18.73f)
                curveTo(4.46f, 18.95f, 4.73f, 19.03f, 4.95f, 18.95f)
                lineTo(7.44f, 17.95f)
                curveTo(7.96f, 18.35f, 8.52f, 18.68f, 9.13f, 18.93f)
                lineTo(9.51f, 21.58f)
                curveTo(9.54f, 21.82f, 9.75f, 22f, 10f, 22f)
                horizontalLineTo(14f)
                curveTo(14.25f, 22f, 14.46f, 21.82f, 14.49f, 21.58f)
                lineTo(14.87f, 18.93f)
                curveTo(15.48f, 18.68f, 16.04f, 18.34f, 16.56f, 17.95f)
                lineTo(19.05f, 18.95f)
                curveTo(19.28f, 19.04f, 19.54f, 18.95f, 19.66f, 18.73f)
                lineTo(21.66f, 15.27f)
                curveTo(21.78f, 15.05f, 21.73f, 14.78f, 21.54f, 14.63f)
                close()
                moveTo(12f, 15.5f)
                curveTo(10.07f, 15.5f, 8.5f, 13.93f, 8.5f, 12f)
                curveTo(8.5f, 10.07f, 10.07f, 8.5f, 12f, 8.5f)
                curveTo(13.93f, 8.5f, 15.5f, 10.07f, 15.5f, 12f)
                curveTo(15.5f, 13.93f, 13.93f, 15.5f, 12f, 15.5f)
                close()
            }
        }
    }

    // ─── General & Action Icons ──────────────────────────────────────────────────
    val Search: ImageVector by lazy {
        strokeIcon("KronozaSearch") {
            strokePath {
                moveTo(11f, 18.5f)
                curveTo(15.14f, 18.5f, 18.5f, 15.14f, 18.5f, 11f)
                curveTo(18.5f, 6.86f, 15.14f, 3.5f, 11f, 3.5f)
                curveTo(6.86f, 3.5f, 3.5f, 6.86f, 3.5f, 11f)
                curveTo(3.5f, 15.14f, 6.86f, 18.5f, 11f, 18.5f)
                close()
                moveTo(20.5f, 20.5f)
                lineTo(16.6f, 16.6f)
            }
        }
    }

    val Bell: ImageVector by lazy {
        strokeIcon("KronozaBell") {
            strokePath {
                moveTo(18f, 9f)
                curveTo(18f, 5.69f, 15.31f, 3f, 12f, 3f)
                curveTo(8.69f, 3f, 6f, 5.69f, 6f, 9f)
                curveTo(6f, 14f, 4f, 16f, 4f, 16f)
                horizontalLineTo(20f)
                curveTo(20f, 16f, 18f, 14f, 18f, 9f)
                close()
                moveTo(13.73f, 19.5f)
                curveTo(13.38f, 20.1f, 12.73f, 20.5f, 12f, 20.5f)
                curveTo(11.27f, 20.5f, 10.62f, 20.1f, 10.27f, 19.5f)
            }
        }
    }

    val Plus: ImageVector by lazy {
        strokeIcon("KronozaPlus") {
            strokePath(strokeWidth = 2.1f) {
                moveTo(12f, 5f)
                verticalLineTo(19f)
                moveTo(5f, 12f)
                horizontalLineTo(19f)
            }
        }
    }

    val Close: ImageVector by lazy {
        strokeIcon("KronozaClose") {
            strokePath(strokeWidth = 2.0f) {
                moveTo(6f, 6f)
                lineTo(18f, 18f)
                moveTo(18f, 6f)
                lineTo(6f, 18f)
            }
        }
    }

    val ArrowBack: ImageVector by lazy {
        strokeIcon("KronozaArrowBack") {
            strokePath(strokeWidth = 2.0f) {
                moveTo(19f, 12f)
                horizontalLineTo(5f)
                moveTo(11f, 6f)
                lineTo(5f, 12f)
                lineTo(11f, 18f)
            }
        }
    }

    val ChevronLeft: ImageVector by lazy {
        strokeIcon("KronozaChevronLeft") {
            strokePath(strokeWidth = 2.0f) {
                moveTo(15f, 6f)
                lineTo(9f, 12f)
                lineTo(15f, 18f)
            }
        }
    }

    val ChevronRight: ImageVector by lazy {
        strokeIcon("KronozaChevronRight") {
            strokePath(strokeWidth = 2.0f) {
                moveTo(9f, 6f)
                lineTo(15f, 12f)
                lineTo(9f, 18f)
            }
        }
    }

    val ChevronDown: ImageVector by lazy {
        strokeIcon("KronozaChevronDown") {
            strokePath(strokeWidth = 2.0f) {
                moveTo(6f, 9f)
                lineTo(12f, 15f)
                lineTo(18f, 9f)
            }
        }
    }

    val Check: ImageVector by lazy {
        strokeIcon("KronozaCheck") {
            strokePath(strokeWidth = 2.2f) {
                moveTo(5f, 12.5f)
                lineTo(9.5f, 17f)
                lineTo(19f, 7.5f)
            }
        }
    }

    val DoneAll: ImageVector by lazy {
        strokeIcon("KronozaDoneAll") {
            strokePath(strokeWidth = 1.9f) {
                moveTo(2.5f, 13f)
                lineTo(6.5f, 17f)
                lineTo(14.5f, 8f)
                moveTo(9.5f, 17f)
                lineTo(12f, 19.5f)
                lineTo(21.5f, 8.5f)
            }
        }
    }

    // ─── Event & Time Icons ──────────────────────────────────────────────────────
    val Clock: ImageVector by lazy {
        strokeIcon("KronozaClock") {
            strokePath {
                moveTo(12f, 21f)
                curveTo(16.97f, 21f, 21f, 16.97f, 21f, 12f)
                curveTo(21f, 7.03f, 16.97f, 3f, 12f, 3f)
                curveTo(7.03f, 3f, 3f, 7.03f, 3f, 12f)
                curveTo(3f, 16.97f, 7.03f, 21f, 12f, 21f)
                close()
                moveTo(12f, 7.5f)
                verticalLineTo(12f)
                lineTo(15f, 14f)
            }
        }
    }

    val LocationPin: ImageVector by lazy {
        strokeIcon("KronozaLocationPin") {
            strokePath {
                moveTo(12f, 21f)
                curveTo(12f, 21f, 18.5f, 15.4f, 18.5f, 10f)
                curveTo(18.5f, 6.41f, 15.59f, 3.5f, 12f, 3.5f)
                curveTo(8.41f, 3.5f, 5.5f, 6.41f, 5.5f, 10f)
                curveTo(5.5f, 15.4f, 12f, 21f, 12f, 21f)
                close()
                moveTo(12f, 12.2f)
                curveTo(13.22f, 12.2f, 14.2f, 11.22f, 14.2f, 10f)
                curveTo(14.2f, 8.78f, 13.22f, 7.8f, 12f, 7.8f)
                curveTo(10.78f, 7.8f, 9.8f, 8.78f, 9.8f, 10f)
                curveTo(9.8f, 11.22f, 10.78f, 12.2f, 12f, 12.2f)
                close()
            }
        }
    }

    val VideoCall: ImageVector by lazy {
        strokeIcon("KronozaVideoCall") {
            strokePath {
                moveTo(4.5f, 7f)
                horizontalLineTo(14.5f)
                curveTo(15.6f, 7f, 16.5f, 7.9f, 16.5f, 9f)
                verticalLineTo(15f)
                curveTo(16.5f, 16.1f, 15.6f, 17f, 14.5f, 17f)
                horizontalLineTo(4.5f)
                curveTo(3.4f, 17f, 2.5f, 16.1f, 2.5f, 15f)
                verticalLineTo(9f)
                curveTo(2.5f, 7.9f, 3.4f, 7f, 4.5f, 7f)
                close()
                moveTo(16.5f, 10.5f)
                lineTo(21.5f, 7.8f)
                verticalLineTo(16.2f)
                lineTo(16.5f, 13.5f)
                close()
            }
        }
    }

    val Repeat: ImageVector by lazy {
        strokeIcon("KronozaRepeat") {
            strokePath {
                moveTo(17f, 3.5f)
                lineTo(20f, 6.5f)
                lineTo(17f, 9.5f)
                moveTo(4f, 11f)
                verticalLineTo(9.5f)
                curveTo(4f, 7.84f, 5.34f, 6.5f, 7f, 6.5f)
                horizontalLineTo(20f)
                moveTo(7f, 20.5f)
                lineTo(4f, 17.5f)
                lineTo(7f, 14.5f)
                moveTo(20f, 13f)
                verticalLineTo(14.5f)
                curveTo(20f, 16.16f, 18.66f, 17.5f, 17f, 17.5f)
                horizontalLineTo(4f)
            }
        }
    }

    val Edit: ImageVector by lazy {
        strokeIcon("KronozaEdit") {
            strokePath {
                moveTo(14.5f, 5.5f)
                lineTo(18.5f, 9.5f)
                moveTo(4f, 20f)
                lineTo(8.2f, 19.2f)
                lineTo(19.2f, 8.2f)
                curveTo(19.8f, 7.6f, 19.8f, 6.6f, 19.2f, 6f)
                lineTo(18f, 4.8f)
                curveTo(17.4f, 4.2f, 16.4f, 4.2f, 15.8f, 4.8f)
                lineTo(4.8f, 15.8f)
                close()
            }
        }
    }

    val Trash: ImageVector by lazy {
        strokeIcon("KronozaTrash") {
            strokePath {
                moveTo(4f, 6.5f)
                horizontalLineTo(20f)
                moveTo(9f, 6.5f)
                verticalLineTo(4.5f)
                curveTo(9f, 3.95f, 9.45f, 3.5f, 10f, 3.5f)
                horizontalLineTo(14f)
                curveTo(14.55f, 3.5f, 15f, 3.95f, 15f, 4.5f)
                verticalLineTo(6.5f)
                moveTo(6.5f, 6.5f)
                lineTo(7.3f, 18.5f)
                curveTo(7.38f, 19.6f, 8.3f, 20.5f, 9.4f, 20.5f)
                horizontalLineTo(14.6f)
                curveTo(15.7f, 20.5f, 16.62f, 19.6f, 16.7f, 18.5f)
                lineTo(17.5f, 6.5f)
                moveTo(10f, 10.5f)
                verticalLineTo(16.5f)
                moveTo(14f, 10.5f)
                verticalLineTo(16.5f)
            }
        }
    }

    val Duplicate: ImageVector by lazy {
        strokeIcon("KronozaDuplicate") {
            strokePath {
                moveTo(8.5f, 8.5f)
                horizontalLineTo(18.5f)
                curveTo(19.6f, 8.5f, 20.5f, 9.4f, 20.5f, 10.5f)
                verticalLineTo(18.5f)
                curveTo(20.5f, 19.6f, 19.6f, 20.5f, 18.5f, 20.5f)
                horizontalLineTo(10.5f)
                curveTo(9.4f, 20.5f, 8.5f, 19.6f, 8.5f, 18.5f)
                close()
                moveTo(15.5f, 8.5f)
                verticalLineTo(5.5f)
                curveTo(15.5f, 4.4f, 14.6f, 3.5f, 13.5f, 3.5f)
                horizontalLineTo(5.5f)
                curveTo(4.4f, 3.5f, 3.5f, 4.4f, 3.5f, 5.5f)
                verticalLineTo(13.5f)
                curveTo(3.5f, 14.6f, 4.4f, 15.5f, 5.5f, 15.5f)
                horizontalLineTo(8.5f)
            }
        }
    }

    val Reschedule: ImageVector by lazy {
        strokeIcon("KronozaReschedule") {
            strokePath {
                moveTo(5.5f, 4.5f)
                horizontalLineTo(18.5f)
                curveTo(19.6f, 4.5f, 20.5f, 5.4f, 20.5f, 6.5f)
                verticalLineTo(11f)
                moveTo(3.5f, 9.5f)
                horizontalLineTo(20.5f)
                moveTo(3.5f, 6.5f)
                verticalLineTo(18.5f)
                curveTo(3.5f, 19.6f, 4.4f, 20.5f, 5.5f, 20.5f)
                horizontalLineTo(11.5f)
                moveTo(17.5f, 20.5f)
                curveTo(19.71f, 20.5f, 21.5f, 18.71f, 21.5f, 16.5f)
                curveTo(21.5f, 14.29f, 19.71f, 12.5f, 17.5f, 12.5f)
                curveTo(15.29f, 12.5f, 13.5f, 14.29f, 13.5f, 16.5f)
                curveTo(13.5f, 18.71f, 15.29f, 20.5f, 17.5f, 20.5f)
                close()
                moveTo(17.5f, 14.7f)
                verticalLineTo(16.7f)
                lineTo(19f, 17.5f)
            }
        }
    }

    val Copy: ImageVector by lazy { Duplicate }

    val Notes: ImageVector by lazy {
        strokeIcon("KronozaNotes") {
            strokePath {
                moveTo(4f, 7f)
                horizontalLineTo(20f)
                moveTo(4f, 12f)
                horizontalLineTo(20f)
                moveTo(4f, 17f)
                horizontalLineTo(13f)
            }
        }
    }

    val Flag: ImageVector by lazy {
        strokeIcon("KronozaFlag") {
            strokePath {
                moveTo(5.5f, 21f)
                verticalLineTo(4f)
                moveTo(5.5f, 4.5f)
                horizontalLineTo(17.5f)
                lineTo(15.5f, 8.5f)
                lineTo(17.5f, 12.5f)
                horizontalLineTo(5.5f)
            }
        }
    }

    val Filter: ImageVector by lazy {
        strokeIcon("KronozaFilter") {
            strokePath {
                moveTo(4f, 6.5f)
                horizontalLineTo(20f)
                moveTo(7f, 12f)
                horizontalLineTo(17f)
                moveTo(10f, 17.5f)
                horizontalLineTo(14f)
            }
        }
    }

    val Sort: ImageVector by lazy {
        strokeIcon("KronozaSort") {
            strokePath {
                moveTo(4f, 7f)
                horizontalLineTo(14f)
                moveTo(4f, 12f)
                horizontalLineTo(11f)
                moveTo(4f, 17f)
                horizontalLineTo(8f)
                moveTo(17f, 6f)
                verticalLineTo(18f)
                moveTo(14f, 15f)
                lineTo(17f, 18f)
                lineTo(20f, 15f)
            }
        }
    }

    val Subtask: ImageVector by lazy {
        strokeIcon("KronozaSubtask") {
            strokePath {
                moveTo(6f, 5f)
                verticalLineTo(14f)
                curveTo(6f, 15.1f, 6.9f, 16f, 8f, 16f)
                horizontalLineTo(12f)
                moveTo(6f, 10f)
                horizontalLineTo(12f)
                moveTo(14f, 10f)
                horizontalLineTo(20f)
                moveTo(14f, 16f)
                horizontalLineTo(20f)
            }
        }
    }

    val Bolt: ImageVector by lazy {
        strokeIcon("KronozaBolt") {
            strokePath {
                moveTo(13f, 2.5f)
                lineTo(5.5f, 13f)
                horizontalLineTo(12f)
                lineTo(11f, 21.5f)
                lineTo(18.5f, 11f)
                horizontalLineTo(12f)
                close()
            }
        }
    }

    val Flame: ImageVector by lazy {
        strokeIcon("KronozaFlame") {
            strokePath {
                moveTo(12.2f, 3f)
                curveTo(12.2f, 3f, 18f, 7.5f, 18f, 13.5f)
                curveTo(18f, 17.09f, 15.31f, 20f, 12f, 20f)
                curveTo(8.69f, 20f, 6f, 17.09f, 6f, 13.5f)
                curveTo(6f, 10.8f, 7.5f, 8.6f, 9f, 7.5f)
                curveTo(9f, 10f, 10.5f, 11f, 11.5f, 10f)
                curveTo(12.5f, 9f, 12.2f, 3f, 12.2f, 3f)
                close()
            }
        }
    }

    val Palette: ImageVector by lazy {
        strokeIcon("KronozaPalette") {
            strokePath {
                moveTo(12f, 21f)
                curveTo(16.97f, 21f, 21f, 16.97f, 21f, 12f)
                curveTo(21f, 7.03f, 16.97f, 3f, 12f, 3f)
                curveTo(7.03f, 3f, 3f, 7.03f, 3f, 12f)
                curveTo(3f, 14.5f, 4.8f, 16f, 7.2f, 16f)
                horizontalLineTo(9f)
                curveTo(10.1f, 16f, 11f, 16.9f, 11f, 18f)
                curveTo(11f, 19.65f, 10.6f, 21f, 12f, 21f)
                close()
                moveTo(7.5f, 10.5f)
                horizontalLineTo(7.6f)
                moveTo(10.5f, 7.5f)
                horizontalLineTo(10.6f)
                moveTo(14.5f, 8f)
                horizontalLineTo(14.6f)
                moveTo(16.5f, 11.5f)
                horizontalLineTo(16.6f)
            }
        }
    }

    val BackupExport: ImageVector by lazy {
        strokeIcon("KronozaBackupExport") {
            strokePath {
                moveTo(12f, 15f)
                verticalLineTo(4f)
                moveTo(8f, 8f)
                lineTo(12f, 4f)
                lineTo(16f, 8f)
                moveTo(4f, 14.5f)
                verticalLineTo(17.5f)
                curveTo(4f, 18.6f, 4.9f, 19.5f, 6f, 19.5f)
                horizontalLineTo(18f)
                curveTo(19.1f, 19.5f, 20f, 18.6f, 20f, 17.5f)
                verticalLineTo(14.5f)
            }
        }
    }

    val BackupImport: ImageVector by lazy {
        strokeIcon("KronozaBackupImport") {
            strokePath {
                moveTo(12f, 4f)
                verticalLineTo(15f)
                moveTo(8f, 11f)
                lineTo(12f, 15f)
                lineTo(16f, 11f)
                moveTo(4f, 14.5f)
                verticalLineTo(17.5f)
                curveTo(4f, 18.6f, 4.9f, 19.5f, 6f, 19.5f)
                horizontalLineTo(18f)
                curveTo(19.1f, 19.5f, 20f, 18.6f, 20f, 17.5f)
                verticalLineTo(14.5f)
            }
        }
    }

    val Camera: ImageVector by lazy {
        strokeIcon("KronozaCamera") {
            strokePath {
                moveTo(4.5f, 7.5f)
                horizontalLineTo(7.5f)
                lineTo(9f, 5f)
                horizontalLineTo(15f)
                lineTo(16.5f, 7.5f)
                horizontalLineTo(19.5f)
                curveTo(20.6f, 7.5f, 21.5f, 8.4f, 21.5f, 9.5f)
                verticalLineTo(17.5f)
                curveTo(21.5f, 18.6f, 20.6f, 19.5f, 19.5f, 19.5f)
                horizontalLineTo(4.5f)
                curveTo(3.4f, 19.5f, 2.5f, 18.6f, 2.5f, 17.5f)
                verticalLineTo(9.5f)
                curveTo(2.5f, 8.4f, 3.4f, 7.5f, 4.5f, 7.5f)
                close()
                moveTo(12f, 16.2f)
                curveTo(13.77f, 16.2f, 15.2f, 14.77f, 15.2f, 13f)
                curveTo(15.2f, 11.23f, 13.77f, 9.8f, 12f, 9.8f)
                curveTo(10.23f, 9.8f, 8.8f, 11.23f, 8.8f, 13f)
                curveTo(8.8f, 14.77f, 10.23f, 16.2f, 12f, 16.2f)
                close()
            }
        }
    }

    val Shield: ImageVector by lazy {
        strokeIcon("KronozaShield") {
            strokePath {
                moveTo(12f, 3f)
                lineTo(19f, 6f)
                verticalLineTo(11.5f)
                curveTo(19f, 16.2f, 15.9f, 19.9f, 12f, 21f)
                curveTo(8.1f, 19.9f, 5f, 16.2f, 5f, 11.5f)
                verticalLineTo(6f)
                close()
                moveTo(9.5f, 12f)
                lineTo(11.2f, 13.7f)
                lineTo(14.8f, 10.1f)
            }
        }
    }

    val Database: ImageVector by lazy {
        strokeIcon("KronozaDatabase") {
            strokePath {
                moveTo(12f, 7.5f)
                curveTo(16.14f, 7.5f, 19.5f, 6.27f, 19.5f, 4.75f)
                curveTo(19.5f, 3.23f, 16.14f, 2f, 12f, 2f)
                curveTo(7.86f, 2f, 4.5f, 3.23f, 4.5f, 4.75f)
                curveTo(4.5f, 6.27f, 7.86f, 7.5f, 12f, 7.5f)
                close()
                moveTo(4.5f, 4.75f)
                verticalLineTo(17.25f)
                curveTo(4.5f, 18.77f, 7.86f, 20f, 12f, 20f)
                curveTo(16.14f, 20f, 19.5f, 18.77f, 19.5f, 17.25f)
                verticalLineTo(4.75f)
                moveTo(4.5f, 11f)
                curveTo(4.5f, 12.52f, 7.86f, 13.75f, 12f, 13.75f)
                curveTo(16.14f, 13.75f, 19.5f, 12.52f, 19.5f, 11f)
            }
        }
    }

    val Info: ImageVector by lazy {
        strokeIcon("KronozaInfo") {
            strokePath {
                moveTo(12f, 21f)
                curveTo(16.97f, 21f, 21f, 16.97f, 21f, 12f)
                curveTo(21f, 7.03f, 16.97f, 3f, 12f, 3f)
                curveTo(7.03f, 3f, 3f, 7.03f, 3f, 12f)
                curveTo(3f, 16.97f, 7.03f, 21f, 12f, 21f)
                close()
                moveTo(12f, 11f)
                verticalLineTo(16f)
                moveTo(12f, 8f)
                horizontalLineTo(12.01f)
            }
        }
    }

    val AlertTriangle: ImageVector by lazy {
        strokeIcon("KronozaAlertTriangle") {
            strokePath {
                moveTo(12f, 3.5f)
                lineTo(20.5f, 19f)
                horizontalLineTo(3.5f)
                close()
                moveTo(12f, 9.5f)
                verticalLineTo(14f)
                moveTo(12f, 16.5f)
                horizontalLineTo(12.01f)
            }
        }
    }

    val Replay: ImageVector by lazy {
        strokeIcon("KronozaReplay") {
            strokePath {
                moveTo(4f, 11.5f)
                curveTo(4f, 7.36f, 7.36f, 4f, 11.5f, 4f)
                curveTo(15.64f, 4f, 19f, 7.36f, 19f, 11.5f)
                curveTo(19f, 15.64f, 15.64f, 19f, 11.5f, 19f)
                curveTo(9.1f, 19f, 6.96f, 17.87f, 5.58f, 16.12f)
                moveTo(4f, 7f)
                verticalLineTo(11.5f)
                horizontalLineTo(8.5f)
            }
        }
    }

    val Sliders: ImageVector by lazy {
        strokeIcon("KronozaSliders") {
            strokePath {
                moveTo(4f, 7f)
                horizontalLineTo(20f)
                moveTo(4f, 17f)
                horizontalLineTo(20f)
                moveTo(9f, 4.5f)
                verticalLineTo(9.5f)
                moveTo(15f, 14.5f)
                verticalLineTo(19.5f)
            }
        }
    }

    val Grid: ImageVector by lazy {
        strokeIcon("KronozaGrid") {
            strokePath {
                moveTo(4f, 4f)
                horizontalLineTo(10f)
                verticalLineTo(10f)
                horizontalLineTo(4f)
                close()
                moveTo(14f, 4f)
                horizontalLineTo(20f)
                verticalLineTo(10f)
                horizontalLineTo(14f)
                close()
                moveTo(4f, 14f)
                horizontalLineTo(10f)
                verticalLineTo(20f)
                horizontalLineTo(4f)
                close()
                moveTo(14f, 14f)
                horizontalLineTo(20f)
                verticalLineTo(20f)
                horizontalLineTo(14f)
                close()
            }
        }
    }

    val ExpandMore: ImageVector by lazy { ChevronDown }
    val ExpandLess: ImageVector by lazy {
        strokeIcon("KronozaExpandLess") {
            strokePath(strokeWidth = 2.0f) {
                moveTo(6f, 15f)
                lineTo(12f, 9f)
                lineTo(18f, 15f)
            }
        }
    }

    // ─── Developer, About & Attribution Icons ────────────────────────────────────
    val GitHub: ImageVector by lazy {
        strokeIcon("KronozaGitHub") {
            fillPath {
                moveTo(12f, 2f)
                curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
                curveTo(2f, 16.42f, 4.87f, 20.17f, 8.84f, 21.49f)
                curveTo(9.34f, 21.58f, 9.52f, 21.27f, 9.52f, 21.01f)
                curveTo(9.52f, 20.77f, 9.51f, 20.14f, 9.51f, 19.31f)
                curveTo(6.73f, 19.91f, 6.14f, 17.97f, 6.14f, 17.97f)
                curveTo(5.69f, 16.81f, 5.03f, 16.5f, 5.03f, 16.5f)
                curveTo(4.12f, 15.88f, 5.1f, 15.89f, 5.1f, 15.89f)
                curveTo(6.1f, 15.96f, 6.63f, 16.92f, 6.63f, 16.92f)
                curveTo(7.52f, 18.45f, 8.97f, 18.01f, 9.54f, 17.75f)
                curveTo(9.63f, 17.1f, 9.89f, 16.66f, 10.17f, 16.41f)
                curveTo(7.95f, 16.16f, 5.62f, 15.3f, 5.62f, 11.47f)
                curveTo(5.62f, 10.38f, 6.01f, 9.49f, 6.65f, 8.79f)
                curveTo(6.55f, 8.54f, 6.2f, 7.51f, 6.75f, 6.14f)
                curveTo(6.75f, 6.14f, 7.59f, 5.87f, 9.5f, 7.16f)
                curveTo(10.3f, 6.94f, 11.15f, 6.83f, 12f, 6.83f)
                curveTo(12.85f, 6.83f, 13.7f, 6.94f, 14.5f, 7.16f)
                curveTo(16.41f, 5.87f, 17.25f, 6.14f, 17.25f, 6.14f)
                curveTo(17.8f, 7.51f, 17.45f, 8.54f, 17.35f, 8.79f)
                curveTo(17.99f, 9.49f, 18.38f, 10.38f, 18.38f, 11.47f)
                curveTo(18.38f, 15.31f, 16.04f, 16.16f, 13.81f, 16.4f)
                curveTo(14.17f, 16.71f, 14.5f, 17.32f, 14.5f, 18.26f)
                curveTo(14.5f, 19.6f, 14.49f, 20.68f, 14.49f, 21.01f)
                curveTo(14.49f, 21.28f, 14.67f, 21.59f, 15.18f, 21.49f)
                curveTo(19.14f, 20.17f, 22f, 16.42f, 22f, 12f)
                curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
                close()
            }
        }
    }

    val ExternalLink: ImageVector by lazy {
        strokeIcon("KronozaExternalLink") {
            strokePath {
                moveTo(14f, 4.5f)
                horizontalLineTo(19.5f)
                verticalLineTo(10f)
                moveTo(19.5f, 4.5f)
                lineTo(11f, 13f)
                moveTo(17.5f, 13.5f)
                verticalLineTo(17.5f)
                curveTo(17.5f, 18.6f, 16.6f, 19.5f, 15.5f, 19.5f)
                horizontalLineTo(6.5f)
                curveTo(5.4f, 19.5f, 4.5f, 18.6f, 4.5f, 17.5f)
                verticalLineTo(8.5f)
                curveTo(4.5f, 7.4f, 5.4f, 6.5f, 6.5f, 6.5f)
                horizontalLineTo(10.5f)
            }
        }
    }

    val Code: ImageVector by lazy {
        strokeIcon("KronozaCode") {
            strokePath {
                moveTo(8.5f, 7f)
                lineTo(3.5f, 12f)
                lineTo(8.5f, 17f)
                moveTo(15.5f, 7f)
                lineTo(20.5f, 12f)
                lineTo(15.5f, 17f)
                moveTo(13.5f, 4.5f)
                lineTo(10.5f, 19.5f)
            }
        }
    }

    val Layers: ImageVector by lazy {
        strokeIcon("KronozaLayers") {
            strokePath {
                moveTo(12f, 3.5f)
                lineTo(20.5f, 8f)
                lineTo(12f, 12.5f)
                lineTo(3.5f, 8f)
                close()
                moveTo(3.5f, 12f)
                lineTo(12f, 16.5f)
                lineTo(20.5f, 12f)
                moveTo(3.5f, 16f)
                lineTo(12f, 20.5f)
                lineTo(20.5f, 16f)
            }
        }
    }

    val Cpu: ImageVector by lazy {
        strokeIcon("KronozaCpu") {
            strokePath {
                moveTo(7f, 6.5f)
                horizontalLineTo(17f)
                curveTo(17.28f, 6.5f, 17.5f, 6.72f, 17.5f, 7f)
                verticalLineTo(17f)
                curveTo(17.5f, 17.28f, 17.28f, 17.5f, 17f, 17.5f)
                horizontalLineTo(7f)
                curveTo(6.72f, 17.5f, 6.5f, 17.28f, 6.5f, 17f)
                verticalLineTo(7f)
                curveTo(6.5f, 6.72f, 6.72f, 6.5f, 7f, 6.5f)
                close()
                moveTo(10f, 10f)
                horizontalLineTo(14f)
                verticalLineTo(14f)
                horizontalLineTo(10f)
                close()
                moveTo(9.5f, 3.5f)
                verticalLineTo(6.5f)
                moveTo(14.5f, 3.5f)
                verticalLineTo(6.5f)
                moveTo(9.5f, 17.5f)
                verticalLineTo(20.5f)
                moveTo(14.5f, 17.5f)
                verticalLineTo(20.5f)
                moveTo(3.5f, 9.5f)
                horizontalLineTo(6.5f)
                moveTo(3.5f, 14.5f)
                horizontalLineTo(6.5f)
                moveTo(17.5f, 9.5f)
                horizontalLineTo(20.5f)
                moveTo(17.5f, 14.5f)
                horizontalLineTo(20.5f)
            }
        }
    }

    val Scale: ImageVector by lazy {
        strokeIcon("KronozaScale") {
            strokePath {
                moveTo(12f, 4f)
                verticalLineTo(20f)
                moveTo(7f, 20f)
                horizontalLineTo(17f)
                moveTo(5f, 7.5f)
                horizontalLineTo(19f)
                moveTo(5f, 7.5f)
                lineTo(3f, 13f)
                curveTo(3.8f, 14.2f, 6.2f, 14.2f, 7f, 13f)
                close()
                moveTo(19f, 7.5f)
                lineTo(17f, 13f)
                curveTo(17.8f, 14.2f, 20.2f, 14.2f, 21f, 13f)
                close()
            }
        }
    }

    val Lock: ImageVector by lazy {
        strokeIcon("KronozaLock") {
            strokePath {
                moveTo(6.5f, 10.5f)
                horizontalLineTo(17.5f)
                curveTo(18.6f, 10.5f, 19.5f, 11.4f, 19.5f, 12.5f)
                verticalLineTo(18.5f)
                curveTo(19.5f, 19.6f, 18.6f, 20.5f, 17.5f, 20.5f)
                horizontalLineTo(6.5f)
                curveTo(5.4f, 20.5f, 4.5f, 19.6f, 4.5f, 18.5f)
                verticalLineTo(12.5f)
                curveTo(4.5f, 11.4f, 5.4f, 10.5f, 6.5f, 10.5f)
                close()
                moveTo(8.5f, 10.5f)
                verticalLineTo(7.5f)
                curveTo(8.5f, 5.57f, 10.07f, 4f, 12f, 4f)
                curveTo(13.93f, 4f, 15.5f, 5.57f, 15.5f, 7.5f)
                verticalLineTo(10.5f)
            }
        }
    }

    val User: ImageVector by lazy {
        strokeIcon("KronozaUser") {
            strokePath {
                moveTo(12f, 11.5f)
                curveTo(14.21f, 11.5f, 16f, 9.71f, 16f, 7.5f)
                curveTo(16f, 5.29f, 14.21f, 3.5f, 12f, 3.5f)
                curveTo(9.79f, 3.5f, 8f, 5.29f, 8f, 7.5f)
                curveTo(8f, 9.71f, 9.79f, 11.5f, 12f, 11.5f)
                close()
                moveTo(4.5f, 20f)
                curveTo(4.5f, 16.69f, 7.86f, 14.5f, 12f, 14.5f)
                curveTo(16.14f, 14.5f, 19.5f, 16.69f, 19.5f, 20f)
            }
        }
    }
}

@Composable
fun KronozaIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    tint: Color = LocalContentColor.current
) {
    val animatedTint by animateColorAsState(targetValue = tint, label = "icon_tint")
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        tint = animatedTint,
        modifier = modifier.size(size)
    )
}
