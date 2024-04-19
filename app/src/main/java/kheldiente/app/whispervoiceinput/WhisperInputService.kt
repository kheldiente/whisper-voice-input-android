package kheldiente.app.whispervoiceinput

import android.inputmethodservice.InputMethodService
import android.media.MediaPlayer
import android.os.Build
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.net.toUri
import com.whispercpp.whisper.WhisperContext
import kheldiente.app.whispervoiceinput.media.decodeWaveFile
import kheldiente.app.whispervoiceinput.recorder.Recorder
import kheldiente.app.whispervoiceinput.ui.keyboard.VoiceInputKeyboard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File

class WhisperInputService: InputMethodService() {

    companion object {
        private const val IME_SWITCH_OPTION_AVAILABILITY_API_LEVEL = 28
    }

    private val recorder = Recorder()
    private var mediaPlayer: MediaPlayer? = null
    private var recordedFile: File? = null
    private var whisperContext: WhisperContext? = null

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreateInputView(): View {
        return VoiceInputKeyboard(this).apply {
            onClickMic = { isRecording -> doOnClickMic(isRecording) }
        }
    }

    override fun onWindowShown() {
        super.onWindowShown()
        initLanguageModel()
    }

    override fun onWindowHidden() {
        super.onWindowHidden()
        runBlocking {
            whisperContext?.release()
            whisperContext = null
        }
    }

    private fun initLanguageModel() = coroutineScope.launch {
        println("Loading model...")
        val models = application.assets.list("models/")
        if (models != null) {
            whisperContext = WhisperContext.createContextFromAsset(application.assets, "models/" + models[0])
            println("Loaded model ${models[0]}.")
        }
    }

    private fun doOnClickMic(isRecording: Boolean) = coroutineScope.launch {
        try {
            if (isRecording) {
                val file = getTempFileForRecording()
                recorder.startRecording(file) { e ->
                    launch {
                        withContext(Dispatchers.Main) {
                            println(e.localizedMessage)
                        }
                    }
                }
                recordedFile = file
            } else {
                recorder.stopRecording()
                recordedFile?.let {
                    val result = transcribeAudio(it)
                    launch {
                        withContext(Dispatchers.Main) {
                            closeAndTypeResult(result)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("${e.localizedMessage}\n")
        }
    }

    private fun closeAndTypeResult(result: String) {
        currentInputConnection.commitText(result, 1)
        switchToPreviousIme()
    }

    private suspend fun transcribeAudio(file: File): String {
        var result = ""
        try {
            println("Reading wave samples... ")
            val data = decodeWaveFile(file)

            println("${data.size / (16000 / 1000)} ms\n")
            println("Transcribing data...\n")

            val start = System.currentTimeMillis()
            result = whisperContext?.transcribeData(data).orEmpty()
            val elapsed = System.currentTimeMillis() - start
            println("Done ($elapsed ms): $result")
        } catch (e: Exception) {
            println(e.localizedMessage)
        }
        return result
    }

    private suspend fun getTempFileForRecording() = withContext(Dispatchers.IO) {
        File.createTempFile("recording", "wav")
    }

    private fun switchToPreviousIme() {
        // Before API Level 28, switchToPreviousInputMethod() was not available
        if (Build.VERSION.SDK_INT >= IME_SWITCH_OPTION_AVAILABILITY_API_LEVEL) {
            switchToPreviousInputMethod()
        } else {
            (getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager)?.run {
                val token = window?.window?.attributes?.token
                switchToLastInputMethod(token)
            }
        }
    }

}