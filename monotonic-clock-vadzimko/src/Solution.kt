/**
 * В теле класса решения разрешено использовать только переменные делегированные в класс RegularInt.
 * Нельзя volatile, нельзя другие типы, нельзя блокировки, нельзя лазить в глобальные переменные.
 */
class Solution : MonotonicClock {
    private var c1_1 by RegularInt(0)
    private var c1_2 by RegularInt(0)
    private var c1_3 by RegularInt(0)
    private var c2_1 by RegularInt(0)
    private var c2_2 by RegularInt(0)
    private var c2_3 by RegularInt(0)

    override fun write(time: Time) {
        // write right-to-left
        c2_1 = time.d1
        c2_2 = time.d2
        c2_3 = time.d3


        c1_3 = time.d3
        c1_2 = time.d2
        c1_1 = time.d1
    }

    override fun read(): Time {
        // read left-to-right
        while (true) {
            val d1_1 = c1_1
            val d1_2 = c1_2
            val d1_3 = c1_3
            val d2_3 = c2_3
            val d2_2 = c2_2
            val d2_1 = c2_1

            if (d1_1 == d2_1 && d1_2 == d2_2 && d1_3 == d2_3) {
                return Time(d1_1, d1_2, d1_3)
            }
        }
    }
}