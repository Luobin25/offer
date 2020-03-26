import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// 利用读多写少的性质， 来构建缓存
public class ReentrantReadWriteForCached {
    private final Map<String, String> map = new TreeMap<>();
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();

    public String get(String key) {
        r.lock();
        try {
            return map.get(key);
        } finally {
            r.unlock();
        }
    }

    public List<String> allKeys(){
        r.lock();
        try{
            return new ArrayList<>(map.keySet());
        }finally {
            r.unlock();
        }
    }


    public String set(String key, String value){
        w.lock();
        try{
            return map.put(key, value);
        }finally {
            w.unlock();
        }
    }

    public void clean(){
        w.lock();
        try{
            map.clear();
        }finally {
            w.unlock();
        }
    }
}
