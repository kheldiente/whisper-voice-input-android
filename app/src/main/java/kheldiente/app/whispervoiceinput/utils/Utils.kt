package kheldiente.app.whispervoiceinput.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

val sampleSystemInfo = """
    ASR model in-use: ggml.bin
    AVS = 1
    PPS = 0
    YKS = 0
    LLL = 1
""".trimIndent()

suspend fun getTempFileForRecording(): File = withContext(Dispatchers.IO) {
    File.createTempFile("recording", "wav")
}