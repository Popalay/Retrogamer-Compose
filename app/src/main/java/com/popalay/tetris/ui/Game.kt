package com.popalay.tetris.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedTask
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.gesture.MinFlingVelocity
import androidx.compose.ui.gesture.TouchSlop
import androidx.compose.ui.gesture.dragGestureFilter
import androidx.compose.ui.gesture.tapGestureFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.DensityAmbient
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.viewModel
import androidx.ui.tooling.preview.Preview
import com.popalay.tetris.game.GameStatus
import com.popalay.tetris.game.TetrisBlock
import com.popalay.tetris.game.TetrisViewModel
import com.popalay.tetris.game.TetrisViewModel.Intent
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ticker
import kotlin.math.min

@OptIn(ObsoleteCoroutinesApi::class)
@Composable
fun Game() {
    val viewModel = viewModel<TetrisViewModel>()
    val state by viewModel.state

    val dragObserver = with(DensityAmbient.current) {
        SwipeDragObserver(TouchSlop.toPx(), MinFlingVelocity.toPx()) { viewModel.consume(Intent.Swipe(it, true)) }
    }
    val onTap: (Offset) -> Unit = { viewModel.consume(Intent.Tap) }
    val tickerChannel = remember { ticker(delayMillis = 300 / state.velocity) }

    LaunchedTask {
        for (event in tickerChannel) {
            viewModel.consume(Intent.GameTick)
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize()
                .tapGestureFilter(onTap)
                .dragGestureFilter(dragObserver)
        ) {
            Statistic(state)
            Board(state, Modifier.align(Alignment.CenterHorizontally))
        }

        if (state.gameStatus == GameStatus.GameOver) {
            GameOver(Modifier.align(Alignment.Center))
        }
    }
}

@Composable
fun Statistic(state: TetrisViewModel.State, modifier: Modifier = Modifier) {
    Row(modifier.padding(all = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("Score: ${state.score}")
        Text("Next:", modifier = Modifier.padding(start = 16.dp))
        NextHero(block = state.heroBag.first().rotate(), modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
fun GameOver(modifier: Modifier = Modifier) {
    Text(
        "Game Over!",
        style = MaterialTheme.typography.h1.copy(color = Color.White),
        textAlign = TextAlign.Center,
        modifier = modifier.background(Color.Red.copy(alpha = 0.7F))
    )
}

@Composable
fun Board(state: TetrisViewModel.State, modifier: Modifier = Modifier) {
    Canvas(
        modifier
            .fillMaxSize()
            .drawBehind { drawBorderBackground(state.size) }
    ) {
        val blockSize = min(
            size.height / state.size.second.toFloat(),
            size.width / state.size.first.toFloat()
        )
        drawBoard(state.blocks, blockSize)
        drawHero(state.hero, blockSize)
        drawProjection(state.projection, blockSize)
    }
}

@Composable
fun NextHero(block: TetrisBlock, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        drawHero(block.adjustOffset(4 to 1), 12.dp.toPx())
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TetrisComposeTheme {
        Game()
    }
}