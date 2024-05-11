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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unwired_android.api.UserStore
import com.example.unwired_android.ui.LoginActivity
import com.example.unwired_android.ui.utils.LoaderCircular
import com.example.unwired_android.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private val testViewModel: TestViewModel by viewModels()

    private val loginActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Do something here
                Toast.makeText(this, "Logged In", Toast.LENGTH_SHORT).show()
                mainViewModel.tokenValid()
            } else {
                mainViewModel.tokenValid()
                println("BACK")
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
            //Thread.sleep(1000)
            testViewModel.removeToken()
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

@HiltViewModel
class TestViewModel @Inject constructor(
    private val userStore: UserStore
) : ViewModel() {
    private val _isTokenValid = MutableLiveData<Boolean>()
    val isTokenValid: LiveData<Boolean> = _isTokenValid

    fun removeToken() {
        viewModelScope.launch {
            try {
                userStore.deleteToken()
            } catch (e: Exception) {
                // Handle error
                _isTokenValid.postValue(false)
            }
        }
    }
}