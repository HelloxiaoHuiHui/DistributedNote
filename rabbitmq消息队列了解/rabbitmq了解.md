# rabbitmq了解
引用：[rabbitmq教程](https://blog.csdn.net/hellozpc/article/details/814369801 "rabbitmq教程")
引用：[RabbitMQ基础教程之基本使用篇](https://www.cnblogs.com/yihuihui/p/9127300.html "RabbitMQ基础教程之基本使用篇")
引用：[Java中简单使用RabbitMQ进行消息收发](https://blog.csdn.net/D578332749/article/details/86147632 "Java中简单使用RabbitMQ进行消息收发")

    安装和启动什么的百度看教程

**消息分发策略**
1. 直接模式：适用于精准的消息分发
2. 路由模式：Routing Key的匹配模式，支持Routing Key的模糊匹配方式，更适用于多类消息的聚合
3. 扇形（广播）模式：忽略Routing Key, 将消息分配给所有的Queue，广播模式，适用于消息的复用场景

**ACK**

    ack机制：Consumer接收到了消息之后，必须返回一个ack的标志，表示消息是否成功消费，如果返回true，
    则表示消费成功了，然后这个消息就会从RabbitMQ的队列中删掉；如果返回false，且设置为重新入队，
    则这个消息可以被重新投递进来。通常实际编码中，默认是自动ACK的，如果消息的重要性程度较高，
    我们应该设置为主动ACK，在接收到消息之后，自主的返回对应的ACK信息
