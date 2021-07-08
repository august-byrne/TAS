package com.example.protosuite.ui.notes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.protosuite.R
import com.example.protosuite.adapters.NoteAdapter
import com.example.protosuite.adapters.NoteListener
import com.example.protosuite.adapters.RecyclerViewFooterAdapter
import com.example.protosuite.data.db.entities.NoteItem
import com.example.protosuite.databinding.NotesListBinding
import com.example.protosuite.ui.MainContentFragmentDirections
import com.google.android.material.transition.MaterialElevationScale
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class NotesFragment : Fragment() {

    private var _binding: NotesListBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var noteAdapter: NoteAdapter

    private lateinit var reference: RecyclerView.AdapterDataObserver

    // Lazy Inject ViewModel
    //private val myViewModel: NoteViewModel by sharedViewModel()
    //private val myViewModel: NoteViewModel by viewModels()
    private val myViewModel: NoteViewModel by activityViewModels()

    private lateinit var mutableNoteList: MutableList<NoteItem>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //binding = DataBindingUtil.inflate(inflater, R.layout.notes_list, container, false)
        _binding = NotesListBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.mainFab.setOnClickListener { fabView: View ->
            exitTransition = MaterialElevationScale(false).apply {
                duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
            }
            reenterTransition = MaterialElevationScale(true).apply {
                duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
            }
            val extras =
                FragmentNavigatorExtras(fabView to getString(R.string.note_detail_transition_name))
            val directions = MainContentFragmentDirections.actionMainContentToNoteData()
            findNavController().navigate(directions, extras)
        }

        // To use the View Model with data binding, you have to explicitly
        // give the binding object a reference to it.
            //binding.noteViewModel = myViewModel

        noteAdapter = NoteAdapter(NoteListener { noteId, noteView ->
                //myViewModel.onNoteClicked(noteId)
            /*
            exitTransition = MaterialElevationScale(false).apply {
                duration =
                    resources.getInteger(R.integer.reply_motion_duration_large).toLong()
                //excludeTarget(noteView, true)
            }
            reenterTransition = MaterialElevationScale(true).apply {
                duration =
                    resources.getInteger(R.integer.reply_motion_duration_large).toLong()
                //excludeTarget(noteView, true)
            }
             */
            val extras =
                FragmentNavigatorExtras(noteView to getString(R.string.note_detail_transition_name))
            // The 'view' is the card view that was clicked to initiate the transition.
            val directions = MainContentFragmentDirections.actionMainContentToNoteData(noteId)
            findNavController().navigate(directions)//, extras)
        })

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.adapter = ConcatAdapter(noteAdapter, RecyclerViewFooterAdapter())

        var dataRefreshes = 0
        myViewModel.allNotes.observe(viewLifecycleOwner, {
            it?.let {
                Log.d("flicker bug", "list is size ${it.size}")
                    //myViewModel.refreshNoteList(it)
                dataRefreshes++
                mutableNoteList = it.toMutableList()
                myViewModel.setNoteListSize(it.size)
                noteAdapter.submitList(mutableNoteList)  //TODO: FIXED FLICKERING from the difference between noteListCopy and the database list
                //noteAdapter.submitList(myViewModel.noteListCopy) no flicker on drag-and-drop, but takes a view refresh before deletions appear
                //TODO: fix crashes when you create a new note too quickly after saving the previous new note
            }
        })

        //could also use SimpleItemTouchHelper()
        ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun isLongPressDragEnabled(): Boolean {
                return true
            }

            override fun isItemViewSwipeEnabled(): Boolean {
                return false
            }

            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
                val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
                return makeMovementFlags(dragFlags, swipeFlags)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.bindingAdapterPosition
                val toPosition = target.bindingAdapterPosition
                if (viewHolder.itemViewType != target.itemViewType) {
                    return false
                }
                Collections.swap(mutableNoteList, fromPosition, toPosition) //reorder our local list
                noteAdapter.onItemMove(
                    fromPosition,
                    toPosition
                )    // Notify the adapter of the move
                return true
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                viewHolder?.apply {
                    itemView.animate().alpha(0.85f).scaleX(0.85f).scaleY(0.85f).translationZ(10f)
                }
                super.onSelectedChanged(viewHolder, actionState)
            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                viewHolder.itemView.animate().alpha(1f).scaleX(1f).scaleY(1f).translationZ(0f)
                super.clearView(recyclerView, viewHolder)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                TODO("Not yet implemented")
            }
        }).attachToRecyclerView(binding.recyclerView)

        reference = object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                Log.d("flicker bug", "onItemRangeInserted")
                if (myViewModel.newLaunch) {
                    binding.recyclerView.scrollToPosition(0)
                    myViewModel.newLaunchComplete()
                }
                if (dataRefreshes > 1) {
                    binding.recyclerView.smoothScrollToPosition(positionStart)
                }
            }
        }
        noteAdapter.registerAdapterDataObserver(reference)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        myViewModel.updateNoteItemOrderInDatabase(mutableNoteList)
        noteAdapter.unregisterAdapterDataObserver(reference)
        _binding = null
    }
}
