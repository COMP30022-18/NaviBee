package au.edu.unimelb.eng.navibee.navigation

import android.app.AlertDialog
import android.app.Dialog
import android.app.SearchManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.speech.RecognizerIntent
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDialogFragment
import android.view.Gravity
import android.widget.TextView
import au.edu.unimelb.eng.navibee.R
import org.jetbrains.anko.bundleOf

class DestinationsVoiceSearchActivity : AppCompatActivity(), VoiceRecognitionCheckListener {

    companion object {
        // Voice recognition activity result ID
        private const val SPEECH_RECOGNITION_RESULT = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startVoiceSearch()
    }

    private fun startVoiceSearch() {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).let {
            it.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
            it.putExtra(RecognizerIntent.EXTRA_PROMPT, resources.getString(R.string.navigation_search_hint))
            startActivityForResult(it, SPEECH_RECOGNITION_RESULT)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            SPEECH_RECOGNITION_RESULT -> {
                if (resultCode == RESULT_OK) {
                    val results = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    VoiceRecognitionCheckFragment().let {
                        it.arguments = bundleOf(
                                VoiceRecognitionCheckFragment.ARGS_RECOGNITION_RESULT to
                                        results[0]
                        )
                        it.show(supportFragmentManager, "recognitionResult")
                    }
                } else {
                    finish()
                }
            }
        }
    }

    override fun onOutcomeCheckRetry() {
        startVoiceSearch()
    }

    override fun onOutcomeCheckOK(result: String) {
        startActivity(
                Intent(this,
                        DestinationsSearchResultActivity::class.java).apply {
                    action = Intent.ACTION_SEARCH
                    putExtra(SearchManager.QUERY, result)
                    putExtra(DestinationsSearchResultActivity.ARGS_SEND_RESULT, true)
                }
        )
        finish()
    }

    override fun onOutcomeCheckCancel() {
        finish()
    }
}

interface VoiceRecognitionCheckListener {
    fun onOutcomeCheckRetry()
    fun onOutcomeCheckOK(result: String)
    fun onOutcomeCheckCancel()
}

class VoiceRecognitionCheckFragment: AppCompatDialogFragment() {
    companion object {
        const val ARGS_RECOGNITION_RESULT = "result"

        private const val BODY_FONT_SIZE = 40f
        private const val BUTTON_FONT_SIZE = 20f
    }

    private lateinit var listener: VoiceRecognitionCheckListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val result = this.arguments?.getString(ARGS_RECOGNITION_RESULT)
        builder.let { it ->
            it.setTitle(R.string.voice_recognition_result_check_title)
            it.setMessage(result)
            it.setPositiveButton(R.string.button_yes) { _, _ ->
                listener.onOutcomeCheckOK(result ?: "")
            }
            it.setNegativeButton(R.string.button_retry) { _, _ ->
                this.dialog.cancel()
                listener.onOutcomeCheckRetry()
            }
            it.setNeutralButton(R.string.button_cancel) { _, _ ->
                this.dialog.cancel()
                listener.onOutcomeCheckCancel()
            }
        }

        return builder.create().apply {
            setOnShowListener {
                val message = findViewById<TextView>(android.R.id.message)
                message.textSize = BODY_FONT_SIZE
                message.gravity = Gravity.CENTER
                message.typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
                getButton(AlertDialog.BUTTON_POSITIVE).textSize = BUTTON_FONT_SIZE
                getButton(AlertDialog.BUTTON_NEGATIVE).textSize = BUTTON_FONT_SIZE
                getButton(AlertDialog.BUTTON_NEUTRAL).textSize = BUTTON_FONT_SIZE
            }
        }
    }

    override fun onCancel(dialog: DialogInterface?) {
        listener.onOutcomeCheckCancel()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            listener = context as VoiceRecognitionCheckListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement SearchResultRetryListener")
        }
    }

}