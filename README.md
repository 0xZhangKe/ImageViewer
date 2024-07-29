# ImageViewer
ImageViewer for Jetpack Compose.

# Using ImageViewer
First, add dependencies:
```
implementation("com.github.0xZhangKe:ImageViewer:1.0.3")
```
The dependency package repository is on JitPack, so you might also need to configure and add the JitPack repository. If you already have it, simply ignore it.
```
repositories {
    maven { setUrl("<https://jitpack.io>") }
}
```

The simplest way to use it is as follows:
```
ImageViewer {
    Image(
        painter = painterResource(R.drawable.vertical_demo_image),
        contentDescription = "Sample Image",
        contentScale = ContentScale.FillBounds,
    )
}
```
ImageViewer is the only `Composable` function provided by this project. It's an image container that will support all the above gesture operations. It contains a function `content` of type Composable as an argument. 

Considering that image frameworks differ by project, it is not dependent on any image framework. All you need to do is write the image code in the content function that's part of the argument.
For example, the code uses Image, but it can be substituted with any other framework, like Coil's AsyncImage, etc.

In addition, you don't usually need to set a size for the above Image. 
The internal rules will adjust it dynamically. After setting the size, some strange changes might occur. 

If you want to control the size, you can set ImageViewer. 

It's best to also set `contentScale` to `FillBounds`.

ImageViewer accepts ImageViewerState to control some behavior.
```
val imageViewerState = rememberImageViewerState(
    minimumScale = 1.0F,
    maximumScale = 3F,
    onDragDismissRequest = {
        finish()
    },
)
```
The `onDragDismissRequest` is an automatic exit mechanism when the image slides down. By default it's null, which means this function doesn't need to be enabled. 

Sliding the image down will not exit it. But, if you assign a value to `onDragDismissRequest`, the image will exceed the threshold and call this function when slid down.

Another thing to note is that the content of ImageViewer can only contain one Composable. Having more than one will give a crash.

The above is a usage introduction of ImageViewer. It is very easy to use.
