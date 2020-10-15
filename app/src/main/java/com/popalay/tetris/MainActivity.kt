package com.popalay.tetris

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.platform.setContent
import com.popalay.tetris.ui.Game
import com.popalay.tetris.ui.TetrisComposeTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TetrisComposeTheme {
                Surface(color = MaterialTheme.colors.background) {
                    Game()
                }
            }
        }
    }
}