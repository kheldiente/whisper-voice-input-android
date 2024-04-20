package kheldiente.app.whispervoiceinput.ui.keyboard

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import kheldiente.app.whispervoiceinput.R
import kheldiente.app.whispervoiceinput.State
import kheldiente.app.whispervoiceinput.databinding.ViewKeyboardBinding

class VoiceInputKeyboard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var viewBinding: ViewKeyboardBinding
    private var isRecording = false

    var onClickMic: ((Boolean) -> Unit)? = null
    var onClickSettings: (() -> Unit)? = null
    var onClickBackSpace: (() -> Unit)? = null
    var onClickBackToPrevImei: (() -> Unit)? = null

    init {
        viewBinding = ViewKeyboardBinding.inflate(LayoutInflater.from(context))
        addView(viewBinding.root)
        setupViews()
    }

    private fun setupViews() {
        with(viewBinding) {
            btnPreviousIme.setOnClickListener { onClickBackToPrevImei?.invoke() }
            btnSettings.setOnClickListener { onClickSettings?.invoke() }
            btnBackspace.setOnClickListener { onClickBackSpace?.invoke() }
            btnMic.setOnClickListener {
                updateMicState()
                onClickMic?.invoke(isRecording)
            }
        }
    }

    fun setKeyboardState(state: State) {
        with (viewBinding) {
            labelStatus.setText(
                when (state) {
                    State.IDLE -> R.string.whisper_to_input
                    State.RECORDING -> R.string.recording_audio
                    State.TRANSCRIBING -> R.string.transcribing_audio
                }
            )
            when (state) {
                State.IDLE -> {
                    showMicrophone(true)
                    setAsLoading(false)
                    resetMicState()
                }
                State.RECORDING -> {
                    showMicrophone(true)
                    setAsLoading(false)
                    setAsRecording(true)
                }
                State.TRANSCRIBING -> {
                    showMicrophone(false)
                    setAsLoading(true)
                }
            }
        }
    }

    private fun setAsLoading(isLoading: Boolean) {
        with(viewBinding) {
            pbWaitingIcon.visibility = if (isLoading) View.VISIBLE else View.INVISIBLE
            btnSettings.visibility= if (isLoading) View.INVISIBLE else View.VISIBLE
            btnBackspace.visibility= if (isLoading) View.INVISIBLE else View.VISIBLE
        }
    }

    private fun setAsRecording(isRecording: Boolean) {
        with(viewBinding) {
            pbWaitingIcon.visibility = if (isRecording) View.INVISIBLE else View.VISIBLE
            btnSettings.visibility= if (isRecording) View.INVISIBLE else View.VISIBLE
            btnBackspace.visibility= if (isRecording) View.INVISIBLE else View.VISIBLE
        }
    }

    private fun showMicrophone(show: Boolean) {
        viewBinding.btnMic.visibility = if (show) View.VISIBLE else View.INVISIBLE
    }

    private fun updateMicState() {
        with(viewBinding.btnMic) {
            if (isRecording) {
                progress = 0f
                cancelAnimation()
            } else {
                playAnimation()
            }
            isRecording = !isRecording
        }
    }

    private fun resetMicState() {
        isRecording = false
        viewBinding.btnMic.apply {
            progress = 0f
            cancelAnimation()
        }
    }

}