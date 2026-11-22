package official.nixhub.assistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import official.nixhub.assistant.ui.XinRoot
import official.nixhub.assistant.ui.screens.SplashScreen
import official.nixhub.assistant.ui.theme.Void
import official.nixhub.assistant.ui.theme.XinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Xin.init(this)
        setContent {
            XinTheme {
                var splash by remember { mutableStateOf(true) }
                if (splash) {
                    SplashScreen { splash = false }
                } else {
                    Surface(modifier = Modifier.fillMaxSize(), color = Void) {
                        XinRoot()
                    }
                }
            }
        }
    }
}
