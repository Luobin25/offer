# Redis 使用场景

## String
Redis的字符串是一种**动态字符串**, 允许修改(不像Java中的字符串). Redis是通过KV的方式来存储字符串的, 其实在redis中都是通过唯一K去访问V(V可以构造为任何数据结构)

常见的用途是作为缓存, 将某些值通过JSON序列化后, 存入refis中(例如用户信息, 或者是session)

其次就是计数器, Redis将自增操作优化成原子性的操作, 保证了在分布式的场景下数据的正确性. 

## List
这个玩法很多, 它类似于Java中的LinkedList, 链表.

它可以存储一些列表型的数据结构, 如文章列表, 粉丝评论列表等.  
对于数据量大的列表, 比如文章列表. 我们还可以通过`lrange`(读取某个闭区间元素)实现分页功能, 或者是微博下拉加载.

`lpop, lpush, rpop, rpush`的命令使得我们对列表的两头进行增删, 即可以模拟成栈, 也可以模拟为队列.  
作为队列, 常用的就是 消息队列了. 也可以说是生产者-消费者模型, 一边负责添加数据, 另一边负责消费数据, 同时, 当数据为空或者满时, 还能进行**阻塞**

## HashMap
类似于Java中的HashMap, 数组+链表的形式. 于HashMap的区别在于, rehash的时候(扩容或降容), HashMap会将老的map直接拷到新的map上, 但会造成O(n)的时间复杂度.  
在redis中, 会保留新和老的map, 查询时, 两个都会查询. 然后一点点的将old的数据迁移到新的上.

用途一般就是存储对象, 注意存储的对象最好避免镶嵌子对象.

