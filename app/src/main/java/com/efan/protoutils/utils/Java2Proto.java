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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import com.efan.protoutils.annotation.Proto;

import android.support.v4.util.SparseArrayCompat;

/**
 * java类生成proto文件
 * 目前只支持基本的proto2
 * 不支持二维数组，不支持Map, 不支持char和char数组
 * 不支持import x.proto
 * 不支持default（与proto3一致）
 * 枚举值一定从0开始（与proto3一致）
 *
 * Created by efan on 2016/9/28.
 */

public class Java2Proto {

    /**
     * 增加缩进
     * @param step 第几层嵌套
     */
    private static void tab(StringBuilder sb, int step){
        for (int i = 0; i < step; i++){
            sb.append("\t");
        }
    }

    /**
     * 枚举类的转化
     * @param beanClass 枚举类
     * @param step 第几层嵌套
     */
    private static String enum2Proto(Class<?> beanClass, int step){
        StringBuilder sb = new StringBuilder();
        tab(sb, step);
        //写 enum 结构
        sb.append("enum ").append(beanClass.getSimpleName()).append(" {\n");

        Field[] fields = beanClass.getDeclaredFields();
        int tagNum = 0;
        //遍历枚举，值从0开始
        for (Field field : fields){
            if (field.isEnumConstant()){    //排除value数组
                tab(sb, step + 1);
                //MON = 0;\n
                sb.append(field.getName()).append(" = ").append(tagNum).append(";\n");
                tagNum ++;
            }
        }
        tab(sb, step);
        sb.append("}\n\n");
        return sb.toString();
    }

    public static String convert(Class<?> beanClass){
        return cls2Proto(beanClass, 0);
    }

    public static String head(String packageName, String outerClassName){
        return "option java_package = \"" + packageName + "\";\n" +
                "option java_outer_classname = \"" + outerClassName + "\";\n\n";
    }

    /**
     * 非枚举类的转化
     * @param beanClass 类
     * @param step 第几层嵌套
     */
    private static String cls2Proto(Class<?> beanClass, int step){
        StringBuilder sb = new StringBuilder();
        tab(sb, step);
        //写message结构
        sb.append("message ").append(beanClass.getSimpleName()).append(" {\n\n");

        //遍历内部类
        Class<?>[] classes = beanClass.getDeclaredClasses();
        for (Class<?> cls: classes) {
            //区分是枚举还是普通类
            String string = cls.isEnum() ? enum2Proto(cls, step + 1)
                    : cls2Proto(cls, step + 1);
            sb.append(string);
        }
        //遍历变量
        Field[] fields = beanClass.getDeclaredFields();
        TagRecorder tagRecorder = new TagRecorder();
        for (Field field : fields){
            //过滤static变量、transient变量、外部类引用
            if (!isTransientOrStatic(field)
                    && !isEnclosing(field, beanClass)){
                String str = field2Proto(field, tagRecorder);
                tab(sb, step + 1);
                sb.append(str).append("\n");
            }
        }
        tab(sb, step);
        sb.append("}\n\n");
        return sb.toString();
    }

    private static class TagRecorder {

        private int offset = 1; //初始偏移
        private SparseArrayCompat<Boolean> record = new SparseArrayCompat<>();

        /**
         * 设置标记值
         * @return 最终的标记值
         */
        int tag(int num) {
            if (num <= 0) {
                // 传非正数表示自动从最小开始寻找空位
                // 偏移直到没有被占用为止
                while (record.get(offset, false) && offset < Integer.MAX_VALUE) {
                    offset++;
                }
                if (offset == Integer.MAX_VALUE) {
                    throw new IllegalStateException("tag reaches MAX Integer");
                }
                record.put(offset, true);
                return offset;
            } else if (record.get(num, false)) {
                // 传正数则直接使用
                throw new IllegalStateException("tag is duplicated");
            } else {
                record.put(num, true);
                return num;
            }
        }

    }

    /**
     * 判断变量是不是外部类的引用
     */
    private static boolean isEnclosing(Field field, Class<?> cls){
        Class<?> enclosingCls = cls.getEnclosingClass();
        return enclosingCls != null && enclosingCls == field.getType();
    }

    /**
     * 判断变量是不是static或transient
     * @param field 变量
     */
    private static boolean isTransientOrStatic(Field field){
        int modifiers = field.getModifiers();
        return Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers);
    }

    /**
     * 变量转化
     * @param field 变量
     * @param tagRecorder 标记记录器
     */
    private static String field2Proto(Field field, TagRecorder tagRecorder){
        Class<?> cls = field.getType();
        StringBuilder sb = new StringBuilder();

        if (List.class.isAssignableFrom(cls)){
            sb.append("repeated ").append(list2Proto(field));
        } else if (isArray(cls)){
            sb.append("repeated ").append(array2Proto(field));
        } else {
            sb.append(checkRequired(field)).append(getTypeString(cls));
        }

        int tagNumber = tagRecorder.tag(getTagNumber(field));
        sb.append(" ").append(field.getName()).append(" = ").append(tagNumber).append(";\n");
        return sb.toString();
    }

    /**
     * 查验是否是required
     * @param field 变量
     * @return 根据是否必需返回不同的关键字
     */
    private static String checkRequired(Field field){
        Proto proto = field.getAnnotation(Proto.class);
        if (proto == null || proto.required()){
            return "required ";
        } else {
            return "optional ";
        }
    }

    private static String array2Proto(Field field){
        Class<?> componentCls = field.getType().getComponentType();
        return getTypeString(componentCls);
    }

    /**
     * 根据注解获取要求的标记值，缺省为0
     */
    private static int getTagNumber(Field field){
        Proto proto = field.getAnnotation(Proto.class);
        return proto != null ? proto.tag() : 0;
    }

    /**
     * list型数据转化
     */
    private static String list2Proto(Field field){
        Type type = field.getGenericType();
        if (type != null && type instanceof ParameterizedType){
            ParameterizedType pt = (ParameterizedType) type;
            Class genericCls = (Class)pt.getActualTypeArguments()[0];
            if (genericCls != null){
                return getTypeString(genericCls);
            } else {
                throw new IllegalArgumentException("genericType is missing");
            }
        } else {
            throw new IllegalArgumentException("genericType is missing");
        }
    }

    /**
     * 是否是数组，其中byte数组另外有类型代替，因此不当作数组
     */
    private static boolean isArray(Class<?> cls){
        Class<?> componentCls = cls.getComponentType();
        return componentCls != null && !byte.class.isAssignableFrom(componentCls);
    }


    private static String getTypeString(Class<?> cls){
        if (cls.isAssignableFrom(Double.class)
                || cls.isAssignableFrom(double.class)) {
            return "double";
        } else if (cls.isAssignableFrom(Float.class)
                || cls.isAssignableFrom(float.class)) {
            return "float";
        } else if (cls.isAssignableFrom(Long.class)
                || cls.isAssignableFrom(long.class)) {
            return "int64";
        } else if (cls.isAssignableFrom(Integer.class)
                || cls.isAssignableFrom(int.class)) {
            return "int32";
        } else if (cls.isAssignableFrom(Boolean.class)
                || cls.isAssignableFrom(boolean.class)) {
            return "bool";
        } else if (cls.isAssignableFrom(String.class)) {
            return "string";
        } else if (cls.isAssignableFrom(byte[].class)) {
            return "bytes";
        } else {
            return cls.getSimpleName();
        }
    }

}
