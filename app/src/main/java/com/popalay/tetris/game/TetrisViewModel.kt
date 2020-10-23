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
    val state = mutableStateOf(State.initial(12 to 24))
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
        Intent.Restart -> State.initial(state.size)
        Intent.Pause -> state.copy(gameStatus = Pause)
        Intent.Resume -> state.copy(gameStatus = InProgress)
        is Intent.Swipe -> {
            val offset = intent.direction.toOffset()
            val newHero = if (intent.force && intent.direction == Direction.DOWN) state.projection else state.hero.move(offset)
            val newProjection = newHero.createProjection(state.blocks)
            if (newHero.isValid(state.blocks)) state.copy(hero = newHero, projection = newProjection) else state
        }
        Intent.Rotate -> {
            val newHero = state.hero.rotate().adjustOffset(state.size)
            val newProjection = newHero.createProjection(state.blocks)
            if (newHero.isValid(state.blocks)) state.copy(hero = newHero, projection = newProjection) else state
        }
        Intent.GameTick -> {
            if (state.gameStatus == InProgress) {
                if (state.hero.move(0 to 1).isValid(state.blocks)) {
                    val newTick = state.tick + 1
                    reduce(state, Intent.Swipe(Direction.DOWN))
                        .copy(tick = newTick, velocity = newTick % 10L)
                } else {
                    val newHero = state.heroBag.first()
                    val (newBlocks, destroyedRows) = state.blocks.modifyBlocks(state.hero)
                    val newGameStatus = if (newHero.isValid(state.blocks)) state.gameStatus else GameOver
                    state.copy(
                        blocks = newBlocks,
                        hero = newHero,
                        projection = newHero.createProjection(newBlocks),
                        heroBag = state.heroBag.minus(newHero).ifEmpty { TetrisBlock.generateBag(state.size) },
                        gameStatus = newGameStatus,
                        score = state.score + calculateScore(destroyedRows)
                    )
                }
            } else state
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
            fun initial(size: BoardSize): State {
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