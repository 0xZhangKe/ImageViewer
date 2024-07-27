package com.zhangke.imageviewer.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zhangke.imageviewer.sample.ui.theme.ImageViewerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ImageViewerTheme {
                MainContent()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainContent() {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    onClick = {
                        ImageViewerActivity.open(
                            activity = this@MainActivity,
                            imageId = R.drawable.vertical_demo_image,
                        )
                    },
                ) {
                    Text(text = "Vertical Image")
                }
                Button(
                    modifier = Modifier.padding(top = 16.dp),
                    onClick = {
                        ImageViewerActivity.open(
                            activity = this@MainActivity,
                            imageId = R.drawable.horizontal_demo_image,
                        )
                    },
                ) {
                    Text(text = "Horizontal Image")
                }
                Button(
                    modifier = Modifier.padding(top = 16.dp),
                    onClick = {
                        ImageViewerActivity.open(
                            activity = this@MainActivity,
                            imageId = R.drawable.small_luancher,
                        )
                    },
                ) {
                    Text(text = "Small Image")
                }
                Button(
                    modifier = Modifier.padding(top = 16.dp),
                    onClick = {
                        ImagePagerActivity.open(this@MainActivity)
                    },
                ) {
                    Text(text = "Image Pager")
                }
            }
        }
    }
}
