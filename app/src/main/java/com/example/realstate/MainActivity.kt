package com.example.realstate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.example.realstate.navigation.AppNavigation
import com.example.realstate.ui.theme.RealStateTheme
import androidx.compose.runtime.Composable as Composable1
import androidx.compose.ui.tooling.preview.Preview as Preview1

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RealStateTheme {
                AppNavigation()
            }
        }
    }
}

@Composable1
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview1(showBackground = true)
@Composable1
fun GreetingPreview() {
    RealStateTheme {
        Greeting("Android")
    }
}