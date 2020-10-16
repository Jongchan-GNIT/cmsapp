@file:Suppress("DEPRECATION")

package kr.goodneighbors.cms.ui.childlist


import android.app.Activity
import android.arch.lifecycle.Observer
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.GsonBuilder
import com.scanlibrary.ScanActivity
import com.scanlibrary.ScanConstants
import de.hdodenhof.circleimageview.CircleImageView
import kr.goodneighbors.cms.R
import kr.goodneighbors.cms.common.Constants
import kr.goodneighbors.cms.extensions.circleImageView
import kr.goodneighbors.cms.extensions.convertDateFormat
import kr.goodneighbors.cms.extensions.extension
import kr.goodneighbors.cms.extensions.getStringValue
import kr.goodneighbors.cms.extensions.getValue
import kr.goodneighbors.cms.extensions.isNetworkAvailable
import kr.goodneighbors.cms.extensions.observeOnce
import kr.goodneighbors.cms.extensions.setItem
import kr.goodneighbors.cms.extensions.setSelectKey
import kr.goodneighbors.cms.extensions.timeToString
import kr.goodneighbors.cms.extensions.viewsRecursive
import kr.goodneighbors.cms.service.entities.ACL
import kr.goodneighbors.cms.service.entities.ATCH_FILE
import kr.goodneighbors.cms.service.entities.INTV
import kr.goodneighbors.cms.service.entities.REMRK
import kr.goodneighbors.cms.service.entities.RPT_BSC
import kr.goodneighbors.cms.service.entities.RPT_DIARY
import kr.goodneighbors.cms.service.entities.SWRT
import kr.goodneighbors.cms.service.model.AclEditViewItem
import kr.goodneighbors.cms.service.model.AclEditViewItemSearch
import kr.goodneighbors.cms.service.model.SpinnerOption
import kr.goodneighbors.cms.service.viewmodel.AclViewModel
import kr.goodneighbors.cms.ui.DialogImageViewFragment
import kr.goodneighbors.cms.ui.MapsVillageActivity
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.bottomPadding
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
import org.jetbrains.anko.radioGroup
import org.jetbrains.anko.rightPadding
import org.jetbrains.anko.scrollView
import org.jetbrains.anko.sdk25.coroutines.onCheckedChange
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onItemSelectedListener
import org.jetbrains.anko.space
import org.jetbrains.anko.spinner
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.dimen
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
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
class AclEditFragment : Fragment() {
    companion object {
        const val REQUEST_CODE = 99

        fun newInstance(chrcp_no: String, rcp_no: String?, year: String): AclEditFragment {
            val fragment = AclEditFragment()
            val args = Bundle()
            args.putString("chrcp_no", chrcp_no)
            args.putString("rcp_no", rcp_no)
            args.putString("year", year)

            fragment.arguments = args
            return fragment
        }
    }

    private val logger: Logger by lazy {
        LoggerFactory.getLogger(AclEditFragment::class.java)
    }

    private val ui = FragmentUI()
    private lateinit var defaultColorList: ColorStateList

    private val viewModel: AclViewModel by lazy {
        AclViewModel()
    }

    private var isEditable = true
    private var returnCode: String? = null

    private lateinit var chrcp_no: String
    private lateinit var rcp_no: String
    private lateinit var year: String

    private var currentItem: AclEditViewItem? = null

    private var aclLastImageFile: File? = null
    private var aclImageFile: File? = null
    private var isChangeFile: Boolean = false

