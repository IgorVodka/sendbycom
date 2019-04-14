package iu5.sendbycom.link.hamming

import java.util.*

class Message(private val bitSet: BitSet) {
    val bits: BitSet
        get() = this.bitSet
}