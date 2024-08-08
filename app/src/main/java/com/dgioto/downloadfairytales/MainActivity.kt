package com.dgioto.downloadfairytales

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dgioto.downloadfairytales.ui.theme.DownloadFairyTalesTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DownloadFairyTalesTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    // We get access to the fire base object
    val fs = Firebase.firestore
    val list = remember {
        mutableStateOf(emptyList<FairyTale>())
    }

    // Write FairyTail objects to the list and we constantly monitor updates
    fs.collection("FairyTales").addSnapshotListener {snapShot, exception ->
        list.value = snapShot?.toObjects(FairyTale::class.java) ?: emptyList()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
        ) {
            items(list.value) { fairyTale ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    Text(
                        text = fairyTale.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth().padding(15.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
            onClick = {
                // Adding a FairyTale object with attributes to the Fairy Tales collection
                fs.collection("FairyTales")
                    .document().set(
                        FairyTale(
                            "My Book",
                            "111111111",
                            "100",
                            "fiction",
                            "url"
                        )
                    )
            }) {
            // Button name
            Text(text = "Add Fairy Tale")
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    DownloadFairyTalesTheme {
//        Greeting("Android")
//    }
//}