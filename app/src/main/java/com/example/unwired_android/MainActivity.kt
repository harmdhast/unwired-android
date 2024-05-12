package com.example.unwired_android

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.unwired_android.api.UserStore
import com.example.unwired_android.ui.theme.UnwiredandroidTheme
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
            if (result.resultCode == RESULT_OK) {
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
            UnwiredandroidTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContent()
                }
            }
        }
    }

    @Composable
    fun MainContent() {

        val isServerUp by mainViewModel.canReachServer.observeAsState()

        // State of bottomBar, set state to false, if current page route is "car_details"
        val bottomBarState = rememberSaveable { (mutableStateOf(true)) }

// State of topBar, set state to false, if current page route is "car_details"
        val topBarState = rememberSaveable { (mutableStateOf(true)) }


        // Error dialog
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder
            .setTitle("Error")
            .setMessage("Couldn't reach the server. Check your internet connection and try again")
            .setNeutralButton("OK") { _, _ ->
                finish() // Exist app on click
            }

        val dialog: AlertDialog = builder.create()


        LaunchedEffect(Unit) {
            mainViewModel.pingServer(API_IP, API_PORT)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(R.drawable.unwired_white),
                "Logo",
                modifier = Modifier
                    .fillMaxWidth(.8f)
                    .fillMaxHeight(.4f)
            )
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                when (isServerUp) {
                    true -> {
                        LoginPage()
                    }

                    false -> {
                        dialog.show()
                    }

                    else -> {
                        LoaderCircular()
                    }
                }
            }

        }
    }

    @Composable
    fun LoginPage() {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val tokenValid by mainViewModel.isTokenValid.observeAsState()

        LaunchedEffect(Unit) {
            testViewModel.removeToken()
            mainViewModel.tokenValid()
        }

        when (tokenValid) {
            true -> {
                Text("Token Valid")
            }

            false -> {
                NavHost(
                    navController = navController,
                    startDestination = "main",
                ) {
                    composable("main") {
                        MainRoute(
                            { navController.navigate("login") },
                            { navController.navigate("register") })
                    }
                    composable("login") {
                        LoginRoute()
                    }
                    composable("register") {
                        RegisterRoute()
                    }
                }
            }

            else -> {
                LoaderCircular()
            }
        }
    }
}

@Composable
fun LoginRoute() {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(.8f)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                // Call ViewModel's login method
                // viewModel.login(username, password, context)
            },
            //modifier = Modifier.fillMaxWidth(),
            //enabled = loginResult != true
        ) {
//            if (loginResult == true) {
//                CircularProgressIndicator()
//            } else {
            Text("Login")
//            }
        }
    }
}

@Composable
fun RegisterRoute() {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(.8f)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                // Call ViewModel's login method
                // viewModel.login(username, password, context)
            },
            //modifier = Modifier.fillMaxWidth(),
            //enabled = loginResult != true
        ) {
//            if (loginResult == true) {
//                CircularProgressIndicator()
//            } else {
            Text("Register")
//            }
        }
    }
}

@Composable
fun MainRoute(
    onClickLogin: () -> Unit,
    onClickRegister: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Button(
            onClick = onClickLogin,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = "Login",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )

        }
        Button(onClick = onClickRegister) {
            Text(
                text = "Register",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
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