# pinpoint-xxljob-plugin

#### 介绍

xxl-job pinpoint插件

公司最近接入了pinPoint分布式全链路监控，分布式任务调度用到了开源软件[xxl-job](https://gitee.com/xuxueli0323/xxl-job)

由于XXL-JOB全异步化设计：XXL-JOB系统中业务逻辑在远程执行器执行，触发流程全异步化设计。所以pinpoint不支持异步追踪，经过半天研究xxl-job插件源码和pinpoint源码，发现可以自定义插件实现异步功能，故开发了当前插件，经过初步测试拿到了结果，开源给大家供大家参考学习，有不合理的地方还望指正，大家一起学习进步。

注意pinpoint版本是：1.8.0，xxl-job版本是1.9.2

安装插件步骤：

1.下载源码打包编译，依赖的pinpoint包官方有提供下载，这里不赘述。

2.分别将打包好的插件包pinpoint-xxljob-plugin-1.8.0.jar放入客服端agent的plugin目录下面和服务端-collector和web下面的WEB-INFO/lib包下面

3.分别重启客服端和服务端，然后通过web监控页面即可看到效果，效果截图如下：


![输入图片说明](https://images.gitee.com/uploads/images/2019/0517/190227_d6870fb3_1274748.png "360截图18830822897670.png")

![输入图片说明](https://images.gitee.com/uploads/images/2019/0517/191002_9775c568_1274748.png "360截图18141219425675.png")
