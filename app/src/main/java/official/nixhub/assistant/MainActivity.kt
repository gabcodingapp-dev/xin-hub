package official.nixhub.assistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import official.nixhub.assistant.ui.XinRoot
import official.nixhub.assistant.ui.theme.Void
import official.nixhub.assistant.ui.theme.XinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Xin.init(this)
        setContent {
            XinTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Void) {
                    XinRoot()
                }
            }
        }
    }
}
