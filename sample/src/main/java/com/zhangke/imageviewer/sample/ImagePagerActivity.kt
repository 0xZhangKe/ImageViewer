package com.zhangke.imageviewer.sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.zhangke.imageviewer.ImageViewer
import com.zhangke.imageviewer.sample.ui.theme.ImageViewerTheme

class ImagePagerActivity : ComponentActivity() {

    companion object {

        fun open(activity: Activity) {
            val intent = Intent(activity, ImagePagerActivity::class.java)
            activity.startActivity(intent)
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            ImageViewerTheme {
                val pagerState = rememberPagerState {
                    4
                }
                HorizontalPager(state = pagerState) { pageIndex ->
                    ImageViewer {
                        val imageId = if (pageIndex % 2 == 0) {
                            R.drawable.vertical_demo_image
                        } else {
                            R.drawable.horizontal_demo_image
                        }
                        Image(
                            painter = painterResource(imageId),
                            contentDescription = "Sample Image",
                            contentScale = ContentScale.FillBounds,
                        )
                    }
                }
            }
        }
    }
}
