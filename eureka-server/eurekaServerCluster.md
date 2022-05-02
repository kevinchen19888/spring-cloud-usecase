### 搭建两个注册中心

> 由于所有服务都会注册到注册中心去，服务之间的调用都是通过从注册中心获取的服务列表来调用，注册中心一旦宕机，所有服务调用都会出现问题。所以我们需要多个注册中心组成集群来提供服务，下面将搭建一个双节点的注册中心集群。

* 给eureka-sever添加配置文件application-replica1.yml配置第一个注册中心

``` 
server:
  port: 8002
spring:
  application:
    name: eureka-server
eureka:
  instance:
    hostname: replica1
  client:
    serviceUrl:
      defaultZone: http://replica2:8003/eureka/ #注册到另一个Eureka注册中心
    fetch-registry: true
    register-with-eureka: true
```

* 给eureka-sever添加配置文件application-replica2.yml配置第二个注册中心

``` 
server:
  port: 8003
spring:
  application:
    name: eureka-server
eureka:
  instance:
    hostname: replica2
  client:
    serviceUrl:
      defaultZone: http://replica1:8002/eureka/ #注册到另一个Eureka注册中心
    fetch-registry: true
    register-with-eureka: true
```

我们通过两个注册中心互相注册，搭建了注册中心的双节点集群，由于defaultZone使用了域名，所以还需在本机的host文件中配置一下。

* 修改本地host文件

``` 
127.0.0.1 replica1
127.0.0.1 replica2
```

### 运行Eureka注册中心集群

设置从原启动配置中复制两个出来,分别设置 active profiles 为:replica1/replica2;

* 启动两个eureka-server，访问其中一个注册中心http://replica1:8002/, 发现另一个已经成为其备份

* 修改Eureka-client，让其连接到集群

> 添加eureka-client的配置文件application-replica.yml，让其同时注册到两个注册中心。

``` 
server:
  port: 8102
spring:
  application:
    name: eureka-client
eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://replica1:8002/eureka/,http://replica2:8003/eureka/ #同时注册到两个注册中心
```
* 以该配置文件启动后访问任意一个注册中心节点都可以看到eureka-client













