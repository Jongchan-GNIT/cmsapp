package kr.goodneighbors.cms.ui.home

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kr.goodneighbors.cms.R
import kr.goodneighbors.cms.extensions.observeOnce
import kr.goodneighbors.cms.extensions.toDateFormat
import kr.goodneighbors.cms.service.model.NoticeItem
import kr.goodneighbors.cms.service.viewmodel.HomeViewModel
import kr.goodneighbors.cms.ui.BaseActivityFragment
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.bottomPadding
import org.jetbrains.anko.dimen
import org.jetbrains.anko.dip
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.imageView
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textView
import org.jetbrains.anko.topPadding
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.view
import org.jetbrains.anko.wrapContent

class NoticeFragment : BaseActivityFragment() {
    companion object {
        fun newInstance(): NoticeFragment {
            return NoticeFragment()
        }
    }

    private val ui = FragmentUI()

    private val viewModel: HomeViewModel by lazy {
        HomeViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = ui.createView(AnkoContext.create(requireContext(), this))
        activity?.title = "Notification"

        ui.itemsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.findAllNoticeItem().observeOnce(this, Observer {
            it?.apply {
                ui.itemsRecyclerView.adapter = ListAdaptor(items = this
                        , onClickListener = {noticeItem->
                            noticeItem.IS_SELECTED = !noticeItem.IS_SELECTED
                            ui.itemsRecyclerView.adapter?.notifyDataSetChanged()
                        })
            }
        })

        return v
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class FragmentUI : AnkoComponent<NoticeFragment> {
        lateinit var itemsRecyclerView: RecyclerView

        override fun createView(ui: AnkoContext<NoticeFragment>) = with(ui) {
            verticalLayout {
                lparams(width = matchParent, height = matchParent)

                itemsRecyclerView = recyclerView {
                }.lparams(width = matchParent, height = 0, weight = 1f)
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class ListAdaptor(
            val items: List<NoticeItem>,
            val onClickListener: (NoticeItem) -> Unit) : RecyclerView.Adapter<ListAdaptor.ListAdaptorViewHolder>() {

        override fun getItemCount(): Int = items.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdaptorViewHolder {
            return ListAdaptorViewHolder(ListViewHolderUI().createView(AnkoContext.create(parent.context, parent)))
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ListAdaptorViewHolder, position: Int) {
            val item = items[position]

            holder.titleTextView.text = "[${item.CTGY_NM}] ${item.TTL}"
            holder.dateTextView.text = item.REG_DT?.toDateFormat() ?: ""
            holder.contentsTextView.text = item.CTS ?: ""

            holder.contentsViewButtonImageView.onClick {
                onClickListener(item)
            }

            if (item.IS_SELECTED) {
                holder.contentsContainer.visibility = View.VISIBLE
                holder.container.backgroundColorResource = R.color.colorNoticeBackground
                holder.contentsViewButtonImageView.imageResource = R.drawable.select_5
            }
            else {
                holder.contentsContainer.visibility = View.GONE
                holder.container.background = null
                holder.contentsViewButtonImageView.imageResource = R.drawable.select_4
            }
        }

        inner class ListAdaptorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var container: ViewGroup = itemView.findViewById(ListViewHolderUI.ID_CONTAINER)
            var contentsContainer: ViewGroup = itemView.findViewById(ListViewHolderUI.ID_CONTENTS_CONTAINER)

            var titleTextView: TextView = itemView.findViewById(ListViewHolderUI.ID_TITLE)
            var dateTextView: TextView = itemView.findViewById(ListViewHolderUI.ID_DATE)
            var contentsViewButtonImageView: ImageView = itemView.findViewById(ListViewHolderUI.ID_CONTENTS_VIEW_BUTTON)
            var contentsTextView: TextView = itemView.findViewById(ListViewHolderUI.ID_CONTENTS)
        }

        class ListViewHolderUI : AnkoComponent<ViewGroup> {
            companion object {
                const val ID_TITLE = 101
                const val ID_DATE = 102
                const val ID_CONTENTS_VIEW_BUTTON = 103
                const val ID_CONTENTS = 104

                const val ID_CONTAINER = 201
                const val ID_CONTENTS_CONTAINER = 202
            }
            override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
                verticalLayout {
                    verticalLayout {
                        padding = dip(20)
                        id = ID_CONTAINER

                        linearLayout {
                            verticalLayout {
                                textView {
                                    id = ID_TITLE
                                    typeface = Typeface.DEFAULT_BOLD
                                    textColorResource = R.color.colorBlack
                                }
                                textView {
                                    id = ID_DATE
                                }
                            }.lparams(width = 0, height = wrapContent, weight = 1f)

                            imageView {
                                imageResource = R.drawable.select_4

                                id = ID_CONTENTS_VIEW_BUTTON
                            }.lparams(width = dimen(R.dimen.px35), height = dimen(R.dimen.px35)) {
                                gravity = Gravity.CENTER_VERTICAL
                            }
                        }

                        verticalLayout {
                            id = ID_CONTENTS_CONTAINER
                            topPadding = dip(10)
                            view { backgroundColorResource = R.color.colorSplitLine }.lparams(width = matchParent, height = dip(1)) { }
                            textView {
                                id = ID_CONTENTS
                                topPadding = dip(10)
                                bottomPadding = dip(10)
                            }
                        }
                    }

                    view { backgroundColorResource = R.color.color888888 }.lparams(width = matchParent, height = dip(1)) { }
                }
            }
        }
    }
}