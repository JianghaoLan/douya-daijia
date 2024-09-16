# 豆芽代驾

## 环境部署

### MySql

```shell
docker run --name mysql \
-e MYSQL_ROOT_PASSWORD=root \
-p 3306:3306 \
-v /path/to/data:/var/lib/mysql \
-v /path/to/conf:/etc/mysql/conf.d \
-d mysql:8.0.30 \
--character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
```

进入容器修改root用户权限以开启远程访问：

```
ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY 'root';
flush privileges;
```

### RabbitMQ

```shell
docker run -d \
--name=rabbitmq \
--restart=always \
-p 5672:5672 -p 15672:15672 \
rabbitmq:3.12.0-management
```

安装延迟队列插件

下载地址：https://github.com/rabbitmq/rabbitmq-delayed-message-exchange/releases

```shell
# 复制插件到容器
docker cp rabbitmq_delayed_message_exchange-3.12.0.ez rabbitmq:/plugins

# 进入容器内部操作
docker exec -it rabbitmq /bin/bash
cd /plugins
rabbitmq-plugins enable rabbitmq_delayed_message_exchange
```

重启容器

```shell
docker restart rabbitmq
```

### Redis

```shell
docker run --name redis -p 6379:6379 -d --restart=always redis:bookworm
```

### Nacos

```shell
docker run -d \
-e MODE=standalone \
-p 8848:8848 \
-p 9848:9848 \
-p 9849:9849 \
-v /your/logdir:/home/nacos/logs \
--name nacos2.1.1 \
--restart=always \
nacos/nacos-server:v2.1.1
```

用户名密码默认都为nacos

### Minio

```shell
docker run \
-p 9000:9000 \
-p 9001:9001 \
--name=minio \
-d --restart=always \
-e "MINIO_ROOT_USER=admin" \
-e "MINIO_ROOT_PASSWORD=admin123456" \
-v /path/to/config:/root/.minio \
-v /path/to/data:/data \
minio/minio server /data --console-address ":9001"
```

### Mongo DB

```shell
docker run -d --restart=always \
-p 27017:27017 \
-v /path/to/mongo/db:/data/db \
--name mongo mongo:7.0.0
```

### Seata

下载二进制包本地搭建。非localhost搭建会出现连不上的问题

附：docker启动方法

```shell
docker run --name seata-server \
--restart=always \
-p 8091:8091 -p 7091:7091 \
-d seataio/seata-server:1.7.1
```

