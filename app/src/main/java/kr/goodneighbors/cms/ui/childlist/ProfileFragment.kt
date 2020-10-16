@file:Suppress("LocalVariableName")

package kr.goodneighbors.cms.ui.childlist


import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.hardware.Camera
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.GsonBuilder
import kr.goodneighbors.cms.R
import kr.goodneighbors.cms.common.Constants
import kr.goodneighbors.cms.extensions.circleImageView
import kr.goodneighbors.cms.extensions.convertDateFormat
import kr.goodneighbors.cms.extensions.extension
import kr.goodneighbors.cms.extensions.getRealPath
import kr.goodneighbors.cms.extensions.getStringValue
import kr.goodneighbors.cms.extensions.getValue
import kr.goodneighbors.cms.extensions.isNetworkAvailable
import kr.goodneighbors.cms.extensions.observeOnce
import kr.goodneighbors.cms.extensions.setItem
import kr.goodneighbors.cms.extensions.setSelectKey
import kr.goodneighbors.cms.extensions.toDateFormat
import kr.goodneighbors.cms.extensions.viewsRecursive
import kr.goodneighbors.cms.service.entities.CH_CUSL_INFO
import kr.goodneighbors.cms.service.model.CounselingListItem
import kr.goodneighbors.cms.service.model.ProfileViewItem
import kr.goodneighbors.cms.service.viewmodel.ProfileViewModel
import kr.goodneighbors.cms.ui.BaseActivityFragment
import kr.goodneighbors.cms.ui.DialogImageViewFragment
import kr.goodneighbors.cms.ui.MapsVillageActivity
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.UI
import org.jetbrains.anko.allCaps
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.bottomPadding
import org.jetbrains.anko.button
import org.jetbrains.anko.dimen
import org.jetbrains.anko.dip
import org.jetbrains.anko.editText
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.imageView
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.rightPadding
import org.jetbrains.anko.scrollView
import org.jetbrains.anko.sdk25.coroutines.onCheckedChange
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.space
import org.jetbrains.anko.spinner
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.support.v4.viewPager
import org.jetbrains.anko.switch
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textResource
import org.jetbrains.anko.textView
import org.jetbrains.anko.topPadding
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.view
import org.jetbrains.anko.wifiManager
import org.jetbrains.anko.wrapContent
import org.jetbrains.anko.yesButton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

@Suppress("PrivatePropertyName")
class ProfileFragment : BaseActivityFragment() {
    companion object {
        const val REQUEST_SIBLING_SPONSORSHIP = 1

        const val REQUEST_IMAGE_FROM_GALLERY = 100
        const val REQUEST_IMAGE_FROM_CAMERA = 101

        fun newInstance(chrcp_no: String): ProfileFragment {
            val fragment = ProfileFragment()
            val args = Bundle()
            args.putString("chrcp_no", chrcp_no)

            fragment.arguments = args
            return fragment
        }
    }

    private val logger: Logger by lazy {
        LoggerFactory.getLogger(ProfileFragment::class.java)
    }

    private val ui = FragmentUI()

    private val viewModel: ProfileViewModel by lazy {
        ProfileViewModel()
    }

    private lateinit var chrcp_no: String

    private var profile: ProfileViewItem? = null

    private var tabPosition = 1
    private var isCounselingEditable = false

    private val sdMain = Environment.getExternalStorageDirectory()
    private val contentsRootDir = File("$sdMain/${Constants.DIR_HOME}/${Constants.DIR_CONTENTS}")

    private var mCurrentPhotoPath: String = ""

    private var counselingFile: File? = null
    private lateinit var defaultColorList: ColorStateList

