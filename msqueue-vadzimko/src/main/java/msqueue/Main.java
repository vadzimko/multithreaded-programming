package msqueue;

public class Main {
    public static void main(String[] args) {
        final MSQueue q = new MSQueue();
        Thread first = new Thread() {
            public void run() {
                q.enqueue(4);
                System.out.println(q.dequeue());
                q.enqueue(3);
                q.enqueue(3);
                System.out.println(q.dequeue());
            }
        };

        Thread second = new Thread() {
            public void run() {
                System.out.println(q.dequeue());
                System.out.println(q.dequeue());
                System.out.println(q.dequeue());
                System.out.println(q.dequeue());
                System.out.println(q.dequeue());
                System.out.println(q.dequeue());
                System.out.println(q.dequeue());
                q.enqueue(1);
                System.out.println(q.dequeue());
                System.out.println(q.dequeue());
            }
        };

        first.start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        second.start();
    }
}
