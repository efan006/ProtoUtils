/*
 * MIT License
 *
 * Copyright (c) 2016. efan
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.efan.protoutils.utils;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * IO工具类
 * Created by wangkw on 2016/9/21.
 */
public class IOUtils {

    public static byte[] toBytes(String string){
        byte[] mBytes = new byte[0];
        try {
            mBytes = string.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return mBytes;
    }

    public static String toString(byte[] bytes){
        String string = "";
        try {
            string = new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return string;
    }


    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable ignored) {
            }
        }
    }

    public static byte[] read(InputStream is) throws IOException {
        int initSize = 1024 * 4;            //初始4K读取空间
        byte[] bytes = new byte[initSize];
        int offset = 0;
        for (;;) {
            //读取数据到剩余空间
            int readCount = is.read(bytes, offset, bytes.length - offset);

            if (readCount == -1) {         //读取完毕
                if (offset < initSize){     //空间有剩余，需要裁剪
                    byte[] newBytes = new byte[offset];
                    System.arraycopy(bytes, 0, newBytes, 0, newBytes.length);
                    bytes = newBytes;
                }
                break;
            }
            offset += readCount;
            if (offset == bytes.length) {  //空间已用完，需要扩容
                byte[] newBytes = new byte[bytes.length * 3 / 2];
                System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
                bytes = newBytes;
            }
        }
        return bytes;
    }

    public static String readString(InputStream is) throws IOException {
        byte[] bytes = read(is);
        return new String(bytes, "UTF-8");
    }

    public static void write(String fileName, String content){
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(fileName);
            fos.write(toBytes(content));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(fos);
        }

    }




}