    fun isEditable(): Boolean {
        return this.isEditable
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        chrcp_no = arguments!!.getString("chrcp_no", "")
        rcp_no = arguments!!.getString("rcp_no", "")
        year = arguments!!.getString("year", "")

        logger.debug("onCreate : chrcp_no = $chrcp_no, rcp_no = $rcp_no, year = $year")
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val sdMain = Environment.getExternalStorageDirectory()
        val ctrCd = sharedPref.getString("user_ctr_cd", "")
        val brcCd = sharedPref.getString("user_brc_cd", "")
        val prjCd = sharedPref.getString("user_prj_cd", "")
        val userid = sharedPref.getString("userid", "")

        val contentsRootDir = File("$sdMain/${Constants.DIR_HOME}/${Constants.DIR_CONTENTS}")


        viewModel.getAclEditViewItem().observe(this, Observer { aclEditViewItem ->
            logger.debug("onCreate : viewModel.getAclEditViewItem() = $aclEditViewItem")
            aclEditViewItem?.apply {
                currentItem = aclEditViewItem

                val isEditable = (RPT_BSC == null || RPT_BSC?.RPT_STCD == "12" || RPT_BSC?.RPT_STCD == "15" || RPT_BSC?.RPT_STCD == "16")
                setHasOptionsMenu(isEditable)

                ui.nameTextView.text = profile?.CHILD_NAME
                ui.childCodeTextView.text = profile?.CHILD_CODE
                val description = ArrayList<String>()
                val genderString = when (profile?.GNDR) {
                    "F" -> "/Female"
                    "M" -> "/Male"
                    else -> ""
                }
                description.add("${profile?.BDAY?.convertDateFormat()}(${profile?.AGE})$genderString")
                profile?.SCHL_NM?.apply { description.add(this) }
                profile?.VLG_NM?.apply { description.add(this) }

                val guardian = ArrayList<String>()
                profile?.MGDN_CD_NM?.let { guardian.add(it) }
                profile?.MGDN_NM?.let { guardian.add(it) }
                description.add("Guardian: ${guardian.joinToString(", ")}")
                ui.descTextView.text = description.joinToString("\n")

                val f = profile?.THUMB_FILE_PATH?.let {
                    val ff = File(contentsRootDir, it)
                    if (ff.exists()) {
                        ff
                    } else null
                }

                if (f == null) {
                    Glide.with(this@AclEditFragment).load(R.drawable.m_childlist)
                            .apply(RequestOptions.skipMemoryCacheOf(true))
                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                            .into(ui.thumbnameImageView)
                } else {
                    Glide.with(this@AclEditFragment).load(f)
                            .apply(RequestOptions.skipMemoryCacheOf(true))
                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                            .into(ui.thumbnameImageView)
                }

                if (profile?.TEL_NO.isNullOrBlank()) {
                    ui.telephoneImageView.visibility = View.GONE
                } else {
                    ui.telephoneImageView.visibility = View.VISIBLE
                    ui.telephoneImageView.onClick {
                        alert(profile?.TEL_NO!!) {
                            yesButton {

                            }
                        }.show()
                    }
                }

                val siblings = ArrayList<String>()
                if (!profile?.SIBLING1.isNullOrBlank()) siblings.add(profile?.SIBLING1!!)
                if (!profile?.SIBLING2.isNullOrBlank()) siblings.add(profile?.SIBLING2!!)
                ui.siblingsImageView.visibility = if (siblings.isEmpty()) View.GONE else View.VISIBLE

                ui.lastYearTypeTextView.text = lastYearType ?: "-"
                ui.lastYearGhostWritingTextView.text = lastYearGhostwriting ?: "-"

                val lastFiles = PREV_ACL_RPT_BSC?.ATCH_FILE
                if (lastFiles != null && lastFiles.size > 0) {

                    val lastFileInfo = lastFiles[0]
                    val lastFile = File(contentsRootDir, "${lastFileInfo.FILE_PATH}/${lastFileInfo.FILE_NM}")
                    if (lastFile.exists()) {
                        aclLastImageFile = lastFile
                        ui.lastYearPhotoImageView.layoutParams.width = dimen(R.dimen.px218)
                        ui.lastYearPhotoImageView.layoutParams.height = dimen(R.dimen.px290)
                        Glide.with(this@AclEditFragment).load(lastFile)
                                .apply(RequestOptions.skipMemoryCacheOf(true))
                                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                                .into(ui.lastYearPhotoImageView)
                    }
                }

                if (RPT_BSC?.RPT_STCD ?: "" == "2" || RPT_BSC?.RPT_STCD ?: "" == "15") {
                    returnCode = RPT_BSC!!.RPT_STCD

                    ui.returnContainer.visibility = View.VISIBLE
                    when (RPT_BSC?.RPT_STCD) {
                        "2" -> {
                            ui.returnRemarkTitleTextView.textResource = R.string.message_return_remark_ihq
                        }
                        "15" -> {
                            ui.returnRemarkTitleTextView.textResource = R.string.message_return_remark_ho
                        }
                    }

                    returns?.let {
                        AnkoContext.createDelegate(ui.returnItemsContainer).apply {
                            returns?.forEachIndexed { index, returnItem ->
                                textView("${index + 1}. ${returnItem.RTRN_BCD_LABEL}")
                                val returns = ArrayList<String>()
                                if (!returnItem.RTRN_SCD_LABEL.isNullOrBlank()) returns.add(returnItem.RTRN_SCD_LABEL)
                                if (!returnItem.RTRN_DETL.isNullOrBlank()) returns.add(returnItem.RTRN_DETL)

                                textView(returns.joinToString(" -> "))

                                when (returnItem.RTRN_BCD) {
                                    "4" -> {
                                        ui.substitutedTitleTextView.textColorResource = R.color.colorAccent
                                    }
                                    "5" -> {
                                        ui.aclScanTitleTextView.textColorResource = R.color.colorAccent
                                    }
                                    "6" -> {
                                        ui.typeTitleTextView.textColorResource = R.color.colorAccent
                                    }
                                }
                            }
                        }
                    }
                } else {
                    ui.returnContainer.visibility = View.GONE
                }

                INPUT_TYPE?.forEach { inputType ->
                    val rb = RadioButton(context)
                    rb.text = inputType.value
                    rb.tag = inputType
                    rb.isClickable = isEditable

                    ui.typeViewContainer.addView(rb)
                }


                INPUT_RELEATIONSHIP_WITH_CHILD?.let { ui.relationshipSpinner.setItem(it, true) }
                INPUT_REASON?.let { ui.reasonSpinner.setItem(it, true) }


                // data mapping
                if (RPT_BSC != null) {
                    val type = RPT_BSC?.ACL?.RPT_TYPE
                    if (type != null) {
                        ui.typeViewContainer.viewsRecursive.filter { it is RadioButton }.forEach {
                            with(it as RadioButton) {
                                if (it.tag != null && it.tag is SpinnerOption && type == (it.tag as SpinnerOption).key) {
                                    it.isChecked = true
                                }
                            }
                        }
                    }

                    val files = RPT_BSC?.ATCH_FILE
                    if (files != null && files.size > 0) {

                        val fileInfo = files[0]
                        val file = File(contentsRootDir, "${fileInfo.FILE_PATH}/${fileInfo.FILE_NM}")
                        if (file.exists()) {
                            aclImageFile = file
                            ui.aclImageView.layoutParams.width = dimen(R.dimen.px218)
                            ui.aclImageView.layoutParams.height = dimen(R.dimen.px290)
                            Glide.with(this@AclEditFragment).load(file)
                                    .apply(RequestOptions.skipMemoryCacheOf(true))
                                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                                    .into(ui.aclImageView)
                        } else {
                            deleteImage()
                        }
                    } else {
                        deleteImage()
                    }

                    ui.substitutedSwitch.isChecked = RPT_BSC?.SWRT?.SWRT_YN ?: "" == "Y"
                    ui.relationshipSpinner.setSelectKey(RPT_BSC?.SWRT?.SWRTR_RLCD ?: "")
                    ui.reasonSpinner.setSelectKey(RPT_BSC?.SWRT?.SWRT_RNCD ?: "")
                    ui.remarkEditText.setText(RPT_BSC?.REMRK?.REMRK_ENG ?: "")


                    //ui.gallaryImageView.visibility = if (isEditable) View.VISIBLE else View.GONE
                    ui.cameraImageView.visibility = if (isEditable) View.VISIBLE else View.GONE
                    ui.deleteImageView.visibility = if (isEditable) View.VISIBLE else View.GONE

                    ui.substitutedSwitch.isEnabled = isEditable
                    ui.relationshipSpinner.isEnabled = isEditable
                    ui.reasonSpinner.isEnabled = isEditable
                    ui.remarkEditText.isEnabled = isEditable
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = ui.createView(AnkoContext.create(requireContext(), this))

        activity?.title = "ACL"

        defaultColorList = ui.emptyTextView.textColors

        ui.aclImageView.onClick {
            if (aclImageFile != null && aclImageFile!!.exists()) {
                val ft = activity!!.supportFragmentManager.beginTransaction()
                val newFragment = DialogImageViewFragment.newInstance(aclImageFile!!.path)
                newFragment.show(ft, "acl_edit_fragment_view")
            }
        }

        ui.lastYearPhotoImageView.onClick {
            if (aclLastImageFile != null && aclLastImageFile!!.exists()) {
                val ft = activity!!.supportFragmentManager.beginTransaction()
                val newFragment = DialogImageViewFragment.newInstance(aclLastImageFile!!.path)
                newFragment.show(ft, "acl_edit_fragment_view")
            }
        }

        //ui.gallaryImageView.onClick {
        //    val intent = Intent(context, ScanActivity::class.java)
        //    intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, ScanConstants.OPEN_MEDIA)
        //    startActivityForResult(intent, REQUEST_CODE)
        //}

        ui.cameraImageView.onClick {
            val intent = Intent(context, ScanActivity::class.java)
            intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, ScanConstants.OPEN_CAMERA)
            startActivityForResult(intent, REQUEST_CODE)
        }

        ui.deleteImageView.onClick {
            deleteImage()
        }

        ui.mapImageView.onClick {
            if (!currentItem?.profile?.VLG_LAT.isNullOrBlank() && !currentItem?.profile?.VLG_LONG.isNullOrBlank()) {
//                if (requireContext().wifiManager.isWifiEnabled) {
                if (requireContext().isNetworkAvailable()) {
                    startActivity<MapsVillageActivity>(
                            "name" to currentItem?.profile?.VLG_NM,
                            "lat" to currentItem?.profile?.VLG_LAT,
                            "lng" to currentItem?.profile?.VLG_LONG
                    )
                } else {
                    toast(R.string.message_wifi_disabled)
                }
            } else {
                toast(R.string.message_location_is_not_define)
            }
        }

        ui.typeViewContainer.setOnCheckedChangeListener { _, checkedId ->

            val lastYearType = currentItem?.lastYearTypeCode

            var checkedRadioButton: RadioButton? = null
            ui.typeViewContainer.viewsRecursive.filter { it is RadioButton && it.isChecked }.forEach {
                with(it as RadioButton) {
                    if (this.isChecked) {
                        checkedRadioButton = this
                    }
                }
            }
            val currentYearType = (checkedRadioButton!!.tag as SpinnerOption).key


            if (lastYearType == "3" && currentYearType != "3") {
                toast(R.string.message_info_acl_type_change)
            }





            //toast(currentYearType.toString())

            /*if (lastYearType == currentYearType) {
                ui.genderDiffReasonContainer.visibility = View.GONE
                ui.genderDiffReasonSpinner.setSelection(0)
            } else {
                ui.genderDiffReasonContainer.visibility = View.VISIBLE
            }*/
        }

        ui.substitutedSwitch.onCheckedChange { _, isChecked ->
            if (isChecked && isEditable) {
                ui.relationshipSpinner.isEnabled = true
                ui.reasonSpinner.isEnabled = true
            } else {
                ui.relationshipSpinner.setSelection(0)
                ui.relationshipSpinner.isEnabled = false

                ui.reasonSpinner.setSelection(0)
                ui.reasonSpinner.isEnabled = false
            }

            if (isChecked && currentItem?.lastYearGhostwriting == "N") {
                ui.substitutedMessageTextView.visibility = View.VISIBLE
            } else {
                ui.substitutedMessageTextView.visibility = View.GONE
            }
        }

        ui.relationshipSpinner.onItemSelectedListener {
            onItemSelected { _, _, _, _ ->
                var isValid = true
                if (!ui.relationshipSpinner.getValue().isNullOrBlank()) {
                    val family = currentItem!!.PREV_RPT_BSC!!.FMLY!!

                    when (ui.relationshipSpinner.getValue()) {
                        "9" -> { // father
                            if (family.FA_LTYN != "Y") isValid = false
                        }
                        "10" -> { // mother
                            if (family.MO_LTYN != "Y") isValid = false
                        }
                        "11" -> { // brother
                            if (family.EBRO_LTNUM.isNullOrBlank() || family.EBRO_LTNUM == "0") isValid = false
                        }
                        "12" -> { // sister
                            if (family.ESIS_LTNUM.isNullOrBlank() || family.ESIS_LTNUM == "0") isValid = false
                        }
                    }
                }

                if (isValid) {
                    ui.relationshipMessageTextView.visibility = View.GONE
                } else {
                    ui.relationshipMessageTextView.visibility = View.VISIBLE
                }
            }
        }

        ui.reasonSpinner.onItemSelectedListener {
            onItemSelected { _, _, _, _ ->
                if (ui.reasonSpinner.getValue() == "1") { // && currentItem?.PREV_RPT_BSC?.CH_BSC?.AGE?.toInt() ?: 0 > 6) {
                    currentItem?.PREV_RPT_BSC?.CH_BSC?.BDAY?.substring(0, 4)?.toIntOrNull()?.apply {
                        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                        if (currentYear - this > 6) {
                            ui.reasonMessageTextView.visibility = View.VISIBLE
                        }
                    }
                } else {
                    ui.reasonMessageTextView.visibility = View.GONE
                }
            }
        }

        viewModel.setAclEditViewItemSearch(AclEditViewItemSearch(chrcp_no, rcp_no, year))

        return v
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        activity?.menuInflater?.inflate(R.menu.toolbar_cif, menu)

        // 저장 버튼 클릭
        menu?.findItem(R.id.cif_toolbar_save)!!.setOnMenuItemClickListener {
            save()
            true
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        logger.debug("onActivityResult($requestCode, $resultCode, $data)")

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            try {
                val uri = data.extras!!.getParcelable<Uri>(ScanConstants.SCANNED_RESULT)

                var bitmap = MediaStore.Images.Media.getBitmap(activity!!.applicationContext.contentResolver, uri)
                if (bitmap.width > bitmap.height) {
                    bitmap = rotateBitmapImage(bitmap)
                }

                val sizedImageFile = createImageFile()
                fun onResizePhoto() {
                    val origin = File(uri.path)
                    if (origin.exists() && origin.isFile) {
                        origin.delete()
                    }

                    if (sizedImageFile.exists()) {
                        ui.aclImageView.layoutParams.width = dimen(R.dimen.px218)
                        ui.aclImageView.layoutParams.height = dimen(R.dimen.px290)
                        Glide.with(this).load(sizedImageFile).into(ui.aclImageView)
                    } else {
                        deleteImage()
                    }

                    val providerURI = FileProvider.getUriForFile(activity!!, "kr.goodneighbors.cms.provider", sizedImageFile)
                    logger.debug("providerURI : $providerURI")
                }

                Glide.with(this)
                        .asBitmap()
                        .load(bitmap)
                        .into(object : SimpleTarget<Bitmap>(500, 667) {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                try {
                                    val out = FileOutputStream(sizedImageFile)
                                    resource.compress(Bitmap.CompressFormat.JPEG, 100, out)
                                    out.flush()
                                    out.close()

                                    aclImageFile = sizedImageFile
                                    isChangeFile = true
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }
                                onResizePhoto()
                            }

                            override fun onLoadFailed(errorDrawable: Drawable?) {
                                logger.error("Glide.onLoadFailed : ", errorDrawable)
                            }
                        })

            } catch (e: IOException) {
                e.printStackTrace()
                logger.error("error : ", e)
            }

        }
    }

    private fun deleteImage() {
        aclImageFile = null
        ui.aclImageView.layoutParams.width = dimen(R.dimen.px96)
        ui.aclImageView.layoutParams.height = dimen(R.dimen.px116)

        Glide.with(this).load(R.drawable.icon_3)
                .apply(RequestOptions.skipMemoryCacheOf(true))
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                .into(ui.aclImageView)
    }

    private fun validate(): Boolean {
        var isValid = true

        if (aclImageFile == null) {
            isValid = false
            ui.aclScanTitleTextView.textColorResource = R.color.colorAccent
        } else {
            ui.aclScanTitleTextView.setTextColor(defaultColorList)
        }

        var checkedRadioButton: RadioButton? = null
        ui.typeViewContainer.viewsRecursive.filter { it is RadioButton }.forEach {
            with(it as RadioButton) {
                if (this.isChecked) {
                    checkedRadioButton = this
                }
            }
        }
        if (checkedRadioButton == null) {
            isValid = false
            ui.typeTitleTextView.textColorResource = R.color.colorAccent
        } else {
            ui.typeTitleTextView.setTextColor(defaultColorList)
        }

        if (ui.substitutedSwitch.isChecked) {
            if (ui.relationshipSpinner.getValue().isNullOrBlank()) {
                isValid = false
                ui.relationshipTitleTextView.textColorResource = R.color.colorAccent
            } else {
                ui.relationshipTitleTextView.setTextColor(defaultColorList)
            }

            if (ui.reasonSpinner.getValue().isNullOrBlank()) {
                isValid = false
                ui.reasonTitleTextView.textColorResource = R.color.colorAccent
            } else {
                ui.reasonTitleTextView.setTextColor(defaultColorList)
            }
        }

        if (ui.substitutedMessageTextView.visibility == View.VISIBLE || ui.relationshipMessageTextView.visibility == View.VISIBLE || ui.reasonMessageTextView.visibility == View.VISIBLE) {
            if (ui.remarkEditText.getStringValue().isBlank() || ui.remarkEditText.getStringValue().length < 30) {
                isValid = false
                ui.remarkTitleTextView.textColorResource = R.color.colorAccent
            } else {
                ui.remarkTitleTextView.setTextColor(defaultColorList)
            }
        } else {
            ui.remarkTitleTextView.setTextColor(defaultColorList)
        }

        return isValid
    }

    private fun save() {
        if (!validate()) {
            toast(R.string.message_require_fields)

            return
        }

        var checkedRadioButton: RadioButton? = null
        ui.typeViewContainer.viewsRecursive.filter { it is RadioButton && it.isChecked }.forEach {
            with(it as RadioButton) {
                if (this.isChecked) {
                    checkedRadioButton = this
                }
            }
        }
        val type = (checkedRadioButton!!.tag as SpinnerOption).key
        val substituted = if (ui.substitutedSwitch.isChecked) "Y" else "N"


        val relationship = ui.relationshipSpinner.getValue()

        val reason = ui.reasonSpinner.getValue()

        val remarkValue = ui.remarkEditText.getStringValue()

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val sdMain = Environment.getExternalStorageDirectory()
        val ctrCd = sharedPref.getString("user_ctr_cd", "")
        val brcCd = sharedPref.getString("user_brc_cd", "")
        val prjCd = sharedPref.getString("user_prj_cd", "")
        val userid = sharedPref.getString("userid", "")
        val contentsRootDir = File("$sdMain/${Constants.DIR_HOME}/${Constants.DIR_CONTENTS}")
        val timestamp = Date().time

        val report = currentItem?.RPT_BSC
                ?: RPT_BSC(CHRCP_NO = chrcp_no, RCP_NO = "${chrcp_no}4$year", RPT_DVCD = "4", YEAR = year)
        if (report.REG_DT == null) {
            report.REG_DT = timestamp
            report.REGR_ID = userid
            report.DEL_YN = "N"
            report.DEGR = 1
            report.EXPT_YN = "N"
            report.PSCRN_YN = "N"
            report.XSCRN_YN = "N"
            report.FRCP_NO = report.RCP_NO
            report.DCMT_YN = "N"
            report.FIDG_YN = "Y"

            report.RPT_DIARY = RPT_DIARY(RCP_NO = report.RCP_NO)
            report.INTV = INTV(RCP_NO = report.RCP_NO, INTVR_NM = userid, INTV_DT = Date().timeToString("yyyyMMdd"))
        } else {
            report.UPD_DT = timestamp
            report.UPDR_ID = userid
        }
        report.RPT_STCD = "13"
        report.LAST_UPD_DT = timestamp
        report.APP_MODIFY_DATE = timestamp

        val acl = report.ACL ?: ACL(RCP_NO = report.RCP_NO)
        report.ACL = acl
        acl.RPT_TYPE = type

        val swrt = report.SWRT ?: SWRT(RCP_NO = report.RCP_NO)
        report.SWRT = swrt
        swrt.SWRT_YN = substituted
        swrt.SWRTR_RLCD = if (substituted == "Y") relationship else null
        swrt.SWRT_RNCD = if (substituted == "Y") reason else null


        val remark = report.REMRK ?: REMRK(RCP_NO = report.RCP_NO)
        report.REMRK = remark
        remark.REMRK_ENG = remarkValue

        if (isChangeFile) {
            val targetDirPath = "sw/${Constants.BUILD}/$ctrCd/${report.CHRCP_NO}"
            val targetDir = File(contentsRootDir, targetDirPath)
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            val imageGeneralFileName = "ACL_${year}_${chrcp_no}_${timestamp}.${aclImageFile!!.extension()}"
            val targetFile = File(targetDir, imageGeneralFileName)
            aclImageFile!!.copyTo(targetFile, true)

            report.ATCH_FILE = arrayListOf(ATCH_FILE(RCP_NO = report.RCP_NO, SEQ_NO = 1, FILE_DVCD = "4", IMG_DVCD = "331001", FILE_PATH = targetDirPath, FILE_NM = imageGeneralFileName))
        }

        logger.debug(GsonBuilder().create().toJson(report))

        viewModel.save(report).observeOnce(this, Observer {
            if (it != null && it == true) {
                isEditable = false
                activity!!.onBackPressed()
            }
        })
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val imageFileName = "ACL_${chrcp_no}_"

        val storageDir = File(Environment.getExternalStorageDirectory().path + "/GoodNeighbors/", "Pictures")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        val image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir     /* directory */
        )


        return image
    }

    private fun rotateBitmapImage(bmp: Bitmap): Bitmap {
        val width = bmp.width
        val height = bmp.height

        val matrix = Matrix()
        matrix.postRotate(90f)

        val resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true)
        bmp.recycle()

        return resizedBitmap
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class FragmentUI : AnkoComponent<AclEditFragment> {
        lateinit var emptyTextView: TextView
        lateinit var thumbnameImageView: CircleImageView
        lateinit var siblingsImageView: ImageView
        lateinit var telephoneImageView: ImageView
        lateinit var mapImageView: ImageView

        lateinit var nameTextView: TextView
        lateinit var childCodeTextView: TextView
        lateinit var descTextView: TextView

        lateinit var lastYearTypeTextView: TextView
        lateinit var lastYearGhostWritingTextView: TextView
        lateinit var lastYearPhotoImageView: ImageView


        lateinit var aclImageView: ImageView
        //lateinit var gallaryImageView: ImageView
        lateinit var cameraImageView: ImageView
        lateinit var deleteImageView: ImageView

        lateinit var relationshipSpinner: Spinner
        lateinit var reasonSpinner: Spinner

        lateinit var substitutedSwitch: Switch
        lateinit var remarkEditText: EditText

        lateinit var typeViewContainer: RadioGroup


        lateinit var returnContainer: LinearLayout
        lateinit var returnRemarkTitleTextView: TextView
        lateinit var returnItemsContainer: LinearLayout

        lateinit var aclScanTitleTextView: TextView

        lateinit var typeTitleTextView: TextView

        lateinit var substitutedTitleTextView: TextView
        lateinit var substitutedMessageTextView: TextView

        lateinit var relationshipTitleTextView: TextView
        lateinit var relationshipMessageTextView: TextView

        lateinit var reasonTitleTextView: TextView
        lateinit var reasonMessageTextView: TextView

        lateinit var remarkTitleTextView: TextView

        override fun createView(ui: AnkoContext<AclEditFragment>) = with(ui) {
            scrollView {
                verticalLayout {
                    // header
                    emptyTextView = textView { visibility = View.GONE }

                    linearLayout {
                        backgroundColorResource = R.color.colorPrimary
                        topPadding = dimen(R.dimen.px46)
                        leftPadding = dimen(R.dimen.px30)
                        rightPadding = dimen(R.dimen.px30)
                        bottomPadding = dimen(R.dimen.px42)

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
                            marginStart = dimen(R.dimen.px40)
                        }
                    }.lparams(width = matchParent, height = wrapContent)

                    linearLayout {
                        backgroundColorResource = R.color.colorFFFFD9
                        leftPadding = dimen(R.dimen.px20)

                        linearLayout {
                            gravity = Gravity.CENTER_VERTICAL

                            textView("* " + owner.getString(R.string.label_last_year)  + ": ")
                            lastYearTypeTextView = textView {
                                setTypeface(null, Typeface.BOLD)
                            }
                        }.lparams(width = 0, height = matchParent, weight = 1f)

                        linearLayout {
                            gravity = Gravity.CENTER_VERTICAL

                            textView("* " + owner.getString(R.string.label_substituted) + ": ")
                            lastYearGhostWritingTextView = textView {
                                setTypeface(null, Typeface.BOLD)
                            }
                        }.lparams(width = 0, height = matchParent, weight = 1f)
                    }.lparams(width = matchParent, height = dimen(R.dimen.px70))

                    frameLayout {
                        backgroundColorResource = R.color.colorFFFFD9
                        padding = dimen(R.dimen.px20)
                        view { backgroundColorResource = R.color.colorFFFFD9 }.lparams(width = matchParent, height = dip(1))

                        lastYearPhotoImageView = imageView {
                            imageResource = R.drawable.icon_3
                        }.lparams(width = dimen(R.dimen.px96), height = dimen(R.dimen.px116)) {
                            gravity = Gravity.LEFT
                        }

                        view { backgroundColorResource = R.color.colorFFFFD9 }.lparams(width = matchParent, height = dip(1))
                    }.lparams(width = matchParent, height = dimen(R.dimen.px290))

                    view {
                        backgroundColorResource = R.color.colorSplitLine
                    }.lparams(width = matchParent, height = dip(1)) { }

                    returnContainer = verticalLayout {
                        padding = dip(15)
                        backgroundColorResource = R.color.colorReturnBackground
                        visibility = View.GONE

                        returnRemarkTitleTextView = textView {
                            typeface = Typeface.DEFAULT_BOLD
                        }
                        view {
                            backgroundColorResource = R.color.colorSplitLine
                        }.lparams(width = matchParent, height = dip(1)) {
                            topMargin = dip(10)
                            bottomMargin = dip(10)
                        }
                        returnItemsContainer = verticalLayout {

                        }
                    }

                    // contents
                    verticalLayout {
                        padding = dimen(R.dimen.px20)

                        aclScanTitleTextView = textView("*1. " + owner.getString(R.string.label_acl_scan)) {
                            gravity = Gravity.CENTER_VERTICAL
                            setTypeface(null, Typeface.BOLD)
                        }.lparams(width = matchParent, height = dimen(R.dimen.px70))

                        linearLayout {
                            leftPadding = dimen(R.dimen.px30)
                            gravity = Gravity.CENTER_VERTICAL

                            frameLayout {
                                view { backgroundColorResource = R.color.color888888 }.lparams(width = matchParent, height = dip(1))

                                aclImageView = imageView {
                                    imageResource = R.drawable.icon_3
                                }.lparams(width = dimen(R.dimen.px96), height = dimen(R.dimen.px116)) {
                                    gravity = Gravity.CENTER
                                }

                                view { backgroundColorResource = R.color.color888888 }.lparams(width = matchParent, height = dip(1)) { gravity = Gravity.BOTTOM }
                            }.lparams(width = dimen(R.dimen.px218), height = dimen(R.dimen.px290))

                            linearLayout {
                               // space {}.lparams(width = dimen(R.dimen.px20), height = matchParent)
                               // gallaryImageView = imageView {
                               //     imageResource = R.drawable.b_gallery02
                               // }.lparams(width = dimen(R.dimen.px70), height = dimen(R.dimen.px70))

                                space {}.lparams(width = dimen(R.dimen.px20), height = matchParent)
                                cameraImageView = imageView {
                                    imageResource = R.drawable.b_camera02
                                }.lparams(width = dimen(R.dimen.px70), height = dimen(R.dimen.px70))

                                space {}.lparams(width = dimen(R.dimen.px20), height = matchParent)
                                deleteImageView = imageView {
                                    imageResource = R.drawable.b_delete02
                                }.lparams(width = dimen(R.dimen.px70), height = dimen(R.dimen.px70))
                            }
                        }

                        textView("2." + owner.getString(R.string.label_acl_information)) {
                            gravity = Gravity.CENTER_VERTICAL
                            setTypeface(null, Typeface.BOLD)
                        }.lparams(width = matchParent, height = dimen(R.dimen.px70))

                        linearLayout {
                            leftPadding = dimen(R.dimen.px20)
                            gravity = Gravity.CENTER_VERTICAL

                            typeTitleTextView = textView("*2.1 " + owner.getString(R.string.label_type))

                            typeViewContainer = radioGroup {
                                orientation = RadioGroup.HORIZONTAL
                            }
                        }.lparams(width = matchParent, height = dimen(R.dimen.px70))

                        linearLayout {
                            leftPadding = dimen(R.dimen.px20)
                            gravity = Gravity.CENTER_VERTICAL

                            substitutedTitleTextView = textView("*2.2 " + owner.getString(R.string.label_substituted_writing))

                            space {}.lparams(width = dimen(R.dimen.px20), height = matchParent)

                            substitutedSwitch = switch { }
                        }.lparams(width = matchParent, height = dimen(R.dimen.px70))

                        substitutedMessageTextView = textView(R.string.message_validate_acl_substituted) {
                            leftPadding = dimen(R.dimen.px20)
                            visibility = View.GONE
                            textColorResource = R.color.colorAccent
                        }

                        relationshipTitleTextView = textView("* 2.3 " + owner.getString(R.string.label_relationship_with_child)) {
                            leftPadding = dimen(R.dimen.px20)
                            gravity = Gravity.CENTER_VERTICAL
                        }.lparams(width = matchParent, height = dimen(R.dimen.px70))
                        relationshipSpinner = spinner {
                            leftPadding = dimen(R.dimen.px20)
                            isEnabled = false
                        }.lparams(width = matchParent, height = dimen(R.dimen.px70))

                        relationshipMessageTextView = textView(R.string.message_validate_acl_relationship) {
                            leftPadding = dimen(R.dimen.px20)
                            visibility = View.GONE
                            textColorResource = R.color.colorAccent
                        }

                        reasonTitleTextView = textView("* 2.4 " + owner.getString(R.string.label_reason)) {
                            leftPadding = dimen(R.dimen.px20)
                            gravity = Gravity.CENTER_VERTICAL
                        }.lparams(width = matchParent, height = dimen(R.dimen.px70))
                        reasonSpinner = spinner {
                            leftPadding = dimen(R.dimen.px20)
                            isEnabled = false
                        }.lparams(width = matchParent, height = dimen(R.dimen.px70))

                        reasonMessageTextView = textView(R.string.message_validate_acl_reason) {
                            leftPadding = dimen(R.dimen.px20)
                            visibility = View.GONE
                            textColorResource = R.color.colorAccent
                        }

                        remarkTitleTextView = textView("3. " + owner.getString(R.string.label_remark)) {
                            setTypeface(null, Typeface.BOLD)
                        }.lparams(width = matchParent, height = dimen(R.dimen.px70))

                        remarkEditText = editText {
                            backgroundResource = R.drawable.layout_border
                            gravity = Gravity.TOP
                            minLines = 8
                            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE

                            filters = arrayOf(InputFilter.LengthFilter(2000))
                        }
                    }.lparams(width = matchParent, height = wrapContent)
                }
            }
        }
    }
}
