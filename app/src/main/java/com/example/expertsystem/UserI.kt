package com.example.expertsystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Driver
import org.neo4j.driver.GraphDatabase
import org.neo4j.driver.Session
import org.neo4j.driver.exceptions.ClientException

class UserI : ComponentActivity() {

    private val neo4jDriver by lazy {
        GraphDatabase.driver("neo4j+ssc://530c5a24.databases.neo4j.io:7687", AuthTokens.basic("neo4j", "b2Tp5SzZ-H566nGBdocqsC_Zagvi_DMP3-T2i_kathc"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SearchScreen(driver = neo4jDriver)
        }
    }

    data class SearchResult(val name: String, val description: String)

    suspend fun fuzzySearch(driver: Driver, searchTerm: String): List<SearchResult> {
        return withContext(Dispatchers.IO) {
            val searchQuery =
                "MATCH (u:Util)-[:HAS_UTIL]->(c:Category) " +
                        "WHERE apoc.text.fuzzyMatch(u.keyword, \$searchTerm) OR apoc.text.fuzzyMatch(u.description, \$searchTerm) " +
                        "RETURN u.name AS name, u.description AS description"

            driver.session().use { session ->
                session.readTransaction { tx ->
                    val result = tx.run(searchQuery, mapOf("searchTerm" to searchTerm))
                    result.list { record ->
                        val name = record["name"].asString()
                        val description = record["description"].asString()
                        SearchResult(name, description)
                    }
                }
            }
        }
    }

    // Composable function to display search results
    @Composable
    fun SearchResults(results: List<SearchResult>) {
        LazyColumn {
            items(results) { result ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Text(text = result.name, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = result.description)
                    }
                }
            }
        }
    }

    @Composable
    fun SearchScreen(driver: Driver) {
        var searchQuery by remember { mutableStateOf("") }
        var searchResults by remember { mutableStateOf(emptyList<SearchResult>()) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // TextField for search input
            TextField(
                value = searchQuery,
                onValueChange = { newQuery ->
                    searchQuery = newQuery
                    // Perform the search using a coroutine
                    GlobalScope.launch(Dispatchers.IO) {
                        searchResults = fuzzySearch(driver, newQuery)
                    }
                },
                label = { Text("Search by keyword") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Button(
                onClick = {
                    // Handle the search button click if needed
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Search")
            }

            // Display search results
            SearchResults(results = searchResults)
        }
    }

}