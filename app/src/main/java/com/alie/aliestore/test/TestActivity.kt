package com.alie.aliestore.test

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.alie.aliestore.R
import com.alie.aliestore.databinding.ActivityMainBinding
import com.alie.aliestore.databinding.ActivityTestBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TestActivity : AppCompatActivity() {

    private val testViewModel by viewModels<TestViewModel>()

    private var binding:ActivityTestBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ActivityTestBinding.inflate(layoutInflater).let {
            binding = it
            binding?.root
        })
        initListener()
    }

    private fun initListener() {
        binding?.btn1?.setOnClickListener {
            testViewModel.syncData()
        }
    }


}