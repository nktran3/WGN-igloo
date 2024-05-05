package com.example.wgn_igloo.database

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wgn_igloo.R

class MemberAdapter(private var members: MutableList<Member>) :
    RecyclerView.Adapter<MemberAdapter.MemberViewHolder>() {

    class MemberViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fullNameTextView: TextView = view.findViewById(R.id.fridge_members)
        val usernameTextView: TextView = view.findViewById(R.id.fridge_members_username)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.members_item_layout, parent, false)
        return MemberViewHolder(view)
    }

//    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
//        val member = members[position]
//        holder.fullNameTextView.text = member.givenName + " " + member.familyName
//        holder.usernameTextView.text = member.username
//    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val member = members[position]
        holder.fullNameTextView.text = "${member.givenName} ${member.familyName}"
        holder.usernameTextView.text = member.username
        Log.d("MemberAdapter", "Binding view for: ${member.givenName} ${member.familyName}") // Debugging to see what's being bound
    }


    override fun getItemCount() = members.size

    fun updateMembers(newMembers: List<Member>) {
        members.clear()
        members.addAll(newMembers)
        notifyDataSetChanged()
    }
}