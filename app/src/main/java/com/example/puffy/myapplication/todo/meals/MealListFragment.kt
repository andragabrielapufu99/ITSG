package com.example.puffy.myapplication.todo.meals

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.work.*
import com.example.puffy.myapplication.R
import com.example.puffy.myapplication.auth.data.AuthRepository
import com.example.puffy.myapplication.common.ConnectivityLiveData
import com.example.puffy.myapplication.common.EventType
import com.example.puffy.myapplication.common.MyWorker
import com.example.puffy.myapplication.todo.data.*
import kotlinx.android.synthetic.main.fragment_meal_list.*
import kotlinx.android.synthetic.main.main_activity.*
import org.json.JSONObject

class MealListFragment : Fragment() {

    private lateinit var adapter : MealListAdapter
    private lateinit var model : MealListViewModel
    private lateinit var connectivityManager : ConnectivityManager
    private lateinit var connectivityLiveData: ConnectivityLiveData
    private lateinit var statusOnlineTextView: TextView
    private lateinit var statusOfflineTextView: TextView
    private lateinit var statusOnlineImageView: ImageView
    private lateinit var statusOfflineImageView: ImageView
    private val tagName: String = "MealListFragment"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(tagName, "onCreate")

        //from MainActivity
        statusOnlineTextView = requireActivity().statusOnline
        statusOfflineTextView = requireActivity().statusOffline
        statusOnlineImageView = requireActivity().statusImageOnline
        statusOfflineImageView = requireActivity().statusImageOffline

        connectivityManager = activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityLiveData = ConnectivityLiveData(connectivityManager)
        connectivityLiveData.observe(this) {
            Log.d(tagName, "Network status : $it")
            model.setNetworkStatus(it)
            if(it){
                setOnline()
            }else{
                setOffline()
            }
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_meal_list, container, false)
    }

    val networkCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP) object: ConnectivityManager.NetworkCallback(){
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Log.d(tagName, "Available network")
            setOnline()
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onLost(network: Network) {
            super.onLost(network)
            Log.d(tagName, "Lost network")
            setOffline()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setOnline(){
        val handler = Handler(Looper.getMainLooper())
        handler.post(Runnable {
            // UI code goes here
            statusOfflineTextView.visibility = View.INVISIBLE
            statusOfflineImageView.visibility = View.INVISIBLE
            statusOnlineTextView.visibility = View.VISIBLE
            statusOnlineImageView.visibility = View.VISIBLE
        })
        model.setNetworkStatus(true)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setOffline(){
        val handler = Handler(Looper.getMainLooper())
        handler.post(Runnable {
            // UI code goes here
            statusOnlineTextView.visibility = View.INVISIBLE
            statusOnlineImageView.visibility = View.INVISIBLE
            statusOfflineTextView.visibility = View.VISIBLE
            statusOfflineImageView.visibility = View.VISIBLE
        })
        model.setNetworkStatus(false)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onStart() {
        super.onStart()
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onStop() {
        super.onStop()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ResourceAsColor")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.v(tagName, "onActivityCreated")
        if(!AuthRepository.isAuthenticated){
            findNavController().navigate(R.id.fragment_login)
            return
        }
        setupList()

        //add
        addBtn.setOnClickListener {
            Log.v(tagName, "Click on add button")
            findNavController().navigate(R.id.action_MealListFragment_to_MealEditFragment)
        }

        //logout
        logoutBtn.setOnClickListener {
            model.logout()
            findNavController().navigate(R.id.fragment_login)
        }

        //filter
        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                model.refreshLocal()
                adapter.items = model.items.value!! as ArrayList<Meal>
                if (newText != null) {
                    if (!adapter.filterByText(newText)) {
                        Toast.makeText(
                            activity,
                            "Nothing found... Back to default data",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                return false
            }

        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupList(){
        adapter = MealListAdapter(this)
        item_list.adapter = adapter
        model = ViewModelProvider(this).get(MealListViewModel::class.java)

        model.items.observe(viewLifecycleOwner) {
            Log.v(tagName, "update meals")
            adapter.items = it as ArrayList<Meal>
        }

        model.loading.observe(viewLifecycleOwner) {
            Log.i(tagName, "update loading")
            fetchProgress.visibility = if (it) View.VISIBLE else View.GONE
        }

        model.loadingError.observe(viewLifecycleOwner) {
            if (it != null) {
                Log.i(tagName, "update loading error")
                val message = "Loading exception ${it.message}"
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
                model.refreshLocal()
            }
        }

        model.networkStatus.observe(viewLifecycleOwner) {
            if(it){
                if(!MealRepository.needWorkers){
                    model.refresh()
                }else{
                    println("Start workers")
                    startWokers()
                }
            }else if(!it){
                model.refreshLocal()
                Toast.makeText(
                    activity,
                    "You're not connected to server. All operations will be done on local data and we will send as soon as we can to server.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v(tagName, "onDestroy")
    }

    fun startWokers(){
        MealRepository.addedLocal.forEach{ item -> startOneWorker(item, EventType.CREATED)}
        MealRepository.updatedLocal.forEach{ item -> startOneWorker(item, EventType.UPDATED)}
//        ItemRepository.itemsAddLocal = ArrayList()
//        ItemRepository.itemsUpdatedLocal = ArrayList()
    }

    @SuppressLint("RestrictedApi")
    fun startOneWorker(item: Meal, eventType: EventType){
        val jsonObj = JSONObject()
        jsonObj.put("eventType", eventType)

        val itemObj = JSONObject()
        itemObj.put("id", item.id)
        itemObj.put("category", item.category)
        itemObj.put("served_on", item.served_on)
        itemObj.put("created_at", item.created_at)
        itemObj.put("updated_at", item.updated_at)
        itemObj.put("foods", item.foods)
        jsonObj.put("item", itemObj)

        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build() //connectat
        val inputData = Data.Builder().put("data", jsonObj.toString()).build()
        val myWork = OneTimeWorkRequest.Builder(MyWorker::class.java)
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()
        val workId = myWork.id
        activity?.let {
            WorkManager.getInstance(it.applicationContext).apply {
                enqueue(myWork)
                getWorkInfoByIdLiveData(workId).observe(viewLifecycleOwner) { status ->
                    val isFinished : Boolean = status?.state?.isFinished == true
                    if(isFinished){

                    }
                    println("Status : $status")
                }
            }
        }

    }

}