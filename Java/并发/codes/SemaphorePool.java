import java.util.concurrent.Semaphore;

// 模拟一个线程池
public class SemaphorePool {
    private static final int MAX_COUNT = 10;
    private final Semaphore available = new Semaphore(MAX_COUNT, true);         // 这是线程池， 每一个连接请求都代表一个客户端， 所以需要引入公平锁

    public Object getItem() throws InterruptedException {
        available.acquire();            // 如果 acquire 失败， 会阻塞再这
        return getNextAvailableItem();  // acquire成功， 返回线程
    }

    public void putItem(Object x){
        if(marksAsUnused(x))
            available.release();
    }

    protected Object[] items = new Object[MAX_COUNT];       // 定义为任何你想管理的东西
    protected boolean[] used = new boolean[MAX_COUNT];

    public synchronized Object getNextAvailableItem(){
        for(int i = 0; i < MAX_COUNT; i++){
            if(!used[i]){
                used[i] = true;
                return items[i];
            }
        }

        return null;            // 永远不会到达
    }

    public synchronized boolean marksAsUnused(Object x){
        for(int i = 0; i < MAX_COUNT; i++){
            if(x == items[i]){
                if(used[i]){
                    used[i] = false;
                    return true;
                }else{
                    return false;
                }
            }
        }

        return false;
    }
}
