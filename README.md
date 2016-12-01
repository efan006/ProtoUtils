# Proto辅助工具 

可以把工程里的Java原生类转成支持proto协议的Java类  
目前只支持基本的proto2  
不支持二维数组，不支持Map,   
不支持char和char数组  
不支持import x.proto  
不支持default（与proto3一致）  
枚举值一定从0开始（与proto3一致） 

可选择用Square的还是Google的方式 

# 使用方式
1. 把想要转化的java原生bean拷贝到这个工程
2. 配置ProtoUtils.main方法

``` java
    public static void main(String[] args) throws IOException {
        String outerName = "ProtoBean";             //输出的类名
        String packageName = "com.efan.proto";      //输出的包名
        genProtoFile(packageName, outerName, ArrayProto.class, TestProto.class);    //传入需要转化的java类，可以多个
        String outputPath = packageName.replace(".", File.separator);
        squareCompile(outputPath, outerName);       //Square方式，方法数和代码量是Google的1/5
//        googleCompile(outputPath, outerName);     //Google方式
    }
```

3. 右键Run这个方法，会弹出cmd  
4. 转化成功按任意键会弹出最终java类所在的文件夹  
