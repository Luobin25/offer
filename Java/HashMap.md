## HashMap 的深思

hashMap作为一个面试必考题, 真的是需要认真的品源码, 分析, 理解它的设计需求.

## HashMap初始化的时候做了什么
正常我们在用的时候, 都是直接`HashMap<Integer, Integer> map = new HashMap<>()`,有考虑这样的不好之处吗?

来看源码
```java
public HashMap() {
    this.loadFactor = 0.75F;
}
```
对于它来说, 只是简简单单设置了个 `loadFactor`, 负载因子. 其他什么都没做. 这体现了它的第一个思想: **HashMap是 lazy-load 模式, 即懒加载**

我们往里面添加一个数据看看, `map.put(1,1)`.  
在看源码前, 我们要先明白HashMap的结构, 它是由 **数组 + 链表**的组合方式, 数组, 我们可以理解为桶, 通过hash函数, 找到相应的桶位置(array[idx], 找到相应的idx), 然后以<key, value>的形式存放

所以对一个put函数来说, 我们需要提供三个值, `通过key计算好的hash值`, `key`, `value`
```java
    public V put(K key, V value) {
    //目前我们不去考虑后面两个变量, 用不到
    return this.putVal(hash(key), key, value, false, true); 
}

    // 我对程序没运行到的地方进行了删减
    final V putVal(int hash, K key, V value, boolean onlyIfAbsent, boolean evict) {
        HashMap.Node[] tab;
        int n;
        //table的格式为 transient HashMap.Node<K, V>[] table, 默认一开始为null
        if ((tab = this.table) == null || (n = tab.length) == 0) {
            n = (tab = this.resize()).length;   // 这里才开始初始化hashmap, 证实了是 懒加载模式
        }

        Object p;
        int i;
        // 总共大小为n, 初始化后n的长度为16, 可存放的idx为 (0~15)
        // 所以 通过 n - 1 & hash 找到相应的位置
        if ((p = tab[i = n - 1 & hash]) == null) {
            tab[i] = this.newNode(hash, key, value, (HashMap.Node)null);
        } 
        ++this.modCount;
        // 这里很关键, 记得在resize()中我们设置了阀值
        // hashmap扩容与否的判断条件在于 总元素 是否大于 阀值, 如果大于, 扩容.
        if (++this.size > this.threshold) {
            this.resize();
        }

        return null;
    }
    
    // 同样做了删减
    final HashMap.Node<K, V>[] resize() {
        HashMap.Node<K, V>[] oldTab = this.table;
        int oldCap = oldTab == null ? 0 : oldTab.length;        // table为null, oldCap = 0
        int oldThr = this.threshold;                            // 默认也为0
        int newThr = 0;
        int newCap;
        if (oldCap > 0) {
            ...
        } else if (oldThr > 0) {
            ...
        } else {// 条件判断后进入这里, 这里大家要清楚, 这就是大家熟知的初始化默认大小
            // 至于 newThr = 12, 还记得上文默认初始化的 LoadFactor = 0.75吗
            // 12 = 16 * 0.75, 也就是设置的阀值
            newCap = 16;                                        
            newThr = 12;
        }

        this.threshold = newThr;
        HashMap.Node<K, V>[] newTab = new HashMap.Node[newCap];
        this.table = newTab;
        if (oldTab != null) {
            ...
        }

        return newTab;  // 访问一个数组大小为16, 存放的对象是 Node
    }
    
    static class Node<K, V> implements Entry<K, V> {
        // 每一个Node存放的是我们提到的三元素, hash, key, value
        final int hash;
        final K key;
        V value;
        // 我们说过hashmap是数组+链表的格式, 链表体现在哪, 体现在这里. 每个**拥有相同hash值的节点**会根据
        // next指针连接起来
        HashMap.Node<K, V> next;

        Node(int hash, K key, V value, HashMap.Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }
    」
```

