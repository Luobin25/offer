# TCP

本篇围绕着TCP的特性来讲解,前篇文章我们讲述了UDP和TCP的一些原理,如果你还不清楚,请参考[TCPorUDP](https://github.com/Luobin25/offer/blob/backup/%E7%BD%91%E7%BB%9C/TCPorUDP.md)

![tcp header](http://telescript.denayer.wenk.be/~hcr/cn/idoceo/images/tcp_header.gif)

## TCP的连接
一说到TCP就会提到一个很经典的问题 **为什么TCP握手是三次?, 为什么断开是四次?**  
在解答这个问题前,我们再来聊一聊另一个问题

### Sequence Number 和 ACK
我们提到序列号是用来标识数据包的,那这就涉及到两个问题

#### 序列号和ACK的递增模式 
**SeqNum的增加是和传输的字节数相关的**  
举例,这里我们假设初始值为0,且是客户端单向传输
```
对于 stop-wait 协议, 发送两次数据包一次为10,一次为20
client:               Server
seq为0, len为10         
                     ack为11
seq为11, len为20  
                     ack为31
```
如果只看左半边, 第一次的seq为0, 发送的数据长度为10. 所以下次发送的数据起始位置为11  
那对于ack的回复来说,第一次它代表着我已经收到了你序列号为0,长度为10的数据.我现在`期待`着你序列号为11的数据.   
第二次同理,我`期待`着你序列号为31的数据(因为10+20=30)

#### 序列号的初始化
假设我们从0开始传输数据. 每次数据长度为5字节,传了5次后(共25字节).网络断了(不是连接断了),client重连后, 又从0开始发送. 那么server就蒙了,它给client回复,我需要的是序列号为26的字节, 这就会造成混乱.  
同时通过采用随机序列号,也是为了防止攻击

序列号的在包头中的长度为4bytes, 为2^32. 当超过这个数后,又从0开始接着

### TCP的三次握手
先附上一个大牛的回答, [three handshake](https://networkengineering.stackexchange.com/questions/24068/why-do-we-need-a-3-way-handshake-why-not-just-2-way)

前面我们提到了一件很重要的事,就是序列号需要初始化的.   
举例, Alice想和Bob建立通信, 那么两个首先要做的事是初始化序列号,然后通知对方
```
Alice SYNchronize with my Initial Sequence Number of X
Bob   I received your syn, I ACKnowledge that I am ready for [X+1]
Bob   SYNchronize with my Initial Sequence Number of Y
Alice I received your syn, I ACKnowledge that I am ready for [Y+1]
```
这是一个正常的流程, 两端建立起连接(设置好序列号),然后开始发送数据

再来看一个图, ![SYN](https://www.cspsprotocol.com/wp-content/uploads/2019/11/tcp-sequence-number-capture.jpg)  
我们看到开头有个很吸引我们眼球的单词**SYN**,无论是包头还是上面的举例都有显示这个东西.它的全称为`Synchronize Sequence Number`, 代表着同步序列号.什么意义呢? 它用来告知对端这个数据包是用来建立连接用的, 不含有真实数据

SYN在包头中仅仅占一个bit,所以我们完全可以将第二步和第三步合并, 也就形成了我们常说的 **TCP**三次握手


### TCP的四次断开
![TCP four close](http://www.tcpipguide.com/free/diagrams/tcpclosesimul.png)  
先认识以下FIN标签: `FIN -> The sender is finished sending data.`

也是从单方面来看, client收到上层应用的中断指令
- 发送带有FIN标签的数据包给server
- server回复ack

所以从双方面角度来看就是四次断开了. 其中主动断开的称为主动方, 被动断开的为被动方.

为什么这里的四次不能整合为3次?
你认真读一下FIN标签, 它标示**我不向你发数据了,但我还可以接受接受你未发完的数据**.

就好比如小明和小黄各欠对方钱, 然后小明把钱还完后,发了一个FIN标签.这时候小黄还没把钱还完, 那他肯定不能直接回复一个“ACK+FIN”.它只会发送ACK,代表确认小明还完钱了.然后继续还钱给小明,直到钱还完,再发送FIN

如图,为什么主动方要等待2个MSL(Max Segment Lifetime)时间后才CLOSE掉?
1. 这也是出于防止被动方没有收到主动方发出的ACK.需要有足够的时间让对方确保收到ACK,或者重新再发一次FIN
2. 为使旧的数据包过期,不至于对新连接产生影响

## 滑动窗口(flow control,流量控制)
滑动窗口本质上就是一个缓存区,有了这个东西以后, TCP的传输才不再是 *wait-stop* 的了,它可以在不收到 ack 的情况下,继续发送它的数据.同时也可以动态调整发送数据的大小

既然有了缓存,我们就要考虑到两种情况: 发的比收的快 以及 收的比发的快. 所以导致了 两端均需要缓存区. 同时我们又在包头中添加了一个参数:`window`, **它会告知对方,它自己目前还有多少缓冲区可以接受数据**

![tcp windows](https://coolshell.cn/wp-content/uploads/2014/05/sliding_window-900x358.jpg)

正是因为有了缓存区, 比如client同时发送了1,2,3 3个数据包,如果2中途丢失了. 对于server端来说,它不会把3也丢弃,它会把3先存入.然后回复client, 它期望收到的下一个数据包为2(ack)

来看图右边, `NextByteExpected`表示期望收到的数据,`LastByteRcvd`表示收到的包的最后一个位置  
对于图左边,`LastByteAcked`表示收到已经确认的数据包位置, `LastByteSent`表示发出去的

那为什么称自为滑动窗口呢? 因为无论是哪端, 它的变量值是一直在改变的. 而且改变的方向一直都是向右的(递增),就像是一个滑动窗口一样.

对于当某一端窗口占满时, TCP使用了 `Zero Window Probe` 技术   
当windwos变成0, 你觉得两端还需要发数据吗? 如果接受端不发送数据了, 那发送端如何告知你呢?(有人会想为什么不让发送端主动告知, 因为接受端也不知道你后面还要不要发啊). 所以TCP使用了`Zero Window Probe`技术, 就是每隔多久, 发送端就发送一个ZWP包给接收方, 让接收方来ack一个它的window尺寸. 同时设定一个阀值,超过几次后, 还是0的话,有的TCP实现就会发RST把链接断了

### 糊涂窗口综合症
我们已知一个TCP的包头为40字节, 考虑两个场景:
1. 你使用telnet连接一台服务器,然后发送了个字符‘s'(1字节)
2. 你去访问新浪网页, 该首页大小1380字节

那么对于两者包头的占比为 40/41, 40/1420. 我们为了传输1个字节,结果用了40个字节的包头,真的是豪无人性啊  
为了杜绝这种现象, TCP引入了一个算法: `Nagle`

再说该算法前, 我们先要知道网络上到底**最大的传输单元是多少**?
> MTU(Maximum Transmission Unit), 在以太网中默认为1500字节.  
> MSS(Max Segment Size), 除去TCP和IP头部的40字节, 那么 MSS = MTU - 40 = 1460(最多能传1460个字节,结果你就传了1个...)

它的算法对于每次传输数据有两个要求:
1. 如果传输的数据大于MSS, 也大于 Windows Size, 发出
2. 如果前面发送的ACK均已收到.则立即发出,无论数据大小.(这种就是我有钱(带宽好)我任性(随便传)

## 拥塞算法
这是TCP中最精华的一部分了,为了不去玷污它.请参考陈皓哥的[TCP那些事儿(下)](https://coolshell.cn/articles/11609.html) 
它分成了4个部分
- 慢启动
- 拥塞避免
- 拥塞发生
- 快速恢复(用于duplicate ACK的重传)

从滑动窗口中我们知道了rwnd(receive window), 但一味的根据rwnd是不科学的, 因为我们还需要去考虑网络的通畅情况.这就引入了 `cwnd(Congestion window,拥塞窗口)`. 根据两者的最小值,来决定发送数据包的大小

而拥塞算法中的这4部分都是围绕着 `cwnd` 去做调整的.

## 总结
1. 网络中最常见的几种情况?
丢包,乱序和重传. 而TCP协议就是用来解决这些问题的, 所以TCP也称为可靠的协议

2. TCP的可靠体现在哪?
- 对于数据保证 无差错(完整性),不丢失(可靠性),不重复(一致性),按序到达
- 通过流量控制(Flow Control) 和 拥塞控制(Congestion Control)来保证网络环境,在网络延迟高时,避免进一步加剧网络的拥堵(不像UDP)
- 数据分段 TCP会自动整合应用层传输下来的数据, 数据过大会切割,数据过小会聚拢.

3. TCP的可靠性用了哪些机制
- 对于数据按序到达, 使用了**序列号和ACK机制**
- 数据不重复, 接受端会自动丢弃已经接受过的数据
- 对于数据无差错, 头部添加了**checksum(检验码)**
- 对于数据不丢失, **启动定时器(Timer),超时重传**. 同时又加入了快速重传的机制(通过duplicate ack特性),加快响应.
- 滑动窗口
- 拥塞控制

其他:
- TCP全双工的, 即两边均可以发送数据和接受数据
- TCP是面向流字节的, “流”代表着: TCP不去care应用层传输的是什么数据, 而是仅仅看成一连串的无结构的**字节流**

参考:   
[TCP奶糕（Nagle）](https://zhuanlan.zhihu.com/p/38148765)  
[TCP那些事儿(下)](https://coolshell.cn/articles/11609.html)  
[理解TCP/IP传输层拥塞控制算法](https://cloud.tencent.com/developer/article/1590918)
