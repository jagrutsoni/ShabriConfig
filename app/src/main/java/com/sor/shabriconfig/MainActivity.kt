package com.sor.shabriconfig

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.sor.shabriconfig.ui.Constants.CAPTIONS_JSON_FILE_NAME
import com.sor.shabriconfig.ui.Constants.EXCEL_AUTHOR_KEY
import com.sor.shabriconfig.ui.Constants.EXCEL_COPIED_KEY
import com.sor.shabriconfig.ui.Constants.EXCEL_HASHTAGS_KEY
import com.sor.shabriconfig.ui.Constants.EXCEL_HASHTAG_SYNONYMS_KEY
import com.sor.shabriconfig.ui.Constants.EXCEL_ID_KEY
import com.sor.shabriconfig.ui.Constants.EXCEL_LANGUAGE_KEY
import com.sor.shabriconfig.ui.Constants.EXCEL_NAME_KEY
import com.sor.shabriconfig.ui.Constants.EXCEL_TEXT_KEY
import com.sor.shabriconfig.ui.Constants.FIRESTORE_CAPTIONS_COLLECTION_NAME
import com.sor.shabriconfig.ui.Constants.FIRESTORE_COLLECTION_ERROR_MESSAGE
import com.sor.shabriconfig.ui.Constants.FIRESTORE_HASHTAGS_COLLECTION_NAME
import com.sor.shabriconfig.ui.Constants.HASHTAGS_JSON_FILE_NAME
import com.sor.shabriconfig.ui.Constants.TAG
import com.sor.shabriconfig.ui.theme.ShabriConfigTheme
import org.json.JSONArray

class MainActivity : ComponentActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShabriConfigTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Data read from json and write to firestore")
                }
            }
        }

        db = Firebase.firestore
        writeCaptionsJsonToFireStore()
        writeHashtagsJsonToFirestore()
    }

    private fun writeCaptionsJsonToFireStore() {
        val storage = Firebase.storage
        val storageRef = storage.reference

        var islandRef = storageRef.child(CAPTIONS_JSON_FILE_NAME)

        val ONE_MEGABYTE: Long = 1024 * 1024
        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {
            val s = String(it)
            val jsonArray = JSONArray(s)

            for (i in 0 until jsonArray.length()) {
                Log.d("test1", jsonArray.getJSONObject(i).toString())
                val jsonObject = jsonArray.getJSONObject(i)
                val array: List<String> = jsonObject.getString(EXCEL_HASHTAGS_KEY).split(',')
                val newArray: MutableList<String> = mutableListOf()
                /* val prefix = "$EXCEL_HASHTAG_KEY/"
             for (obj in array) {
                 obj.trimStart()
                 .trimEnd()
                     .lowercase()
                 newArray.add(prefix + obj)
             }*/
                for (obj in array) {
                    newArray.add(
                        obj.trimStart()
                            .trimEnd()
                            .lowercase()
                    )
                }
                val id = jsonObject.getInt(EXCEL_ID_KEY)
                val captionHash = hashMapOf(
                    EXCEL_ID_KEY to id,
                    EXCEL_TEXT_KEY to jsonObject.getString(EXCEL_TEXT_KEY).trimStart().trimEnd(),
                    EXCEL_AUTHOR_KEY to jsonObject.getString(EXCEL_AUTHOR_KEY).trimStart().trimEnd(),
                    EXCEL_LANGUAGE_KEY to jsonObject.getString(EXCEL_LANGUAGE_KEY).trimStart().trimEnd(),
                    EXCEL_COPIED_KEY to jsonObject.getInt(EXCEL_COPIED_KEY),
                    EXCEL_HASHTAGS_KEY to newArray
                    // EXCEL_HASHTAG_DOC_KEY to newArray
                )

                db.collection(FIRESTORE_CAPTIONS_COLLECTION_NAME).document(id.toString())
                    .set(captionHash)
            }
        }.addOnFailureListener {
            Log.d(TAG, FIRESTORE_COLLECTION_ERROR_MESSAGE)

        }
    }

    private fun writeHashtagsJsonToFirestore() {
        val storage = Firebase.storage
        val storageRef = storage.reference

        var islandRef = storageRef.child(HASHTAGS_JSON_FILE_NAME)

        val ONE_MEGABYTE: Long = 1024 * 1024
        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {
            val s = String(it)
            val jsonArray = JSONArray(s)

            for (i in 0 until jsonArray.length()) {
                if (jsonArray.isNull(i)) {
                    Log.d("test1", "" + jsonArray.isNull(i))
                    continue
                }
                val jsonObject = jsonArray.getJSONObject(i)
                Log.d("test1", jsonObject.toString())
                val synonyms: List<String> =
                    jsonObject.getString(EXCEL_HASHTAG_SYNONYMS_KEY).split(',')
                synonyms.forEach { synonym ->

                    synonym.trimEnd()
                        .trimStart()
                        .lowercase()
                }

                val hashtagMap = hashMapOf(
                    EXCEL_ID_KEY to jsonObject.getInt(EXCEL_ID_KEY),
                    EXCEL_NAME_KEY to jsonObject.getString(EXCEL_NAME_KEY).trimStart().trimEnd()
                        .lowercase(),
                    EXCEL_HASHTAG_SYNONYMS_KEY to synonyms
                )
                db.collection(FIRESTORE_HASHTAGS_COLLECTION_NAME)
                    .document("${jsonObject.getInt(EXCEL_ID_KEY)}").set(hashtagMap)
            }
        }.addOnFailureListener {
            Log.d(TAG, FIRESTORE_COLLECTION_ERROR_MESSAGE)

        }
    }


}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ShabriConfigTheme {
        Greeting("Data read from json and write to firestore")
    }
}
