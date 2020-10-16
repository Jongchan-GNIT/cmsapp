package kr.goodneighbors.cms.service.model

data class StatisticsItem(
        var codeSupportCountry: List<SpinnerOption> ?= null
)

data class StatisticCifItem(
        var SPLY_MON: String ?= null,
        var SUB: String ?= null,
        var AP: String ?= null,
        var RE: String ?= null,
        var WA: String ?= null,
        var NR: String ?= null,
        var AP_PCT: String ?= null,
        var RE_PCT: String ?= null,
        var WA_PCT: String ?= null,
        var NR_PCT: String ?= null
)

data class StatisticAprItem(
        var SUB: String ?= null,
        var AP: String ?= null,
        var RE: String ?= null,
        var WA: String ?= null,
        var NR: String ?= null,
        var AP_PCT: String ?= null,
        var RE_PCT: String ?= null,
        var WA_PCT: String ?= null,
        var NR_PCT: String ?= null
)

data class StatisticAclItem(
        var SUB: String ?= null,
        var AP: String ?= null,
        var RE: String ?= null,
        var WA: String ?= null,
        var NR: String ?= null,
        var AP_PCT: String ?= null,
        var RE_PCT: String ?= null,
        var WA_PCT: String ?= null,
        var NR_PCT: String ?= null
)

data class StatisticDropoutItem(
        var REG_CNT: String ?= null,
        var AP: String ?= null,
        var RE: String ?= null,
        var WA: String ?= null,
        var AP_PCT: String ?= null,
        var RE_PCT: String ?= null,
        var WA_PCT: String ?= null
)

data class StatisticGmlItem(
        var MONTH: String ?= null,
        var CNT: String ?= null,
        var AMT: String ?= null,
        var AP: String ?= null,
        var RE: String ?= null,
        var WA: String ?= null,
        var NR: String ?= null,
        var AP_PCT: String ?= null,
        var RE_PCT: String ?= null,
        var WA_PCT: String ?= null,
        var NR_PCT: String ?= null
)