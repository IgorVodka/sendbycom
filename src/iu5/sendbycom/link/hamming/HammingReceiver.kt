package iu5.sendbycom.link.hamming

import sun.security.util.BitArray
import java.util.*
import kotlin.math.log2

class HammingReceiver {
    fun findError(message: Message, length: Int): Int {
        assert(length >= 2)

        val syndrome = BitSet(log2(length.toDouble()).toInt() + 1)

        for(i in 0 until length) {
            var tmp = i + 1
            var pos = 0
            while(tmp > 0) {
                val checkingBitRepresentsCurrent = tmp % 2 == 1
                if(checkingBitRepresentsCurrent && message.bits[i]) {
                    val representingCheckingBit = Math.pow(2.0, pos.toDouble()).toInt() - 1
                    val errorIndex = log2(representingCheckingBit.toDouble() + 1).toInt()
                    syndrome[errorIndex] = !syndrome[errorIndex]
                }
                pos++
                tmp /= 2
            }
        }

        return toInt(syndrome, length) - 1
    }

    fun selectInformationBits(message: Message, length: Int): Message {
        val result = BitSet(length - log2(length.toDouble()).toInt() - 1)

        var curOriginalPos = 0
        var representedCount = 1
        var curRepresented = 1

        for (curHammingPos in 0 until length) {
            val isChecking = ((curHammingPos) and (curHammingPos + 1) == 0)
            if (isChecking) {
                curRepresented = 0
                representedCount *= 2
            } else {
                result[curOriginalPos] = message.bits[curHammingPos]
                curRepresented++
                curOriginalPos++
            }
        }

        return Message(result)
    }

    fun fixBits(message: Message, errorIndex: Int, length: Int): Message {
        assert(errorIndex >= -1 && errorIndex < length)
        val copy = message.bits.clone() as BitSet
        if (errorIndex != -1) {
            copy[errorIndex] = !copy[errorIndex]
        }
        return Message(copy)
    }

    private fun toInt(bits: BitSet, length: Int): Int {
        var result = 0
        var multiplier = 1

        for (i in 0 until length) {
            val value = bits[i]
            if (value) result += multiplier
            multiplier *= 2
        }

        return result
    }
}
