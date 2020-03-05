# HTTP
超文本传输协议(Hypertext Transfer protocol)是应用层协议.  
HTTP是一种请求/响应式的协议,即一个客户端与服务器建立连接后,向服务器发送一个请求.服务器接受到请求之后,给予相应的响应信息.

## HTTP 无状态
在最近的温习中, 有一个问题靠扰着我.就是 ***HTTP是基于TCP的,而TCP是面向连接的,为什么HTTP还是无状态的?***

为了搞清楚这个, 先来看官方定义
> HTTP is a stateless protocol. A stateless protocol does not require the server the retain information or status about each user for the duration of multiple requests

人话版: HTTP对于事务处理是没有记忆能力,在服务端中是没有“老客户”这一个概念的, 对于每一次请求,对它来讲都是“新客户”,因为每次请求并没有携带任何有关‘记忆’的信息

对于早期的互联网, 来看HTTP 0.9版本
```
client:
  GET /index.html
  
server:
<html>
  ...
</html>
```

传输的东西没有现在那么多,也没有现在那么杂.所以设定为无状态,能极大的简化HTTP模型的难度.

可是现在的互联网,随便访问一个网页,都会夹带着许多静态文件(图片,视频等).如果按照HTTP 0.9版本,那每一次建立完TCP连接,只用于传输一个请求(图片1).然后在建立一个TCP连接,再传输一个请求(图片2),会导致带宽和时间的双双浪费(复用连接)

再者,比如你访问很多网站时,都需要你登陆账号密码.你在首页输入网站后,不可能当一刷新或者一点击下一个页面,还要再输入账号密码.这就引申出了很多有状态的机制(coockies,session)

总结:
**对于HTTP1和HTTP之前的版本,我们都称呼它为无状态版本.虽然我们后面开发出了很多有状态的机制(coockies,session).但它们只能说是附加在HTTP上面的,本质上HTTP还是无状态.到了HTTP2后推出了很多自带的状态机制,我们也称呼HTTP2为有状态**

## coockies 和 seesion
> An HTTP cookie(web cookie, browser cookie) is a small piece of data that a server sends to the user's web browser

它的存在使得HTTP可以当作是有状态的.因为服务端会通过coockie向浏览器发送一些信息.浏览器收到了可以保持下来,下次再发送的时候可以附带该信息

它有什么用呢?   
比如说你访问豆瓣,输入账号密码后.如何保证你接下来不需要在输入信息? 服务端可以简单在回复请求设置`coockie: loginStatus  = yes`.  
可这样也不行啊, 如何确保是你呢? 服务端改进了一下,改成`coockie: suid = 01`.这样你在接下来访问的时候,每次都给服务端发送这个coockie,它就不需要你在重新输入信息了

coockie简单来说,就是一段你想存在浏览器端的数据(为了方便你之后的利用).但正是因为它存储在浏览器端, 就会有hayck攻击的风险.而且对于我们举的例子, 如果我知道了你的coockie格式. 那么我就可以进行仿造,获取你的信息.

> 对于概念上的session, 它是抽象的,开发者为了实现中断和继续等操作,将user agent和server之间一对一的交互,抽象为“会话”,进而衍生出“会话状态”,也就是session的概念

所以session实际上它不属于HTTP协议, 它只是为了绕开coockie的各种限制和风险,通过借助coockie本身和后端存储实现的,一种更高级的会话状态实现.

session是存储在服务器端的,它解决了安全隐患.它也符合了一种理念: **用户不应该知道,甚至控制两端交互所需要的信息,所有的信息都放在服务端,一是保证了安全性,二也是保证了控制性,当存在泄露风险时,服务端只需要清空这些session就足以**

### 模拟一次豆瓣登陆
1. 浏览器打开豆瓣,输入账号密码点击确认.request中会包含用户的信息
2. 如果账号核实,服务端初始化一个session,存在用户的信息.并返回sessionID给浏览器(可理解为sessionID是一串随机的乱码)
3. response中设置 coockie sessionID = `xxxxxxx`
4. 浏览器存下来, 当访问豆瓣其他网站时,附带该coockie
5. 服务端对coockie中的sessionID进行匹配,找到该用户,进行相应的操作

注意: 为什么有时候过几天或者过一会,甚至重新打开浏览器,我们需要再重新登陆一次? 因为每个网站处理session的处理都不一致,或者说取决于设置的**session过期时间**

## HTTP的method
比较常用的有: `GET`, `HEAD`, `POST`, `PUT`, `DELETE`  
除了`HEAD`, 就是我们常说的增删改查

在了解其中区别前,我们先提到一个次 **幂等性**
> 幂等性: 连续调用一次或者多次的效果相同(无副作用)

除了POST以外,其他都是幂等性的

- GET: 通用用于请求服务器发送某些资源(查)
- HEAD: 请求资源的头部信息,并与HTTP GET方法请求时返回的一致
- DELETE: 删除指定的资源(删)

对于增和改来说呢? POST和PUT均可以实现,但是两者的场景不同

### POST vs PUT(均适用于增和改)
假设我们想上传一篇,请求体中含有该文章的所有信息.那么我们就应该使用`PUT`
```
PUT /benny/firstArticile HTTP 1.1
...

<full content of article>
```

因为PUT均有幂等性, 无论我们重复上传该文章多少次,返回的结果都是一样的.而且不会影响该文章

但是POST就不一样了, 它是非幂等性的. 所以重复上传,会造成返回的结果是不一致的.**它是面向资源集合的**

当我们在开发一个博客系统，当我们要创建一篇文章的时候往往用POST benny/articles，这个请求的语义是，在articles的资源集合下创建一篇新的文章，如果我们多次提交这个请求会创建多个文章，这是非幂等的。

相比PUT,我们的目标是创建一篇或多篇文章. 我们不是创建某一具体文章(比如上文的firstArticile).我们的POST请求体传递的是一些参数和内容,服务器通过相应API利用这些参数和内容生成一篇文章,并返回

## HTTP的报文

![http request](https://user-gold-cdn.xitu.io/2019/6/14/16b545c9bac2897b?imageslim)

![http responser](https://miro.medium.com/max/2900/1*5QCrgA5LoA8AKR30ce6x5A.png)

```
比如更新我主页的一篇文章(举例)
请求:
PUT /benny/myArticile HTTP 1.1 
User-Agent: Google-Chrome
Coockie: uuid=benny
Host: 10.10.10.10:80

articleName=myFirstArticle

响应:
HTTP/1.1 200 OK
Content-Length: 520
Content-Type: text/html

<html>
...
</html>
```

部首参数这里就不提及了,自行google

### 状态码
- 2xx 表成功
  - 200 OK 请求被正确处理
  - 201 Created, 请求已被实现,而且有一个新的资源已经依据请求的需要而建立
  - 204 No content, 表示请求成功,但响应报文不含实体的主体部分
- 3xx 重定向
  - 302 found 临时性重定向,表示资源临时被分配了新的URL
- 4xx 客户端错误
  - 404 Not found, 在服务器上没有找到请求的资源
  - 407 Proxy Authentication Required 代理服务器认证
- 5xx 服务端错误
