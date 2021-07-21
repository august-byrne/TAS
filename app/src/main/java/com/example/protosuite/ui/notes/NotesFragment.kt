package com.example.protosuite.ui.notes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDirections
import com.example.protosuite.data.db.entities.NoteItem
import com.example.protosuite.ui.MainContentFragmentDirections
import com.example.protosuite.ui.values.blue500
import com.example.protosuite.ui.values.special400
import com.example.protosuite.ui.values.yellow100
import com.example.protosuite.ui.values.yellow50
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class NotesFragment : Fragment() {
/*
    private var _binding: NotesListBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var noteAdapter: NoteAdapter

    private lateinit var reference: RecyclerView.AdapterDataObserver

    // Lazy Inject ViewModel
    private val myViewModel: NoteViewModel by activityViewModels()

    private lateinit var mutableNoteList: MutableList<NoteItem>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

 */
}

@Composable
fun NoteListUI(myViewModel: NoteViewModel = viewModel(),onNavigate: (NavDirections) -> Unit) {
    val notes: List<NoteItem> by myViewModel.allNotes.observeAsState(listOf())
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(yellow50),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(notes) { note ->
            NoteItemUI(
                note,
                {
                    val directions =
                        MainContentFragmentDirections.actionMainContentToNoteData(note.id)
                    onNavigate(directions)
                },
                {}
            )
        }
        item {
            Spacer(modifier = Modifier.size(80.dp))
        }
    }
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End
    ) {
        FloatingActionButton(
            onClick = {
                val directions =
                    MainContentFragmentDirections.actionMainContentToNoteData(0)
                onNavigate(directions)
            },
            shape = RoundedCornerShape(
                topStart = 16.dp
            ),
            backgroundColor = blue500
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New Note"
            )
        }
    }
}

@Composable
fun NoteItemUI (
    note: NoteItem,
    onClick: () -> Unit,
    onClickStart: () -> Unit
    ) {
    //val creationDate = if (note.creation_date != null) (SimpleDateFormat.getDateInstance().format(note.creation_date.time)) else ""
    val creationDate = SimpleDateFormat.getDateInstance().format(note.creation_date!!.time)
    Card(
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .padding(6.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = 4.dp,
        backgroundColor = yellow100
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.wrapContentHeight(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    style = MaterialTheme.typography.h6,
                    text = note.title
                )
                Text(
                    style = MaterialTheme.typography.body1,
                    text = creationDate
                )
                Text(
                    style = MaterialTheme.typography.body2,
                    text = note.description
                )
            }
            TextButton(
                border = BorderStroke(1.dp, special400),
                shape = MaterialTheme.shapes.small,
                onClick = onClickStart
            ) {
                Text(
                    color = special400,
                    text = "Start"
                )
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    tint = special400,
                    contentDescription = "Play"
                )
            }
        }
    }
}

@Preview
@Composable
fun NoteItemUITest() {
    val note = NoteItem(
        id = 1,
        title = "Title",
        description = "Description",
        order = 1,
        last_edited_on = Calendar.getInstance(),
        creation_date = Calendar.getInstance()
    )
    NoteItemUI(note,{},{})
}