package hu.akarnokd.queue;

public class ArrayQueue {
    Object[] array = new Object[8];
    int mask = 7;
    
    long producerIndex;
    long consumerIndex;
    
    public void offer(Object o) {
        if (consumerIndex + mask + 1 == producerIndex) {
            
            Object[] b = new Object[array.length << 1];
            int mask2 = b.length - 1;
            
            int oldOffset = (int)consumerIndex & mask;
            int newOffset = (int)consumerIndex & mask2;
            
            System.arraycopy(array, oldOffset, b, newOffset, array.length - oldOffset);
            System.arraycopy(array, 0, b, array.length, oldOffset);
            
            mask = mask2;
            
            array = b;
            
            b[(int)producerIndex & mask] = o;
            producerIndex++;
        } else {
            int offset = (int)producerIndex & mask;
            array[offset] = o;
            producerIndex++;
        }
    }
    
    public Object poll() {
        if (producerIndex == consumerIndex) {
            return null;
        }
        int offset = (int)consumerIndex & mask;
        Object v = array[offset];
        array[offset] = null;
        consumerIndex++;
        return v;
    }
    
    public static void main(String[] args) {
        ArrayQueue q = new ArrayQueue();
        
        q.offer(1);
        q.offer(2);
        q.offer(3);
        
        q.poll();
        q.poll();
        q.poll();
        
        for (int i = 4; i < 16; i++) {
            q.offer(i);
        }
        
        for (int i = 4; i < 16; i++) {
            System.out.println(q.poll());
        }
    }
}
