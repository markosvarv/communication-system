package shared_buffer;

import java.util.Vector;

public class SharedBuffer {
    private int size;
    private Vector<String> buffer;

    public SharedBuffer (int buffer_size) {
        size = buffer_size;
        buffer = new Vector<>();
    }

    synchronized String get() {
        if (buffer.isEmpty()) {
            try{
                wait();
            } catch (InterruptedException e) {
                System.out.println("InterruptedException caught");
            }
        }
        String got=null;
        if (!buffer.isEmpty()) got = buffer.remove(0);

        notify();
        return got;
    }

    synchronized void put(String n) {
        if (buffer.size() == size) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("InterruptedException caught");
            }
        }
        buffer.add(n);
        notify();
    }
}