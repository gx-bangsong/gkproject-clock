package com.gkprojct.clock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


// Import shared definitions from SharedDefinitions.kt (if any are needed here, like Theme or common icons)
// import com.gkprojct.clock.SharedDefinitions.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BedtimeIntroScreen(
    onGetStartedClick: () -> Unit // Callback for the Get started button
) {
    // This screen does NOT have the bottom navigation bar


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bedtime") }
                // Initial screen screenshot does not show a menu icon
                // actions = { IconButton(onClick = { /* ... */ }) { Icon(Icons.Default.MoreVert, contentDescription = "More options") } }
            )
        },
        // No FAB on this screen
        // No bottomBar on this setup screen
    ) { paddingValues ->
        // Main screen content area
        Column(
            modifier = Modifier
                .padding(paddingValues) // Apply padding from Scaffold
                .fillMaxSize()
                .padding(horizontal = 24.dp), // Add larger horizontal padding
            horizontalAlignment = Alignment.CenterHorizontally, // Center content horizontally
            verticalArrangement = Arrangement.SpaceAround // Distribute major items vertically
        ) {
            // Use weights to control vertical distribution
            Spacer(modifier = Modifier.weight(0.5f)) // Space at the top

            // Title text
            Text(
                text = "Set a consistent bedtime for better sleep",
                fontSize = 24.sp, // Larger font
                fontWeight = FontWeight.SemiBold, // Semi-bold or bold
                textAlign = TextAlign.Center, // Center text within its bounds
                modifier = Modifier.fillMaxWidth() // Allow text to take full width for centering
            )

            Spacer(modifier = Modifier.height(16.dp)) // Space between title and description

            // Description text
            Text(
                text = "Choose a regular bedtime, disconnect from your device, and listen to soothing sounds",
                fontSize = 16.sp, // Normal font
                textAlign = TextAlign.Center, // Center text
                color = MaterialTheme.colorScheme.onSurfaceVariant, // Muted color
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f)) // Space between description and illustration

            // Illustration Area (Placeholder using an Image Composable)
            // You need to add the illustration graphic as a drawable resource in your project
            // Replace R.drawable.bedtime_illustration with your actual drawable resource ID
//            Image(
//                // TODO: Replace with your actual drawable ID for the bedtime illustration
//                // Example: painter = painterResource(id = com.gkprojct.clock.R.drawable.bedtime_illustration),
//                painter = painterResource(id = R.drawable.bedtime_illustration_placeholder), // Using a placeholder name
//                contentDescription = "Illustration of a person sleeping", // Accessibility description
//                modifier = Modifier
//                    .fillMaxWidth(0.8f) // Illustration width 80% of container
//                    .aspectRatio(1.2f) // Adjust aspect ratio to match screenshot if needed
//                    .align(Alignment.CenterHorizontally), // Ensure illustration is centered
//                contentScale = ContentScale.Fit // Fit image within bounds
//            )

            Spacer(modifier = Modifier.weight(1f)) // Space between illustration and button

            // "Get started" Button
            Button(
                onClick = onGetStartedClick, // Use the provided callback
                modifier = Modifier.fillMaxWidth(0.8f) // Button width 80% of container
            ) {
                Text("Get started")
            }

            Spacer(modifier = Modifier.weight(0.5f)) // Space at the bottom
        }
    }
}


// --- Preview ---
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Bedtime Intro Dark")
@Composable
fun BedtimeIntroScreenPreview() {
    // For previewing, you might need a placeholder drawable resource
    // named 'bedtime_illustration_placeholder' or replace the Image
    // with a simple Box or Icon for visual structure testing.
    MaterialTheme { // Use MaterialTheme or your app's theme
        BedtimeIntroScreen(onGetStartedClick = {}) // Pass an empty lambda for preview
    }
}

@Preview(showBackground = true, name = "Bedtime Intro Light")
@Composable
fun BedtimeIntroScreenPreviewLight() {
    // For previewing, you might need a placeholder drawable resource
    MaterialTheme { // Use MaterialTheme or your app's theme
        BedtimeIntroScreen(onGetStartedClick = {}) // Pass an empty lambda for preview
    }
}