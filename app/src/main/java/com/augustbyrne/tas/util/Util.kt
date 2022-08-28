package com.augustbyrne.tas.util

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.layout
import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Type Converters
 */
class Converters {
    @TypeConverter
    fun toLocalDateTime(s: String?): LocalDateTime? = s?.let { LocalDateTime.parse(s) }

    @TypeConverter
    fun fromLocalDateTime(l: LocalDateTime?): String? =
        l?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
}

/**
 * Volatile Enum Classes
 */

enum class TimerState {
    Stopped, Paused, Running, Delayed
}

enum class CompletionType {
    Normal, Final
}

/**
 * Stored Enum Classes
 */

enum class DarkMode(val mode: Int) {
    System(0),
    Off(1),
    On(2);
    companion object {
        fun getMode(modeValue: Int): DarkMode {
            return values().first { value ->
                value.mode == modeValue
            }
        }
    }
}

enum class TimerTheme(val theme: Int) {
    Original(0),
    Vibrant(1),
    VaporWave(2);
    companion object {
        fun getTheme(themeValue: Int): TimerTheme {
            return values().first { value ->
                value.theme == themeValue
            }
        }
    }
}

enum class SortType(val type: Int) {
    Creation(0),
    LastEdited(1),
    Order(2),
    Default(3);
    companion object {
        fun getType(typeValue: Int): SortType {
            return values().first { value ->
                value.type == typeValue
            }
        }
    }
}

/**
 * A [Modifier] that affects the location and size of an app bar. This is the classic
 * system bar style where the bars slide out of view of the screen.
 *
 * An app bar that uses this with a prepared [TopAppBarState] will slide out of view when
 * the nested content is pulled up, and will slide back in when the content is pulled down.
 *
 * @param scrollState a [TopAppBarState] used to receive scroll events
 * @param barType determines whether the app bar is a top or bottom bar
 */
@OptIn(ExperimentalMaterial3Api::class)
fun Modifier.classicSystemBarScrollBehavior(scrollState: TopAppBarState, barType: BarType) =
    clipToBounds()
        .layout { measurable, constraints ->
            // Measure the composable
            val placeable = measurable.measure(constraints)
            if (barType == BarType.Top && scrollState.heightOffsetLimit != -placeable.height.toFloat()) {
                scrollState.heightOffsetLimit = -placeable.height.toFloat()
            }
            val placeableResizedY =
                placeable.height + (if (barType == BarType.Top) scrollState.heightOffset else scrollState.heightOffset * 1.25f).toInt()
            val yOffset = if (barType == BarType.Top) scrollState.heightOffset.toInt() else 0
            layout(placeable.width, placeableResizedY) {
                // Where the composable gets placed
                placeable.placeRelative(0, yOffset)
            }
        }

enum class BarType {
    Top, Bottom
}