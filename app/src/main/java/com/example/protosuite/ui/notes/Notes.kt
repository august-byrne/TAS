package com.example.protosuite.ui.notes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
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
import com.example.protosuite.ui.MainContentDirections
import com.google.android.material.transition.MaterialElevationScale
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.*


class Notes : Fragment() {

    private var _binding: NotesListBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var _noteAdapter: NoteAdapter? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val noteAdapter get() = _noteAdapter!!

    private lateinit var reference: RecyclerView.AdapterDataObserver

    // Lazy Inject ViewModel
    private val myViewModel: NoteViewModel by sharedViewModel()

    private lateinit var mutableNoteList: MutableList<NoteItem>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("DB_Save", "onCreateView has been called")
        //binding = DataBindingUtil.inflate(inflater, R.layout.notes_list, container, false)
        _binding = NotesListBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.mainFab.setOnClickListener { fabView: View ->
            exitTransition = MaterialElevationScale(false).apply {
                duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
                excludeChildren(fabView, true)
            }
            reenterTransition = MaterialElevationScale(true).apply {
                duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
                excludeChildren(fabView, true)
            }
            val extras =
                FragmentNavigatorExtras(fabView to getString(R.string.note_detail_transition_name))
            val directions = MainContentDirections.actionMainContentToNoteData()
            findNavController().navigate(directions, extras)
        }

        // To use the View Model with data binding, you have to explicitly
        // give the binding object a reference to it.
        binding.noteViewModel = myViewModel

        _noteAdapter = NoteAdapter(NoteListener { noteId, view ->
            myViewModel.onNoteClicked(noteId, view)
        })

        val footerAdapter = RecyclerViewFooterAdapter()
        val adapter = ConcatAdapter(noteAdapter, footerAdapter)

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.adapter = adapter

        myViewModel.navigateToNoteEditor.observe(viewLifecycleOwner, { NoteId ->
            NoteId?.let {
                myViewModel.noteView.observe(viewLifecycleOwner, { View ->
                    View?.let {
                        exitTransition = MaterialElevationScale(false).apply {
                            duration =
                                resources.getInteger(R.integer.reply_motion_duration_large).toLong()
                            //excludeChildren(View, true)
                        }
                        reenterTransition = MaterialElevationScale(true).apply {
                            duration =
                                resources.getInteger(R.integer.reply_motion_duration_large).toLong()
                            //excludeChildren(View, true)
                        }
                        val extras =
                            FragmentNavigatorExtras(View to getString(R.string.note_detail_transition_name))
                        // The 'view' is the card view that was clicked to initiate the transition.
                        val directions = MainContentDirections.actionMainContentToNoteData(NoteId)
                        findNavController().navigate(directions, extras)
                        myViewModel.onNoteNavigated()
                    }
                })
            }
        })

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

        //val callback: ItemTouchHelper.Callback = SimpleItemTouchHelperCallback(noteAdapter)
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
                    .withEndAction {
                        //myViewModel.updateNoteItems(mutableNoteList)
                    }
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        postponeEnterTransition()
        view.doOnPreDraw {
            startPostponedEnterTransition()
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        myViewModel.updateNoteItemOrderInDatabase(mutableNoteList)
        noteAdapter.unregisterAdapterDataObserver(reference)
        _noteAdapter = null
        _binding = null
    }
}
