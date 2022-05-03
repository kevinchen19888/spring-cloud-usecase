### Spring Cloud Gateway：新一代API网关服务

#### Gateway 简介

Gateway是在Spring生态系统之上构建的API网关服务，基于Spring 5，Spring Boot 2和 Project
Reactor等技术。Gateway旨在提供一种简单而有效的方式来对API进行路由，以及提供一些强大的过滤器功能， 例如：熔断、限流、重试等。

Spring Cloud Gateway 具有如下特性：

* 基于Spring Framework 5, Project Reactor 和 Spring Boot 2.0 进行构建；
* 动态路由：能够匹配任何请求属性；
* 可以对路由指定 Predicate（断言）和 Filter（过滤器）；
* 集成Hystrix的断路器功能；
* 集成 Spring Cloud 服务发现功能；
* 易于编写的 Predicate（断言）和 Filter（过滤器）；
* 请求限流功能；
* 支持路径重写。

#### 相关概念

* Route（路由）：路由是构建网关的基本模块，它由ID，目标URI，一系列的断言和过滤器组成，如果断言为true则匹配该路由；
* Predicate（断言）：指的是Java 8 的 Function Predicate。 输入类型是Spring框架中的ServerWebExchange。
  这使开发人员可以匹配HTTP请求中的所有内容，例如请求头或请求参数。如果请求与断言相匹配，则进行路由；
* Filter（过滤器）：指的是Spring框架中GatewayFilter的实例，使用过滤器，可以在请求被路由前后对请求进行修改。

---

#### Gateway 使用示例

* 创建 api-gateway模块

* 在pom.xml中添加相关依赖

``` 
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
```

* 两种不同的配置路由方式

> Gateway 提供了两种不同的方式用于配置路由，一种是通过yml文件来配置，另一种是通过Java Bean来配置，下面分别介绍下。

1. 使用yml配置
    * 在application.yml中进行配置：
      ``` 
      server:
        port: 9201
      service-url:
        user-service: http://localhost:8201
      spring:
        cloud:
          gateway:
            routes:
              - id: path_route #路由的ID
                uri: ${service-url.user-service}/user/{id} #匹配后路由地址
                predicates: 
                  # 断言，路径相匹配的进行路由
                  - Path=/user/{id}
      ```
    * 启动eureka-server，user-service和api-gateway服务，并调用该地址测试：`http://localhost:9201/user/1`, 可以发现
      该请求被路由到了user-service的该路径上：`http://localhost:8201/user/1`;

2. 使用Java Bean配置
    * 添加相关配置类`GatewayConfig`，并配置一个RouteLocator对象：
    * 重新启动api-gateway服务，并调用该地址测试：`http://localhost:9201/user/getByUsername?username=andy`,
      可以发现发现该请求被路由到了user-service的该路径上：`http://localhost:8201/user/getByUsername?username=` ;

#### Route Predicate 的使用

Spring Cloud Gateway包括许多内置的Route Predicate工厂。 所有这些Predicate都与HTTP请求的不同属性匹配。 多个Route Predicate工厂可以进行组合;

> 注意：Predicate中提到的配置都在application-predicate.yml文件中进行修改，并用该配置启动api-gateway服务。

``` 
spring:
  cloud:
    gateway:
      routes:
        - id: after_route
          uri: ${service-url.user-service}
          predicates:
            # 断言，路径相匹配的进行路由
            - Path=/user/{id}
            # 在指定时间之后的请求会匹配该路由
            - After=2021-01-24T16:30:00+08:00[Asia/Shanghai]
            # 在指定时间之前的请求会匹配该路由
            - Before=22021-01-24T16:30:00+08:00[Asia/Shanghai]
            # 在指定时间区间内的请求会匹配该路由
            - Between=2019-09-24T16:30:00+08:00[Asia/Shanghai], 2019-09-25T16:30:00+08:00[Asia/Shanghai]
            # 带有指定Cookie的请求会匹配该路由
            - Cookie=username,andy
            # 带有指定请求头的请求会匹配该路由
            - Header=X-Request-Id, \d+
            # 带有指定Host的请求会匹配该路由 
            - Host=**.kevin.com
            
```

