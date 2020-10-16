package kr.goodneighbors.cms

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kr.goodneighbors.cms.extensions.toDateFormat
import kr.goodneighbors.cms.service.entities.CH_MST
import kr.goodneighbors.cms.service.entities.FMLY
import kr.goodneighbors.cms.service.entities.REMRK
import kr.goodneighbors.cms.service.entities.RPT_BSC
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

class LogicTest {
    @Test
    fun logic() {
        val string = "3583010901224331538316010879"
        val now = Date().time
        println(now)
        println(now.toDateFormat())
        println(1535900400000.toDateFormat())
        println(now.toString(32))

        println(string.toBigInteger().toString(32))
    }

    @Test
    fun slice() {
        val list = arrayListOf<Int>(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        println("before list : $list")
        val s = list.slice(0..5)

        println("after list : $list")
        println("s : $s")
    }

    @Test
    fun percent() {
//        total : 21648891, current: 14308859
//        val current: Long = 14308859
//        val total: Long = 21648891
//        val done: Int = ((current / total.toDouble()) * 100).toInt()
//        println( done )

        val a: Long = 1230
        val b: Long = 122
        println(a.compareTo(b))
    }

    @Test
    fun substr() {
        val a = "201801"
        println(a.substring(0, 4))
        println(a.substring(4))
    }

    @Test
    fun testJson() {
        val gson = GsonBuilder().serializeNulls().setPrettyPrinting().create()
        val a = CH_MST(CHRCP_NO = "123", CTR_CD = "A", BRC_CD = "B", PRJ_CD = "C")
        println(a)
        println(gson.toJson(a))
    }

    @Test
    fun testJson2() {
        val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().serializeNulls().setPrettyPrinting().create()
        val a = RPT_BSC(CHRCP_NO = "123", RCP_NO = "456")
        val remark = REMRK(RCP_NO = "123", REMRK_ENG = "remark")
        a.REMRK = remark

        println(a)
        println(gson.toJson(a))
    }

    @Test
    fun testJson3() {
        val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().serializeNulls().setPrettyPrinting().create()
        val a = RPT_BSC(CHRCP_NO = "123", RCP_NO = "456")
        val b = FMLY(RCP_NO = "123")
        val gson2 = Gson()
    }

    @Test
    fun testTimestamp() {
        val timestamp = Date().time
        println(timestamp)
        println(SimpleDateFormat("YYYY-MM-dd HH", Locale.UK).format(Date(timestamp)))
        println(SimpleDateFormat("YYYY-MM-dd HH", Locale.US).format(Date(timestamp)))
        println(SimpleDateFormat("YYYY-MM-dd HH", Locale.KOREA).format(Date(timestamp)))
    }

    @Test
    fun testLoop() {
        val start = 1
        val end = 1

        for(i in end downTo start) {
            println("i = $i")
        }
    }
}


