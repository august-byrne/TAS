package com.example.protosuite.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.protosuite.data.db.entities.NoteItem
import com.example.protosuite.databinding.NoteDataHeaderBinding
import com.example.protosuite.databinding.RecyclerviewFooterBinding


class RecyclerViewHeaderAdapter(private var item: NoteItem) : RecyclerView.Adapter<RecyclerViewHeaderAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent, MyCustomEditTextListener())
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        holder.enableTextWatcher()
        super.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        holder.disableTextWatcher()
        super.onViewDetachedFromWindow(holder)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.myEditTextListener.updatePosition(holder.absoluteAdapterPosition)
        //holder.binding.noteContent.setText(item.description)
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return 1
    }

    class ViewHolder(
        val binding: NoteDataHeaderBinding, var myEditTextListener: MyCustomEditTextListener
    ) : RecyclerView.ViewHolder(binding.root) {
        fun enableTextWatcher() {
            binding.noteContent.addTextChangedListener(myEditTextListener)
        }

        fun disableTextWatcher() {
            binding.noteContent.removeTextChangedListener(myEditTextListener)
        }

        fun bind(item: NoteItem) {
            binding.noteItem = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup, textListener: MyCustomEditTextListener): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = NoteDataHeaderBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding, textListener)
            }
        }
    }

    // we make TextWatcher aware of the position it currently works with
    // this way, once a new item is attached in onBindViewHolder, it will
    // update current position MyCustomEditTextListener, the reference to which is kept by ViewHolder
    inner class MyCustomEditTextListener : TextWatcher {
        private var position = 0
        fun updatePosition(position: Int) {
            this.position = position
        }

        fun getItem(): NoteItem {
            return item
        }

        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {
            // no op
        }

        override fun onTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {
            //mDataset[position] = charSequence.toString()
            //item.description = charSequence.toString()
            item = item.copy(
                id = item.id,
                creation_date = item.creation_date,
                last_edited_on = item.last_edited_on,
                order = item.order,
                title = item.title,
                description = charSequence.toString()
            )
        }

        override fun afterTextChanged(editable: Editable) {
            // no op
        }
    }
}

class RecyclerViewFooterAdapter : RecyclerView.Adapter<RecyclerViewFooterAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int {
        return 1
    }

    class ViewHolder private constructor(val binding: RecyclerviewFooterBinding) :
        RecyclerView.ViewHolder(
            binding.root
        ) {
        fun bind() {
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RecyclerviewFooterBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}
