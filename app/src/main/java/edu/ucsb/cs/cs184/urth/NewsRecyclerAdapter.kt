package edu.ucsb.cs.cs184.urth

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class NewsRecyclerAdapter: RecyclerView.Adapter<NewsRecyclerAdapter.ViewHolder>() {
    private var headlines: ArrayList<String> = arrayListOf()
    private var dates: ArrayList<String> = arrayListOf()
    private var publishers: ArrayList<String> = arrayListOf()
    private var imageURLs: ArrayList<String> = arrayListOf()
    private var newsURLs: ArrayList<String> = arrayListOf()

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var headlineView: TextView
        var dateView: TextView
        var publisherView: TextView
        var newsImageView: ImageView

        init {
            headlineView = itemView.findViewById(R.id.item_title)
            dateView = itemView.findViewById(R.id.item_date)
            publisherView = itemView.findViewById(R.id.item_publisher)
            newsImageView = itemView.findViewById(R.id.item_image)

            itemView.setOnClickListener {
                var position = adapterPosition
                val browser: Intent = Intent(Intent.ACTION_VIEW, Uri.parse(newsURLs[position]))
                it.context.startActivity(browser)
                Log.d("request", "attempt to open browser")
            }
        }
    }

    fun setContent(headlinesArr: ArrayList<String>, datesArr: ArrayList<String>,
                   publisherArr: ArrayList<String>, imageUrlArr: ArrayList<String>,
                   newsUrlArr: ArrayList<String>) {
        headlines = headlinesArr
        dates = datesArr
        publishers = publisherArr
        imageURLs = imageUrlArr
        newsURLs = newsUrlArr
//        headlines.add(str)
//        dates.add(str)
//        publishers.add(str)
//        imageURLs.add("https://s1.reutersmedia.net/resources_v2/images/rcom-default.png?w=800")
//        newsURLs.add(str)
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