package kheldiente.app.whispervoiceinput.ui.main

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.whispercpp.whisper.WhisperContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class MainScreenViewModel(private val application: Application) : ViewModel() {

    var systemInfo by mutableStateOf("")
        private set

    private var whisperContext: WhisperContext? = null

    init {
        viewModelScope.launch {
            loadData()
            generateSystemSpecs()
        }
    }

    private suspend fun generateSystemSpecs() {
        val systemSpecs = WhisperContext.getSystemInfo().split("|")
        systemSpecs.forEachIndexed { index, value ->
            val info = value.trim()
            if (info.isNotBlank()) {
                if (index == systemSpecs.lastIndex) {
                    printMessage(String.format("%s", info))
                } else {
                    printMessage(String.format("%s\n", info))
                }
            }
        }
    }

    private suspend fun loadData() {
        try {
            loadBaseModel()
        } catch (e: Exception) {
            printMessage("${e.localizedMessage}\n")
        }
    }

    private suspend fun printMessage(msg: String) = withContext(Dispatchers.Main) {
        systemInfo += msg
    }

    private suspend fun loadBaseModel() = withContext(Dispatchers.IO) {
        application.assets.list("models/")?.let { models ->
            whisperContext = WhisperContext.createContextFromAsset(application.assets, "models/" + models.first())
            printMessage("ASR model in-use: ${models.first()}\n")
        }
    }

    override fun onCleared() {
        runBlocking {
            whisperContext?.release()
            whisperContext = null
        }
    }

    companion object {
        fun factory() = viewModelFactory {
            initializer {
                val application =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                MainScreenViewModel(application)
            }
        }
    }

}