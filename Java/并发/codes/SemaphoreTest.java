import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

// 通过 Semaphore 设置最大允许3个进行并发（类似线程池）， 注意 acquire（） 和 release（）只保证设置 信号量的时候原子性
// 获取信号量处理的内容是不保证的， 必要时还要加上 synchronized
public class SemaphoreTest {


    public static void main(String[] args) {
        ExecutorService executors = Executors.newCachedThreadPool();
        Semaphore s = new Semaphore(3);

        System.out.println("初始化， 当前有 " + (3 - s.availablePermits()) + " 个并发");
        for(int i = 0; i < 10; i++){
            final int No = i;
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    try{
                        s.acquire();
                        System.out.println(Thread.currentThread().getName()
                                + "获取许可， Number为 " + No + " 剩余 " + s.availablePermits());
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }finally {
                        s.release();
                        System.out.println(Thread.currentThread().getName()
                                + "释放许可， Number为 " + No + " 剩余 " + s.availablePermits());
                    }
                }
            };

            executors.execute(run);
        }

        executors.shutdown();
    }
}
