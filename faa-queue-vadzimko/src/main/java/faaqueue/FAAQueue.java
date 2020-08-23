package faaqueue;

import kotlinx.atomicfu.AtomicInt;
import kotlinx.atomicfu.AtomicRef;

import static faaqueue.FAAQueue.Node.NODE_SIZE;


public class FAAQueue<T> implements Queue<T> {
    private static final Object DONE = new Object(); // Marker for the "DONE" slot state; to avoid memory leaks

    private AtomicRef<Node> head; // Head pointer, similarly to the Michael-Scott queue (but the first node is _not_ sentinel)
    private AtomicRef<Node> tail; // Tail pointer, similarly to the Michael-Scott queue

    public FAAQueue() {
        Node firstNode = new Node();
        head = new AtomicRef<>(firstNode);
        tail = new AtomicRef<>(firstNode);
    }

    @Override
    public void enqueue(T x) {
        while (true) {
            Node tail = this.tail.getValue();
            int enqIdx = tail.enqIdx.getAndIncrement();
            if (enqIdx >= NODE_SIZE) {
                Node newTail = new Node(x);
                if (tail.next.compareAndSet(null, newTail)
                        && this.tail.compareAndSet(tail, newTail)) {

                    return;
                }
            } else {
                if (tail.CAS(enqIdx, null, x)) {
                    return;
                }
            }
        }
    }

    @Override
    public T dequeue() {
        while (true) {
            Node head = this.head.getValue();
            if (head.isEmpty()) {
                Node headNext = head.next.getValue();
                if (headNext == null)
                    return null;
                this.head.compareAndSet(head, headNext);
            } else {
                int deqIdx = head.deqIdx.getAndIncrement();
                if (deqIdx >= NODE_SIZE) {
                    continue;
                }

                AtomicRef<Object> elem = (AtomicRef<Object>) head.data[deqIdx];
                Object res = elem.getAndSet(DONE);
                if (res == null) {
                    continue;
                }

                return (T) res;
            }
        }
    }

    static class Node {
        static final int NODE_SIZE = 2; // CHANGE ME FOR BENCHMARKING ONLY

        private AtomicRef<Node> next = new AtomicRef<>(null);
        private AtomicInt enqIdx = new AtomicInt(0); // index for the next enqueue operation
        private AtomicInt deqIdx = new AtomicInt(0); // index for the next dequeue operation
        private final Object[] data = new Object[NODE_SIZE];

        Node() {
            for (int i = 0; i < NODE_SIZE; i++) {
                this.data[i] = new AtomicRef<>(null);
            }
        }

        Node(Object x) {
            this.enqIdx.setValue(1);
            this.data[0] = new AtomicRef<>(x);
            for (int i = 1; i < NODE_SIZE; i++) {
                this.data[i] = new AtomicRef<>(null);
            }
        }

        boolean CAS(int index, Object oldValue, Object newValue) {
            AtomicRef<Object> val = (AtomicRef<Object>) data[index];
            return val.compareAndSet(oldValue, newValue);
        }

        private boolean isEmpty() {
            return this.deqIdx.getValue() >= this.enqIdx.getValue() || this.deqIdx.getValue() >= NODE_SIZE;
        }
    }
}