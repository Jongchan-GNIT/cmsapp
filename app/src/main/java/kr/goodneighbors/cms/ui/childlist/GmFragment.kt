@file:Suppress("PrivatePropertyName", "LocalVariableName")

package kr.goodneighbors.cms.ui.childlist


import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
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
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.GsonBuilder
import kr.goodneighbors.cms.R
import kr.goodneighbors.cms.common.Constants
import kr.goodneighbors.cms.common.ImageEditAction
import kr.goodneighbors.cms.extensions.circleImageView
import kr.goodneighbors.cms.extensions.convertDateFormat
import kr.goodneighbors.cms.extensions.extension
import kr.goodneighbors.cms.extensions.getRealPath
import kr.goodneighbors.cms.extensions.getStringValue
import kr.goodneighbors.cms.extensions.isNetworkAvailable
import kr.goodneighbors.cms.extensions.observeOnce
import kr.goodneighbors.cms.extensions.toDateFormat
import kr.goodneighbors.cms.extensions.viewsRecursive
import kr.goodneighbors.cms.service.entities.ATCH_FILE
import kr.goodneighbors.cms.service.entities.GIFT_BRKDW
import kr.goodneighbors.cms.service.entities.INTV
import kr.goodneighbors.cms.service.entities.REMRK
import kr.goodneighbors.cms.service.entities.RPLY
import kr.goodneighbors.cms.service.entities.RPT_BSC
import kr.goodneighbors.cms.service.entities.RPT_DIARY
import kr.goodneighbors.cms.service.entities.SWRT
import kr.goodneighbors.cms.service.model.GiftConfirmData
import kr.goodneighbors.cms.service.model.GiftItemDetail
import kr.goodneighbors.cms.service.model.GmEditItem
import kr.goodneighbors.cms.service.model.GmImageItem
import kr.goodneighbors.cms.service.model.GmlEditItemSearch
import kr.goodneighbors.cms.service.viewmodel.GmlViewModel
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
import org.jetbrains.anko.checkBox
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
import org.jetbrains.anko.sdk25.coroutines.textChangedListener
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.indeterminateProgressDialog
import org.jetbrains.anko.support.v4.space
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.support.v4.viewPager
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
import java.math.BigDecimal
import java.util.*

class GmFragment : Fragment() {
    companion object {
        const val REQUEST_SIBLING_SPONSORSHIP = 1
        const val REQUEST_CONFIRM = 2

        const val REQUEST_IMAGE_FROM_GALLERY = 101
        const val REQUEST_IMAGE_FROM_CAMERA = 102

        const val REQUEST_VIDEO_FROM_GALLERY = 103
        const val REQUEST_VIDEO_FROM_CAMERA = 104

        private const val RPT_STCD_TS = "12"
        private const val RPT_STCD_WACDP = "13"

        fun newInstance(chrcp_no: String, rcp_no: String?, mng_no: String): GmFragment {
            val fragment = GmFragment()
            val args = Bundle()
            args.putString("chrcp_no", chrcp_no)
            args.putString("rcp_no", rcp_no)
            args.putString("mng_no", mng_no)

            fragment.arguments = args
            return fragment
        }
    }

    private val logger: Logger by lazy {
        LoggerFactory.getLogger(GmFragment::class.java)
    }

    private val viewModel: GmlViewModel by lazy {
        GmlViewModel()
    }

    private val ui = FragmentUI()

    private lateinit var defaultColorList: ColorStateList

    private val sdMain = Environment.getExternalStorageDirectory()
    private val contentsRootDir = File("$sdMain/${Constants.DIR_HOME}/${Constants.DIR_CONTENTS}")

    private lateinit var chrcp_no: String
    private lateinit var rcp_no: String
    private lateinit var mng_no: String

    lateinit var progress: ProgressDialog

    private var isEditable = true
    private var returnCode: String? = null

    private var currentItem: GmEditItem? = null

    private var viewPagerAdapter: ImageAdapter? = null
    private var mCurrentPhotoPath: String = ""

    private var videoFile: File? = null
    private var tempVideoFile: File? = null

