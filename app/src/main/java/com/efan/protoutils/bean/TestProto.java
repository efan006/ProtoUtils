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
package com.efan.protoutils.bean;

import java.util.List;

import com.efan.protoutils.annotation.Proto;

/**
 * 测试普通类型
 * Created by wangkw on 2016/9/29.
 */

public class TestProto {

    private int _int;
    private Integer __Int;
    private double _double;
    private Double __Double;
    private float _float;
    private Float __Float;
    private long _long;
    private Long __Long;
    private boolean _boolean;
    private Boolean __Boolean;
    private String _String;
    private byte[] _bytes;

    @Proto(tag = 300)
    private ListProto listProto;
    @Proto(tag = 301)
    private ArrayProto arrayProto;
    @Proto(tag = 302)
    private List<DAY> days;

    private enum DAY{
        MON,TUE,WED,THU,FRI,SAT,SUN
    }

    private class ListProto {
        private List<Integer> _intList;
        private List<Double> _doubleList;
        private List<Float> _floatList;
        private List<Long> _longList;
        private List<Boolean> _booleanList;
        private List<String> _stringList;
    }








}
