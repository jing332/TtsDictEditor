package com.github.jing332

import com.github.jing332.tts_dict_editor.help.DictFileManager
import com.github.jing332.tts_dict_editor.help.DictFileManager.Companion.toTxt
import com.github.jing332.tts_dict_editor.help.GroupWithReplaceRule
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream

class TestDict {
    val dicter = DictFileManager()

    @Test
    fun testDictGroups() {
        runBlocking {
            dicter.load(Constants.DICT_TXT)
            val list = dicter.groupWithReplaceRules()
            println(list.toTxt())
        }
    }

    @Test
    fun dict() {


//
//        val ttsrvpath =
//            "G:\\Users\\jing\\AndroidStudioProjects\\TtsDictEditor\\app\\src\\test\\assets\\ttsrv-replaces.json"
//        val groupWithRules: List<GroupWithReplaceRule> =
//            Json { ignoreUnknownKeys = true }.decodeFromString(File(ttsrvpath).readText())
//
//        val groupWithRulesTxt = groupWithRules.toTxt()
//        println("groupWithRulesTxt:\n$groupWithRulesTxt")
//
//        println("=========================================")
//        val dicter = DictFileManager()
//        runBlocking {
//            dicter.load(groupWithRulesTxt.byteInputStream())
//            val list = dicter.groupWithReplaceRules()
//            println(list.toTxt())
////            println(Json {
////                ignoreUnknownKeys = true
////                prettyPrint = true
////            }.encodeToString(list))
//        }

//        runBlocking {
//            loader.load(File("G:\\Users\\jing\\Desktop\\dict.txt").inputStream())
//            var str = """
//                我在装x！
//                你在装X？
//            """.trimIndent()
//
//            loader.rules.forEach {
//                str = str.replace(it.pattern.toRegex(), it.replacement)
//            }
//            println(str)
//
//            println(loader.rules.toTxt())
//            loader.load(ByteArrayInputStream(loader.rules.toTxt().toByteArray()))
//            loader.rules.forEach {
//                str = str.replace(it.pattern.toRegex(), it.replacement)
//            }
//            println(str)
//
//        }
    }
}