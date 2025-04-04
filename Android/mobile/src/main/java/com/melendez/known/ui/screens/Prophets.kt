package com.melendez.known.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.generationConfig
import com.melendez.known.BuildConfig
import com.melendez.known.R
import com.melendez.known.ui.viewmodel.prophets.ProphetsUiState
import com.melendez.known.ui.viewmodel.prophets.ProphetsViewModel

@SuppressLint("ViewModelConstructorInComposable")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Prophets(navTotalController: NavHostController) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.prophets)) },
                navigationIcon = {
                    IconButton(onClick = { navTotalController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        val generativeModel = GenerativeModel(
            modelName = "gemini-pro",
            apiKey = BuildConfig.apiKey,
            generationConfig = generationConfig {
                temperature = 0.9f
                topK = 1
                topP = 1f
                maxOutputTokens = 2048
            },
            safetySettings = listOf(
                SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE),
                SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE),
                SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.LOW_AND_ABOVE),
                SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE),
            )
        )
        val viewModel = ProphetsViewModel(generativeModel)

        AskRoute(paddingValues = paddingValues, knownerViewModel = viewModel)
    }
}

@Composable
internal fun AskRoute(
    paddingValues: PaddingValues,
    knownerViewModel: ProphetsViewModel = viewModel(),
) {
    val knownerUiState by knownerViewModel.uiState.collectAsState()
    ChatArea(
        paddingValues = paddingValues,
        uiState = knownerUiState,
        onSummarizeClicked = { prompt -> knownerViewModel.ask(prompt) }
    )
}

@Composable
fun ChatArea(
    paddingValues: PaddingValues,
    uiState: ProphetsUiState = ProphetsUiState.Initial,
    onSummarizeClicked: (String) -> Unit = {},
) {

    var prompt by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = paddingValues.calculateTopPadding())
    ) {
        Box(modifier = Modifier.verticalScroll(rememberScrollState())) {
            when (uiState) {
                ProphetsUiState.Initial -> {
                    // Nothing is shown
                }

                ProphetsUiState.Loading -> {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                is ProphetsUiState.Success -> {
                    LinearProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.padding(horizontal = 8.dp)) {
                        Icon(
                            Icons.Rounded.Person,
                            contentDescription = stringResource(id = R.string.prophets)
                        )
                        Text(
                            text = uiState.outputText,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                    }
                }

                is ProphetsUiState.Error -> {
                    LinearProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = uiState.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            }
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomEnd)
                    .padding(4.dp),
                label = { Text(text = stringResource(id = R.string.ask_something)) },
                leadingIcon = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            imageVector = Icons.Rounded.Image,
                            contentDescription = stringResource(R.string.upload_ai)
                        )
                    }
                }, trailingIcon = {
                    IconButton(
                        onClick = { onSummarizeClicked(prompt) },
                        enabled = prompt.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = stringResource(R.string.ask)
                        )
                    }
                }
            )
        }
    }
}

@Preview(device = "id:pixel_9_pro")
@Composable
fun Prophets_Preview() {
    Prophets(navTotalController = rememberNavController())
}