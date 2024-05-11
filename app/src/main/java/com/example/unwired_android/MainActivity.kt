package com.example.unwired_android

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import com.example.unwired_android.ui.LoginActivity
import com.example.unwired_android.ui.utils.LoaderCircular
import com.example.unwired_android.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    private val loginActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Do something here
                Toast.makeText(this, "Logged In", Toast.LENGTH_SHORT).show()
                mainViewModel.tokenValid()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            //UnwiredandroidTheme {
            // A surface container using the 'background' color from the theme
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                MainContent()
            }
            //}
        }
    }

    @Composable
    fun MainContent() {
        val tokenValid by mainViewModel.isTokenValid.observeAsState()

        SideEffect {
            mainViewModel.tokenValid()
        }

        when (tokenValid) {
            true -> {
                Text("Token Valid")
            }

            false -> {
                loginActivity.launch(
                    LoginActivity.getIntent(this)
                )
            }

            // Loading indicator
            else -> {
                LoaderCircular()
            }
        }

    }
}
