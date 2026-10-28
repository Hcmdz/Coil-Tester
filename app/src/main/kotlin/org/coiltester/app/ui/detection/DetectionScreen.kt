package org.coiltester.app.ui.detection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.coiltester.app.di.AppRoute
import org.coiltester.app.viewmodel.DetectionIntent
import org.coiltester.app.viewmodel.DetectionViewModel
import org.coiltester.core.model.DetectionStatus

@Composable
fun DetectionScreen(
    viewModel: DetectionViewModel,
    onNavigate: (AppRoute) -> Unit
) {
    val state = viewModel.state.collectAsState().value

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Distance guide
        if (state.showDistanceGuide) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(org.coiltester.app.R.string.distance_guide),
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Detection circle placeholder
        Box(
            modifier = Modifier
                .size(200.dp)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when (state.status) {
                    DetectionStatus.IDLE -> stringResource(org.coiltester.app.R.string.detection_ready)
                    DetectionStatus.CALIBRATING -> stringResource(org.coiltester.app.R.string.detection_calibrating)
                    DetectionStatus.DETECTING -> stringResource(org.coiltester.app.R.string.detection_detecting)
                    DetectionStatus.CONFIRMED_AC -> stringResource(org.coiltester.app.R.string.detection_ac_confirmed)
                    DetectionStatus.CONFIRMED_DC -> stringResource(org.coiltester.app.R.string.detection_dc_confirmed)
                    DetectionStatus.ERROR -> stringResource(org.coiltester.app.R.string.detection_error)
                    else -> stringResource(org.coiltester.app.R.string.detection_error)
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = when (state.status) {
                    DetectionStatus.DETECTING -> Color.Blue
                    DetectionStatus.CONFIRMED_AC -> Color.Green
                    DetectionStatus.CONFIRMED_DC -> Color(0xFFFFA500)
                    DetectionStatus.ERROR -> Color.Red
                    else -> Color.Gray
                }
            )
        }

        // Status text
        Text(
            text = when (state.status) {
                DetectionStatus.IDLE -> stringResource(org.coiltester.app.R.string.detection_ready)
                DetectionStatus.CALIBRATING -> stringResource(org.coiltester.app.R.string.detection_calibrating)
                DetectionStatus.DETECTING -> stringResource(org.coiltester.app.R.string.detection_detecting)
                DetectionStatus.CONFIRMED_AC -> stringResource(org.coiltester.app.R.string.detection_ac_confirmed)
                DetectionStatus.CONFIRMED_DC -> stringResource(org.coiltester.app.R.string.detection_dc_confirmed)
                DetectionStatus.ERROR -> stringResource(org.coiltester.app.R.string.detection_error)
                else -> stringResource(org.coiltester.app.R.string.detection_error)
            },
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )

        // Control buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.sendIntent(DetectionIntent.Calibrate) },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(org.coiltester.app.R.string.detection_calibrate))
            }

            Button(
                onClick = {
                    if (state.status == DetectionStatus.DETECTING) {
                        viewModel.sendIntent(DetectionIntent.Stop)
                    } else {
                        viewModel.sendIntent(DetectionIntent.Start(state.config))
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (state.status == DetectionStatus.DETECTING)
                    stringResource(org.coiltester.app.R.string.detection_stop)
                    else stringResource(org.coiltester.app.R.string.detection_start)
                )
            }
        }

        state.error?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(org.coiltester.app.R.string.detection_error) + ": $error",
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}