总结一下:
- Java1.8版本之后, 以`HashMap.Node<K,V>`作为存储节点. 
- HashMap是懒加载, 真正开始存放数据时, 才构建
- HashMap存放的核心是 三元素, Hash(key), key, value
- 每一个key的存放位置取决于它的hash, `hash(key) & (tab.length - 1)`

问题来了, 假设我明确知道我的map要放20个元素, 那如果我通过`HashMap map = new HashMap()`的方式, 意味着我要初识化table一次, 然后扩容一次. 既然我都知道了整体大小, 是不是一开始就设置好, 会比较好?  
除此之外, 每一次resize()也是需要耗费性能的

> 在业务中我们可以根据公式, `RealSize = Cap * Threshold`, 来估计初识Cap大小, 来避免多次resize()扩容.
> 而且HashMap是线程不安全的, 并发下多次resize()会导致死循环

### put相同hash位置的key,value
想要构建相同位置, 就先要了解它的hash函数构造

```java
    static final int hash(Object key) {
        int h;
        return key == null ? 0 : (h = key.hashCode()) ^ h >>> 16;
    }
    
    // 存放位置, 从上文截取
    if ((p = tab[i = n - 1 & hash]) == null) {
        tab[i] = this.newNode(hash, key, value, (HashMap.Node)null);
    } 
```
隐藏信息:  
key == null, 代表着我们可以向HashMap存储key为null的Object, 该对象会存在位置为0的地方



这里引入几个问题?
1. 为什么不直接采用key.hashcode() 作为idx值
因为任意元素的hashcode()值为int, int的长度为32. 如果直接 tab[key.hashcode()]会导致溢出, 因为tab.length没那么大. 引入了 `n - 1 & hash`
2. 为什么不直接采用key.hashcode()作为hash值
因为n的长度一般都不大, 而hashcode()返回的int长度为32,  当进行`n - 1 & hash`时, 很多高位都会被忽略掉, 总是使用低位来比较, 冲突的概率会增加. 所以 `hash ^ hash >>> 16`, 完美的将hash的高位和低位结合后, 在比较

> 无论hash()函数的内容是什么, 我们的出发原则都是**尽可能通过该函数后, 将输出值变得均匀, 分散, 充分利用空间**. 如果当你发现你在使用默认hash时, 很多key都往一个地方跑, 这时候你可以考虑稍微改造下hash()

我们知道Integer的hashcode, 默认为其value值. 所以 1 & 1 >>> 16 还是1, 再加上 16 - 1 & 1 = 1的结果, 我们可以找到idx也为1的intger, 17, 35.

```java
//截选putVal中的部分, 输入 map.put(17,17), map.put(35,35)
// p不空, p为前面创建的 1
if ((p = tab[i = n - 1 & hash]) == null) {
    tab[i] = this.newNode(hash, key, value, (HashMap.Node)null);
} else {
    Object e;
    Object k;
    // 这里是做替换, 
    if (((HashMap.Node)p).hash == hash && ((k = ((HashMap.Node)p).key) == key || key != null && key.equals(k))) {
        e = p;
    } else if (p instanceof HashMap.TreeNode) {     // 后面会提到
        e = ((HashMap.TreeNode)p).putTreeVal(this, tab, hash, key, value);
    } else {
        int binCount = 0;

        while(true) {
            // 创建 17的时候, p的next为空, 所以直接设置为 p.next = 17, 然后退出循环了
            // 第二次是获取 e为17 != null, 不符合
            if ((e = ((HashMap.Node)p).next) == null) {
                ((HashMap.Node)p).next = this.newNode(hash, key, value, (HashMap.Node)null);
                if (binCount >= 7) {                // 后面会提到
                    this.treeifyBin(tab, hash);
                }
                break;
            }
            
            // 这里也是做替换
            if (((HashMap.Node)e).hash == hash && ((k = ((HashMap.Node)e).key) == key || key != null && key.equals(k))) {
                break;
            }
            // 将e 17 赋给 p, 下次循环的时候,就会判断 p.next为null, 直接插入
            p = e;
            ++binCount;
        }
        
        // 替不替换的差别在于 查到的e是空的还是有值, 有值代表是我们想要在替换了
        if (e != null) {
            V oldValue = ((HashMap.Node)e).value;
            if (!onlyIfAbsent || oldValue == null) {
                ((HashMap.Node)e).value = value;
            }
            
            this.afterNodeAccess((HashMap.Node)e);
            return oldValue;
        }
    }
```

