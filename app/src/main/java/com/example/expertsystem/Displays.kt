package com.example.expertsystem

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.GraphDatabase
import org.neo4j.driver.Session
import org.neo4j.driver.exceptions.ClientException
import java.util.concurrent.CompletableFuture

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Demo_ExposedDropdownMenuBox() {
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf("") }
    var customCategory by remember { mutableStateOf("") }
    var categories by remember { mutableStateOf(emptyList<String>()) }
    var name by remember { mutableStateOf("") }
    var keyword by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val categoriesFuture = getCategoriesFromNeo4j()
    categories = categoriesFuture.get()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(32.dp)
    ) {
        // ExposedDropdownMenu
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            // TextField with IntrinsicSize.Max
            TextField(
                value = selectedText,
                onValueChange = {
                    selectedText = it
                    customCategory = it
                },
                readOnly = false,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .width(IntrinsicSize.Max)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .width(IntrinsicSize.Max)
            ) {
                categories.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(text = item) },
                        onClick = {
                            selectedText = item
                            expanded = false
                        }
                    )
                }

                // Option to enter a custom category
                DropdownMenuItem(
                    text = { Text("Enter your category") },
                    onClick = {
                        selectedText = "Enter your category"
                        expanded = false
                    }
                )
            }
        }

        // If "Enter your category" is selected, show a TextField for custom input
        if (selectedText == "Enter your category") {
            TextField(
                value = customCategory,
                onValueChange = { customCategory = it },
                label = { Text("Enter your category") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )
        }

        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Введите название") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

        // First TextField
        TextField(
            value = keyword,
            onValueChange = { keyword = it },
            label = { Text("Введите ключевые слова") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

        // Second TextField
        TextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Введите описание") },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(top = 16.dp)
                .weight(1f)
        )

        // Button
        Button(
            onClick = {
                addCategoryToNeo4j(customCategory, name, keyword, description)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Нажми меня")
        }
    }
}

private fun getCategoriesFromNeo4j(): CompletableFuture<List<String>> {
    val future = CompletableFuture<List<String>>()

    Thread {
        try {
            val driver = GraphDatabase.driver(
                "neo4j+ssc://530c5a24.databases.neo4j.io:7687",
                AuthTokens.basic("neo4j", "b2Tp5SzZ-H566nGBdocqsC_Zagvi_DMP3-T2i_kathc")
            )
            driver.use { driver ->
                val session: Session = driver.session()
                val query = "MATCH (c:Category) RETURN c.name AS categoryName"
                val result = session.run(query)
                val categories = result.list { it["categoryName"].asString() }
                future.complete(categories)
            }
        } catch (e: ClientException) {
            e.printStackTrace()
            future.complete(emptyList())
        }
    }.start()
    return future
}

fun addCategoryToNeo4j(category: String, name: String, keyword: String, description: String) {
    Thread {
        try {
            val driver = GraphDatabase.driver(
                "neo4j+ssc://530c5a24.databases.neo4j.io:7687",
                AuthTokens.basic("neo4j", "b2Tp5SzZ-H566nGBdocqsC_Zagvi_DMP3-T2i_kathc")
            )

            driver.use { driver ->
                val session: Session = driver.session()
                val createCategoryQuery = "CREATE (:Category {name: \$category})".trimIndent()
                session.writeTransaction {
                    it.run(createCategoryQuery, mapOf("category" to category))
                }

                val createNameQuery = "MATCH (c:Category {name: \$category}) CREATE (u:Util {name: \$name, keyword: \$keyword, description: \$description})-[:HAS_UTIL]->(c)"
                session.writeTransaction {
                    it.run(createNameQuery, mapOf("category" to category, "name" to name,"keyword" to keyword,"description" to description))
                }
            }
        } catch (e: ClientException) {
            e.printStackTrace()
        }
    }.start()
}