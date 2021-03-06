package com.example.whatsappclone.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsappclone.EmptyViewHolder
import com.example.whatsappclone.R
import com.example.whatsappclone.User
import com.example.whatsappclone.modules.UserViewHolder
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_chat.*

private const val DELETED_VIEW_TYPE = 1
private const val NORMAL_VIEW_TYPE = 2

class ContactsFragment : Fragment() {

    lateinit var mAdapter : FirestorePagingAdapter<User, RecyclerView.ViewHolder>
    private lateinit var viewManager: RecyclerView.LayoutManager
    val auth by lazy {
        FirebaseAuth.getInstance()
    }
    val database by lazy {
        FirebaseFirestore.getInstance().collection("users")
            .orderBy("name", Query.Direction.ASCENDING)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.e("check", "Setting up adapter")
        viewManager = LinearLayoutManager(requireContext())
        setupAdapter()
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    //Setting up adapter for Pagination
    private fun setupAdapter() {
        val config = PagedList.Config.Builder()
            .setPrefetchDistance(2)
            .setPageSize(10)
            .setEnablePlaceholders(false)
            .build()

        //Setting lifecycle owner so that network calls happen only when used by user not in background
        val options = FirestorePagingOptions.Builder<User>()
            .setLifecycleOwner(this)
            .setQuery(database, config, User::class.java)
            .build()

        mAdapter = object : FirestorePagingAdapter<User, RecyclerView.ViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                return when(viewType) {
                      NORMAL_VIEW_TYPE ->  UserViewHolder(layoutInflater.inflate(
                          R.layout.list_item, parent, false
                      ))
                    else -> EmptyViewHolder(layoutInflater.inflate(
                        R.layout.empty_view, parent, false
                    ))
                }
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: User) {
                if(holder is UserViewHolder) {
                    holder.bind(user = model)
                } else {

                }
            }

            override fun getItemViewType(position: Int): Int {
                val item = getItem(position)?.toObject(User::class.java)
                return if(auth.uid == item!!.uid) {
                    DELETED_VIEW_TYPE
                } else {
                    NORMAL_VIEW_TYPE
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }
    }
}
