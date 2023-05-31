package android.util

object Log {
    // 定义彩色代码
    private const val ANSI_RESET = "\u001B[0m"
    private const val ANSI_RED = "\u001B[31m"
    private const val ANSI_GREEN = "\u001B[32m"
    private const val ANSI_YELLOW = "\u001B[33m"
    private const val ANSI_CYAN = "\u001B[36m"

    private fun printlnAnsi(str: String, ansi: String) {
        println(ansi + str + ANSI_RESET)
    }

    @JvmStatic
    fun d(tag: String, msg: String): Int {
        printlnAnsi("$tag | $msg", ANSI_CYAN)
        return 0
    }

    @JvmStatic
    fun i(tag: String, msg: String): Int {
        printlnAnsi("$tag | $msg", ANSI_GREEN)
        return 0
    }

    @JvmStatic
    fun w(tag: String, msg: String): Int {
        printlnAnsi("$tag | $msg", ANSI_YELLOW)
        return 0
    }

    @JvmStatic
    fun e(tag: String, msg: String): Int {
        printlnAnsi("$tag | $msg", ANSI_RED)
        return 0
    }
}