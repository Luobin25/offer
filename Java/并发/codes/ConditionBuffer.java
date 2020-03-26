import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// 构造消息队列， 当队列无论时空还是满的时候，阻塞它（ArrayBlockingQueue就跟这个差不多）。
public class ConditionBuffer {
    final Lock lock = new ReentrantLock();
    final Condition notEmpty = lock.newCondition();
    final Condition notFull = lock.newCondition();

    final Object[] items = new Object[10];
    int count, putptr, takeptr;

    public void put(Object x) throws InterruptedException{
        lock.lock();
        try{
            while(count == items.length)
                notFull.await();

            items[putptr] = x;
            if(++putptr == items.length)    putptr = 0;
            ++count;

            notEmpty.signalAll();
        }finally {
            lock.unlock();
        }

    }

    public Object take() throws InterruptedException{
        lock.lock();
        try{
            while(count == 0)
                notEmpty.await();
            Object x = items[takeptr];
            if(++takeptr == items.length)   takeptr = 0;
            --count;

            notFull.signalAll();
            return  x;
        }finally {
            lock.unlock();
        }
    }
}
