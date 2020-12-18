package edu.ucsb.cs.cs184.urth.adapter

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.view.View
import android.view.ViewGroup

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import edu.ucsb.cs.cs184.urth.R

class NewsRecyclerAdapter : RecyclerView.Adapter<NewsRecyclerAdapter.ViewHolder>() {

    companion object {
        private val TAG = NewsRecyclerAdapter::class.simpleName
    }

    private var headlines: ArrayList<String> = arrayListOf()
    private var dates: ArrayList<String> = arrayListOf()
    private var publishers: ArrayList<String> = arrayListOf()
    private var imageURLs: ArrayList<String> = arrayListOf()
    private var newsURLs: ArrayList<String> = arrayListOf()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var headlineView: TextView = itemView.findViewById(R.id.item_title)
        var dateView: TextView = itemView.findViewById(R.id.item_date)
        var publisherView: TextView = itemView.findViewById(R.id.item_publisher)
        var newsImageView: ImageView = itemView.findViewById(R.id.item_image)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                val browser = Intent(Intent.ACTION_VIEW, Uri.parse(newsURLs[position]))
                it.context.startActivity(browser)
                Log.d(TAG, "Attempting to open browser...")
            }
        }
    }

    fun setContent(
        headlinesArr: ArrayList<String>, datesArr: ArrayList<String>,
        publisherArr: ArrayList<String>, imageUrlArr: ArrayList<String>,
        newsUrlArr: ArrayList<String>
    ) {
        headlines = headlinesArr
        dates = datesArr
        publishers = publisherArr
        imageURLs = imageUrlArr
        newsURLs = newsUrlArr
    }

    // creates a view holder via inflating our view
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        // creates a view from our layout
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.news_card, viewGroup, false)
        return ViewHolder(v) // pass the view created to view holder
    }

    // binds the view to the view holder
    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        viewHolder.headlineView.text = headlines[i]
        viewHolder.dateView.text = dates[i]
        viewHolder.publisherView.text = publishers[i]
        Glide.with(viewHolder.itemView.context)
            .load(imageURLs[i])
            .centerCrop()
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(viewHolder.newsImageView)
    }

    override fun getItemCount(): Int {
        return headlines.size
    }
}
