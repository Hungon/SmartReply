package com.trials.smartreply

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseTextMessage
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestionResult

class MainActivity : AppCompatActivity() {

    private var conversation = ArrayList<FirebaseTextMessage>()
    private lateinit var textMessage: TextView
    private lateinit var editInputLocal: EditText
    private lateinit var buttonSendLocal: Button
    private lateinit var editInputRemote: EditText
    private lateinit var buttonSendRemote: Button
    private lateinit var textResult: TextView
    private var messages = ArrayList<String>()
    private var userId = "101"
    private var handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    private fun initView() {
        textMessage = findViewById(R.id.text_message)
        editInputLocal = findViewById(R.id.edit_input_local)
        buttonSendLocal = findViewById(R.id.button_send_local)
        editInputRemote = findViewById(R.id.edit_input_remote)
        buttonSendRemote = findViewById(R.id.button_send_remote)
        textResult = findViewById(R.id.text_message)

        buttonSendLocal.setOnClickListener {
            send(userId, editInputLocal.text.toString(), true)
        }

        buttonSendRemote.setOnClickListener {
            send(userId, editInputRemote.text.toString(), false)
        }
    }

    private fun send(userId: String = "", mes: String, local: Boolean) {
        if (mes.isEmpty()) return
        if (local) {
            messages.add(mes)
            // append message in local
            textResult.append(mes)
            conversation.add(
                FirebaseTextMessage.createForLocalUser(
                    "heading out now", System.currentTimeMillis()
                )
            )
        } else {
            conversation.add(
                FirebaseTextMessage.createForRemoteUser(
                    mes, System.currentTimeMillis(), userId
                )
            )
        }
        handler.postDelayed({
            getSuggestion()
        }, 1000L)
    }

    private fun getSuggestion() {
        val smartReply = FirebaseNaturalLanguage.getInstance().smartReply
        smartReply.suggestReplies(conversation)
            .addOnSuccessListener { result ->
                if (result.status == SmartReplySuggestionResult.STATUS_NOT_SUPPORTED_LANGUAGE) {
                    // The conversation's language isn't supported, so the
                    // the result doesn't contain any suggestions.
                    textResult.text = getString(R.string.nlp_result_not_supported_language)
                } else if (result.status == SmartReplySuggestionResult.STATUS_SUCCESS) {
                    for (suggestion in result.suggestions) {
                        // Task completed successfully
                        textResult.append("${suggestion.text}\n")
                    }
                }
            }
            .addOnFailureListener {
                // Task failed with an exception
                textResult.text = getString(R.string.nlp_result_failed_get_result)
                Log.e(TAG, it.message)
            }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}
