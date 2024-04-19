package kheldiente.app.whispervoiceinput

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import kheldiente.app.whispervoiceinput.ui.main.MainScreen
import kheldiente.app.whispervoiceinput.ui.main.MainScreenViewModel
import kheldiente.app.whispervoiceinput.ui.theme.WhisperCppDemoTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainScreenViewModel by viewModels { MainScreenViewModel.factory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WhisperCppDemoTheme {
                MainScreen(viewModel)
            }
        }
    }
}