package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import com.example.ui.AppNavigation
import com.example.viewmodel.MainViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Safely manual-initialize Firebase to prevent startup crash due to missing google-services.json
    try {
      if (FirebaseApp.getApps(this).isEmpty()) {
        val options = FirebaseOptions.Builder()
          .setApplicationId("1:1234567890:android:1234567890")
          .setApiKey("AIzaSyDummyKeyForStartup-OK")
          .setProjectId("dummy-project-id")
          .setGcmSenderId("1234567890")
          .build()
        FirebaseApp.initializeApp(this, options)
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }

    enableEdgeToEdge()
    setContent {
      val mainViewModel: MainViewModel = viewModel()
      AppNavigation(viewModel = mainViewModel)
    }
  }
}
