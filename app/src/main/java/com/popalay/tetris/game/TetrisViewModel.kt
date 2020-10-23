package com.popalay.tetris.game

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.gesture.Direction
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.popalay.tetris.game.GameStatus.GameOver
import com.popalay.tetris.game.GameStatus.InProgress
import com.popalay.tetris.game.GameStatus.Pause
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class TetrisViewModel : ViewModel() {
    val state = mutableStateOf(State.start(12 to 24))
    private val intents = Channel<Intent>(Channel.UNLIMITED)

    init {
        viewModelScope.launch {
            handleIntents()
        }
    }

    fun consume(intent: Intent) {
        intents.offer(intent)
    }

    private suspend fun handleIntents() {
        intents.consumeAsFlow().collect { intent ->
            state.value = reduce(state.value, intent)
        }
    }

    private fun reduce(state: State, intent: Intent): State = when (intent) {
        Intent.Tap -> if (state.gameStatus == GameOver) reduce(state, Intent.Restart) else reduce(state, Intent.Rotate)
        Intent.Restart -> State.start(state.size)
        Intent.Pause -> state.copy(gameStatus = Pause)
        Intent.Resume -> state.copy(gameStatus = InProgress)
        is Intent.Swipe -> {
            val offset = intent.direction.toOffset()
            val newHero = if (intent.force && intent.direction == Direction.DOWN) state.projection else state.hero.move(offset)
            val newProjection = getProjection(newHero, state.blocks)
            if (isValidLocation(newHero, state.blocks)) state.copy(hero = newHero, projection = newProjection) else state
        }
        Intent.Rotate -> {
            val newHero = state.hero.rotate().adjustOffset(state.size)
            val newProjection = getProjection(newHero, state.blocks)
            if (isValidLocation(newHero, state.blocks)) state.copy(hero = newHero, projection = newProjection) else state
        }
        Intent.GameTick -> {
            if (state.gameStatus == InProgress) {
                if (isValidLocation(state.hero.move(0 to 1), state.blocks)) {
                    reduce(state, Intent.Swipe(Direction.DOWN))
                        .copy(tick = state.tick + 1, velocity = state.tick % 10L)
                } else {
                    val newHero = state.heroBag.first()
                    val (newBlocks, destroyedRows) = state.blocks.modifyBlocks(state.hero)
                    val newGameStatus = if (isValidLocation(newHero, state.blocks)) state.gameStatus else GameOver
                    state.copy(
                        blocks = newBlocks,
                        hero = newHero,
                        projection = getProjection(newHero, newBlocks),
                        heroBag = state.heroBag.minus(newHero).ifEmpty { TetrisBlock.generateBag(state.size) },
                        gameStatus = newGameStatus,
                        score = state.score + calculateScore(destroyedRows)
                    )
                }
            } else state
        }
    }

    private fun getProjection(hero: TetrisBlock, blocks: Board): TetrisBlock {
        var newHero = hero
        while (isValidLocation(newHero.move(0 to 1), blocks)) {
            newHero = newHero.move(0 to 1)
        }
        return newHero
    }

    private fun Board.modifyBlocks(block: TetrisBlock): Pair<Board, Int> {
        val size = multiSize
        var newBoard = this.toMutableList()
        var destroyedRows = 0
        block.coordinates.forEach {
            newBoard = newBoard.update(it.first, it.second, block.color)
        }
        if (newBoard.removeAll { row -> row.all { it != Color.Unspecified } }) {
            destroyedRows = size.second - newBoard.size
            newBoard.addAll(0, (0 until destroyedRows).map { (0 until size.first).map { Color.Unspecified } })
        }
        return newBoard to destroyedRows
    }

    private fun isValidLocation(hero: TetrisBlock, blocks: Board): Boolean {
        val size = blocks.multiSize
        return hero.coordinates.none {
            it.first < 0 || it.first > size.first - 1 || it.second > size.second - 1 ||
                    blocks[it.second][it.first] != Color.Unspecified
        }
    }

    data class State(
        val size: BoardSize,
        val hero: TetrisBlock,
        val projection: TetrisBlock,
        val heroBag: List<TetrisBlock>,
        val blocks: Board,
        val velocity: Long,
        val gameStatus: GameStatus,
        val tick: Int,
        val score: Int
    ) {
        companion object {
            fun start(size: BoardSize): State {
                val heroBag = TetrisBlock.generateBag(size)
                val hero = heroBag.first()
                return State(
                    size = size,
                    hero = hero,
                    projection = hero.move(0 to size.second),
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
    }

    sealed class Intent {
        data class Swipe(val direction: Direction, val force: Boolean = false) : Intent()
        object Restart : Intent()
        object Pause : Intent()
        object Resume : Intent()
        object Rotate : Intent()
        object Tap : Intent()
        object GameTick : Intent()
    }
}

typealias Board = List<List<Color>>
typealias BoardSize = Pair<Int, Int>