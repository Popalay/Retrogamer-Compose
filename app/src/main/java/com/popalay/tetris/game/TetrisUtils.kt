package com.popalay.tetris.game

import androidx.compose.ui.gesture.Direction
import androidx.compose.ui.graphics.Color

fun calculateScore(linesDestroyed: Int) = when (linesDestroyed) {
    1 -> 100
    2 -> 300
    3 -> 700
    4 -> 1500
    else -> 0
}

val shapeVariants = listOf(
    listOf(Pair(0, -1), Pair(0, 0), Pair(-1, 0), Pair(-1, 1)) to Color(0xFF3D76B5),
    listOf(Pair(0, -1), Pair(0, 0), Pair(1, 0), Pair(1, 1)) to Color(0xFFA369B8),
    listOf(Pair(0, -1), Pair(0, 0), Pair(0, 1), Pair(0, 2)) to Color(0xFFFF0128),
    listOf(Pair(0, 1), Pair(0, 0), Pair(0, -1), Pair(1, 0)) to Color(0xFF43D462),
    listOf(Pair(0, 0), Pair(-1, 0), Pair(0, -1), Pair(-1, -1)) to Color(0xFFFBCD05),
    listOf(Pair(-1, -1), Pair(0, -1), Pair(0, 0), Pair(0, 1)) to Color(0xFF53B1FD),
    listOf(Pair(1, -1), Pair(0, -1), Pair(0, 0), Pair(0, 1)) to Color(0xFFEDEAE9)
)

operator fun Pair<Int, Int>.plus(pair: Pair<Int, Int>): Pair<Int, Int> =
    Pair(first + pair.first, second + pair.second)

operator fun Pair<Int, Int>.div(pair: Pair<Int, Int>): Pair<Int, Int> =
    Pair(first / pair.first, second / pair.second)

operator fun Pair<Int, Int>.times(pair: Pair<Int, Int>): Pair<Int, Int> =
    Pair(first * pair.first, second * pair.second)

fun <T> MutableList<List<T>>.update(i: Int, j: Int, item: T): MutableList<List<T>> = apply {
    this[j] = this[j].toMutableList().apply {
        this[i] = item
    }
}

val <T> List<List<T>>.multiSize: Pair<Int, Int> get() = (firstOrNull()?.size ?: 0) to size

fun Direction.toOffset() = when (this) {
    Direction.LEFT -> -1 to 0
    Direction.UP -> 0 to -1
    Direction.RIGHT -> 1 to 0
    Direction.DOWN -> 0 to 1
}