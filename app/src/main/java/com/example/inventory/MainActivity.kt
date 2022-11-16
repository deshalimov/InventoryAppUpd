/*
 * Copyright (C) 2021 The Android Open Source Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.inventory

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import com.example.inventory.data.Item
import com.google.gson.Gson
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var navController: NavController

    private val viewModel : InventoryViewModel by viewModels {
        InventoryViewModelFactory(
            (application as InventoryApplication).database.itemDao()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve NavController from the NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        // Set up the action bar for use with the NavController
        setupActionBarWithNavController(this, navController)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.file_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_setting -> {
                val action = ItemListFragmentDirections.actionItemListFragmentToSettingFragment()
                navController.navigate(action)
            }
            R.id.menu_load_from_file -> {
                openFile(Uri.parse("/"))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openFile(pickerInitialUri: Uri) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            // Optionally, specify a URI for the directory that should be opened in
            // the system file picker before your app creates the document.
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }
        requestUri.launch(intent)
    }

    @SuppressLint("Range")
    private fun dumpFileName(uri: Uri) : String{
        // The query, because it only applies to a single document, returns only
        // one row. There's no need to filter, sort, or select fields,
        // because we want all fields for one document.
        val cursor: Cursor? = contentResolver.query(
            uri, null, null, null, null, null)

        cursor?.use {
            // moveToFirst() returns false if the cursor has 0 rows. Very handy for
            // "if there's anything to look at, look at it" conditionals.
            if (it.moveToFirst()) {

                // Note it's called "Display Name". This is
                // provider-specific, and might not necessarily be the file name.
                val displayName: String =
                    it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))

                return displayName
            }
        }
        return ""
    }

    private var requestUri = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result != null && result.resultCode == Activity.RESULT_OK) {
            result.data?.let { intent ->
                intent.data?.let { fileUri ->
                    val mainKey = MasterKey.Builder(applicationContext)
                        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                        .build()

                    val cacheFileToWrite  = File(applicationContext.cacheDir, dumpFileName(fileUri))

                    applicationContext.contentResolver.openInputStream(fileUri)!!.copyTo(
                        applicationContext.contentResolver.openOutputStream(cacheFileToWrite.toUri())!!
                    )
                    val encryptedFile = EncryptedFile.Builder(
                        applicationContext,
                        cacheFileToWrite,
                        mainKey,
                        EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
                    ).build()

                    try {
                        val inputStream = encryptedFile.openFileInput()
                        val byteArrayOutputStream = ByteArrayOutputStream()
                        var nextByte: Int = inputStream.read()
                        while (nextByte != -1) {
                            byteArrayOutputStream.write(nextByte)
                            nextByte = inputStream.read()
                        }

                        val plaintext = byteArrayOutputStream.toByteArray()

                        val gson = Gson()
                        val item = gson.fromJson(plaintext.toString(Charsets.UTF_8), Item::class.java)


                        viewModel.addNewItem(item)

                    }
                    catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    /**
     * Handle navigation when the user chooses Up from the action bar.
     */
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
