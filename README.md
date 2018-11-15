# hostswapAgent

注意事项：
1.不要使用远程调试，加载agent会有问题.
2.启动时-Xbootclasspath/a:/home/user/jdk/lib/tools.jar -jar /xx.jar，因为instrument工具类在此jar里，否则加载agent失败
3.修改agent jar上传后要重启jvm，避免jar不重新加载
4.要将需要重新加载的class放入agent的jar包内