使用curl工具发送带有cookie为username=andy的请求可以匹配该路由。
> curl `http://localhost:9201/user/1` --cookie "username=andy"

使用curl工具发送带有请求头为X-Request-Id:123的请求可以匹配该路由。
> curl `http://localhost:9201/user/1` -H "X-Request-Id:123"

使用curl工具发送带有请求头为Host:`www.kevin.com`的请求可以匹配该路由。

> curl `http://localhost:9201/user/1` -H "Host:`www.kevin.com`"

以及还有其他路由规则...

### Route Filter 的使用

> 路由过滤器可用于修改进入的HTTP请求和返回的HTTP响应，路由过滤器只能指定路由进行使用。Spring Cloud Gateway 内置了多种路由过滤器，他们都由GatewayFilter的工厂类来产生

``` 
spring:
  cloud:
    gateway:
      routes:
        - id: add_request_parameter_route
          uri: http://localhost:8201
          filters:
            # 给请求添加参数的过滤器
            - AddRequestParameter=username, andy
          predicates:
            - Method=GET
```

对GET请求添加username=andy的请求参数，通过curl工具使用以下命令进行测试
> curl `http://localhost:9201/user/getByUsername`

相当于发起该请求：
> curl `http://localhost:8201/user/getByUsername?username=andy`

* Hystrix GatewayFilter    
  Hystrix 过滤器允许将断路器功能添加到网关路由中，使服务免受级联故障的影响，并提供服务降级处理。
    * 要开启断路器功能，我们需要在pom.xml中添加Hystrix的相关依赖：
        ``` 
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
        </dependency>
        ```
    * 然后添加相关服务降级的处理类：FallbackController
    * 在application-filter.yml中添加相关配置，当路由出错时会转发到服务降级处理的控制器上：
    * 关闭user-service，调用该地址进行测试：`http://localhost:9201/user/1` ，发现已经返回了服务降级的处理信息。

* RequestRateLimiter GatewayFilter   
RequestRateLimiter 过滤器可以用于限流，使用RateLimiter实现来确定是否允许当前请求继续进行，如果请求太大默认会返回HTTP 429-太多请求状态。   
    * 在pom.xml中添加相关依赖: 
        ```
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
        </dependency>
        ```
    * 添加限流策略的配置类:RedisRateLimiterConfig，这里有两种策略一种是根据请求参数中的username进行限流，另一种是根据访问IP进行限流；
    * 使用Redis来进行限流，所以需要添加Redis和RequestRateLimiter的配置，这里对所有的GET请求都进行了按IP来限流的操作；
    * 多次请求该地址：`http://localhost:9201/user/1` ，会返回状态码为429的错误；

[comment]: <> (    TODO: 此处演示发现未成功进行限流,待进一步确认)

* Retry GatewayFilter   
  对路由请求进行重试的过滤器，可以根据路由请求返回的HTTP状态码来确定是否进行重试。
    * 修改配置文件,添加 retry_route：
    * 当调用返回500时会进行重试，访问测试地址：`http://localhost:9201/user/111`;
    * 可以发现user-service控制台报错2次，说明进行了一次重试。


* 结合注册中心使用   

使用Zuul作为网关结合注册中心进行使用时，默认情况下Zuul会根据注册中心注册的服务列表，以服务名为路径创建动态路由，Gateway同样也实现了该功能,
此处不再详述

* 使用到的模块

``` 
spring-cloud-usecase
├── eureka-server -- eureka注册中心
├── user-service -- 提供User对象CRUD接口的服务
└── api-gateway -- gateway作为网关的测试服务
```























