package com.hiroshi.cimoc.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.util.Log
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.math.floor

class JMTTUtil {

     public fun decodeImage(img: InputStream): ByteArray {
        // 使用bitmap进行图片处理
        val input = BitmapFactory.decodeStream(img)
        // 漫画高度 and width
        val height = input.height
        val width = input.width
        // 水平分割10个小图
        val rows = 10
        // 未除尽像素
        val remainder = (height % rows)
        // 创建新的图片对象
        val resultBitmap = Bitmap.createBitmap(input.width, input.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        // 分割图片
        for (x in 0 until rows) {
            // 分割算法(详情见html源码页的方法"function scramble_image(img)")
            var copyH = floor(height / rows.toDouble()).toInt()
            var py = copyH * (x)
            val y = height - (copyH * (x + 1)) - remainder
            if (x == 0) {
                copyH += remainder
            } else {
                py += remainder
            }
            // 要裁剪的区域
            val crop = Rect(0, y, width, y + copyH)
            // 裁剪后应放置到新图片对象的区域
            val splic = Rect(0, py, width, py + copyH)

            canvas.drawBitmap(input, crop, splic, null)
        }
        // 创建输出流
        val output = ByteArrayOutputStream()
        resultBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
        return output.toByteArray()
    }
}