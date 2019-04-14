package iu5.sendbycom.link.hamming

import java.lang.Math.ceil
import java.lang.Math.pow
import java.util.*
import kotlin.math.log2

class HammingEncoder {
    fun encode(original: Message, length: Int): Message {
        assert(length >= 2)

        val nbits = length + ceil(log2(length.toDouble())).toInt() + 1
        val hamming = BitSet(nbits)

        var curOriginalPos = 0
        var representedCount = 1
        var curRepresented = 1

        for (curHammingPos in 0 until nbits) {
            val isChecking = ((curHammingPos) and (curHammingPos + 1) == 0)
            if (isChecking) {
                curRepresented = 0
                representedCount *= 2
            } else {
                // разложить число+1 на двойки, допустим 8 => 8+1 => 9 => 1001 => свапнуть биты 2^0 и 2^3=8
                var tmpPos = curHammingPos + 1
                var checkingPos = 0
                while(tmpPos > 0) {
                    val checkingBitRepresentsCurrent = tmpPos % 2 == 1
                    val isOdd = original.bits[curOriginalPos]
                    if (checkingBitRepresentsCurrent && isOdd) {
                        val representingCheckingBit = pow(2.0, checkingPos.toDouble()).toInt() - 1
                        hamming[representingCheckingBit] = !hamming[representingCheckingBit]
                    }
                    checkingPos++
                    tmpPos /= 2
                }
                hamming[curHammingPos] = original.bits[curOriginalPos++]
                curRepresented++
            }
        }

        return Message(hamming)
    }
}