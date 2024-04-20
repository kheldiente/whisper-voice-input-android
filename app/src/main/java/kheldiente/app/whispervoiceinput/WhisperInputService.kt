package kheldiente.app.whispervoiceinput

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.whispercpp.whisper.WhisperContext
import kheldiente.app.whispervoiceinput.media.decodeWaveFile
import kheldiente.app.whispervoiceinput.recorder.Recorder
import kheldiente.app.whispervoiceinput.ui.keyboard.VoiceInputKeyboard
import kheldiente.app.whispervoiceinput.utils.getTempFileForRecording
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

class WhisperInputService: InputMethodService() {

    companion object {
        private const val IMEI_SWITCH_OPTION_AVAILABILITY_API_LEVEL = 28
        private const val PATH_MODELS = "models/"
    }

    private lateinit var recorder: Recorder
    private var recordedFile: File? = null
    private var whisperContext: WhisperContext? = null
    private lateinit var keyboard: VoiceInputKeyboard

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreateInputView(): View {
        keyboard = VoiceInputKeyboard(this).apply {
            onClickMic = { isRecording ->
                if (recorder.isMicPermissionGranted(context)) {
                    doOnClickMic(isRecording)
                } else {
                    setKeyboardState(State.IDLE)
                    Toast.makeText(context, R.string.grant_microphone_permission, Toast.LENGTH_SHORT).show()
                }
            }
            onClickBackToPrevImei = { switchToPreviousImei() }
            onClickSettings = { goToSettings() }
            onClickBackSpace = { doOnClickBackSpace() }
        }
        return keyboard
    }

    override fun onWindowShown() {
        super.onWindowShown()
        println("Keyboard is shown!")
        initRecorder()
        initLanguageModel()
    }

    override fun onWindowHidden() {
        super.onWindowHidden()
        println("Keyboard is hidden!")
        clearResources()
    }

    private fun initRecorder() {
        recorder = Recorder()
    }

    private fun initLanguageModel() = coroutineScope.launch {
        println("Loading model...")
        application.assets.list(PATH_MODELS)?.let { models ->
            whisperContext = WhisperContext.createContextFromAsset(application.assets, PATH_MODELS + models.first())
            println("Loaded model ${models.first()}.")
        }
    }

    private fun clearResources() {
        runBlocking {
            whisperContext?.release()
            whisperContext = null
            println("Whisper context release!")

            recorder.stopRecording()
            setState(State.IDLE)
        }
    }

    private fun doOnClickMic(isRecording: Boolean) = coroutineScope.launch {
        try {
            if (isRecording) {
                setState(State.RECORDING)

                val file = getTempFileForRecording()
                recorder.startRecording(file) { e ->
                    launch(Dispatchers.Main) {
                        println(e.localizedMessage)
                    }
                }
                recordedFile = file
            } else {
                recorder.stopRecording()

                recordedFile?.let {
                    setState(State.TRANSCRIBING)
                    val result = transcribeAudio(it)

                    launch(Dispatchers.Main) {
                        setState(State.IDLE)
                        closeAndTypeResult(result)
                    }
                }
            }
        } catch (e: Exception) {
            println("${e.localizedMessage}\n")
        }
    }

    private fun setState(state: State) {
        keyboard.setKeyboardState(state)
    }

    private fun closeAndTypeResult(result: String) {
        currentInputConnection.commitText(result, 1)
    }

    private fun goToSettings() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun doOnClickBackSpace() {
        currentInputConnection?.run {
            // Deletes cursor pointed text, or all selected texts
            if (getSelectedText(0).isNullOrEmpty()) {
                deleteSurroundingText(1, 0)
            } else {
                commitText("", 1)
            }
        }
    }

    private suspend fun transcribeAudio(file: File): String {
        var result = ""
        try {
            println("Reading wave samples from ${file.name}...")
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

    private fun switchToPreviousImei() {
        // Before API Level 28, switchToPreviousInputMethod() was not available
        if (Build.VERSION.SDK_INT >= IMEI_SWITCH_OPTION_AVAILABILITY_API_LEVEL) {
            switchToPreviousInputMethod()
        } else {
            (getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager)?.run {
                val token = window?.window?.attributes?.token
                switchToLastInputMethod(token)
            }
        }
    }

}