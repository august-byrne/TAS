package com.example.protosuite.ui.notes

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.transition.Fade
import com.example.protosuite.R
import com.example.protosuite.adapters.*
import com.example.protosuite.data.db.entities.DataItem
import com.example.protosuite.data.db.entities.NoteItem
import com.example.protosuite.databinding.NoteDataBinding
import com.example.protosuite.ui.MainContentArgs
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.transition.MaterialContainerTransform
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.*


class NoteData: Fragment() {

    private var _binding: NoteDataBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var arguments: MainContentArgs

    private val myViewModel: NoteViewModel by sharedViewModel()

    //private val newNote: NoteItem = NoteItem(0, Calendar.getInstance(), null, 0, "", "")
    private var delete: Int = 0

    private lateinit var noteDataListCopy: MutableList<DataItem>

    private lateinit var lazyNote: NoteItem

    private lateinit var toolbarTextBoxLayout: TextInputLayout
    private lateinit var toolbarTextBox: EditText

    private lateinit var headerAdapter: RecyclerViewHeaderAdapter
    private lateinit var concatAdapter: ConcatAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //get the arguments passed from the previous fragment
        arguments = MainContentArgs.fromBundle(requireArguments())

        //binding = DataBindingUtil.inflate(inflater, R.layout.note_data, container, false)
        _binding = NoteDataBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.noteViewModel = myViewModel

        val footerAdapter = RecyclerViewFooterAdapter()

        binding.noteDataRecyclerView.setHasFixedSize(true)
        //binding.noteDataRecyclerView.setItemViewCacheSize(100)    //TODO: Get non-cached NoteDataAdapter List to be draggable

        //lock and hide the navigation drawer, so no accidental swipe opening from this fragment!
        val drawerLayout = requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        //show the toolbar text box for the note title, and initialize it for use
        toolbarTextBoxLayout = requireActivity().findViewById(R.id.note_text_box_activity_main)
        fadein()
        toolbarTextBox = requireActivity().findViewById(R.id.note_title_activity_main)

        //are we loading a note, or making a new one?
        if (arguments.noteId != 0) {
            myViewModel.getNoteWithItemsById(arguments.noteId).observe(viewLifecycleOwner, {
                it?.let {
                    lazyNote =
                        it.note   //this is fine because "it" is now made up of vals, so it doesn't mess with us later
                    toolbarTextBox.setText(it.note.title)
                    headerAdapter =
                        RecyclerViewHeaderAdapter(it.note) //use lazynote only after initializing it
                    noteDataListCopy = it.dataItems.toMutableList()
                    val noteItemAdapter = NoteDataAdapter(DataListener { itemId, view ->
                        myViewModel.onNoteClicked(itemId, view)
                    }, noteDataListCopy)
                    binding.noteDataListFab.setOnClickListener {
                        noteDataListCopy.add(DataItem(0, lazyNote.id, 0, "", 0, 0))
                        noteItemAdapter.submitList(noteDataListCopy)
                        noteItemAdapter.onItemInserted(noteDataListCopy.size + 1) //(noteItemAdapter.itemCount+1)
                    }
                    //myViewModel.refreshNoteDataList(it.dataItems)
                    noteItemAdapter.submitList(noteDataListCopy)
                    concatAdapter = ConcatAdapter(headerAdapter, noteItemAdapter, footerAdapter)
                    binding.noteDataRecyclerView.adapter = concatAdapter
                }
            })
        } else {
            Log.d("DB_Save", "size of list is ${myViewModel.noteListSize}")
            lazyNote = NoteItem(0, Calendar.getInstance(), null, myViewModel.noteListSize, "", "")
            toolbarTextBox.setText("")
            noteDataListCopy = mutableListOf()
            headerAdapter =
                RecyclerViewHeaderAdapter(lazyNote) //use lazynote only after initializing it
            val noteItemAdapter = NoteDataAdapter(DataListener { itemId, view ->
                myViewModel.onNoteClicked(itemId, view)
            }, noteDataListCopy)
            binding.noteDataListFab.setOnClickListener {
                noteDataListCopy.add(DataItem(0, lazyNote.id, 0, "", 0, 0))
                noteItemAdapter.submitList(noteDataListCopy)
                noteItemAdapter.onItemInserted(noteDataListCopy.size + 1) //(noteItemAdapter.itemCount+1)
            }
            concatAdapter = ConcatAdapter(
                headerAdapter,
                noteItemAdapter,
                footerAdapter
            )
            binding.noteDataRecyclerView.adapter = concatAdapter

            sharedElementReturnTransition = Fade()
        }

        setHasOptionsMenu(true)

        return view
    }

    override fun onDestroyView() {
        if (delete == 0) {
            upsertNote()
        } else {
            myViewModel.deleteNote(arguments.noteId)
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
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(requireContext().themeColor(R.attr.colorSurface))
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
                delete = 1
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
        Log.d(
            "testingTheAdapter",
            "Adapter says $newDescription and lazyNote says ${lazyNote.description}"
        )

        if (newTitle.isEmpty() && newDescription.isEmpty() && noteDataListCopy.isEmpty()) {
            if (MainContentArgs.fromBundle(requireArguments()).noteId == 0) {
                Toast.makeText(context, "Didn't save blank note", Toast.LENGTH_SHORT).show()
            } else {
                myViewModel.deleteNote(arguments.noteId)
                Toast.makeText(context, "Deleted blank note", Toast.LENGTH_SHORT).show()
            }
        } else {
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
        //TODO: upsert all of the note data items ----> DONE YOOOOOO HAHAHAHHA
    }

    private fun fadein() {
        toolbarTextBoxLayout.apply {
            // Set the content view to 0% opacity but visible, so that it is visible
            // (but fully transparent) during the animation.
            alpha = 0f
            visibility = View.VISIBLE

            // Animate the content view to 100% opacity, and clear any animation
            // listener set on the view.
            animate()
                .alpha(1f)
                .setDuration(resources.getInteger(R.integer.reply_motion_duration_large).toLong())
                .setListener(null)
        }
    }
}
