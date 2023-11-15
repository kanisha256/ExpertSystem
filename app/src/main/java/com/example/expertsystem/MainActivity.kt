package com.example.expertsystem

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, start = 16.dp)
                    ) {
                        Button(
                            onClick = {
                                val intent =
                                    Intent(this@MainActivity, UserI::class.java)
                                this@MainActivity.startActivity(intent)
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Text("")
                        }
                    }
                    Demo_ExposedDropdownMenuBox()

                }
            }
        }
    }
}

