package com.example.puffy.myapplication.auth.login

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.example.puffy.myapplication.R
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.main_activity.*


class LoginFragment : Fragment() {
    private lateinit var viewModel : LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //from MainActivity
        var statusOnlineTextView = requireActivity().statusOnline
        var statusOfflineTextView = requireActivity().statusOffline
        var statusOnlineImageView = requireActivity().statusImageOnline
        var statusOfflineImageView = requireActivity().statusImageOffline
        val handler = Handler(Looper.getMainLooper())
        handler.post(Runnable {
            // UI code goes here
            statusOfflineTextView.visibility = View.INVISIBLE
            statusOfflineImageView.visibility = View.INVISIBLE
            statusOnlineTextView.visibility = View.INVISIBLE
            statusOnlineImageView.visibility = View.INVISIBLE
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        setupViewModel()
    }

    private fun setupViewModel(){
        viewModel.checkToken()

        viewModel.loginResult.observe(viewLifecycleOwner) {
            loading.visibility = View.GONE
            findNavController().navigate(R.id.fragment_meal_list)
        }

        viewModel.loginError.observe(viewLifecycleOwner) {
            if(it != null){
                loading.visibility = View.GONE
                Toast.makeText(activity, it.message, Toast.LENGTH_LONG).show()
            }else{
                viewModel.login(email.text.toString(), password.text.toString())
            }
        }

        loginBtn.setOnClickListener{
            loading.visibility = View.VISIBLE
            viewModel.loginDataChanged(email.text.toString(), password.text.toString())
        }
    }
}

