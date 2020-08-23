import java.util.concurrent.atomic.AtomicReference;

public class Solution implements Lock<Solution.Node> {
    private final Environment env;
    private final AtomicReference<Node> tail = new AtomicReference<>(null);

    public Solution(Environment env) {
        this.env = env;
    }

    @Override
    public Node lock() {
        Node my = new Node();
        my.locked.set(true);
        Node prev = tail.getAndSet(my);
        if (prev != null) {
            prev.next.set(my);
            while (my.locked.get()) env.park();
        }

        return my;
    }

    @Override
    public void unlock(Node node) {
        if (node.next.get() == null) {
            if (tail.compareAndSet(node, null)) {
                return;
            } else {
                while (node.next.get() == null) {}
            }
        }

        Node next = node.next.get();
        next.locked.set(false);
        env.unpark(next.thread);
    }

    static class Node {
        final Thread thread = Thread.currentThread();
        final AtomicReference<Boolean> locked = new AtomicReference<>(false);
        final AtomicReference<Node> next = new AtomicReference<>(null);
    }
}
