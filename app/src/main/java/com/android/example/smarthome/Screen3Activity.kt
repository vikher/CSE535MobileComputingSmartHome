package com.android.example.smarthome

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.example.smarthome.databinding.ActivityScreen3Binding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
// Define the API interface
interface ApiService {
    @Multipart
    @POST("upload")
    fun uploadVideo(@Part file: MultipartBody.Part): Call<UploadResponse>
}
data class UploadResponse(val message: String)

class Screen3Activity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityScreen3Binding
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var uri: Uri
    private var selectedGesture: String? = null
    private val userLastName = "HERNANDEZ-SANCHEZ"
    private val BASE_URL = "http://192.168.1.184:5000/"
    private val REQUEST_CODE_PERMISSIONS = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityScreen3Binding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        selectedGesture = intent.getStringExtra("selectedGesture")

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        viewBinding.imageCaptureButton.setOnClickListener { uploadVideos() }
        viewBinding.videoCaptureButton.setOnClickListener { captureVideo() }
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun captureVideo() {
        val videoCapture = this.videoCapture ?: return

        val curRecording = recording
        if (curRecording != null) {
            // Stop the current recording session.
            curRecording.stop()
            recording = null
            return
        }
        val fileName = String.format("%s_PRACTICE_%d_%s.mp4", selectedGesture, CounterSingleton.counter, userLastName).uppercase()

        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
        }

        val mediaStoreOutput = MediaStoreOutputOptions.Builder(this.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        recording = videoCapture.output
            .prepareRecording(this, mediaStoreOutputOptions)
            .start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                when(recordEvent) {
                    is VideoRecordEvent.Start -> {
                        viewBinding.videoCaptureButton.apply {
                            text = getString(R.string.stop_capture)
                        }
                        // Stop recording after 5 seconds
                        Handler(Looper.getMainLooper()).postDelayed({
                            recording?.stop()
                            recording = null
                        }, 5000)
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            val outputUriPath  = "${recordEvent.outputResults.outputUri.path}"
                            uri = recordEvent.outputResults.outputUri
                            val successMsg = "Video capture completed successfully"
                            Toast.makeText(baseContext, successMsg, Toast.LENGTH_SHORT).show()
                            viewBinding.videoCaptureButton.apply {
                                text = getString(R.string.start_capture)
                            }
                        } else {
                            recording?.close()
                            recording = null
                            val errorMsg = "Video capture ends with error: ${recordEvent.error}"
                            Toast.makeText(baseContext, errorMsg, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
    }

    private fun getRealPathFromURI(context: Context, contentUri: Uri): String? {
        context.contentResolver.query(contentUri, arrayOf(MediaStore.Video.Media.DATA), null, null, null)?.use { cursor ->
            cursor.moveToFirst()
            val column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            return cursor.getString(column_index)
        }
        return null
    }

    private fun uploadVideos() {
        val realPath = getRealPathFromURI(this, uri)
        val videoFile = File(realPath)

        videoFile.takeIf { it.exists() }?.let {
            val requestBody = RequestBody.create("video/mp4".toMediaTypeOrNull(), it)
            val filePart: MultipartBody.Part = MultipartBody.Part.createFormData("file", it.name, requestBody)

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val apiService = retrofit.create(ApiService::class.java)
            apiService.uploadVideo(filePart).enqueue(getUploadCallback())
        } ?: run {
            showToast("File not found")
        }
    }

    private fun getUploadCallback(): Callback<UploadResponse> {
        return object : Callback<UploadResponse> {
            override fun onResponse(call: Call<UploadResponse>, response: Response<UploadResponse>) {
                handleResponse(response)
            }

            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                showToast("Upload failed: ${t.message}")
            }
        }
    }

    private fun handleResponse(response: Response<UploadResponse>) {
        try {
            if (response.isSuccessful) {
                response.body()?.let {
                    showToast(it.message)
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish() // Close the current activity
                } ?: showToast("Unknown response")
            } else {
                showToast("Failed")
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }

    private fun handleException(e: Exception) {
        showToast("Error: ${e.message}")
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }
    
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
        )
    }
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }
            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HD))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            imageCapture = ImageCapture.Builder().build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding Log", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && it.value == false)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(baseContext,
                    "Permission request denied",
                    Toast.LENGTH_SHORT).show()
            } else {
                startCamera()
            }
        }

    companion object {
        private const val TAG = "CameraXApp"
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO

            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}