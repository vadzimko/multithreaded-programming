import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class BlockingStackImpl<E> : BlockingStack<E> {

    // ==========================
    // Segment Queue Synchronizer
    // ==========================

    private val queue = FAAQueue<Continuation<E>>()

    private suspend fun suspend(): E {
        return suspendCoroutine { cont ->
            queue.enqueue(cont)
        }
    }

    private fun resume(element: E) {
        queue.dequeue()!!.resume(element)
    }

    // ==============
    // Blocking Stack
    // ==============


    private val head = AtomicReference<Node<E>?>()
    private val elements = AtomicInteger()

    override fun push(element: E) {
        val elements = this.elements.getAndIncrement()
        if (elements >= 0) {
            while (true) {
                val h = head.get()
                if (h?.element === SUSPENDED) {
                    if (head.compareAndSet(h, h.next)) {
                        resume(element)
                        return
                    }
                    continue
                }

                val newNode = Node(element, h)
                if (head.compareAndSet(h, newNode)) {
                    return
                }
            }
        } else {
            resume(element)
        }
    }

    override suspend fun pop(): E {
        val elements = this.elements.getAndDecrement()
        if (elements > 0) {
            // remove the top element from the stack
            while (true) {
                val h = head.get()
                if (h === null || h.element === SUSPENDED) {
                    val suspendNode = Node(SUSPENDED, h)
                    if (head.compareAndSet(h, suspendNode))
                        return suspend()
                    else continue
                }
                if (head.compareAndSet(h, h.next)) {
                    return h.element as E
                }
            }
        } else {
            return suspend()
        }
    }
}

private class Node<E>(val element: Any?, val next: Node<E>?)

private val SUSPENDED = Any()

class FAAQueue<T> {

    private val head: AtomicReference<QueueNode> // Head pointer, similarly to the Michael-Scott queue (but the first node is _not_ sentinel)
    private val tail: AtomicReference<QueueNode> // Tail pointer, similarly to the Michael-Scott queue

    private val enqIdx = AtomicLong()
    private val deqIdx = AtomicLong()

    init {
        val firstNode = QueueNode()
        head = AtomicReference(firstNode)
        tail = AtomicReference(firstNode)
    }

    fun enqueue(x: Any) {
        while (true) {
            val node = QueueNode(x)
            val tail = this.tail.get()
            if (tail.next.compareAndSet(null, node) && this.tail.compareAndSet(tail, node)) {
                enqIdx.incrementAndGet()
                return
            }
        }
    }

    fun dequeue(): T? {
        while (true) {
            if (deqIdx.get() >= enqIdx.get()) {
                continue
            } else {
                val head = this.head.get()
                val headNext = head.next.get() ?: continue
                if (this.head.compareAndSet(head, headNext)) {
                    deqIdx.incrementAndGet()
                    return headNext.data as T
                }
            }
        }
    }

    internal class QueueNode {
        val next = AtomicReference<QueueNode>(null)
        val data: Any?

        constructor(x: Any) {
            data = x
        }

        constructor() {
            data = null
        }
    }
}