package kheldiente.app.whispervoiceinput.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

suspend fun getTempFileForRecording(): File = withContext(Dispatchers.IO) {
    File.createTempFile("recording", "wav")
}