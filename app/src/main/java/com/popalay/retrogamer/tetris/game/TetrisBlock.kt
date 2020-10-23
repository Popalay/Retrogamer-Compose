package com.popalay.retrogamer.tetris.game

import androidx.compose.ui.graphics.Color
import kotlin.math.absoluteValue
import kotlin.random.Random

data class TetrisBlock(
    val shape: List<Pair<Int, Int>>,
    val offset: Pair<Int, Int>,
    val color: Color
) {
    companion object {
        fun generateBag(size: Pair<Int, Int>): List<TetrisBlock> = shapeVariants
            .map { (coordinates, color) ->
                TetrisBlock(
                    coordinates,
                    Random.nextInt(size.first) to 0,
                    color
                ).adjustOffset(size)
            }.shuffled()
    }

    val coordinates: List<Pair<Int, Int>> = shape.map { it + offset }

    fun move(step: Pair<Int, Int>): TetrisBlock = copy(offset = offset + step)

    fun rotate(): TetrisBlock {
        val newShape = shape.toMutableList()
        for (i in shape.indices) {
            newShape[i] = shape[i].second to -shape[i].first
        }
        return copy(shape = newShape)
    }

    fun adjustOffset(size: Pair<Int, Int>): TetrisBlock {
        val yOffset = (coordinates.minByOrNull { it.second }?.second?.takeIf { it < 0 }?.absoluteValue ?: 0) +
                (coordinates.maxByOrNull { it.second }?.second?.takeIf { it > size.second - 1 }?.let { size.second - it - 1 } ?: 0)
        val xOffset = (coordinates.minByOrNull { it.first }?.first?.takeIf { it < 0 }?.absoluteValue ?: 0) +
                (coordinates.maxByOrNull { it.first }?.first?.takeIf { it > size.first - 1 }?.let { size.first - it - 1 } ?: 0)
        return move(xOffset to yOffset)
    }
}