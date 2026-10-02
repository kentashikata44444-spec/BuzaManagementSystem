package com.example.buzaimanagementsystem

import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.buzaimanagementsystem.utils.FileLogger
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import java.util.concurrent.Executors

private const val TAG = "OcrCameraView"

/**
 * カメラ映像を表示し、ML Kitでバーコード/QRコードを読み取るコンポーネント
 */
@Composable
fun OcrCameraView(
    onTextScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    /**
     * ログ出力用の共通ヘルパー
     */
    fun writeLog(level: String, message: String, throwable: Throwable? = null) {
        when (level) {
            "D" -> {
                Log.d(TAG, message, throwable)
                FileLogger.writeLog(context, "DEBUG", TAG, message + (throwable?.let { " : ${it.message}" } ?: ""))
            }
            "W" -> {
                Log.w(TAG, message, throwable)
                FileLogger.writeLog(context, "WARN", TAG, message + (throwable?.let { " : ${it.message}" } ?: ""))
            }
            "E" -> {
                Log.e(TAG, message, throwable)
                FileLogger.writeLog(context, "ERROR", TAG, message + (throwable?.let { " : ${it.message}" } ?: ""))
            }
            "I" -> {
                Log.i(TAG, message, throwable)
                FileLogger.writeLog(context, "INFO", TAG, message + (throwable?.let { " : ${it.message}" } ?: ""))
            }
        }
    }

    // QRコードや各種バーコードを全般的に読み取る設定
    val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_QR_CODE,
            Barcode.FORMAT_CODE_128,
            Barcode.FORMAT_CODE_39,
            Barcode.FORMAT_EAN_13
        )
        .build()
    val barcodeScanner = remember { BarcodeScanning.getClient(options) }

    var isProcessing by remember { mutableStateOf(false) }

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            writeLog("D", "onDispose: カメラエグゼキュータをシャットダウンします")
            cameraExecutor.shutdown()
        }
    }

    LaunchedEffect(lifecycleOwner) {
        writeLog("D", "LaunchedEffect: カメラプロバイダーの初期化を開始します")
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    if (isProcessing) {
                        imageProxy.close()
                        return@setAnalyzer
                    }
                    isProcessing = true

                    processBarcodeProxy(
                        imageProxy = imageProxy,
                        barcodeScanner = barcodeScanner,
                        onTextScanned = { scannedText ->
                            writeLog("I", "バーコード読み取り成功: scannedText=$scannedText")
                            onTextScanned(scannedText)
                        },
                        onFinished = {
                            isProcessing = false
                        },
                        writeLog = { level, msg, err -> writeLog(level, msg, err) }
                    )
                }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )
                writeLog("I", "カメラのライフサイクルバインドが完了しました")
            } catch (e: Exception) {
                writeLog("E", "カメラプロバイダーの初期化中にエラーが発生しました", e)
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { previewView }
    )
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun processBarcodeProxy(
    imageProxy: ImageProxy,
    barcodeScanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    onTextScanned: (String) -> Unit,
    onFinished: () -> Unit,
    writeLog: (String, String, Throwable?) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes: List<Barcode> ->
                for (barcode in barcodes) {
                    val rawValue = barcode.rawValue
                    if (!rawValue.isNullOrBlank()) {
                        onTextScanned(rawValue)
                        break
                    }
                }
            }
            .addOnFailureListener { e ->
                writeLog("E", "BarcodeScanner processing failed", e)
            }
            .addOnCompleteListener {
                imageProxy.close()
                onFinished()
            }
    } else {
        writeLog("W", "mediaImage が null のため画像処理をスキップしました", null)
        imageProxy.close()
        onFinished()
    }
}