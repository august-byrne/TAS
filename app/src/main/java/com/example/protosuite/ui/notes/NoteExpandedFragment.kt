package com.example.protosuite.ui.notes

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.example.protosuite.data.db.entities.DataItem
import com.example.protosuite.data.db.entities.NoteItem
import com.example.protosuite.ui.values.blue200
import com.example.protosuite.ui.values.blue500
import com.example.protosuite.ui.values.yellow100
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class NoteExpandedFragment: Fragment() {
/*
    private var _binding: NoteDataBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var arguments: MainContentFragmentArgs

    private val myViewModel: NoteViewModel by activityViewModels()

    private var deleteNote: Boolean = false

    private val noteDataListCopy: MutableList<DataItem> = mutableListOf()
    private val noteDataInitialList: MutableList<DataItem> = mutableListOf()

    private lateinit var lazyNote: NoteItem

    private lateinit var toolbarTextBoxLayout: TextInputLayout
    private lateinit var toolbarTextBox: EditText

    private lateinit var headerAdapter: RecyclerViewHeaderAdapter
    private lateinit var concatAdapter: ConcatAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        //get the arguments passed from the previous fragment
        arguments = MainContentFragmentArgs.fromBundle(requireArguments())

        //binding = DataBindingUtil.inflate(inflater, R.layout.note_data, container, false)
        _binding = NoteDataBinding.inflate(inflater, container, false)
        val view = binding.root

        //binding.noteViewModel = myViewModel

        binding.noteDataRecyclerView.setHasFixedSize(true)
        //TODO: Make non-cached NoteDataAdapter List draggable

        //lock and hide the navigation drawer, so no accidental swipe opening from this fragment!
        val drawerLayout = requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        //show the toolbar text box for the note title, and initialize it for use
        toolbarTextBoxLayout = requireActivity().findViewById(R.id.note_text_box_activity_main)
        toolbarTextBoxLayout.apply {
            //visible but fully transparent during the animation.
            alpha = 0f
            visibility = View.VISIBLE
            // Animate the content view to 100% opacity, and clear any animation
            // listener set on the view.
            animate().alpha(1f)
                .setDuration(resources.getInteger(R.integer.reply_motion_duration_large).toLong())
                .setListener(null)
        }
        toolbarTextBox = requireActivity().findViewById(R.id.note_title_activity_main)

        val noteItemAdapter = NoteDataAdapter(DataListener { itemId, itemView ->
            //myViewModel.onNoteClicked(itemId, view)
            //TODO: Implement timer here


        }, noteDataListCopy)

        //checking to see if we are loading a note or making a new one
        if (arguments.noteId != 0) {
            myViewModel.getNoteWithItemsById(arguments.noteId).observe(viewLifecycleOwner, {
                it?.let {
                    lazyNote =
                        it.note   //this is fine because "it" is now made up of vals, so it doesn't mess with us later
                    toolbarTextBox.setText(it.note.title)
                    headerAdapter =
                        RecyclerViewHeaderAdapter(it.note) //use lazynote only after initializing it
                    noteDataListCopy.clear()
                    noteDataListCopy.addAll(it.dataItems.toMutableList())
                    noteDataInitialList.clear()
                    noteDataInitialList.addAll(it.dataItems.toMutableList())
                    //myViewModel.refreshNoteDataList(it.dataItems)
                    noteItemAdapter.submitList(noteDataListCopy)
                    concatAdapter =
                        ConcatAdapter(headerAdapter, noteItemAdapter, RecyclerViewFooterAdapter())
                    binding.noteDataRecyclerView.adapter = concatAdapter
                }
            })
        } else {
            Log.d("DB_Save", "size of list is ${myViewModel.noteListSize}")
            lazyNote = NoteItem(0, Calendar.getInstance(), null, myViewModel.noteListSize, "", "")
            toolbarTextBox.setText("")
            headerAdapter =
                RecyclerViewHeaderAdapter(lazyNote) //use lazynote only after initializing it
            noteItemAdapter.submitList(noteDataListCopy)
            concatAdapter =
                ConcatAdapter(headerAdapter, noteItemAdapter, RecyclerViewFooterAdapter())
            binding.noteDataRecyclerView.adapter = concatAdapter
        }

        binding.noteDataListFab.setOnClickListener {
            noteDataListCopy.add(DataItem(0, lazyNote.id, 0, "", 0, 0))
            noteItemAdapter.submitList(noteDataListCopy)
            noteItemAdapter.onItemInserted(noteDataListCopy.size + 1) //(noteItemAdapter.itemCount+1)
        }

        sharedElementReturnTransition = Slide() //or can use Fade()

        setHasOptionsMenu(true)

        return view
    }

    override fun onDestroyView() {
        if (deleteNote) {
            myViewModel.deleteNote(arguments.noteId)
        } else {
            upsertNote()
        }
        requireActivity().findViewById<MaterialToolbar>(R.id.toolbar).setOnClickListener(null)
        super.onDestroyView()
        _binding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.my_nav_host_fragment
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
            scrimColor = android.graphics.Color.TRANSPARENT
            //setAllContainerColors(requireContext().themeColor(R.attr.colorSurface))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.note_bar_menu, menu)

        val toolbar: MaterialToolbar = requireActivity().findViewById(R.id.toolbar)
        //remove the delete note option if we didn't load a previous note
        if (arguments.noteId == 0) {
            toolbar.menu.removeItem(toolbar.menu.findItem(R.id.delete_note).itemId)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.settings -> {
                // Handle settings icon press
                true
            }
            R.id.share -> {
                // Handle contact icon press
                true
            }
            R.id.delete_note -> {
                // Handle delete note item press
                deleteNote = true
                findNavController().navigateUp()
                Toast.makeText(context, "Note ${arguments.noteId} was deleted", Toast.LENGTH_LONG)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun upsertNote() {
        val newTitle = toolbarTextBox.text.toString()
        val newDescription = headerAdapter.MyCustomEditTextListener().getItem().description

        if (newTitle.isEmpty() && newDescription.isEmpty() && noteDataListCopy.isEmpty()) {
            if (arguments.noteId == 0) {
                Toast.makeText(context, "Didn't save blank note", Toast.LENGTH_SHORT).show()
            } else {
                myViewModel.deleteNote(arguments.noteId)
                Toast.makeText(context, "Deleted blank note", Toast.LENGTH_SHORT).show()
            }
        } else if (newTitle != lazyNote.title || newDescription != lazyNote.description || noteDataListCopy != noteDataInitialList) {
            val note = lazyNote.copy(
                id = lazyNote.id,
                creation_date = lazyNote.creation_date,
                last_edited_on = Calendar.getInstance(),
                order = lazyNote.order,
                title = newTitle,
                description = newDescription
            )
            myViewModel.upsertNoteAndData(note, noteDataListCopy)
        }
    }

 */
}

