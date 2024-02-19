package com.example.assignment1_new

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val (showInKilometers, setShowInKilometers) = remember { mutableStateOf(true) }
            val (nextStopReached, setNextStopReached) = remember { mutableStateOf(false) }
            var currentStopIndex by remember { mutableStateOf(0) }
            var totalDistanceCovered by remember { mutableStateOf(0f) }
            val distanceRegex = Regex("[0-9.]+") // Match any sequence of digits or decimals
            var totalDistanceLeft by remember {
                mutableStateOf(
                    stations.sumOf { station ->
                        val distanceString =
                            if (showInKilometers) station.distanceKm else station.distanceMiles
                        distanceRegex.find(distanceString)?.value?.toDouble() ?: 0.0
                    }.toFloat()
                )
            }

            val stationsState = remember { mutableStateOf(stations.toMutableList()) }
            val reachedStations = remember { mutableStateOf(List(stations.size) { false }) }

            Column(modifier = Modifier.background(Color(0xFF535C91))) {
                CustomTopAppBar()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (stationsState.value.isNotEmpty()) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            JourneyProgressIndicator(
                                stations = stations,
                                showInKilometers = showInKilometers,
                                currentStopIndex = currentStopIndex,
                                totalDistanceCovered = totalDistanceCovered,
                                totalDistanceLeft = totalDistanceLeft,
                                reachedStations = reachedStations.value
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    onClick = {
                                        setShowInKilometers(!showInKilometers)
                                        // Update total distance left based on the changed distance unit
                                        totalDistanceLeft = stations.sumOf { station ->
                                            val distanceString =
                                                if (showInKilometers) station.distanceKm else station.distanceMiles
                                            distanceRegex.find(distanceString)?.value?.toDouble() ?: 0.0
                                        }.toFloat()
                                    },
                                    modifier = Modifier
                                        .padding(start = 4.dp, top = 8.dp)
                                        .weight(1f)
                                ) {
                                    Text("Show in Miles")
                                }
                                Button(
                                    onClick = {
                                        if (!nextStopReached) {
                                            setNextStopReached(true)
                                            // Calculate the distance to the next stop
                                            val distanceToNextStop = if (showInKilometers)
                                                stations[currentStopIndex].distanceKm.split(" ")[0].toFloat()
                                            else
                                                stations[currentStopIndex].distanceMiles.split(" ")[0].toFloat()
                                            // Add the distance to the total distance covered
                                            totalDistanceCovered += distanceToNextStop
                                            // Subtract the distance to the next stop from the total distance left
                                            totalDistanceLeft -= distanceToNextStop
                                            // Remove the reached station
                                            stationsState.value.removeAt(0)
                                            // Increment the current stop index
                                            currentStopIndex++
                                            // Set the flag for the next stop reached
                                            reachedStations.value = reachedStations.value.mapIndexed { index, reached ->
                                                index == currentStopIndex
                                            }
                                            // Reset the flag for the next stop reached
                                            setNextStopReached(false)
                                        }
                                    },
                                    modifier = Modifier
                                        .padding(top = 8.dp, end = 4.dp)
                                        .weight(1f)
                                ) {
                                    Text("Next Stop Reached")
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "No more stations remaining.",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                }
                StationList(stationsState.value, showInKilometers)
            }
        }

    }
}

@Composable
fun DistanceUnitToggle(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Button(
        onClick = { onCheckedChange(!checked) },
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(if (checked) "Show in Miles" else "Show in Kilometers")
    }
}

@Composable
fun CustomTopAppBar() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        color = Color(0xFFA7D397), // Change the color as per your theme
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Journey Assistant",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFFF5EEC8), // Change the color as per your theme
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun CustomProgressBar(
    progress: Float,
    reachedStations: List<Boolean>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Station Dots Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            reachedStations.forEachIndexed { index, reached ->
                StationDot(index, progress, reached)
            }
        }

        // Linear Progress Indicator
        LinearProgressIndicator(
            progress = progress + 0.01f,
            color = Color(0xFFF5E8C7),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
        )
    }
}

@Composable
fun StationList(stations: List<Station>, showInKilometers: Boolean) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        itemsIndexed(stations) { index, station ->
            StationItem(index, station, showInKilometers)
        }
    }
}

@Composable
fun StationItem(index: Int, station: Station, showInKilometers: Boolean) {
    val distance = if (showInKilometers) station.distanceKm else station.distanceMiles
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = CircleShape,
        color = Color.LightGray,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = station.name,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start,
                color = Color.Black,
                fontSize = 16.sp
            )
            Text(
                text = distance,
                textAlign = TextAlign.End,
                color = Color.Black,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun JourneyProgressIndicator(
    stations: List<Station>,
    showInKilometers: Boolean,
    currentStopIndex: Int,
    totalDistanceCovered: Float,
    totalDistanceLeft: Float,
    reachedStations: List<Boolean>
) {
    Surface(
        modifier = Modifier.padding(16.dp),
        color = Color.White,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Text indicating current stop and distance
            Text(
                text = "Current Stop: ${stations[currentStopIndex].name} - ${
                    if (showInKilometers) stations[currentStopIndex].distanceKm
                    else stations[currentStopIndex].distanceMiles
                }",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Text indicating total distance covered and total distance left
            Text(
                text = "Total Distance Covered: ${
                    if (showInKilometers) "$totalDistanceCovered km"
                    else "${totalDistanceCovered * 0.621371} mi"
                }",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Total Distance Left: ${
                    if (showInKilometers) "$totalDistanceLeft km"
                    else "${totalDistanceLeft * 0.621371} mi"
                }",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Progress Bar
            CustomProgressBar(
                progress = totalDistanceCovered / (totalDistanceCovered + totalDistanceLeft),
                reachedStations = reachedStations
            )
        }
    }
}

@Composable
fun StationDot(
    index: Int,
    progress: Float,
    reached: Boolean
) {
    val dotColor = if (reached) Color(0xFFFF57D1F) else Color.Gray

    Box(
        modifier = Modifier
            .size(16.dp)
            .padding(horizontal = 2.dp)
            .clip(CircleShape)
            .background(dotColor),
    )
}

data class Station(val name: String, val distanceKm: String, val distanceMiles: String)

val stations = listOf(
    Station("Station A", "2 km", "1.2 mi"),
    Station("Station B", "5 km", "3.1 mi"),
    Station("Station C", "8 km", "5 mi"),
    Station("Station D", "11 km", "6.8 mi"),
    Station("Station E", "14 km", "8.7 mi"),
    Station("Station F", "17 km", "10.6 mi"),
    Station("Station G", "20 km", "12.4 mi"),
    Station("Station H", "23 km", "14.3 mi"),
    Station("Station I", "26 km", "16.2 mi"),
    Station("Station J", "29 km", "18 mi")
)
