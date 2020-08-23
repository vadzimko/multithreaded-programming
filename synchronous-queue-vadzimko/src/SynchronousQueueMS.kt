import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SynchronousQueueMS<E> : SynchronousQueue<E> {

    private val dummy: Node<E?> = Node(null, false)
    private val head: AtomicReference<Node<E?>> = AtomicReference(dummy)
    private val tail: AtomicReference<Node<E?>> = AtomicReference(dummy)

    class Node<E>(item: E?, val isRequest: Boolean) {
        val next: AtomicReference<Node<E?>?> = AtomicReference(null)
        val elem: AtomicReference<E?> = AtomicReference(item)
        var cont: Continuation<Unit>? = null
    }

    override suspend fun receive(): E {
        val offer: Node<E?> = Node(null, true)

        while (true) {
            val tail = this.tail.get()
            var head = this.head.get()
            if (head == tail || tail.isRequest) {
                val next = tail.next.get()
                if (tail == this.tail.get()) {
                    if (next != null) {
                        this.tail.compareAndSet(tail, next)
                    } else if (tail.next.compareAndSet(next, offer)) {
                        this.tail.compareAndSet(tail, offer)
                        suspendCoroutine<Unit> { cont ->
                            offer.cont = cont
                        }

                        val elem = offer.elem.get()
                        head = this.head.get()
                        if (offer == head.next.get()) {
                            this.head.compareAndSet(head, offer)
                        }

                        return elem!!
                    }
                }
            } else {
                val next = head.next.get()
                if (tail != this.tail.get() || head != this.head.get() || next == null) {
                    continue
                }
                val cont = next.cont ?: continue

                val elem = next.elem.get()
                if (elem != null) {
                    val success = next.elem.compareAndSet(elem, null)
                    this.head.compareAndSet(head, next)
                    if (success) {
                        cont.resume(Unit)
                        return elem
                    }
                }
            }
        }
    }

    override suspend fun send(element: E) {
        val offer: Node<E?> = Node(element, false)

        while (true) {
            val tail = this.tail.get()
            var head = this.head.get()
            if (head == tail || !tail.isRequest) {
                val next = tail.next.get()
                if (tail == this.tail.get()) {
                    if (next != null) {
                        this.tail.compareAndSet(tail, next)
                    } else if (tail.next.compareAndSet(next, offer)) {
                        this.tail.compareAndSet(tail, offer)
                        suspendCoroutine<Unit> { cont ->
                            offer.cont = cont
                        }

                        head = this.head.get()
                        if (offer == head.next.get()) {
                            this.head.compareAndSet(head, offer)
                        }

                        return
                    }
                }
            } else {
                val next = head.next.get()
                if (tail != this.tail.get() || head != this.head.get() || next == null) {
                    continue
                }

                val cont = next.cont ?: continue
                val success = next.elem.compareAndSet(null, element)
                this.head.compareAndSet(head, next)
                if (success) {
                    cont.resume(Unit)
                    return
                }
            }
        }
    }
}
