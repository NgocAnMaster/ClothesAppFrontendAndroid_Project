package com.master.myapplication2

import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar

class EditImageFragment : Fragment() {

    private lateinit var imageView: ImageView
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_image, container, false)

        imageView = view.findViewById(R.id.editImageView)

        val uri = arguments?.getParcelable<Uri>("imageUri")
        uri?.let { imageView.setImageURI(it) }

//        // get Uri passed from CreateFragment
//        arguments?.getParcelable<Uri>("imageUri")?.let {
//            imageUri = it
//            imageView.setImageURI(it)
//        }

        val topAppBar: MaterialToolbar = view.findViewById(R.id.topAppBar)
        topAppBar.setNavigationOnClickListener {
            findNavController().popBackStack() // back to Create tab
        }
        topAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_camera -> {
                    // reopen camera: Navigate back to CreateFragment's camera flow
                    findNavController().previousBackStackEntry
                        ?.savedStateHandle?.set("reopenCamera", true)
                    findNavController().popBackStack()
                    true
                }
                R.id.action_done -> {
                    // Navigate to Products page (placeholder destination)
                    findNavController().navigate(R.id.action_editImage_to_products)
                    true
                }
                else -> false
            }
        }

        // Tool buttons (currently stubs)
        view.findViewById<ImageButton>(R.id.btnCrop).setOnClickListener {
            // TODO: implement crop
        }
        view.findViewById<ImageButton>(R.id.btnFilter).setOnClickListener {
            // TODO: implement filters
        }
        view.findViewById<ImageButton>(R.id.btnPen).setOnClickListener {
            // TODO: implement drawing
        }
        view.findViewById<ImageButton>(R.id.btnSticker).setOnClickListener {
            // TODO: implement stickers
        }
        view.findViewById<ImageButton>(R.id.btnUndo).setOnClickListener {
            // TODO: implement undo
        }
        view.findViewById<ImageButton>(R.id.btnRedo).setOnClickListener {
            // TODO: implement redo
        }

        return view
    }
}
