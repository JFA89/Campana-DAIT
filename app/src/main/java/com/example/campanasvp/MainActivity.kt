package com.example.campanasvp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.webkit.WebViewAssetLoader
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var cameraImageUri: Uri? = null

    companion object {
        const val PERMISSIONS_REQUEST_CODE = 100
        const val FILE_CHOOSER_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.webView)
        pedirPermisos()
    }

    private fun pedirPermisos() {
        val permisosFaltantes = mutableListOf<String>()
        val permisos = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_MEDIA_IMAGES
        )
        permisos.forEach { permiso ->
            if (ContextCompat.checkSelfPermission(this, permiso)
                != PackageManager.PERMISSION_GRANTED) {
                permisosFaltantes.add(permiso)
            }
        }
        if (permisosFaltantes.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permisosFaltantes.toTypedArray(),
                PERMISSIONS_REQUEST_CODE
            )
        } else {
            iniciarWebView()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            iniciarWebView()
        }
    }

    private fun iniciarWebView() {

        // ── WebViewAssetLoader: sirve assets/ como https://appassets.androidplatform.net/assets/
        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()

        webView.addJavascriptInterface(AndroidBridge(this), "Android")
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.setGeolocationEnabled(true)

        webView.webViewClient = object : WebViewClient() {

            // Interceptar requests y servir desde assets locales
            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? {
                return assetLoader.shouldInterceptRequest(request.url)
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                if (request.isForMainFrame) {
                    webView.loadData(
                        """
                        <html>
                        <body style="display:flex; justify-content:center; 
                                     align-items:center; height:100vh; margin:0;
                                     font-family:sans-serif; text-align:center;">
                            <div>
                                <h2>Sin conexión ⛔</h2>
                                <p>Revise su estado de red</p>
                                <br>
                                <button onclick="window.location.reload()"
                                        style="background-color:blue; color:white; border:none;
                                               padding:14px 28px; font-size:16px; border-radius:8px;
                                               cursor:pointer;">
                                    🔄 Reintentar
                                </button>
                            </div>
                        </body>
                        </html>
                        """.trimIndent(),
                        "text/html",
                        "utf-8"
                    )
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {

            // Manejo de fotos
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                this@MainActivity.filePathCallback?.onReceiveValue(null)
                this@MainActivity.filePathCallback = filePathCallback

                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                val photoFile = crearArchivoImagen()
                cameraImageUri = FileProvider.getUriForFile(
                    this@MainActivity,
                    "${packageName}.provider",
                    photoFile
                )
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)

                val galleryIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "image/*"
                }

                val chooser = Intent.createChooser(galleryIntent, "Seleccionar imagen").apply {
                    putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))
                }

                startActivityForResult(chooser, FILE_CHOOSER_REQUEST_CODE)
                return true
            }

            // Manejo de GPS
            override fun onGeolocationPermissionsShowPrompt(
                origin: String,
                callback: GeolocationPermissions.Callback
            ) {
                callback.invoke(origin, true, false)
            }
        }

        // ── URL virtual en lugar de file:///android_asset/
        val url = "https://appassets.androidplatform.net/assets/Inspeccion.html"
        webView.loadUrl(url)

        val fabRecargar = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabRecargar)
        fabRecargar.setOnClickListener {
            webView.loadUrl(url)
        }
    }

    private fun crearArchivoImagen(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("IMG_${timeStamp}_", ".jpg", storageDir)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            val uriOriginal: Uri? = when {
                resultCode != Activity.RESULT_OK -> null
                data?.data != null -> data.data!!
                else -> cameraImageUri
            }

            val results: Array<Uri>? = uriOriginal?.let {
                val uriComprimida = comprimirImagen(it)
                arrayOf(uriComprimida)
            }

            filePathCallback?.onReceiveValue(results)
            filePathCallback = null
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    private fun comprimirImagen(uri: Uri): Uri {
        val inputStream = contentResolver.openInputStream(uri)
        val imagenOriginal = BitmapFactory.decodeStream(inputStream)

        val maxAncho = 800
        val ratio = maxAncho.toFloat() / imagenOriginal.width
        val nuevoAlto = (imagenOriginal.height * ratio).toInt()

        val imagenRedimensionada = Bitmap.createScaledBitmap(
            imagenOriginal, maxAncho, nuevoAlto, true
        )

        val archivoComprimido = File(
            getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "foto_comprimida_${System.currentTimeMillis()}.jpg"
        )

        val outputStream = FileOutputStream(archivoComprimido)
        imagenRedimensionada.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
        outputStream.flush()
        outputStream.close()

        imagenOriginal.recycle()
        imagenRedimensionada.recycle()

        return Uri.fromFile(archivoComprimido)
    }
}