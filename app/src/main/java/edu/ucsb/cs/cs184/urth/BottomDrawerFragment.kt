package edu.ucsb.cs.cs184.urth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomDrawerFragment: BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_drawer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val newsButton: Button = view.findViewById(R.id.drawer_news_button)

        newsButton.setOnClickListener {
            Toast.makeText(context, "Finding news articles...", Toast.LENGTH_SHORT).show()
            (parentFragment as SearchFragment).transitionToNewsArticles()
        }
    }
}