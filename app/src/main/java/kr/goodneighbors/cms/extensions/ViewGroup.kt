package kr.goodneighbors.cms.extensions

import android.view.View
import android.view.ViewGroup
import android.widget.Spinner

fun ViewGroup.asSequence(): Sequence<View> = object : Sequence<View> {

    override fun iterator(): Iterator<View> = object : Iterator<View> {
        private var nextValue: View? = null
        private var done = false
        private var position: Int = 0

        override fun hasNext(): Boolean {
            if (nextValue == null && !done) {
                nextValue = getChildAt(position)
                position++
                if (nextValue == null) done = true
            }
            return nextValue != null
        }

        override fun next(): View {
            if (!hasNext()) {
                throw NoSuchElementException()
            }
            val answer = nextValue
            nextValue = null
            return answer!!
        }
    }
}

    val ViewGroup.views: List<View>
    get() = asSequence().toList()

    val ViewGroup.viewsRecursive: List<View>
    get() = views.flatMap {
        if (it is Spinner) {
            listOf(it)
        }
        else {
            when (it) {
                is ViewGroup -> it.viewsRecursive
                else -> listOf(it)
            }
        }
    }