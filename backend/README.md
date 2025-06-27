# TomatoMall Backend

> 2025年软工二大作业 - Spring Boot后端服务

## 项目简介

TomatoMall后端是番茄书城的服务端实现，提供完整的图书交易平台后端服务。采用Spring Boot框架构建，支持用户管理、商店管理、商品交易、支付处理、评论系统等核心功能。

## 技术栈

- **后端框架**: Spring Boot 2.3.4.RELEASE
- **Java版本**: JDK 8
- **数据库**: MySQL 8.0+
- **ORM框架**: Spring Data JPA + Hibernate
- **认证授权**: JWT (java-jwt 3.10.3)
- **数据验证**: Hibernate Validator
- **支付集成**: 支付宝SDK 4.40.133
- **云存储**: 阿里云OSS 3.15.1
- **实时通信**: WebSocket
- **重试机制**: Spring Retry
- **工具库**: Lombok、Commons IO
- **构建工具**: Maven

## 核心功能模块

- 👤 **用户系统** - 注册登录、个人信息管理、JWT认证
- 🏪 **商店管理** - 多商家入驻、店铺信息管理、员工权限
- 📚 **商品管理** - 商品CRUD、库存管理、商品快照
- 🛒 **订单系统** - 购物车、订单流程、状态管理
- 💰 **支付系统** - 支付宝集成、支付状态跟踪
- 🚚 **物流管理** - 发货跟踪、物流信息更新
- 📝 **评论系统** - 商品评价、店铺评分、回复功能
- 📢 **广告系统** - 广告位管理、投放审核
- 🔍 **搜索功能** - 商品搜索、店铺搜索
- 💬 **消息通知** - WebSocket实时消息、系统通知

## 环境要求

- **JDK**: 8+
- **MySQL**: 8.0+
- **Maven**: 3.6+
- **内存**: 推荐4GB+

## 快速开始

### 1. 克隆项目

```bash
git clone <repository-url>
cd TomatoMall/backend
```

### 2. 数据库配置

创建MySQL数据库：

```sql
CREATE DATABASE tomatomall CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

修改 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/TomatoMall?createDatabaseIfNotExist=true&characterEncoding=utf-8mb4&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: your_mysql_username
    password: your_mysql_password
  
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect

# 初始化数据配置
app:
  init-data:
    root-user:
      username: admin # 管理员用户名
      phone: # 管理员手机号
      password: # 管理员密码
```

### 3. 第三方服务配置

如需使用完整功能，请在 `application.yml` 中完善：

```yaml
# 阿里云OSS配置（文件上传功能）
aliyun:
  oss:
    endpoint: your-oss-endpoint
    accessKeyId: your-access-key-id
    accessKeySecret: your-access-key-secret
    bucketName: your-bucket-name

# 支付宝配置（支付功能）
alipay:
  app-id: your-app-id
  gateway: https://openapi-sandbox.dl.alipaydev.com/gateway.do
  notify-url: http://your-domain/api/alipay/notify
  sign-type: RSA2
  charset: UTF-8
  app-cert-path: classpath:alipay/appPublicCert.crt
  alipay-cert-path: classpath:alipay/alipayPublicCert.crt
  alipay-root-cert-path: classpath:alipay/alipayRootCert.crt
  private-key-path: classpath:alipay/appPrivateKey.txt
```

### 4. 运行项目

```bash
# 方式1: 使用Maven直接运行
mvn spring-boot:run

# 方式2: 打包后运行
mvn clean package
java -jar target/TomatoMall-0.0.1-SNAPSHOT.jar
```

### 5. 验证部署

访问 http://localhost:8080，如果看到Spring Boot启动页面则部署成功。

## 项目结构

```
src/main/java/cn/edu/nju/TomatoMall/
├── TomatoMallApplication.java    # 主启动类
├── configure/                    # 配置类
├── controller/                   # 控制器层 - REST API接口
├── enums/                       # 枚举定义
├── exception/                   # 全局异常处理
├── models/                      # 数据模型
│   ├── dto/                    # 数据传输对象
│   ├── po/                     # 持久化对象(实体类)
│   └── vo/                     # 视图对象
├── repository/                  # 数据访问层 - JPA Repository
├── service/                     # 业务逻辑层
│   └── impl/                   # 服务实现类
├── util/                       # 工具类
└── websocket/                  # WebSocket相关
```

## API文档

完整的API接口文档请访问： 🔗 **[TomatoMall API Documentation](https://apifox.com/apidoc/shared-de9913da-f5ee-489b-9356-e7553b6ed953)**

### 主要接口概览

| 模块     | 接口路径          | 功能说明                 |
| -------- | ----------------- | ------------------------ |
| 用户管理 | `/api/users/*`    | 注册、登录、个人信息管理 |
| 商店管理 | `/api/stores/*`   | 商店CRUD、员工管理       |
| 商品管理 | `/api/products/*` | 商品CRUD、库存管理       |
| 订单管理 | `/api/orders/*`   | 购物车、订单处理         |
| 支付管理 | `/api/payments/*` | 支付流程、状态查询       |
| 评论系统 | `/api/comments/*` | 评论、回复、点赞         |
| 搜索功能 | `/api/search/*`   | 商品、店铺搜索           |

## 数据库设计

系统采用MySQL关系型数据库，主要数据表包括：

- **users** - 用户表
- **stores** - 商店表
- **products** - 商品表
- **orders** - 订单表
- **payments** - 支付表
- **comments** - 评论表
- **messages** - 消息表
- **inventories** - 库存表

首次运行时，Spring Data JPA会根据实体类自动创建数据表结构。

## 测试

项目集成了完整的测试套件：

```bash
# 运行所有测试
mvn test

# 运行集成测试
mvn integration-test

# 生成测试报告
mvn surefire-report:report
```

### 测试工具

- **JUnit Jupiter** - 单元测试框架
- **Testcontainers** - 数据库集成测试
- **Mockito** - Mock测试
- **WireMock** - HTTP服务模拟
- **H2** - 内存数据库

## 部署指南

### 开发环境

```bash
# 使用开发配置启动
java -jar target/TomatoMall-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

### 生产环境

```bash
# 构建生产包
mvn clean package -Pprod

# 使用生产配置启动
java -jar target/TomatoMall-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### Docker部署

```bash
# 构建镜像
docker build -t tomatomall-backend .

# 运行容器
docker run -d -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e MYSQL_URL=jdbc:mysql://host:3306/tomatomall \
  tomatomall-backend
```

