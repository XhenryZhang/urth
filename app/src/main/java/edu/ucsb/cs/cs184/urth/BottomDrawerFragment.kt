package edu.ucsb.cs.cs184.urth

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomDrawerFragment: BottomSheetDialogFragment() {
    var activityCallback: NavigationListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_drawer, container, false)
    }

    interface NavigationListener {
        fun performNewsQuery()
        fun getLocation(): String
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // access interface methods defined in SearchFragment
        activityCallback = parentFragment as NavigationListener

        // button to perform news query
        val newsButton: Button = view.findViewById(R.id.drawer_news_button)
        val textView: TextView = view.findViewById(R.id.drawer_text)
        textView.text = activityCallback?.getLocation().toString()

        newsButton.setOnClickListener {
            Toast.makeText(context, "Finding news articles...", Toast.LENGTH_SHORT).show()
            activityCallback?.performNewsQuery()
        }
    }
}