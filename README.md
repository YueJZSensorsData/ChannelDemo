# 多渠道打包插件配置

类似如下代码，但却对所有渠道都生效了，后面我记录了这个问题，这篇文章也分析了这个问题及解决方案

```groovy
productFlavors.all { flavor ->
        flavor.manifestPlaceholders = [DOWNLOAD_CHANNEL: name] //动态地修改AndroidManifest中的渠道名
        if (name == 'GooglePlay'){
            sensorsAnalytics {
                sdk {
                    disableIMEI = true
                }
            }
        }
    }

```

首先我看了网上很多关于[多渠道打包教程](<https://juejin.im/post/5cdbf5e3e51d45473d10ff11#heading-1>)，网上多渠道打包教程大多是一些如何记录渠道变量、如何配置变体、以及如何根据不同的渠道，引入不同的第三方包，但是对于多渠道，如何配置插件，并没有相关文章的介绍，我尝试用客户的代码，的确发现了这种写法有问题，比如我在 productFlavors 做了如下的配置

```groovy
if (name == 'GooglePlay') {
    println("productFlavors_googleplay")
}else if (name == 'yingyongbao') {
    println("productFlavors_yingyongbao")
}
```

然后利用  ./gradlew assembleGooglePlay 打了 GooglePlay 的渠道包，但是会发现编译日志把输出都打印了

```groovy
Creating configuration testHuaweiImplementation
Creating configuration testHuaweiRuntimeOnly
Creating configuration testHuaweiCompileOnly
Creating configuration testHuaweiWearApp
Creating configuration testHuaweiAnnotationProcessor
productFlavors_googleplay
productFlavors_yingyongbao
Parsing the SDK, no caching allowed
Parsing /Users/yuejz/Library/Android/sdk/build-tools/25.0.3/package.xml
Parsing /Users/yuejz/Library/Android/sdk/build-tools/26.0.2/package.xml
Parsing /Users/yuejz/Library/Android/sdk/build-tools/26.0.3/package.xml
```

查看编译日志会发现，虽然执行的命令是 assembleGooglePlay，但是日志中有很多渠道的日志，怀疑是编译时会初始化各个渠道的值，会导致如果我用这种 if else 判断渠道，生效的会是最后一个 if 里面的值，因为 if 里面的条件都会走，最后一个配置会覆盖之前的配置，所以用这种方法判断是不准确的

```
Creating configuration yingyongbaoCompile
Creating configuration yingyongbaoApk
Creating configuration yingyongbaoProvided
Creating configuration yingyongbaoApi
Creating configuration yingyongbaoImplementation
...
Creating configuration GooglePlayCompile
Creating configuration GooglePlayApk
Creating configuration GooglePlayProvided
Creating configuration GooglePlayApi
Creating configuration GooglePlayImplementation
Creating configuration GooglePlayRuntimeOnly
Creating configuration GooglePlayCompileOnly
...
Creating configuration HuaweiApk
Creating configuration HuaweiProvided
Creating configuration HuaweiApi
Creating configuration HuaweiImplementation
Creating configuration HuaweiRuntimeOnly
Creating configuration HuaweiCompileOnly
Creating configuration HuaweiWearApp
Creating configuration HuaweiAnnotationProcessor
Creating configuration androidTestHuaweiCompile
Creating configuration androidTestHuaweiApk
Creating configuration androidTestHuaweiProvided
Creating configuration androidTestHuaweiApi
Creating configuration androidTestHuaweiImplementation
```

然后又尝试了在 productFlavors 中利用 task 名称判断，代码如下：

```groovy
    productFlavors.all { flavor ->
        flavor.manifestPlaceholders = [DOWNLOAD_CHANNEL: name] 
        gradle.startParameter.getTaskNames().each {task ->
            println("task" + task)
            if (task.contains(name)) {
                println("gradle.startParameter.getTaskNames" + name)
                if (name == 'GooglePlay'){
                    println("gradle.startParameter.getTaskNames.SA" + name)
                    sensorsAnalytics {
                        sdk {
                            disableIMEI = true
                        }
                    }
                }

            }
        }
    }
```

此方法用 ./gradlew assembleyingyongbao 与 ./gradlew assembleGooglePlay  测试，能正确的配置成功，但是并没有结束，利用  ./gradlew assemble 打全量的渠道包，此时配置是不成功的，猜测原因应该是 productFlavors 的函数只会在编译初始化阶段执行，之后不会执行此代码，日志也是只有在编译刚开始会打印，但在打全量包在执行 assembleGooglePlay 任务是不会打印，如果我直接利用 ./gradlew assembleGooglePlay 此命令，在初始化的时候，任务也是包含渠道的，所以能判断成功

呃。。。。

看来是不能在 productFlavors 中处理了，需要看看有没有其它的方法曲线救国，然后又尝试了一种方法，代码如下：

```groovy
//在 Project 配置结束后调用
this.afterEvaluate { Project project ->
    //需要特殊配置的渠道
    def channelName = "yingyongbao"
    def allTask =  project.tasks.getNames()
    //测试发现，渠道是yingyongbao，但是会执行
    // transformClassesWithSensorsAnalyticsAutoTrackForYingyongbaoRelease 这样名称的任务，
    // 所以需要对大小写进行处理
    allTask.each { taskName ->
        if (taskName.toUpperCase().contains("transformClassesWithSensorsAnalyticsAutoTrackFor".toUpperCase() + channelName.toUpperCase())) {
            def preBuildTask = project.tasks.getByName(taskName)
            //开始任务之前的配置
            preBuildTask.doFirst {
                println("preBuildTaskdoFirst" + channelName)
                sensorsAnalytics {
                    sdk {
                        disableIMEI = true
                    }
                }
            }
            //任务结束之后的配置，防止影响其它任务
            preBuildTask.doLast {
                println("preBuildTaskdoLast" + channelName)
                sensorsAnalytics {
                    sdk {
                        disableIMEI = false
                    }
                }
            }
        }
    }
}
```

此方法测试成功

方法代码参考 [GitHub](https://github.com/YueJZSensorsData/ChannelDemo) 网址
