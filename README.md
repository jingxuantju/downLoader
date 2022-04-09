# downLoader
## 多线程下载器

该项目是个基于Java开发的多线程下载器，通过对目标资源的分片，使用多线程并行下载，并同时每秒打印下载信息，实现网络资源的高效下载。可以巩固对JUC包的理解。

基于Java自定义线程池**ThreadPoolExecutor**类实现对网络资源的高效分片下载。

使用**ScheduledExecutorService**类实现每秒对下载信息的打印。

使用**LongAdder**等原子类保证在多线程操作下变量的数据安全。

使用**CountDownLatch**保证多线程操作下整个下载全部完成才会删除临时文件。

自定义日志类**LogUtils**记录下载的相关信息。