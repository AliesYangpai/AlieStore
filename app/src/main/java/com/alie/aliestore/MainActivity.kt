package com.alie.aliestore

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.alie.aliestore.databinding.ActivityMainBinding
import com.alie.aliestore.test.TestActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var binding:ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ActivityMainBinding.inflate(layoutInflater).let {
            binding = it
            binding?.root
        })
        startActivity(Intent(this,TestActivity::class.java))
    }

}