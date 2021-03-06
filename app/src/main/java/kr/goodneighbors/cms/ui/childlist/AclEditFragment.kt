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
import android.hardware.Camera
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
import kr.goodneighbors.cms.extensions.*
import kr.goodneighbors.cms.service.entities.ACL
import kr.goodneighbors.cms.service.entities.ATCH_FILE
import kr.goodneighbors.cms.service.entities.INTV
import kr.goodneighbors.cms.service.entities.REMRK
import kr.goodneighbors.cms.service.entities.RPT_BSC
import kr.goodneighbors.cms.service.entities.RPT_DIARY
import kr.goodneighbors.cms.service.entities.SWRT
import kr.goodneighbors.cms.service.model.*
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
import org.jetbrains.anko.support.v4.*
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
        const val CHILD_REQUEST_CODE = 100
        const val REQUEST_VIDEO_CAPTURE = 2
        const val REQUEST_SCAN = 201
        const val PICK_FROM_CAMERA = 0

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
    private var childImageFile: File? = null
    private var isChangeFile: Boolean = false
    private var isChildChangeFile: Boolean = false

    private var videoFile: File? = null
    private var tempVideoFile: File? = null

    private var mCurrentPhotoPath: String = ""

    private var imageUri: Uri? = null
    private var photoURI: Uri? = null
    private var albumURI: Uri? = null

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

                val age = profile?.AGE
                INPUT_TYPE?.forEach { inputType ->
                    val rb = RadioButton(context)
                    rb.text = inputType.value
                    rb.tag = inputType
                    rb.isClickable = isEditable

                    if(age.toString().toInt()!! >= 18) {
                        if(rb.text.equals("Thank You Letter")) {
                            rb.isChecked = true
                        } else {
                            //rb.isClickable = false
                            rb.isEnabled = false
                        }
                    } else {
                        if(rb.text.equals("Thank You Letter")) {
                            rb.isEnabled = false
                        }
                    }

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

                    /*
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
                     */


                    val images = ArrayList<AclImageItem>()
                    RPT_BSC?.ATCH_FILE?.forEach {
                        if (it.IMG_DVCD == "331001") {
                            aclImageFile = File("$contentsRootDir/${it.FILE_PATH}/${it.FILE_NM}")
                            if (aclImageFile!!.exists()) {
                                ui.aclImageView.layoutParams.width = dimen(R.dimen.px218)
                                ui.aclImageView.layoutParams.height = dimen(R.dimen.px290)
                                Glide.with(this@AclEditFragment)
                                        .asBitmap()
                                        .load(aclImageFile)
                                        .into(ui.aclImageView)
                            } else {
                                deleteImage()
                            }
                        } else if (it.IMG_DVCD == "331007") {
                            childImageFile = File("$contentsRootDir/${it.FILE_PATH}/${it.FILE_NM}")
                            if (childImageFile!!.exists()) {
                                ui.childImageView.layoutParams.width = dimen(R.dimen.px218)
                                ui.childImageView.layoutParams.height = dimen(R.dimen.px290)
                                Glide.with(this@AclEditFragment)
                                        .asBitmap()
                                        .load(childImageFile)
                                        .into(ui.childImageView)
                            } else {
                                childDeleteImage()
                            }
                        } else if (it.IMG_DVCD == "331004") {
                            try {
                                videoFile = File("$contentsRootDir/${it.FILE_PATH}/${it.FILE_NM}")
                                if (videoFile!!.exists()) {
                                    Glide.with(this@AclEditFragment)
                                            .asBitmap()
                                            .load(videoFile)
                                            .into(ui.videoImageView)
                                } else {
                                    videoFile = null
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    ui.substitutedSwitch.isChecked = RPT_BSC?.SWRT?.SWRT_YN ?: "" == "Y"
                    ui.relationshipSpinner.setSelectKey(RPT_BSC?.SWRT?.SWRTR_RLCD ?: "")
                    ui.reasonSpinner.setSelectKey(RPT_BSC?.SWRT?.SWRT_RNCD ?: "")
                    ui.remarkEditText.setText(RPT_BSC?.REMRK?.REMRK_ENG ?: "")

                    //ui.gallaryImageView.visibility = if (isEditable) View.VISIBLE else View.GONE
                    ui.cameraImageView.visibility = if (isEditable) View.VISIBLE else View.GONE
                    ui.deleteImageView.visibility = if (isEditable) View.VISIBLE else View.GONE

                    ui.childCameraImageView.visibility = if (isEditable) View.VISIBLE else View.GONE
                    ui.childDeleteImageView.visibility = if (isEditable) View.VISIBLE else View.GONE

                    ui.videoFromCameraImageView.visibility = if (isEditable) View.VISIBLE else View.GONE
                    ui.videoFromDeleteImageView.visibility = if (isEditable) View.VISIBLE else View.GONE

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

        ui.childImageView.onClick {
            if (aclImageFile != null && aclImageFile!!.exists()) {
                val ft = activity!!.supportFragmentManager.beginTransaction()
                val newFragment = DialogImageViewFragment.newInstance(childImageFile!!.path)
                newFragment.show(ft, "acl_edit_fragment_view")
            }
        }

        ui.videoImageView.onClick {
            if (videoFile != null && videoFile!!.exists()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoFile!!.path))
                intent.setDataAndType(Uri.parse(videoFile!!.path), "video/*")
                startActivity(intent)
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

        ui.cameraImageView.setOnClickListener {
            val intent = Intent(context, ScanActivity::class.java)
            intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, ScanConstants.OPEN_CAMERA)
            startActivityForResult(intent, REQUEST_SCAN)
        }

        ui.deleteImageView.onClick {
            deleteImage()
        }

        ui.childCameraImageView.onClick {
            captureCamera(PICK_FROM_CAMERA)
        }

        ui.childDeleteImageView.onClick {
            childDeleteImage()
        }

        ui.videoFromCameraImageView.onClick {
            val prefix = "ACL_"
            val storageDir = File(Environment.getExternalStorageDirectory().path + "/GoodNeighbors/", "Pictures")
            val video = File.createTempFile(
                    prefix, /* prefix */
                    ".mp4", /* suffix */
                    storageDir     /* directory */
            )
            tempVideoFile = File(video.path)

            val providerURI = FileProvider.getUriForFile(activity!!, "kr.goodneighbors.cms.provider", video)
            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10)
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, providerURI)
            if (intent.resolveActivity(activity!!.packageManager) != null) {
                startActivityForResult(intent, REQUEST_VIDEO_CAPTURE)
            }
        }

        ui.videoFromDeleteImageView.onClick {
            ui.videoImageView.imageResource = R.drawable.movie
            videoFile = null
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

            if(!currentItem?.profile?.AGE.isNullOrBlank() && currentItem?.profile?.AGE.toString().toInt() >= 18) {
                // 18세 이상일 땐 무조건 Thank you Letter
            } else {
                val currentYearType = (checkedRadioButton!!.tag as SpinnerOption).key

                if (lastYearType == "3" && currentYearType != "3") {
                    toast(R.string.message_info_acl_type_change)
                }
            }
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

        } else if (requestCode == PICK_FROM_CAMERA && resultCode == Activity.RESULT_OK) {
            try {
                logger.debug("PICK_FROM_CAMERA : {}", imageUri)
                val originalFile = mCurrentPhotoPath

                val sizedImageFile = createImageFile()
                logger.debug("PICK_FROM_CAMERA resize: {}", sizedImageFile.path)

                childImageFile = sizedImageFile
                isChildChangeFile = true

                fun onResizePhoto() {
                    val origin = File(originalFile)

                    ui.childImageView.layoutParams.width = dimen(R.dimen.px218)
                    ui.childImageView.layoutParams.height = dimen(R.dimen.px290)
                    Glide.with(this).load(sizedImageFile).into(ui.childImageView)

                    val photoCropImageFile = createImageFile()
                    val providerURI = FileProvider.getUriForFile(activity!!, "kr.goodneighbors.cms.provider", origin)
                    photoURI = providerURI
                    albumURI = Uri.fromFile(photoCropImageFile)
                    //cropImage()
                }

                Glide.with(this)
                        .asBitmap()
                        .load(imageUri)
                        .into(object : SimpleTarget<Bitmap>(500, 667) {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                try {
                                    val out = FileOutputStream(sizedImageFile)
                                    resource.compress(Bitmap.CompressFormat.JPEG, 100, out)
                                    out.flush()
                                    out.close()
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

        } else if (requestCode == REQUEST_VIDEO_CAPTURE  && resultCode == Activity.RESULT_OK) {
            try {
                videoFile = File(tempVideoFile?.path)
                Glide.with(this)
                        .asBitmap()
                        .load(tempVideoFile?.path)
                        .into(ui.videoImageView)
            } catch (e: Exception) {
                logger.error("REQUEST_VIDEO_CAPTURE  : ", e)
            }
        } else if (requestCode == REQUEST_SCAN && resultCode == Activity.RESULT_OK && data != null) {
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
//                                origin.delete()
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

    private fun childDeleteImage() {
        childImageFile = null
        ui.childImageView.layoutParams.width = dimen(R.dimen.px96)
        ui.childImageView.layoutParams.height = dimen(R.dimen.px116)

        Glide.with(this).load(R.drawable.icon_2)
                .apply(RequestOptions.skipMemoryCacheOf(true))
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                .into(ui.childImageView)
    }

    private fun validate(): Boolean {
        var isValid = true

        if (aclImageFile == null) {
            isValid = false
            ui.aclScanTitleTextView.textColorResource = R.color.colorAccent
        } else {
            ui.aclScanTitleTextView.setTextColor(defaultColorList)
        }

        if (currentItem?.profile?.AGE.toString().toInt() >= 18){
            if (childImageFile == null) {
                isValid = false
                ui.childPhotoTitleTextView.textColorResource = R.color.colorAccent
            } else {
                ui.childPhotoTitleTextView.setTextColor(defaultColorList)
            }
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

        val targetDirPath = "sw/${Constants.BUILD}/$ctrCd/${report.CHRCP_NO}"
        val targetDir = File(contentsRootDir, targetDirPath)
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }
        val files = ArrayList<ATCH_FILE>()

        if (isChangeFile) {
            val targetFileName = "ACL_${year}_${chrcp_no}_1_${timestamp}.${aclImageFile!!.extension()}"
            val targetFile = File(targetDir, targetFileName)
            aclImageFile!!.copyTo(targetFile, true)

            files.add(ATCH_FILE(RCP_NO = report.RCP_NO, SEQ_NO = files.size + 1,
                    FILE_DVCD = "4", IMG_DVCD = "331001",
                    FILE_PATH = targetDirPath, FILE_NM = targetFileName))
        }

        if (isChildChangeFile) {
            val targetFileName = "ACL_${year}_${chrcp_no}_2_${timestamp}.${childImageFile!!.extension()}"
            val targetFile = File(targetDir, targetFileName)
            childImageFile!!.copyTo(targetFile, true)

            files.add(ATCH_FILE(RCP_NO = report.RCP_NO, SEQ_NO = files.size + 1,
                    FILE_DVCD = "4", IMG_DVCD = "331007",
                    FILE_PATH = targetDirPath, FILE_NM = targetFileName))
        }

        if (videoFile != null && videoFile!!.exists()) {
            val targetFileName = "ACL_${year}_${chrcp_no}_VI_${timestamp.toDateFormat("yyyyMMddHHmmss")}.${videoFile!!.extension()}"
            val targetFile = File(targetDir, targetFileName)

            if (videoFile!! != targetFile) {
                videoFile!!.copyTo(targetFile, true)
            }

            files.add(ATCH_FILE(SEQ_NO = files.size + 1, RCP_NO = report.RCP_NO,
                    FILE_DVCD = report.RPT_DVCD, IMG_DVCD = "331004",
                    FILE_NM = targetFileName, FILE_PATH = targetDirPath))
        }

        if (files.isNotEmpty()) report.ATCH_FILE = files

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

        mCurrentPhotoPath = image.absolutePath
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

    private fun captureCamera(requestCode: Int) {

        val state = Environment.getExternalStorageState()

        if (Environment.MEDIA_MOUNTED == state) {
            val camera = Camera.open()
            val parameters = camera.parameters
            val sizeList = parameters.supportedPictureSizes

            // 원하는 최적화 사이즈를 1280x720 으로 설정
//            val size = getOptimalPictureSize(parameters.supportedPictureSizes, 1280, 720)
            val size = getOptimalPictureSize(sizeList, 667, 500)
            parameters.setPreviewSize(size.width, size.height)
            parameters.setPictureSize(size.width, size.height)

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
                    imageUri = providerURI

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerURI)

                    startActivityForResult(takePictureIntent, requestCode)
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
                logger.debug("가로사이즈 변경 / 기존 가로사이즈 : ${prevSize.width}, 새 가로사이즈 : ${optSize.width}")
            }
            // 이전 사이즈보다 현재 사이즈의 세로사이즈 차이가 적을 경우 && 현재까지 최적화 된 가로길이 차이보다 현재 가로길이 차이가 적거나 같을 경우에만 적용
            if (diffHeight < diffHeightPrev && diffWidth <= diffWidthOpt) {
                optSize = size
                logger.debug("세로사이즈 변경 / 기존 세로사이즈 : ${prevSize.height}, 새 세로사이즈 : ${optSize.height}")
            }

            // 현재까지 사용한 사이즈를 이전 사이즈로 지정
            prevSize = size
        }
        logger.debug("결과 OptimalPictureSize : ${optSize.width}, ${optSize.height}")
        return optSize
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
        lateinit var childImageView: ImageView
        lateinit var videoImageView: ImageView
        //lateinit var gallaryImageView: ImageView
        lateinit var cameraImageView: ImageView
        lateinit var deleteImageView: ImageView
        lateinit var childCameraImageView: ImageView
        lateinit var childDeleteImageView: ImageView
        lateinit var videoFromCameraImageView: ImageView
        lateinit var videoFromDeleteImageView: ImageView

        lateinit var relationshipSpinner: Spinner
        lateinit var reasonSpinner: Spinner

        lateinit var substitutedSwitch: Switch
        lateinit var remarkEditText: EditText

        lateinit var typeViewContainer: RadioGroup
        lateinit var videoContainer: LinearLayout
        lateinit var videoButtonContainer: LinearLayout

        lateinit var returnContainer: LinearLayout
        lateinit var returnRemarkTitleTextView: TextView
        lateinit var returnItemsContainer: LinearLayout

        lateinit var aclScanTitleTextView: TextView
        lateinit var childPhotoTitleTextView: TextView
        lateinit var videoTitleTextView: TextView

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
                            bottomPadding = dimen(R.dimen.px20)
                            gravity = Gravity.TOP

                            typeTitleTextView = textView("*2.1 " + owner.getString(R.string.label_type))

                            typeViewContainer = radioGroup {
                                orientation = RadioGroup.VERTICAL
                            }
                        }.lparams(width = matchParent, height = dimen(R.dimen.px290))

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

                        childPhotoTitleTextView = textView("2.5 " + owner.getString(R.string.label_child_photo)) {
                            gravity = Gravity.CENTER_VERTICAL
                            setTypeface(null, Typeface.BOLD)
                        }.lparams(width = matchParent, height = dimen(R.dimen.px70))

                        linearLayout {
                            leftPadding = dimen(R.dimen.px30)
                            gravity = Gravity.CENTER_VERTICAL

                            frameLayout {
                                view { backgroundColorResource = R.color.color888888 }.lparams(width = matchParent, height = dip(1))

                                childImageView = imageView {
                                    imageResource = R.drawable.icon_2
                                }.lparams(width = dimen(R.dimen.px96), height = dimen(R.dimen.px116)) {
                                    gravity = Gravity.CENTER
                                }

                                view { backgroundColorResource = R.color.color888888 }.lparams(width = matchParent, height = dip(1)) { gravity = Gravity.BOTTOM }
                            }.lparams(width = dimen(R.dimen.px218), height = dimen(R.dimen.px290))

                            linearLayout {
                                space {}.lparams(width = dimen(R.dimen.px20), height = matchParent)
                                childCameraImageView = imageView {
                                    imageResource = R.drawable.b_camera02
                                }.lparams(width = dimen(R.dimen.px70), height = dimen(R.dimen.px70))

                                space {}.lparams(width = dimen(R.dimen.px20), height = matchParent)
                                childDeleteImageView = imageView {
                                    imageResource = R.drawable.b_delete02
                                }.lparams(width = dimen(R.dimen.px70), height = dimen(R.dimen.px70))
                            }
                        }

                        videoTitleTextView = textView("2.6 " + owner.getString(R.string.label_child_video)) {
                            gravity = Gravity.CENTER_VERTICAL
                            leftPadding = dimen(R.dimen.px20)
                        }.lparams(width = matchParent, height = dimen(R.dimen.px40))

                        linearLayout {
                            videoContainer = verticalLayout {
                                videoImageView = imageView {
                                    imageResource = R.drawable.movie
                                }.lparams(width = dimen(R.dimen.px315), height = dimen(R.dimen.px420)) {
                                    gravity = Gravity.CENTER
                                }

                                videoButtonContainer = linearLayout {
                                    gravity = Gravity.CENTER
                                    //videoFromGalleryImageView = imageView {
                                    //    imageResource = R.drawable.b_gallery02
                                    //}.lparams(width = dimen(R.dimen.px70), height = dimen(R.dimen.px70))

                                    space {}.lparams(width = dimen(R.dimen.px20), height = matchParent)
                                    videoFromCameraImageView = imageView {
                                        imageResource = R.drawable.b_camera02
                                    }.lparams(width = dimen(R.dimen.px70), height = dimen(R.dimen.px70))

                                    space {}.lparams(width = dimen(R.dimen.px20), height = matchParent)
                                    videoFromDeleteImageView = imageView {
                                        imageResource = R.drawable.b_delete02
                                    }.lparams(width = dimen(R.dimen.px70), height = dimen(R.dimen.px70))
                                }.lparams(width = matchParent, height = dimen(R.dimen.px116))
                            }.lparams(width = 0, weight = 1f)
                            space { }.lparams(width = 0, weight = 1f)
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