总结:
- Java1.8版本之后, 使用尾插法进行对象插入, 一直循环, 直到p.next 为null
- 如果中途遇到 e == 要插入的对象, 代表我们要做的是替换

改进:
```java
if (binCount >= 7) {                
    this.treeifyBin(tab, hash);
}

++binCount;
```
在1.8版本后, binCount代表的是记录该桶的链表长度是多少, 每迭代一次加1, 如果当链表的长度为8的时候(数组头 + 7个链表元素), 我们会将其转换成树的模式(红黑树)

树的好处在于, 无论是增删改, 它的时间复杂度都是O(logn), 而链表是O(n). 那为什么要等到8的时候,才进行转换呢? 如果一开始就用红黑树, 当是构建这棵树所消耗的性能远远大于链表, 所以大师们经过测试后, 设置8这个合理值.


## resize()的扩容
带着问题去看, 扩容一次扩多大, 数据是怎么被移动到新table上去的
```java
final HashMap.Node<K, V>[] resize() {
    HashMap.Node<K, V>[] oldTab = this.table;
    int oldCap = oldTab == null ? 0 : oldTab.length;        // oldCap = 16
    int oldThr = this.threshold;                            // oldThr = 12
    int newThr = 0;
    int newCap;
    if (oldCap > 0) {
        if (oldCap >= 1073741824) {                         // 数组的最大长度只能为2的30次方, 也就是这个值
            this.threshold = 2147483647;
            return oldTab;
        }
        
        // 这里就是每次扩容的关键, 对于每次扩容, 我们只需要左移一位就好, 即原来的2倍
        // 注意, 数组的长度永远都是2的n次方, (16为2的4次方, 扩容后为32, 2的5次方).阀值也乘2
        if ((newCap = oldCap << 1) < 1073741824 && oldCap >= 16) {
            newThr = oldThr << 1;
        }
    } else if (oldThr > 0) {
        newCap = oldThr;
    } else {
        newCap = 16;
        newThr = 12;
    }

    if (newThr == 0) {
        float ft = (float)newCap * this.loadFactor;
        newThr = newCap < 1073741824 && ft < 1.07374182E9F ? (int)ft : 2147483647;
    }

    this.threshold = newThr;
    // 创建新的table
    HashMap.Node<K, V>[] newTab = new HashMap.Node[newCap];
    this.table = newTab;
    if (oldTab != null) {
     // 这里可以看出来, 是一次性将所有值从oldTable更提到新table上
        for(int j = 0; j < oldCap; ++j) {
            HashMap.Node e;
            if ((e = oldTab[j]) != null) {
                oldTab[j] = null;
                // 单有数组头的, 直接赋过去就好
                if (e.next == null) {
                    newTab[e.hash & newCap - 1] = e;
                } else if (e instanceof HashMap.TreeNode) { // 树的移值
                    ((HashMap.TreeNode)e).split(this, newTab, j, oldCap);
                } else {    // 链表的移值
                    // 这里为什么有这么多变量, 其实就是将老的链表拆分成两个链表, lo 和 hi
                    // lo链表放在原来的idx位置, 比如1
                    // hi链表放在 idx + oldCap的位置, 1 + 16 = 17, 也是将key合理分布的一种手段
                    HashMap.Node<K, V> loHead = null;
                    HashMap.Node<K, V> loTail = null;
                    HashMap.Node<K, V> hiHead = null;
                    HashMap.Node hiTail = null;

                    HashMap.Node next;
                    do {
                        next = e.next;
                        // 拆分依据就是这个 e.hash & oldCap 为0放老的, 不为0放新的
                        if ((e.hash & oldCap) == 0) {
                            // 这里就是尾插法插入数据, 不停的插入到loTail.next, 然后将loTail = loTail.next
                            if (loTail == null) {
                                loHead = e;
                            } else {
                                loTail.next = e;
                            }

                            loTail = e;
                        } else {
                            if (hiTail == null) {
                                hiHead = e;
                            } else {
                                hiTail.next = e;
                            }

                            hiTail = e;
                        }

                        e = next;
                    } while(next != null);

                    if (loTail != null) {
                        loTail.next = null;
                        // 老位置
                        newTab[j] = loHead;
                    }

                    if (hiTail != null) {
                        hiTail.next = null;
                        // 新位置
                        newTab[j + oldCap] = hiHead;
                    }
                }
            }
        }
    }

    return newTab;
}
```

