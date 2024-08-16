package com.dgioto.downloadfairytales

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dgioto.downloadfairytales.ui.theme.DownloadFairyTalesTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage

import java.io.ByteArrayOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // We get access to the fire base object
            val fs = Firebase.firestore
            //Create a Firebase file storage object and create an Image folder inside the storage
            val storage = Firebase.storage.reference.child("images")

            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia()
            ) { uri ->
                if (uri == null) return@rememberLauncherForActivityResult

                val task = storage.child("test_image.jpg").putBytes(bitmapToByteArray(this, uri))
                task.addOnSuccessListener { upLoadTask ->
                    upLoadTask.metadata?.reference?.downloadUrl?.addOnCompleteListener { uriTask ->
                        saveFairyTale(fs, uriTask.result.toString())
                    }
                }
            }

            DownloadFairyTalesTheme {
                MainScreen(fs){
                    launcher.launch(PickVisualMediaRequest(
                        mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                    ))
                }
            }
        }
    }
}

@Composable
fun MainScreen(fs: FirebaseFirestore, onClick: () -> Unit) {
    val list = remember {
        mutableStateOf(emptyList<FairyTale>())
    }

    // Write FairyTail objects to the list and we constantly monitor updates
    fs.collection("FairyTales").addSnapshotListener { snapShot, exception ->
        list.value = snapShot?.toObjects(FairyTale::class.java) ?: emptyList()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) {
            items(list.value) { fairyTale ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = fairyTale.imageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(90.dp)
                                .padding(start = 10.dp)
                        )

                        Text(
                            text = fairyTale.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentWidth()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
            onClick = { onClick() }) {
            // Button name
            Text(text = "Add Fairy Tale")
        }
    }
}

private fun bitmapToByteArray(context: Context, uri: Uri): ByteArray {
    val inputStream = context.contentResolver.openInputStream(uri)
    val bitmap = BitmapFactory.decodeStream(inputStream)
    val baos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
    return baos.toByteArray()
}

private fun saveFairyTale(fs: FirebaseFirestore, url: String) {
    // Adding a FairyTale object with attributes to the Fairy Tales collection
    fs.collection("FairyTales")
        .document().set(
            FairyTale(
                "My Book",
                "111111111",
                "100",
                "fiction",
                url
            )
        )
}