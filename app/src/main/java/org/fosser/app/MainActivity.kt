package org.fosser.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import org.fosser.app.data.repository.DonationRepository
import org.fosser.app.navigation.FosserNavGraph
import org.fosser.app.ui.components.DonateDialog
import org.fosser.app.ui.theme.FosserTheme
import org.fosser.app.util.Browser

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as FosserApp).container
        // Debug/test hook: force the donation dialog regardless of open count.
        // adb shell am start -n org.fosser.app/.MainActivity --ez fosser_force_donate true
        val forceDonate = intent.getBooleanExtra(EXTRA_FORCE_DONATE, false)
        setContent {
            val themeSettings by container.themeRepository.settings.collectAsState()
            val context = LocalContext.current
            var showDonate by remember {
                mutableStateOf(forceDonate || container.donationRepository.shouldPrompt())
            }
            FosserTheme(themeSettings) {
                Surface(Modifier.fillMaxSize()) {
                    FosserNavGraph(container)
                }
                if (showDonate) {
                    DonateDialog(
                        onDonate = {
                            if (Browser.open(context, DonationRepository.DONATE_URL)) {
                                container.donationRepository.markDonated()
                            }
                            showDonate = false
                        },
                        onLater = { showDonate = false },
                        onNever = {
                            container.donationRepository.markNeverAsk()
                            showDonate = false
                        },
                    )
                }
            }
        }
    }

    companion object {
        const val EXTRA_FORCE_DONATE = "fosser_force_donate"
    }
}
