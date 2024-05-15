package com.example.unwired_android.ui.utils

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.unwired_android.R

fun base64ToBitmap(b64: String): ImageBitmap? {
    return try {
        val imageBytes = Base64.decode(b64, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size).asImageBitmap()
    } catch (_: Exception) {
        println("Couldn't convert avatar")
        null
    }
}


@Composable
fun Base64Avatar(b64: String?, size: Int = 200, modifier: Modifier = Modifier): Unit {
    if (b64 == null) {
        return Image(
            painter = painterResource(id = R.drawable.avatar),
            contentDescription = "Avatar",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .then(modifier)
        )
    }
    val decodedImage = base64ToBitmap(b64)
    return if (decodedImage != null) {
        Image(
            bitmap = decodedImage,
            contentDescription = "Avatar",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
        )
    } else {
        Image(
            painter = painterResource(id = R.drawable.avatar),
            contentDescription = "Avatar",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
        )
    }
}