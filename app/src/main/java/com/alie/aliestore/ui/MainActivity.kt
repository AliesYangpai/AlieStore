package com.alie.aliestore.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.alie.aliestore.databinding.ActivityMainBinding
import com.alie.aliestore.test.TestActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var binding:ActivityMainBinding? = null
    private val mainViewModel by viewModels<MainViewModel>()

    private val downloadUrl = "https://imtt.dd.qq.com/16891/apk/86C93A138C0B939A7D594D07B9D6AD1B.apk?fsname=com.moji.mjweather_7.0911.02_7091102.apk"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ActivityMainBinding.inflate(layoutInflater).let {
            binding = it
            binding?.root
        })

        binding?.btn1?.setOnClickListener {
            mainViewModel.fetchAppDetail(this.lifecycleScope)
        }
//        startActivity(Intent(this,TestActivity::class.java))

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.tipUiState.collectLatest {
                    when(it.isSuccess) {
                        true-> binding?.tv1?.text = it.data?.name
                        else-> binding?.tv1?.text = it.msg
                    }
                }
            }
        }

        binding?.btnDownload?.setOnClickListener {
            mainViewModel.downloadApk(downloadUrl)
        }
        binding?.btnDownload2?.setOnClickListener {
            mainViewModel.downloadApkInRange(downloadUrl)
        }
    }

}