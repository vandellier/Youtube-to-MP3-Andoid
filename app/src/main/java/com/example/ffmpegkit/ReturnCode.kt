package com.example.ffmpegkit

/**
 * Represents the return code of an FFmpeg execution session.
 */
class ReturnCode(private val value: Int) {

    fun getValue(): Int = value

    fun isSuccess(): Boolean = value == 0

    fun isCancel(): Boolean = value == 255

    fun isError(): Boolean = !isSuccess() && !isCancel()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ReturnCode) return false
        return value == other.value
    }

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = "ReturnCode($value)"

    companion object {
        val SUCCESS = ReturnCode(0)
        val CANCEL = ReturnCode(255)

        fun isSuccess(returnCode: ReturnCode?): Boolean = returnCode?.isSuccess() == true
        fun isCancel(returnCode: ReturnCode?): Boolean = returnCode?.isCancel() == true
    }
}
