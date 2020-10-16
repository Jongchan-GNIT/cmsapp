@file:Suppress("LocalVariableName", "PrivatePropertyName", "ConstantConditionIf", "DEPRECATION")

package kr.goodneighbors.cms.ui.childlist

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.arch.lifecycle.Observer
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
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
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ArrayAdapter
import android.widget.Button
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
import de.hdodenhof.circleimageview.CircleImageView
import kr.goodneighbors.cms.R
import kr.goodneighbors.cms.common.Constants
import kr.goodneighbors.cms.common.RangeInputFilter
import kr.goodneighbors.cms.extensions.*
import kr.goodneighbors.cms.service.entities.ATCH_FILE
import kr.goodneighbors.cms.service.entities.CD
import kr.goodneighbors.cms.service.entities.CH_BSC
import kr.goodneighbors.cms.service.entities.CH_MST
import kr.goodneighbors.cms.service.entities.CH_SPSL_INFO
import kr.goodneighbors.cms.service.entities.EDU
import kr.goodneighbors.cms.service.entities.FMLY
import kr.goodneighbors.cms.service.entities.HLTH
import kr.goodneighbors.cms.service.entities.INTV
import kr.goodneighbors.cms.service.entities.PRSN_ANS_INFO
import kr.goodneighbors.cms.service.entities.REMRK
import kr.goodneighbors.cms.service.entities.RPT_BSC
import kr.goodneighbors.cms.service.entities.RPT_DIARY
import kr.goodneighbors.cms.service.entities.SIBL
import kr.goodneighbors.cms.service.model.CifEditViewItem
import kr.goodneighbors.cms.service.model.PersonalInfoItem
import kr.goodneighbors.cms.service.model.SpinnerOption
import kr.goodneighbors.cms.service.viewmodel.CifViewModel
import kr.goodneighbors.cms.ui.DialogImageViewFragment
import kr.goodneighbors.cms.ui.MapsVillageActivity
import kr.goodneighbors.cms.ui.QrScanActivity
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.bottomPadding
import org.jetbrains.anko.button
import org.jetbrains.anko.dimen
import org.jetbrains.anko.dip
import org.jetbrains.anko.editText
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.hintResource
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.imageView
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.radioButton
import org.jetbrains.anko.radioGroup
import org.jetbrains.anko.scrollView
import org.jetbrains.anko.sdk25.coroutines.onCheckedChange
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onItemSelectedListener
import org.jetbrains.anko.sdk25.coroutines.textChangedListener
//import org.jetbrains.anko.space
import org.jetbrains.anko.spinner
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.space
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.switch
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textResource
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.view
import org.jetbrains.anko.wifiManager
import org.jetbrains.anko.wrapContent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class CifFragment : Fragment() {
    companion object {
        const val PICK_FROM_CAMERA = 0
        const val PICK_FROM_ALBUM = 1
        const val CROP_IMAGE = 2
        const val PICK_FROM_ALBUM_PROFILE = 3
        const val PICK_FROM_ALBUM_BIRTH = 4
        const val PICK_FROM_CAMERA_BIRTH = 5

        const val CROP_IMAGE_PICK_FROM_ALBUM = 6

        const val REQUEST_SIGNATURE_IMAGE = 10

        const val REQUEST_SIBLING_SPONSORSHIP = 11
        const val REQUEST_DUPLICATE = 12
        const val REQUEST_QR_CODE = 13

        const val RPT_STCD_TEMP = "12"
        const val RPT_STCD_STANDBY = "13"

        fun newInstance(rcp_no: String? = null
                        , reintake_origin: String? = null, reintake_link: String? = null, reintake_reason: String? = null): CifFragment {
            val fragment = CifFragment()
            val args = Bundle()
            args.putString("rcp_no", rcp_no ?: "")
            args.putString("reintake_origin", reintake_origin)
            args.putString("reintake_link", reintake_link)
            args.putString("reintake_reason", reintake_reason)

            fragment.arguments = args
            return fragment
        }
    }

    private val logger: Logger by lazy {
        LoggerFactory.getLogger(CifFragment::class.java)
    }

    private val viewModel: CifViewModel by lazy {
        //        ViewModelProviders.of(this).get(CifViewModel::class.java)
        CifViewModel()
    }

    private val ui = FragmentUI()
    private lateinit var defaultColorList: ColorStateList

    private val sdMain = Environment.getExternalStorageDirectory()
    private val contentsRootDir = File("$sdMain/${Constants.DIR_HOME}/${Constants.DIR_CONTENTS}")

    private var editViewItem: CifEditViewItem? = null

    private var rcp_no: String? = null
    private var reintake_origin: String? = null
    private var reintake_link: String? = null
    private var reintake_reason: String? = null

    private var isEditable = true

    private var generalImage: File? = null
    private var profileImage: File? = null
    private var birthImage: File? = null
    private var consentImage: File? = null

    private var mCurrentPhotoPath: String = ""

    private var imageUri: Uri? = null
    private var photoURI: Uri? = null
    private var albumURI: Uri? = null

    private var siblings: ArrayList<String>? = null

    private var returnCode: String? = null

    private var isInitPlan: Boolean = false
    private var initPlan: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rcp_no = arguments!!.getString("rcp_no")
        reintake_origin = arguments!!.getString("reintake_origin")
        reintake_link = arguments!!.getString("reintake_link")
        reintake_reason = arguments!!.getString("reintake_reason")

        viewModel.getCifEditViewItem().observe(this, Observer {

            it?.apply {
                codeVillage?.apply { ui.villageSpinner.setItem(items = this, hint = "Select Village") }
                codeDisability?.apply { ui.disabilitySpinner.setItem(items = this, hint = "Select Disability") }
                codeDisabilityReason?.apply { ui.disabilityReasonSpinner.setItem(items = this, hint = "Select Reason") }
                codeIllness?.apply { ui.illnessSpinner.setItem(items = this, hint = "Select Illness") }
                codeIllnessReason?.apply { ui.illnessReasonSpinner.setItem(items = this, hint = "Select Reason") }
                codeSchoolType?.apply { ui.schoolTypeSpinner.setItem(items = this, hint = "Select School Type") }
                codeSchoolTypeReason?.apply { ui.schoolTypeReasonSpinner.setItem(items = this, hint = "Select not attending school reason") }
                codeSchoolName?.apply { ui.schoolNameSpinner.setItem(items = this, hint = "Select School Name") }


                codeSupportCountry?.apply { ui.supportCountrySpinner.setItem(items = this) }
                ui.supplyPlanSpinner.setItem(items = it.getSupplyPlan(ui.supportCountrySpinner.getValue() ?: ""))

                codeRelationship?.apply { ui.relationshipSpinner.setItem(items = this, hint = "Select Relationship") }
                codeInterviewPlace?.apply { ui.interviewPlaceSpinner.setItem(items = this, hint = "Select Interview Place") }

                codeFatherReason?.apply {
                    ui.fatherReasonSpinner.setItem(items = this, hint = "Select Reason")
                    ui.motherReasonSpinner.setItem(items = this, hint = "Select Reason")
                }
                codeMainGuardian?.apply {
                    ui.mainguardianSpinner.setItem(items = this, hint = "Select Guardian")
                    ui.incomeProviderSpinner.setItem(items = this, hint = "Select Provider")
                }
                codeSpecialCase?.apply {
                    // special case 1
                    run {
                        val options = arrayListOf(SpinnerOption("", ""))
                        options.addAll(this)

                        val spinnerAdapter = object : ArrayAdapter<SpinnerOption>(context, R.layout.spinneritem_dark, options) {
                            override fun isEnabled(position: Int): Boolean {
                                return !(position > 0 && (ui.specialCase2Spinner.selectedItemPosition == position || ui.specialCase3Spinner.selectedItemPosition == position))
                            }

                            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
                                val view = super.getDropDownView(position, convertView, parent)
                                val tv = view as TextView
                                if (position > 0 && (ui.specialCase2Spinner.selectedItemPosition == position || ui.specialCase3Spinner.selectedItemPosition == position)) {
                                    // Set the disable item text color
                                    tv.setTextColor(Color.GRAY)
                                } else {
                                    tv.setTextColor(Color.BLACK)
                                }
                                return view
                            }
                        }

                        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                        ui.specialCase1Spinner.adapter = spinnerAdapter
                        ui.specialCase1Spinner.tag = options
                    }

                    // special case 2
                    run {
                        val options = arrayListOf(SpinnerOption("", ""))
                        options.addAll(this)

                        val spinnerAdapter = object : ArrayAdapter<SpinnerOption>(context, R.layout.spinneritem_dark, options) {
                            override fun isEnabled(position: Int): Boolean {
                                return !(position > 0 && (ui.specialCase1Spinner.selectedItemPosition == position || ui.specialCase3Spinner.selectedItemPosition == position))
                            }

                            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
                                val view = super.getDropDownView(position, convertView, parent)
                                val tv = view as TextView
                                if (position > 0 && (ui.specialCase1Spinner.selectedItemPosition == position || ui.specialCase3Spinner.selectedItemPosition == position)) {
                                    tv.setTextColor(Color.GRAY)
                                } else {
                                    tv.setTextColor(Color.BLACK)
                                }
                                return view
                            }
                        }

                        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                        ui.specialCase2Spinner.adapter = spinnerAdapter
                        ui.specialCase2Spinner.tag = options
                    }

                    // special case 3
                    run {
                        val options = arrayListOf(SpinnerOption("", ""))
                        options.addAll(this)

                        val spinnerAdapter = object : ArrayAdapter<SpinnerOption>(context, R.layout.spinneritem_dark, options) {
                            override fun isEnabled(position: Int): Boolean {
                                return !(position > 0 && (ui.specialCase1Spinner.selectedItemPosition == position || ui.specialCase2Spinner.selectedItemPosition == position))
                            }

                            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
                                val view = super.getDropDownView(position, convertView, parent)
                                val tv = view as TextView
                                if (position > 0 && (ui.specialCase1Spinner.selectedItemPosition == position || ui.specialCase2Spinner.selectedItemPosition == position)) {
                                    // Set the disable item text color
                                    tv.setTextColor(Color.GRAY)
                                } else {
                                    tv.setTextColor(Color.BLACK)
                                }
                                return view
                            }
                        }

                        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                        ui.specialCase3Spinner.adapter = spinnerAdapter
                        ui.specialCase3Spinner.tag = options
                    }
                }

                AnkoContext.createDelegate(ui.personalInfoLayout).apply {
                    verticalLayout {
                        codePersonalInfo?.apply {
                            forEachIndexed { index, personalInfoItem ->
                                textView("*4.${index + 1} ${personalInfoItem.master.CD_ENM}") {
                                }.lparams(width = matchParent, height = wrapContent)

                                spinner {
                                    val options = arrayListOf(SpinnerOption("", ""))
                                    options.addAll(personalInfoItem.detail)

                                    val spinnerAdapter = ArrayAdapter(context, R.layout.spinneritem_dark, options)
                                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                                    adapter = spinnerAdapter
                                    tag = PersonalInfoItem(personalInfoItem.master, options)
                                }.lparams(width = matchParent, height = wrapContent)
                            }
                        }
                    }
                }
            }

            editViewItem = it
            editViewItem?.rpt_bsc?.apply {
                loadData(this)

                ui.duplicateCheckButton.isEnabled = false
            } ?: run {
                ui.disabilitySpinner.isEnabled = false
                ui.disabilityReasonSpinner.isEnabled = false

                ui.illnessSpinner.isEnabled = false
                ui.illnessReasonSpinner.isEnabled = false

                ui.fatherReasonSpinner.isEnabled = true
                ui.motherReasonSpinner.isEnabled = true
                ui.fatherSwitch.isChecked = true
                ui.motherSwitch.isChecked = true

                ui.specialCase1Spinner.isEnabled = false
                ui.specialCase2Spinner.isEnabled = false
                ui.specialCase3Spinner.isEnabled = false

                ui.duplicateCheckButton.isEnabled = true
                ui.duplicateCheckContainer.visibility = View.GONE

                ui.remarkEditText.setText(reintake_reason ?: "")

                updateBirthImage()
                updateConsentImage()
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = ui.createView(AnkoContext.create(requireContext(), this))
        activity?.title = "CIF"

        defaultColorList = ui.title1TextView.textColors
        initUI()

        viewModel.setRcpNo(rcp_no)
        return v
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        activity?.menuInflater?.inflate(R.menu.toolbar_cif, menu)

        // 저장 버튼 클릭
        menu?.findItem(R.id.cif_toolbar_save)!!.setOnMenuItemClickListener {
            if (validate0() && validate1() && validate2() && validate3() && validate4() && validate5()) {
                save(RPT_STCD_STANDBY)
            }
            true
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        logger.debug("onActivityResult = requestCode : $requestCode // result code : $resultCode // data : $data")
        when (requestCode) {
            REQUEST_QR_CODE -> {
                if (resultCode == Activity.RESULT_OK && data?.extras != null) {
                    val s = data.extras?.getString("qrcode")

                    if (siblings == null) siblings = ArrayList()

                    if (!s.isNullOrBlank()) {
                        if (siblings!!.contains(s)) {
                            toast(R.string.message_validate_registered_sibling)
                        } else {

                            siblings!!.add(s)
                            onChangeSiblings()
                        }
                    }
                }
            }
            REQUEST_SIBLING_SPONSORSHIP -> {
                ui.siblingSponsorshipEditText.setText("")
                ui.siblingSponsorshipEditText.clearFocus()

                if (resultCode == Activity.RESULT_OK && data?.extras != null) {
                    val s = data.extras?.getStringArrayList("siblings")
                    if (siblings == null) siblings = ArrayList()

                    s?.forEach {
                        if (siblings!!.contains(it)) {
                            toast(R.string.message_validate_registered_sibling)
                        } else {
                            siblings!!.add(it)
                        }
                    }
                    onChangeSiblings()
                }
            }
            REQUEST_DUPLICATE -> {
                if (resultCode == Activity.RESULT_OK && data?.extras != null) {
                    val s = data.extras?.getBoolean("continue") ?: false
                    if (s) {
                        ui.duplicateCheckButton.isEnabled = false
                        ui.duplicateCheckContainer.visibility = View.VISIBLE
                    } else {
                        activity!!.onBackPressed()
                    }
                }
            }
            PICK_FROM_CAMERA -> {
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        logger.debug("PICK_FROM_CAMERA : {}", imageUri)
                        val originalFile = mCurrentPhotoPath

                        val sizedImageFile = createImageFile()
                        logger.debug("PICK_FROM_CAMERA resize: {}", sizedImageFile.path)

                        generalImage = sizedImageFile

                        fun onResizePhoto() {
                            val origin = File(originalFile)

                            Glide.with(this).load(sizedImageFile).into(ui.photoImageView)

                            val photoCropImageFile = createImageFile()
                            val providerURI = FileProvider.getUriForFile(activity!!, "kr.goodneighbors.cms.provider", origin)
                            photoURI = providerURI
                            albumURI = Uri.fromFile(photoCropImageFile)
                            cropImage()
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
                                        logger.error("Glide.onLoadFailed", errorDrawable)
                                    }
                                })
                    } catch (e: Exception) {
                        logger.error("REQUEST_TAKE_PHOTO", e)
                    }
                }
            }
            CROP_IMAGE_PICK_FROM_ALBUM -> {
                if (resultCode == Activity.RESULT_OK) {
                    generalImage = File(albumURI!!.path)
                    Glide.with(this).load(albumURI).into(ui.photoImageView)
                } else {
                    val file = File(albumURI!!.path)
                    if (file.exists()) file.delete()
                }
            }
            PICK_FROM_ALBUM -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (data?.data != null) {
                        try {
                            val albumFile = createImageFile()
                            photoURI = data.data
                            albumURI = Uri.fromFile(albumFile)
                            generalImage = albumFile
                            cropImage_general()
                        } catch (e: Exception) {
                            logger.error("REQUEST_TAKE_ALBUM", e)
                        }
                    }
                }
            }
            /*
            PICK_FROM_ALBUM -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (data?.data != null) {
                        try {
                            generalImage = File(data.data!!.getRealPath(context!!))

                            Glide.with(this).load(data.data).into(ui.photoImageView)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            logger.error("REQUEST_TAKE_ALBUM", e)
                        }
                    }
                }
            }
            */
            PICK_FROM_ALBUM_PROFILE -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (data?.data != null) {
                        try {
                            val albumFile = createImageFile()
                            photoURI = data.data
                            albumURI = Uri.fromFile(albumFile)
                            profileImage = albumFile
                            cropImage()
                        } catch (e: Exception) {
                            logger.error("REQUEST_TAKE_ALBUM", e)
                        }
                    }
                }
            }
            CROP_IMAGE -> {
                if (resultCode == Activity.RESULT_OK) {
                    profileImage = File(albumURI!!.path)

                    Glide.with(this).load(albumURI).into(ui.profileImageImageView)
                } else {
                    val file = File(albumURI!!.path)
                    if (file.exists()) file.delete()
                }
            }
            REQUEST_SIGNATURE_IMAGE -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (data?.extras != null) {
                        val uri = data.extras!!.getString("URI")

                        val file = File(uri)
                        updateConsentImage(file)
                    }
                }
            }
            PICK_FROM_ALBUM_BIRTH -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (data?.data != null) {
                        try {
                            val file = File(data.data!!.getRealPath(context!!))
                            updateBirthImage(file)

                        } catch (e: Exception) {
                            logger.error("PICK_FROM_ALBUM_BIRTH", e)
                        }
                    }
                }
            }
            PICK_FROM_CAMERA_BIRTH -> {
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        logger.debug("PICK_FROM_CAMERA_BIRTH : {}", imageUri)
                        val originalFile = mCurrentPhotoPath

                        val sizedImageFile = createImageFile()
                        logger.debug("PICK_FROM_CAMERA_BIRTH resize: {}", sizedImageFile.path)

                        birthImage = sizedImageFile

                        fun onResizePhoto() {
                            val origin = File(originalFile)
                            if (origin.exists() && origin.isFile) {
                                origin.delete()
                            }

                            updateBirthImage(sizedImageFile)
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
                                        logger.error("Glide.onLoadFailed", errorDrawable)
                                    }
                                })
                    } catch (e: Exception) {
                        logger.error("PICK_FROM_CAMERA_BIRTH", e)
                    }
                }

            }
        }
    }

    fun isEditable(): Boolean {
        return isEditable
    }

    @SuppressLint("SetTextI18n")
    private fun initUI() {
        ui.title1.onClick {
            if (!isEditable || validate0()) {
                toggleDetail(ui.toggleButton1, ui.detail1)
            }
        }

        ui.title2.onClick {
            if (!isEditable || validate1()) {
                toggleDetail(ui.toggleButton2, ui.detail2)
            }
        }

        ui.title3.onClick {
            if (!isEditable || validate2()) {
                toggleDetail(ui.toggleButton3, ui.detail3)
            }
        }

        ui.title4.onClick {
            if (!isEditable || validate3()) {
                toggleDetail(ui.toggleButton4, ui.detail4)
            }
        }

        ui.title5.onClick {
            if (!isEditable || validate4()) {
                toggleDetail(ui.toggleButton5, ui.detail5)
            }
        }

        ui.nextButton1.onClick {
            if (validate1()) {
                save((if (returnCode == null) RPT_STCD_TEMP else returnCode)!!)
                toggleDetail(ui.toggleButton1, ui.detail1)

                if (ui.detail2.visibility == View.GONE) {
                    toggleDetail(ui.toggleButton2, ui.detail2)
                }
            } else {
                toast(R.string.message_require_fields)
            }
        }

        ui.nextButton2.onClick {
            if (validate2()) {
                save((if (returnCode == null) RPT_STCD_TEMP else returnCode)!!)
                toggleDetail(ui.toggleButton2, ui.detail2)

                if (ui.detail3.visibility == View.GONE) {
                    toggleDetail(ui.toggleButton3, ui.detail3)
                }
            } else {
                toast(R.string.message_require_fields)
            }
        }

        ui.nextButton3.onClick {
            if (validate3()) {
                save((if (returnCode == null) RPT_STCD_TEMP else returnCode)!!)
                toggleDetail(ui.toggleButton3, ui.detail3)

                if (ui.detail4.visibility == View.GONE) {
                    toggleDetail(ui.toggleButton4, ui.detail4)
                }
            } else {
                toast(R.string.message_require_fields)
            }
        }

        ui.nextButton4.onClick {
            if (validate4()) {
                save((if (returnCode == null) RPT_STCD_TEMP else returnCode)!!)
                toggleDetail(ui.toggleButton4, ui.detail4)

                if (ui.detail5.visibility == View.GONE) {
                    toggleDetail(ui.toggleButton5, ui.detail5)
                }
            } else {
                toast(R.string.message_require_fields)
            }
        }

        ui.nextButton5.onClick {
            if (validate5()) {
                save((if (returnCode == null) RPT_STCD_TEMP else returnCode)!!)
                setHasOptionsMenu(true)
                toggleDetail(ui.toggleButton5, ui.detail5)
            } else {
                toast(R.string.message_require_fields)
            }
        }

        // 일반 사진 앨범
        ui.photoAlbumButton.onClick {
            getAlbumImage(PICK_FROM_ALBUM)
        }

        // 일반 사진 카메라
        ui.photoCameraButton.onClick {
            captureCamera(PICK_FROM_CAMERA)
        }

        // 일반 사진 삭제
        ui.photoDeleteButton.onClick {
            ui.photoImageView.imageResource = R.drawable.icon_2
            generalImage = null
        }

        // 프로필 사진 앨범
        ui.profileImageAlbumButton.onClick {
            getAlbumImage(PICK_FROM_ALBUM_PROFILE)
        }

        // 프로필 사진 삭제
        ui.profileImageDeleteButton.onClick {
            ui.profileImageImageView.imageResource = R.drawable.icon_2
            profileImage = null
        }

        // 출생증명서 앨범
        //ui.birthAlbumButton.onClick {
        //    getAlbumImage(PICK_FROM_ALBUM_BIRTH)
        //}

        // 출생증명서 카메라
        ui.birthCameraButton.onClick {
            captureCamera(PICK_FROM_CAMERA_BIRTH)
        }

        // 출생증명서 삭제
        ui.birthDeleteButton.onClick {
            updateBirthImage()
        }

        ui.birthImageView.onClick {
            birthImage?.apply {
                val ft = activity!!.supportFragmentManager.beginTransaction()
                val newFragment = DialogImageViewFragment.newInstance(this.path)
                newFragment.show(ft, "birth_view")
            }
        }

        // 보호자 서명 등록
        ui.consentAddButton.onClick {
            val ft = activity!!.supportFragmentManager.beginTransaction()
            val newFragment = CifDialogConsentFragment.newInstance(rcp_no ?: "")
            newFragment.setTargetFragment(this@CifFragment, REQUEST_SIGNATURE_IMAGE)
            newFragment.show(ft, "consent")
        }

        // 보호자 서명 삭제
        ui.consentDeleteButton.onClick {
            updateConsentImage()
        }

        // 보호자 서명 클릭
        ui.consentImageView.onClick {
            consentImage?.apply {
                val ft = activity!!.supportFragmentManager.beginTransaction()
                val newFragment = DialogImageViewFragment.newInstance(this.path)
                newFragment.show(ft, "consent_view")
            }
        }

        // Support country -> Supply plan
        ui.supportCountrySpinner.onItemSelectedListener {
            onItemSelected { _, _, _, id ->
                ui.supplyPlanSpinner.setItem(editViewItem?.getSupplyPlan(ui.supportCountrySpinner.getValue() ?: ""))
                if (!isInitPlan) {
                    ui.supplyPlanSpinner.setSelection(initPlan, false)
                    isInitPlan = true
                }
            }
        }

        ui.schoolTypeOtherEditText.isEnabled = false
        ui.schoolTypeReasonSpinner.isEnabled = false
        ui.schoolGradeEditText.isEnabled = false
        ui.schoolNameSpinner.isEnabled = false

        ui.schoolTypeSpinner.onItemSelectedListener {
            onItemSelected { _, _, _, _ ->
                logger.debug("""$isEditable && ${ui.schoolTypeSpinner.getValue()} != "" && ${ui.schoolTypeSpinner.getValue()} != "1" && ${ui.schoolTypeSpinner.getValue()} != "2" = ${isEditable && ui.schoolTypeSpinner.getValue() != "" && ui.schoolTypeSpinner.getValue() != "1" && ui.schoolTypeSpinner.getValue() != "2"}""")
                ui.schoolTypeTitleTextView.setText("")
                ui.schollTypeMessageTextView.visibility = View.GONE
                ui.schoolTypeOtherEditText.isEnabled = isEditable && ui.schoolTypeSpinner.getValue() == "99"
                if (!ui.schoolTypeOtherEditText.isEnabled) {
                    ui.schoolTypeOtherEditText.setText("")
                    ui.schoolTypeOtherEditText.visibility = View.GONE
                }else{
                    ui.schoolTypeOtherEditText.visibility = View.VISIBLE
                }

                ui.schoolTypeOtherEditText.hint = if (ui.schoolTypeOtherEditText.isEnabled) getString(R.string.label_others) else ""

                ui.schoolTypeReasonSpinner.isEnabled = isEditable && ui.schoolTypeSpinner.getValue() == "1"
                ui.schoolNameSpinner.isEnabled = isEditable && ui.schoolTypeSpinner.getValue() != "" && ui.schoolTypeSpinner.getValue() != "1" && ui.schoolTypeSpinner.getValue() != "2" && ui.schoolTypeSpinner.getValue() != "13" && ui.schoolTypeSpinner.getValue() != "14" && ui.schoolTypeSpinner.getValue() != "15" && ui.schoolTypeSpinner.getValue() != "16" && ui.schoolTypeSpinner.getValue() != "99"
                ui.schoolGradeEditText.isEnabled = isEditable && ui.schoolTypeSpinner.getValue() != "" && ui.schoolTypeSpinner.getValue() != "1" && ui.schoolTypeSpinner.getValue() != "2" && ui.schoolTypeSpinner.getValue() != "13" && ui.schoolTypeSpinner.getValue() != "14" && ui.schoolTypeSpinner.getValue() != "15" && ui.schoolTypeSpinner.getValue() != "16" && ui.schoolTypeSpinner.getValue() != "99"
                if (ui.schoolTypeSpinner.getValue() == "1" || ui.schoolTypeSpinner.getValue() == "2" || ui.schoolTypeSpinner.getValue() == "13" || ui.schoolTypeSpinner.getValue() == "14" || ui.schoolTypeSpinner.getValue() == "15" || ui.schoolTypeSpinner.getValue() == "16" || ui.schoolTypeSpinner.getValue() == "99") {
                    ui.schoolTypeReasonSpinner.setSelection(0)
                    ui.schoolNameSpinner.setSelection(0)
                    ui.schoolGradeEditText.setText("")
                    /*
                    if (ui.schoolTypeSpinner.getValue() == "2" && ui.ageEditText.getStringValue().toInt() > 12){
                        ui.schoolTypeSpinner.setSelection(0)
                        ui.schollTypeMessageTextView.textResource = R.string.message_validate_range_nursery_13
                        ui.schoolTypeTitleTextView.textColorResource = R.color.colorAccent
                    }else{
                        ui.schoolTypeTitleTextView.setTextColor(defaultColorList)
                    }
                    */
                }
            }
        }

        ui.schoolTypeReasonSpinner.onItemSelectedListener {
            onItemSelected { _, _, _, _ ->
                if (ui.schoolTypeReasonSpinner.getValue() == "1" && ui.ageEditText.getStringValue().toInt() > 5) {
                    //ui.schollTypeMessageTextView.visibility = View.VISIBLE
                    //ui.schoolTypeTitleTextView.textColorResource = R.color.colorAccent
                    //ui.schollTypeMessageTextView.textResource = R.string.message_validate_under_school_age
                    toast(R.string.message_validate_under_school_age)
                    ui.schoolTypeReasonSpinner.setSelection(0)
                }else{
                    ui.schollTypeMessageTextView.visibility = View.GONE
                    ui.schoolTypeTitleTextView.setTextColor(defaultColorList)
                }

            }
        }

        // 생년월일 다이얼로그
        ui.birthDateEditText.onClick {
            val c = Calendar.getInstance()
            var mYear = c.get(Calendar.YEAR)
            var mMonth = c.get(Calendar.MONTH)
            var mDay = c.get(Calendar.DAY_OF_MONTH)

            if (!ui.birthDateEditText.getStringValue().isBlank()) {
                val b = ui.birthDateEditText.getStringValue()
                mYear = b.convertDateFormat(before = "MM-dd-yyyy", after = "yyyy").toInt()
                mMonth = b.convertDateFormat(before = "MM-dd-yyyy", after = "MM").toInt() - 1
                mDay = b.convertDateFormat(before = "MM-dd-yyyy", after = "dd").toInt()
            }

            val datePickerDialog = DatePickerDialog(activity, android.R.style.Theme_Holo_Dialog,
                    DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                        ui.birthDateEditText.setText("""${(monthOfYear + 1).toString().padStart(2, '0')}-${dayOfMonth.toString().padStart(2, '0')}-$year""")
                    }, mYear, mMonth, mDay)
            datePickerDialog.datePicker.maxDate = Date().time
            datePickerDialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            datePickerDialog.show()
        }

        // 생년월일 -> 나이
        ui.birthDateEditText.textChangedListener {
            afterTextChanged {
                try {
                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                    val birthDate = ui.birthDateEditText.getStringValue()
                    if (birthDate.length == 10) {
                        val yearString = ui.birthDateEditText.getStringValue().convertDateFormat(before = "MM-dd-yyyy", after = "yyyy")
                        ui.ageEditText.setText((currentYear - yearString.toInt()).toString())
                    }

                    calculateBMI()
                   // ui.duplicateCheckButton.isEnabled = true
                   // ui.duplicateCheckContainer.visibility = View.GONE
                   // ui.detail2.visibility = View.GONE
                   // ui.detail3.visibility = View.GONE
                   // ui.detail4.visibility = View.GONE
                   // ui.detail5.visibility = View.GONE
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        ui.firstNameEditText.textChangedListener {
            afterTextChanged {
               // ui.duplicateCheckButton.isEnabled = true
               // ui.duplicateCheckContainer.visibility = View.GONE
               // ui.detail2.visibility = View.GONE
               // ui.detail3.visibility = View.GONE
               // ui.detail4.visibility = View.GONE
               // ui.detail5.visibility = View.GONE
            }
        }

        ui.lastNameEditText.textChangedListener {
            afterTextChanged {
               // ui.duplicateCheckButton.isEnabled = true
               // ui.duplicateCheckContainer.visibility = View.GONE
               // ui.detail2.visibility = View.GONE
               // ui.detail3.visibility = View.GONE
               // ui.detail4.visibility = View.GONE
               // ui.detail5.visibility = View.GONE
            }
        }

        ui.duplicateCheckButton.onClick {
            if (validateDuplicateInfo()) {
                viewModel.findAllDuplicateChildren(firstName = ui.firstNameEditText.getStringValue(),
                        middleName = ui.middleNameEditText.getStringValue(),
                        lastName = ui.lastNameEditText.getStringValue(),
                        gender = if (ui.genderRadioGroup.checkedRadioButtonId == ui.genderFemaleRadio.id) "F" else "M",
                        birth = ui.birthDateEditText.getStringValue().convertDateFormat(before = "MM-dd-yyyy", after = "yyyyMMdd")
                ).observeOnce(this@CifFragment, Observer { duplicateChildren ->
                    logger.debug("viewModel.findAllDuplicateChildren : $duplicateChildren")
                    if (duplicateChildren == null || duplicateChildren.isEmpty()) {
                        ui.duplicateCheckButton.isEnabled = false
                        ui.duplicateCheckContainer.visibility = View.VISIBLE
                    } else {
                        val ft = activity!!.supportFragmentManager.beginTransaction()
                        val newFragment = DuplicatedChildrenDialogFragment.newInstance(
                                firstName = ui.firstNameEditText.getStringValue(),
                                middleName = ui.middleNameEditText.getStringValue(),
                                lastName = ui.lastNameEditText.getStringValue(),
                                gender = if (ui.genderRadioGroup.checkedRadioButtonId == ui.genderFemaleRadio.id) "F" else "M",
                                birth = ui.birthDateEditText.getStringValue().convertDateFormat(before = "MM-dd-yyyy", after = "yyyyMMdd"))
                        newFragment.setTargetFragment(this@CifFragment, REQUEST_DUPLICATE)
                        newFragment.show(ft, "REQUEST_DUPLICATE")
                    }
                })
            } else {
                toast(R.string.message_require_fields)
            }
        }

        ui.heightEditText.textChangedListener {
            afterTextChanged {
                calculateBMI()
            }
        }

        ui.weightEditText.textChangedListener {
            afterTextChanged {
                calculateBMI()
            }
        }

        ui.genderRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            calculateBMI()
            //ui.duplicateCheckButton.isEnabled = true
            //ui.detail2.visibility = View.GONE
            //ui.detail3.visibility = View.GONE
            //ui.detail4.visibility = View.GONE
            //ui.detail5.visibility = View.GONE
        }

        ui.disabilitySwitch.onCheckedChange { _, isChecked ->
            ui.disabilitySpinner.isEnabled = isEditable && isChecked
            ui.disabilityReasonSpinner.isEnabled = isEditable && isChecked

            if (isChecked) {
                if (!ui.specialCaseSwitch.isChecked) {
                    ui.specialCaseSwitch.isChecked = true
                }
                if (!(ui.specialCase1Spinner.getValue() == "1" || ui.specialCase2Spinner.getValue() == "1" || ui.specialCase3Spinner.getValue() == "1")) {
                    when {
                        ui.specialCase1Spinner.getValue().isNullOrBlank() -> ui.specialCase1Spinner.setSelectKey("1")
                        ui.specialCase2Spinner.getValue().isNullOrBlank() -> ui.specialCase2Spinner.setSelectKey("1")
                        ui.specialCase3Spinner.getValue().isNullOrBlank() -> ui.specialCase3Spinner.setSelectKey("1")
                    }
                }
            } else {
                ui.disabilitySpinner.setSelection(0)
                ui.disabilityReasonSpinner.setSelection(0)
            }
        }

        ui.illnessSwitch.onCheckedChange { _, isChecked ->
            ui.illnessSpinner.isEnabled = isEditable && isChecked
            ui.illnessReasonSpinner.isEnabled = isEditable && isChecked

            if (!isChecked) {
                ui.illnessSpinner.setSelection(0)
                ui.illnessReasonSpinner.setSelection(0)
            }
        }

        ui.illnessSpinner.onItemSelectedListener {
            onItemSelected { _, _, _, _ ->
                val checklist = arrayOf("6", "9", "14", "16", "18", "21", "26", "27", "31")
                if (checklist.contains(ui.illnessSpinner.getValue())) {
                    if (!ui.specialCaseSwitch.isChecked) {
                        ui.specialCaseSwitch.isChecked = true
                    }

                    if (!(ui.specialCase1Spinner.getValue() == "2" || ui.specialCase2Spinner.getValue() == "2" || ui.specialCase3Spinner.getValue() == "2")) {
                        when {
                            ui.specialCase1Spinner.getValue().isNullOrBlank() -> ui.specialCase1Spinner.setSelectKey("2")
                            ui.specialCase2Spinner.getValue().isNullOrBlank() -> ui.specialCase2Spinner.setSelectKey("2")
                            ui.specialCase3Spinner.getValue().isNullOrBlank() -> ui.specialCase3Spinner.setSelectKey("2")
                        }
                    }
                }
            }
        }

        ui.fatherSwitch.onCheckedChange { _, isChecked ->
            if (isChecked) {
                ui.fatherReasonSpinner.isEnabled = false
                ui.fatherReasonSpinner.setSelection(0)
            } else {
                ui.fatherReasonSpinner.isEnabled = true
            }
        }

        ui.motherSwitch.onCheckedChange { _, isChecked ->
            if (isChecked) {
                ui.motherReasonSpinner.isEnabled = false
                ui.motherReasonSpinner.setSelection(0)
            } else {
                ui.motherReasonSpinner.isEnabled = true
            }
        }

        ui.mainguardianSpinner.onItemSelectedListener {
            onItemSelected { _, _, _, _ ->
                if (ui.mainguardianSpinner.getValue() == "7") {
                    ui.specialCaseSwitch.isChecked = true
                    when {
                        ui.specialCase1Spinner.getValue().isNullOrBlank() -> ui.specialCase1Spinner.setSelectKey("4")
                        ui.specialCase2Spinner.getValue().isNullOrBlank() -> ui.specialCase2Spinner.setSelectKey("4")
                        ui.specialCase3Spinner.getValue().isNullOrBlank() -> ui.specialCase3Spinner.setSelectKey("4")
                    }
                }
            }
        }

        ui.specialCaseSwitch.onCheckedChange { _, isChecked ->
            logger.debug("----------ui.specialCaseSwitch.onCheckedChange : $isChecked")
            ui.specialCase1Spinner.isEnabled = isEditable && isChecked
            ui.specialCase2Spinner.isEnabled = isEditable && isChecked
            ui.specialCase3Spinner.isEnabled = isEditable && isChecked

            if (!isChecked) {
                ui.specialCase1Spinner.setSelection(0)
                ui.specialCase2Spinner.setSelection(0)
                ui.specialCase3Spinner.setSelection(0)
            }
        }

        ui.siblingSponsorshipQrcodeImageView.onClick {
            if (siblings.isNullOrEmpty() || siblings!!.size < 2) {
                val intent = Intent(activity!!, QrScanActivity::class.java)
                startActivityForResult(intent, REQUEST_QR_CODE)
            } else {
                toast(R.string.message_exceeded_siblings_number)
            }
        }

        ui.siblingSponsorshipImageView.onClick {
            if (siblings.isNullOrEmpty() || siblings!!.size < 2) {
                if (ui.siblingSponsorshipEditText.getStringValue().isNotBlank()) {
                    val ft = activity!!.supportFragmentManager.beginTransaction()
                    val newFragment = SiblingSponsorshipFindDialogFragment.newInstance(word = ui.siblingSponsorshipEditText.getStringValue(), selectedCount = siblings?.size
                            ?: 0)
                    newFragment.setTargetFragment(this@CifFragment, REQUEST_SIBLING_SPONSORSHIP)
                    newFragment.show(ft, "REQUEST_SIBLING_SPONSORSHIP")
                } else {
                    toast(R.string.label_input_child_code_or_name)
                }
            } else {
                toast(R.string.message_exceeded_siblings_number)
            }
        }

        ui.villageMapButton.onClick {
            logger.debug("village : ${ui.villageSpinner.getValue()}")
//            if (requireContext().wifiManager.isWifiEnabled) {
            if (requireContext().isNetworkAvailable()) {
                if (!ui.villageSpinner.getValue().isNullOrBlank()) {
                    viewModel.getLocationOfVillage(ui.villageSpinner.getValue()!!).observeOnce(this@CifFragment, Observer { loc ->
                        if (loc != null && !loc.LAT.isNullOrBlank() && !loc.LNG.isNullOrBlank()) {
                            startActivity<MapsVillageActivity>(
                                    "name" to loc.VLG_NM,
                                    "lat" to loc.LAT,
                                    "lng" to loc.LNG
                            )
                        } else {
                            toast(R.string.message_location_is_not_define)
                        }
                    })
                }
            } else {
                toast(R.string.message_wifi_disabled)
            }
        }
    }

    private fun calculateBMI() {
        val birth = ui.birthDateEditText.getStringValue().convertDateFormat("MM-dd-yyyy", "yyyyMMdd")
        val height = ui.heightEditText.getStringValue()
        val weight = ui.weightEditText.getStringValue()
        val gender = if (ui.genderRadioGroup.checkedRadioButtonId == ui.genderFemaleRadio.id) "F" else "M"

        viewModel.getBMI(birth, height, weight, gender).observeOnce(this, Observer {
            if (it == null) {
                ui.growthEditText.setText("")
                ui.growthEditText.tag = null
            } else {
                ui.growthEditText.setText(it.CD_ENM)
                ui.growthEditText.tag = it
            }
        })
    }

    private fun toggleDetail(_cif_toggle_detail: ImageView, _cif_detail: LinearLayout) {
        if (_cif_detail.visibility == View.GONE) {
            val deg = _cif_toggle_detail.rotation - 180f
            _cif_toggle_detail.animate().rotation(deg).interpolator = AccelerateDecelerateInterpolator()

            _cif_detail.visibility = View.VISIBLE
        } else {
            val deg = _cif_toggle_detail.rotation + 180f
            _cif_toggle_detail.animate().rotation(deg).interpolator = AccelerateDecelerateInterpolator()

            _cif_detail.visibility = View.GONE
        }
    }

    private fun validate0(): Boolean {
        return generalImage != null
    }

    private fun validateDuplicateInfo(): Boolean {
        val isValidName = ui.firstNameEditText.getStringValue().isNotBlank() && ui.lastNameEditText.getStringValue().isNotBlank()

        val isValidGender = ui.genderFemaleRadio.isChecked || ui.genderMaleRadio.isChecked

        // birth date
        val isValidBirthdate = ui.birthDateEditText.getStringValue().isNotBlank()
        val age = ui.ageEditText.getStringValue()

        val isValidAge = age.isNumber() && age.toInt() in 2..13
        if (isValidBirthdate && isValidAge) {
            ui.birthDateMessageTextView.visibility = View.GONE
        } else {
            ui.birthDateMessageTextView.visibility = View.VISIBLE

        }

        if (isValidName) ui.childNameTextView.setTextColor(defaultColorList)
        else ui.childNameTextView.textColorResource = R.color.colorAccent

        if (isValidGender) ui.genderTitleTextView.setTextColor(defaultColorList)
        else ui.genderTitleTextView.textColorResource = R.color.colorAccent

        if (isValidBirthdate && isValidAge) ui.birthDateTitleTextView.setTextColor(defaultColorList)
        else ui.birthDateTitleTextView.textColorResource = R.color.colorAccent

        return isValidName && isValidGender && isValidBirthdate && isValidAge
    }

    @SuppressLint("SetTextI18n")
    private fun validate1(): Boolean {
        logger.debug("validate1()")

        var isValidate = false
        if (validateDuplicateInfo()) {
            // growth
            var isValidGrowth = ui.heightEditText.getStringValue().isNotBlank() && ui.weightEditText.getStringValue().isNotBlank()

            val height = ui.heightEditText.getStringValue()
            if (height.isNumber() && (height.toInt() in 50..220)) {
                ui.heightMessageTextView.visibility = View.GONE
            } else {
                ui.heightMessageTextView.visibility = View.VISIBLE
                isValidGrowth = false
            }

            val weight = ui.weightEditText.getStringValue()
            if (weight.isNumber() && (weight.toInt() in 5..120)) {
                ui.weightMessageTextView.visibility = View.GONE
            } else {
                ui.weightMessageTextView.visibility = View.VISIBLE
                isValidGrowth = false
            }
            if (isValidGrowth) ui.growthTitleTextView.setTextColor(defaultColorList)
            else ui.growthTitleTextView.textColorResource = R.color.colorAccent

            // home address
            val isValidHomeAddress = ui.villageSpinner.getValue()?.isNotBlank() ?: false
                    && ui.addressEditText.getStringValue().isNotBlank()

            if (isValidHomeAddress) ui.addressTitleTextView.setTextColor(defaultColorList)
            else ui.addressTitleTextView.textColorResource = R.color.colorAccent


            var isValidDisability = true
            if (ui.disabilitySwitch.isChecked
                    && (ui.disabilitySpinner.getValue().isNullOrBlank() || ui.disabilityReasonSpinner.getValue().isNullOrBlank())) {
                isValidDisability = false
            }
            if (isValidDisability) ui.disabilitySwitch.setTextColor(defaultColorList)
            else ui.disabilitySwitch.textColorResource = R.color.colorAccent

            var isValidIllness = true
            if (ui.illnessSwitch.isChecked
                    && (ui.illnessSpinner.getValue().isNullOrBlank() || ui.illnessReasonSpinner.getValue().isNullOrBlank())) {
                isValidIllness = false
            }
            if (isValidIllness) ui.illnessSwitch.setTextColor(defaultColorList)
            else ui.illnessSwitch.textColorResource = R.color.colorAccent

            // school type
            val age = ui.ageEditText.getStringValue()
            val isRequiredSchoolType = ui.schoolTypeSpinner.getValue()?.isNotBlank() ?: false
            var isValidSchoolType = true
            if (isRequiredSchoolType && age.isNumber()) {
                when (ui.schoolTypeSpinner.getValue()) {
                    "2" -> {
                        if (age.toInt() > 12) {
                            ui.schollTypeMessageTextView.textResource = R.string.message_validate_range_nursery_13
                            isValidSchoolType = false
                        }
                    }
                    "3", "4" -> {
                        if (age.toInt() > 99) {
                            isValidSchoolType = false
                        }
                    }
                    "99" -> {
                        if (ui.schoolTypeOtherEditText.getStringValue().isBlank()) {
                            isValidSchoolType = false
                            ui.schollTypeMessageTextView.textResource = R.string.message_validate_school_type_others
                        }
                }
                }
            }

            if (ui.schoolTypeReasonSpinner.getValue().isNullOrBlank() && ui.schoolTypeReasonSpinner.isEnabled == true) {
                isValidSchoolType = false
                ui.schollTypeMessageTextView.textResource = R.string.message_validate_not_attending_school
            }

            if (isValidSchoolType) {
                ui.schollTypeMessageTextView.visibility = View.GONE
                ui.schoolTypeTitleTextView.setTextColor(defaultColorList)
            } else {
                ui.schollTypeMessageTextView.visibility = View.VISIBLE
                ui.schoolTypeTitleTextView.textColorResource = R.color.colorAccent
            }


//      *1.8 *Grade 항목 값이 학년과 맞지 않을 경우 안내 메세지가 표시 됩니다.
//      It’s not Permitted Range (1~12 available)
//      초등학교: 1~12학년,
//      중고등학교: 1~18학년,
//      직업학교: 1~12학년
            val grade = ui.schoolGradeEditText.getStringValue()
            var isValidSchoolGrade = true

            if (isEditable && ui.schoolTypeSpinner.getValue() != "" && ui.schoolTypeSpinner.getValue() != "1" && ui.schoolTypeSpinner.getValue() != "2"
                    && ui.schoolTypeSpinner.getValue() != "13" && ui.schoolTypeSpinner.getValue() != "14" && ui.schoolTypeSpinner.getValue() != "15" && ui.schoolTypeSpinner.getValue() != "16" && ui.schoolTypeSpinner.getValue() != "99") {
                isValidSchoolGrade = grade.isNotBlank() && grade.isNumber()

                if (isValidSchoolGrade) {
                    when (ui.schoolTypeSpinner.getValue()) {
                        "3", "5" -> {
                            if (grade.toInt() !in 1..12) {
                                isValidSchoolGrade = false
                                ui.schoolGradeMessageTextView.textResource = R.string.message_validate_range_1_12
                            }
                        }
                        "4" -> {
                            if (grade.toInt() !in 1..18) {
                                isValidSchoolGrade = false
                                ui.schoolGradeMessageTextView.textResource = R.string.message_validate_range_1_18
                            }
                        }
                    }
                }
            }

            var isValidSchoolName = true
            if (ui.schoolNameSpinner.isEnabled) {
                if (ui.schoolNameSpinner.getValue().isNullOrBlank()) isValidSchoolName = false
            }
            if (isValidSchoolName) ui.schoolNameTitleTextView.setTextColor(defaultColorList)
            else ui.schoolNameTitleTextView.textColorResource = R.color.colorAccent

            if (isValidSchoolGrade) {
                ui.schoolGradeMessageTextView.visibility = View.GONE
                ui.schoolGradeTitleTextView.setTextColor(defaultColorList)
            } else {
                ui.schoolGradeMessageTextView.visibility = View.VISIBLE
                ui.schoolGradeTitleTextView.textColorResource = R.color.colorAccent
            }



            isValidate = isValidGrowth && isValidHomeAddress
                    && isValidDisability && isValidIllness
                    && isRequiredSchoolType && isValidSchoolName && isValidSchoolGrade
                    && isValidSchoolType
        }


        if (isValidate) {
            ui.title1.backgroundColorResource = R.color.colorBgLiteGray
            ui.title1ValidImageView.visibility = View.VISIBLE
        } else {
            ui.title1.backgroundResource = R.drawable.header_cif
            ui.title1ValidImageView.visibility = View.GONE
        }

        return isValidate
    }

    private fun validate2(): Boolean {
        // support country
        val isValidSupportCountry = ui.supportCountrySpinner.getValue()?.isNotBlank() ?: false
        if (isValidSupportCountry) ui.supportCountryTitleTextView.setTextColor(defaultColorList)
        else ui.supportCountryTitleTextView.textColorResource = R.color.colorAccent

        // supply plan
        val isValidSupplyPlan = ui.supplyPlanSpinner.getValue()?.isNotBlank() ?: false
        if (isValidSupplyPlan) ui.supplyPlanTitleTextView.setTextColor(defaultColorList)
        else ui.supplyPlanTitleTextView.textColorResource = R.color.colorAccent

        // respondent name
        val isValidRespondentName = ui.respondentNameEditText.getStringValue().isNotBlank()
        if (isValidRespondentName) ui.respondentNameTitleTextView.setTextColor(defaultColorList)
        else ui.respondentNameTitleTextView.textColorResource = R.color.colorAccent

        // relationship with child
        val isValidRelationship = ui.relationshipSpinner.getValue()?.isNotBlank() ?: false
        if (isValidRelationship) ui.relationshipTitleTextView.setTextColor(defaultColorList)
        else ui.relationshipTitleTextView.textColorResource = R.color.colorAccent

        // interview place
        val isValidInterviewPlace = ui.interviewPlaceSpinner.getValue()?.isNotBlank() ?: false
        if (isValidInterviewPlace) ui.interviewPlaceTitleTextView.setTextColor(defaultColorList)
        else ui.interviewPlaceTitleTextView.textColorResource = R.color.colorAccent

        val isValidate = isValidSupportCountry && isValidSupplyPlan && isValidRespondentName && isValidRelationship && isValidInterviewPlace
        if (isValidate) {
            ui.title2.backgroundColorResource = R.color.colorBgLiteGray
            ui.title2ValidImageView.visibility = View.VISIBLE
        } else {
            ui.title2.backgroundResource = R.drawable.header_cif
            ui.title2ValidImageView.visibility = View.GONE
        }

        return isValidate
    }

    @SuppressLint("SetTextI18n")
    private fun validate3(): Boolean {
        var isValidFamilyMember = true
        if (!ui.fatherSwitch.isChecked && ui.fatherReasonSpinner.getValue().isNullOrBlank()) {
            isValidFamilyMember = false
        }
        if (!ui.motherSwitch.isChecked && ui.motherReasonSpinner.getValue().isNullOrBlank()) {
            isValidFamilyMember = false
        }

        if (isValidFamilyMember) ui.familyMemberTitleTextView.setTextColor(defaultColorList)
        else ui.familyMemberTitleTextView.textColorResource = R.color.colorAccent

        if (!ui.fatherSwitch.isChecked && !ui.motherSwitch.isChecked) {
            ui.parentMessageTextView.visibility = View.VISIBLE
        } else {
            ui.parentMessageTextView.visibility = View.GONE
        }

        var isValidMainGuardian = true
        if (ui.mainguardianSpinner.getValue().isNullOrBlank()) {
            isValidMainGuardian = false
        }
        if (!ui.fatherSwitch.isChecked && ui.mainguardianSpinner.getValue() == "2") {
            isValidMainGuardian = false
            ui.mainguardianMessageTextView.textResource = R.string.message_validate_father_is_not_living
        }

        if (!ui.motherSwitch.isChecked && ui.mainguardianSpinner.getValue() == "5") {
            isValidMainGuardian = false
            ui.mainguardianMessageTextView.textResource = R.string.message_validate_mother_is_not_living
        }

        if (isValidMainGuardian) {
            ui.mainguardianMessageTextView.visibility = View.GONE
        } else {
            ui.mainguardianMessageTextView.visibility = View.VISIBLE
        }

        var isValidMainGuardianName = true
        if (ui.mainguardianSpinner.getValue() == "99" && ui.guardianNameEditText.getStringValue().isBlank()) {
            isValidMainGuardianName = false
        }

        if (ui.mainguardianSpinner.getValue().isNullOrBlank() || !isValidMainGuardianName) ui.mainguardianTitleTextView.textColorResource = R.color.colorAccent
        else ui.mainguardianTitleTextView.setTextColor(defaultColorList)

        var isValidIncomeProvider = true
        if (ui.incomeProviderSpinner.getValue().isNullOrBlank()) {
            isValidIncomeProvider = false
        }
        /* 2019/04/11 validation 해제
        if ((!ui.fatherSwitch.isChecked && ui.incomeProviderSpinner.getValue() == "2")
                || (!ui.motherSwitch.isChecked && ui.incomeProviderSpinner.getValue() == "5")) {
            isValidIncomeProvider = false
            ui.incomeProviderMessageTextView.visibility = View.VISIBLE
        } else {
            ui.incomeProviderMessageTextView.visibility = View.GONE
        }
        */
        if (isValidIncomeProvider) ui.incomeProviderTitleTextView.setTextColor(defaultColorList)
        else ui.incomeProviderTitleTextView.textColorResource = R.color.colorAccent

        var isValidSibling = true
        if (ui.noBrotherEditText.getStringValue().isBlank() || ui.noSisterEditText.getStringValue().isBlank()) {
            isValidSibling = false
        }
        if (isValidSibling) ui.siblingTitleTextView.setTextColor(defaultColorList)
        else ui.siblingTitleTextView.textColorResource = R.color.colorAccent

        var isValidCases = true
        if (ui.disabilitySwitch.isChecked && !ui.disabilitySpinner.getValue().isNullOrBlank()
                && ui.specialCase1Spinner.getValue() != "1"
                && ui.specialCase2Spinner.getValue() != "1"
                && ui.specialCase3Spinner.getValue() != "1") {
            isValidCases = false
        }

        if (!ui.disabilitySwitch.isChecked
                && (ui.specialCase1Spinner.getValue() == "1"
                        || ui.specialCase2Spinner.getValue() == "1"
                        || ui.specialCase3Spinner.getValue() == "1")) {
            isValidCases = false
        }

        val illnessChecklist = arrayOf("6", "9", "14", "16", "18", "21", "26", "27", "31")
        if (ui.illnessSwitch.isChecked && illnessChecklist.contains(ui.illnessSpinner.getValue())
                && ui.specialCase1Spinner.getValue() != "2"
                && ui.specialCase2Spinner.getValue() != "2"
                && ui.specialCase3Spinner.getValue() != "2") {
            isValidCases = false
        }

        if (!(ui.illnessSwitch.isChecked && illnessChecklist.contains(ui.illnessSpinner.getValue()))
                && (ui.specialCase1Spinner.getValue() == "2"
                        || ui.specialCase2Spinner.getValue() == "2"
                        || ui.specialCase3Spinner.getValue() == "2")) {
            isValidCases = false
        }

        if (ui.mainguardianSpinner.getValue() == "7"
                && ui.specialCase1Spinner.getValue() != "4"
                && ui.specialCase2Spinner.getValue() != "4"
                && ui.specialCase3Spinner.getValue() != "4") {
            isValidCases = false
        }

        if (ui.mainguardianSpinner.getValue() != "7"
                && (ui.specialCase1Spinner.getValue() == "4"
                        || ui.specialCase2Spinner.getValue() == "4"
                        || ui.specialCase3Spinner.getValue() == "4")) {
            isValidCases = false
        }

        if (isValidCases) {
            ui.specialCaseSwitch.setTextColor(defaultColorList)
        } else {
            ui.specialCaseSwitch.textColorResource = R.color.colorAccent
            toast(R.string.message_require_case)
        }

        val isValidate = isValidFamilyMember && isValidMainGuardian && isValidMainGuardianName && isValidIncomeProvider && isValidSibling
                && isValidCases
        if (isValidate) {
            ui.title3.backgroundColorResource = R.color.colorBgLiteGray
            ui.title3ValidImageView.visibility = View.VISIBLE
        } else {
            ui.title3.backgroundResource = R.drawable.header_cif
            ui.title3ValidImageView.visibility = View.GONE
        }

        return isValidate
    }

    private fun validate4(): Boolean {
        var isValidate = true
        var titleTextView: TextView? = null
        ui.personalInfoLayout.viewsRecursive.filter { it is TextView || it is Spinner }.forEach {
            when (it) {
                is TextView -> titleTextView = it
                is Spinner -> {
                    if (it.getValue().isNullOrBlank()) {
                        isValidate = false
                        titleTextView?.textColorResource = R.color.colorAccent
                    } else {
                        titleTextView?.setTextColor(defaultColorList)
                    }
                }
            }
        }

        if (isValidate) {
            ui.title4.backgroundColorResource = R.color.colorBgLiteGray
            ui.title4ValidImageView.visibility = View.VISIBLE
        } else {
            ui.title4.backgroundResource = R.drawable.header_cif
            ui.title4ValidImageView.visibility = View.GONE
        }

        return isValidate
    }

    private fun validate5(): Boolean {
        var isValidRemark = true
        if (ui.relationshipSpinner.getValue() == "99" && ui.remarkEditText.getStringValue().isBlank()) {
            isValidRemark = false
            ui.remarkRelationshipMessageTextView.visibility = View.VISIBLE
        } else {
            ui.remarkRelationshipMessageTextView.visibility = View.GONE
        }

        if (ui.illnessSwitch.isChecked && ui.illnessSpinner.getValue() == "99" && ui.remarkEditText.getStringValue().isBlank()) {
            isValidRemark = false
            ui.remarkIllnessMessageTextView.visibility = View.VISIBLE
        } else {
            ui.remarkIllnessMessageTextView.visibility = View.GONE
        }

        if (ui.remarkEditText.getStringValue().length > 2000) {
            ui.remarkMessageTextView.textResource = R.string.message_validate_length_2000
            ui.remarkMessageTextView.visibility = View.VISIBLE
            isValidRemark = false
        }

        if (isValidRemark) {
            ui.title5.backgroundColorResource = R.color.colorBgLiteGray
            ui.title5ValidImageView.visibility = View.VISIBLE
        } else {
            ui.title5.backgroundResource = R.drawable.header_cif
            ui.title5ValidImageView.visibility = View.GONE
        }

        return isValidRemark
    }

    private fun updateBirthImage(file: File? = null) {
        birthImage = file

        if (file == null) {
            ui.birthImageView.imageResource = R.drawable.icon_1

            //ui.birthAlbumButton.visibility = if (isEditable) View.VISIBLE else View.GONE
            ui.birthCameraButton.visibility = if (isEditable) View.VISIBLE else View.GONE
            ui.birthDeleteButton.visibility = View.GONE
        } else {
            if (file.exists()) {
               // ui.birthAlbumButton.visibility = View.GONE
                ui.birthCameraButton.visibility = View.GONE
                ui.birthDeleteButton.visibility = if (isEditable) View.VISIBLE else View.GONE

                Glide.with(this).load(file)
                        .apply(RequestOptions.skipMemoryCacheOf(true))
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                        .into(ui.birthImageView)
            } else {
                //ui.birthAlbumButton.visibility = if (isEditable) View.VISIBLE else View.GONE
                ui.birthCameraButton.visibility = if (isEditable) View.VISIBLE else View.GONE
                ui.birthDeleteButton.visibility = View.GONE
            }
        }
    }

    private fun updateConsentImage(file: File? = null) {
        consentImage = file

        if (file == null) {
            ui.consentImageView.imageResource = R.drawable.icon_1

            ui.consentAddButton.visibility = View.VISIBLE
            ui.consentDeleteButton.visibility = View.GONE
        } else {
            if (file.exists()) {
                ui.consentAddButton.visibility = View.GONE
                ui.consentDeleteButton.visibility = View.VISIBLE

                Glide.with(this).load(file)
                        .apply(RequestOptions.skipMemoryCacheOf(true))
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                        .into(ui.consentImageView)
            } else {
                ui.consentAddButton.visibility = View.VISIBLE
                ui.consentDeleteButton.visibility = View.GONE
            }
        }
    }

    private fun removeSiblings(sibling: String) {
        siblings?.remove(sibling)
        onChangeSiblings()
    }

    private fun onChangeSiblings() {
        ui.siblingsContainer.removeAllViews()
        ui.siblingsContainer.addView(UI {
            linearLayout {
                lparams(width = matchParent, height = wrapContent)

                siblings?.forEach { sibling ->
                    linearLayout {
                        padding = dimen(R.dimen.px10)

                        textView(sibling) {}.lparams(width = 0, height = wrapContent, weight = 1f)
                        if (isEditable) {
                            imageView {
                                imageResource = R.drawable.close
                                onClick {
                                    removeSiblings(sibling)
                                }
                            }.lparams(width = dimen(R.dimen.px26), height = dimen(R.dimen.px26)) {
                                gravity = Gravity.CENTER
                            }
                        }
                    }.lparams(width = 0, height = wrapContent, weight = 1f)
                }

                if (!siblings.isNullOrEmpty() && siblings!!.size == 1) {
                    space {}.lparams(width = 0, height = wrapContent, weight = 1f)
                }
            }
        }.view)

    }

    private fun getAlbumImage(requestCode: Int) {
        logger.debug("getAlbumImage($requestCode)")

        val intent = Intent(Intent.ACTION_PICK)
        intent.setType("image/*")
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE)

        startActivityForResult(intent, requestCode)
    }

    private fun captureCamera(requestCode: Int) {
        val state = Environment.getExternalStorageState()

        if (Environment.MEDIA_MOUNTED == state) {
            val camera = Camera.open()
            val parameters = camera.parameters
            val sizeList = parameters.supportedPictureSizes

            // 원하는 최적화 사이즈를 1280x720 으로 설정
//            val size = getOptimalPictureSize(parameters.supportedPictureSizes, 1280, 720)
            val size = getOptimalPictureSize(parameters.supportedPictureSizes, 667, 500)
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

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val imageFileName = "CIF_"

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

    private fun cropImage() {
        logger.debug("cropImage() ------ photoURI : $photoURI / albumURI : $albumURI")

        val cropIntent = Intent("com.android.camera.action.CROP")

        cropIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        cropIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        cropIntent.setDataAndType(photoURI, "image/*")
        cropIntent.putExtra("outputX", 200)
        cropIntent.putExtra("outputY", 200)
        cropIntent.putExtra("aspectX", 1)
        cropIntent.putExtra("aspectY", 1)
        cropIntent.putExtra("scale", true)
        cropIntent.putExtra("output", albumURI)

        startActivityForResult(cropIntent, CROP_IMAGE)
    }

    private fun cropImage_general() {
        logger.debug("cropImage() ------ photoURI : $photoURI / albumURI : $albumURI")

        val cropIntent = Intent("com.android.camera.action.CROP")

        cropIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        cropIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        cropIntent.setDataAndType(photoURI, "image/*")
        cropIntent.putExtra("outputX", 500)
        cropIntent.putExtra("outputY", 667)
        cropIntent.putExtra("aspectX", 3)
        cropIntent.putExtra("aspectY", 4)
        cropIntent.putExtra("scale", true)
        cropIntent.putExtra("output", albumURI)

        startActivityForResult(cropIntent, CROP_IMAGE_PICK_FROM_ALBUM)
    }

    private fun loadData(report: RPT_BSC) {
        logger.debug("loadData : ${GsonBuilder().create().toJson(report)}")
        isEditable = report.RPT_STCD ?: "" == "12" || report.RPT_STCD ?: "" == "15"

        report.ATCH_FILE?.forEach {
            when (it.IMG_DVCD) {
                "331001" -> {
                    // 일반
                    val file = File(contentsRootDir, "${it.FILE_PATH}/${it.FILE_NM}")
                    if (file.exists()) {
                        generalImage = file
                        Glide.with(this).load(file)
                                .apply(RequestOptions.skipMemoryCacheOf(true))
                                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                                .into(ui.photoImageView)
                    }
                }
                "331002" -> {
                    // 출생증명서
                    val file = File(contentsRootDir, "${it.FILE_PATH}/${it.FILE_NM}")
                    updateBirthImage(file)
                }
                "331003" -> {
                    // 동의서
                    val file = File(contentsRootDir, "${it.FILE_PATH}/${it.FILE_NM}")
                    updateConsentImage(file)
                }
                "331005" -> {
                    // 썸네일
                    val file = File(contentsRootDir, "${it.FILE_PATH}/${it.FILE_NM}")

                    if (file.exists()) {
                        profileImage = file
                        Glide.with(this).load(file)
                                .apply(RequestOptions.skipMemoryCacheOf(true))
                                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                                .into(ui.profileImageImageView)
                    }
                }
            }
        }

        report.CH_MST?.apply {
            ui.firstNameEditText.setText(CH_EFNM)
            ui.middleNameEditText.setText(CH_EMNM)
            ui.lastNameEditText.setText(CH_ELNM)

            ui.supportCountrySpinner.setSelectKey(DNCTR_CD)
        }

        report.CH_BSC?.apply {
            ui.genderRadioGroup.check(if (GNDR == "F") ui.genderFemaleRadio.id else ui.genderMaleRadio.id)

            ui.birthDateEditText.setText(BDAY?.convertDateFormat())
            ui.ageEditText.setText(AGE ?: "")

            ui.villageSpinner.setSelectKey(VLG_CD)
            ui.addressEditText.setText(HS_ADDR)
            ui.addressDetailEditText.setText(HS_ADDR_DTL)

            ui.telEditText.setText(TEL_NO)
        }

        report.HLTH?.apply {
            ui.heightEditText.setText(HGHT?.toString())
            ui.weightEditText.setText(WGHT?.toString())

            ui.disabilitySwitch.isChecked = DISB_YN == "Y"
            ui.disabilitySpinner.setSelectKey(DISB_CD)
            ui.disabilityReasonSpinner.setSelectKey(DISB_RNCD)

            ui.illnessSwitch.isChecked = ILNS_YN == "Y"
            ui.illnessSpinner.setSelectKey(ILNS_CD)
            ui.illnessReasonSpinner.setSelectKey(ILNS_RNCD)
        }

        report.EDU?.apply {
            ui.schoolTypeSpinner.setSelectKey(SCTP_CD)
            ui.schoolTypeOtherEditText.setText(SCTP_ETC)
            ui.schoolTypeReasonSpinner.setSelectKey(PRSCH_RNCD)
            ui.schoolNameSpinner.setSelectKey(SCHL_CD)
            ui.schoolGradeEditText.setText(GRAD)
        }

        report.INTV?.apply {
            ui.respondentNameEditText.setText(RSPN_NM)
            ui.relationshipSpinner.setSelectKey(RSPN_RLCD)
            ui.interviewPlaceSpinner.setSelectKey(INTPLC_CD)
        }

        report.FMLY?.apply {
            ui.fatherSwitch.isChecked = FA_LTYN == "Y"
            ui.fatherReasonSpinner.setSelectKey(FA_LARCD)

            ui.motherSwitch.isChecked = MO_LTYN == "Y"
            ui.motherReasonSpinner.setSelectKey(MO_LARCD)

            ui.mainguardianSpinner.setSelectKey(MGDN_CD)
            ui.guardianNameEditText.setText(MGDN_NM)
            ui.incomeProviderSpinner.setSelectKey(MICM_RLCD)

            ui.incomeEditText.setText(MON_INCM)

            val VAL_EBRO_LTNUM = EBRO_LTNUM?.toIntOrNull()?:0
            val VAL_YBRO_LTNUM = YBRO_LTNUM?.toIntOrNull()?:0
            val VAL_BRO_LTNUM = VAL_EBRO_LTNUM + VAL_YBRO_LTNUM

            if(EBRO_LTNUM =="" && YBRO_LTNUM ==""){
                ui.noBrotherEditText.setText(EBRO_LTNUM)
            }
            else{
                ui.noBrotherEditText.setText(VAL_BRO_LTNUM.toString())
            }

            val VAL_ESIS_LTNUM = ESIS_LTNUM?.toIntOrNull()?:0
            val VAL_YSIS_LTNUM = YSIS_LTNUM?.toIntOrNull()?:0
            val VAL_SIS_LTNUM = VAL_ESIS_LTNUM + VAL_YSIS_LTNUM

            if(ESIS_LTNUM =="" && YSIS_LTNUM ==""){
                ui.noSisterEditText.setText(ESIS_LTNUM)
            }
            else{
                ui.noSisterEditText.setText(VAL_SIS_LTNUM.toString())
            }

        }

        report.SIBL?.forEach {
            if (siblings == null) siblings = ArrayList()
            siblings!!.add(it.CHRCP_NO)
            onChangeSiblings()
        }

        report.CH_SPSL_INFO?.apply {
            ui.specialCaseSwitch.isChecked = true
            forEachIndexed { index, ch_spsl_info ->
                when (index) {
                    0 -> ui.specialCase1Spinner.setSelectKey(ch_spsl_info.SPSL_CD)
                    1 -> ui.specialCase2Spinner.setSelectKey(ch_spsl_info.SPSL_CD)
                    2 -> ui.specialCase3Spinner.setSelectKey(ch_spsl_info.SPSL_CD)
                }
            }
        }

        ui.personalInfoLayout.viewsRecursive.filter { it is Spinner }.forEach {
            if (it is Spinner && it.tag is PersonalInfoItem) {
                val personalInfoItem = it.tag as PersonalInfoItem
                report.PRSN_ANS_INFO?.forEach { prsn_ans_info ->
                    if (prsn_ans_info.PRSN_CD == personalInfoItem.master.CD) {
                        personalInfoItem.detail.forEachIndexed { index, spinnerOption ->
                            if (prsn_ans_info.ANS_CD == spinnerOption.key) {
                                it.setSelection(index)
                            }
                        }
                    }
                }
            }
        }

        report.REMRK?.apply {
            ui.remarkEditText.setText(REMRK_ENG)
        }

        val i = ui.supplyPlanSpinner.getSelectedIndex("""${report.YEAR}${report.SPLY_MON}""")
        initPlan = i

        if (report.RPT_STCD ?: "" == "2" || report.RPT_STCD ?: "" == "15") {
            returnCode = report.RPT_STCD
            ui.returnContainer.visibility = View.VISIBLE
            when (report.RPT_STCD) {
                "2" -> {
                    ui.returnRemarkTitleTextView.textResource = R.string.message_return_remark_ihq
                }
                "15" -> {
                    ui.returnRemarkTitleTextView.textResource = R.string.message_return_remark_ho
                }
            }
            var title1Count = 0
            var title2Count = 0
            var title3Count = 0
            var title4Count = 0
            var title5Count = 0

            AnkoContext.createDelegate(ui.returnItemsContainer).apply {
                editViewItem!!.returns?.forEachIndexed { index, returnItem ->
                    textView("${index + 1}. ${returnItem.RTRN_DETL}")
                    val returns = ArrayList<String>()
                    if (!returnItem.RTRN_BCD_LABEL.isNullOrBlank()) returns.add(returnItem.RTRN_BCD_LABEL)
                    if (!returnItem.RTRN_SCD_LABEL.isNullOrBlank()) returns.add(returnItem.RTRN_SCD_LABEL)
                    textView(returns.joinToString(" -> "))

                    when (returnItem.RTRN_BCD) {
                        "2", "3", "4", "5", "6", "18", "21" -> {
                            title1Count++
                        }
                        "7" -> {
                            title3Count++
                        }
                        "10", "11" -> {
                            title5Count++
                        }
                        "22", "23" -> {
                            title2Count++
                        }
                    }
                }
            }

            if (title1Count > 0) {
                ui.title1ReturnCountTextView.text = title1Count.toString()
                ui.title1ReturnCountTextView.visibility = View.VISIBLE
                ui.title1TextView.textColorResource = R.color.colorAccent
            } else {
                ui.title1ReturnCountTextView.visibility = View.GONE
                ui.title1TextView.textColorResource = R.color.colorDefault
            }

            if (title2Count > 0) {
                ui.title2ReturnCountTextView.text = title2Count.toString()
                ui.title2ReturnCountTextView.visibility = View.VISIBLE
                ui.title2TextView.textColorResource = R.color.colorAccent
            } else {
                ui.title2ReturnCountTextView.visibility = View.GONE
                ui.title2TextView.textColorResource = R.color.colorDefault
            }

            if (title3Count > 0) {
                ui.title3ReturnCountTextView.text = title3Count.toString()
                ui.title3ReturnCountTextView.visibility = View.VISIBLE
                ui.title3TextView.textColorResource = R.color.colorAccent
            } else {
                ui.title3ReturnCountTextView.visibility = View.GONE
                ui.title3TextView.textColorResource = R.color.colorDefault
            }

            if (title4Count > 0) {
                ui.title4ReturnCountTextView.text = title4Count.toString()
                ui.title4ReturnCountTextView.visibility = View.VISIBLE
                ui.title4TextView.textColorResource = R.color.colorAccent
            } else {
                ui.title4ReturnCountTextView.visibility = View.GONE
                ui.title4TextView.textColorResource = R.color.colorDefault
            }

            if (title5Count > 0) {
                ui.title5ReturnCountTextView.text = title5Count.toString()
                ui.title5ReturnCountTextView.visibility = View.VISIBLE
                ui.title5TextView.textColorResource = R.color.colorAccent
            } else {
                ui.title5ReturnCountTextView.visibility = View.GONE
                ui.title5TextView.textColorResource = R.color.colorDefault
            }
        } else {
            ui.returnContainer.visibility = View.GONE
            ui.title1ReturnCountTextView.visibility = View.GONE
            ui.title2ReturnCountTextView.visibility = View.GONE
            ui.title3ReturnCountTextView.visibility = View.GONE
            ui.title4ReturnCountTextView.visibility = View.GONE
            ui.title5ReturnCountTextView.visibility = View.GONE

            ui.title1TextView.textColorResource = R.color.colorDefault
            ui.title2TextView.textColorResource = R.color.colorDefault
            ui.title3TextView.textColorResource = R.color.colorDefault
            ui.title4TextView.textColorResource = R.color.colorDefault
            ui.title5TextView.textColorResource = R.color.colorDefault
        }


        ui.container.viewsRecursive.filter { it is EditText || it is Spinner || it is Switch || it is Button }.forEach {
            it.isEnabled = isEditable
            if (it is EditText && !isEditable) it.hint = ""
        }

        ui.nextButton1.visibility = if (isEditable) View.VISIBLE else View.GONE
        ui.nextButton2.visibility = if (isEditable) View.VISIBLE else View.GONE
        ui.nextButton3.visibility = if (isEditable) View.VISIBLE else View.GONE
        ui.nextButton4.visibility = if (isEditable) View.VISIBLE else View.GONE
        ui.nextButton5.visibility = if (isEditable) View.VISIBLE else View.GONE

        ui.duplicateCheckButton.visibility = if (isEditable) View.VISIBLE else View.GONE

        ui.generalImageButtonContainer.visibility = if (isEditable) View.VISIBLE else View.GONE
        ui.profileImageButtonContainer.visibility = if (isEditable) View.VISIBLE else View.GONE
        ui.birthButtonContainer.visibility = if (isEditable) View.VISIBLE else View.GONE

        ui.siblingsEditContainer.visibility = if (isEditable) View.VISIBLE else View.GONE

        if (isEditable) {
            validate1()
            validate2()
            validate3()
            if (validate4()) validate5()
        }
    }

    private fun save(rpt_stcd: String = "13") {
        val timestamp = Date().time
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val ctrCd = sharedPref.getString("user_ctr_cd", "")
        val brcCd = sharedPref.getString("user_brc_cd", "")
        val prjCd = sharedPref.getString("user_prj_cd", "")
        val userid = sharedPref.getString("userid", "")
        val username = sharedPref.getString("username", "")

        val report = editViewItem?.rpt_bsc ?: RPT_BSC(RCP_NO = "$timestamp", CHRCP_NO = "$timestamp", RPT_DVCD = "1")
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
        } else {
            report.UPD_DT = timestamp
            report.UPDR_ID = userid
        }
        report.RPT_STCD = rpt_stcd
        report.LAST_UPD_DT = timestamp
        report.APP_MODIFY_DATE = timestamp

        val plan = ui.supplyPlanSpinner.getValue()
        if (!plan.isNullOrBlank() && plan.length >= 6) {
            report.YEAR = plan.substring(0, 4)
            report.SPLY_MON = plan.substring(4)
        }

        val ch_mst = report.CH_MST ?: CH_MST(CHRCP_NO = "$timestamp", CTR_CD = ctrCd, BRC_CD = brcCd, PRJ_CD = prjCd)
        report.CH_MST = ch_mst
        ch_mst.CH_EFNM = ui.firstNameEditText.getStringValue()
        ch_mst.CH_EMNM = ui.middleNameEditText.getStringValue()
        ch_mst.CH_ELNM = ui.lastNameEditText.getStringValue()
        ch_mst.DNCTR_CD = ui.supportCountrySpinner.getValue()
        if (ch_mst.REG_DT == null) {
            ch_mst.REGR_ID = userid
            ch_mst.REG_DT = timestamp

            ch_mst.ORG_CHRCP_NO = reintake_origin
            ch_mst.BF_CHRCP_NO = reintake_link
        } else {
            ch_mst.UPDR_ID = userid
            ch_mst.UPD_DT = timestamp
        }
        ch_mst.APP_MODIFY_DATE = timestamp
        ch_mst.DEL_YN = "N"

        val ch_bsc = report.CH_BSC ?: CH_BSC(RCP_NO = report.RCP_NO, CHRCP_NO = report.CHRCP_NO)
        report.CH_BSC = ch_bsc
        ch_bsc.GNDR = if (ui.genderFemaleRadio.isChecked) "F" else "M"
        ch_bsc.BDAY = ui.birthDateEditText.getStringValue().convertDateFormat("MM-dd-yyyy", "yyyyMMdd")
        ch_bsc.AGE = ui.ageEditText.getStringValue()
        ch_bsc.VLG_CD = ui.villageSpinner.getValue()
        ch_bsc.HS_ADDR = ui.addressEditText.getStringValue()
        ch_bsc.HS_ADDR_DTL = ui.addressDetailEditText.getStringValue()
        ch_bsc.TEL_NO = ui.telEditText.getStringValue()

        val hlth = report.HLTH ?: HLTH(RCP_NO = report.RCP_NO)
        report.HLTH = hlth
        hlth.HGHT = ui.heightEditText.getIntValue()
        hlth.WGHT = ui.weightEditText.getIntValue()
        hlth.DISB_YN = if (ui.disabilitySwitch.isChecked) "Y" else "N"
        hlth.DISB_CD = ui.disabilitySpinner.getValue()
        hlth.DISB_RNCD = ui.disabilityReasonSpinner.getValue()
        hlth.ILNS_YN = if (ui.illnessSwitch.isChecked) "Y" else "N"
        hlth.ILNS_CD = ui.illnessSpinner.getValue()
        hlth.ILNS_RNCD = ui.illnessReasonSpinner.getValue()
        if (ui.growthEditText.tag != null && ui.growthEditText.tag is CD) {
            val cd = ui.growthEditText.tag as CD
            hlth.BMI_CD = cd.CD
        } else {
            hlth.BMI_CD = null
        }

        val edu = report.EDU ?: EDU(RCP_NO = report.RCP_NO)
        report.EDU = edu
        edu.SCTP_CD = ui.schoolTypeSpinner.getValue()
        edu.SCTP_ETC = ui.schoolTypeOtherEditText.getStringValue()
        edu.PRSCH_RNCD = ui.schoolTypeReasonSpinner.getValue()
        edu.SCHL_CD = ui.schoolNameSpinner.getValue()
        edu.GRAD = ui.schoolGradeEditText.getStringValue()

        val intv = report.INTV
                ?: INTV(RCP_NO = report.RCP_NO, INTVR_NM = username, INTV_DT = timestamp.toDateFormat("YYYYMMdd"))
        report.INTV = intv
        intv.RSPN_NM = ui.respondentNameEditText.getStringValue()
        intv.RSPN_RLCD = ui.relationshipSpinner.getValue()
        intv.INTPLC_CD = ui.interviewPlaceSpinner.getValue()

        val fmly = report.FMLY ?: FMLY(RCP_NO = report.RCP_NO)
        report.FMLY = fmly
        fmly.FA_LTYN = if (ui.fatherSwitch.isChecked) "Y" else "N"
        fmly.FA_NUM = if (ui.fatherSwitch.isChecked) "1" else "0"
        fmly.FA_LARCD = ui.fatherReasonSpinner.getValue()
        fmly.MO_LTYN = if (ui.motherSwitch.isChecked) "Y" else "N"
        fmly.MO_NUM = if (ui.motherSwitch.isChecked) "1" else "0"
        fmly.MO_LARCD = ui.motherReasonSpinner.getValue()
        fmly.MGDN_CD = ui.mainguardianSpinner.getValue()
        fmly.MGDN_NM = ui.guardianNameEditText.getStringValue()
        fmly.MICM_RLCD = ui.incomeProviderSpinner.getValue()
        fmly.MON_INCM = ui.incomeEditText.getStringValue()
        fmly.EBRO_LTNUM = ui.noBrotherEditText.getStringValue()
        fmly.ESIS_LTNUM = ui.noSisterEditText.getStringValue()
        fmly.YBRO_LTNUM = "0"
        fmly.YSIS_LTNUM = "0"

        val selectedSiblings = ArrayList<SIBL>()
        siblings?.forEach {
            selectedSiblings.add(SIBL(RCP_NO = report.RCP_NO, CHRCP_NO = it))
        }
        report.SIBL = if (selectedSiblings.isEmpty()) null else selectedSiblings

        val specialCases = ArrayList<CH_SPSL_INFO>()
        if (ui.specialCaseSwitch.isChecked) {
            val case1 = ui.specialCase1Spinner.getValue()
            if (!case1.isNullOrBlank()) {
                specialCases.add(CH_SPSL_INFO(RCP_NO = report.RCP_NO, SPSL_CD = case1))
            }

            val case2 = ui.specialCase2Spinner.getValue()
            if (!case2.isNullOrBlank()) {
                specialCases.add(CH_SPSL_INFO(RCP_NO = report.RCP_NO, SPSL_CD = case2))
            }

            val case3 = ui.specialCase3Spinner.getValue()
            if (!case3.isNullOrBlank()) {
                specialCases.add(CH_SPSL_INFO(RCP_NO = report.RCP_NO, SPSL_CD = case3))
            }
        }
        report.CH_SPSL_INFO = if (specialCases.isEmpty()) null else specialCases

        val personalInfo = ArrayList<PRSN_ANS_INFO>()
        ui.personalInfoLayout.viewsRecursive.filter { it is Spinner }.forEach {
            if (it is Spinner && it.selectedItemPosition > 0 && it.tag is PersonalInfoItem) {
                val personalInfoItem = it.tag as PersonalInfoItem
                personalInfo.add(PRSN_ANS_INFO(RCP_NO = report.RCP_NO,
                        PRSN_CD = personalInfoItem.master.CD,
                        ANS_CD = it.getValue(),
                        REGR_ID = userid,
                        REG_DT = timestamp))
            }
        }
        if (personalInfo.isNotEmpty()) {
            report.PRSN_ANS_INFO = personalInfo
        }

        val remark = report.REMRK ?: REMRK(RCP_NO = report.RCP_NO)
        report.REMRK = remark
        remark.REMRK_ENG = ui.remarkEditText.getStringValue()

        val targetDirPath = "sw/${Constants.BUILD}/$ctrCd/${report.CHRCP_NO}"
        val targetDir = File(contentsRootDir, targetDirPath)

        val files = ArrayList<ATCH_FILE>()
        generalImage?.apply {
            if (exists()) {
                val targetFileName = "CIF_${report.RCP_NO}_${timestamp.toDateFormat("yyyyMMddHHmmss")}.${this.extension()}"
                val targetFile = File(targetDir, targetFileName)

                if (this != targetFile) {
                    copyTo(targetFile, true)
                }

                files.add(ATCH_FILE(SEQ_NO = 1, RCP_NO = report.RCP_NO,
                        FILE_DVCD = report.RPT_DVCD, IMG_DVCD = "331001",
                        FILE_NM = targetFileName, FILE_PATH = targetDirPath))
            }
        }

        profileImage?.apply {
            if (exists()) {
                val targetFileName = "CIF_${report.RCP_NO}_TH_${timestamp.toDateFormat("yyyyMMddHHmmss")}.${this.extension()}"
                val targetFile = File(targetDir, targetFileName)

                if (this != targetFile) {
                    copyTo(targetFile, true)
                }

                files.add(ATCH_FILE(SEQ_NO = 2, RCP_NO = report.RCP_NO,
                        FILE_DVCD = report.RPT_DVCD, IMG_DVCD = "331005",
                        FILE_NM = targetFileName, FILE_PATH = targetDirPath))
            }
        }

        birthImage?.apply {
            if (exists()) {
                val targetFileName = "CIF_${report.RCP_NO}_CE_${timestamp.toDateFormat("yyyyMMddHHmmss")}.${this.extension()}"
                val targetFile = File(targetDir, targetFileName)

                if (this != targetFile) {
                    copyTo(targetFile, true)
                }

                files.add(ATCH_FILE(SEQ_NO = 3, RCP_NO = report.RCP_NO,
                        FILE_DVCD = report.RPT_DVCD, IMG_DVCD = "331002",
                        FILE_NM = targetFileName, FILE_PATH = targetDirPath))
            }
        }

        consentImage?.apply {
            if (exists()) {
                val targetFileName = "CIF_${report.RCP_NO}_CO_${timestamp.toDateFormat("yyyyMMddHHmmss")}.${this.extension()}"
                val targetFile = File(targetDir, targetFileName)

                if (this != targetFile) {
                    copyTo(targetFile, true)
                }

                files.add(ATCH_FILE(SEQ_NO = 4, RCP_NO = report.RCP_NO,
                        FILE_DVCD = report.RPT_DVCD, IMG_DVCD = "331003",
                        FILE_NM = targetFileName, FILE_PATH = targetDirPath))
            }
        }

        if (!files.isEmpty()) {
            report.ATCH_FILE = files
        }

        logger.debug(GsonBuilder().create().toJson(report))

        viewModel.save(report).observeOnce(this, Observer {
            editViewItem?.rpt_bsc = it
            if (rpt_stcd == RPT_STCD_STANDBY) {
                isEditable = false
                activity!!.onBackPressed()
            }
        })
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class FragmentUI : AnkoComponent<CifFragment> {
        private val DEBUG_UI = false

        lateinit var container: ViewGroup

        lateinit var nextButton1: Button
        lateinit var nextButton2: Button
        lateinit var nextButton3: Button
        lateinit var nextButton4: Button
        lateinit var nextButton5: Button

        lateinit var title1: LinearLayout
        lateinit var title2: LinearLayout
        lateinit var title3: LinearLayout
        lateinit var title4: LinearLayout
        lateinit var title5: LinearLayout

        lateinit var title1TextView: TextView
        lateinit var title2TextView: TextView
        lateinit var title3TextView: TextView
        lateinit var title4TextView: TextView
        lateinit var title5TextView: TextView

        lateinit var title1ValidImageView: ImageView
        lateinit var title2ValidImageView: ImageView
        lateinit var title3ValidImageView: ImageView
        lateinit var title4ValidImageView: ImageView
        lateinit var title5ValidImageView: ImageView

        lateinit var toggleButton1: ImageView
        lateinit var toggleButton2: ImageView
        lateinit var toggleButton3: ImageView
        lateinit var toggleButton4: ImageView
        lateinit var toggleButton5: ImageView

        lateinit var photoImageView: ImageView
        lateinit var photoAlbumButton: ImageView
        lateinit var photoCameraButton: ImageView
        lateinit var photoDeleteButton: ImageView

        lateinit var profileImageImageView: CircleImageView
        lateinit var profileImageAlbumButton: ImageView
        lateinit var profileImageDeleteButton: ImageView


        lateinit var detail1: LinearLayout
        lateinit var childNameTextView: TextView
        lateinit var firstNameEditText: EditText
        lateinit var middleNameEditText: EditText
        lateinit var lastNameEditText: EditText

        lateinit var genderTitleTextView: TextView
        lateinit var genderRadioGroup: RadioGroup
        lateinit var genderFemaleRadio: RadioButton
        lateinit var genderMaleRadio: RadioButton

        lateinit var birthDateTitleTextView: TextView
        lateinit var birthDateEditText: EditText
        lateinit var birthDateMessageTextView: TextView

        lateinit var ageEditText: EditText

        lateinit var duplicateCheckButton: Button
        lateinit var duplicateCheckContainer: LinearLayout

        lateinit var birthImageView: ImageView
        //lateinit var birthAlbumButton: ImageView
        lateinit var birthCameraButton: ImageView
        lateinit var birthDeleteButton: ImageView

        lateinit var growthTitleTextView: TextView
        lateinit var heightEditText: EditText
        lateinit var weightEditText: EditText
        lateinit var growthEditText: EditText
        lateinit var heightMessageTextView: TextView
        lateinit var weightMessageTextView: TextView

        lateinit var addressTitleTextView: TextView
        lateinit var villageSpinner: Spinner
        lateinit var villageMapButton: ImageView

        lateinit var addressEditText: EditText
        lateinit var addressDetailEditText: EditText

        lateinit var telEditText: EditText

        lateinit var disabilitySwitch: Switch

        lateinit var disabilitySpinner: Spinner
        lateinit var disabilityReasonSpinner: Spinner

        lateinit var illnessSwitch: Switch
        lateinit var illnessSpinner: Spinner
        lateinit var illnessReasonSpinner: Spinner

        lateinit var schoolTypeTitleTextView: TextView
        lateinit var schoolTypeSpinner: Spinner
        lateinit var schoolTypeOtherEditText: EditText
        lateinit var schoolTypeReasonSpinner: Spinner
        lateinit var schoolNameTitleTextView: TextView
        lateinit var schoolNameSpinner: Spinner
        lateinit var schoolGradeTitleTextView: TextView
        lateinit var schoolGradeEditText: EditText
        lateinit var schollTypeMessageTextView: TextView
        lateinit var schoolGradeMessageTextView: TextView

        lateinit var detail2: LinearLayout
        lateinit var supportCountryTitleTextView: TextView
        lateinit var supportCountrySpinner: Spinner
        lateinit var supplyPlanTitleTextView: TextView
        lateinit var supplyPlanSpinner: Spinner
        lateinit var respondentNameTitleTextView: TextView
        lateinit var respondentNameEditText: EditText
        lateinit var relationshipTitleTextView: TextView
        lateinit var relationshipSpinner: Spinner
        lateinit var interviewPlaceTitleTextView: TextView
        lateinit var interviewPlaceSpinner: Spinner

        lateinit var detail3: LinearLayout
        lateinit var familyMemberTitleTextView: TextView
        lateinit var fatherSwitch: Switch
        lateinit var fatherReasonSpinner: Spinner
        lateinit var motherSwitch: Switch
        lateinit var motherReasonSpinner: Spinner
        lateinit var parentMessageTextView: TextView

        lateinit var mainguardianTitleTextView: TextView
        lateinit var mainguardianSpinner: Spinner
        lateinit var mainguardianMessageTextView: TextView
        lateinit var guardianNameEditText: EditText

        lateinit var incomeProviderTitleTextView: TextView
        lateinit var incomeProviderSpinner: Spinner
        lateinit var incomeEditText: EditText
        lateinit var incomeProviderMessageTextView: TextView

        lateinit var consentTitleTextView: TextView
        lateinit var consentImageView: ImageView
        lateinit var consentButtonContainer: LinearLayout
        lateinit var consentAddButton: ImageView
        lateinit var consentDeleteButton: ImageView

        lateinit var siblingTitleTextView: TextView
        lateinit var noBrotherEditText: EditText
        lateinit var noSisterEditText: EditText

        lateinit var siblingSponsorshipEditText: EditText
        lateinit var siblingSponsorshipQrcodeImageView: ImageView
        lateinit var siblingSponsorshipImageView: ImageView
        lateinit var siblingsEditContainer: LinearLayout
        lateinit var siblingsContainer: LinearLayout

        lateinit var specialCaseSwitch: Switch
        lateinit var specialCase1Spinner: Spinner
        lateinit var specialCase2Spinner: Spinner
        lateinit var specialCase3Spinner: Spinner

        lateinit var detail4: LinearLayout
        lateinit var personalInfoLayout: LinearLayout

        lateinit var detail5: LinearLayout
        lateinit var remarkEditText: EditText
        lateinit var remarkMessageTextView: TextView
        lateinit var remarkRelationshipMessageTextView: TextView
        lateinit var remarkIllnessMessageTextView: TextView

        lateinit var generalImageButtonContainer: LinearLayout
        lateinit var profileImageButtonContainer: LinearLayout
        lateinit var birthButtonContainer: LinearLayout


        lateinit var returnContainer: LinearLayout
        lateinit var returnRemarkTitleTextView: TextView
        lateinit var returnItemsContainer: LinearLayout

        lateinit var title1ReturnCountTextView: TextView
        lateinit var title2ReturnCountTextView: TextView
        lateinit var title3ReturnCountTextView: TextView
        lateinit var title4ReturnCountTextView: TextView
        lateinit var title5ReturnCountTextView: TextView

        @SuppressLint("SetTextI18n")
        override fun createView(ui: AnkoContext<CifFragment>) = with(ui) {
            scrollView {
                isFocusableInTouchMode = true

                container = verticalLayout {
                    // 사진 등록
                    linearLayout {
                        backgroundColorResource = R.color.colorLightGray
                        padding = dip(10)

                        // 일반 사진
                        verticalLayout {
                            gravity = Gravity.CENTER
                            padding = dip(5)

                            photoImageView = imageView {
                                imageResource = R.drawable.icon_2
                            }.lparams(width = dip(157), height = dip(210))
                            generalImageButtonContainer = linearLayout {
                                gravity = Gravity.CENTER

                                photoAlbumButton = imageView { imageResource = R.drawable.b_gallery
                                }.lparams(width = dip(35), height = dip(35))
                                space {}.lparams(width = dimen(R.dimen.px20), height = matchParent)
                                photoCameraButton = imageView {
                                    imageResource = R.drawable.b_camera
                                }.lparams(width = dip(35), height = dip(35))
                                space {}.lparams(width = dimen(R.dimen.px20), height = matchParent)
                                photoDeleteButton = imageView {
                                    imageResource = R.drawable.b_delete
                                }.lparams(width = dip(35), height = dip(35))
                            }.lparams(width = matchParent, height = wrapContent) {
                                topMargin = dimen(R.dimen.px10)
                            }
                        }.lparams(width = 0, height = wrapContent, weight = 1f)

                        // 프로파일 사진
                        verticalLayout {
                            gravity = Gravity.CENTER
                            padding = dip(5)

                            verticalLayout {
                                gravity = Gravity.CENTER

                                profileImageImageView = circleImageView {
                                    imageResource = R.drawable.m_childlist
                                }.lparams(width = dip(79), height = dip(79))
                            }.lparams(width = dip(157), height = dip(210))
                            profileImageButtonContainer = linearLayout {
                                gravity = Gravity.CENTER

                                profileImageAlbumButton = imageView {
                                    imageResource = R.drawable.b_gallery
                                }.lparams(width = dip(35), height = dip(35))
                                space {}.lparams(width = dimen(R.dimen.px20), height = matchParent)
                                profileImageDeleteButton = imageView {
                                    imageResource = R.drawable.b_delete
                                }.lparams(width = dip(35), height = dip(35))
                            }.lparams(width = matchParent, height = wrapContent) {
                                topMargin = dimen(R.dimen.px10)
                            }
                        }.lparams(width = 0, height = wrapContent, weight = 1f)
                    }.lparams(width = matchParent, height = wrapContent)

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

                    // 1. Child information Title
                    title1 = linearLayout {
                        backgroundResource = R.drawable.header_cif
                        gravity = Gravity.CENTER or Gravity.START
                        padding = dip(15)

                        textView("1") {
                            typeface = Typeface.DEFAULT_BOLD
                        }.lparams(width = dip(20), height = dip(20))

                        linearLayout {
                            title1TextView = textView(R.string.label_child_information) {
                                typeface = Typeface.DEFAULT_BOLD
                            }.lparams(width = wrapContent, height = wrapContent)
                            title1ValidImageView = imageView {
                                imageResource = R.drawable.check
                                visibility = View.GONE
                            }.lparams(width = dimen(R.dimen.px40), height = dimen(R.dimen.px40))
                        }.lparams(width = 0, height = wrapContent, weight = 1f)

                        title1ReturnCountTextView = textView {
                            backgroundResource = R.drawable.orange_dot
                            textColorResource = R.color.colorWhite
                            gravity = Gravity.CENTER
                            visibility = View.GONE
                        }.lparams(width = dimen(R.dimen.px40), height = dimen(R.dimen.px40))
                        toggleButton1 = imageView {
                            imageResource = R.drawable.select_4
                        }
                    }.lparams(width = matchParent, height = dip(55))

                    // 1. Child information Detail
                    detail1 = verticalLayout {
                        isFocusableInTouchMode = true
                        padding = dip(15)
                        visibility = if (DEBUG_UI) View.VISIBLE else View.GONE

                        // 1.1 Child name
                        childNameTextView = textView("*1.1 " + owner.getString(R.string.label_child_name)) {
                        }
                        verticalLayout {
                            firstNameEditText = editText {
                                hint = "*" + owner.getString(R.string.label_first_name)
                                inputType = InputType.TYPE_CLASS_TEXT
                            }.lparams(width = matchParent, height = wrapContent)
                            middleNameEditText = editText {
                                hintResource = R.string.label_middle
                                inputType = InputType.TYPE_CLASS_TEXT
                            }.lparams(width = matchParent, height = wrapContent)
                            lastNameEditText = editText {
                                hint = "*" + owner.getString(R.string.label_last)
                                inputType = InputType.TYPE_CLASS_TEXT
                            }.lparams(width = matchParent, height = wrapContent)
                        }.lparams(width = matchParent, height = wrapContent)

                        // 1.2 Gender
                        linearLayout {
                            gravity = Gravity.CENTER_VERTICAL

                            genderTitleTextView = textView("*1.2 " + owner.getString(R.string.label_gender)) {
                            }
                            genderRadioGroup = radioGroup {
                                orientation = LinearLayout.HORIZONTAL

                                genderFemaleRadio = radioButton { textResource = R.string.label_female }
                                genderMaleRadio = radioButton { textResource = R.string.label_male }
                            }
                        }.lparams(width = matchParent, height = wrapContent)

                        // 1.3 Birth Date
                        birthDateTitleTextView = textView("*1.3 " + owner.getString(R.string.label_birthdate))
                        linearLayout {
                            birthDateEditText = editText {
                                isFocusable = false
                                hint = "MM-DD-YYYY"
                            }.lparams(width = 0, height = wrapContent, weight = 1f)
                            imageView { imageResource = R.drawable.re_4 }.lparams(width = dip(24), height = dip(24)) {
                                gravity = Gravity.CENTER_VERTICAL
                            }
                            ageEditText = editText {
                                hintResource = R.string.label_age
                                isFocusable = false
                                inputType = InputType.TYPE_CLASS_NUMBER
                                filters = arrayOf(InputFilter.LengthFilter(2))
                            }.lparams(width = 0, height = wrapContent, weight = 1f)

                        }
                        birthDateMessageTextView = textView(R.string.message_validate_birthdate) {
                            textColorResource = R.color.colorAccent
                            visibility = View.GONE
                        }
                        duplicateCheckButton = button(R.string.button_duplicate_check) {
                        }

                        duplicateCheckContainer = verticalLayout {
                            textView(R.string.label_birth_certificate).lparams(width = matchParent, height = wrapContent)
                            linearLayout {
                                gravity = Gravity.CENTER or Gravity.START

                                frameLayout {
                                    backgroundResource = R.drawable.layout_border

                                    birthImageView = imageView {
                                        imageResource = R.drawable.icon_1
                                    }.lparams(height = wrapContent) {
                                        gravity = Gravity.CENTER
                                    }
                                }.lparams(width = dimen(R.dimen.px218), height = dimen(R.dimen.px290))
                                birthButtonContainer = linearLayout {
                                    //space {}.lparams(width = dimen(R.dimen.px20), height = matchParent)
                                    //birthAlbumButton = imageView {
                                    //    imageResource = R.drawable.b_gallery02
                                    //}.lparams(width = dip(35), height = dip(35))
                                    space {}.lparams(width = dimen(R.dimen.px20), height = matchParent)
                                    birthCameraButton = imageView {
                                        imageResource = R.drawable.b_camera02
                                    }.lparams(width = dip(35), height = dip(35))
                                    space {}.lparams(width = dimen(R.dimen.px20), height = matchParent)
                                    birthDeleteButton = imageView {
                                        imageResource = R.drawable.b_delete02
                                    }.lparams(width = dip(35), height = dip(35))
                                }
                            }.lparams(width = matchParent, height = wrapContent)

                            // 1.4 Growth
                            growthTitleTextView = textView("*1.4 " + owner.getString(R.string.label_growth))
                            verticalLayout {
                                linearLayout {
                                    textView(R.string.label_height)
                                    heightEditText = editText {
                                        inputType = InputType.TYPE_CLASS_NUMBER
                                        filters = arrayOf(RangeInputFilter(max = 220))
                                    }.lparams(width = dip(80), height = wrapContent)
                                }.lparams(width = matchParent, height = wrapContent)
                                heightMessageTextView = textView(R.string.message_validate_height) {
                                    textColorResource = R.color.colorAccent
                                    visibility = View.GONE
                                }
                                linearLayout {
                                    textView(R.string.label_weight)
                                    weightEditText = editText {
                                        inputType = InputType.TYPE_CLASS_NUMBER
                                        filters = arrayOf(RangeInputFilter(max = 120))
                                    }.lparams(width = dip(80), height = wrapContent)
                                }.lparams(width = matchParent, height = wrapContent)
                                weightMessageTextView = textView(R.string.message_validate_weight) {
                                    textColorResource = R.color.colorAccent
                                    visibility = View.GONE
                                }
                                linearLayout {
                                    imageView {
                                        imageResource = R.drawable.re_4
                                    }.lparams(width = dip(24), height = dip(24)) {
                                        gravity = Gravity.CENTER_VERTICAL
                                    }
                                    textView(R.string.label_level_of_growth)
                                    growthEditText = editText {
                                        focusable = View.NOT_FOCUSABLE
                                    }.lparams(width = 0, height = wrapContent, weight = 1f)
                                }
                            }.lparams(width = matchParent, height = wrapContent)

                            // 1.5 Home address
                            addressTitleTextView = textView("*1.5 " + owner.getString(R.string.label_home_address))
                            verticalLayout {
                                visibility = View.VISIBLE

                                linearLayout {
                                    textView("*" +  owner.getString(R.string.label_village)).lparams(width = wrapContent, height = wrapContent)
                                    space {}.lparams(width = dimen(R.dimen.px10))
                                    villageSpinner = spinner {
                                    }
                                    villageMapButton = imageView {
                                        imageResource = R.drawable.b_map02
                                    }.lparams(width = dip(35), height = dip(35)) {
                                        gravity = Gravity.CENTER_VERTICAL
                                    }
                                }.lparams(width = matchParent, height = wrapContent)
                                linearLayout {
                                    textView("*" + owner.getString(R.string.label_addr) + "1.")
                                    addressEditText = editText {
                                    }.lparams(width = 0, height = wrapContent, weight = 1f)
                                }.lparams(width = matchParent, height = wrapContent)
                                linearLayout {
                                    textView(owner.getString(R.string.label_addr) + "2")
                                    addressDetailEditText = editText {
                                    }.lparams(width = 0, height = wrapContent, weight = 1f)
                                }.lparams(width = matchParent, height = wrapContent)
                            }.lparams(width = matchParent, height = wrapContent)

                            // 1.6 Tel. number
                            linearLayout {
                                textView("1.6 " + owner.getString(R.string.label_tel_number))
                                telEditText = editText {
                                    inputType = InputType.TYPE_CLASS_PHONE
                                    filters = arrayOf(InputFilter.LengthFilter(100))
                                }.lparams(width = 0, height = wrapContent, weight = 1f)
                            }.lparams(width = matchParent, height = wrapContent)

                            // 1.7
                            textView("*1.7")
                            verticalLayout {
                                disabilitySwitch = switch {
                                    textResource = R.string.label_disability
                                }
                                disabilitySpinner = spinner {
                                }.lparams(width = matchParent, height = wrapContent)
                                disabilityReasonSpinner = spinner {
                                }.lparams(width = matchParent, height = wrapContent)

                                illnessSwitch = switch {
                                    textResource = R.string.label_illness
                                }
                                illnessSpinner = spinner {
                                }.lparams(width = matchParent, height = wrapContent)
                                illnessReasonSpinner = spinner {
                                }.lparams(width = matchParent, height = wrapContent)
                            }.lparams(width = matchParent, height = wrapContent)

                            // 1.8
                            textView("*1.8") {
                            }.lparams(width = matchParent, height = wrapContent)
                            verticalLayout {
                                schoolTypeTitleTextView = textView("*" + owner.getString(R.string.label_school_type)) {
                                }.lparams(width = matchParent, height = wrapContent)
                                linearLayout {
                                    gravity = Gravity.CENTER
                                    schoolTypeSpinner = spinner {
                                    }.lparams(width = 0, height = wrapContent, weight = 1f)
                                }.lparams(width = matchParent, height = wrapContent)
                                schoolTypeOtherEditText = editText {
                                    hintResource = R.string.label_other
                                }.lparams(width = matchParent, height = wrapContent)
                                schollTypeMessageTextView = textView(R.string.message_validate_school_type) {
                                    textColorResource = R.color.colorAccent
                                    visibility = View.GONE
                                }
                                schoolTypeReasonSpinner = spinner {
                                }.lparams(width = matchParent, height = wrapContent)
                                linearLayout {
                                    gravity = Gravity.CENTER

                                    schoolNameTitleTextView = textView("*" + owner.getString(R.string.label_school_name)).lparams(width = wrapContent, height = wrapContent)
                                    space {}.lparams(width = dimen(R.dimen.px10))
                                    schoolNameSpinner = spinner {
                                    }.lparams(width = 0, height = wrapContent, weight = 1f)
                                }.lparams(width = matchParent, height = wrapContent)
                                linearLayout {
                                    schoolGradeTitleTextView = textView("*" + owner.getString(R.string.label_grade)).lparams(width = wrapContent, height = wrapContent)
                                    schoolGradeEditText = editText {
                                        inputType = InputType.TYPE_CLASS_NUMBER
                                        filters = arrayOf(InputFilter.LengthFilter(2))
                                    }.lparams(width = 0, height = wrapContent, weight = 1f)
                                }.lparams(width = matchParent, height = wrapContent)
                                schoolGradeMessageTextView = textView() {
                                    textColorResource = R.color.colorAccent
                                    visibility = View.GONE
                                }
                            }.lparams(width = matchParent, height = wrapContent)

                            nextButton1 = button(R.string.label_next) {
                                backgroundColorResource = R.color.colorBrown
                                textColorResource = R.color.colorWhite
                            }
                        }
                    }.lparams(width = matchParent, height = wrapContent)

                    // 2. Interview information
                    title2 = linearLayout {
                        backgroundResource = R.drawable.header_cif
                        gravity = Gravity.CENTER or Gravity.START
                        padding = dip(15)

                        textView("2") {
                            typeface = Typeface.DEFAULT_BOLD
                        }.lparams(width = dip(20), height = dip(20))
                        title2TextView = textView("Interview information") {
                            typeface = Typeface.DEFAULT_BOLD
                        }.lparams(width = wrapContent, height = wrapContent)
                        title2ValidImageView = imageView {
                            imageResource = R.drawable.check
                            visibility = View.GONE
                        }.lparams(width = dimen(R.dimen.px40), height = dimen(R.dimen.px40))
                        space {}.lparams(width = 0, height = wrapContent, weight = 1f)

                        title2ReturnCountTextView = textView {
                            backgroundResource = R.drawable.orange_dot
                            textColorResource = R.color.colorWhite
                            gravity = Gravity.CENTER
                            visibility = View.GONE
                        }.lparams(width = dimen(R.dimen.px40), height = dimen(R.dimen.px40))

                        toggleButton2 = imageView {
                            imageResource = R.drawable.select_4
                        }.lparams(width = wrapContent, height = wrapContent)
                    }.lparams(width = matchParent, height = dip(55))

                    detail2 = verticalLayout {
                        isFocusableInTouchMode = true
                        padding = dip(15)
                        visibility = if (DEBUG_UI) View.VISIBLE else View.GONE

                        supportCountryTitleTextView = textView("*2.1 " + owner.getString(R.string.label_support_country)) {
                        }.lparams(width = matchParent, height = wrapContent)
                        supportCountrySpinner = spinner {
                        }.lparams(width = matchParent, height = wrapContent)

                        supplyPlanTitleTextView = textView("*2.2 " + owner.getString(R.string.label_supply_plan)) {
                        }.lparams(width = matchParent, height = wrapContent)
                        supplyPlanSpinner = spinner {
                        }.lparams(width = matchParent, height = wrapContent)

                        respondentNameTitleTextView = textView("*2.3 " + owner.getString(R.string.label_respondent_name)) {
                        }.lparams(width = matchParent, height = wrapContent)
                        respondentNameEditText = editText {
                        }.lparams(width = matchParent, height = wrapContent)

                        relationshipTitleTextView = textView("*2.4 " + owner.getString(R.string.label_relationship_with_child)) {
                        }.lparams(width = matchParent, height = wrapContent)
                        relationshipSpinner = spinner {
                        }.lparams(width = matchParent, height = wrapContent)

                        interviewPlaceTitleTextView = textView("*2.5 " + owner.getString(R.string.label_interview_place)) {
                        }.lparams(width = matchParent, height = wrapContent)
                        interviewPlaceSpinner = spinner {
                        }.lparams(width = matchParent, height = wrapContent)

                        nextButton2 = button(R.string.label_next) {
                            backgroundColorResource = R.color.colorBrown
                            textColorResource = R.color.colorWhite
                        }.lparams(width = matchParent, height = wrapContent)
                    }.lparams(width = matchParent, height = wrapContent)

                    // 3. Family information
                    title3 = linearLayout {
                        backgroundResource = R.drawable.header_cif
                        gravity = Gravity.CENTER or Gravity.START
                        padding = dip(15)

                        textView("3") {
                            typeface = Typeface.DEFAULT_BOLD
                        }.lparams(width = dip(20), height = dip(20))
                        title3TextView = textView(R.string.label_family_information) {
                            typeface = Typeface.DEFAULT_BOLD
                        }.lparams(width = wrapContent, height = wrapContent)
                        title3ValidImageView = imageView {
                            imageResource = R.drawable.check
                            visibility = View.GONE
                        }.lparams(width = dimen(R.dimen.px40), height = dimen(R.dimen.px40))
                        space {}.lparams(width = 0, height = wrapContent, weight = 1f)

                        title3ReturnCountTextView = textView {
                            backgroundResource = R.drawable.orange_dot
                            textColorResource = R.color.colorWhite
                            gravity = Gravity.CENTER
                            visibility = View.GONE
                        }.lparams(width = dimen(R.dimen.px40), height = dimen(R.dimen.px40))

                        toggleButton3 = imageView {
                            imageResource = R.drawable.select_4
                        }.lparams(width = wrapContent, height = wrapContent)
                    }.lparams(width = matchParent, height = dip(55))

                    detail3 = verticalLayout {
                        isFocusableInTouchMode = true
                        padding = dip(15)
                        visibility = if (DEBUG_UI) View.VISIBLE else View.GONE

                        textView(R.string.label_based_on_the_member_of_living_together) {
                            textColorResource = R.color.colorAccent
                            bottomPadding = dimen(R.dimen.px10)
                        }.lparams(width = wrapContent, height = wrapContent)

                        familyMemberTitleTextView = textView("*3.1 " + owner.getString(R.string.label_no_of_family_member)) {
                        }.lparams(width = matchParent, height = wrapContent)
                        linearLayout {
                            fatherSwitch = switch {
                                textResource = R.string.label_father
                            }.lparams(width = dip(100), height = wrapContent)
                            fatherReasonSpinner = spinner {
                            }.lparams(width = matchParent, height = wrapContent)
                        }.lparams(width = matchParent, height = wrapContent)
                        linearLayout {
                            motherSwitch = switch {
                                textResource = R.string.label_mother
                            }.lparams(width = dip(100), height = wrapContent)
                            motherReasonSpinner = spinner {
                            }.lparams(width = matchParent, height = wrapContent)
                        }.lparams(width = matchParent, height = wrapContent)
                        parentMessageTextView = textView(R.string.message_validate_child_is_not_living_with_parents) {
                            textColorResource = R.color.colorAccent
                            visibility = View.GONE
                        }

                        mainguardianTitleTextView = textView("*3.2 " + owner.getString(R.string.label_main_guardian)) {
                        }.lparams(width = matchParent, height = wrapContent)
                        mainguardianSpinner = spinner {
                        }.lparams(width = matchParent, height = wrapContent)
                        guardianNameEditText = editText {
                            hint = "Guardian Name"
                        }.lparams(width = matchParent, height = wrapContent)
                        mainguardianMessageTextView = textView() {
                            textColorResource = R.color.colorAccent
                            visibility = View.GONE
                        }

                        incomeProviderTitleTextView = textView("*3.3 " + owner.getString(R.string.label_main_income_provider)) {
                        }.lparams(width = matchParent, height = wrapContent)
                        incomeProviderSpinner = spinner {
                        }.lparams(width = matchParent, height = wrapContent)
                        linearLayout {
                            incomeEditText = editText {
                                inputType = InputType.TYPE_CLASS_NUMBER
                                filters = arrayOf(InputFilter.LengthFilter(4))
                            }.lparams(width = 0, height = wrapContent, weight = 1f)
                            textView("(Per month)").lparams(width = wrapContent, height = wrapContent)
                        }.lparams(width = matchParent, height = wrapContent)
                        incomeProviderMessageTextView = textView(R.string.message_validate_child_is_not_living_with_parents) {
                            textColorResource = R.color.colorAccent
                            visibility = View.GONE
                        }

                        consentTitleTextView = textView("*3.4 " + owner.getString(R.string.label_guardians_consent)) {
                        }.lparams(width = matchParent, height = wrapContent)

                        linearLayout {
                            gravity = Gravity.CENTER or Gravity.START

                            frameLayout {
                                backgroundResource = R.drawable.layout_border

                                consentImageView = imageView {
                                    imageResource = R.drawable.icon_1
//                                }.lparams(width = dimen(R.dimen.px96), height = dimen(R.dimen.px116)) {
                                }.lparams(height = wrapContent) {
                                    gravity = Gravity.CENTER
                                }
                            }.lparams(width = dimen(R.dimen.px218), height = dimen(R.dimen.px290))
                            consentButtonContainer = linearLayout {
                                space {}.lparams(width = dimen(R.dimen.px20), height = matchParent)
                                consentAddButton = imageView {
                                    visibility = View.VISIBLE
                                    imageResource = R.drawable.modify
                                }.lparams(width = dip(35), height = dip(35))
                                space {}.lparams(width = dimen(R.dimen.px20), height = matchParent)
                                consentDeleteButton = imageView {
                                    visibility = View.GONE
                                    imageResource = R.drawable.b_delete02
                                }.lparams(width = dip(35), height = dip(35))
                            }
                        }.lparams(width = matchParent, height = wrapContent)

                        siblingTitleTextView = textView("*3.5 " + owner.getString(R.string.label_no_of_sibling)) {
                        }.lparams(width = matchParent, height = wrapContent)
                        linearLayout {
                            textView(R.string.label_brother).lparams(width = wrapContent, height = wrapContent)
                            noBrotherEditText = editText {
                                inputType = InputType.TYPE_CLASS_NUMBER
                                filters = arrayOf(InputFilter.LengthFilter(1))
                            }.lparams(width = 0, height = wrapContent, weight = 1f)
                            textView(R.string.label_sister).lparams(width = wrapContent, height = wrapContent)
                            noSisterEditText = editText {
                                inputType = InputType.TYPE_CLASS_NUMBER
                                filters = arrayOf(InputFilter.LengthFilter(1))
                            }.lparams(width = 0, height = wrapContent, weight = 1f)
                        }.lparams(width = matchParent, height = wrapContent)

                        textView(R.string.label_sibling_sponsorship_status).lparams(width = wrapContent, height = wrapContent)
                        siblingsEditContainer = linearLayout {

                            siblingSponsorshipEditText = editText {
                                hintResource = R.string.label_search_child
                            }.lparams(width = 0, height = wrapContent, weight = 1f)
                            space {}.lparams(width = dimen(R.dimen.px20), height = matchParent)

                            siblingSponsorshipQrcodeImageView = imageView {
                                imageResource = R.drawable.qr
                            }.lparams(width = dip(35), height = dip(35)) {
                                gravity = Gravity.CENTER_VERTICAL
                            }
                            space {}.lparams(width = dimen(R.dimen.px20), height = matchParent)

                            siblingSponsorshipImageView = imageView {
                                imageResource = R.drawable.search
                            }.lparams(width = dip(35), height = dip(35)) {
                                gravity = Gravity.CENTER_VERTICAL
                            }
                        }.lparams(width = matchParent, height = wrapContent)

                        siblingsContainer = verticalLayout {
                        }.lparams(width = matchParent, height = wrapContent)

                        linearLayout {
                            specialCaseSwitch = switch {
                                text = "3.6 " + owner.getString(R.string.label_special_case_type)
                            }.lparams(width = wrapContent, height = wrapContent)
                        }.lparams(width = matchParent, height = wrapContent)
                        specialCase1Spinner = spinner {
                        }.lparams(width = matchParent, height = wrapContent)
                        specialCase2Spinner = spinner {
                        }.lparams(width = matchParent, height = wrapContent)
                        specialCase3Spinner = spinner {
                        }.lparams(width = matchParent, height = wrapContent)

                        nextButton3 = button(R.string.label_next) {
                            backgroundColorResource = R.color.colorBrown
                            textColorResource = R.color.colorWhite
                        }.lparams(width = matchParent, height = wrapContent)
                    }.lparams(width = matchParent, height = wrapContent)

                    // 4. Personal information
                    title4 = linearLayout {
                        backgroundResource = R.drawable.header_cif
                        gravity = Gravity.CENTER or Gravity.START
                        padding = dip(15)

                        textView("4") {
                            typeface = Typeface.DEFAULT_BOLD
                        }.lparams(width = dip(20), height = dip(20))
                        title4TextView = textView(R.string.label_personal_information) {
                            typeface = Typeface.DEFAULT_BOLD
                        }.lparams(width = wrapContent, height = wrapContent)
                        title4ValidImageView = imageView {
                            imageResource = R.drawable.check
                            visibility = View.GONE
                        }.lparams(width = dimen(R.dimen.px40), height = dimen(R.dimen.px40))
                        space {}.lparams(width = 0, height = wrapContent, weight = 1f)

                        title4ReturnCountTextView = textView {
                            backgroundResource = R.drawable.orange_dot
                            textColorResource = R.color.colorWhite
                            gravity = Gravity.CENTER
                            visibility = View.GONE
                        }.lparams(width = dimen(R.dimen.px40), height = dimen(R.dimen.px40))

                        toggleButton4 = imageView {
                            imageResource = R.drawable.select_4
                        }.lparams(width = wrapContent, height = wrapContent)
                    }.lparams(width = matchParent, height = dip(55))

                    detail4 = verticalLayout {
                        isFocusableInTouchMode = true
                        padding = dip(15)
                        visibility = if (DEBUG_UI) View.VISIBLE else View.GONE

                        personalInfoLayout = verticalLayout {

                        }

                        nextButton4 = button(R.string.label_next) {
                            backgroundColorResource = R.color.colorBrown
                            textColorResource = R.color.colorWhite
                        }.lparams(width = matchParent, height = wrapContent)
                    }.lparams(width = matchParent, height = wrapContent)

                    // 5. Remark
                    title5 = linearLayout {
                        backgroundResource = R.drawable.header_cif
                        gravity = Gravity.CENTER or Gravity.START
                        padding = dip(15)

                        textView("5") {
                            typeface = Typeface.DEFAULT_BOLD
                        }.lparams(width = dip(20), height = dip(20))
                        title5TextView = textView(R.string.label_remark) {
                            typeface = Typeface.DEFAULT_BOLD
                        }.lparams(width = wrapContent, height = wrapContent)
                        title5ValidImageView = imageView {
                            imageResource = R.drawable.check
                            visibility = View.GONE
                        }.lparams(width = dimen(R.dimen.px40), height = dimen(R.dimen.px40))
                        space {}.lparams(width = 0, height = wrapContent, weight = 1f)

                        title5ReturnCountTextView = textView {
                            backgroundResource = R.drawable.orange_dot
                            textColorResource = R.color.colorWhite
                            gravity = Gravity.CENTER
                            visibility = View.GONE
                        }.lparams(width = dimen(R.dimen.px40), height = dimen(R.dimen.px40))

                        toggleButton5 = imageView {
                            imageResource = R.drawable.select_4
                        }.lparams(width = wrapContent, height = wrapContent)
                    }.lparams(width = matchParent, height = dip(55))

                    detail5 = verticalLayout {
                        isFocusableInTouchMode = true
                        padding = dip(15)
                        visibility = if (DEBUG_UI) View.VISIBLE else View.GONE

                        remarkEditText = editText {
                            backgroundResource = R.drawable.layout_border
                            gravity = Gravity.TOP
                            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                            minLines = 8
                            filters = arrayOf(InputFilter.LengthFilter(2000))
                        }.lparams(width = matchParent, height = wrapContent)
                        remarkMessageTextView = textView {
                            textColorResource = R.color.colorAccent
                            visibility = View.GONE
                        }
                        remarkRelationshipMessageTextView = textView(R.string.message_validate_remark_interviewee) {
                            textColorResource = R.color.colorAccent
                            visibility = View.GONE
                        }
                        remarkIllnessMessageTextView = textView(R.string.message_validate_remark_illness) {
                            textColorResource = R.color.colorAccent
                            visibility = View.GONE
                        }
                        nextButton5 = button(R.string.label_next) {
                            backgroundColorResource = R.color.colorBrown
                            textColorResource = R.color.colorWhite
                        }.lparams(width = matchParent, height = wrapContent) {
                            topMargin = dip(5)
                        }
                    }.lparams(width = matchParent, height = wrapContent)
                }.lparams(width = matchParent, height = wrapContent)
            }
        }
    }
}