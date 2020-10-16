@file:Suppress("PrivatePropertyName")

package kr.goodneighbors.cms.ui.childlist


import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.DialogFragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import kr.goodneighbors.cms.R
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.allCaps
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.button
import org.jetbrains.anko.dimen
import org.jetbrains.anko.dip
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.imageView
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.progressBar
import org.jetbrains.anko.scrollView
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textSizeDimen
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.view
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class CifDialogConsentFragment : DialogFragment() {
    companion object {
        private const val REQUEST_SIGNATURE = 0

        fun newInstance(rcp_no: String): CifDialogConsentFragment {
            val fragment = CifDialogConsentFragment()
            val args = Bundle()
            args.putString("rcp_no", rcp_no)

            fragment.arguments = args
            return fragment
        }
    }

    private val logger: Logger by lazy {
        LoggerFactory.getLogger(CifDialogConsentFragment::class.java)
    }

    private val ui = FragmentUI()

    private lateinit var rcp_no: String

    private var signatureImageUri: Uri? = null
    private var mCurrentPhotoPath: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = ui.createView(AnkoContext.create(requireContext(), this))

        rcp_no = arguments!!.getString("rcp_no", "")

        ui.signatureButton.onClick {
            val ft = activity!!.supportFragmentManager.beginTransaction()
            val newFragment = CifDialogSignatureFragment.newInstance(rcp_no)
            newFragment.setTargetFragment(this@CifDialogConsentFragment, REQUEST_SIGNATURE)
            newFragment.show(ft, "signature")
        }

        // Cancel 버튼
        ui.cancelButtonTextView.onClick {
            dismiss()
        }

        // Ok 버튼
        ui.saveButtonTextView.onClick {
            if (signatureImageUri != null) {
                ui.progressContainer.visibility = View.VISIBLE
                createSignatureImage()
            }
        }

        ui.deleteImageView.onClick {
            ui.signatureButton.visibility = View.VISIBLE
            ui.signatureImageContainer.visibility = View.GONE

            if (signatureImageUri != null) {
                logger.debug("imageurl : $signatureImageUri")
                val imageFile = File(signatureImageUri!!.path)
                imageFile.delete()
                signatureImageUri = null
            }
        }

        return v
    }

    override fun onStart() {
        super.onStart()
//        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.setCanceledOnTouchOutside(false)
    }

    private fun createSignatureImage() {
        doAsync {
            val backgroundImage = BitmapFactory.decodeResource(context!!.resources, R.drawable.sponsorship)
            val foregroundImage = MediaStore.Images.Media.getBitmap(activity!!.contentResolver, signatureImageUri)
            val signaturedConsentImage = Bitmap.createBitmap(backgroundImage.width, backgroundImage.height, backgroundImage.config)

            createImageFile()

            val alphaPaint = Paint()
            alphaPaint.alpha = 125

            val canvas = Canvas(signaturedConsentImage)
            canvas.drawBitmap(backgroundImage, Matrix(), null)

            val x = 330
            val xx = 620
            val y = 1380
            val yy = 1470
            canvas.drawBitmap(foregroundImage, null, Rect(x, y, xx, yy), alphaPaint)

            backgroundImage.recycle()
            foregroundImage.recycle()

            try {
                val mFileOutStream = FileOutputStream(mCurrentPhotoPath)

                signaturedConsentImage!!.compress(Bitmap.CompressFormat.PNG, 90, mFileOutStream)

                mFileOutStream.flush()
                mFileOutStream.close()

                finishedAsyncProcess()
            } catch (e: Exception) {
                logger.error("signatured consent image : ", e)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_SIGNATURE -> {
                if (resultCode == RESULT_OK && data != null && data.extras != null) {
                    val uri = data.extras?.get("URI") as Uri

                    signatureImageUri = uri

                    Glide.with(this).load(uri).into(ui.signatureImageView)
                    ui.signatureButton.visibility = View.GONE
                    ui.signatureImageContainer.visibility = View.VISIBLE
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val imageFileName = "SIG_${rcp_no}_"
        val storageDir = File(Environment.getExternalStorageDirectory().absolutePath + "/GoodNeighbors/", "Pictures")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        val image = File.createTempFile(
                imageFileName, /* prefix */
                ".png", /* suffix */
                storageDir     /* directory */
        )

        mCurrentPhotoPath = image.absolutePath
        return image
    }

    private fun finishedAsyncProcess() {
        runOnUiThread {
            logger.debug("mCurrentPhotoPath : $mCurrentPhotoPath")
            val data = Intent()
            data.putExtra("URI", mCurrentPhotoPath)

            targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, data)
            dismiss()
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class FragmentUI : AnkoComponent<CifDialogConsentFragment> {
        lateinit var contentsContainer: LinearLayout
        lateinit var progressContainer: LinearLayout
        lateinit var signatureImageContainer: FrameLayout

        lateinit var cancelButtonTextView: TextView
        lateinit var saveButtonTextView: TextView

        lateinit var signatureButton: Button

        lateinit var deleteImageView: ImageView

        lateinit var signatureImageView: ImageView


        override fun createView(ui: AnkoContext<CifDialogConsentFragment>) = with(ui) {
            frameLayout {
                lparams(width = matchParent, height = matchParent)

                contentsContainer = verticalLayout {
                    verticalLayout {
                        padding = dimen(R.dimen.px34)

                        textView(R.string.label_guardians_consent) {
                            typeface = Typeface.DEFAULT_BOLD
                            textColorResource = R.color.colorPrimaryDark
                            textSizeDimen = R.dimen.px34
                        }

                        view { backgroundColorResource = R.color.colorMenuSplitLine }.lparams(width = matchParent, height = dip(1)) {
                            topMargin = dimen(R.dimen.px20)
                            bottomMargin = dimen(R.dimen.px30)
                        }

                        scrollView {
                            scrollBarSize = dimen(R.dimen.px4)

                            verticalLayout {
                                textView(R.string.consent_1_title) { typeface = Typeface.DEFAULT_BOLD }
                                textView(R.string.consent_1_contents) {}.lparams { topMargin = dimen(R.dimen.px16) }

                                textView(R.string.consent_2_title) { typeface = Typeface.DEFAULT_BOLD }.lparams { topMargin = dimen(R.dimen.px26) }
                                textView(R.string.consent_2_contents) {}.lparams { topMargin = dimen(R.dimen.px16) }

                                textView(R.string.consent_3_title) { typeface = Typeface.DEFAULT_BOLD }.lparams { topMargin = dimen(R.dimen.px26) }
                                textView(R.string.consent_3_contents) {}.lparams { topMargin = dimen(R.dimen.px16) }

                                textView(R.string.consent_4_title) { typeface = Typeface.DEFAULT_BOLD }.lparams { topMargin = dimen(R.dimen.px26) }
                                textView(R.string.consent_4_contents) {}.lparams { topMargin = dimen(R.dimen.px16) }

                                textView(R.string.consent_5_title) { typeface = Typeface.DEFAULT_BOLD }.lparams { topMargin = dimen(R.dimen.px26) }
                                textView(R.string.consent_5_contents) {}.lparams { topMargin = dimen(R.dimen.px16) }
                            }
                        }.lparams(width = matchParent, height = 0, weight = 1f) {

                        }

                        view { backgroundColorResource = R.color.colorMenuSplitLine }.lparams(width = matchParent, height = dip(1)) {
                            topMargin = dimen(R.dimen.px30)
                            bottomMargin = dimen(R.dimen.px30)
                        }

                        signatureButton = button(R.string.label_signature) {
                            backgroundColorResource = R.color.colorBrown
                            textColorResource = R.color.colorWhite
                            allCaps = false
                        }

                        signatureImageContainer = frameLayout {
                            visibility = View.GONE

                            signatureImageView = imageView { }
                            deleteImageView = imageView {
                                imageResource = R.drawable.close
                            }.lparams(width = dimen(R.dimen.px34), height = dimen(R.dimen.px34)) {
                                gravity = Gravity.END
                            }
                        }.lparams(width = matchParent, height = dimen(R.dimen.px140))
                    }.lparams(width = matchParent, height = 0, weight = 1f)

                    linearLayout {
                        minimumHeight = dimen(R.dimen.px100)

                        cancelButtonTextView = textView(R.string.label_cancel) {
                            backgroundResource = R.drawable.layout_border
                            setTypeface(null, Typeface.BOLD)
                            textColorResource = R.color.colorBlack
                            gravity = Gravity.CENTER
                        }.lparams(width = 0, height = matchParent, weight = 1f) {
                            bottomMargin = dip(-1)
                            leftMargin = dip(-1)
                            rightMargin = dip(-1)
                        }

                        saveButtonTextView = textView(R.string.label_ok) {
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
                progressContainer = verticalLayout {
                    visibility = View.GONE
                    gravity = Gravity.CENTER
                    backgroundColor = Color.parseColor("#CC000000")
                    progressBar { }
                }
            }
        }
    }
}
