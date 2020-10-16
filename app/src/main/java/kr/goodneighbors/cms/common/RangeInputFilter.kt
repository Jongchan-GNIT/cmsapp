package kr.goodneighbors.cms.common

import android.text.InputFilter
import android.text.Spanned


class RangeInputFilter(var min: Int = 0, var max: Int = 0) : InputFilter {

    override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
        try {
            println("filter($source: CharSequence, $start: Int, $end: Int, $dest: Spanned, $dstart: Int, $dend: Int)")
            val input = Integer.parseInt(dest.subSequence(0, dstart).toString() + source + dest.subSequence(dend, dest.length))
            val isin = isInRange(min, max, input)
            println("min = $min, max = $max, input = $input, is in = $isin")
            if (isin)
                return null
        } catch (nfe: NumberFormatException) {
        }

        return ""
    }

    private fun isInRange(a: Int, b: Int, c: Int): Boolean {
        return if (b > a) c in a..b else c in b..a
    }
}