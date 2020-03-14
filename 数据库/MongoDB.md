目前在NoSQL领域中, mongodb一直被大家所推荐.让我们通过对mysql的比较, 分析它的不同

## MongoDB
> MongoDB is a document database designed for ease of development and scaling.
这句话体现了MongoDB的一个重点 `document, 文档`. 而对于sql来说它是基于**行存储**模式  
```
{
  name: "sure",
  age: "26",
  status: "A",
  scores: {
    math: 90,
    history: 80
   }
  teacher: tearcherId(另一个集合)
}
```
来看下它的一些特点:
- 一个文档也可以称作键值对的结合
- 它的结构类似于JSON
- 无需要对document设定schema, 想加什么就加什么. 
- 每一个document存在的值都可以是不一样
- 支持各种数据类型, 甚至包括数组, 文档嵌入式子文档

![Nosql vs Mysql](https://img.draveness.me/2017-09-06-Translating-Between-RDBMS-and-MongoDB.jpg-1000width)  

`row`的集合`为table`, `document`的集合为`collection`. 我们知道对于表来说, 两张表直接通常是靠join来连接的.   
对于MongoDB, 两个集合可以通过引用的方式(比如上文的teacher).但引用不能叫做join, 它不可以直接通过join的方式查找, 需要使用额外的查询找到引用的该模型.会增加客户端和MongoDB的交互次数, 导致查询变慢, 影响性能.  
第二种是通过内嵌(embedded), 类似上文的teacher, 我们直接把它依附在这个学生集合中, 减少查询和交互次数.

## 事务
早先版本中, MongoDB对单个文档操作都是属于原子性的, 我们可以通过嵌入文档或Array来避免对多文档,多集合的操作.  
但4.2之后支持多个文档或集合的事务操作, 也支持分片集群事务功能  

每个事务运行前会开启一个snapshot,同时会给相应的集合,数据库上意向锁, document上S或者X锁,来保证ACID.(注意,这里的事务跟mysql的事务不同, 这里的事务很简单)

## 索引Indexs
类似Mysql, 使用B-Tree构建索引. 支持单键索引, 复合索引, 多键索引, 文本索引

## 分片
> Sharding is a method for distributing data across multiple machines. MongoDB uses sharding to support deployments with very large data sets and high throughput operations.

![Shared](https://docs.mongodb.com/manual/_images/sharded-cluster-production-architecture.bakedsvg.svg)

分片:
MongoDB分片集群将数据分布在一个或多个分片上, 每个分片部署成一个MongoDB副本集,该副本集保存了集群整体数据的一部分.(因为每个分片都是一个副本集,所以它们用自己的复制机制,能够自动进行故障转移)

mongos路由器:
我们需要一个接口连接整个集群, 这就是mongos. mongos进程是一个路由器, 将所有的读写请求指引到合适的分片上.  
mongos进程是轻量级且非持久化的, 最好的部署的方式是将mongos部署在和你应用服务器的同一个系统

config 服务器:
如果mongos进程是非持久化的, 那么必须有地方能持久保存集群的公认状态, 这就是配置服务器的工作, 其中持久化了分片集群的元数据: 每个数据库,集合和特定范围数据的位置.

balanceer:
它是一个后台进程用来监视每个shard上含有chunks的数量, 如某台shard上所含的chunks超过阀值后, 会将其迁移到其他shard上, 保持一个平衡.

## Replication
MongoDB存储主从复制和副本集复制, 目前不推荐使用主从复制, 因为主节点若是down机后, 需要手动重新设计主节点.

副本集主要依赖于两个基础机制: `oplog` 和 心跳. oplog使得数据的心跳成为可能, 而心跳则监控健康情况并处罚故障转移.

对于选举:  
一般来说, 集群数量要为奇数位,当主节点异常的时候,能快速的选出新主节点. 可如果一些特殊场景, 仅能提供你一台主服务器,一台从服务器. 那么当主节点异常时,如何将从服务器变为主节点呢? MongoDB引入了 `Arbiter`

![three server](https://docs.mongodb.com/manual/_images/replica-set-primary-with-two-secondaries.bakedsvg.svg)  
![two server](https://docs.mongodb.com/manual/_images/replica-set-primary-with-secondary-and-arbiter.bakedsvg.svg)

Arbiter没有任何作用, 不存储数据,只负责投票.

对于复制:  
oplog往往会配合上`checkpoint 检查点`来保证数据的复制.(跟其他数据库备份差不多)

## 业务
对于mongo适应的场景,来看几个问题
- 应用不需要事务及复杂join支持
- 新应用,数据模型无法确定,想快速迭代开发
- 应用需要TB甚至PB级别存储
- 应用支持快速水平扩展
- 应用需要大量的地理位置查询,文本查询

满足其中2点,就可以考虑mongodb了.