    fun isEditable(): Boolean {
        return this.isEditable
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        chrcp_no = arguments!!.getString("chrcp_no", "")
        rcp_no = arguments!!.getString("rcp_no", "")
        mng_no = arguments!!.getString("mng_no", "")

        viewModel.getGmEditItem().observe(this, Observer { item: GmEditItem? ->
            logger.debug("viewModel.getGmEditItem() : $item")
            currentItem = item
            item?.apply {

                isEditable = (RPT_STCD == "12" || RPT_STCD == "15" || RPT_STCD == "16")
                if (isEditable && RPT_STCD != "16") setHasOptionsMenu(true)

                if (isEditable) {
                    ui.videoButtonContainer.visibility = View.VISIBLE
                    ui.remarkEditText.isEnabled = true
                } else {
                    ui.videoButtonContainer.visibility = View.GONE
                    ui.remarkEditText.isEnabled = false
                }

                ui.nameTextView.text = CHILD_NAME
                ui.childCodeTextView.text = CHILD_CODE

                val description = ArrayList<String>()
                val genderString = when (GNDR) {
                    "F" -> "/Female"
                    "M" -> "/Male"
                    else -> ""
                }
                description.add("${BDAY?.convertDateFormat()}(${AGE})$genderString")
                SCHL_NM?.apply { description.add(this) }
                VLG_NM?.apply { description.add(this) }

                val guardian = ArrayList<String>()
                MGDN_CD_NM?.let { guardian.add(it) }
                MGDN_NM?.let { guardian.add(it) }
                description.add("Guardian: ${guardian.joinToString(", ")}")

                ui.descTextView.text = description.joinToString("\n")

                val thumb = THUMB_FILE_PATH?.let {
                    val ff = File(contentsRootDir, it)
                    if (ff.exists()) {
                        ff
                    } else null
                }

                if (thumb == null) {
                    Glide.with(this@GmFragment).load(R.drawable.m_childlist)
                            .apply(RequestOptions.skipMemoryCacheOf(true))
                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                            .into(ui.thumbnameImageView)
                } else {
                    Glide.with(this@GmFragment).load(thumb)
                            .apply(RequestOptions.skipMemoryCacheOf(true))
                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                            .into(ui.thumbnameImageView)
                }

                val photo = FILE_PATH?.let {
                    val ff = File(contentsRootDir, it)
                    if (ff.exists()) {
                        ff
                    } else null
                }

                if (photo == null) {
                    Glide.with(this@GmFragment).load(R.drawable.m_childlist)
                            .apply(RequestOptions.skipMemoryCacheOf(true))
                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                            .into(ui.generalImageView)
                } else {
                    Glide.with(this@GmFragment).load(photo)
                            .apply(RequestOptions.skipMemoryCacheOf(true))
                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                            .into(ui.generalImageView)
                }

                if (TEL_NO.isNullOrBlank()) {
                    ui.telephoneImageView.visibility = View.GONE
                } else {
                    ui.telephoneImageView.visibility = View.VISIBLE
                    ui.telephoneImageView.onClick {
                        alert(TEL_NO!!) {
                            yesButton {

                            }
                        }.show()
                    }
                }

                val siblings = ArrayList<String>()
                if (!SIBLING1.isNullOrBlank()) siblings.add(SIBLING1!!)
                if (!SIBLING2.isNullOrBlank()) siblings.add(SIBLING2!!)
                ui.siblingsImageView.visibility = if (siblings.isEmpty()) View.GONE else View.VISIBLE

                ui.mngNoTextView.text = MNG_NO ?: ""
                ui.sponsorNameTextView.text = RELMEM_NM ?: ""
                ui.giftMoneyTextView.text = "$ ${GIFT_DAMT ?: "0"}"
                ui.giftMoneyTextView2.text = "${GIFT_DAMT ?: "0"}"
                ui.sponsorMessageTextView.text = MBSH_REQ ?: ""

                logger.debug(GsonBuilder().create().toJson(gifts))

                if (rpt_bsc?.RPT_STCD ?: "" == "2" || rpt_bsc?.RPT_STCD ?: "" == "15") {
                    returnCode = rpt_bsc!!.RPT_STCD

                    ui.returnContainer.visibility = View.VISIBLE
                    when (rpt_bsc!!.RPT_STCD) {
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
                                    "1", "7" -> {
                                        ui.giftlistTitleTextView.textColorResource = R.color.colorAccent
                                    }
                                    "2", "3", "5" -> {
                                        ui.photoTitleTextView.textColorResource = R.color.colorAccent
                                    }
                                }
                            }
                        }
                    }
                } else {
                    ui.returnContainer.visibility = View.GONE
                }

                ui.remarkEditText.setText(rpt_bsc?.REMRK?.REMRK_ENG ?: "")

                ui.giftListContainer.removeAllViews()
//                var totalSum: BigDecimal = BigDecimal.ZERO
                if (gifts != null) {
                    AnkoContext.createDelegate(ui.giftListContainer).apply {
                        gifts!!.forEach { giftItem ->
                            var detailContainer: ViewGroup? = null

                            verticalLayout {
                                gravity = Gravity.CENTER_VERTICAL
                                topPadding = dip(15)
                                bottomPadding = dip(15)

                                view { backgroundColorResource = R.color.colorSplitLine }.lparams(width = matchParent, height = dip(1))
                                linearLayout {
                                    gravity = Gravity.CENTER_VERTICAL

                                    textView(giftItem.master.CD_ENM) {

                                    }.lparams(width = 0, height = wrapContent, weight = 1f)

                                    imageView {
                                        imageResource = R.drawable.select_4
                                        onClick {
                                            if (detailContainer?.visibility == View.VISIBLE) {
                                                detailContainer?.visibility = View.GONE
                                                imageResource = R.drawable.select_4
                                            } else {
                                                detailContainer?.visibility = View.VISIBLE
                                                imageResource = R.drawable.select_5
                                            }
                                        }
                                    }
                                }
                            }
                            detailContainer = verticalLayout {
                                visibility = View.GONE
                                linearLayout {
                                    gravity = Gravity.CENTER_VERTICAL

                                    textView(R.string.label_gift_list) {
                                        typeface = Typeface.DEFAULT_BOLD
                                    }.lparams(width = 0, height = wrapContent, weight = 4.5f)
                                    textView("No.") {
                                        typeface = Typeface.DEFAULT_BOLD
                                    }.lparams(width = 0, height = wrapContent, weight = 1.5f)
                                    textView(R.string.label_unit_price) {
                                        typeface = Typeface.DEFAULT_BOLD
                                    }.lparams(width = 0, height = wrapContent, weight = 2f)
                                    textView(R.string.label_total_price) {
                                        typeface = Typeface.DEFAULT_BOLD
                                    }.lparams(width = 0, height = wrapContent, weight = 2f)
                                }
                                verticalLayout {
                                    isFocusableInTouchMode = true

                                    giftItem.detail.forEach { detailItem ->
                                        var giftCheckBox: CheckBox?
                                        var countEditText: EditText? = null
                                        var priceEditText: EditText? = null
                                        var sumEditText: EditText? = null

                                        fun calculateSum() {
                                            val price: BigDecimal = if (priceEditText?.getStringValue()?.isBlank() == true) BigDecimal.ZERO
                                            else priceEditText!!.getStringValue().toBigDecimal()
                                            val count: BigDecimal = if (countEditText?.getStringValue()?.isBlank() == true) BigDecimal.ONE
                                            else countEditText!!.getStringValue().toBigDecimal()
                                            val itemSum = price.multiply(count)
                                            sumEditText?.setText(itemSum.toString())
                                            calculateTotal()
                                        }

                                        linearLayout {
                                            gravity = Gravity.CENTER_VERTICAL
                                            tag = detailItem

                                            linearLayout {
                                                giftCheckBox = checkBox {
                                                    text = detailItem.GIFT_DTBD
                                                    isEnabled = isEditable

                                                    if (isEditable) {
                                                        onCheckedChange { _, isChecked ->
                                                            if (isChecked) {
                                                                countEditText?.isEnabled = true
                                                                priceEditText?.isEnabled = true
                                                            } else {
                                                                countEditText?.isEnabled = false
                                                                countEditText?.setText("")
                                                                priceEditText?.isEnabled = false
                                                                priceEditText?.setText("")
                                                                sumEditText?.setText("")
                                                            }
                                                        }
                                                    }
                                                }
                                                if (detailItem.GIFT_DAMT?.compareTo(0) ?: 0 > 0) {
                                                    giftCheckBox?.isChecked = true
                                                }
                                            }.lparams(width = 0, height = wrapContent, weight = 4.5f)

                                            linearLayout {
                                                padding = dip(10)
                                                countEditText = editText {
                                                    backgroundResource = R.drawable.layout_border
                                                    inputType = InputType.TYPE_CLASS_NUMBER
                                                    isEnabled = false
                                                    tag = "COUNT"
                                                    textChangedListener {
                                                        onTextChanged { _, _, _, _ ->
                                                            calculateSum()
                                                        }
                                                    }
                                                }.lparams(width = matchParent)
                                                detailItem.GIFT_NUM?.toString()?.apply { countEditText!!.setText(this) }
                                            }.lparams(width = 0, height = wrapContent, weight = 1.5f)

                                            linearLayout {
                                                padding = dip(10)
                                                priceEditText = editText {
                                                    backgroundResource = R.drawable.layout_border
                                                    inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                                                    isEnabled = false
                                                    tag = "PRICE"
                                                    textChangedListener {
                                                        onTextChanged { _, _, _, _ ->
                                                            calculateSum()
                                                        }
                                                    }
                                                }.lparams(width = 0, height = wrapContent, weight = 1f)
                                                textView(" $")
                                                detailItem.GIFT_DAMT?.apply { priceEditText!!.setText(this.toString()) }
                                            }.lparams(width = 0, height = wrapContent, weight = 2f)

                                            linearLayout {
                                                padding = dip(10)
                                                sumEditText = editText {
                                                    backgroundResource = R.drawable.layout_border
                                                    isFocusable = false
                                                    isClickable = false
                                                    tag = "SUM"
                                                }.lparams(width = 0, height = wrapContent, weight = 1f)
                                                textView(" $")
                                            }.lparams(width = 0, height = wrapContent, weight = 2f)

                                            calculateSum()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                initializeImages()

                if (isEditable) {
                    ui.confirmButton.visibility = View.VISIBLE
//                    ui.editContainer.viewsRecursive.filter { it is EditText }.forEach { node ->
//                        node.isEnabled = true
//                    }
                } else {
                    ui.confirmButton.visibility = View.GONE
//                    ui.editContainer.viewsRecursive.filter { it is EditText }.forEach { node ->
//                        node.isEnabled = false
//                    }
                }

                progress.dismiss()
            }
        })
    }

    private fun initializeImages() {
        val images = ArrayList<GmImageItem>()
        currentItem?.rpt_bsc?.ATCH_FILE?.forEach {
            if (it.IMG_DVCD == "331001") {
                images.add(GmImageItem(path = "$contentsRootDir/${it.FILE_PATH}/${it.FILE_NM}"))
            } else if (it.IMG_DVCD == "331004") {
                try {
                    videoFile = File("$contentsRootDir/${it.FILE_PATH}/${it.FILE_NM}")
                    if (videoFile!!.exists()) {
                        Glide.with(this)
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

        while (images.size < 3) {
            images.add(GmImageItem())
        }

        logger.debug("initializeImages : $images")

        val viewPager = ui.imageViewPager
        viewPagerAdapter = ImageAdapter(context = requireContext(), images = images, isEditable = isEditable,
                onClickListener = { item, action, index -> onImageEditListener(item, action, index) })
        viewPager.adapter = viewPagerAdapter

        viewPager.clipToPadding = false
        viewPager.leftPadding = 20
        viewPager.rightPadding = 20
        viewPager.pageMargin = 10
    }

    private var currentImageIndex = -1
    private fun onImageEditListener(item: GmImageItem, action: ImageEditAction, index: Int) {
        when (action) {
            ImageEditAction.IMAGE -> {
                val thumnail = File(item.path)
                if (thumnail.exists()) {
                    val ft = activity!!.supportFragmentManager.beginTransaction()
                    val newFragment = DialogImageViewFragment.newInstance(thumnail.path)
                    newFragment.show(ft, "gm_fragment_view")
                }
            }
            ImageEditAction.GALLARY -> {
                currentImageIndex = index

                val intent = Intent(Intent.ACTION_PICK)
                intent.setType("image/*")
                intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE)

                startActivityForResult(intent, REQUEST_IMAGE_FROM_GALLERY)
            }
            ImageEditAction.CAMERA -> {
                currentImageIndex = index
                captureCamera()
            }
            ImageEditAction.DELETE -> {
                viewPagerAdapter?.deleteImageItemByPosition(index)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        activity?.title = "GM"

        val v = ui.createView(AnkoContext.create(requireContext(), this))
        progress = indeterminateProgressDialog(R.string.message_fetching_data)

        defaultColorList = ui.emptyTextView.textColors

        ui.siblingsImageView.onClick {
            val ft = activity!!.supportFragmentManager.beginTransaction()
            val newFragment = SiblingSponsorshipDialogFragment.newInstance(chrcp_no = chrcp_no)
            newFragment.setTargetFragment(this@GmFragment, REQUEST_SIBLING_SPONSORSHIP)
            newFragment.show(ft, "SIBLINGS")
        }

        ui.mapImageView.onClick {
            if (!currentItem!!.VLG_LAT.isNullOrBlank() && !currentItem!!.VLG_LONG.isNullOrBlank()) {
//                if (requireContext().wifiManager.isWifiEnabled) {
                if (requireContext().isNetworkAvailable()) {
                    startActivity<MapsVillageActivity>(
                            "name" to currentItem?.VLG_NM,
                            "lat" to currentItem?.VLG_LAT,
                            "lng" to currentItem?.VLG_LONG
                    )
                } else {
                    toast(R.string.message_wifi_disabled)
                }
            } else {
                toast(R.string.message_location_is_not_define)
            }
        }

        ui.confirmButton.onClick {
            openConfirmDialog()
        }

        ui.videoImageView.onClick {
            //            if (videoFile != null && videoFile!!.exists()) {
//                val ft = activity!!.supportFragmentManager.beginTransaction()
//                val newFragment = DialogVideoViewFragment.newInstance(videoFile!!.path)
//                newFragment.show(ft, "gm_fragment_video_view")
//            }

            if (videoFile != null && videoFile!!.exists()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoFile!!.path))
                intent.setDataAndType(Uri.parse(videoFile!!.path), "video/*")
                startActivity(intent)
            }
        }

        //ui.videoFromGalleryImageView.onClick {
        //    val intent = Intent(Intent.ACTION_PICK)
        //    intent.setType("video/*")
//      //      intent.setType(android.provider.MediaStore.Video.Media.CONTENT_TYPE)

        //    startActivityForResult(intent, REQUEST_VIDEO_FROM_GALLERY)
        //}

        ui.videoFromCameraImageView.onClick {
            val prefix = "GML_"

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
            startActivityForResult(intent, REQUEST_VIDEO_FROM_CAMERA)
        }
        ui.videoDeleteImageView.onClick {
            ui.videoImageView.imageResource = R.drawable.movie
            videoFile = null
        }

        viewModel.setGmEditItemSearch(GmlEditItemSearch(chrcp_no = chrcp_no, rcp_no = rcp_no, mng_no = mng_no))

        return v
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        logger.debug("onCreateOptionsMenu")
        activity?.menuInflater?.inflate(R.menu.toolbar_cif, menu)

        // 저장 버튼 클릭
        menu?.findItem(R.id.cif_toolbar_save)!!.setOnMenuItemClickListener {
            save(RPT_STCD_WACDP)
            true
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CONFIRM -> {
                if (resultCode == Activity.RESULT_OK) {
                    save(RPT_STCD_TS)
                }
            }
            REQUEST_IMAGE_FROM_GALLERY -> {
                if (resultCode == Activity.RESULT_OK) {

                    if (data?.data != null) {
                        try {
                            val file = File(data.data.getRealPath(context!!))
                            viewPagerAdapter?.updateImageItemByPosition(file, currentImageIndex)
                            currentImageIndex = -1
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

                                            viewPagerAdapter?.updateImageItemByPosition(sizedImageFile, currentImageIndex)
                                            currentImageIndex = -1
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
            REQUEST_VIDEO_FROM_CAMERA -> {
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        videoFile = File(tempVideoFile?.path)
                        Glide.with(this)
                                .asBitmap()
                                .load(tempVideoFile?.path)
                                .into(ui.videoImageView)
                    } catch (e: Exception) {
                        logger.error("REQUEST_VIDEO_FROM_CAMERA : ", e)
                    }
                }
            }
            REQUEST_VIDEO_FROM_GALLERY -> {
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        val videoUri = data?.data
                        if (videoUri != null) {
                            Glide.with(this)
                                    .asBitmap()
                                    .load(videoUri)
                                    .into(ui.videoImageView)

                            videoFile = File(videoUri.getRealPath(requireContext()))
                        } else
                            videoFile = null
                    } catch (e: Exception) {
                        logger.error("REQUEST_VIDEO_FROM_GALLERY : ", e)
                    }
                }
            }
        }
    }

    private fun openConfirmDialog() {
        val checkedItems = LinkedList<GiftConfirmData>()
        ui.editContainer.viewsRecursive.filter { it is CheckBox && it.isChecked }.forEach { node ->
            val rowNode = node.parent.parent as LinearLayout

            val checkedItem = GiftConfirmData(TITLE = (node as CheckBox).text as String?)
            rowNode.viewsRecursive.filter { it is EditText }.forEach { childNode ->
                when (childNode.tag) {
                    "COUNT" -> checkedItem.COUNT = (childNode as EditText).getStringValue()
                    "PRICE" -> checkedItem.PRICE = (childNode as EditText).getStringValue()
                    "SUM" -> checkedItem.TOTAL = (childNode as EditText).getStringValue()
                }
            }

            logger.debug("checkedItem : $checkedItem")
            checkedItems.add(checkedItem)
        }

        if (checkedItems.isNotEmpty()) {
            val ft = activity!!.supportFragmentManager.beginTransaction()
            val newFragment = GmConfirmDialogFragment.newInstance(checkedItems)
            newFragment.setTargetFragment(this@GmFragment, REQUEST_CONFIRM)
            newFragment.show(ft, "REQUEST_CONFIRM")
        }
    }

    private fun calculateTotal(): Boolean {
        val prices = ArrayList<BigDecimal>()
        ui.editContainer.viewsRecursive.filter { it is CheckBox && it.isChecked }.forEach { node ->
            val rowNode = node.parent.parent as LinearLayout

            rowNode.viewsRecursive.filter { it is EditText && it.tag == "SUM" }.forEach { childNode ->
                val sumEditText = childNode as EditText

                val price: BigDecimal = if (sumEditText.getStringValue()?.isBlank() == true) BigDecimal.ZERO
                else sumEditText.getStringValue().toBigDecimal()

                prices.add(price)
            }
        }

        ui.totalSumTextView.text = prices.fold(BigDecimal.ZERO, BigDecimal::add).toString()

        val v1: BigDecimal = if (ui.totalSumTextView.text.toString().isBlank()) BigDecimal.ZERO else ui.totalSumTextView.text.toString().toBigDecimal()
        val v2: BigDecimal = if (ui.giftMoneyTextView2.text.toString().isBlank()) BigDecimal.ZERO else ui.giftMoneyTextView2.text.toString().toBigDecimal()

        logger.debug("v1 : $v1, v2 : $v2, v1 < v2 : ${v1 < v2}, v1 == v2 : ${v1 == v2}, v1 > v2 : ${v1 > v2}")
        when {
            v1 < v2 -> {
                ui.totalSumTextView.textColorResource = R.color.colorBlue
                ui.confirmButton.isEnabled = false
            }
            v1.compareTo(v2) == 0 -> {
                ui.totalSumTextView.setTextColor(defaultColorList)
                ui.confirmButton.isEnabled = true

                return true
            }
            v1 > v2 -> {
                ui.totalSumTextView.textColorResource = R.color.colorAccent
                ui.confirmButton.isEnabled = false
            }
        }

        return false
    }

    private fun captureCamera() {
        val state = Environment.getExternalStorageState()

        if (Environment.MEDIA_MOUNTED == state) {
            val camera = Camera.open()
            val parameters = camera.parameters
            val sizeList = parameters.supportedPictureSizes
            //카메라 SupportedPictureSize목록 출력 로그
//            logger.debug("--SupportedPictureSizeList Start--")
//            sizeList.forEach {
//                logger.debug("Width : ${it.width}, Height : ${it.height}")
//            }

//            logger.debug("--SupportedPictureSizeList End--")
            // 원하는 최적화 사이즈를 1280x720 으로 설정
//            val size = getOptimalPictureSize(parameters.supportedPictureSizes, 1280, 720)
//            val size = getOptimalPictureSize(parameters.supportedPictureSizes, 320, 240)
            val size = getOptimalPictureSize(parameters.supportedPictureSizes, 667, 500)
//            logger.debug("Selected Optimal Size : (" + size.width + ", " + size.height + ")")
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
        val imageFileName = "GML_"

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

    private fun validate(rpt_stcd: String): Boolean {
        var isValid = true

        if (!calculateTotal()) {
            isValid = false
            ui.giftlistTitleTextView.textColorResource = R.color.colorAccent
        } else {
            ui.giftlistTitleTextView.setTextColor(defaultColorList)
        }

        if (rpt_stcd != RPT_STCD_TS) {
            val fileCount = (ui.imageViewPager.adapter as ImageAdapter).getItems().filter { !it.path.isNullOrBlank() }.size
            if (fileCount < 1) {
                isValid = false
                ui.photoTitleTextView.textColorResource = R.color.colorAccent
            } else {
                ui.photoTitleTextView.setTextColor(defaultColorList)
            }
        }

        return isValid
    }

    private fun save(rpt_stcd: String) {
        if (!validate(rpt_stcd)) {
            toast(R.string.message_require_fields)
            return
        }

        val timestamp = Date().time
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val ctrCd = sharedPref.getString("user_ctr_cd", "")
        val brcCd = sharedPref.getString("user_brc_cd", "")
        val prjCd = sharedPref.getString("user_prj_cd", "")
        val userid = sharedPref.getString("userid", "")
        val username = sharedPref.getString("username", "")
        val auth_cd = sharedPref.getString("user_auth_cd", "")

        val report = currentItem?.rpt_bsc ?: RPT_BSC(RCP_NO = "$chrcp_no$mng_no", CHRCP_NO = chrcp_no, RPT_DVCD = "5")
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
            report.SWRT = SWRT(RCP_NO = report.RCP_NO)
            report.INTV = INTV(RCP_NO = report.RCP_NO, INTVR_NM = userid, INTV_DT = timestamp.toDateFormat("yyyyMMdd"))
        } else {
            report.UPD_DT = timestamp
            report.UPDR_ID = userid
        }
        report.YEAR = currentItem?.YEAR
        report.RPT_STCD = rpt_stcd
        report.LAST_UPD_DT = timestamp
        report.APP_MODIFY_DATE = timestamp

        val gifts = ArrayList<GIFT_BRKDW>()
        ui.editContainer.viewsRecursive.filter { it is CheckBox && it.isChecked }.forEach { node ->
            val rowNode = node.parent.parent as LinearLayout
            val rowItem = rowNode.tag as GiftItemDetail
//            GiftItemDetail(GRP_CD=247, GIFT_BCD=1, GIFT_SCD=1, GIFT_DTBD=T-Shirts, RCP_NO=null, SEQ_NO=null, GIFT_DAMT=null, GIFT_NUM=null)
            logger.debug("selected node : ${rowNode.tag}")


            val gift_brkdw = GIFT_BRKDW(RCP_NO = report.RCP_NO, SEQ_NO = gifts.size + 1, GIFT_BCD = rowItem.GIFT_BCD, GIFT_SCD = rowItem.GIFT_SCD, GIFT_DTBD = rowItem.GIFT_DTBD)

            rowNode.viewsRecursive.filter { it is EditText }.forEach { childNode ->
                when (childNode.tag) {
                    "COUNT" -> gift_brkdw.GIFT_NUM = (childNode as EditText).getStringValue()
                    "PRICE" -> gift_brkdw.GIFT_DAMT = (childNode as EditText).getStringValue().toDouble()
                }
            }

            gifts.add(gift_brkdw)
        }

        if (gifts.isNotEmpty()) {
            report.GIFT_BRKDW = gifts
        }

        val contentsRootDir = File("$sdMain/${Constants.DIR_HOME}/${Constants.DIR_CONTENTS}")
        val targetDirPath = "sw/${Constants.BUILD}/$ctrCd/${report.CHRCP_NO}"
        val targetDir = File(contentsRootDir, targetDirPath)
        val files = ArrayList<ATCH_FILE>()
        viewPagerAdapter?.getItems()?.filter { !it.path.isNullOrBlank() }?.forEachIndexed { index, gmImageItem ->
            val f = File(gmImageItem.path)
            if (f.exists()) {
                val targetFileName = "GML_${mng_no}_${chrcp_no}_${index + 1}_${timestamp.toDateFormat("yyyyMMddHHmmss")}.${f.extension()}"
                val targetFile = File(targetDir, targetFileName)

                if (f != targetFile) {
                    f.copyTo(targetFile, true)
                }

                files.add(ATCH_FILE(SEQ_NO = index + 1, RCP_NO = report.RCP_NO,
                        FILE_DVCD = report.RPT_DVCD, IMG_DVCD = "331001",
                        FILE_NM = targetFileName, FILE_PATH = targetDirPath))
            }
        }

        if (videoFile != null && videoFile!!.exists()) {
            val targetFileName = "GML_${mng_no}_${chrcp_no}_VI_${timestamp.toDateFormat("yyyyMMddHHmmss")}.${videoFile!!.extension()}"
            val targetFile = File(targetDir, targetFileName)

            if (videoFile!! != targetFile) {
                videoFile!!.copyTo(targetFile, true)
            }

            files.add(ATCH_FILE(SEQ_NO = files.size + 1, RCP_NO = report.RCP_NO,
                    FILE_DVCD = report.RPT_DVCD, IMG_DVCD = "331004",
                    FILE_NM = targetFileName, FILE_PATH = targetDirPath))
        }

        if (files.isNotEmpty()) report.ATCH_FILE = files

        report.REMRK = REMRK(RCP_NO = report.RCP_NO, REMRK_ENG = ui.remarkEditText.getStringValue())

        report.RPLY = RPLY(RCP_NO = report.RCP_NO, GMNY_MNGNO = mng_no, RELSH_CD = currentItem?.RELSH_CD
                , VISIT_DT = timestamp.toDateFormat("yyyyMMdd"), VSTR_NM = username, VSTP_CD = if (auth_cd == "330005") "4" else "1")

        logger.debug(GsonBuilder().create().toJson(report))

        viewModel.saveGm(report).observeOnce(this, Observer {
            if (it != null && it == true) {
                if (rpt_stcd == RPT_STCD_TS) {
                    setHasOptionsMenu(true)
                } else {
                    isEditable = false
                    activity!!.onBackPressed()
                }
            }
        })
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class FragmentUI : AnkoComponent<GmFragment> {
        lateinit var emptyTextView: TextView
        lateinit var editContainer: LinearLayout

        lateinit var nameTextView: TextView
        lateinit var childCodeTextView: TextView
        lateinit var descTextView: TextView

        lateinit var thumbnameImageView: ImageView
        lateinit var siblingsImageView: ImageView

        lateinit var telephoneImageView: ImageView
        lateinit var mapImageView: ImageView

        lateinit var mngNoTextView: TextView
        lateinit var sponsorNameTextView: TextView
        lateinit var giftMoneyTextView: TextView
        lateinit var giftMoneyTextView2: TextView
        lateinit var sponsorMessageTextView: TextView

        lateinit var giftListContainer: LinearLayout

        lateinit var totalSumTextView: TextView

        lateinit var confirmButton: Button

        lateinit var sponsorshipTitleTextView: TextView

        lateinit var remarkEditText: EditText

        lateinit var generalImageView: ImageView
        lateinit var imageContainer: LinearLayout

        lateinit var videoContainer: LinearLayout
        lateinit var videoButtonContainer: LinearLayout

        lateinit var imageViewPager: ViewPager

        lateinit var videoImageView: ImageView
        //lateinit var videoFromGalleryImageView: ImageView
        lateinit var videoFromCameraImageView: ImageView
        lateinit var videoDeleteImageView: ImageView

        lateinit var returnContainer: LinearLayout
        lateinit var returnRemarkTitleTextView: TextView
        lateinit var returnItemsContainer: LinearLayout

        lateinit var giftlistTitleTextView: TextView
        lateinit var photoTitleTextView: TextView

        override fun createView(ui: AnkoContext<GmFragment>) = with(ui) {
            scrollView {
                verticalLayout {
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

                    editContainer = verticalLayout {
                        padding = dimen(R.dimen.px20)

                        sponsorshipTitleTextView = textView("1. " + owner.getString(R.string.label_sponsorship_information)) {
                            gravity = Gravity.CENTER_VERTICAL
                            setTypeface(null, Typeface.BOLD)
                        }.lparams(width = matchParent, height = dimen(R.dimen.px70))

                        mngNoTextView = textView {
                            gravity = Gravity.CENTER_VERTICAL
                            textColorResource = R.color.colorAccent
                            leftPadding = dimen(R.dimen.px20)
                            setTypeface(null, Typeface.BOLD)
                        }.lparams(width = matchParent, height = dimen(R.dimen.px40))

                        textView("1.1 " + owner.getString(R.string.label_sponsor_name)) {
                            gravity = Gravity.CENTER_VERTICAL
                            leftPadding = dimen(R.dimen.px20)
                        }.lparams(width = matchParent, height = dimen(R.dimen.px70))
                        sponsorNameTextView = textView {
                            gravity = Gravity.CENTER_VERTICAL
                            leftPadding = dimen(R.dimen.px20)
                            setTypeface(null, Typeface.BOLD)
                        }.lparams(width = matchParent, height = dimen(R.dimen.px40))

                        textView("1.2 " + owner.getString(R.string.label_gift_money)) {
                            gravity = Gravity.CENTER_VERTICAL
                            leftPadding = dimen(R.dimen.px20)
                        }.lparams(width = matchParent, height = dimen(R.dimen.px70))
                        giftMoneyTextView = textView {
                            gravity = Gravity.CENTER_VERTICAL
                            leftPadding = dimen(R.dimen.px20)
                            setTypeface(null, Typeface.BOLD)
                        }.lparams(width = matchParent, height = dimen(R.dimen.px40))

                        textView("1.3 " + owner.getString(R.string.label_sponsors_message)) {
                            gravity = Gravity.CENTER_VERTICAL
                            leftPadding = dimen(R.dimen.px20)
                        }.lparams(width = matchParent, height = dimen(R.dimen.px70))
                        sponsorMessageTextView = textView("{{}}") {
                            gravity = Gravity.CENTER_VERTICAL
                            leftPadding = dimen(R.dimen.px20)
                            setTypeface(null, Typeface.BOLD)
                        }.lparams(width = matchParent, height = dimen(R.dimen.px40))


                        linearLayout {
                            giftlistTitleTextView = textView("*2." + owner.getString(R.string.label_gift_list)) {
                                setTypeface(null, Typeface.BOLD)
                            }

                            confirmButton = button(R.string.button_gift_list_confirm) {
                                visibility = View.GONE
                                isEnabled = false
                                backgroundColorResource = R.color.colorBrown
                                textColorResource = R.color.colorWhite
                                allCaps = false
                            }.lparams(width = wrapContent, height = dimen(R.dimen.px70)) {
                                leftMargin = dip(10)
                            }

                            textView("$") {
                            }.lparams {
                                leftMargin = dip(10)
                            }
                            totalSumTextView = textView {}

                            textView(" / $")
                            giftMoneyTextView2 = textView {}
                        }

                        giftListContainer = verticalLayout {
                        }

                        photoTitleTextView = textView("*3." + owner.getString(R.string.label_child_reply_photo)) {
                            gravity = Gravity.CENTER_VERTICAL
                            setTypeface(null, Typeface.BOLD)
                        }.lparams(width = matchParent, height = dimen(R.dimen.px70))


                        verticalLayout {
                            linearLayout {
                                generalImageView = imageView {

                                }.lparams(width = 0, weight = 1f) {
                                    padding = dip(10)
                                }
                                space {}.lparams(width = dip(10))
                                imageContainer = verticalLayout {
                                    //                                    padding = dip(10)

                                    imageViewPager = viewPager {

                                    }.lparams(width = matchParent, height = dimen(R.dimen.px550))
                                }.lparams(width = 0, weight = 1f)
                            }
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
                                        videoDeleteImageView = imageView {
                                            imageResource = R.drawable.b_delete02
                                        }.lparams(width = dimen(R.dimen.px70), height = dimen(R.dimen.px70))
                                    }.lparams(width = matchParent, height = dimen(R.dimen.px116))
                                }.lparams(width = 0, weight = 1f)
                                space { }.lparams(width = 0, weight = 1f)
                            }
                        }

                        textView("4. "+owner.getString(R.string.label_remark)) {
                            gravity = Gravity.CENTER_VERTICAL
                            setTypeface(null, Typeface.BOLD)
                        }.lparams(width = matchParent, height = dimen(R.dimen.px70))

                        remarkEditText = editText {
                            backgroundResource = R.drawable.layout_border
                            gravity = Gravity.TOP
                            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                            minLines = 8
                            filters = arrayOf(InputFilter.LengthFilter(2000))
                        }.lparams(width = matchParent, height = wrapContent)
                    }
                }
            }
        }
    }

    class ImageAdapter(private val context: Context, private val images: List<GmImageItem>,
                       private val isEditable: Boolean = true,
                       val onClickListener: (GmImageItem, ImageEditAction, Int) -> Unit) : PagerAdapter() {
        private val logger: Logger by lazy {
            LoggerFactory.getLogger(ImageAdapter::class.java)
        }

        fun updateImageItemByPosition(file: File, index: Int) {
            images[index].path = file.path
            notifyDataSetChanged()
        }

        fun deleteImageItemByPosition(index: Int) {
            images[index].path = null
            notifyDataSetChanged()
        }

        fun getItems(): List<GmImageItem> {
            return images
        }

        override fun getItemPosition(`object`: Any): Int {
            return POSITION_NONE
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object` as View
        }

        override fun destroyItem(parent: ViewGroup, position: Int, `object`: Any) {
            parent.removeView(`object` as View)
        }

        override fun getCount(): Int {
            return images.size
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val item = images[position]
            val view = context.UI {
                linearLayout {
                    lparams(width = matchParent, height = wrapContent)

//                    backgroundColorResource = R.color.colorLightGray
//                    padding = dip(10)
                    visibility = View.VISIBLE

                    verticalLayout {
                        gravity = Gravity.CENTER
//                        padding = dip(5)

                        imageView {
                            var hasImage = false
                            try {
                                logger.debug("ImageFile = $item")
                                if (item.path.isNullOrBlank()) {
                                    imageResource = R.drawable.icon_2
                                } else {
                                    val generalImageFile = File(item.path)

                                    if (generalImageFile.exists()) {
                                        Glide.with(container).load(generalImageFile)
                                                .apply(RequestOptions.skipMemoryCacheOf(true))
                                                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                                                .into(this)
                                        hasImage = true
                                    } else {
                                        imageResource = R.drawable.icon_2
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                imageResource = R.drawable.icon_2
                            }
                            onClick {
                                if (hasImage) onClickListener(item, ImageEditAction.IMAGE, position)
                            }
                        }.lparams(width = dip(157), height = dip(210))

                        if (isEditable) {
                            linearLayout {
                                gravity = Gravity.CENTER

                                //imageView {
                                //    imageResource = R.drawable.b_gallery
                                //    onClick {
                                //        onClickListener(item, ImageEditAction.GALLARY, position)
                                //    }
                                //}.lparams(width = dip(35), height = dip(35))
                                space {}.lparams(width = dimen(R.dimen.px20), height = matchParent)
                                imageView {
                                    imageResource = R.drawable.b_camera
                                    onClick {
                                        onClickListener(item, ImageEditAction.CAMERA, position)
                                    }
                                }.lparams(width = dip(35), height = dip(35))
                                space {}.lparams(width = dimen(R.dimen.px20), height = matchParent)
                                imageView {
                                    imageResource = R.drawable.b_delete
                                    onClick {
                                        onClickListener(item, ImageEditAction.DELETE, position)
                                    }
                                }.lparams(width = dip(35), height = dip(35))
                            }.lparams(width = matchParent, height = wrapContent) {
                                topMargin = dimen(R.dimen.px20)
                            }
                        } else {
                            space {}.lparams(width = matchParent, height = dip(35))
                        }
                    }.lparams(width = 0, height = wrapContent, weight = 1f)
                }
            }.view

            container.addView(view)

            return view
        }
    }
}
