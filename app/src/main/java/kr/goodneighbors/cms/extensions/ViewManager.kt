package kr.goodneighbors.cms.extensions

import android.view.ViewManager
import de.hdodenhof.circleimageview.CircleImageView
import kr.goodneighbors.cms.ui.childlist.custom.CustomBottomNavigationView
import org.jetbrains.anko.custom.ankoView

inline fun ViewManager.circleImageView() = circleImageView {}
inline fun ViewManager.circleImageView(init: CircleImageView.() -> Unit): CircleImageView {
    return ankoView({ CircleImageView(it) }, theme = 0, init = init)
}

inline fun ViewManager.customBottomNavigationView() = customBottomNavigationView {}
inline fun ViewManager.customBottomNavigationView(init: CustomBottomNavigationView.() -> Unit): CustomBottomNavigationView {
    return ankoView({ CustomBottomNavigationView(it) }, theme = 0, init = init)
}
