class Solution : AtomicCounter {
    // объявите здесь нужные вам поля

    private val root = Node(0)
    private val last = ThreadLocal<Node>()
    private val my = ThreadLocal<Int>()

    override fun getAndAdd(x: Int): Int {
        if (last.get() == null) {
            last.set(root)
            my.set(0)
        }

        val node = Node(x)
        var res = 0
        while (last.get() !== node) {
            last.set(last.get().next.decide(node))
            res = my.get()
            my.set(res + last.get().value)
        }

        return res
    }

    // вам наверняка потребуется дополнительный класс
    private class Node internal constructor(val value: Int) {
        val next = Consensus<Node>()
    }
}
