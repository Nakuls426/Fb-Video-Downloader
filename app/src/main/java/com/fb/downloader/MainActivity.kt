package com.fb.downloader

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.LENGTH_SHORT
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.fb.downloader.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.lang.RuntimeException
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var writeStorage: Int = 127
    private var tag = "MainActivity"
    private var videoId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        checkingPermission()
        progress_Bar.visibility = View.GONE

        fun viewsVisibility() {
            binding.sdFormat.visibility = View.VISIBLE
            binding.hdFormat.visibility = View.VISIBLE
            binding.availableFormat.visibility = View.VISIBLE
        }

        binding.searchingBtn.setOnClickListener {
            progress_Bar.visibility = View.VISIBLE
            if (binding.searchBar1.text.isNotEmpty() && !binding.searchBar1.text.startsWith("https://www.facebook.com/")) {
                Toast.makeText(this@MainActivity, "Invalid url", LENGTH_LONG)
                    .show()
            }
            if (binding.searchBar1.text.startsWith("https://www.facebook.com/")
                && ((ContextCompat.checkSelfPermission(
                    this@MainActivity, android.Manifest.permission
                        .WRITE_EXTERNAL_STORAGE
                )) == PackageManager.PERMISSION_DENIED)
            ) {
                Toast.makeText(this@MainActivity, "Grant permission to download", LENGTH_LONG)
                    .show()
                checkingPermission()
            }
            if (binding.searchBar1.text.startsWith("https://www.facebook.com/")
                && ((ContextCompat.checkSelfPermission(
                    this@MainActivity, android.Manifest.permission
                        .WRITE_EXTERNAL_STORAGE
                )) == PackageManager.PERMISSION_GRANTED)
            ) {
                @SuppressLint("StaticFieldLeak")
                object : BackgroundActivity(
                    this@MainActivity,
                    binding.searchBar1.text.toString(),
                    false
                ) {
                    override fun onExtractionComplete(getAndSet: GetAndSet) {
                        Log.d(tag, "SD url : ${getAndSet.sdUrl}")
                        Log.d(tag, "HD url : ${getAndSet.hdUrl}")
                        if (getAndSet.hdUrl.isBlank()) {
                            if (hd_format.isEnabled)
                                hd_format.isEnabled = false
                        } else {
                            viewsVisibility()
                        }
                    progress_Bar.visibility = View.GONE
                    }
                    override fun onExtractionFail(exception: Exception) {
                        exception.printStackTrace()
                    }
                }
            } else if (binding.searchBar1.text.isBlank()) {
                binding.searchBar1.error = "Please paste url"
            }
        }
        binding.sdFormat.setOnClickListener {
            @SuppressLint("StaticFieldLeak")
            object :
                BackgroundActivity(this@MainActivity, binding.searchBar1.text.toString(), false) {
                @SuppressLint("StaticFieldLeak")
                override fun onExtractionComplete(getAndSet: GetAndSet) {
                    val video: String = System.currentTimeMillis().toString()
                    val videoTitle = "$video.mp4"
                    val createD = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "")
                    Log.d(tag, "SD src ${getAndSet.sdUrl}")
                    val file = File(createD, videoTitle)
                    if (binding.searchBar1.text.isNotEmpty()) {
                        try {
                            val request: DownloadManager.Request =
                                (DownloadManager.Request(Uri.parse(getAndSet.sdUrl))).also {
                                    it.allowScanningByMediaScanner()
                                    it.setDescription(videoTitle)
                                        .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE)
                                        .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE)
                                        .setDestinationUri(Uri.fromFile(file))
                                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                        .setNotificationVisibility(View.VISIBLE)
                                        .setTitle(videoTitle)
                                }
                            val downloadManager: DownloadManager =
                                this@MainActivity.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                            videoId = downloadManager.enqueue(request)
                            Toast.makeText(
                                this@MainActivity,
                                "Download started",
                                LENGTH_LONG
                            ).show()
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@MainActivity,
                                "Video can't be downloaded",
                                LENGTH_LONG
                            ).show()
                        }
                    }
                }
                override fun onExtractionFail(exception: Exception) {
                    exception.printStackTrace()
                }
            }
        }
        binding.hdFormat.setOnClickListener {
            @SuppressLint("StaticFieldLeak")
            object :
                BackgroundActivity(this@MainActivity, binding.searchBar1.text.toString(), false) {
                override fun onExtractionComplete(@SuppressLint("StaticFieldLeak") getAndSet: GetAndSet) {
                    Log.d(tag, "Facebook hd url ${getAndSet.hdUrl}")
                    val name: String = System.currentTimeMillis().toString()
                    val videoTitle = "$name.mp4"
                    val createD = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "")
                    val file = File(createD, videoTitle)
                    if (binding.searchBar1.text.isNotEmpty()) {
                        try {
                            val request: DownloadManager.Request =
                                (DownloadManager.Request(Uri.parse(getAndSet.hdUrl))).also {
                                    it.allowScanningByMediaScanner()
                                    it.setDescription(videoTitle)
                                        .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE)
                                        .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE)
                                        .setDestinationUri(Uri.fromFile(file))
                                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                        .setNotificationVisibility(View.VISIBLE)
                                        .setTitle(videoTitle)
                                }
                            val downloadManager: DownloadManager =
                                this@MainActivity.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                            videoId = downloadManager.enqueue(request)
                            Toast.makeText(this@MainActivity, "Download started", LENGTH_LONG)
                                .show()
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@MainActivity,
                                "Video can't be downloaded",
                                LENGTH_LONG
                            ).show()
                        }
                    }
                }

                override fun onExtractionFail(exception: Exception) {
                    Log.e(tag, "$exception")
                }
            }
        }
    }

    private fun checkingPermission() {
        if ((ContextCompat.checkSelfPermission(
                this@MainActivity, android.Manifest.permission
                    .WRITE_EXTERNAL_STORAGE
            )) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 127
            )
        }
        if ((ContextCompat.checkSelfPermission(
                this@MainActivity,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(tag, "Permission already granted.")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == writeStorage) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(tag, "Permission Granted")
            } else {
                for (permission: String in permissions) {
                    var showRationale = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        showRationale = shouldShowRequestPermissionRationale(permission)
                    if (!showRationale)
                        openSettings()
                }
            }
        }
    }

    private fun openSettings() {
        val intent = object : Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS) {}
        val uri = Uri.fromParts("package", this@MainActivity.packageName, null)
        intent.data = uri
        this@MainActivity.startActivity(intent)
    }
}