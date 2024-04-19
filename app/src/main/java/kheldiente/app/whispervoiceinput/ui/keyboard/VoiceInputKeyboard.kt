package kheldiente.app.whispervoiceinput.ui.keyboard

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import kheldiente.app.whispervoiceinput.R
import kheldiente.app.whispervoiceinput.databinding.ViewKeyboardBinding

class VoiceInputKeyboard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var viewBinding: ViewKeyboardBinding
    private var isRecording = false

    var onClickMic: ((Boolean) -> Unit)? = null

    init {
        viewBinding = ViewKeyboardBinding.inflate(LayoutInflater.from(context))
        addView(viewBinding.root)
        setupViews()
    }

    private fun setupViews() {
        with(viewBinding) {
            btnMic.setOnClickListener {
                updateMicState()
                onClickMic?.invoke(isRecording)
            }
        }
    }

    private fun updateMicState() {
        with(viewBinding.btnMic) {
            val imageRes = if (isRecording) R.drawable.mic_idle else R.drawable.mic_pressed
            setImageResource(imageRes)
            isRecording = !isRecording
        }
    }

}