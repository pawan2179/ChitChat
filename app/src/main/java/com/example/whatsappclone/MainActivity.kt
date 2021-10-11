package com.example.whatsappclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.whatsappclone.adapters.ScreenSliderAdapter
import com.example.whatsappclone.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding.toolbar)
        binding.viewPager.adapter = ScreenSliderAdapter(this)
        TabLayoutMediator(binding.tabs, binding.viewPager,
            TabLayoutMediator.TabConfigurationStrategy{ tab: TabLayout.Tab, position: Int ->
                when(position) {
                    0-> tab.text = "Chats"
                    else ->tab.text = "Contacts"
                }
        }).attach()

    }
}