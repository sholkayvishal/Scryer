/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.scryer

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation

class CategoryFragment : Fragment() {
    private lateinit var screenshotListView: RecyclerView
    private val screenshotAdapter = ScreenshotAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.fragment_category, container, false)
        screenshotListView = layout.findViewById(R.id.screenshot_list)
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initScreenshotList(view.context)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        (activity as? AppCompatActivity)?.setSupportActionBar(view?.findViewById(R.id.toolbar))
        getSupportActionBar(activity)?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let {
            when(it.itemId) {
                android.R.id.home -> Navigation.findNavController(view!!).navigateUp()
                else -> return super.onOptionsItemSelected(item)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initScreenshotList(context: Context) {
        val manager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
        screenshotListView.layoutManager = manager
        screenshotListView.adapter = screenshotAdapter
        screenshotListView.addItemDecoration(SpacesItemDecoration(dp2px(context, 8f)))
        val factory = ScreenshotViewModelFactory(ScreenshotRepository.from(context))
        ViewModelProviders.of(this, factory).get(ScreenshotViewModel::class.java)
                .getScreenshots()
                .observe(this, Observer { screenshots ->
                    screenshots?.filter {
                        it.category == arguments?.getString("category_name")?:""
                    }?.let {
                        screenshotAdapter.setScreenshotList(it)
                        screenshotAdapter.notifyDataSetChanged()
                        getSupportActionBar(activity)?.setSubtitle("${it.size} shots")
                    }
                })
    }
}

open class ScreenshotAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var screenshotList: List<ScreenshotModel>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_screenshot, parent, false)
        view.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT

        view.layoutParams.height = (parent.measuredWidth / 2f).toInt()
        view.setPadding(0, 0, 0, 0)

        val holder = ScreenshotItemHolder(view)
        holder.title = view.findViewById(R.id.title)
        holder.itemView.setOnClickListener { _ ->
            holder.adapterPosition.takeIf { position ->
                position != RecyclerView.NO_POSITION

            }?.let { position: Int ->
                Toast.makeText(parent.context, "Item ${screenshotList[position].name} clicked",
                        Toast.LENGTH_SHORT).show()
            }
        }
        return holder
    }

    override fun getItemCount(): Int {
        return screenshotList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val titleView = (holder as ScreenshotItemHolder).title
        titleView?.apply {
            text = screenshotList[position].name
        }
    }

    fun getItemAt(position: Int): ScreenshotModel {
        return screenshotList[position]
    }

    open fun setScreenshotList(list: List<ScreenshotModel>) {
        screenshotList = list
    }
}
