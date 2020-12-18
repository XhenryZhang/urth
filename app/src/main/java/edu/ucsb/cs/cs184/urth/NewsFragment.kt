package edu.ucsb.cs.cs184.urth

import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class NewsFragment : Fragment() {
    private lateinit var viewModel: NewsViewModel
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<NewsRecyclerAdapter.ViewHolder>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val newsRecyclerView: RecyclerView = view.findViewById(R.id.newsRecyclerView)

        // view model to store data from the API
        viewModel =
            ViewModelProvider(activity as ViewModelStoreOwner).get(NewsViewModel::class.java)

        viewModel.news.observe(viewLifecycleOwner, Observer {
            layoutManager = LinearLayoutManager(context)
            newsRecyclerView.layoutManager = layoutManager
            val nra = NewsRecyclerAdapter()

            val headlines: ArrayList<String> = arrayListOf()
            val dates: ArrayList<String> = arrayListOf()
            val publishers: ArrayList<String> = arrayListOf()
            val imageURLs: ArrayList<String> = arrayListOf()
            val newsURLs: ArrayList<String> = arrayListOf()

            for (newsInfo in it) {
                headlines.add(newsInfo!!.headline)
                dates.add(newsInfo!!.datePublished)
                publishers.add(newsInfo!!.publisher)
                imageURLs.add(newsInfo!!.imageUrl)
                newsURLs.add(newsInfo!!.url)
            }

            // hide loading spinner if articles returned
            if (it.isNotEmpty()) {
                val progress = requireActivity().findViewById<ProgressBar>(R.id.progressBar)
                progress.visibility = View.INVISIBLE
            }

            // empty message display after 3 seconds if no news returned
            val timer = object : CountDownTimer(3000, 1000) {
                override fun onTick(millisUntilFinished: Long) {}

                override fun onFinish() {
                    if (viewModel.getNews()?.isEmpty() == true) {
                        val oopsMsg = view.findViewById<TextView>(R.id.oopsTextView)
                        val emptyMsg = view.findViewById<TextView>(R.id.noNewsTextView)
                        val progress = view.findViewById<ProgressBar>(R.id.progressBar)
                        progress.visibility = View.INVISIBLE
                        oopsMsg.visibility = View.VISIBLE
                        emptyMsg.visibility = View.VISIBLE
                    }
                }
            }
            timer.start()

            nra.setContent(
                headlines,
                dates,
                publishers,
                imageURLs,
                newsURLs
            ) // adds info about each news article to our adapter
            adapter = nra
            newsRecyclerView.adapter = adapter
        })
    }
}
