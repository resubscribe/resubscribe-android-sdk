import ai.resubscribe.android.sdk.ResubscribeActivity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.util.Locale

@Parcelize
enum class AIType : Parcelable {
    Intent, Churn
}
fun AIType.toLowercaseString(): String {
    return this.name.lowercase()
}

@Parcelize
data class ResubscribeColors(
    val primary: @RawValue Color,
    val text: @RawValue Color,
    val background: @RawValue Color
) : Parcelable

@Parcelize
data class ResubscribeOptions(
    val slug: String,
    val apiKey: String,
    val aiType: AIType,
    val userId: String,
    val userEmail: String? = null,
    val title: String? = null,
    val description: String? = null,
    val primaryButtonText: String? = null,
    val cancelButtonText: String? = null,
    val onClose: ((String) -> Unit)? = null,
    val colors: ResubscribeColors? = null
) : Parcelable

@Composable
fun ResubscribeComponent(options: ResubscribeOptions, onClose: (String) -> Unit) {
    var state by remember { mutableStateOf("confirming") }
    val coroutineScope = rememberCoroutineScope()

    when (state) {
        "confirming" -> ConfirmDialog(options) { via ->
            if (via == "close") {
                state = "open"
                coroutineScope.launch {
                    registerConsent(options)
                }
            } else {
                onClose(via)
            }
        }
        "open" -> ChatDialog(options) {
            onClose("close")
        }
    }
}

@Composable
fun ConfirmDialog(options: ResubscribeOptions, onAction: (String) -> Unit) {
    Dialog(onDismissRequest = { onAction("cancel-consent") }) {
        Card(
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = options.title ?: getTitle(options.aiType),
                    color = options.colors?.text ?: Color.Black,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = options.description ?: getDescription(options.aiType),
                    color = options.colors?.text?.copy(alpha = 0.8f) ?: Color.Gray,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { onAction("cancel-consent") },
                        colors = ButtonDefaults.buttonColors()
                    ) {
                        Text(options.cancelButtonText ?: "Not right now", fontSize = 16.sp)
                    }
                    Button(
                        onClick = { onAction("close") },
                        colors = ButtonDefaults.buttonColors()
                    ) {
                        Text(options.primaryButtonText ?: "Let's chat!", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatDialog(options: ResubscribeOptions, onClose: () -> Unit) {
    val url = remember(options) {
        buildChatUrl(options)
    }

    var showConfirmationDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        webViewClient = WebViewClient()
                        loadUrl(url)
                    }
                },
                modifier = Modifier.fillMaxSize().imePadding()
            )
            IconButton(
                onClick = {
                    showConfirmationDialog = true
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.Gray
                )
            }
            if (showConfirmationDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showConfirmationDialog = false
                    },
                    title = { Text("Close Chat?") },
                    text = { Text("Are you sure you want to close the chat?") },
                    confirmButton ={
                        Button(onClick = onClose) {
                            Text("Yes")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showConfirmationDialog = false }) {
                            Text("No")
                        }
                    }
                )
            }
        }
    }
}

fun buildChatUrl(options: ResubscribeOptions): String {
    val queryParams = mapOf(
        "ait" to options.aiType.toLowercaseString(),
        "uid" to options.userId,
        "email" to options.userEmail,
        "iframe" to "true",
        "hideclose" to "true"
    )
    val queryString = queryParams.entries
        .filter { (_, value) -> value != null }
        .joinToString("&") { (key, value) -> "$key=${Uri.encode(value.toString())}" }

    return "https://app.resubscribe.ai/chat/${options.slug}?$queryString#apiKey=${options.apiKey}"
}

fun getTitle(aiType: AIType): String {
    return when (aiType) {
        AIType.Intent -> "Not ready to pay?"
        AIType.Churn -> "We're sorry to see you go"
//        is AIType.Subscriber -> "Would you like to tell us about your experience?"
//        is AIType.Presubscription, is AIType.Precancel -> "Can we ask you a few questions?"
        else -> {throw RuntimeException("Unexpected aiType ${aiType}")}
    }
}

fun getDescription(aiType: AIType): String {
    return when (aiType) {
        AIType.Intent, AIType.Churn ->
            "Can we ask you a few questions? It should only take a few minutes."
//        is AIType.Presubscription, is AIType.Precancel ->
//            "We'd love to hear your thoughts. It should only take a few minutes."
        else -> {throw RuntimeException("Unexpected aiType ${aiType}")}
    }
}

suspend fun registerConsent(options: ResubscribeOptions) {
    val params = mapOf(
        "slug" to options.slug,
        "uid" to options.userId,
        "email" to options.userEmail,
        "ait" to options.aiType.toLowercaseString(),
        "brloc" to Locale.getDefault().language
    )

    try {
        val client = HttpClient()
        val response: HttpResponse = client.get("https://api.resubscribe.ai/sessions/consent") {
            url {
                params.filterValues { it != null }.forEach {
                    parameters.append(it.key, it.value.toString())
                }
            }
            header("Authorization", "Bearer ${options.apiKey}")
        }
        
//        if (response.status.isSuccess()) {
//            Log.d("ResubscribeSDK", "Consent registered successfully")
//        } else {
//            Log.e("ResubscribeSDK", "Failed to register consent: ${response.status}")
//        }
    } catch (e: Exception) {
        Log.e("ResubscribeSDK", "Failed to fetch sessions/consent: ${e.message}")
    }
}

object Resubscribe {
    fun openWithConsent(context: Context, options: ResubscribeOptions) {
        Log.d("TAG", "OPTIONS: ${options}")
        val intent = Intent(context, ResubscribeActivity::class.java).apply {
            putExtra(ResubscribeActivity.EXTRA_OPTIONS, options)
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        }
        context.startActivity(intent)
    }

//    object headless {
//        private var headlessOptions: ResubscribeOptions? = null
//
//        fun setOptions(options: ResubscribeOptions) {
//            headlessOptions = options
//        }
//
//        fun openChat(context: Context, partialOptions: ResubscribeOptions? = null) {
//            val options = headlessOptions?.copy() ?: return
//            partialOptions?.let { partial ->
//                // Merge partial options with headless options
//                options.copy(
//                    title = partial.title ?: options.title,
//                    description = partial.description ?: options.description,
//                    // ... (merge other fields as needed)
//                )
//            }
//            openWithConsent(context, options)
//        }
//
//        fun registerConsentRequest(context: Context) {
//            headlessOptions?.let { options ->
//                // Implement consent registration logic here
//            }
//        }
//    }
}