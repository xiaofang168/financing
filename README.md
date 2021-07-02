# financing
个人投资管理后端服务

## 技术栈
- cats
   - OptionT
   - Traverse
- chimney对象copy
- akka http
- reactiveMongo

## maven打可运行jar包
```
$ mvn assembly:assembly
$ unzip financing-assembly.zip
$ sh run.sh
$ sh stop.sh
```