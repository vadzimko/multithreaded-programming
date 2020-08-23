package linked_list_set;

import kotlinx.atomicfu.AtomicRef;

public class SetImpl implements Set {
    interface INode {
        int getX();

        AtomicRef<INode> getNext();
    }

    public class Node implements INode {
        AtomicRef<INode> next;
        int x;

        Node(int x, INode next) {
            this.x = x;
            this.next = new AtomicRef<>(next);
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public AtomicRef<INode> getNext() {
            return next;
        }
    }

    public class RemovedNode implements INode {
        final Node node;

        RemovedNode(Node node) {
            this.node = node;
        }

        @Override
        public int getX() {
            return node.getX();
        }

        INode getNode() {
            return node;
        }

        @Override
        public AtomicRef<INode> getNext() {
            return node.getNext();
        }
    }

    private class Window {
        INode cur, next;

        Window(INode cur, INode next) {
            this.cur = cur;
            this.next = next;
        }
    }

    private final Node head = new Node(Integer.MIN_VALUE, new Node(Integer.MAX_VALUE, null));

    /**
     * Returns the {@link Window}, where cur.x < x <= next.x
     */
    private Window findWindow(int x) {
        while (true) {
            INode cur = head;
            INode next = cur.getNext().getValue();

            boolean needBrake = false;
            while (next.getX() < x) {
                INode node = next.getNext().getValue();
                if (node instanceof RemovedNode) {
                    if (next instanceof Node && !cur.getNext().compareAndSet(next, ((RemovedNode) node).getNode())) {
                        needBrake = true;
                        break;
                    }
                    next = ((RemovedNode) node).getNode();
                } else {
                    cur = next;
                    next = cur.getNext().getValue();
                }
            }

            if (needBrake) {
                continue;
            }

            if (next.getX() == Integer.MAX_VALUE) {
                return new Window(cur, next);
            }

            INode node = next.getNext().getValue();
            if (node instanceof Node) {
                return new Window(cur, next);
            }

            cur.getNext().compareAndSet(next, ((RemovedNode) node).getNode());
        }
    }

    @Override
    public boolean add(int x) {
        while (true) {
            Window w = findWindow(x);
            if (w.next.getX() == x) {
                return false;
            }

            INode node = new Node(x, w.next);
            if (w.next instanceof Node && w.cur.getNext().compareAndSet(w.next, node)) {
                return true;
            }
        }
    }

    @Override
    public boolean remove(int x) {
        while (true) {
            Window w = findWindow(x);
            if (w.next.getX() != x) {
                return false;
            }

            INode nextNext = w.next.getNext().getValue();
            if (nextNext instanceof RemovedNode) {
                return false;
            }

            INode removedNextNext = new RemovedNode((Node) nextNext);
            if (w.next.getNext().compareAndSet(nextNext, removedNextNext)) {
                w.cur.getNext().compareAndSet(w.next, nextNext);
                return true;
            }
        }
    }

    @Override
    public boolean contains(int x) {
        Window w = findWindow(x);
        return w.next.getX() == x;
    }
}