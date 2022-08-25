package org.techtown.kiosk

import android.app.Activity
import android.content.Context
import android.content.Context.CAMERA_SERVICE
import android.graphics.Bitmap
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.Image
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import android.util.Log
import android.util.SparseIntArray
import android.view.Surface
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import java.io.IOException
import java.nio.ByteBuffer

class VisionImage {

    private fun imageFromBitmap(bitmap: Bitmap) {
        val image = FirebaseVisionImage.fromBitmap(bitmap)
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private fun imageFromMediaImage(mediaImage: Image, rotation: Int) {
        val image = FirebaseVisionImage.fromMediaImage(mediaImage, rotation)
    }

    private fun imageFromBuffer(buffer: ByteBuffer, rotation: Int) {
        // [START set_metadata]
        val metadata = FirebaseVisionImageMetadata.Builder()
            .setWidth(480) //
            .setHeight(360) //
            .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
            .setRotation(rotation)
            .build()
        // [END set_metadata]
        // [START image_from_buffer]
        val image = FirebaseVisionImage.fromByteBuffer(buffer, metadata)
        // [END image_from_buffer]
    }

    private fun imageFromArray(byteArray: ByteArray, rotation: Int) {
        val metadata = FirebaseVisionImageMetadata.Builder()
            .setWidth(480)
            .setHeight(360)
            .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
            .setRotation(rotation)
            .build()
        // [START image_from_array]
        val image = FirebaseVisionImage.fromByteArray(byteArray, metadata)
        // [END image_from_array]
    }

    private fun imageFromPath(context: Context, uri: Uri) {
        // [START image_from_path]
        val image: FirebaseVisionImage
        try {
            image = FirebaseVisionImage.fromFilePath(context, uri)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        // [END image_from_path]
    }

    // [START get_rotation]
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Throws(CameraAccessException::class)
    private fun getRotationCompensation(cameraId: String, activity: Activity, context: Context): Int {
        val deviceRotation = activity.windowManager.defaultDisplay.rotation
        var rotationCompensation = ORIENTATIONS.get(deviceRotation)

        val cameraManager = context.getSystemService(CAMERA_SERVICE) as CameraManager
        val sensorOrientation = cameraManager
            .getCameraCharacteristics(cameraId)
            .get(CameraCharacteristics.SENSOR_ORIENTATION)!!
        rotationCompensation = (rotationCompensation + sensorOrientation + 270) % 360

        val result: Int
        when (rotationCompensation) {
            0 -> result = FirebaseVisionImageMetadata.ROTATION_0
            90 -> result = FirebaseVisionImageMetadata.ROTATION_90
            180 -> result = FirebaseVisionImageMetadata.ROTATION_180
            270 -> result = FirebaseVisionImageMetadata.ROTATION_270
            else -> {
                result = FirebaseVisionImageMetadata.ROTATION_0
                Log.e(TAG, "Bad rotation value: $rotationCompensation")
            }
        }
        return result
    }
    // [END get_rotation]

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Throws(CameraAccessException::class)
    private fun getCompensation(activity: Activity, context: Context) {
        // Get the ID of the camera using CameraManager. Then:
        val rotation = getRotationCompensation(MY_CAMERA_ID, activity, context)
    }

    companion object {  //동반객체로 생성

        private val TAG = "MLKIT"
        private val MY_CAMERA_ID = "my_camera_id"

        // [START camera_orientations]
        private val ORIENTATIONS = SparseIntArray()

        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }
        // [END camera_orientations]
    }
}
}