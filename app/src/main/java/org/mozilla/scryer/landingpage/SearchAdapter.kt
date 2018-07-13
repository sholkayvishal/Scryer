/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.scryer.landingpage

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.widget.Filter
import android.widget.Filterable
import org.mozilla.scryer.persistence.ScreenshotModel

class SearchAdapter : ScreenshotAdapter(), Filterable {
    private lateinit var originalList: List<ScreenshotModel>
    private var searchTarget: String = ""

    override fun setScreenshotList(list: List<ScreenshotModel>) {
        originalList = list
        super.setScreenshotList(list)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val titleView = (holder as ScreenshotItemHolder).title
        titleView?.apply {
            val name = getItemFileName(position)
            val spannable = SpannableString(name)
            val start = name.indexOf(searchTarget)
            spannable.setSpan(BackgroundColorSpan(Color.RED), start, start + searchTarget.length,
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            titleView.text = spannable
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val newList = mutableListOf<ScreenshotModel>()
                constraint?.takeIf { it.isNotEmpty() }?.let {
                    for (screenshot in originalList) {
                        if (screenshot.path.toLowerCase().contains(constraint.toString().toLowerCase())) {
                            newList.add(screenshot)
                        }
                    }
                }
                // TODO: How to reference to super here?
                setScreenshotListToSuper(newList)
                val result = FilterResults()
                result.values = newList

                return result
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                searchTarget = constraint.toString()
                notifyDataSetChanged()
            }
        }
    }

    private fun setScreenshotListToSuper(list: List<ScreenshotModel>) {
        super.setScreenshotList(list)
    }
}