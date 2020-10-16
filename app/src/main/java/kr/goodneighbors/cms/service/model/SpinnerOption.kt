package kr.goodneighbors.cms.service.model

data class SpinnerOption(
        val key: String,
        val value: String
) {
    override fun toString(): String {
        return value
    }

    override fun hashCode(): Int {
        return key.toInt()
    }
}