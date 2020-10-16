package kr.goodneighbors.cms.ui


import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.imageView
import org.jetbrains.anko.progressBar
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.toast

class DialogImageViewFragment : DialogFragment() {
    companion object {
        fun newInstance(src: String): DialogImageViewFragment {
            val fragment = DialogImageViewFragment()
            val args = Bundle()
            args.putString("src", src)

            fragment.arguments = args
            return fragment
        }
    }

    private val ui = FragmentUI()

    var src: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = ui.createView(AnkoContext.create(requireContext(), this))

        src = arguments!!.getString("src")

        Glide.with(this)
                .load(src)
                .listener(object: RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        ui.dialogProgressBar.visibility = View.GONE
                        toast("Image load failed")
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        ui.dialogProgressBar.visibility = View.GONE
                        return false
                    }

                })
                .into(ui.dialogImageView)

        ui.dialogImageView.onClick {
            dismiss()
        }

        return v
    }

    override fun onStart() {
        super.onStart()
//        dialog.window.setBackgroundDrawableResource(R.drawable.rounded_dialog)
        dialog.window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class FragmentUI : AnkoComponent<DialogImageViewFragment> {
        lateinit var dialogProgressBar: ProgressBar

        lateinit var dialogImageView: ImageView

        override fun createView(ui: AnkoContext<DialogImageViewFragment>) = with(ui) {
            frameLayout {
                dialogProgressBar = progressBar {

                }
                dialogImageView = imageView {
                    adjustViewBounds = true
                }.lparams {
                    gravity = Gravity.CENTER
                }
            }
        }
    }
}
