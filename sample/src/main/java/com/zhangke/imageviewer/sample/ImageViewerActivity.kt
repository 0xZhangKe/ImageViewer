package com.zhangke.imageviewer.sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.zhangke.imageviewer.ImageViewer
import com.zhangke.imageviewer.rememberImageViewerState
import com.zhangke.imageviewer.sample.ui.theme.ImageViewerTheme

class ImageViewerActivity : ComponentActivity() {

    companion object {

        private const val PARAMS_IMAGE_ID = "params_image_id"

        fun open(activity: Activity, imageId: Int) {
            val intent = Intent(activity, ImageViewerActivity::class.java)
            intent.putExtra(PARAMS_IMAGE_ID, imageId)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imageId = intent.getIntExtra(PARAMS_IMAGE_ID, 0)
        enableEdgeToEdge()
        setContent {
            ImageViewerTheme {
                ImageViewerContent(imageId)
            }
        }
    }

    @Composable
    private fun ImageViewerContent(imageId: Int) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val state = rememberImageViewerState(
                    aspectRatio = 1F,
                    onDragDismissRequest = {
                        finish()
                    },
                )
                ImageViewer(
                    state = state,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Image(
                        painter = painterResource(id = imageId),
                        contentDescription = "Sample Image",
                    )
                }
            }
        }
    }
}
