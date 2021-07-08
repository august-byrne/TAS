package com.example.protosuite.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.protosuite.R
import com.example.protosuite.data.db.entities.NoteItem
import com.example.protosuite.databinding.NoteItemBinding
import java.util.*


class NoteAdapter(private val clickListener: NoteListener): ListAdapter<NoteItem, NoteAdapter.ViewHolder>(
    NoteDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!, clickListener)
    }

    fun onItemDismiss(position: Int) {
        notifyItemRemoved(position)
    }

    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    fun actionComplete(){

    }

    class ViewHolder private constructor(val binding: NoteItemBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        private val pastelColorArray: List<Int> = listOf(
            R.color.pink,
            R.color.orange,
            R.color.orange_alternate,
            R.color.green,
            R.color.blue,
            R.color.blue_mid,
            R.color.brown,
            R.color.green_mid,
            R.color.pink_high,
            R.color.grey
        )

        fun bind(item: NoteItem, clickListener: NoteListener) {
            binding.dragDropImage.setBackgroundResource(pastelColorArray[item.creation_date!!.get(Calendar.SECOND)%10])
            binding.noteOrder.text = item.order.toString()
            binding.noteItem = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = NoteItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    class NoteDiffCallback : DiffUtil.ItemCallback<NoteItem>() {
        override fun areItemsTheSame(oldItem: NoteItem, newItem: NoteItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NoteItem, newItem: NoteItem): Boolean {
            return oldItem == newItem
        }
    }
}

class NoteListener(val clickListener: (noteId: Int, noteView: View) -> Unit) {
    fun onClick(item: NoteItem, noteView: View) = clickListener(item.id, noteView)
}
