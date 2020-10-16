package kr.goodneighbors.cms.ui.childlist

import android.app.Activity
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.DialogFragment
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kr.goodneighbors.cms.R
import kr.goodneighbors.cms.common.Constants
import kr.goodneighbors.cms.extensions.convertDateFormat
import kr.goodneighbors.cms.extensions.observeOnce
import kr.goodneighbors.cms.service.model.DuplicateChildItem
import kr.goodneighbors.cms.service.viewmodel.CifViewModel
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.UI
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.dimen
import org.jetbrains.anko.dip
import org.jetbrains.anko.imageView
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.rightPadding
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.space
import org.jetbrains.anko.support.v4.viewPager
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textView
import org.jetbrains.anko.topPadding
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent
import java.io.File

class DuplicatedChildrenDialogFragment : DialogFragment() {
    companion object {
        fun newInstance(firstName: String, middleName: String, lastName: String, gender: String, birth: String): DuplicatedChildrenDialogFragment {
            val fragment = DuplicatedChildrenDialogFragment()
            val args = Bundle()
            args.putString("firstName", firstName)
            args.putString("middleName", middleName)
            args.putString("lastName", lastName)
            args.putString("gender", gender)
            args.putString("birth", birth)

            fragment.arguments = args
            return fragment
        }
    }

    private val viewModel: CifViewModel by lazy {
        CifViewModel()
    }

    private val ui = FragmentUI()

    private lateinit var firstName: String
    private lateinit var middleName: String
    private lateinit var lastName: String
    private lateinit var gender: String
    private lateinit var birth: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firstName = arguments!!.getString("firstName")
        middleName = arguments!!.getString("middleName")
        lastName = arguments!!.getString("lastName")
        gender = arguments!!.getString("gender")
        birth = arguments!!.getString("birth")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val v = ui.createView(AnkoContext.create(requireContext(), this))

