package kheldiente.app.whispervoiceinput.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kheldiente.app.whispervoiceinput.R
import kheldiente.app.whispervoiceinput.ui.theme.WhisperVoiceInputTheme
import kheldiente.app.whispervoiceinput.utils.sampleSystemInfo

@Composable
fun MainScreen(viewModel: MainScreenViewModel) {
    MainScreen(
        info = viewModel.systemInfo,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen(info: String) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(8.dp)
        ) {
            Column(verticalArrangement = Arrangement.SpaceEvenly) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Microphone permission",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    Spacer(Modifier.weight(1f))
                    GrantMicPermissionView()
                }
                Divider(
                    modifier = Modifier.padding(top = 4.dp),
                    color = Color.LightGray
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "System Info:",
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            SystemInfoView(info)
        }
    }
}


@Composable
private fun SystemInfoView(info: String) {
    SelectionContainer(
        modifier = Modifier.background(Color.LightGray)
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .verticalScroll(rememberScrollState()),
            text = info
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun GrantMicPermissionView(modifier: Modifier = Modifier) {
    val micPermissionState = rememberPermissionStateSafe(
        permission = android.Manifest.permission.RECORD_AUDIO,
        onPermissionResult = { _ -> }
    )

    if (!micPermissionState.status.isGranted) {
        Button(
            modifier = modifier,
            onClick = { micPermissionState.launchPermissionRequest() }
        ) {
            Text("Grant")
        }
    } else {
        Text(
            text = "Granted",
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

@ExperimentalPermissionsApi
@Composable
fun rememberPermissionStateSafe(permission: String, onPermissionResult: (Boolean) -> Unit = {}) = when {
    LocalInspectionMode.current -> remember {
        object : PermissionState {
            override val permission = permission
            override val status = PermissionStatus.Granted
            override fun launchPermissionRequest() = Unit
        }
    }
    else -> rememberPermissionState(permission, onPermissionResult)
}

@Preview("System Info")
@Composable
fun MainScreenPreview() {
    WhisperVoiceInputTheme {
        MainScreen(info = sampleSystemInfo)
    }
}