这里说明下, `(e.hash & oldCap) == 0`的拆分, 我们已知(1,1), (17,17), (33,33)都放在idx为1的地方,  这里加个49
```
16 的二进制为 10000
1  为         00001             => lo
17 为         10001             => hi
33 为        100001             => lo
49 为        110001             => hi

根据 oldCap的那个1来进行判断, 只要key含有该位置的1, 就放了hi中, 否则为lo
```


## 总结
HashMap最最重要的两个参数莫过于: `LoadFactor`, `Threshold`. 前者影响了后者, 后者影响了当数据量到达多少的时候要进行扩容, 而扩容影响了空间的使用率. 

> HashMap中默认的LoadFactor 为0.75L, 一般情况下, 我们不需要去更改它. 如果要去更改它, 你要明白当它往大的调, 其实是时间换空间的选择, 它越大, 它的阀值就越大, 那造成的冲突就会更多, 时间就会更久. 而因为阀值大了, 我们对数组的利用率就大了.

第二个重要的就是hash函数, hash函数的好坏体现了冲突的概率. 在实际业务中, 如果你清楚你的key构造, 你可以根据key的特点人为的设置hash函数, 来判断key的均匀分布

第三, 引入了红黑树, 在Java1.7中是没有的. 红黑树的引入, 使得当我们某个idx存在的值过多时, 不至于达到O(n)的遍历时间

HashMap中的`put`函数是精华中的精华, 因为hashMap是懒加载模式, 所以无论是初始化还是扩容, 都是在put函数内调用的, 你理解好put中的每一个具体操作, HashMap你也就基本上理解了.

## java1.7 resize()的问题
这个问题,也是面试的高频点. 在1.7之前, HashMap都是使用头插法. 但是1.7之后, HashMap使用了尾插法.无论是创建数据, 还是转移数据. 具体原因就不在分析了, 给几个参考连接

[race problem](https://mailinator.blogspot.com/2009/06/beautiful-race-condition.html)  
[HashMap 死循环](https://juejin.im/post/5a66a08d5188253dc3321da0)

主要原因, 就是resize()中, 头插法的机制, 导致在**多线程**下(注意,单线程不会有问题), 当两个线程都在进行扩容时, 会造成其中一个线程转移数据的过程中死循环, 即 `A.next 为 B, B.next 为A`

但Java的人也说, 这不是一个BUG, 默认情况下, HashMap就是**线程不安全的集合**, 如果你非要用在多线程, 它们也没办法

## HashTable
hashTable是Java提供的同步容器, 也就是线程安全的集合. 但整个class都是通过`synchronized`的关键字, 来保证线程安全. 

这些同步容器虽然保证了线程安全, 但从效率上来看, 由于引用了`sychronized`这种厚重锁, 效率变得很低下

## CoccurencyHashMap

