// about 150 lines of compose code
// vs
// 250 (NoteExpandedFragment.kt) - 50 (imports) + 100 (data_item.xml) + 50 (note_data.xml) + 50 (note_data_header.xml) + 200 (NoteDataAdapter.kt)
// = 600 for old fragment xml ui design
@Composable
fun ExpandedNoteUI (id: Int, myViewModel: NoteViewModel) {//, onNavigate: (NavDirections) -> Unit) {
    val note by myViewModel.getNoteWithItemsById(id).observeAsState()
    val listState = rememberLazyListState()
    val lastEdited = if (note?.note?.last_edited_on?.time != null) {
        myViewModel.simpleDateFormat.format(note!!.note.last_edited_on!!.time)
    } else {
        "never"
    }
    if (!myViewModel.beginTyping) {
        myViewModel.currentNoteTitle =
            note?.note?.title ?: ""
        myViewModel.currentNoteDescription =
            note?.note?.description ?: ""
    }
    DisposableEffect(key1 = myViewModel) {
        onDispose {
            Log.d("Side Effect Tracker", "navigating away from ExpandedNoteUI")
            if (myViewModel.beginTyping) {
                myViewModel.beginTyping = false
                myViewModel.upsert(
                    note?.note?.copy(
                        last_edited_on = Calendar.getInstance(),
                        title = myViewModel.currentNoteTitle,
                        description = myViewModel.currentNoteDescription
                    ) ?: NoteItem(
                        id = 0,
                        creation_date = Calendar.getInstance(),
                        last_edited_on = Calendar.getInstance(),
                        order = 0,
                        title = myViewModel.currentNoteTitle,
                        description = myViewModel.currentNoteDescription
                    )
                )
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(yellow100),
        state = listState,
        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            BasicTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(blue200)
                    .padding(8.dp),
                textStyle = MaterialTheme.typography.body1,
                maxLines = 4,
                value = myViewModel.currentNoteDescription,
                onValueChange = { newValue ->
                    myViewModel.beginTyping = true
                    myViewModel.currentNoteDescription = newValue
                    Log.d("textError", "Desc String: "+myViewModel.currentNoteDescription)
                },
                decorationBox = { innerTextField ->
                    if (myViewModel.currentNoteDescription.isEmpty()) {
                        Text(
                            text = "Description",
                            color = Color.Gray,
                            style = MaterialTheme.typography.body1
                        )
                    }
                    innerTextField()
                }
            )
            Text(
                text = "last edited: $lastEdited",
                modifier = Modifier
                    .fillMaxWidth()
                    .background(blue200)
                    .padding(2.dp),
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.body2
            )
        }

        note?.let {
            items(it.dataItems) { dataItem ->
                DataItemUI(
                    dataItem
                ) {
                    //open timer for dataItem.parent_id
                    //begin at dataItem.order
                }
                Divider()
            }
        }
        item {
            Spacer(modifier = Modifier.size(80.dp))
        }
    }
    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FloatingActionButton(
            onClick = {
                //noteDataListCopy.add(DataItem(0, lazyNote.id, 0, "", 0, 0))
            },
            shape = CircleShape,
            backgroundColor = blue500
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New Item"
            )
        }
    }
}

