import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// 构建缓存， 当key不存在的时候， 从DB中获取
public class ReentrantReadWriteForCachedByDB {
    private final Map<String, String> map = new TreeMap<>();
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.readLock();

    public String get(String key){
        String v = null;
        r.lock();
        try{
            v = map.get(key);
        }finally {
            r.unlock();
        }

        if(v != null)
            return v;

        w.lock();
        try{
            // 需要再次验证， 原因类似单例模型需要两次检验一样. 因为存在多个线程同时进入，通过判断，可以避免每个线程都像数据库调用一次
            if(v == null){
                // 查询数据库
                v = "demo";
                map.put(key, v);
            }
        }finally {
            w.unlock();
        }

        return v;
    }

    // 这里模拟一个写锁降级的场景: 通过cacheValid来判断cache是否失效
    private boolean cachedValid;
    private String data;
    public void processCachedData(){
        // 获取读锁
        r.lock();
        if(!cachedValid){
            // 释放读锁 获取 写锁， 注意 可重入读写锁 不支持 读锁升级， 只支持写锁降级
            r.unlock();
            w.lock();
            try{
                if(!cachedValid){       //同上面一样
                    // 从DB获取最新数据
                    data = "demo";
                    cachedValid = true;
                }
                // 写锁降级, 直接在拥有写锁的情况，可获取读锁
                r.lock();
            }finally {
                w.unlock();
            }
        }

        try{
            use(data);
        }finally {
            r.unlock();
        }
    }

    public void use(String data){
        // ... do something
    }
}
