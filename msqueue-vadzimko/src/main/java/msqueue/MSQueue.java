package msqueue;

import kotlinx.atomicfu.AtomicRef;

public class MSQueue implements Queue {
    private AtomicRef<Node> head;
    private AtomicRef<Node> tail;

    public MSQueue() {
        Node dummy = new Node(0);
        head = new AtomicRef<>(dummy);
        tail = new AtomicRef<>(dummy);
    }

    @Override
    public void enqueue(int x) {
        Node newTail = new Node(x);
        while (true) {
            if (tryMoveTail()) {
                continue;
            }

            Node tail = this.tail.getValue();
            if (tail.next.compareAndSet(null, newTail)) {
                this.tail.compareAndSet(tail, newTail);
                return;
            }
        }
    }

    @Override
    public int dequeue() {
        while (true) {
            Node head = this.head.getValue();
            while (tryMoveTail()) { }
            if (head == this.tail.getValue()) {
                return Integer.MIN_VALUE;
            }

            Node next = head.next.getValue();
            if (this.head.compareAndSet(head, next)) {
                return next.x;
            }
        }
    }

    private boolean tryMoveTail() {
        Node tail = this.tail.getValue();
        Node nextTail = tail.next.getValue();

        boolean needMove = nextTail != null;
        if (needMove) {
            this.tail.compareAndSet(tail, nextTail);
        }
        return needMove;
    }

    @Override
    public int peek() {
        while (true) {
            Node head = this.head.getValue();
            Node next = head.next.getValue();
            while (tryMoveTail()) {}
            if (head == tail.getValue()) {
                return Integer.MIN_VALUE;
            }
            if (next == null) {
                continue;
            }

            return next.x;
        }
    }

    private class Node {
        final int x;
        AtomicRef<Node> next;

        Node(int x) {
            this.x = x;
            next = new AtomicRef<>(null);
        }
    }
}