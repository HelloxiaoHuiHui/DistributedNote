# Spring Cloud整合Consul实现服务注册和发现

    好记性不如烂笔头，看了几篇微服务相关文章，根据其中一篇记录下微服务概念。
引用：
* [Consul实现服务注册和发现](https://blog.csdn.net/lvhu123/article/details/90692752?depth_1-utm_source=distribute.pc_relevant.none-task&utm_source=distribute.pc_relevant.none-task "Consul")
*  [注册中心 Consul 使用详解](http://www.ityouknow.com/springcloud/2018/07/20/spring-cloud-consul.html)

**Consul简介：**一款适用于分布式系统下服务发现和配置的开源工具，支持服务健康检查、key/value数据存储、分布一致性实现、多数据中心方案，并且内置服务注册和发现框架，不再需要依赖其它工具（比如 ZooKeeper 等）,支持Spring Cloud集成。Consul 使用 Go 语言编写，因此具有天然可移植性(支持Linux、windows和Mac OS X)；安装包仅包含一个可执行文件，方便部署，与 Docker 等轻量级容器可无缝配合。

**安装consul**

1. 下载安装包：https://www.consul.io/downloads.html 。若是在Windows上下载win版本的，若是在Linux上下载Linux版的。
2. win7安装consul：consul.exe文件所在的文件路径添加到Path中，以便在命令行中可以直接使用consul命令。命令行输入：consul、或者consul --version，出现版本号就表示成功。
3. 启动：命令行输入以下命令：consul agent -dev （以开发模式启动代理,不会持久化信息）。也可以不用放path，在同级目录下执行consul agent -dev启动consul服务端也可以。
4. 访问：在浏览器中输入地址：http://localhost:8500 。看到consul界面则表示安装成功

**Consul 工作原理：**

![图片][consul1]

[consul1]:http://favorites.ren/assets/images/2018/springcloud/consol_service.png "consul"

1. 当 Producer 启动的时候，会向 Consul 发送一个 post 请求，告诉 Consul 自己的 IP 和 Port
2. Consul 接收到 Producer 的注册后，每隔10s（默认）会向 Producer 发送一个健康检查的请求，检验Producer是否健康
3. 当 Consumer 发送 GET 方式请求 /api/address 到 Producer 时，会先从 Consul 中拿到一个存储服务 IP 和 Port 的临时表，从表中拿到 Producer 的 IP 和 Port 后再发送 GET 方式请求 /api/address
4. 该临时表每隔10s会更新，只包含有通过了健康检查的 Producer


# 代码实现

首先，创建一个Maven父项目springcloudconsultest，用于管理项目依赖包版本。由于Spring Cloud组件很多，为保证不同组件之间的兼容性，一般通过spring-cloud-dependencies统一管理Spring Cloud组件版本，而非每个组件单独引入。

pom.xml如下：

```
<parent>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-parent</artifactId>
	<version>2.2.6.RELEASE</version>
	<relativePath/> <!-- lookup parent from repository -->
</parent>
<groupId>com.consultest</groupId>
<artifactId>springcloudconsultest</artifactId>
<version>0.0.1-SNAPSHOT</version>
<name>springcloudconsultest</name>
<description>Spring Cloud整合Consul实现服务注册和发现</description>

<properties>
	<java.version>1.8</java.version>
	<spring-cloud.version>Hoxton.SR3</spring-cloud.version>
</properties>

<dependencies>
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-test</artifactId>
		<scope>test</scope>
		<exclusions>
			<exclusion>
				<groupId>org.junit.vintage</groupId>
				<artifactId>junit-vintage-engine</artifactId>
			</exclusion>
		</exclusions>
	</dependency>
</dependencies>

<dependencyManagement>
	<dependencies>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-dependencies</artifactId>
			<version>${spring-cloud.version}</version>
			<type>pom</type>
			<scope>import</scope>
		</dependency>
	</dependencies>
</dependencyManagement>

<build>
	<plugins>
		<plugin>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-maven-plugin</artifactId>
		</plugin>
	</plugins>
</build>
```

## 创建服务提供者

* 在springcloudconsultest项目下创建一个子项目consulProvider，添加spring-boot-starter-actuator和spring-cloud-starter-consul-discovery

pom.xml配置如下:

```
<parent>
    <artifactId>springcloudconsultest</artifactId>
    <groupId>com.consultest</groupId>
    <version>0.0.1-SNAPSHOT</version>
</parent>
<modelVersion>4.0.0</modelVersion>
<artifactId>consulProvider</artifactId>
<name>consulProvider</name>
<description>服务提供者</description>

<properties>
    <java.version>1.8</java.version>
</properties>
<dependencies>
    <!-- springmvc -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <!-- 支持健康检查 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <!-- Consul-->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-consul-discovery</artifactId>
    </dependency>
</dependencies>
```

* 添加Spring Boot配置文件application.yml，配置如下：

```
# 配置
server:
  port: 8019
  servlet:
    context-path: /consulProvider
spring:
  application:
    name: order-service # 应用服务名称
  cloud:
    consul:
      host: 127.0.0.1 # Consul所在ip
      port: 8500 # Consul监听端口
      discovery:
        register: true # 配置服务注册到Consul
        healthCheckPath: ${server.servlet.context-path}/health # 配置Consul实例的服务健康检查地址，默认为/health，使用非默认上下文路径也应修改这里
        healthCheckInterval: 2s # Consul健康检查频率，也叫心跳频率
        instance-id: ${spring.application.name} # 配置注册到Consul的服务id
        service-name: ${spring.application.name}
        tags: urlprefis-/${spring.application.name}
```

* consulProvider启动类添加注解@EnableDiscoveryClient向Consul注册服务 自动加载服务注册、心跳检测等。

```
@EnableDiscoveryClient //支持向Consul注册服务 自动加载服务注册、心跳检测等配置
@SpringBootApplication
public class ConsulProviderApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConsulProviderApplication.class, args);
	}

}
```

* 新建HealthCheckController添加consul需要调用的健康检查接口。为了方便把提供服务的方法也写在这里。

```
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 添加consul需要调用的健康检查接口
 * */
@RestController
public class HealthCheckContrller {

    // 改路径需要和配置文件中健康检查地址保持一致
    @GetMapping("/health")
    public String healthCheck(){
        return "ok";
    }

    /**
     * 模拟提供服务的方法
     */
    @RequestMapping("/services")
    public Object services(String param1,String param2){
        List<String> pList=new ArrayList<String>();
        pList.add(param1);
        pList.add(param2);
        pList.add("Hello 1");
        if(!pList.isEmpty()){
            System.out.println("模拟的业务代码，测试consul");
        }
        return pList;
    }
}
```
启动consulProvider成功后，浏览器输入localhost:8500可以看到order-service，表示服务注册成功。

## 服务消费者

* 在springcloudconsultest项目下创建一个子项目consulConsumer，添加spring-boot-starter-actuator和spring-cloud-starter-consul-discovery；pom配置和consulProvider一致即可。

* 添加Spring Boot配置文件application.yml，配置如下：

```
# 配置
spring:
  application:
    name: service-client
  cloud:
    consul:
      host: localhost # consul服务端
      port: 8500 # consul服务端监听端口
      discovery:
        register: false # 不需要注册到consul上，若需要改为true
server:
  port: 8020
```

* 启动类可以不添加注解@EnableDiscoveryClient。
        实际项目中如果consulConsumer也向其他项目提供服务的话，也需要和consulProvider中有类似配置。这里spring.cloud.consul.discovery.register置为false表示不向consul提供服务，所以主类上@EnableDiscoveryClient注解也可省略。

```
//@EnableDiscoveryClient // 已配置spring.cloud.consul.discovery.register=false,客户端不向consul注册服务，这个注解也可省略
@SpringBootApplication
public class ConsulConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConsulConsumerApplication.class, args);
	}

}
```

* 新建ApiController访问consul中服务

```
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private LoadBalancerClient loadBalancerClient;
    @Autowired
    private DiscoveryClient discoveryClient;

    @RequestMapping("/getServiceInfo")
    public Object getServiceInfo(){
        ServiceInstance serviceInstance=loadBalancerClient.choose("order-service"); //服务ID
        System.out.println(serviceInstance);
        String url="http://"+serviceInstance.getHost()+":"+serviceInstance.getPort()
                +"/consulProvider/services?param1={1}&param2={2}";

        return new RestTemplate().getForObject(url, List.class,"hello p1","hello p2");
    }

    /**
     * 从所有服务中选择一个服务
     */
    @RequestMapping("/discover")
    public Object discover(){
        return loadBalancerClient.choose("order-service").getUri().toString();
    }
    /**
     * 获取所有服务
     */
    @RequestMapping("/services")
    public Object services(){
        List<ServiceInstance> services = discoveryClient.getInstances("order-service");
        return services;
    }
}
```
开启consul-consumer成功后，在浏览器中分别输入：

http://localhost:8020/api/services

http://localhost:8020/api/discover

http://localhost:8020/api/getServiceInfo

此时就实现了Spring Cloud Consul服务注册、发现，这里只描述了单个消费者和单个服务提供者的例子，如果涉及到多服务集群的需求，参照consul-provider并修改相关host/port以及服务名称、访问路径等配置即可。
