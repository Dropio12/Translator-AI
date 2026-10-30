package io.github.dropio12.translatorai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import io.github.dropio12.translatorai.ui.TranslatorApp
import io.github.dropio12.translatorai.ui.TranslatorViewModel
import io.github.dropio12.translatorai.ui.theme.TranslatorTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TranslatorViewModel by viewModels { TranslatorViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TranslatorTheme {
                TranslatorApp(viewModel)
            }
        }
    }
}
