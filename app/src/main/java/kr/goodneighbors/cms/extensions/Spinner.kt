package kr.goodneighbors.cms.extensions

import android.widget.ArrayAdapter
import android.widget.Spinner
import kr.goodneighbors.cms.R
import kr.goodneighbors.cms.service.entities.CD
import kr.goodneighbors.cms.service.model.SpinnerOption
import java.util.*


fun Spinner.setItem(items: List<SpinnerOption>?, hasEmptyOption: Boolean = false, hint: String? = null, adapterResource: Int = R.layout.spinneritem_dark) {
    if (items != null) {
        if (hasEmptyOption || hint != null) {
            val options = arrayListOf(SpinnerOption("", hint ?: ""))
            options.addAll(items)

            val spinnerAdapter = ArrayAdapter(context, adapterResource, options)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            adapter = spinnerAdapter
            tag = options
        } else {
            val spinnerAdapter = ArrayAdapter(context, adapterResource, items)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            adapter = spinnerAdapter
            tag = items
        }
    } else {
        val options = arrayListOf(SpinnerOption("", ""))
        val spinnerAdapter = ArrayAdapter(context, adapterResource, options)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        adapter = spinnerAdapter
        tag = options
    }
}


fun Spinner.setCodeItem(items: List<CD>?, hasEmptyOption: Boolean = false, hint: String? = null, adapterResource: Int = R.layout.spinneritem_dark) {
    val tagList = if (items != null) ArrayList(items) else ArrayList<CD>()

    val options = LinkedList<String>()
    if (hasEmptyOption || hint != null) {
        tagList.add(0, CD(GRP_CD = "", CD = ""))
        options.add(hint ?: "")
    }
    items?.forEach {
        options.add(it.CD_ENM?:"")
    }
    val spinnerAdapter = ArrayAdapter(context, adapterResource, options)
    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

    adapter = spinnerAdapter
    tag = tagList
}

fun Spinner.setSelectKey(key: String?) {
    var selectedIndex = 0
    if (tag != null && tag is List<*>) {
        try {
            val items = tag as List<*>

            items.forEachIndexed { index, option ->
                if (option is SpinnerOption) {
                    if (option.key == key) {
                        selectedIndex = index
                    }
                }
                else if (option is CD) {
                    if (option.CD == key) {
                        selectedIndex = index
                    }
                }
            }
        } catch (e: Exception) {
            System.err.println(e)
        }
    }

    setSelection(selectedIndex)
}

fun Spinner.setSelectValue(value: String?) {
    var selectedIndex = 0
    if (tag != null && tag is List<*>) {
        try {
            val items = tag as List<*>

            items.forEachIndexed { index, option ->
                if (option is SpinnerOption) {
                    if (option.value.toUpperCase() == value?.toUpperCase()) {
                        selectedIndex = index
                    }
                }
                else if (option is CD) {
                    if ((option.CD_ENM?:"").toUpperCase() == value?.toUpperCase()) {
                        selectedIndex = index
                    }
                }
            }
        } catch (e: Exception) {
            System.err.println(e)
        }
    }

    setSelection(selectedIndex)
}

fun Spinner.getValue(): String? {
    if (tag != null && tag is List<*>) {
        try {
            val items = tag as List<*>
            val item = items[selectedItemPosition]
            return when (item) {
                is SpinnerOption -> item.key
                is CD -> item.CD
                else -> item.toString()
            }
        } catch (e: Exception) {
            System.err.println(e)
        }
    }
    else {
        try {
            val item = selectedItem
            return when (item){
                is SpinnerOption -> item.key
                is CD -> item.CD
                else -> item.toString()
            }
        } catch (e: Exception) {
            System.err.println(e)
        }
    }
    return null
}

fun Spinner.getText(): String? {
    if (tag != null && tag is List<*>) {
        try {
            val items = tag as List<*>
            val item = items[selectedItemPosition]
            return when (item) {
                is SpinnerOption -> item.value
                is CD -> item.CD_ENM
                else -> item.toString()
            }
        } catch (e: Exception) {
            System.err.println(e)
        }
    }
    return null
}

fun Spinner.getSelectedIndex(key: String?): Int {
    var selectedIndex = 0
    if (tag != null && tag is List<*>) {
        try {
            val items = tag as List<*>

            items.forEachIndexed { index, option ->
                if (option is SpinnerOption) {
                    if (option.key == key) {
                        selectedIndex = index
                    }
                }
                else if (option is CD) {
                    if (option.CD == key) {
                        selectedIndex = index
                    }
                }
            }
        } catch (e: Exception) {
            System.err.println(e)
        }
    }
    return selectedIndex
}


