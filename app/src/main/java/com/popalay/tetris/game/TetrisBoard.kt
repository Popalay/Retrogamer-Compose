package com.popalay.tetris.game

import androidx.compose.ui.gesture.Direction
import androidx.compose.ui.graphics.Color
import com.popalay.tetris.game.GameStatus.GameOver
import com.popalay.tetris.game.GameStatus.InProgress
import com.popalay.tetris.game.GameStatus.Pause
import com.popalay.tetris.utils.calculateScore
import com.popalay.tetris.utils.toOffset
import com.popalay.tetris.utils.update

data class TetrisBoard(
    val size: Pair<Int, Int>,
    val hero: TetrisBlock,
    val heroBag: List<TetrisBlock>,
    val blocks: List<List<Color>>,
    val velocity: Long,
    val gameStatus: GameStatus,
    val tick: Int,
    val score: Int
) {
    companion object {
        fun start(size: Pair<Int, Int>): TetrisBoard {
            val heroBag = TetrisBlock.generateBag(size)
            val hero = heroBag.first()
            return TetrisBoard(
                size = size,
                hero = hero,
                heroBag = heroBag.minus(hero),
                blocks = (0 until size.second).map {
                    (0 until size.first).map { Color.Unspecified }
                },
                velocity = 1,
                gameStatus = InProgress,
                tick = 0,
                score = 0,
            )
        }
    }

    val nextHero = heroBag.first()

    fun restart() = start(size)

    fun pause() = copy(gameStatus = Pause)

    fun resume() = copy(gameStatus = InProgress)

    fun gameTick(): TetrisBoard {
        if (gameStatus == InProgress) {
            return if (canMove()) {
                move()
                    .destroyCompletedRows()
                    .copy(tick = tick + 1, velocity = tick % 10L)
            } else {
                nextHero().checkGameOver()
            }
        }
        return this
    }

    fun move(direction: Direction = Direction.DOWN, force: Boolean = false): TetrisBoard {
        val offset = direction.toOffset()
        val newHero = if (force && direction == Direction.DOWN) getProjection(hero) else hero.move(offset)
        return if (isValidLocation(newHero)) copy(hero = newHero) else this
    }

    fun rotate(): TetrisBoard {
        val newHero = hero.rotate().adjustOffset(size)
        return if (isValidLocation(newHero)) copy(hero = newHero) else this
    }

    fun getProjection(hero: TetrisBlock): TetrisBlock {
        var newHero = hero.move(0 to 1)
        while (true) {
            if (isValidLocation(newHero)) {
                newHero = newHero.move(0 to 1)
            } else {
                return newHero.move(0 to -1)
            }
        }
    }

    fun canMove(): Boolean = isValidLocation(hero.move(0 to 1))

    private fun nextHero(): TetrisBoard = copy(
        blocks = fillBlocks(hero),
        hero = heroBag.first(),
        heroBag = updateHeroBag()
    )

    private fun checkGameOver(): TetrisBoard {
        val newGameStatus = if (isValidLocation(hero)) gameStatus else GameOver
        return copy(gameStatus = newGameStatus)
    }

    private fun destroyCompletedRows(): TetrisBoard {
        with(blocks.toMutableList()) {
            return if (removeAll { row -> row.all { it != Color.Unspecified } }) {
                val diff = this@TetrisBoard.size.second - size
                addAll(0, (0 until diff).map { (0 until this@TetrisBoard.size.first).map { Color.Unspecified } })
                copy(blocks = this, score = score + calculateScore(diff))
            } else this@TetrisBoard
        }
    }

    private fun fillBlocks(block: TetrisBlock): List<List<Color>> {
        var newBoard = blocks
        block.coordinates.forEach {
            newBoard = newBoard.update(it.first, it.second, hero.color)
        }
        return newBoard
    }

    private fun updateHeroBag(): List<TetrisBlock> = heroBag.minus(heroBag.first())
        .ifEmpty { TetrisBlock.generateBag(size) }

    private fun isValidLocation(hero: TetrisBlock): Boolean = hero.coordinates.none {
        it.first < 0 || it.first > size.first - 1 || it.second > size.second - 1 ||
                blocks[it.second][it.first] != Color.Unspecified
    }
}