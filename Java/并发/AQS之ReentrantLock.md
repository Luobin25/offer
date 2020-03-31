# AQS 之 ReentrantLock

AQS是 JUC包的基础, 基本上都是围绕着AQS来沿伸的. 然而, 如果直接看AQS的代码, 会有很多需要重写函数, 不利于理解. 所以我们选择了个AQS中最简单的 ReentrantLock来分析

注意, 这里不是所有都分析完. 为了保持简单性, 我们先分析它的几个特性.
- AQS是如何进行互斥的
- 是如何引入公平原则的
- 不考虑condition, 即wait()和notify()
- 不考虑中断异常情况

下面用的java版本是 1.8, 我们将通过两个线程来演示

## AQS 初识化参数
```java
// 做了些精简
public abstract class AbstractQueuedSynchronizer extends AbstractOwnableSynchronizer{
    // 核心1: CLH队列
    volatile AbstractQueuedSynchronizer.Node head;
    volatile AbstractQueuedSynchronizer.Node tail;
    
    // 核心2: 表示锁的状态
    volatile int state;
    
    // 核心3: 代表当前持有锁的线程, 来自extends AbstractOwnableSynchronize内的变量
    protected final void setExclusiveOwnerThread(Thread thread) {
        this.exclusiveOwnerThread = thread;
    }
    
    // 核心4, Node节点
    static final class Node {
        // Node节点类型, 有两种, 共享锁 和 互斥锁. 
        static final Node SHARED = new Node();
        static final Node EXCLUSIVE = null;
        
        // 代表当前线程取消了
        static final int CANCELLED = 1;
        // 代表当前线程需要唤醒其后继节点
        static final int SIGNAL = -1;
        // 本篇先忽略
        static final int CONDITION = -2;
        static final int PROPAGATE = -3;
        
        volatile int waitStatus;
        
        // Node是双向的, 即拥有prev 和 next指针
        volatile AbstractQueuedSynchronizer.Node prev;
        volatile AbstractQueuedSynchronizer.Node next;
        
        // 把线程加载到节点, 用节点来作为线程之间的运转
        volatile Thread thread;
    }
```
解析:
1. CLH队列
![CLH Queue](https://github.com/Luobin25/offer/blob/backup/Java/pic/CLHQueue.png)
对于互斥锁来说, 任意时刻只能由一个线程持有该锁, 那拿不到锁的线程只能阻塞, 我们通过构建一个双向队列(注意Node的prev和next指针), 把所有阻塞的线程插入到队尾中.
注意, 这里是个逻辑队列, 该队列只有2个变量, head 和 tail. head我们通常不把它当作为阻塞节点, 它一般指的是占有锁的线程, 它的next节点才是阻塞队列的起点(请认真品味, 后面很重要)

2. 锁的状态
在AQS里, 我们通过定一个整形变量, 来表示锁的状态. 因为任意int的操作, 我们都可以使用原子性指令, 会大幅低提高性能. 所以, ReentrantLock 中state分两种情况:
    - state = 0, 表示当前锁可抢夺
    - state > 0, 表示被占用, 由于是可重入的, 所以state 可以为1, 2, 3...(每次获取, 都state++)

3. Node节点
如果队列存入的是线程, 不方便我们自定义和管理化. 所以引入了Node节点, 将thread包装在那里, 进行管理. 比如, 对Node设置 prev 和 next 指针, 加入阻塞队列. 监控 waitStatus 状态, 来进行相应的指令.

## ReentranLock 的使用方式
```java
Lock lock = new ReentrantLock(true);          // true代表使用公平锁

lock.lock();
try{
    // do something...
}finally{
    lock.unlock();
}
```
## 加锁的过程
``` java
public void lock() {
    this.sync.acquire(1);
}

// ReentantLock 实际是通过 Sync 来加锁和解锁. 而 Sync来源于 AQS, 注意它是抽象类
public ReentrantLock(boolean fair) {    // 构造函数
    this.sync = (ReentrantLock.Sync)(fair ? new ReentrantLock.FairSync() : new ReentrantLock.NonfairSync());
}

abstract static class Sync extends AbstractQueuedSynchronizer {}
// Java 在 Sync 的基础上, 延伸出两种类型: 公平锁 和 非公平锁
static final class FairSync extends ReentrantLock.Sync {}
static final class NonfairSync extends ReentrantLock.Sync {}

// 方法实现自 AQS 中
public final void acquire(int arg) {
    // 这里通过两种途径获取锁:
    // 1. tryAcquire(). 正如`try`的含义,  尝试获取.本来就没人持有锁，根本没必要进队列等待(又是挂起，又是等待被唤醒的)
    // 2. 有人在使用, 那我们就老老实实排队(公平的原则), 
    // 2.1 先用addWaiter()函数把该线程加入到tail(队尾)
    // 2.2 加完线程后, 通过acquireQueued把它挂起等待
  if (!this.tryAcquire(arg) && this.acquireQueued(this.addWaiter(AbstractQueuedSynchronizer.Node.EXCLUSIVE), arg)) {
      // 这里正常下, 不会进行中断检测,.
      selfInterrupt();
  }
}

// 方式来自 FariSync(公平锁)中
static final class FairSync extends ReentrantLock.Sync {

    FairSync() {}

    protected final boolean tryAcquire(int acquires) {
        Thread current = Thread.currentThread();
        // 记得核心2说的锁状态吗, 这里获取锁状态. 如果为0, 代表没人用
        int c = this.getState();
        if (c == 0) {
            // hsaQueuedProcessors() 当每个线程进来try的时候, 先要判定队列是否有人在等待, 如果有, 我们为了保持公平原则, 就不能直接抢占.
            if (!this.hasQueuedPredecessors() && 
                // 这里会通过CAS进行修改, 如果不成功意味着什么?
                // 意味着几乎同一时刻, 另外一个线程获取到了锁, 修改好了值
                this.compareAndSetState(0, acquires)) {
                
                // 设置锁的主人, 告知大家, 我占用了锁
                this.setExclusiveOwnerThread(current);
                return true;
            }
            // 这里载入 非公平的 tryAcquire, 做比较分析. 唯一不同就是在有没有 hsaQueuedProcessors.
            // if (this.compareAndSetState(0, acquires)) {
            //       this.setExclusiveOwnerThread(current);
            //       return true;
            // }
            
        // 可重入就体现在这里, 当持有锁的线程再次调用 lock.lock时, 我们会通过核心三的变量exclusiveOwnerThread来判断是否为同一个线程.
        // 若相等, 就给 state + 1
        // 注意, 你重入多少次, 就要相应的 unlock 多少次, 则到state为0
        } else if (current == this.getExclusiveOwnerThread()) {
            int nextc = c + acquires;
            if (nextc < 0) {
                throw new Error("Maximum lock count exceeded");
            }

            this.setState(nextc);
            return true;
        }

        return false;
    }
}
    // tryAquire()返回 true 和 false. 当我能尝试获取到锁的时候, 会放回true, 否则都是 flase.
    
// 回到主线, !false = true, 紧接着调用addWaiter. 因为是互斥模式, 传入互斥 mode. 本文用不上
// if (!this.tryAcquire(arg) && this.acquireQueued(this.addWaiter(AbstractQueuedSynchronizer.Node.EXCLUSIVE), arg))

// 此方法的作用是把线程包装成node，同时进入到队列中
// 参数mode此时是Node.EXCLUSIVE，代表独占模式
private Node addWaiter(Node mode) {
    Node node = new Node(mode);

    AbstractQueuedSynchronizer.Node oldTail;
    
    // 如果你熟悉 CAS模版的话, 这里你就会很清晰.
    // 就是每次都取tail(因为防止并发的时候, tail被需改). 设置 node.prev = tail. 尝试用CAS写入, 不成功继续
    do {
        while(true) {
            oldTail = this.tail;
            if (oldTail != null) {
                node.setPrevRelaxed(oldTail);
                break;
            }
            
            // 队列为空的, 构建一个
            // private final void initializeSyncQueue() {
            //        Node h;
            //    if (HEAD.compareAndSet(this, (Void)null, h = new Node())) {
            //        this.tail = h;
            //    }
            // }
            this.initializeSyncQueue();
        }
    } while(!this.compareAndSetTail(oldTail, node));
    
    // 写入成功后, 在把oldTail.next指向node
    oldTail.next = node;
    return node;
}

//又回到主线, 并返回该node. 记住, addWaiter仅仅把当前线程封装成Node后加入到阻塞队列中(通过死循环,保证一定加入成功)

final boolean acquireQueued(Node node, int arg) {
    boolean interrupted = false;

    try {
        // 每个node都会在一个死循环中, 除非轮到你了, 否则会一直呆在这里面
        while(true) {
            // 如果p为head, 回想核心1我们提到的. 说明此时, 我们是阻塞队列的第一个,  那我们可以尝试的再去抢夺一下锁
            // 为什么说可以尝试, 原因1: 它是第一个
            // 原因2: 还记得当队列为null的时候, 我们会构建一个队列吗, new 一个 node 作为head.
            // 代码为: Node() {}, 它什么都没设置, 相当于一个哨兵.只是为了符合我们的编写逻辑.
            // 所以对于哨兵, 它不属于任何线程, 作为队列可以去试一试.
            Node p = node.predecessor();
            if (p == this.head && this.tryAcquire(arg)) {
                // 获取锁之后, 把自己设置为哨兵(头节点), node.thread = null, node.prev = null
                this.setHead(node);
                p.next = null;
                return interrupted; 
            }
            // 要么p不是队头, 要么抢不了或者没抢到, 都会进入到这里. 抢到的直接就可以return了.
            if (shouldParkAfterFailedAcquire(p, node)) {
                interrupted |= this.parkAndCheckInterrupt();
            }
        }
    } catch (Throwable var5) {
        this.cancelAcquire(node);
        if (interrupted) {
            selfInterrupt();
        }

        throw var5;
    }
}

// pred 为其前继节点, node为自身. 该函数用来判断, 当该线程没有抢到锁时, 需不需要进行阻塞
private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
    // 这里就用上了核心四Node节点, 那些定义好的状态
    int ws = pred.waitStatus;
    // -1 代表当前线程(pred)需要唤醒其后继节点, 所以如果前继的状态为-1, 我们可以安心被阻塞了, 前继执行完之后会通知我们
    if (ws == -1) {
        return true;
    } else {
        // 大于0的情况, 目前只有1种, 为1. 代表该节点已经取消了, 我们肯定不能用已经取消的前继节点来唤醒我们,
        // 所以我们不停往前找, 找到 waitStaus <= 0 的情况
        if (ws > 0) {
            do {
                node.prev = pred = pred.prev;
            } while(pred.waitStatus > 0);

            pred.next = node;
        } else {
            // ws 小于等于0的情况有 0, -1, -2, -3. 不考虑-2, -3的情况, 如果是-1前面就已经返回了.
            // 所以就只有0的情况, 0在什么时候出现呢, 当我们无论创建是head节点, 还是自身的时候, 我们并没有设置该值,所以默认下均为0
            // 所以每次当新的一个tail入队后, 会把上一个tail设置为-1, 让它来通知我. 而自身不设置为-1的原因,
            // 我觉得是因为我是最后一个, 从我的视角, 我不需要去唤醒我的后继. 我只需要更改前继就好
            pred.compareAndSetWaitStatus(ws, -1);
        }
        
        // 如果你仔细观察 acquireQueued, 会发现它是死循环的, 所以第一次更改前继为-1, 返回false
        // 第二次进来, 就返回true了
        return false;
    }
}

// 一个线程正常情况下, 会进入两次, 第一次修改返回false, 第二次返回true.
// 当为true之后, 调用 parkAndCheckInterrupt() 进行阻塞
if (shouldParkAfterFailedAcquire(p, node)) {
    // 使用的是 LockSupport.park(this); 来阻塞, 线程就会停到这里, 直到被人唤醒
    // 被人唤醒后, 重复之前的操作, 判断前继是否为head, 是的话tryAcquire()
    interrupted |= this.parkAndCheckInterrupt();
}

// 这里用了LockSupport.park(this)来挂起线程，然后就停在这里了，等待被唤醒=======
private final boolean parkAndCheckInterrupt() {
    LockSupport.park(this);
    return Thread.interrupted();
}

// 这里有个细节, 为什么我们设置完-1, 后不能直接调用parkAndCheckInterrupt() 进行阻塞呢
// 应对在经过这个方法后，node已经是head的直接后继节点了.(注意, 阻塞和唤醒永远都是费时操作, 能白嫖就多白嫖)
```

## 解锁操作
```java
public void unlock() {
    // 注意这个1,后面会提到
    this.sync.release(1);
}

public final boolean release(int arg) {
    if (this.tryRelease(arg)) {
        AbstractQueuedSynchronizer.Node h = this.head;
        // 当我为0时, 代表我没有后继节点需要去唤醒
        if (h != null && h.waitStatus != 0) {
            this.unparkSuccessor(h);
        }

        return true;
    } else {
        return false;
    }
}

protected final boolean tryRelease(int releases) {
    // 记得我说的那个1吗, 释放的时候会对state进行减一操作, 如果你已经可重入N次, 那么你需要连续释放N次才行
    int c = this.getState() - releases;
    if (Thread.currentThread() != this.getExclusiveOwnerThread()) {
        throw new IllegalMonitorStateException();
    } else {
        boolean free = false;
        // 为0的时候, 才真正释放, 释放就是把 锁的拥有者设置为null
        // 但本文中这个变量只用于可重入的性质, 对于锁的争夺, 全靠 state 这个变量
        if (c == 0) {
            free = true;
            this.setExclusiveOwnerThread((Thread)null);
        }

        this.setState(c);
        return free;
    }
}

private void unparkSuccessor(Node node) {
    int ws = node.waitStatus;
    if (ws < 0) {
        node.compareAndSetWaitStatus(ws, 0);
    }

    AbstractQueuedSynchronizer.Node s = node.next;
    if (s == null || s.waitStatus > 0) {
        s = null;
        // 如果后继的Node状态大于0(1), 代表后继取消了, 从tail往前开始找
        // 这里有个细节, 为什么要从后往前, 而不是直接从前往后
        // 在 addWairer()函数中, 死循环里有一段
        // oldTail = this.tail;
        //    if (oldTail != null) {
        //        node.setPrevRelaxed(oldTail);
        //        break;
        //    }
        // 这里, 我么只设置了 node.prev = oldTail, 最后退出循环在设置的, oldTail.next = node
        // 所以, 从前往后可能会造成断环
        for(AbstractQueuedSynchronizer.Node p = this.tail; p != node && p != null; p = p.prev) {
            if (p.waitStatus <= 0) {
                s = p;
            }
        }
    }

    if (s != null) {
        // 唤醒线程, 唤醒的线程, 从
        // private final boolean parkAndCheckInterrupt() {
        //      LockSupport.park(this);
        //      return Thread.interrupted();        从这里继续
        // }
        LockSupport.unpark(s.thread);
    }
}
```

# 总结
我参考了大神的这篇[一行一行源码分析清楚AbstractQueuedSynchronizer
](https://javadoop.com/post/AbstractQueuedSynchronizer), 加上自己的一些分析. 所以, 如果你还不是很清楚的, 请参考他的文章
