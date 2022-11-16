package com.example.inventory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.inventory.databinding.FragmentSettingBinding


class SettingFragment: Fragment() {
    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val masterKeyAlias = MasterKey.Builder(requireContext()).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()

        val sharedPreferences = EncryptedSharedPreferences.create(
            requireContext(),
            "Setting",
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        binding.apply {
            defaultProviderName.setText(sharedPreferences.getString("ProviderName",""))
            defaultProviderEmail.setText(sharedPreferences.getString("ProviderEmail",""))
            defaultProviderPhone.setText(sharedPreferences.getString("ProviderPhone",""))
            switchDefault.isChecked  = sharedPreferences.getBoolean("SwitchDefault",false)
            switchHide.isChecked = sharedPreferences.getBoolean("SwitchHide",false)
            switchForbid.isChecked = sharedPreferences.getBoolean("SwitchForbid",false)

            saveAction.setOnClickListener {
                sharedPreferences.edit().apply {
                    putString("ProviderName", defaultProviderName.text.toString())
                    putString("ProviderEmail", defaultProviderEmail.text.toString())
                    putString("ProviderPhone", defaultProviderPhone.text.toString())
                    putBoolean("SwitchDefault", switchDefault.isChecked)
                    putBoolean("SwitchHide", switchHide.isChecked)
                    putBoolean("SwitchForbid", switchForbid.isChecked)
                }.apply()

                val action = SettingFragmentDirections.actionSettingFragmentToItemListFragment()
                findNavController().navigate(action)
            }
        }


    }

}