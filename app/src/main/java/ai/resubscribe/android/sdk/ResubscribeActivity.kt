package ai.resubscribe.android.sdk

import ResubscribeComponent
import ResubscribeOptions
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class ResubscribeActivity : ComponentActivity() {
    private lateinit var options: ResubscribeOptions

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        options = intent.getParcelableExtra(EXTRA_OPTIONS)!!

        setContent {
            ResubscribeComponent(options) { via ->
                options.onClose?.invoke(via)
                finish()
            }
        }
    }

    companion object {
        const val EXTRA_OPTIONS = "extra_options"
    }
}