@Composable
fun DataItemUI (
    dataItem: DataItem,
    onClickStart: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val iconResource = if (expanded) {
        Icons.Default.ArrowDropUp
    } else {
        Icons.Default.ArrowDropDown
    }
    val timeTypeName: String =
        when (dataItem.unit) {
            1 -> "sec"
            2 -> "min"
            3 -> "hr"
            else -> "min"
        }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(yellow100),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        OutlinedTextField(
            label = {Text("Activity")},
            modifier = Modifier
                .weight(weight = 0.5F)
                .padding(top = 2.dp, bottom = 8.dp, start = 2.dp, end = 2.dp),
            textStyle = MaterialTheme.typography.body1,
            singleLine = true,
            value = dataItem.activity,
            onValueChange = { newValue ->
                //dataItem.activity = newValue
            }
        )
        OutlinedTextField(
            label = {Text("Time")},
            modifier = Modifier
                .weight(weight = 0.25F)
                .padding(top = 2.dp, bottom = 8.dp, end = 2.dp),
            textStyle = MaterialTheme.typography.body1,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            value = dataItem.time.toString(),
            onValueChange = { newValue ->
                //dataItem.time = newValue
            }
        )
        Box(
            modifier = Modifier
                .padding(start = 4.dp)
                .weight(weight = 0.12F)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = timeTypeName,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .clickable(onClick = { expanded = true })
                )
                Icon(
                    modifier = Modifier
                        .padding(start = 1.dp, top = 3.dp)
                        .clickable(onClick = { expanded = true }),
                    imageVector = iconResource,
                    contentDescription = "time increment selector"
                )
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(onClick = { /*TODO*/ }) {
                    Text(text = "seconds")
                }
                DropdownMenuItem(onClick = { /*TODO*/ }) {
                    Text(text = "minutes")
                }
                DropdownMenuItem(onClick = { /*TODO*/ }) {
                    Text(text = "hours")
                }
            }
        }
        IconButton(
            modifier = Modifier.weight(weight = 0.13F),
            onClick = onClickStart
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                tint = Color.Green,
                contentDescription = "Play"
            )
        }
    }
}

@Preview
@Composable
fun DataItemUITest() {
    val dataItem = DataItem(
        id = 1,
        activity = "Activity",
        parent_id = 1,
        order = 1,
        time = 12,
        unit = 1
    )
    DataItemUI(dataItem,{})
}