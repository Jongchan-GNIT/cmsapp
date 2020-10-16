@file:Suppress("PrivatePropertyName")

package kr.goodneighbors.cms.ui.childlist


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.DialogFragment
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import kr.goodneighbors.cms.R
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.dimen
import org.jetbrains.anko.dip
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class CifDialogSignatureFragment : DialogFragment() {
    companion object {
        private const val STROKE_WIDTH = 5f
        private const val HALF_STROKE_WIDTH = STROKE_WIDTH / 2

        fun newInstance(rcp_no: String): CifDialogSignatureFragment {
            val fragment = CifDialogSignatureFragment()
            val args = Bundle()
            args.putString("rcp_no", rcp_no)

            fragment.arguments = args
            return fragment
        }
    }

    private val ui = FragmentUI()

    private lateinit var rcp_no: String

    private var bitmap: Bitmap? = null
    private var signature: Signature? = null
    private var mCurrentPhotoPath: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = ui.createView(AnkoContext.create(requireContext(), this))

        rcp_no = arguments!!.getString("rcp_no", "")

        signature = Signature(activity!!, null)
        signature!!.setBackgroundColor(Color.WHITE)

        ui.signatureView.addView(signature, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        ui.saveButtonTextView.onClick {
            val imageFile: File = createImageFile()
            signature!!.save(v, mCurrentPhotoPath)

            val data = Intent()
            data.putExtra("URI", Uri.fromFile(imageFile))

            targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, data)
            dismiss()
        }

        ui.cancelButtonTextView.onClick {
            dismiss()
        }

        return v
    }

    override fun onStart() {
        super.onStart()
        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 600)
        dialog.setCanceledOnTouchOutside(false)
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val imageFileName = "CIF_${rcp_no}_SIG_"

        val storageDir = File(Environment.getExternalStorageDirectory().absolutePath + "/GoodNeighbors/", "Pictures")
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    inner class Signature(context: Context, attrs: AttributeSet?) : View(context, attrs) {
        private val paint = Paint()
        private val path = Path()

        private var lastTouchX: Float = 0f
        private var lastTouchY: Float = 0f
        private val dirtyRect = RectF()

        init {
            paint.isAntiAlias = true
            paint.color = Color.BLACK
            paint.style = Paint.Style.STROKE
            paint.strokeJoin = Paint.Join.ROUND
            paint.strokeWidth = STROKE_WIDTH
        }

        fun save(v: View, storedPath: String) {
            if (bitmap == null) {
                bitmap = Bitmap.createBitmap(ui.signatureView.width, ui.signatureView.height, Bitmap.Config.RGB_565)
            }
            val canvas = Canvas(bitmap!!)
            try {
                // Output the file
                val mFileOutStream = FileOutputStream(storedPath)
                v.draw(canvas)
                // Convert the output file to Image such as .png
                bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, mFileOutStream)

                mFileOutStream.flush()
                mFileOutStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        fun clear() {
            path.reset()
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            canvas.drawPath(path, paint)
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouchEvent(event: MotionEvent): Boolean {
            val eventX = event.x
            val eventY = event.y
            ui.saveButtonTextView.isEnabled = true

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    path.moveTo(eventX, eventY)
                    lastTouchX = eventX
                    lastTouchY = eventY
                    return true
                }

                MotionEvent.ACTION_MOVE,

                MotionEvent.ACTION_UP -> {
                    resetDirtyRect(eventX, eventY)
                    val historySize = event.historySize
                    for (i in 0 until historySize) {
                        val historicalX = event.getHistoricalX(i)
                        val historicalY = event.getHistoricalY(i)
                        expandDirtyRect(historicalX, historicalY)
                        path.lineTo(historicalX, historicalY)
                    }
                    path.lineTo(eventX, eventY)
                }
                else -> {
                    debug("Ignored touch event: $event")
                    return false
                }
            }

            invalidate((dirtyRect.left - HALF_STROKE_WIDTH).toInt(),
                    (dirtyRect.top - HALF_STROKE_WIDTH).toInt(),
                    (dirtyRect.right + HALF_STROKE_WIDTH).toInt(),
                    (dirtyRect.bottom + HALF_STROKE_WIDTH).toInt())

            lastTouchX = eventX
            lastTouchY = eventY

            return true
        }

        private fun debug(string: String) {
            Log.v("log_tag", string)
        }

        private fun expandDirtyRect(historicalX: Float, historicalY: Float) {
            if (historicalX < dirtyRect.left) {
                dirtyRect.left = historicalX
            } else if (historicalX > dirtyRect.right) {
                dirtyRect.right = historicalX
            }

            if (historicalY < dirtyRect.top) {
                dirtyRect.top = historicalY
            } else if (historicalY > dirtyRect.bottom) {
                dirtyRect.bottom = historicalY
            }
        }

        private fun resetDirtyRect(eventX: Float, eventY: Float) {
            dirtyRect.left = Math.min(lastTouchX, eventX)
            dirtyRect.right = Math.max(lastTouchX, eventX)
            dirtyRect.top = Math.min(lastTouchY, eventY)
            dirtyRect.bottom = Math.max(lastTouchY, eventY)
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class FragmentUI : AnkoComponent<CifDialogSignatureFragment> {
        lateinit var signatureView: LinearLayout

        lateinit var cancelButtonTextView: TextView
        lateinit var saveButtonTextView: TextView

        override fun createView(ui: AnkoContext<CifDialogSignatureFragment>) = with(ui) {
            verticalLayout {
                lparams(width = matchParent, height = dimen(R.dimen.px500))

                signatureView = verticalLayout {
                    backgroundColorResource = R.color.colorWhite
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
        }
    }
}
