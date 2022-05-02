
* 创建一个eureka-security-server模块，在pom.xml中添加以下依赖

> 需要添加SpringSecurity模块。

``` 
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

* 添加application.yml配置文件

> 主要是配置登录注册中心的用户名和密码。

``` 
server:
  port: 8004
spring:
  application:
    name: eureka-security-server
  security: #配置SpringSecurity登录用户名和密码
    user:
      name: root
      password: root
eureka:
  instance:
    hostname: localhost
  client:
    fetch-registry: false
    register-with-eureka: false
```

* 添加Java配置WebSecurityConfig

> 默认情况下添加SpringSecurity依赖的应用每个请求都需要添加CSRF token才能访问，Eureka客户端注册时并不会添加，所以需要配置/eureka/**路径不需要CSRF token。

```java

@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().ignoringAntMatchers("/eureka/**");
        super.configure(http);
    }
}
```

* 运行eureka-security-server，访问http://localhost:8004发现需要输入用户名&密码进行登录认证;

###  eureka-client注册到有登录认证的注册中心

* 配置文件中需要修改注册中心地址格式
> http://${username}:${password}@${hostname}:${port}/eureka/

* 添加application-security.yml配置文件，按格式修改用户名和密码

``` 
server:
  port: 8103
spring:
  application:
    name: eureka-client
eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://root:root@localhost:8004/eureka/
```

* 以application-security.yml配置运行eureka-client，可以在注册中心界面看到eureka-client已经成功注册

### Eureka的常用配置

``` 
eureka:
  client: #eureka客户端配置
    register-with-eureka: true #是否将自己注册到eureka服务端上去
    fetch-registry: true #是否获取eureka服务端上注册的服务列表
    service-url:
      defaultZone: http://localhost:8001/eureka/ # 指定注册中心地址
    enabled: true # 启用eureka客户端
    registry-fetch-interval-seconds: 30 #定义去eureka服务端获取服务列表的时间间隔
  instance: #eureka客户端实例配置
    lease-renewal-interval-in-seconds: 30 #定义服务多久去注册中心续约
    lease-expiration-duration-in-seconds: 90 #定义服务多久不去续约认为服务失效
    metadata-map:
      zone: jiangsu #所在区域
    hostname: localhost #服务主机名称
    prefer-ip-address: false #是否优先使用ip来作为主机名
  server: #eureka服务端配置
    enable-self-preservation: false #关闭eureka服务端的保护机制
```

使用到的模块:
``` 
springcloud-learning
├── eureka-server -- eureka注册中心
├── eureka-security-server -- 带登录认证的eureka注册中心
└── eureka-client -- eureka客户端
```



