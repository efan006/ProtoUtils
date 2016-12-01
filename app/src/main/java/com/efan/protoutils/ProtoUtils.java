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
package com.efan.protoutils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.efan.protoutils.bean.ArrayProto;
import com.efan.protoutils.bean.TestProto;
import com.efan.protoutils.utils.IOUtils;
import com.efan.protoutils.utils.Java2Proto;

/**
 * Proto转换工具，可以把工程里的Java原生类转成支持proto协议的Java类
 * Created by wangkw on 2016/9/30.
 */

public class ProtoUtils {

    private static final String DIR_WORKSPACE = "." + File.separator + "proto" + File.separator;

    public static void main(String[] args) throws IOException {
        String outerName = "ProtoBean";             //输出的类名
        String packageName = "com.efan.proto";      //输出的包名
        genProtoFile(packageName, outerName, ArrayProto.class, TestProto.class);    //传入需要转化的java类，可以多个

        String outputPath = packageName.replace(".", File.separator);
        squareCompile(outputPath, outerName);       //Square方式，方法数和代码量是Google的1/5
//        googleCompile(outputPath, outerName);     //Google方式
    }

    /**
     * 生成Proto文件
     * @param outerName 输出文件名
     * @param classes 需要转化的java类
     */
    private static void genProtoFile(String packageName, String outerName, Class<?>... classes) {
        StringBuilder sb = new StringBuilder();
        sb.append(Java2Proto.head(packageName, outerName));
        for (Class<?> clazz : classes){
            sb.append(Java2Proto.convert(clazz));
        }
        IOUtils.write(DIR_WORKSPACE + outerName + ".proto", sb.toString());
    }

    /**
     * 采用Square工具转Java
     * @throws IOException
     */
    private static void squareCompile(String outputPath, String outerName) throws IOException {
        //square
        runCmd("cd ./proto",
                "java -jar -Dfile.encoding=UTF-8 wire-compiler-2.2.0-jar-with-dependencies.jar "
                        + "--proto_path=. --java_out=./ "+ outerName + ".proto",
                "pause",
                "start" + "." + File.separator + outputPath,
                "exit");
    }

    /**
     * 采用谷歌工具转java
     * @throws IOException
     */
    private static void googleCompile(String outputPath, String outerName) throws IOException {
        //google
        runCmd("cd ./proto",
                "protoc-java.exe --java_out=./ " + outerName + ".proto",
                "pause",
                "start" + "." + File.separator + outputPath,
                "exit");
    }

    /**
     * 多条命令生成bat并执行
     * @param commands 命令数组
     * @throws IOException
     */
    private static void runCmd(String... commands) throws IOException {
        File file = new File(DIR_WORKSPACE + "command.bat");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        for (String command : commands) {
            writer.write(command);
            writer.newLine();
        }
        writer.close();
        Runtime.getRuntime().exec("cmd /c start " + file.getAbsolutePath());
    }

}
