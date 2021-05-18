package com.example.protosuite.adapters

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.protosuite.data.db.entities.DataItem
import com.example.protosuite.databinding.DataItemBinding


class NoteDataAdapter(private val clickListener: DataListener, private var tempDataList: MutableList<DataItem>): ListAdapter<DataItem, NoteDataAdapter.ViewHolder>(
    NoteDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent, ActivityTextListener(), TimeTextListener())
    }

    fun onItemInserted(position: Int) {
        notifyItemInserted(position)
    }

    override fun getItemCount(): Int {
        return tempDataList.size
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        holder.enableTextWatcher(tempDataList)
        super.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        holder.disableTextWatcher()
        super.onViewDetachedFromWindow(holder)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.activityTextListener.updatePosition(holder.bindingAdapterPosition)
        holder.timeTextListener.updatePosition(holder.bindingAdapterPosition)
        Log.d("flicker bug", "onBindViewHolder bind gets item at position $position")
        holder.bind(getItem(position)!!, clickListener)
    }

    class ViewHolder(
        val binding: DataItemBinding,
        var activityTextListener: ActivityTextListener,
        var timeTextListener: TimeTextListener
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun enableTextWatcher(tempDataList: MutableList<DataItem>) {
            binding.dataItemTitle.addTextChangedListener(activityTextListener)
            binding.dataItemTime.addTextChangedListener(timeTextListener)
            binding.timeIncrementPicker.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        //null
                    }

                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        unitPosition: Int,
                        id: Long
                    ) {
                        tempDataList[bindingAdapterPosition] =
                            tempDataList[bindingAdapterPosition].copy(
                                id = tempDataList[bindingAdapterPosition].id,
                                parent_id = tempDataList[bindingAdapterPosition].parent_id,
                                order = tempDataList[bindingAdapterPosition].order,
                                activity = tempDataList[bindingAdapterPosition].activity,
                                time = tempDataList[bindingAdapterPosition].time,
                                unit = unitPosition
                            )
                    }
                }
        }

        fun disableTextWatcher() {
            binding.dataItemTitle.removeTextChangedListener(activityTextListener)
            binding.dataItemTime.removeTextChangedListener(timeTextListener)
            binding.timeIncrementPicker.onItemSelectedListener = null
        }

        fun bind(item: DataItem, clickListener: DataListener) {
            binding.dataItem = item
            binding.clickListener = clickListener
            binding.timeIncrementPicker.setSelection(item.unit)
            binding.executePendingBindings()
        }

        companion object {
            fun from(
                parent: ViewGroup,
                activityTextListener: ActivityTextListener,
                timeTextListener: TimeTextListener
            ): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = DataItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding, activityTextListener, timeTextListener)
            }
        }
    }

    class NoteDiffCallback : DiffUtil.ItemCallback<DataItem>() {
        override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem.parent_id == newItem.parent_id
        }

        override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem == newItem
        }
    }

    inner class TimeTextListener : TextWatcher {
        private var position = 0

        //private var tempList: MutableList<String> = mutableListOf()
        fun updatePosition(position: Int) {
            this.position = position
        }

        fun getItem(): MutableList<DataItem> {
            return tempDataList
        }

        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {
            // no op
        }

        override fun onTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {
            val userInput: Int = if (charSequence.toString() == "") {
                0
            } else {
                charSequence.toString().toInt()
            }
            tempDataList[position] = tempDataList[position].copy(
                id = tempDataList[position].id,
                parent_id = tempDataList[position].parent_id,
                order = tempDataList[position].order,
                activity = tempDataList[position].activity,
                time = userInput,
                unit = tempDataList[position].unit
            )
        }

        override fun afterTextChanged(editable: Editable) {
            // no op
        }
    }

    inner class ActivityTextListener : TextWatcher {
        private var position = 0

        //private var tempList: MutableList<String> = mutableListOf()
        fun updatePosition(position: Int) {
            this.position = position
        }

        fun getItem(): MutableList<DataItem> {
            return tempDataList
        }

        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {
            // no op
        }

        override fun onTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {
            //mDataset[position] = charSequence.toString()
            //item.description = charSequence.toString()
            //tempList[position] = charSequence.toString()
            tempDataList[position] = tempDataList[position].copy(
                id = tempDataList[position].id,
                parent_id = tempDataList[position].parent_id,
                order = tempDataList[position].order,
                activity = charSequence.toString(),
                time = tempDataList[position].time,
                unit = tempDataList[position].unit
            )
        }

        override fun afterTextChanged(editable: Editable) {
            // no op
        }
    }
}

class DataListener(val clickListener: (parent_id: Int, noteDataView: View) -> Unit){
    fun onClick(item: DataItem, noteDataView: View) = clickListener(item.parent_id, noteDataView)
}