因为单个hash的成本是比字符串高的, 有人问说 HashMap vs String 的选择: 参考[string VS hashmap](https://stackoverflow.com/questions/16375188/redis-strings-vs-redis-hashes-to-represent-json-efficiency)

## Set
相当于Java中的HashSet, 关于HashSet和HashMap的区别, HashSet是HashMap的特殊构造, HashSet里面每个key存的值都为NULL.

它是无序集合, 所以经常会用于去重. 但是如果当集合过大的时候, 我们可能会用 `bloomFilter, 布隆过滤器`.  
比如, 注册系统, 我们需要判断该用户名是否被注册. 

除此之外, 它有并集, 交集, 差集的功能. 可以用来做共同好友(并集)之类的

## Zset
有序列表, 它类似于 Java 的 SortedSet 和 HashMap 的结合体，一方面它是一个 set，保证了内部 value 的唯一性，另一方面它可以给每个 value 赋予一个 score，代表这个 value 的排序权 重。它的内部实现用的是一种叫着「跳跃列表」的数据结构

场景很多, 因为我们通过这个score可以玩出很多花样, 比如排行榜, 微博热搜榜. 再比如任务的权重列表, 通过给每个任务赋予相应的权重, 来设置任务优先级

## 分布式锁
`setnx` 就是 set 字符串时加了个条件`nx`(if not exist), 表示当该key不存在的时候, 才能创建. 所以当多线程同时访问时, 只有一个线程能创建该字符串, 用完后删除, 供其他线程重新创建

```
setnx lock:code true
expire lock:code 5
...
do soething
...
del lock:code
```
这里看似很完美, 但有一个问题, 就是setnx 和 expire之间不是原子性的, 如果中途crash的话, 会导致锁无法被释放.

redis在2.8版本后整合了两个操作, `set lock:code true ex 5 nx OK`

## 延时队列
延时队列的内部用的就是Zset, 我们可以将任务json话后存入到value中, score设置为时间戳, 通过zset的排序功能, 系统每一个刻从队列中抽取前N位进行处理

场景:
- 任务(比如获取分布式锁失败, 将其丢入到延时队列中)
- 关闭空闲连接。服务器中，有很多客户端的连接，空闲一段时间之后需要关闭之
- 订餐通知:下单成功后60s之后给用户发送短信通知。

## 位图和布隆过滤器
我们都知道一个byte等于8个字节, int为4字节, char为1字节等, bit的存放内容为1和0. 那我们可不可以为这个字节抽象为某种有意义的东西, 比如全班50个同学, 每一个同学分配一个bit(0~49bit), 谁今天来了,就在相应的bit上设置为1.再或者说一个员工的365天考勤表.

这就是位图, 我们就bit拟人化, 来节省空间. 比如员工的365天考勤表, 可以整成一个 boolean[365] days 的布尔值数组.

布隆过滤器运用的也是bit的概念, 但它的功能和位图是不一样的, 它用于大数据的去重, 筛选.

### 布隆过滤器
理解布隆过滤器, 首先你要明白hash算法. 即通过一个key, 在O(1)的时间内查找出相对应的value.

假设, 我们想设计一个文章推荐系统, 给用户推荐它没看过文章, 这里不考虑个性化推荐. 那做法可以是如下:
```java
HashSet<Article_id> user_id_set = new HashMap<>();

void check(Article_id aid){
    while(user_id_set.contains(aid)
        // 重新推荐
    return aid;
}
```
我们定义了个Set集合, 每一个用户都拥有一个自己历史文章的集合. 当我们推荐文章的时候, 判断集合中是否存在该文章, 不存在就推荐.

但是, 这里有两个隐患: 1. 文章数越来越大 2. 用户数越来越多. 这两个隐患都会导致空间的大量消耗.  
所以, 我们先用位图的思想, 把Set替换成位图来节省空间, 接着在运用hash的思想, 通过hash(aid) -> bit, 找到相应的bit来判断该值为0或1

这里又存在着一个隐患: 如果当靠一个bit来判断文章的重复, 误判率会很大. 所以我们引入了多个hashmap. 通过多个无偏的hashmap(能把key算得比较均匀).

> 对于布隆过滤器来说, 如果一个key存在, 那么它一定存在. 如果这个key不存在, 那么它可能存在. 这里会引入一个误判率, 误判率的大小取决于两点: 位图的size 和 hash函数的数量. 幸运的是, 很多布隆器可以根据设置的误判率和初始大小, 来自动分配 size 和 hash函数

对于推荐系统, 我们肯定不希望推荐用户已经看过的东西(一个key存在, 那么它一定存在). 对于没看过的, 如果误判为已经看过,影响也不大(一个key不存在, 那么它可能存在), 类似的还有注册系统

除此之外, 布隆过滤器的引用很广, 比如垃圾邮件过滤器, Nosql对Row的过滤, 爬虫系统对已经爬去的URL过滤

## HyperLohgLog
这个东西, 大概知道它的用法就好了.
> HyperLogLog is an algorithm for the count-distinct problem, approximating the number of distinct elements in a multiset.[1] Calculating the exact cardinality of a multiset requires an amount of memory proportional to the cardinality, which is impractical for very large data sets. Probabilistic cardinality estimators, such as the HyperLogLog algorithm, use significantly less memory than this, at the cost of obtaining only an approximation of the cardinality. 

假设你要做个UV的统计, 统计每年, 月,日用户对相应网站的点击率. 那么你需要设置一个Set(因为集合有去重的功能, 对于一个用户的多次点击, 我们只计算为1), 问题就像上面的问题, 随着网站越来越多, 用户越来越多, 这个Sets就越来越多.

HyperLogLog的创建就是为了这个, 它只能计算大约值, 会对实际值有偏差. 但本身统计UV这个东西, 要的也不是精确值, 我们只需要它的底数就好了(千, 万, 百万).

在redis中, 一个HyperLogLog只需要占用**12KB**的大小. 同时支持多个HyperLogLog的合并(会自动去重).

## 漏斗限流(funnel)
## 地理位置(geoHash)
## 游标分布扫描(scan)
scan会存在重复的key, 原因是rehash的缩容,造成的重复遍历.

这三个, 就不再详细说明. 推荐[Redis 深度历险](https://book.douban.com/subject/30386804/), 里面都讲的很详细











