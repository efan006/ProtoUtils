cd ./proto
java -jar -Dfile.encoding=UTF-8 wire-compiler-2.2.0-jar-with-dependencies.jar --proto_path=. --java_out=./ ProtoBean.proto
pause
start.\com\efan\proto
exit
