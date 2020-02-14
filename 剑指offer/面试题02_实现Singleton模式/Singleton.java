import java.util.concurrent.Executor;

// 考察知识点：
// 1。 单例模式的运用
// 2。 对多线程的理解
// 3。 对静态函数等理解

public class Singleton{
    public static void main(String[] args) {}
}

class Singleton1 {
    private Singleton1(){}

    private static Singleton1 instance;

    // 缺点： 如果多线程同时访问，会造成产生多个变量
    // 因为存在同时进入null的可能性，或者一个正在new的时候，另一个判断为null
    public static Singleton1 getInstance(){
        if(instance == null){
            instance = new Singleton1();
        }

        return instance;
    }
}

class Singleton2 {
    private Singleton2(){}

    private static Singleton2 instance;

    // 缺点： 尽管这样避免了线程不安全的问题，但也会造成大量线程等待的困扰，没必要加锁整个函数
    public static synchronized Singleton2 getInstance(){
        if(instance == null){
            instance = new Singleton2();
        }

        return instance;
    }
}

// 双重校验锁
class Singleton3 {
    private Singleton3(){}

    // 对于一个new 操作分为三步：
    // 1. 为 singleton3 分配内存
    // 2. 初始化 singleton3
    // 3。将singleton3指向分配的内存空间
    // 对于java来说，JVM具有指令重排的特性，有时候执行顺序可能变成 1->3->2
    // 假设 t1执行了1和3， 然后轮到了t2， 这时候它会发现instance不为空，因此直接返回singleton3， 但它还未进行初始化
    // volatile 可以禁止指令重排
    private volatile static Singleton3 instance;

    // 好处在于： 相比2， 我们只需要在创建instance的时候加锁
    // 但是为什么是两次判断呢？ 假如只有一个if， 如果两个进程同时进入到了 if（instance ==  null）里面
    // 那么尽管其中一个会被锁住，但解锁后它还是会子创建新的一个
    public static Singleton3 getInstance(){
        if(instance == null){
            synchronized (Singleton3.class){
                if(instance == null){
                    instance = new Singleton3();
                }
            }
        }

        return instance;
    }
}

// 静态内部类实现, 比起自身静态类好处在于：
class Singleton5 {
    private Singleton5(){}

    // SingletonHolder 只有当getInstance被调用的时候，从而触发SingletonHolder.INSTANCE时
    // SingletonHolder才会被触发，从而创建实例
    // 比起自身静态类， 它既保证了只能拥有一个实例（因为静态变量只会在一开始时触发一次），又延迟了加载
    private static class SingletonHolder {
        private static final Singleton5 INSTANCE = new Singleton5();
    }

    public static Singleton5 getInstance(){
        return SingletonHolder.INSTANCE;
    }
}

//  枚举 Enum, 还可以防止反射攻击和反序列化
enum Singleton6{
    INSTANCE;
}

// 总结：
// a. 如果没有特许情况（如实例的创建是依赖参数或配置文件，又或者在getInstance前需要调用某些参数给它），考虑 饿汉式
// b. 如果明确要求懒加载， 可以使用 静态内部类
// c. 如果想防止反序列化和反射攻击， 使用枚举

