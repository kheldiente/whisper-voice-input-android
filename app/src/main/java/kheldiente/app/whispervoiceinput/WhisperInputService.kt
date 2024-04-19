package kheldiente.app.whispervoiceinput

import android.inputmethodservice.InputMethodService
import android.view.View
import kheldiente.app.whispervoiceinput.ui.keyboard.VoiceInputKeyboard

class WhisperInputService: InputMethodService() {

    override fun onCreateInputView(): View {
        return VoiceInputKeyboard(this)
    }

    override fun onWindowShown() {
        super.onWindowShown()
    }

    override fun onWindowHidden() {
        super.onWindowHidden()
    }

}