    private val IMEI: String by lazy {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        sharedPref.getString("GN_ID", "") ?: ""
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        chrcp_no = arguments!!.getString("chrcp_no") ?: ""
        logger.debug("onCreate : chrcp_no = $chrcp_no")

        viewModel.getProfileData().observe(this, Observer { viewItem ->
            logger.debug("onCreate : viewModel.getProfileData() = $viewItem")
            profile = viewItem
            if (viewItem != null) {
//                headerContainer
                if (viewItem.CH_STCD == "6") {
                    ui.headerContainer.backgroundColorResource = R.color.colorError
                } else {
                    ui.headerContainer.backgroundColorResource = R.color.colorPrimary
                }

                ui.nameTextView.text = viewItem.CHILD_NAME
                ui.childCodeTextView.text = viewItem.CHILD_CODE

                if (viewItem.BF_CHRCP_NO == null) {
                    ui.childCodeLinkTextView.visibility = View.GONE
                } else {
                    ui.childCodeLinkTextView.visibility = View.VISIBLE
                    ui.childCodeLinkTextView.text = "Pre. ${viewItem.BF_CHILD_CODE}"
                }

                val description = ArrayList<String>()
                val genderString = when (viewItem.GNDR) {
                    "F" -> "/Female"
                    "M" -> "/Male"
                    else -> ""
                }
                description.add("${viewItem.BDAY?.convertDateFormat()}(${viewItem.AGE})$genderString")
                viewItem.SCHL_NM?.apply { description.add(this) }
                viewItem.VLG_NM?.apply { description.add(this) }

                val guardian = ArrayList<String>()
                if (!viewItem.MGDN_CD_NM.isNullOrBlank()) guardian.add(viewItem.MGDN_CD_NM!!)
                if (!viewItem.MGDN_NM.isNullOrBlank()) guardian.add(viewItem.MGDN_NM!!)

                description.add("Guardian: ${guardian.joinToString(", ")}")

                ui.descTextView.text = description.joinToString("\n")

                ui.registeredDateTextView.text = viewItem.CIF_APRV_DT?.convertDateFormat() ?: ""
                ui.recentReportDateTextView.text = viewItem.APRV_DT?.convertDateFormat() ?: ""
                ui.counselingDateTextView.text = viewItem.CH_CUSL_INFO_REG_DT?.toDateFormat() ?: ""

                val cases = ArrayList<String>()
                if (!viewItem.CASE1_NM.isNullOrBlank()) cases.add(viewItem.CASE1_NM!!)
                if (!viewItem.CASE2_NM.isNullOrBlank()) cases.add(viewItem.CASE2_NM!!)
                if (!viewItem.CASE3_NM.isNullOrBlank()) cases.add(viewItem.CASE3_NM!!)
                ui.specialCaseTextView.text = cases.joinToString(", ")

                ui.childNameTextView.text = viewItem.CHILD_NAME
                ui.birthDateTextView.text = "${viewItem.BDAY?.convertDateFormat()}(${viewItem.AGE})"

                ui.genderTextView.text = viewItem.GNDR ?: ""

                ui.levelOfHealthTextView.text = viewItem.BMI_NM ?: ""

                ui.villageTextView.text = viewItem.VLG_NM ?: ""

                val addressItems = ArrayList<String>()
                if (!viewItem.HS_ADDR.isNullOrBlank()) addressItems.add(viewItem.HS_ADDR!!)
                if (!viewItem.HS_ADDR_DTL.isNullOrBlank()) addressItems.add(viewItem.HS_ADDR_DTL!!)
                ui.addressTextView.text = addressItems.joinToString(" ")

                val disabilityItems = ArrayList<String>()
                if (!viewItem.DISB_NM.isNullOrBlank()) disabilityItems.add(viewItem.DISB_NM!!)
                if (!viewItem.ILNS_NM.isNullOrBlank()) disabilityItems.add(viewItem.ILNS_NM!!)
                ui.disabillityTextView.text = disabilityItems.joinToString(" / ")

                val schoolItems = ArrayList<String>()
                if (!viewItem.SCTP_NM.isNullOrBlank()) schoolItems.add(viewItem.SCTP_NM!!)
                if (!viewItem.SCHL_NM.isNullOrBlank()) schoolItems.add(viewItem.SCHL_NM!!)
                if (!viewItem.GRAD.isNullOrBlank()) schoolItems.add(viewItem.GRAD!!)
                ui.schoolTextView.text = schoolItems.joinToString(" / ")

                val family = ArrayList<String>()
                if (viewItem.FA_LTYN == "Y") family.add(this.getString(R.string.label_father))
                if (viewItem.MO_LTYN == "Y") family.add(this.getString(R.string.label_mother))


                val VAL_EBRO_LTNUM = viewItem.EBRO_LTNUM?.toIntOrNull()?:0
                val VAL_YBRO_LTNUM = viewItem.YBRO_LTNUM?.toIntOrNull()?:0
                val VAL_BRO_LTNUM = VAL_EBRO_LTNUM + VAL_YBRO_LTNUM
                if (VAL_BRO_LTNUM > 0) family.add(this.getString(R.string.label_brother)+"(${VAL_BRO_LTNUM})")
                //if (!viewItem.EBRO_LTNUM.isNullOrBlank() && viewItem.EBRO_LTNUM!! > "0") family.add("Brother(${viewItem.EBRO_LTNUM!!})")

                val VAL_ESIS_LTNUM = viewItem.ESIS_LTNUM?.toIntOrNull()?:0
                val VAL_YSIS_LTNUM = viewItem.YSIS_LTNUM?.toIntOrNull()?:0
                val VAL_SIS_LTNUM = VAL_ESIS_LTNUM + VAL_YSIS_LTNUM
                if (VAL_SIS_LTNUM > 0) family.add(this.getString(R.string.label_sister)+"(${VAL_SIS_LTNUM})")
                //if (!viewItem.ESIS_LTNUM.isNullOrBlank() && viewItem.ESIS_LTNUM!! > "0") family.add("Sister(${viewItem.ESIS_LTNUM!!})")
                ui.familyTextView.text = family.joinToString(", ")

                val siblings = ArrayList<String>()
                if (!viewItem.SIBLING1.isNullOrBlank()) siblings.add(viewItem.SIBLING1!!)
                if (!viewItem.SIBLING2.isNullOrBlank()) siblings.add(viewItem.SIBLING2!!)
                ui.siblingsTextView.text = siblings.joinToString(", ")
                ui.siblingsImageView.visibility = if (siblings.isEmpty()) View.GONE else View.VISIBLE

                val viewPager = ui.profileImageViewPager
                val images = viewItem.GENERAL_FILE_PATH ?: listOf("")
                viewPager.adapter = ProfileImagePageAdaptor(context!!, images)
                viewPager.clipToPadding = false
                viewPager.leftPadding = 20
                viewPager.rightPadding = 20
                viewPager.pageMargin = 10

                val f = viewItem.THUMB_FILE_PATH?.let {
                    val ff = File(contentsRootDir, it)
                    if (ff.exists()) {
                        ff
                    } else null
                }

                if (f == null) {
                    Glide.with(this@ProfileFragment).load(R.drawable.m_childlist)
                            .apply(RequestOptions.skipMemoryCacheOf(true))
                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                            .into(ui.thumbnameImageView)
                } else {
                    Glide.with(this@ProfileFragment).load(f)
                            .apply(RequestOptions.skipMemoryCacheOf(true))
                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                            .into(ui.thumbnameImageView)
                }

                if (viewItem.TEL_NO.isNullOrBlank()) {
                    ui.telephoneImageView.visibility = View.GONE
                } else {
                    ui.telephoneImageView.visibility = View.VISIBLE
                    ui.telephoneImageView.onClick {
                        alert(viewItem.TEL_NO!!) {
                            yesButton {

                            }
                        }.show()
                    }
                }

                if (viewItem.AGE?.toInt()?.compareTo(18) ?: -1 > -1 || viewItem.PLAN_YN != null) {
                    when (viewItem.PLAN_YN) {
                        "Y" -> {
                            ui.planTitleTextView.textResource = R.string.label_future_plan
                            ui.planDetailTitleTextView.textResource = R.string.label_detail_plan
                            ui.planTextView.text = viewItem.FTPLN_NM
                            ui.planDetailTextView.text = viewItem.FTPLN_DTL
                        }
                        "N" -> {
                            ui.planTitleTextView.textResource = R.string.label_continue_spon_reason
                            ui.planDetailTitleTextView.textResource = R.string.label_detail_reason
                            ui.planTextView.text = viewItem.CTNSPN_RNNM
                            ui.planDetailTextView.text = viewItem.CTNSPN_DTL
                        }
                        else -> {
                            ui.planTitleTextView.textResource = R.string.label_future_plan
                            ui.planDetailTitleTextView.textResource = R.string.label_detail_plan
                            ui.planTextView.text = ""
                            ui.planDetailTextView.text = ""
                        }
                    }

                    ui.planContainer.visibility = View.VISIBLE
                } else {
                    ui.planContainer.visibility = View.GONE
                }


                if (viewItem.DROP_RCP_NO == null) {
                    ui.registerButton.visibility = View.VISIBLE
                    ui.registerButton.isEnabled = true
                    ui.registerButton.backgroundColorResource = R.color.colorBrown

                    ui.reintakeContainer.visibility = View.GONE

                    ui.remarkTextView.text = viewItem.REMRK_ENG ?: ""
                } else {
                    if (viewItem.DROP_RPT_STCD == "1") {
                        ui.dropoutContainer.visibility = View.VISIBLE
                        ui.dropoutTextView.text = "Y"
                        ui.dropoutReasonTextView.text = viewItem.DROP_RNNM ?: ""

                        ui.registerButton.visibility = View.GONE
                        ui.reintakeContainer.visibility = View.VISIBLE

                        ui.remarkTextView.text = viewItem.DROPOUT_REMRK_ENG ?: ""
                    } else {
                        ui.registerButton.visibility = View.VISIBLE
                        ui.registerButton.isEnabled = false
                        ui.registerButton.backgroundColorResource = R.color.colorGray

                        ui.reintakeContainer.visibility = View.GONE

                        ui.remarkTextView.text = viewItem.REMRK_ENG ?: ""
                    }
                }

                ui.editCounselingInterviewPlaceSpinner.setItem(items = viewItem.codeInterviewPlace, hint = "")
                ui.editCounselingRelationshipSpinner.setItem(items = viewItem.codeRelationship, hint = "")
            }
        })

        viewModel.getCounselingList().observe(this, Observer { counselingListItems ->
            loadCounselingList(counselingListItems)
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = ui.createView(AnkoContext.create(requireContext(), this))

        activity?.title = "Profile"

        defaultColorList = ui.emptyTextView.textColors

        setHasOptionsMenu(true)

        logger.debug("onCreateView : chrcp_no = $chrcp_no")

        ui.siblingsImageView.onClick {
            val ft = activity!!.supportFragmentManager.beginTransaction()
            val newFragment = SiblingSponsorshipDialogFragment.newInstance(chrcp_no = chrcp_no)
            newFragment.setTargetFragment(this@ProfileFragment, REQUEST_SIBLING_SPONSORSHIP)
            newFragment.show(ft, "SIBLINGS")
        }

        ui.reintakeSwitch.onCheckedChange { _, isChecked ->
            if (isChecked) {
                ui.reintakeEditContainer.visibility = View.VISIBLE
            } else {
                ui.reintakeEditContainer.visibility = View.GONE
            }
        }

        ui.reintakeButton.onClick {
            if (ui.reintakeReasonEditText.getStringValue().length < 30 || ui.reintakeReasonEditText.getStringValue().length > 2000) {
                toast(R.string.message_validate_range_30_2000)
            } else {
                if (profile != null) {
                    val id = profile!!.CHRCP_NO
                    val origin = profile!!.ORG_CHRCP_NO ?: id
                    changeFragment.onChangeFragment(CifFragment.newInstance(reintake_origin = origin, reintake_link = id, reintake_reason = ui.reintakeReasonEditText.getStringValue()))
                }
            }
        }

        ui.mapImageView.onClick {
            if (!profile!!.VLG_LAT.isNullOrBlank() && !profile!!.VLG_LONG.isNullOrBlank()) {
//                if (requireContext().wifiManager.isWifiEnabled) {
                if (requireContext().isNetworkAvailable()) {
                    startActivity<MapsVillageActivity>(
                            "name" to profile?.VLG_NM,
                            "lat" to profile?.VLG_LAT,
                            "lng" to profile?.VLG_LONG
                    )
                } else {
                    toast(R.string.message_wifi_disabled)
                }
            } else {
                toast(R.string.message_location_is_not_define)
            }

        }

        ui.editCounselingImageView.onClick {
            if (counselingFile != null && counselingFile!!.exists()) {
                val ft = activity!!.supportFragmentManager.beginTransaction()
                val newFragment = DialogImageViewFragment.newInstance(counselingFile!!.path)
                newFragment.show(ft, "counseling_edit_view")
            }
        }

        //ui.editCounselingImageGalleryImageView.onClick {
        //    val intent = Intent(Intent.ACTION_PICK)
        //    intent.setType("image/*")
        //    intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE)
        //
        //    startActivityForResult(intent, REQUEST_IMAGE_FROM_GALLERY)
        //}

        ui.editCounselingImageCameraImageView.onClick {
            captureCamera()
        }

        ui.editCounselingImageDeleteImageView.onClick {
            ui.editCounselingImageView.imageResource = R.drawable.icon_2
            counselingFile = null
        }

        viewModel.setId(chrcp_no)

        return v
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        logger.debug("onCreateOptionsMenu($menu)")
        activity?.menuInflater?.inflate(R.menu.toolbar_profile, menu)

        menu?.findItem(R.id.toolbar_profile_add_icon)?.setOnMenuItemClickListener {
            onClickEditButton()
            true
        }

        menu?.findItem(R.id.toolbar_profile_save_icon)?.setOnMenuItemClickListener {
            save()
            true
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        logger.debug("onPrepareOptionsMenu($menu) : $tabPosition")
        if (profile?.CH_STCD == "1") {
            when (tabPosition) {
                1 -> {
                    menu?.findItem(R.id.toolbar_profile_add_icon)?.isVisible = false
                    menu?.findItem(R.id.toolbar_profile_save_icon)?.isVisible = false
                }
                2 -> {
                    menu?.findItem(R.id.toolbar_profile_add_icon)?.isVisible = true
                    menu?.findItem(R.id.toolbar_profile_save_icon)?.isVisible = false
                }
                3 -> {
                    menu?.findItem(R.id.toolbar_profile_add_icon)?.isVisible = false
                    menu?.findItem(R.id.toolbar_profile_save_icon)?.isVisible = isCounselingEditable
                }
            }
        } else {
            menu?.findItem(R.id.toolbar_profile_add_icon)?.isVisible = false
            menu?.findItem(R.id.toolbar_profile_save_icon)?.isVisible = false
        }

        super.onPrepareOptionsMenu(menu)
    }

    @Suppress("unused")
    fun onBackPressed(): Boolean {
        return if (ui.counselingEditLayer.visibility == View.VISIBLE) {
            ui.counselingListLayer.visibility = View.VISIBLE
            ui.counselingEditLayer.visibility = View.GONE
            onChangeTab(2)
            viewModel.setCounselingListTrigger(Date().time)
            false
        } else {
            true
        }
    }

    fun onClickImageListener(item: CounselingListItem) {
        val sdMain = Environment.getExternalStorageDirectory()
        val targetDir = File("$sdMain/${Constants.DIR_HOME}/${Constants.DIR_CONTENTS}")

        if (!item.IMG_FP.isNullOrBlank() && !item.IMG_NM.isNullOrBlank()) {
            val thumnail = File(targetDir, "${item.IMG_FP}/${item.IMG_NM}")
            if (thumnail.exists()) {
                val ft = activity!!.supportFragmentManager.beginTransaction()
                val newFragment = DialogImageViewFragment.newInstance(thumnail.path)
                newFragment.show(ft, "counseling_list_view")
            }
        }
    }

    private fun onClickEditButton(item: CounselingListItem? = null) {

        if (item != null) { // 수정
            logger.debug("onClickEditButton($item)")
            ui.counselingListLayer.tag = item

            ui.editCounselingDateTextView.text = item.CUSL_DT
            ui.editCounselingDateTextView.tag = item.CUSL_DT

            ui.editCounselingCountOfVisitionTextView.text = item.RN.toString()
            ui.editCounselingInterviewPlaceSpinner.setSelectKey(item.INTPLC_CD)
            ui.editCounselingRelationshipSpinner.setSelectKey(item.RSPN_RLCD)
            ui.editCounselingContentEditText.setText(item.CUSL_CTS)

            val f = File(contentsRootDir, "${item.IMG_FP}/${item.IMG_NM}")
            if (f.exists()) {
                Glide.with(this).load(f)
                        .apply(RequestOptions.skipMemoryCacheOf(true))
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                        .into(ui.editCounselingImageView)
                counselingFile = f
            } else {
                ui.editCounselingImageView.imageResource = R.drawable.icon_2
                counselingFile = f
            }

            isCounselingEditable = item.CUSL_STCD != "1"

            initCounselingEditUI()
        } else { // 등록
            ui.counselingListLayer.tag = null

            logger.debug("===========================IMEI : $IMEI")
            viewModel.getNextCounselingIndex(chrcp_no, IMEI).observeOnce(this, Observer {
                val now = Date().time
                ui.editCounselingDateTextView.text = now.toDateFormat()
                ui.editCounselingDateTextView.tag = now.toDateFormat("yyyyMMdd")

                ui.editCounselingCountOfVisitionTextView.text = it?.VISIT ?: ""
                ui.editCounselingCountOfVisitionTextView.tag = it?.NEXT_SEQ ?: ""
                ui.editCounselingInterviewPlaceSpinner.setSelection(0)
                ui.editCounselingRelationshipSpinner.setSelection(0)
                ui.editCounselingContentEditText.setText("")
                ui.editCounselingImageView.imageResource = R.drawable.icon_2
                counselingFile = null

                isCounselingEditable = true
                initCounselingEditUI()
            })
        }


    }

    private fun initCounselingEditUI() {
        ui.counselingEditLayer.viewsRecursive.filter { it is Spinner || it is EditText }.forEach {
            it.isEnabled = isCounselingEditable
        }

        if (isCounselingEditable) {
            ui.editCounselingImageButtonContainer.visibility = View.VISIBLE
        } else {
            ui.editCounselingImageButtonContainer.visibility = View.GONE
        }

        ui.editCounselingInterviewPlaceTitleTextView.setTextColor(defaultColorList)
        ui.editCounselingRelationshipTitleTextView.setTextColor(defaultColorList)
        ui.editCounselingContentTitleTextView.setTextColor(defaultColorList)

        onChangeTab(3)
        ui.counselingListLayer.visibility = View.GONE
        ui.counselingEditLayer.visibility = View.VISIBLE

        requireActivity().invalidateOptionsMenu()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_SIBLING_SPONSORSHIP -> {
                if (resultCode == Activity.RESULT_OK && data?.extras != null) {
                    val s = data.extras?.getString("chrcp_no")
                    if (!s.isNullOrBlank()) {
                        changeFragment.onChangeFragment(ProfileFragment.newInstance(s))
                    }
                }
            }
            REQUEST_IMAGE_FROM_GALLERY -> {
                if (resultCode == Activity.RESULT_OK) {

                    if (data?.data != null) {
                        try {
                            val file = File(data.data.getRealPath(context!!))
                            val filePath = file.path
                            logger.debug("REQUEST_IMAGE_FROM_GALLERY : $filePath")
                            counselingFile = file
                            Glide.with(this).load(file)
                                    .apply(RequestOptions.skipMemoryCacheOf(true))
                                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                                    .into(ui.editCounselingImageView)


                        } catch (e: Exception) {
                            logger.error("REQUEST_IMAGE_FROM_GALLERY", e)
                        }
                    }
                }
            }
            REQUEST_IMAGE_FROM_CAMERA -> {
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        logger.debug("REQUEST_IMAGE_FROM_CAMERA : $mCurrentPhotoPath")
                        val originalFile = mCurrentPhotoPath

                        val sizedImageFile = createImageFile()
                        logger.debug("REQUEST_IMAGE_FROM_CAMERA resize: ${sizedImageFile.path}")

                        Glide.with(this)
                                .asBitmap()
                                .load(originalFile)
                                .into(object : SimpleTarget<Bitmap>(500, 667) {
                                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                        try {
                                            val out = FileOutputStream(sizedImageFile)
                                            resource.compress(Bitmap.CompressFormat.JPEG, 100, out)
                                            out.flush()
                                            out.close()

                                            Glide.with(this@ProfileFragment).load(sizedImageFile)
                                                    .apply(RequestOptions.skipMemoryCacheOf(true))
                                                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                                                    .into(ui.editCounselingImageView)
                                            counselingFile = sizedImageFile
                                        } catch (e: IOException) {
                                            e.printStackTrace()
                                        }
                                    }

                                    override fun onLoadFailed(errorDrawable: Drawable?) {
                                        logger.error("Glide.onLoadFailed : ", errorDrawable)
                                    }
                                })
                    } catch (e: Exception) {
                        logger.error("REQUEST_IMAGE_FROM_CAMERA : ", e)
                    }
                }
            }
        }
    }

    private fun loadCounselingList(items: List<CounselingListItem>?) {
        ui.counselingListLayer.removeAllViews()
        items?.apply {
            AnkoContext.createDelegate(ui.counselingListLayer).apply {
                verticalLayout {
                    forEach { item ->
                        var detailContainer: ViewGroup? = null

                        verticalLayout {
                            lparams(matchParent, wrapContent) {
                                topMargin = dimen(R.dimen.px20)
                            }

                            padding = dimen(R.dimen.px20)
                            backgroundColorResource = R.color.colorBgLiteGray

                            linearLayout {
                                gravity = Gravity.CENTER_VERTICAL
                                val counselingImageView = imageView {
                                    imageResource = R.drawable.icon_3

                                    onClick {
                                        onClickImageListener(item)
                                    }
                                }.lparams(width = dimen(R.dimen.px220), height = dimen(R.dimen.px150))

                                val f = File(contentsRootDir, "${item.IMG_FP}/${item.IMG_NM}")
                                if (f.exists()) {
                                    Glide.with(this@ProfileFragment).load(f)
                                            .apply(RequestOptions.skipMemoryCacheOf(true))
                                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                                            .into(counselingImageView)
                                } else {
                                    Glide.with(this@ProfileFragment).load(R.drawable.icon_3)
                                            .apply(RequestOptions.skipMemoryCacheOf(true))
                                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                                            .into(counselingImageView)
                                }

                                space {}.lparams(width = dimen(R.dimen.px20), height = matchParent)
                                verticalLayout {
                                    textView("${item.YYYYMM ?: ""} Counseling") {
                                        setTypeface(null, Typeface.BOLD)
                                    }
                                    textView("Counseling date:")
                                    textView(item.CUSL_DT?.convertDateFormat() ?: "")
                                }.lparams(width = 0, height = wrapContent, weight = 1f)

                                verticalLayout {
                                    gravity = Gravity.CENTER

                                    val detailToggleImageView = imageView {
                                        imageResource = R.drawable.select_4

                                    }.lparams(width = dimen(R.dimen.px35), height = dimen(R.dimen.px35)) {
                                        gravity = Gravity.CENTER
                                    }

                                    onClick {
                                        if (detailContainer?.visibility == View.VISIBLE) {
                                            detailContainer?.visibility = View.GONE
                                            detailToggleImageView.imageResource = R.drawable.select_4
                                        } else {
                                            detailContainer?.visibility = View.VISIBLE
                                            detailToggleImageView.imageResource = R.drawable.select_5
                                        }
                                    }
                                }.lparams(width = dimen(R.dimen.px46), height = dimen(R.dimen.px46))
                            }

                            detailContainer = verticalLayout {
                                visibility = View.GONE

                                textView(R.string.label_child_counseling_information) {
                                    setTypeface(null, Typeface.BOLD)
                                    gravity = Gravity.CENTER_VERTICAL
                                    backgroundColorResource = R.color.colorBgLiteGray
                                }.lparams(width = matchParent, height = dimen(R.dimen.px70))

                                verticalLayout {

                                    linearLayout {
                                        minimumHeight = dimen(R.dimen.px70)

                                        textView(R.string.label_no_of_visiting) {
                                            gravity = Gravity.CENTER_VERTICAL
                                            padding = dimen(R.dimen.px10)
                                            backgroundResource = R.drawable.layout_border
                                        }.lparams(width = 0, height = matchParent, weight = 1f) {
                                            rightMargin = dip(-1)
                                        }
                                        textView(item.RN?.toString() ?: "") {
                                            gravity = Gravity.CENTER_VERTICAL
                                            padding = dimen(R.dimen.px10)
                                            backgroundResource = R.drawable.layout_border
                                        }.lparams(width = 0, height = matchParent, weight = 1f) {
                                        }
                                    }
                                    linearLayout {
                                        minimumHeight = dimen(R.dimen.px70)

                                        textView(R.string.label_interview_place) {
                                            gravity = Gravity.CENTER_VERTICAL
                                            padding = dimen(R.dimen.px10)
                                            backgroundResource = R.drawable.layout_border
                                        }.lparams(width = 0, height = matchParent, weight = 1f) {
                                            rightMargin = dip(-1)
                                            topMargin = dip(-1)
                                        }
                                        textView(item.INTPLC_NM ?: "") {
                                            gravity = Gravity.CENTER_VERTICAL
                                            padding = dimen(R.dimen.px10)
                                            backgroundResource = R.drawable.layout_border
                                        }.lparams(width = 0, height = matchParent, weight = 1f) {
                                            topMargin = dip(-1)
                                        }
                                    }
                                    linearLayout {
                                        minimumHeight = dimen(R.dimen.px70)

                                        textView(R.string.label_interviewer_name) {
                                            gravity = Gravity.CENTER_VERTICAL
                                            padding = dimen(R.dimen.px10)
                                            backgroundResource = R.drawable.layout_border
                                        }.lparams(width = 0, height = matchParent, weight = 1f) {
                                            rightMargin = dip(-1)
                                            topMargin = dip(-1)
                                        }
                                        textView(item.INTVR_NM ?: "") {
                                            gravity = Gravity.CENTER_VERTICAL
                                            padding = dimen(R.dimen.px10)
                                            backgroundResource = R.drawable.layout_border
                                        }.lparams(width = 0, height = matchParent, weight = 1f) {
                                            topMargin = dip(-1)
                                        }
                                    }
                                    linearLayout {
                                        minimumHeight = dimen(R.dimen.px70)

                                        textView(R.string.label_relationship_with_child) {
                                            gravity = Gravity.CENTER_VERTICAL
                                            padding = dimen(R.dimen.px10)
                                            backgroundResource = R.drawable.layout_border
                                        }.lparams(width = 0, height = matchParent, weight = 1f) {
                                            rightMargin = dip(-1)
                                            topMargin = dip(-1)
                                        }
                                        textView(item.RSPN_RLNM ?: "") {
                                            gravity = Gravity.CENTER_VERTICAL
                                            padding = dimen(R.dimen.px10)
                                            backgroundResource = R.drawable.layout_border
                                        }.lparams(width = 0, height = matchParent, weight = 1f) {
                                            topMargin = dip(-1)
                                        }
                                    }
                                    linearLayout {
                                        minimumHeight = dimen(R.dimen.px70)

                                        textView(R.string.label_content_of_counseling) {
                                            gravity = Gravity.CENTER_VERTICAL
                                            padding = dimen(R.dimen.px10)
                                            backgroundResource = R.drawable.layout_border
                                        }.lparams(width = 0, height = matchParent, weight = 1f) {
                                            rightMargin = dip(-1)
                                            topMargin = dip(-1)
                                        }
                                        textView(item.CUSL_CTS ?: "") {
                                            gravity = Gravity.CENTER_VERTICAL
                                            padding = dimen(R.dimen.px10)
                                            backgroundResource = R.drawable.layout_border
                                        }.lparams(width = 0, height = matchParent, weight = 1f) {
                                            topMargin = dip(-1)
                                        }
                                    }
                                    linearLayout {
                                        minimumHeight = dimen(R.dimen.px70)

                                        textView(R.string.label_opinion_of_manager) {
                                            gravity = Gravity.CENTER_VERTICAL
                                            padding = dimen(R.dimen.px10)
                                            backgroundResource = R.drawable.layout_border
                                        }.lparams(width = 0, height = matchParent, weight = 1f) {
                                            rightMargin = dip(-1)
                                            topMargin = dip(-1)
                                        }
                                        textView(item.MNG_OPN ?: "") {
                                            gravity = Gravity.CENTER_VERTICAL
                                            padding = dimen(R.dimen.px10)
                                            backgroundResource = R.drawable.layout_border
                                        }.lparams(width = 0, height = matchParent, weight = 1f) {
                                            topMargin = dip(-1)
                                        }
                                    }
                                }

                                button(R.string.button_modify) {
                                    onClick {
                                        onClickEditButton(item)
                                    }
                                }
                            }
                        }
                    }
                    space { }.lparams(width = matchParent, height = dip(10))
                }
            }
        }
    }

    /**
     * position
     * 1 : profile
     * 2 : counseling list
     * 3 : counseling edit
     */
    fun onChangeTab(position: Int) {
        tabPosition = position
        logger.debug("onChangeTab($tabPosition)")
        requireActivity().invalidateOptionsMenu()

        if (position == 1) {
            activity!!.title = "Profile"
        }
        if (position == 2) {
            activity!!.title = "Counseling"
            viewModel.setCounselingListTrigger(Date().time)
        }
    }

    private fun captureCamera() {
        val state = Environment.getExternalStorageState()

        if (Environment.MEDIA_MOUNTED == state) {
            val camera = Camera.open()
            val parameters = camera.parameters
            val sizeList = parameters.supportedPictureSizes
            //카메라 SupportedPictureSize목록 출력 로그
//            Log.d(Constants.TAG, "--SupportedPictureSizeList Start--")
//            sizeList.forEach {
//                Log.d(Constants.TAG, "Width : ${it.width}, Height : ${it.height}")
//            }

//            Log.d(Constants.TAG, "--SupportedPictureSizeList End--")
            // 원하는 최적화 사이즈를 1280x720 으로 설정
//            val size = getOptimalPictureSize(parameters.supportedPictureSizes, 1280, 720)
            //val size = getOptimalPictureSize(parameters.supportedPictureSizes, 320, 240)
            val size = getOptimalPictureSize(parameters.supportedPictureSizes, 667, 500)
//            Log.d(Constants.TAG, "Selected Optimal Size : (" + size.width + ", " + size.height + ")")
            parameters.setPreviewSize(size.width, size.height)
            parameters.setPictureSize(size.width, size.height)
//            parameters.setPreviewSize(320, 240)
//            parameters.setPictureSize(320, 240)
//            parameters.setRotation(90)
//            parameters.jpegQuality = 80

            camera.parameters = parameters
            camera.release()

            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            if (takePictureIntent.resolveActivity(activity!!.packageManager) != null) {
                var photoFile: File? = null
                try {
                    photoFile = createImageFile()
                } catch (ex: IOException) {
                    logger.error("captureCamera Error", ex)
                }

                if (photoFile != null) {
                    val providerURI = FileProvider.getUriForFile(activity!!, "kr.goodneighbors.cms.provider", photoFile)

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerURI)

                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_FROM_CAMERA)
                }
            }
        } else {
            toast(R.string.message_inaccessible_storage)
            return
        }
    }

    private fun getOptimalPictureSize(sizeList: MutableList<Camera.Size>, width: Int, height: Int): Camera.Size {
        logger.debug("getOptimalPictureSize, 기준 width,height : ($width, $height)")
        var prevSize = sizeList[0]
        var optSize = sizeList[1]
        sizeList.forEach { size: Camera.Size ->
            // 현재 사이즈와 원하는 사이즈의 차이
            val diffWidth = Math.abs((size.width - width))
            val diffHeight = Math.abs((size.height - height))

            // 이전 사이즈와 원하는 사이즈의 차이
            val diffWidthPrev = Math.abs((prevSize.width - width))
            val diffHeightPrev = Math.abs((prevSize.height - height))

            // 현재까지 최적화 사이즈와 원하는 사이즈의 차이
            val diffWidthOpt = Math.abs((optSize.width - width))
            val diffHeightOpt = Math.abs((optSize.height - height))

            // 이전 사이즈보다 현재 사이즈의 가로사이즈 차이가 적을 경우 && 현재까지 최적화 된 세로높이 차이보다 현재 세로높이 차이가 적거나 같을 경우에만 적용
            if (diffWidth < diffWidthPrev && diffHeight <= diffHeightOpt) {
                optSize = size
                logger.debug("가로사이즈 변경 / 기존 가로사이즈 : " + prevSize.width + ", 새 가로사이즈 : " + optSize.width)
            }
            // 이전 사이즈보다 현재 사이즈의 세로사이즈 차이가 적을 경우 && 현재까지 최적화 된 가로길이 차이보다 현재 가로길이 차이가 적거나 같을 경우에만 적용
            if (diffHeight < diffHeightPrev && diffWidth <= diffWidthOpt) {
                optSize = size
                logger.debug("세로사이즈 변경 / 기존 세로사이즈 : " + prevSize.height + ", 새 세로사이즈 : " + optSize.height)
            }

            // 현재까지 사용한 사이즈를 이전 사이즈로 지정
            prevSize = size
        }
        logger.debug("결과 OptimalPictureSize : " + optSize.width + ", " + optSize.height)
        return optSize
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val imageFileName = "CUSL_"

        val storageDir = File(Environment.getExternalStorageDirectory().path + "/GoodNeighbors/", "Pictures")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        val image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir     /* directory */
        )

        mCurrentPhotoPath = image.absolutePath
        return image
    }

    fun save() {
        var isValidate = true
        if (ui.editCounselingInterviewPlaceSpinner.getValue().isNullOrBlank()) {
            isValidate = false
            ui.editCounselingInterviewPlaceTitleTextView.textColorResource = R.color.colorAccent
        } else {
            ui.editCounselingInterviewPlaceTitleTextView.setTextColor(defaultColorList)
        }

        if (ui.editCounselingRelationshipSpinner.getValue().isNullOrBlank()) {
            isValidate = false
            ui.editCounselingRelationshipTitleTextView.textColorResource = R.color.colorAccent
        } else {
            ui.editCounselingRelationshipTitleTextView.setTextColor(defaultColorList)
        }

        if (ui.editCounselingContentEditText.getStringValue().isBlank()) {
            isValidate = false
            ui.editCounselingContentTitleTextView.textColorResource = R.color.colorAccent
        } else {
            ui.editCounselingContentTitleTextView.setTextColor(defaultColorList)
        }

        if (isValidate) {
            val timestamp = Date().time
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
            val ctrCd = sharedPref.getString("user_ctr_cd", "")
            val userid = sharedPref.getString("userid", "")
            val username = sharedPref.getString("username", "")

            val ch_cusl_info: CH_CUSL_INFO
            if (ui.counselingListLayer.tag == null) { //등록
                ch_cusl_info = CH_CUSL_INFO(CHRCP_NO = chrcp_no, CRT_TP = IMEI, SEQ_NO = (ui.editCounselingCountOfVisitionTextView.tag as String).toLong(), REGR_ID = userid, REG_DT = timestamp)

                if (!(profile!!.CASE1_NM.isNullOrBlank() && profile!!.CASE2_NM.isNullOrBlank() && profile!!.CASE3_NM.isNullOrBlank())) {
                    ch_cusl_info.SPRCP_NO = profile!!.RCP_NO
                }
            } else { // 수정
                val editItem = ui.counselingListLayer.tag as CounselingListItem
                ch_cusl_info = CH_CUSL_INFO(CHRCP_NO = editItem.CHRCP_NO!!, CRT_TP = editItem.CRT_TP!!, SEQ_NO = editItem.SEQ_NO!!.toLong()
                        , SPRCP_NO = editItem.SPRCP_NO, REGR_ID = editItem.REGR_ID, REG_DT = editItem.REG_DT, UPDR_ID = userid, UPD_DT = timestamp)
            }
            ch_cusl_info.INTVR_NM = username
            ch_cusl_info.INTPLC_CD = ui.editCounselingInterviewPlaceSpinner.getValue()
            ch_cusl_info.RSPN_RLCD = ui.editCounselingRelationshipSpinner.getValue()
            ch_cusl_info.CUSL_CTS = ui.editCounselingContentEditText.getStringValue()
            ch_cusl_info.CUSL_DT = timestamp.toDateFormat("yyyyMMdd")
            ch_cusl_info.CUSL_STCD = "13"
            ch_cusl_info.DEL_YN = "N"
            ch_cusl_info.APP_MODIFY_DATE = timestamp

            if (counselingFile != null && counselingFile!!.exists()) {
                val targetDirPath = "sw/${Constants.BUILD}/$ctrCd/${chrcp_no}"
                val targetDir = File(contentsRootDir, targetDirPath)

                val targetFileName = "CUSL_${timestamp.toDateFormat("yyyy")}_${ch_cusl_info.SEQ_NO}_${chrcp_no}_${timestamp.toDateFormat("yyyyMMddHHmmss")}.${counselingFile!!.extension()}"
                val targetFile = File(targetDir, targetFileName)

                counselingFile!!.copyTo(targetFile, true)

                ch_cusl_info.IMG_FP = targetDirPath

                ch_cusl_info.IMG_NM = targetFileName
            }

            logger.debug(GsonBuilder().create().toJson(ch_cusl_info))
            viewModel.saveCounseling(ch_cusl_info).observeOnce(this, Observer {
                onChangeTab(2)
                onBackPressed()
            })
        } else {
            toast(R.string.message_require_fields)
        }
    }

    fun onChangeBottomNavigation(position: Int) {
        when (position) {
            0 -> {
                changeFragment.onChangeFragment(ProfileFragment.newInstance(chrcp_no))
            }
            1 -> {
                changeFragment.onChangeFragment(ReportFragment.newInstance(chrcp_no))
            }
            2 -> {
                changeFragment.onChangeFragment(ProvidedServiceFragment.newInstance(chrcp_no))
            }
            3 -> {
                changeFragment.onChangeFragment(AclFragment.newInstance(chrcp_no))
            }
            4 -> {
                changeFragment.onChangeFragment(GmlFragment.newInstance(chrcp_no))
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class FragmentUI : AnkoComponent<ProfileFragment> {
        lateinit var headerContainer: LinearLayout

        lateinit var informationLayer: View
        lateinit var informationTextView: TextView
        lateinit var informationTabLine: View

        lateinit var counselingLayer: View
        lateinit var counselingTextView: TextView
        lateinit var counselingTabLine: View

        lateinit var counselingListLayer: ViewGroup
        lateinit var counselingEditLayer: ViewGroup

        lateinit var nameTextView: TextView
        lateinit var childCodeTextView: TextView
        lateinit var childCodeLinkTextView: TextView
        lateinit var descTextView: TextView

        lateinit var registeredDateTextView: TextView
        lateinit var recentReportDateTextView: TextView
        lateinit var counselingDateTextView: TextView

        lateinit var profileImageViewPager: ViewPager

        lateinit var specialCaseTextView: TextView
        lateinit var childNameTextView: TextView
        lateinit var birthDateTextView: TextView

        lateinit var genderTextView: TextView
        lateinit var levelOfHealthTextView: TextView
        lateinit var villageTextView: TextView
        lateinit var addressTextView: TextView
        lateinit var disabillityTextView: TextView
        lateinit var schoolTextView: TextView
        lateinit var familyTextView: TextView
        lateinit var siblingsTextView: TextView
        lateinit var remarkTextView: TextView

        lateinit var thumbnameImageView: ImageView
        lateinit var siblingsImageView: ImageView

        lateinit var telephoneImageView: ImageView
        lateinit var mapImageView: ImageView

        lateinit var planContainer: LinearLayout
        lateinit var planTitleTextView: TextView
        lateinit var planTextView: TextView
        lateinit var planDetailTitleTextView: TextView
        lateinit var planDetailTextView: TextView

        lateinit var registerButton: Button

        lateinit var dropoutContainer: LinearLayout
        lateinit var dropoutTextView: TextView
        lateinit var dropoutReasonTextView: TextView

        lateinit var reintakeContainer: LinearLayout
        lateinit var reintakeSwitch: Switch
        lateinit var reintakeEditContainer: LinearLayout
        lateinit var reintakeMessageTextView: TextView
        lateinit var reintakeReasonEditText: EditText
        lateinit var reintakeButton: Button

        lateinit var editCounselingDateTextView: TextView
        lateinit var editCounselingCountOfVisitionTextView: TextView
        lateinit var editCounselingInterviewPlaceTitleTextView: TextView
        lateinit var editCounselingInterviewPlaceSpinner: Spinner
        lateinit var editCounselingRelationshipTitleTextView: TextView
        lateinit var editCounselingRelationshipSpinner: Spinner
        lateinit var editCounselingContentTitleTextView: TextView
        lateinit var editCounselingContentEditText: EditText
        lateinit var editCounselingImageView: ImageView
        lateinit var editCounselingImageButtonContainer: LinearLayout
        //lateinit var editCounselingImageGalleryImageView: ImageView
        lateinit var editCounselingImageCameraImageView: ImageView
        lateinit var editCounselingImageDeleteImageView: ImageView

        lateinit var emptyTextView: TextView

        override fun createView(ui: AnkoContext<ProfileFragment>) = with(ui) {
            scrollView {
                isFillViewport = true

                verticalLayout {
                    // header
                    headerContainer = linearLayout {
                        backgroundColorResource = R.color.colorPrimary
                        topPadding = dimen(R.dimen.px46)
                        leftPadding = dimen(R.dimen.px30)
                        rightPadding = dimen(R.dimen.px30)
                        bottomPadding = dimen(R.dimen.px42)

                        emptyTextView = textView {
                            visibility = View.GONE
                        }

                        frameLayout {
                            thumbnameImageView = circleImageView {
                                imageResource = R.drawable.icon_2
                            }.lparams(width = dimen(R.dimen.px157), height = dimen(R.dimen.px157)) {
                                gravity = Gravity.CENTER_HORIZONTAL

                            }

                            siblingsImageView = imageView(R.drawable.b_family) {
                                visibility = View.GONE
                            }.lparams(width = dimen(R.dimen.px70), height = dimen(R.dimen.px70)) {
                                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL

                            }
                        }.lparams(width = dimen(R.dimen.px157), height = dimen(R.dimen.px210))

                        verticalLayout {
                            nameTextView = textView {
                                setTypeface(null, Typeface.BOLD)
                                textColorResource = R.color.colorWhite
                            }
                            childCodeTextView = textView {
                                textColorResource = R.color.colorYellow
                            }

                            childCodeLinkTextView = textView {
                                textColorResource = R.color.colorYellow
                            }

                            view {
                                setBackgroundResource(R.color.colorLine)
                            }.lparams(width = matchParent, height = dip(1)) {
                                topMargin = dimen(R.dimen.px10)
                                bottomMargin = dimen(R.dimen.px10)
                            }

                            linearLayout {
                                descTextView = textView {
                                    textColorResource = R.color.colorWhite
                                }.lparams(width = dip(0), height = wrapContent, weight = 1f)

                                telephoneImageView = imageView(R.drawable.b_number) {
                                    visibility = View.GONE
                                }.lparams(width = dimen(R.dimen.px70), height = dimen(R.dimen.px70))

                                mapImageView = imageView(R.drawable.b_map) {
                                }.lparams(dimen(R.dimen.px70), dimen(R.dimen.px70)) {
                                    leftMargin = dimen(R.dimen.px10)
                                }
                            }
                        }.lparams {
                            marginStart = dimen(R.dimen.px20)
                        }


                    }.lparams(width = matchParent, height = wrapContent)
                    // end header

                    // tab
                    linearLayout {
                        backgroundColorResource = R.color.colorBgLiteGray

                        frameLayout {
                            onClick {
                                informationLayer.visibility = View.VISIBLE
                                informationTextView.textColorResource = R.color.colorPrimary
                                informationTabLine.visibility = View.VISIBLE

                                counselingLayer.visibility = View.GONE
                                counselingTextView.textColorResource = R.color.color888888
                                counselingTabLine.visibility = View.GONE

                                owner.onChangeTab(1)
                            }

                            informationTextView = textView(R.string.label_information) {
                                textColorResource = R.color.colorPrimary
                                typeface = Typeface.DEFAULT_BOLD
                            }.lparams {
                                gravity = Gravity.CENTER
                            }

                            informationTabLine = view {
                                setBackgroundResource(R.color.colorTabLine)
                            }.lparams(width = matchParent, height = dip(2)) {
                                gravity = Gravity.BOTTOM
                            }
                        }.lparams(width = 0, height = matchParent, weight = 1f)

                        frameLayout {
                            onClick {
                                informationLayer.visibility = View.GONE
                                informationTextView.textColorResource = R.color.color888888
                                informationTabLine.visibility = View.GONE

                                counselingLayer.visibility = View.VISIBLE
                                counselingTextView.textColorResource = R.color.colorPrimary
                                counselingTabLine.visibility = View.VISIBLE

                                owner.onChangeTab(2)
                            }

                            counselingTextView = textView(R.string.label_counseling) {
                                textColorResource = R.color.color888888
                                typeface = Typeface.DEFAULT_BOLD
                            }.lparams {
                                gravity = Gravity.CENTER
                            }

                            counselingTabLine = view {
                                setBackgroundResource(R.color.colorTabLine)
                                visibility = View.GONE
                            }.lparams(width = matchParent, height = dip(2)) {
                                gravity = Gravity.BOTTOM
                            }
                        }.lparams(width = 0, height = matchParent, weight = 1f)
                    }.lparams(height = dimen(R.dimen.px90)) {

                    } // end tab

                    // contents
                    frameLayout {
                        // information
                        informationLayer = verticalLayout {
                            padding = dimen(R.dimen.px20)

                            space {}.lparams(width = matchParent, height = dimen(R.dimen.px40))

                            profileImageViewPager = viewPager {

                            }.lparams(width = dimen(R.dimen.px500), height = dimen(R.dimen.px666)) {
                                gravity = Gravity.CENTER_HORIZONTAL
                            }

                            space {}.lparams(width = matchParent, height = dimen(R.dimen.px40))

                            linearLayout {
                                linearLayout {
                                    textView("1") {
                                        setTypeface(null, Typeface.BOLD)
                                        backgroundColorResource = R.color.colorBgLiteGray
                                        gravity = Gravity.CENTER
                                    }.lparams(width = dimen(R.dimen.px40), height = dimen(R.dimen.px40)) {
                                        gravity = Gravity.CENTER
                                    }
                                    textView(R.string.label_cif_approved_date) {
                                        setTypeface(null, Typeface.BOLD)
                                    }.lparams {
                                        gravity = Gravity.CENTER
                                    }
                                }.lparams(width = 0, height = matchParent, weight = 1f)

                                registeredDateTextView = textView {
                                    gravity = Gravity.CENTER_VERTICAL
                                }.lparams(width = 0, height = matchParent, weight = 1f)


                            }.lparams(width = matchParent, height = dimen(R.dimen.px70)) {

                            }

                            linearLayout {
                                linearLayout {
                                    textView("2") {
                                        setTypeface(null, Typeface.BOLD)
                                        backgroundColorResource = R.color.colorBgLiteGray
                                        gravity = Gravity.CENTER
                                    }.lparams(width = dimen(R.dimen.px40), height = dimen(R.dimen.px40)) {
                                        gravity = Gravity.CENTER
                                    }

                                    textView(R.string.label_recent_report_date) {
                                        setTypeface(null, Typeface.BOLD)
                                    }.lparams {
                                        gravity = Gravity.CENTER
                                    }
                                }.lparams(width = 0, height = matchParent, weight = 1f)

                                recentReportDateTextView = textView {
                                    gravity = Gravity.CENTER_VERTICAL
                                }.lparams(width = 0, height = matchParent, weight = 1f)
                            }.lparams(width = matchParent, height = dimen(R.dimen.px70))

                            linearLayout {
                                linearLayout {
                                    textView("3") {
                                        setTypeface(null, Typeface.BOLD)
                                        backgroundColorResource = R.color.colorBgLiteGray
                                        gravity = Gravity.CENTER
                                    }.lparams(width = dimen(R.dimen.px40), height = dimen(R.dimen.px40)) {
                                        gravity = Gravity.CENTER
                                    }

                                    textView(R.string.label_counseling_date) {
                                        setTypeface(null, Typeface.BOLD)
                                    }.lparams {
                                        gravity = Gravity.CENTER
                                    }
                                }.lparams(width = 0, height = matchParent, weight = 1f)

                                counselingDateTextView = textView {
                                    gravity = Gravity.CENTER_VERTICAL
                                }.lparams(width = 0, height = matchParent, weight = 1f)
                            }.lparams(width = matchParent, height = dimen(R.dimen.px70))

                            linearLayout {
                                textView("4") {
                                    setTypeface(null, Typeface.BOLD)
                                    backgroundColorResource = R.color.colorBgLiteGray
                                    gravity = Gravity.CENTER
                                }.lparams(width = dimen(R.dimen.px40), height = dimen(R.dimen.px40)) {
                                    gravity = Gravity.CENTER
                                }

                                textView(R.string.label_detail_information) {
                                    setTypeface(null, Typeface.BOLD)
                                }.lparams {
                                    gravity = Gravity.CENTER
                                }

                            }.lparams(width = matchParent, height = dimen(R.dimen.px70))

                            // detail information #eaede4
                            linearLayout {
                                textView(R.string.label_category) {
                                    gravity = Gravity.CENTER
                                    backgroundResource = R.drawable.layout_title_border
                                    setTypeface(null, Typeface.BOLD)
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                    rightMargin = dip(-1)
                                }
                                textView(R.string.label_information) {
                                    gravity = Gravity.CENTER
                                    backgroundResource = R.drawable.layout_title_border
                                    setTypeface(null, Typeface.BOLD)
                                }.lparams(width = 0, height = matchParent, weight = 1f)
                            }.lparams(width = matchParent, height = dimen(R.dimen.px70))

                            linearLayout {
                                minimumHeight = dimen(R.dimen.px70)

                                textView(R.string.label_special_case) {
                                    gravity = Gravity.CENTER_VERTICAL
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_title_border
                                    setTypeface(null, Typeface.BOLD)
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                    rightMargin = dip(-1)
                                }
                                specialCaseTextView = textView {
                                    gravity = Gravity.CENTER_VERTICAL
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_title_border
                                    setTypeface(null, Typeface.BOLD)
                                }.lparams(width = 0, height = matchParent, weight = 1f)
                            }.lparams(width = matchParent, height = wrapContent) {
                                topMargin = dip(-1)
                            }

                            linearLayout {
                                minimumHeight = dimen(R.dimen.px70)

                                textView(R.string.label_child_name) {
                                    gravity = Gravity.CENTER_VERTICAL
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                    rightMargin = dip(-1)
                                }
                                childNameTextView = textView {
                                    gravity = Gravity.CENTER_VERTICAL
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f)
                            }.lparams(width = matchParent, height = wrapContent) {
                                topMargin = dip(-1)
                            }

                            linearLayout {
                                minimumHeight = dimen(R.dimen.px70)

                                textView(R.string.label_birthdate) {
                                    gravity = Gravity.CENTER_VERTICAL
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                    rightMargin = dip(-1)
                                }
                                birthDateTextView = textView {
                                    gravity = Gravity.CENTER_VERTICAL
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f)
                            }.lparams(width = matchParent, height = wrapContent) {
                                topMargin = dip(-1)
                            }

                            linearLayout {
                                minimumHeight = dimen(R.dimen.px70)

                                textView(R.string.label_gender) {
                                    gravity = Gravity.CENTER_VERTICAL
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                    rightMargin = dip(-1)
                                }
                                genderTextView = textView {
                                    gravity = Gravity.CENTER_VERTICAL
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f)
                            }.lparams(width = matchParent, height = wrapContent) {
                                topMargin = dip(-1)
                            }

                            linearLayout {
                                minimumHeight = dimen(R.dimen.px70)

                                textView(R.string.label_level_of_health) {
                                    gravity = Gravity.CENTER_VERTICAL
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                    rightMargin = dip(-1)
                                }
                                levelOfHealthTextView = textView {
                                    gravity = Gravity.CENTER_VERTICAL
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f)
                            }.lparams(width = matchParent, height = wrapContent) {
                                topMargin = dip(-1)
                            }

                            linearLayout {
                                minimumHeight = dimen(R.dimen.px70)

                                textView(R.string.label_village) {
                                    gravity = Gravity.CENTER_VERTICAL
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                    rightMargin = dip(-1)
                                }
                                villageTextView = textView {
                                    gravity = Gravity.CENTER_VERTICAL
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f)
                            }.lparams(width = matchParent, height = wrapContent) {
                                topMargin = dip(-1)
                            }

                            linearLayout {
                                minimumHeight = dimen(R.dimen.px70)

                                textView(R.string.label_address) {
                                    gravity = Gravity.CENTER_VERTICAL
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                    rightMargin = dip(-1)
                                }
                                addressTextView = textView {
                                    gravity = Gravity.CENTER_VERTICAL
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f)
                            }.lparams(width = matchParent, height = wrapContent) {
                                topMargin = dip(-1)
                            }

                            linearLayout {
                                minimumHeight = dimen(R.dimen.px70)

                                textView(R.string.label_disabillity_illness) {
                                    gravity = Gravity.CENTER_VERTICAL
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                    rightMargin = dip(-1)
                                }
                                disabillityTextView = textView {
                                    gravity = Gravity.CENTER_VERTICAL
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f)
                            }.lparams(width = matchParent, height = wrapContent) {
                                topMargin = dip(-1)
                            }

                            linearLayout {
                                minimumHeight = dimen(R.dimen.px70)

                                textView(R.string.label_school_information) {
                                    gravity = Gravity.CENTER_VERTICAL
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                    rightMargin = dip(-1)
                                }
                                schoolTextView = textView {
                                    gravity = Gravity.CENTER_VERTICAL
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f)
                            }.lparams(width = matchParent, height = wrapContent) {
                                topMargin = dip(-1)
                            }

                            linearLayout {
                                minimumHeight = dimen(R.dimen.px70)

                                textView(R.string.label_family_information) {
                                    gravity = Gravity.CENTER_VERTICAL
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                    rightMargin = dip(-1)
                                }
                                familyTextView = textView {
                                    gravity = Gravity.CENTER_VERTICAL
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f)
                            }.lparams(width = matchParent, height = wrapContent) {
                                topMargin = dip(-1)
                            }

                            linearLayout {
                                minimumHeight = dimen(R.dimen.px70)

                                textView(R.string.label_sibling_sponsorship) {
                                    gravity = Gravity.CENTER_VERTICAL
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                    rightMargin = dip(-1)
                                }
                                siblingsTextView = textView {
                                    gravity = Gravity.CENTER_VERTICAL
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f)
                            }.lparams(width = matchParent, height = wrapContent) {
                                topMargin = dip(-1)
                            }

                            planContainer = verticalLayout {
                                visibility = View.GONE

                                linearLayout {
                                    minimumHeight = dimen(R.dimen.px70)

                                    planTitleTextView = textView(R.string.label_future_plan) {
                                        gravity = Gravity.CENTER_VERTICAL
                                        padding = dimen(R.dimen.px10)
                                        backgroundResource = R.drawable.layout_border
                                        textColorResource = R.color.colorAccent
                                    }.lparams(width = 0, height = matchParent, weight = 1f) {
                                        rightMargin = dip(-1)
                                    }
                                    planTextView = textView {
                                        gravity = Gravity.CENTER_VERTICAL
                                        padding = dimen(R.dimen.px10)
                                        backgroundResource = R.drawable.layout_border
                                    }.lparams(width = 0, height = matchParent, weight = 1f)
                                }.lparams(width = matchParent, height = wrapContent) {
                                    topMargin = dip(-1)
                                }

                                linearLayout {
                                    minimumHeight = dimen(R.dimen.px70)

                                    planDetailTitleTextView = textView(R.string.label_detail_plan) {
                                        gravity = Gravity.CENTER_VERTICAL
                                        padding = dimen(R.dimen.px10)
                                        backgroundResource = R.drawable.layout_border
                                        textColorResource = R.color.colorAccent
                                    }.lparams(width = 0, height = matchParent, weight = 1f) {
                                        rightMargin = dip(-1)
                                    }
                                    planDetailTextView = textView {
                                        gravity = Gravity.CENTER_VERTICAL
                                        padding = dimen(R.dimen.px10)
                                        backgroundResource = R.drawable.layout_border
                                    }.lparams(width = 0, height = matchParent, weight = 1f)
                                }.lparams(width = matchParent, height = wrapContent) {
                                    topMargin = dip(-1)
                                }
                            }

                            dropoutContainer = verticalLayout {
                                linearLayout {
                                    minimumHeight = dimen(R.dimen.px70)

                                    textView(R.string.label_dropout) {
                                        gravity = Gravity.CENTER_VERTICAL
                                        padding = dimen(R.dimen.px10)
                                        backgroundResource = R.drawable.layout_border
                                    }.lparams(width = 0, height = matchParent, weight = 1f) {
                                        rightMargin = dip(-1)
                                    }
                                    dropoutTextView = textView {
                                        gravity = Gravity.CENTER_VERTICAL
                                        padding = dimen(R.dimen.px10)
                                        backgroundResource = R.drawable.layout_border
                                    }.lparams(width = 0, height = matchParent, weight = 1f)
                                }.lparams(width = matchParent, height = wrapContent) {
                                    topMargin = dip(-1)
                                }

                                linearLayout {
                                    minimumHeight = dimen(R.dimen.px70)

                                    textView(R.string.label_drop_out_reason) {
                                        gravity = Gravity.CENTER_VERTICAL
                                        padding = dimen(R.dimen.px10)
                                        backgroundResource = R.drawable.layout_border
                                    }.lparams(width = 0, height = matchParent, weight = 1f) {
                                        rightMargin = dip(-1)
                                    }
                                    dropoutReasonTextView = textView {
                                        gravity = Gravity.CENTER_VERTICAL
                                        padding = dimen(R.dimen.px10)
                                        backgroundResource = R.drawable.layout_border
                                    }.lparams(width = 0, height = matchParent, weight = 1f)
                                }.lparams(width = matchParent, height = wrapContent) {
                                    topMargin = dip(-1)
                                }
                            }

                            linearLayout {
                                minimumHeight = dimen(R.dimen.px70)

                                textView(R.string.label_remark) {
                                    gravity = Gravity.CENTER_VERTICAL
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f) {
                                    rightMargin = dip(-1)
                                }
                                remarkTextView = textView {
                                    gravity = Gravity.CENTER_VERTICAL
                                    padding = dimen(R.dimen.px10)
                                    backgroundResource = R.drawable.layout_border
                                }.lparams(width = 0, height = matchParent, weight = 1f)
                            }.lparams(width = matchParent, height = wrapContent) {
                                topMargin = dip(-1)
                            }

                            registerButton = button(R.string.button_register_drop_out) {
                                backgroundColorResource = R.color.colorBrown
                                textColorResource = R.color.colorWhite
                                allCaps = false
                                onClick {
                                    owner.changeFragment.onChangeFragment(DropoutFragment.newInstance(owner.chrcp_no))
                                }
                            }.lparams(width = matchParent) {
                                topMargin = dimen(R.dimen.px10)
                            }

                            reintakeContainer = verticalLayout {
                                visibility = View.GONE
                                linearLayout {
                                    textView("5") {
                                        setTypeface(null, Typeface.BOLD)
                                        backgroundColorResource = R.color.colorBgLiteGray
                                        gravity = Gravity.CENTER
                                    }.lparams(width = dimen(R.dimen.px40), height = dimen(R.dimen.px40)) {
                                        gravity = Gravity.CENTER
                                    }

                                    textView(R.string.label_re_intake) {
                                        setTypeface(null, Typeface.BOLD)
                                    }.lparams {
                                        gravity = Gravity.CENTER
                                    }

                                    space {}.lparams(width = 0, weight = 1f)
                                    reintakeSwitch = switch {

                                    }
                                }.lparams(width = matchParent, height = dimen(R.dimen.px70))

                                reintakeEditContainer = verticalLayout {
                                    visibility = View.GONE
                                    reintakeMessageTextView = textView(R.string.message_reintake) {
                                        textColorResource = R.color.colorAccent
                                    }
                                    textView("*5.1 " + owner.getString(R.string.label_reason_for_reintake))

                                    reintakeReasonEditText = editText {
                                        backgroundResource = R.drawable.layout_border
                                        gravity = Gravity.TOP
                                        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                                        minLines = 8
                                        filters = arrayOf(InputFilter.LengthFilter(2000))
                                    }.lparams(width = matchParent, height = wrapContent)


                                    reintakeButton = button(R.string.button_register_new_cif) {
                                        backgroundColorResource = R.color.colorBrown
                                        textColorResource = R.color.colorWhite
                                        allCaps = false
                                    }.lparams(width = matchParent) {
                                        topMargin = dimen(R.dimen.px10)
                                    }
                                }.lparams(width = matchParent, height = wrapContent)
                            }.lparams(width = matchParent, height = wrapContent)
                        }
                        // end information

                        // counseling
                        counselingLayer = frameLayout {
                            visibility = View.GONE

                            // counseling list
                            counselingListLayer = verticalLayout {
                            }
                            // end counseling list

                            // counseling edit
                            counselingEditLayer = verticalLayout {
                                visibility = View.GONE
                                isFocusableInTouchMode = true

                                padding = dimen(R.dimen.px20)

                                linearLayout {
                                    gravity = Gravity.CENTER_VERTICAL

                                    textView("1. Date") {
                                        setTypeface(null, Typeface.BOLD)
                                    }.lparams(width = 0, height = wrapContent, weight = 1f)

                                    editCounselingDateTextView = textView {}.lparams(width = 0, height = wrapContent, weight = 1f)
                                }.lparams(width = matchParent, height = dimen(R.dimen.px70))

                                linearLayout {
                                    gravity = Gravity.CENTER_VERTICAL

                                    textView("2. " + owner.getString(R.string.label_no_of_visiting)) {
                                        setTypeface(null, Typeface.BOLD)
                                    }.lparams(width = 0, height = wrapContent, weight = 1f)
                                    editCounselingCountOfVisitionTextView = textView {}.lparams(width = 0, height = wrapContent, weight = 1f)
                                }.lparams(width = matchParent, height = dimen(R.dimen.px70))

                                editCounselingInterviewPlaceTitleTextView = textView("*3. " + owner.getString(R.string.label_interview_place)) {
                                    gravity = Gravity.CENTER_VERTICAL
                                    setTypeface(null, Typeface.BOLD)
                                }.lparams(width = matchParent, height = dimen(R.dimen.px70))
                                editCounselingInterviewPlaceSpinner = spinner { }

                                editCounselingRelationshipTitleTextView = textView("*4. " + owner.getString(R.string.label_relationship_with_child)) {
                                    gravity = Gravity.CENTER_VERTICAL
                                    setTypeface(null, Typeface.BOLD)
                                }.lparams(width = matchParent, height = dimen(R.dimen.px70))
                                editCounselingRelationshipSpinner = spinner { }

                                editCounselingContentTitleTextView = textView("*5. " + owner.getString(R.string.label_content_of_counseling)) {
                                    gravity = Gravity.CENTER_VERTICAL
                                    setTypeface(null, Typeface.BOLD)
                                }.lparams(width = matchParent, height = dimen(R.dimen.px70))
                                editCounselingContentEditText = editText {
                                    backgroundResource = R.drawable.layout_border
                                    gravity = Gravity.TOP
                                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                                    minLines = 8
                                    filters = arrayOf(InputFilter.LengthFilter(2000))
                                }

                                textView("6. " + owner.getString(R.string.label_photo_of_counseling)) {
                                    gravity = Gravity.CENTER_VERTICAL
                                    setTypeface(null, Typeface.BOLD)
                                }.lparams(width = matchParent, height = dimen(R.dimen.px70))

                                linearLayout {
                                    leftPadding = dimen(R.dimen.px30)
                                    gravity = Gravity.CENTER_VERTICAL

                                    frameLayout {
                                        editCounselingImageView = imageView {
                                            imageResource = R.drawable.icon_2
                                            scaleType = ImageView.ScaleType.CENTER_INSIDE
                                        }.lparams(width = matchParent, height = matchParent) {
                                            gravity = Gravity.CENTER
                                            topMargin = dip(2)
                                            bottomMargin = dip(2)
                                        }
                                        view { backgroundColorResource = R.color.color888888 }.lparams(width = matchParent, height = dip(1)) { gravity = Gravity.TOP }
                                        view { backgroundColorResource = R.color.color888888 }.lparams(width = matchParent, height = dip(1)) { gravity = Gravity.BOTTOM }
                                    }.lparams(width = dimen(R.dimen.px218), height = dimen(R.dimen.px290))

                                    editCounselingImageButtonContainer = linearLayout {
                                        //space {}.lparams(width = dimen(R.dimen.px20), height = matchParent)
                                        //editCounselingImageGalleryImageView = imageView {
                                        //    imageResource = R.drawable.b_gallery02
                                        //}.lparams(width = dimen(R.dimen.px70), height = dimen(R.dimen.px70))

                                        space {}.lparams(width = dimen(R.dimen.px20), height = matchParent)
                                        editCounselingImageCameraImageView = imageView {
                                            imageResource = R.drawable.b_camera02
                                        }.lparams(width = dimen(R.dimen.px70), height = dimen(R.dimen.px70))

                                        space {}.lparams(width = dimen(R.dimen.px20), height = matchParent)
                                        editCounselingImageDeleteImageView = imageView {
                                            imageResource = R.drawable.b_delete02
                                        }.lparams(width = dimen(R.dimen.px70), height = dimen(R.dimen.px70))
                                    }
                                }
                            }
                            // end counseling edit
                        }
                        // end counseling
                    }
                    // end contents

                    space { }.lparams(width = matchParent, height = 0, weight = 1f)

                    linearLayout {
                        gravity = Gravity.CENTER

                        button {
                            setBackgroundResource(R.drawable.gnb_child_on)
//                            onClickListener {
//                                owner.onChangeBottomNavigation(0)
//                            }
                        }.lparams(width = 0, height = dimen(R.dimen.px112), weight = 1f)
                        button("Report") {
                            setBackgroundResource(R.drawable.gnb_bgl_off)
                            textColorResource = R.color.colorWhite
                            allCaps = false
                            onClick {
                                owner.onChangeBottomNavigation(1)
                            }
                        }.lparams(width = 0, height = dimen(R.dimen.px112), weight = 1f)
                        button("Provied Service") {
                            setBackgroundResource(R.drawable.gnb_bgl_off)
                            textColorResource = R.color.colorWhite
                            allCaps = false
                            onClick {
                                owner.onChangeBottomNavigation(2)
                            }
                        }.lparams(width = 0, height = dimen(R.dimen.px112), weight = 1f)
                        button("ACL") {
                            setBackgroundResource(R.drawable.gnb_bgl_off)
                            textColorResource = R.color.colorWhite
                            allCaps = false
                            onClick {
                                owner.onChangeBottomNavigation(3)
                            }
                        }.lparams(width = 0, height = dimen(R.dimen.px112), weight = 1f)
                        button("GML") {
                            setBackgroundResource(R.drawable.gnb_bgr_off)
                            textColorResource = R.color.colorWhite
                            allCaps = false
                            onClick {
                                owner.onChangeBottomNavigation(4)
                            }
                        }.lparams(width = 0, height = dimen(R.dimen.px112), weight = 1f)
                    }
                }
            }
        }
    }

    class ProfileImagePageAdaptor(private val context: Context, private val images: List<String>) : PagerAdapter() {
        private val sdMain = Environment.getExternalStorageDirectory()
        private val contentsRootDir = File("$sdMain/${Constants.DIR_HOME}/${Constants.DIR_CONTENTS}")

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object` as View
        }

        override fun getCount(): Int {
            return images.size
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            var itemImageView: ImageView? = null
            val view = context.UI {
                verticalLayout {
                    itemImageView = imageView {

                    }
                }
            }.view

            val f = images[position]
            var imageFile: File? = null

            if (!f.isBlank()) {
                imageFile = File(contentsRootDir, f)
            }

            if (imageFile != null && imageFile.exists()) {
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

            container.addView(view)

            return view
        }


        override fun destroyItem(parent: ViewGroup, position: Int, `object`: Any) {
            parent.removeView(`object` as View)
        }

    }
}
