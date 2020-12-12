package edu.ucsb.cs.cs184.urth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        return inflater.inflate(R.layout.fragment_news, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val newsRecyclerView: RecyclerView = view.findViewById(R.id.newsRecyclerView)

        // view model to store data from the API
        viewModel = ViewModelProvider(activity as ViewModelStoreOwner).get(NewsViewModel::class.java)

        viewModel.news.observe(viewLifecycleOwner, Observer {
            layoutManager = LinearLayoutManager(context)
            newsRecyclerView.layoutManager = layoutManager
            val nra: NewsRecyclerAdapter = NewsRecyclerAdapter()

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

            nra.setContent(headlines, dates, publishers, imageURLs, newsURLs) // adds info about each news article to our adapter
            adapter = nra
            newsRecyclerView.adapter = adapter
        })

//        view.findViewById<Button>(R.id.button_second).setOnClickListener {
//            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
//        }
    }
}