        ui.cancelButtonTextView.onClick {
            val data = Intent()
            data.putExtra("continue", false)
            targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, data)
            dismiss()
        }

        ui.saveButtonTextView.onClick {
            val data = Intent()
            data.putExtra("continue", true)
            targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, data)
            dismiss()
        }

        viewModel.findAllDuplicateChildren(firstName, middleName, lastName, gender, birth).observeOnce(this, Observer {
            it?.apply {
                ui.duplicateViewPager.adapter = DuplicationChildrenPageAdaptor(context!!, this)
            }
        })
        return v
    }

    override fun onStart() {
        super.onStart()
        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setCanceledOnTouchOutside(false)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class FragmentUI : AnkoComponent<DuplicatedChildrenDialogFragment> {
        lateinit var cancelButtonTextView: TextView
        lateinit var saveButtonTextView: TextView

        lateinit var duplicateViewPager: ViewPager

        override fun createView(ui: AnkoContext<DuplicatedChildrenDialogFragment>) = with(ui) {
            verticalLayout {
                verticalLayout {
                    topPadding = dimen(R.dimen.px40)
                    leftPadding = dimen(R.dimen.px40)
                    rightPadding = dimen(R.dimen.px40)

                    linearLayout {
                        minimumHeight = dimen(R.dimen.px70)

                        textView(R.string.label_registered_child) {
                            setTypeface(null, Typeface.BOLD)
                            textColorResource = R.color.colorPrimary
                            gravity = Gravity.CENTER_VERTICAL
                        }
                    }
                    space {  }.lparams(width = matchParent, height = dimen(R.dimen.px30))


                    duplicateViewPager = viewPager {

                    }.lparams(width = matchParent, height = wrapContent)

                }.lparams(width = matchParent, height = dip(500))
                space { }.lparams(width = matchParent, height = dimen(R.dimen.px40))

                linearLayout {
                    minimumHeight = dimen(R.dimen.px80)

                    cancelButtonTextView = textView(R.string.label_back_to_cif_list) {
                        backgroundResource = R.drawable.layout_border
                        setTypeface(null, Typeface.BOLD)
                        textColorResource = R.color.colorBlack
                        gravity = Gravity.CENTER
                    }.lparams(width = 0, height = matchParent, weight = 1f) {
                        bottomMargin = dip(-1)
                        leftMargin = dip(-1)
                        rightMargin = dip(-1)
                    }

                    saveButtonTextView = textView(R.string.label_back_to_register) {
                        backgroundResource = R.drawable.layout_border
                        setTypeface(null, Typeface.BOLD)
                        textColorResource = R.color.colorAccent
                        gravity = Gravity.CENTER
                    }.lparams(width = 0, height = matchParent, weight = 1f) {
                        bottomMargin = dip(-1)
                        rightMargin = dip(-1)
                    }
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class DuplicationChildrenPageAdaptor(private val context: Context, private val items: List<DuplicateChildItem>): PagerAdapter() {
        private val sdMain = Environment.getExternalStorageDirectory()
        private val contentsRootDir = File("$sdMain/${Constants.DIR_HOME}/${Constants.DIR_CONTENTS}")

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object` as View
        }

        override fun getCount(): Int {
            return items.size
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val item = items[position]

            var itemImageView:ImageView ?= null
            val view = context.UI {
                verticalLayout {
                    lparams(width = matchParent, height = wrapContent)
                    itemImageView = imageView {

                    }.lparams(width = dimen(R.dimen.px420)) {
                        gravity = Gravity.CENTER
                    }

                    space {  }.lparams(width = matchParent, height = dimen(R.dimen.px40))

                    linearLayout {
                        minimumHeight = dimen(R.dimen.px70)

                        textView(R.string.label_child_code){}.lparams(width = 0, height = wrapContent, weight = 0.4f)
                        textView("${item.CTR_CD}-${item.BRC_CD}${item.PRJ_CD}-${item.CH_CD}"){}.lparams(width = 0, height = wrapContent, weight = 0.6f)
                    }
                    linearLayout {
                        minimumHeight = dimen(R.dimen.px70)

                        textView(R.string.label_name){}.lparams(width = 0, height = wrapContent, weight = 0.4f)
                        textView("${item.CH_EFNM} ${item.CH_EMNM?:""} ${item.CH_ELNM}"){}.lparams(width = 0, height = wrapContent, weight = 0.6f)
                    }
                    linearLayout {
                        minimumHeight = dimen(R.dimen.px70)

                        textView(R.string.label_gender){}.lparams(width = 0, height = wrapContent, weight = 0.4f)
                        textView(item.GNDR){}.lparams(width = 0, height = wrapContent, weight = 0.6f)
                    }
                    linearLayout {
                        minimumHeight = dimen(R.dimen.px70)

                        textView(R.string.label_birthdate){}.lparams(width = 0, height = wrapContent, weight = 0.4f)
                        textView(item.BDAY?.convertDateFormat()){}.lparams(width = 0, height = wrapContent, weight = 0.6f)
                    }
                }
            }.view


            if (item.FILE_PATH.isNullOrBlank()) {
                Glide.with(view).load(R.drawable.icon_2)
                        .apply(RequestOptions.skipMemoryCacheOf(true))
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                        .into(itemImageView!!)
            }
            else {
                val imageFile = File(contentsRootDir, item.FILE_PATH ?: "")

                if (imageFile.exists()) {
                    Glide.with(view).load(imageFile)
                            .apply(RequestOptions.skipMemoryCacheOf(true))
                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                            .into(itemImageView!!)
                } else {
                    Glide.with(view).load(R.drawable.icon_2)
                            .apply(RequestOptions.skipMemoryCacheOf(true))
                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                            .into(itemImageView!!)
                }
            }

            container.addView(view)

            return view
        }


        override fun destroyItem(parent: ViewGroup, position: Int, `object`: Any) {
            parent.removeView(`object` as View)
        }

    }
}