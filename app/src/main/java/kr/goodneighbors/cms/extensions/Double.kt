package kr.goodneighbors.cms.extensions

import java.math.BigDecimal

fun Double.round(_p: Int = 2) =     BigDecimal(this).setScale(_p, BigDecimal.ROUND_HALF_UP).toDouble()
