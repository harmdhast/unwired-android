package com.example.unwired_android

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.unwired_android.ui.theme.UnwiredandroidTheme
import com.example.unwired_android.ui.utils.LoaderCircular
import com.example.unwired_android.viewmodels.LoginViewModel
import com.example.unwired_android.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

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
            mainViewModel.tokenValid()
        }

        when (tokenValid) {
            true -> {
                startActivity(Intent(this@MainActivity, GroupActivity::class.java))
            }

            false -> {
                NavHost(
                    navController = navController,
                    startDestination = "main",
                    enterTransition = { fadeIn(animationSpec = tween(400)) },
                    exitTransition = { fadeOut(animationSpec = tween(400)) }
                ) {
                    composable("main") {
                        MainRoute(
                            { navController.navigate("login") },
                            { navController.navigate("register") })
                    }
                    composable("login") {
                        LoginRoute(navController = navController)
                    }
                    composable("register") {
                        RegisterRoute(navController = navController)
                    }
                }
            }

            else -> {
                LoaderCircular()
            }
        }
    }

    @Composable
    fun LoginRoute(navController: NavHostController) {
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        val focusManager = LocalFocusManager.current
        val loginViewModel: LoginViewModel = hiltViewModel()
        val loginResult by loginViewModel.loginResult.observeAsState()
        var isLoading by remember { mutableStateOf(false) }

        LaunchedEffect(loginResult) {
            if (loginResult == true) {
                startActivity(Intent(this@MainActivity, GroupActivity::class.java))
                navController.navigate("main") // Reset nav route
            }
        }

        fun tryLogIn() {
            isLoading = true
            try {
                loginViewModel.login(username, password)
            } catch (_: Exception) {

            }
            isLoading = false
        }

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(.8f)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isLoading) {
                TextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardActions = KeyboardActions {
                        focusManager.moveFocus(FocusDirection.Next)
                    },
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardActions = KeyboardActions {
                        tryLogIn()
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        tryLogIn()
                    },
                    enabled = !isLoading
                ) {
                    Text("Login")
                }
            } else {
                LoaderCircular()
            }

        }
    }

    @Composable
    fun RegisterRoute(navController: NavHostController) {
        var username by remember { mutableStateOf("") }
        var usernameErr by remember { mutableStateOf(false) }
        var password by remember { mutableStateOf("") }
        var passwordErr by remember { mutableStateOf(false) }
        var confirmPassword by remember { mutableStateOf("") }
        var confirmPasswordErr by remember { mutableStateOf(false) }
        var errorMsg by remember { mutableStateOf("") }
        val focusManager = LocalFocusManager.current
        val loginViewModel: LoginViewModel = hiltViewModel()
        val registerStatus by loginViewModel.registerResult.observeAsState()
        val registerError by loginViewModel.registerErr.observeAsState()

        fun String.isValidUsernameCharacters(): Boolean = this.matches(Regex("[A-Za-z0-9]+"))
        fun String.isProperLength(): Boolean = this.length in 6..20

        fun resetPW() {
            password = ""
            confirmPassword = ""
        }

        fun doRegister() {
            errorMsg = ""
            usernameErr = false
            passwordErr = false
            confirmPasswordErr = false

            if (!username.isProperLength() || !username.isValidUsernameCharacters()) {
                usernameErr = true
                errorMsg =
                    "Votre nom d'utilisateur doit contenir entre 6 et 20 caractères et ne doit pas contenir de caractères spéciaux."
                resetPW()
                return
            }

            if (!password.isProperLength()) {
                passwordErr = true
                errorMsg = "Votre mot de passe doit contenir entre 6 et 20 caractères."
                resetPW()
                return
            }

            if (password != confirmPassword) {
                passwordErr = true
                confirmPasswordErr = true
                errorMsg = "Les deux mots de passe ne correspondent pas."
                resetPW()
                return
            }

            loginViewModel.register(username, password)
        }

        LaunchedEffect(key1 = registerStatus) {
            when (registerStatus) {
                true -> {
                    navController.navigate("main")
                    //startActivity(Intent(this@MainActivity, GroupActivity::class.java))
                }

                false -> {
                    if (registerError != "" && registerError != null) {
                        errorMsg = registerError as String
                    }
                }

                else -> {
                    println("else")
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(.8f)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = errorMsg, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = usernameErr,
                keyboardActions = KeyboardActions {
                    focusManager.moveFocus(FocusDirection.Next)
                },
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                isError = passwordErr,
                keyboardActions = KeyboardActions {
                    focusManager.moveFocus(FocusDirection.Next)
                },
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                isError = confirmPasswordErr,
                keyboardActions = KeyboardActions {
                    doRegister()
                },
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    doRegister()
                },
            ) {
                Text("Register")
            }
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
        ) {
            Text(
                text = "I have an account",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )

        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onClickRegister,
        ) {
            Text(
                text = "I want to register",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
