/*
 * Copyright 2019 The AoE Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.didi.aoe.library.common.util

import android.content.Context
import android.os.Environment
import com.didi.aoe.library.logging.LoggerFactory
import java.io.*

/**
 * @author noctis
 * @since 1.0.3
 */
object FileUtils {
    private val mLogger =
            LoggerFactory.getLogger("FileUtils")

    /**
     * 获取文件根目录，有外置存储时优先用外置空间
     *
     * @param context 应用上下文
     * @return
     */
    @JvmStatic
    fun getFilesDir(context: Context): String {
        if (isExternalMediaAvailable) {
            val filesDir = context.getExternalFilesDir(null)
            return filesDir!!.absolutePath
        }
        return context.filesDir.absolutePath
    }

    /**
     * 有可用外置存储
     *
     * @return
     */
    @JvmStatic
    val isExternalMediaAvailable: Boolean
        get() = Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() || !Environment.isExternalStorageRemovable()

    @JvmStatic
    fun isExist(filePath: String?): Boolean {
        val file = File(filePath)
        return file.exists()
    }

    @JvmStatic
    fun readString(filePath: String): String? {
        val result = read(filePath)
        return if (result == null) {
            null
        } else {
            String(result)
        }
    }

    @JvmStatic
    fun readString(input: InputStream): String? {
        val result = read(input)
        return if (result == null) {
            null
        } else {
            String(result)
        }
    }

    /**
     * 读取文件路径字节流数组
     *
     * @param filePath 文件全路径
     * @return
     */
    @JvmStatic
    fun read(filePath: String): ByteArray? {
        try {
            FileInputStream(filePath).use { fis -> return read(fis) }
        } catch (e: Exception) {
            mLogger.error("read file exception: ", e)
        }
        return null
    }

    /**
     * 读取字节流数组
     *
     * @param inputStream 输入流
     * @return
     */
    @JvmStatic
    fun read(inputStream: InputStream): ByteArray? {
        try {
            BufferedInputStream(inputStream).use { bis ->
                ByteArrayOutputStream().use { baos ->
                    var len: Int
                    val buf = ByteArray(1024)
                    while (bis.read(buf).also { len = it } != -1) {
                        baos.write(buf, 0, len)
                    }
                    return baos.toByteArray()
                }
            }
        } catch (e: Exception) {
            mLogger.error("read IO exception: ", e)
        }
        return null
    }

    // 将字符串写入到文本文件中 porting form https://github.com/didi/DoraemonKit/blob/master/Android/doraemonkit/src/main/java/com/didichuxing/doraemonkit/util/FileUtil.java
    @JvmStatic
    fun writeTxtToFile(strcontent: String, filePath: String, fileName: String) {
        //生成文件夹之后，再生成文件，不然会出错
        makeFilePath(filePath, fileName)
        val strFilePath = filePath + fileName
        // 每次写入时，都换行写
        val strContent = "$strcontent\r\n"
        try {
            val file = File(strFilePath)
            if (!file.exists()) {
                mLogger.debug("Create the file:$strFilePath")
                file.parentFile.mkdirs()
                file.createNewFile()
            }
            val raf = RandomAccessFile(file, "rwd")
            raf.seek(file.length())
            raf.write(strContent.toByteArray())
            raf.close()
        } catch (e: java.lang.Exception) {
            mLogger.debug("Error on write File:$e")
        }
    }

    // 生成文件
    private fun makeFilePath(filePath: String, fileName: String): File? {
        var file: File? = null
        makeRootDirectory(filePath)
        try {
            file = File(filePath + fileName)
            if (!file.exists()) {
                file.createNewFile()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return file
    }

    // 生成文件夹
    private fun makeRootDirectory(filePath: String) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                file.mkdir()
            }
        } catch (e: java.lang.Exception) {
            mLogger.info("error:", e.toString() + "")
        }
    }
}