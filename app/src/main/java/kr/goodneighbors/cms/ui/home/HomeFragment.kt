package kr.goodneighbors.cms.ui.home

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.graphics.Typeface
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import kr.goodneighbors.cms.R
import kr.goodneighbors.cms.extensions.observeOnce
import kr.goodneighbors.cms.extensions.toDateFormat
import kr.goodneighbors.cms.service.viewmodel.HomeViewModel
import kr.goodneighbors.cms.ui.BaseActivityFragment
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.dimen
import org.jetbrains.anko.dip
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.imageView
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.rightPadding
import org.jetbrains.anko.scrollView
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.space
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent
import java.text.NumberFormat
import java.util.*

class HomeFragment : BaseActivityFragment() {
    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }

    private val ui = FragmentUI()

    private val viewModel: HomeViewModel by lazy {
        HomeViewModel()
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = ui.createView(AnkoContext.create(requireContext(), this))

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)

        val ctrCd = sharedPref.getString("user_ctr_cd", "")
        val brcCd = sharedPref.getString("user_brc_cd", "")
        val prjCd = sharedPref.getString("user_prj_cd", "")

        val userName = sharedPref.getString("username", "")

        ui.cdpTextView.text = "$ctrCd-$brcCd$prjCd"
        ui.userNameTextView.text = userName

        val c = Calendar.getInstance()
        val mYear = c.get(Calendar.YEAR)
        ui.serviceTitleTextView.text = "Service status $mYear"

        viewModel.getHomeItem().observeOnce(this, Observer {
            it?.apply {
                ui.countTextView.text = NumberFormat.getNumberInstance(Locale.US).format(CHILD_COUNT ?: 0)
                ui.syncDateTextView.text = LAST_UPDATE_DATE?.toDateFormat() ?: "-"

                if (HAS_NEW_NOTICE == "Y") {
                    ui.noticeNewTextView.visibility = View.VISIBLE
                }
                else {
                    ui.noticeNewTextView.visibility = View.GONE
                }

                ui.notiContainer.removeAllViews()
                AnkoContext.createDelegate(ui.notiContainer).apply {
                    NOTI_ITEMS?.apply {
                        forEachIndexed { _, noti ->
                            linearLayout {
                                gravity = Gravity.CENTER_VERTICAL
                                padding = dip(5)

                                textView("■") { textSize = dip(2).toFloat() }
                                space {  }.lparams(width = dimen(R.dimen.px10))
                                textView(noti.TTL)
                            }
                        }
                    }
                }

                CIF_STATE?.apply {
                    ui.cifExecTextView.text = E_COUNT ?: "-"
                    ui.cifPlanTextView.text = P_COUNT ?: "-"
                    ui.cifPcTextView.text = (PC ?: "-") + "%"
                    ui.cifFromTextView.text = FROM_DATE ?: "-"
                    ui.cifToTextView.text = TO_DATE ?: "-"
                }

                APR_STATE?.apply {
                    ui.aprExecTextView.text = E_COUNT ?: "-"
                    ui.aprPlanTextView.text = P_COUNT ?: "-"
                    ui.aprPcTextView.text = (PC ?: "-") + "%"
                    ui.aprFromTextView.text = FROM_DATE ?: "-"
                    ui.aprToTextView.text = TO_DATE ?: "-"
                }

                ACL_STATE?.apply {
                    ui.aclExecTextView.text = E_COUNT ?: "-"
                    ui.aclPlanTextView.text = P_COUNT ?: "-"
                    ui.aclPcTextView.text = (PC ?: "-") + "%"
                    ui.aclFromTextView.text = FROM_DATE ?: "-"
                    ui.aclToTextView.text = TO_DATE ?: "-"
                }

                GML_STATE?.apply {
                    ui.gmlExecTextView.text = E_COUNT ?: "-"
                    ui.gmlPlanTextView.text = P_COUNT ?: "-"
                    ui.gmlPcTextView.text = (PC ?: "-") + "%"
                    ui.gmlFromTextView.text = FROM_DATE ?: "-"
                    ui.gmlToTextView.text = TO_DATE ?: "-"
                }
            }
        })

        ui.moreNoticeImageView.onClick {
            changeFragment.onChangeFragment(NoticeFragment.newInstance())
        }

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.title = "Good Neighbors"
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class FragmentUI : AnkoComponent<HomeFragment> {
        lateinit var cdpTextView: TextView
        lateinit var userNameTextView: TextView

        lateinit var serviceTitleTextView: TextView

        lateinit var syncDateTextView: TextView

        lateinit var countTextView: TextView

        lateinit var notiContainer: LinearLayout

        lateinit var noticeNewTextView: TextView
        lateinit var moreNoticeImageView: ImageView

        lateinit var cifPcTextView: TextView
        lateinit var cifExecTextView: TextView
        lateinit var cifPlanTextView: TextView
        lateinit var cifFromTextView: TextView
        lateinit var cifToTextView: TextView

        lateinit var aprPcTextView: TextView
        lateinit var aprExecTextView: TextView
        lateinit var aprPlanTextView: TextView
        lateinit var aprFromTextView: TextView
        lateinit var aprToTextView: TextView

        lateinit var aclPcTextView: TextView
        lateinit var aclExecTextView: TextView
        lateinit var aclPlanTextView: TextView
        lateinit var aclFromTextView: TextView
        lateinit var aclToTextView: TextView

        lateinit var gmlPcTextView: TextView
        lateinit var gmlExecTextView: TextView
        lateinit var gmlPlanTextView: TextView
        lateinit var gmlFromTextView: TextView
        lateinit var gmlToTextView: TextView

        override fun createView(ui: AnkoContext<HomeFragment>) = with(ui) {
            scrollView {
                verticalLayout {

                    verticalLayout {
                        padding = dimen(R.dimen.px20)
                        backgroundColorResource = R.color.colorPrimary

                        linearLayout {
                            linearLayout {
                                imageView {
                                    imageResource = R.drawable.main_cdp
                                }.lparams(width = dimen(R.dimen.px40), height = dimen(R.dimen.px40)) {
                                    gravity = Gravity.CENTER
                                }

                                space {}.lparams(width = dimen(R.dimen.px10))

                                textView("CDP") {
                                    setTypeface(null, Typeface.BOLD)
                                    gravity = Gravity.CENTER_VERTICAL
                                    textColorResource = R.color.colorWhite
                                }.lparams(width = wrapContent, height = dimen(R.dimen.px70))

                                space {}.lparams(width = dimen(R.dimen.px10))

                                cdpTextView = textView {
                                    setTypeface(null, Typeface.BOLD)
                                    gravity = Gravity.CENTER_VERTICAL
                                    textColorResource = R.color.colorYellow
                                }.lparams(width = wrapContent, height = dimen(R.dimen.px70))

                            }.lparams(width = 0, weight = 1f)
                            linearLayout {
                                imageView {
                                    imageResource = R.drawable.main_children
                                }.lparams(width = dimen(R.dimen.px40), height = dimen(R.dimen.px40)) {
                                    gravity = Gravity.CENTER
                                }

                                space {}.lparams(width = dimen(R.dimen.px10))

                                countTextView = textView("-") {
                                    setTypeface(null, Typeface.BOLD)
                                    gravity = Gravity.CENTER_VERTICAL
                                    textColorResource = R.color.colorYellow
                                }.lparams(width = wrapContent, height = dimen(R.dimen.px70))

                                space {}.lparams(width = dimen(R.dimen.px10))

                                textView(R.string.label_children) {
                                    setTypeface(null, Typeface.BOLD)
                                    gravity = Gravity.CENTER_VERTICAL
                                    textColorResource = R.color.colorWhite
                                }.lparams(width = wrapContent, height = dimen(R.dimen.px70))
                            }.lparams(width = 0, weight = 1f)
                        }.lparams(width = matchParent, height = wrapContent)
                        linearLayout {
                            linearLayout {
                                imageView {
                                    imageResource = R.drawable.main_staff
                                }.lparams(width = dimen(R.dimen.px40), height = dimen(R.dimen.px40)) {
                                    gravity = Gravity.CENTER
                                }

                                space {}.lparams(width = dimen(R.dimen.px10))

                                textView(R.string.label_staff) {
                                    setTypeface(null, Typeface.BOLD)
                                    gravity = Gravity.CENTER_VERTICAL
                                    textColorResource = R.color.colorWhite
                                }.lparams(width = wrapContent, height = dimen(R.dimen.px70))

                                space {}.lparams(width = dimen(R.dimen.px10))

                                userNameTextView = textView("-") {
                                    setTypeface(null, Typeface.BOLD)
                                    gravity = Gravity.CENTER_VERTICAL
                                    textColorResource = R.color.colorYellow
                                }.lparams(width = wrapContent, height = dimen(R.dimen.px70))
                            }.lparams(width = 0, weight = 1f)
                            linearLayout {
                                imageView {
                                    imageResource = R.drawable.main_pc
                                }.lparams(width = dimen(R.dimen.px40), height = dimen(R.dimen.px40)) {
                                    gravity = Gravity.CENTER
                                }

                                space {}.lparams(width = dimen(R.dimen.px10))

                                textView("PC->M") {
                                    setTypeface(null, Typeface.BOLD)
                                    gravity = Gravity.CENTER_VERTICAL
                                    textColorResource = R.color.colorWhite
                                }.lparams(width = wrapContent, height = dimen(R.dimen.px70))

                                space {}.lparams(width = dimen(R.dimen.px10))

                                syncDateTextView = textView("-") {
                                    setTypeface(null, Typeface.BOLD)
                                    gravity = Gravity.CENTER_VERTICAL
                                    textColorResource = R.color.colorYellow
                                }.lparams(width = wrapContent, height = dimen(R.dimen.px70))
                            }.lparams(width = 0, weight = 1f)
                        }.lparams(width = matchParent, height = wrapContent)
                    }

                    linearLayout {
                        gravity = Gravity.CENTER_VERTICAL
                        leftPadding = dimen(R.dimen.px20)
                        rightPadding = dimen(R.dimen.px20)
                        backgroundColorResource = R.color.colorLightGray

                        textView(R.string.label_notification) {
                            typeface = Typeface.DEFAULT_BOLD
                        }

                        noticeNewTextView = textView(R.string.label_new) {
                            typeface = Typeface.DEFAULT_BOLD
                            textColorResource = R.color.colorAccent
                            textSize = 12f
                            visibility = View.GONE
                        }.lparams { leftMargin = dip(10) }

                        space {}.lparams(width = 0, weight = 1f)

                        moreNoticeImageView =  imageView {
                            imageResource = R.drawable.more
                        }.lparams(width = dimen(R.dimen.px35), height = dimen(R.dimen.px35))

                    }.lparams(width = matchParent, height = dimen(R.dimen.px70))

                    notiContainer = verticalLayout {
                        padding = dimen(R.dimen.px20)
                    }

                    serviceTitleTextView = textView(R.string.label_service_status) {
                        gravity = Gravity.CENTER_VERTICAL
                        leftPadding = dimen(R.dimen.px20)
                        backgroundColorResource = R.color.colorLightGray
                        typeface = Typeface.DEFAULT_BOLD
                    }.lparams(width = matchParent, height = dimen(R.dimen.px70))

                    verticalLayout {
                        linearLayout {
                            gravity = Gravity.CENTER_VERTICAL
                            padding = dip(20)

                            textView("CIF") {
                                typeface = Typeface.DEFAULT_BOLD
                            }.lparams(width = 0, weight = 1f)

                            cifPcTextView = textView {
                                gravity = Gravity.CENTER
                                textColorResource = R.color.colorAccent
                            }.lparams(width = 0, weight = 1f)

                            verticalLayout {
                                linearLayout {
                                    cifExecTextView = textView {
                                        typeface = Typeface.DEFAULT_BOLD
                                        textColorResource = R.color.colorAccent
                                    }
                                    textView(" / ")
                                    cifPlanTextView = textView {}
                                    textView ("  children")
                                }
                                linearLayout {
                                    cifFromTextView = textView {}
                                    textView(" ~ ")
                                    cifToTextView = textView {}
                                }
                            }.lparams(width = 0, weight = 4f)
                        }

                        linearLayout {
                            gravity = Gravity.CENTER_VERTICAL
                            padding = dip(20)
                            backgroundColorResource = R.color.colorBgLiteGray

                            textView("APR") {
                                typeface = Typeface.DEFAULT_BOLD
                            }.lparams(width = 0, weight = 1f)

                            aprPcTextView = textView {
                                gravity = Gravity.CENTER
                                textColorResource = R.color.colorAccent
                            }.lparams(width = 0, weight = 1f)

                            verticalLayout {
                                linearLayout {
                                    aprExecTextView = textView {
                                        typeface = Typeface.DEFAULT_BOLD
                                        textColorResource = R.color.colorAccent
                                    }
                                    textView(" / ")
                                    aprPlanTextView = textView {}
                                    textView ("  children")
                                }
                                linearLayout {
                                    aprFromTextView = textView {}
                                    textView(" ~ ")
                                    aprToTextView = textView {}
                                }
                            }.lparams(width = 0, weight = 4f)
                        }

                        linearLayout {
                            gravity = Gravity.CENTER_VERTICAL
                            padding = dip(20)

                            textView("ACL") {
                                typeface = Typeface.DEFAULT_BOLD
                            }.lparams(width = 0, weight = 1f)

                            aclPcTextView = textView {
                                gravity = Gravity.CENTER
                                textColorResource = R.color.colorAccent
                            }.lparams(width = 0, weight = 1f)

                            verticalLayout {
                                linearLayout {
                                    aclExecTextView = textView {
                                        typeface = Typeface.DEFAULT_BOLD
                                        textColorResource = R.color.colorAccent
                                    }
                                    textView(" / ")
                                    aclPlanTextView = textView {}
                                    textView ("  children")
                                }
                                linearLayout {
                                    aclFromTextView = textView {}
                                    textView(" ~ ")
                                    aclToTextView = textView {}
                                }
                            }.lparams(width = 0, weight = 4f)
                        }

                        linearLayout {
                            gravity = Gravity.CENTER_VERTICAL
                            padding = dip(20)
                            backgroundColorResource = R.color.colorBgLiteGray

                            textView("GML") {
                                typeface = Typeface.DEFAULT_BOLD
                            }.lparams(width = 0, weight = 1f)

                            gmlPcTextView = textView {
                                gravity = Gravity.CENTER
                                textColorResource = R.color.colorAccent
                            }.lparams(width = 0, weight = 1f)

                            verticalLayout {
                                linearLayout {
                                    gmlExecTextView = textView {
                                        typeface = Typeface.DEFAULT_BOLD
                                        textColorResource = R.color.colorAccent
                                    }
                                    textView(" / ")
                                    gmlPlanTextView = textView {}
                                    textView ("  children")
                                }
                                linearLayout {
                                    gmlFromTextView = textView {}
                                    textView(" ~ ")
                                    gmlToTextView = textView {}
                                }
                            }.lparams(width = 0, weight = 4f)
                        }
                    }
                }
            }
        }
    }
}
