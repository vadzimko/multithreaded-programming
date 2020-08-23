package stack;

import kotlinx.atomicfu.AtomicRef;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StackImpl implements Stack {
    private static class Node {
        final AtomicRef<Node> next;
        final int x;

        Node(int x, Node next) {
            this.next = new AtomicRef<>(next);
            this.x = x;
        }
    }

    private static int ELIMINATION_ARRAY_SIZE = 100;
    private static int ELIMINATION_MAX_SHIFT = 2;
    private static int SPIN_NUMBER = 30;
    private static int DONE = Integer.MIN_VALUE;

    private List<AtomicRef<Integer>> eliminationArray = new ArrayList<>(ELIMINATION_ARRAY_SIZE);
    private static Random random = new Random(System.currentTimeMillis());

    public StackImpl() {
        super();
        for (int i = 0; i < ELIMINATION_ARRAY_SIZE; i++) {
            eliminationArray.add(new AtomicRef<Integer>(null));
        }
    }

    // head pointer
    private AtomicRef<Node> head = new AtomicRef<>(null);

    private boolean tryInsertToEliminationArray(int index, int x) {
        return eliminationArray.get(index).compareAndSet(null, x);
    }

    private int getLowerBound(int startIndex) {
        return Math.min(startIndex + ELIMINATION_MAX_SHIFT, ELIMINATION_ARRAY_SIZE - 1);
    }

    private int getUpperBound(int startIndex) {
        return Math.max(0, startIndex - ELIMINATION_MAX_SHIFT);
    }

    @Override
    public void push(int x) {
        int eliminationStartIndex = random.nextInt(ELIMINATION_ARRAY_SIZE);
        int eliminationIndex = -1;

        int lowerBound = getLowerBound(eliminationStartIndex);
        int upperBound = getUpperBound(eliminationStartIndex);
        for (int index = lowerBound; index <= upperBound; index++) {

            if (tryInsertToEliminationArray(index, x)) {
                eliminationIndex = index;
                break;
            }
        }

        if (eliminationIndex >= 0) {
            AtomicRef<Integer> value = eliminationArray.get(eliminationIndex);
            for (int i = 0; i < SPIN_NUMBER; i++) {
                if (value.compareAndSet(DONE, null)) {
                    return;
                }
            }

            Integer result = value.getAndSet(null);
            if (result == DONE) {
                return;
            }
        }

        while (true) {
            Node newHead = new Node(x, head.getValue());
            if (head.compareAndSet(newHead.next.getValue(), newHead)) {

                return;
            }
        }
    }

    @Override
    public int pop() {
        int eliminationStartIndex = random.nextInt(ELIMINATION_ARRAY_SIZE);

        int lowerBound = getLowerBound(eliminationStartIndex);
        int upperBound = getUpperBound(eliminationStartIndex);
        for (int index = lowerBound; index <= upperBound; index++) {
            Integer value = eliminationArray.get(index).getValue();
            if (value != null && value != DONE && eliminationArray.get(index).compareAndSet(value, DONE)) {
                return value;
            }
        }

        while (true) {
            Node curHead = head.getValue();
            if (curHead == null) {
                return Integer.MIN_VALUE;
            }

            if (head.compareAndSet(curHead, curHead.next.getValue())) {
                return curHead.x;
            }
        }
    }
}
