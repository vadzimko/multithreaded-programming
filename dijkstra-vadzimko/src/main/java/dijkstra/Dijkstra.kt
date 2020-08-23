package dijkstra

import java.util.*
import java.util.concurrent.Phaser
import java.util.concurrent.atomic.AtomicInteger
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

private val NODE_DISTANCE_COMPARATOR = Comparator<Node> { o1, o2 -> o1!!.distance.compareTo(o2!!.distance) }

// Returns `Integer.MAX_VALUE` if a path has not been found.
fun shortestPathParallel(start: Node) {
    val workers = Runtime.getRuntime().availableProcessors()
    // The distance to the start node is `0`
    start.distance = 0
    // Create a priority (by distance) queue and add the start node into it
    val q = MultiQueue(workers)
    q.add(start)
    // Run worker threads and wait until the total work is done
    val onFinish = Phaser(workers + 1) // `arrive()` should be invoked at the end by each worker
    val activeNodes = AtomicInteger(1)
    repeat(workers) {
        thread {
            while (true) {
                val cur: Node = q.poll() ?: if (activeNodes.get() == 0)
                    break else continue

                for (e in cur.outgoingEdges) {
                    while (true) {
                        val oldDist = e.to.distance
                        val newDist = cur.distance + e.weight
                        if (oldDist > newDist) {
                            if (e.to.casDistance(oldDist, newDist)) {
                                q.add(e.to)
                                activeNodes.incrementAndGet()
                                break
                            }
                        } else break
                    }
                }
                activeNodes.decrementAndGet()
            }
            onFinish.arrive()
        }
    }
    onFinish.arriveAndAwaitAdvance()
}

class MultiQueue(workers: Int) {
    private val comparator = NODE_DISTANCE_COMPARATOR
    private var n = 2 * workers
    private var qs: List<PriorityQueue<Node>> = ArrayList(Collections.nCopies(n, PriorityQueue(comparator)))

    fun add(node: Node) {
        val qNum = (0 until n).random()
        val q = qs[qNum]
        synchronized(q) { q.add(node) }
    }

    fun poll(): Node? {
        var q1Num = (0 until n).random()
        var q2Num = (0 until n).random()
        if (q1Num == q2Num) {
            q2Num = (q2Num + 1) % n
        }
        if (q1Num > q2Num) {
            q1Num = q2Num.also { q2Num = q1Num }
        }
        val q1 = qs[q1Num]
        val q2 = qs[q2Num]
        synchronized(q1) {
            synchronized(q2) {
                if (q1.peek() == null) {
                    return q2.peek()
                }

                if (q2.peek() == null) {
                    return q1.peek()
                }

                return if (q1.peek().distance < q2.peek().distance) {
                    q1.poll()
                } else {
                    q2.poll()
                }

            }
        }
    }
}