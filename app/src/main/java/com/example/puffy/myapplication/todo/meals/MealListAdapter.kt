package com.example.puffy.myapplication.todo.meals

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.puffy.myapplication.R
import com.example.puffy.myapplication.todo.data.Meal
import com.example.puffy.myapplication.todo.meal.MealEditFragment
import kotlinx.android.synthetic.main.view_meal_list.view.*
import java.util.concurrent.Executors

class MealListAdapter(private val fragment: Fragment) : RecyclerView.Adapter<MealListAdapter.ViewHolder>(){

    var items = arrayListOf<Meal>()
        set(value){
            field = value
            notifyDataSetChanged()
        }

    private lateinit var onItemClick : View.OnClickListener
    private val tagName: String = "MealListAdapter"
    private var searchBar: SearchView?
    private var progressBar: ProgressBar?

    init {
        //click on an item
        onItemClick = View.OnClickListener { view ->
            println("view tag ${view.tag}")
            val item = view.tag as Meal

            //navigate to item edit fragment
            fragment.findNavController().navigate(R.id.action_MealListFragment_to_MealEditFragment, Bundle().apply {
                putInt(MealEditFragment.ITEM_ID, item.id)
            })

        }
        searchBar = fragment.activity?.findViewById(R.id.searchBar)
        progressBar = fragment.activity?.findViewById(R.id.fetchProgress)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.imageView
        val textView: TextView = view.text
        val progressImage: ProgressBar = view.fetchImageProgress
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_meal_list, parent, false)
        Log.v(tagName, "onCreateViewHolder")
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.v(tagName, "onBindViewHolder $position")
        val item = items[position]
        holder.itemView.tag = item
        holder.imageView.setImageURI(null)
        holder.imageView.visibility = View.GONE
        holder.progressImage.visibility = View.VISIBLE
        if(item.pathImage?.isNotEmpty() == true){
            this.getImageFromUrl(holder.imageView, item.pathImage!!, holder.progressImage)
            holder.imageView.visibility = View.VISIBLE
        }
        holder.textView.text = item.toString()
        holder.itemView.setOnClickListener(onItemClick)

        searchBar?.bringToFront()
        progressBar?.bringToFront()
    }

    fun filterByText(text : String) : Boolean{
        var newItems : MutableList<Meal> = arrayListOf<Meal>()
        items.forEach { item ->
            if(item.category.contains(text)){
                newItems.add(item)
            }
        }
        if(newItems.size > 0){
            items = newItems as ArrayList<Meal>
            return true
        }
        return false
    }

    fun getImageFromUrl(imageView: ImageView, url: String, progressImage: ProgressBar) {
        val executor = Executors.newSingleThreadExecutor()

        // Once the executor parses the URL
        // and receives the image, handler will load it
        // in the ImageView
        val handler = android.os.Handler(Looper.getMainLooper())

        // Initializing the image
        var image: Bitmap? = null

        // Only for Background process (can take time depending on the Internet speed)
        executor.execute {

            // Image URL
            val imageURL = url
            // Tries to get the image and post it in the ImageView
            // with the help of Handler
            try {
                val `in` = java.net.URL(imageURL).openStream()
                image = BitmapFactory.decodeStream(`in`)

                // Only for making changes in UI
                handler.post {
                    imageView.setImageBitmap(image)
                    progressImage.visibility = View.GONE
                }
            }

            // If the URL doesnot point to
            // image or any other kind of failure
            catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}