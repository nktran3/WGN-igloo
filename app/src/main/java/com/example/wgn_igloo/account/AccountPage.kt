package com.example.wgn_igloo.account

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wgn_igloo.auth.LoginActivity
import com.example.wgn_igloo.R
import com.example.wgn_igloo.databinding.FragmentAccountPageBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.wgn_igloo.account.AccountItemAdapter.AccountItem
import com.google.firebase.firestore.ktx.firestore

private const val TAG = "ProfilePage"

class AccountPage : Fragment() {

    private lateinit var accountItemAdapter: AccountItemAdapter
    private lateinit var binding: FragmentAccountPageBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    // Preset list of account page items for RecyclerView
    private val data: List<AccountItem> = listOf(
        AccountItem(R.drawable.profile_icon, "Profile"),
        AccountItem(R.drawable.friends_icon, "Friends"),
        AccountItem(R.drawable.support_icon, "Support"),
        AccountItem(R.drawable.faq_icon, "FAQ"),
        AccountItem(R.drawable.setting_icon, "Settings"),
        AccountItem(R.drawable.about_icon, "About Us"),
        AccountItem(R.drawable.logout_icon, "Logout")

    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Firebase Auth
        auth = Firebase.auth
        // Initialize Google Sign-In options
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(com.firebase.ui.auth.R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso.
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAccountPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView and its layout manager
        val recyclerView: RecyclerView = view.findViewById(R.id.profile_recycler)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize and set the adapter for RecyclerView
        accountItemAdapter = AccountItemAdapter(data) { item ->

            // Handle click events on RecyclerView items
            when (item.text) {
                "Profile" -> showProfileDetails()

                "Friends" -> showFriends()

                "Support" -> openGoogleApp()

                "FAQ" -> showFAQpage()

                "Settings" -> showSettings()

                "About Us" -> showAbout()

                "Logout" -> {
                    signOut()
                    goToLoginActivity()
                    Log.d(TAG, "Signed out")
                }

            }
        }
        recyclerView.adapter = accountItemAdapter

        // Fetch and display the username
        fetchUsername()

    }

    // Fetches the username from Firestore and updates UI
    private fun fetchUsername() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.d(TAG, "No user logged in")
            // Possibly update the UI to reflect that no user is logged in
            updateUsernameOnUI(null, null)  // Show "Unknown User" or prompt login
            return
        }

        val userDocRef = Firebase.firestore.collection("users").document(userId)
        userDocRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {

                // Extract name and username from document snapshot
                val givenName = documentSnapshot.getString("givenName")
                val familyName = documentSnapshot.getString("familyName")
                val name = "$givenName $familyName"
                val username = documentSnapshot.getString("username")
                updateUsernameOnUI(username, name)
            } else {
                Log.d(TAG, "No such document")
                updateUsernameOnUI(null, null)  // Handle case where document is missing
            }
        }.addOnFailureListener { exception ->
            Log.d(TAG, "Error getting documents: ", exception)
            updateUsernameOnUI(null, null)  // Handle error case
        }
    }

    // Update user's name on ui
    private fun updateUsernameOnUI(username: String?, name: String?) {
        activity?.runOnUiThread {
            if (username == null) {
                binding.accountName.text = "Unknown User"
                Toast.makeText(context, "Please log in to view profile.", Toast.LENGTH_LONG).show()
            } else {
                binding.accountName.text = name ?: "Unknown User"
            }
        }
    }

    // Inflate friends fragment
    private fun showFriends() {
        val friendsFragment = FriendsFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, friendsFragment)
            .addToBackStack(null)
            .commit()
    }

    // Inflate profile fragment
    private fun showProfileDetails(){
        val profileFragment = ProfileFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, profileFragment)
            .addToBackStack(null)
            .commit()
    }

    // Inflate setting fragment
    private fun showSettings(){
        val settingsFragment = SettingsFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, settingsFragment)
            .addToBackStack(null)
            .commit()
    }

    // Inflate about us fragment
    private fun showAbout(){
        val aboutFragment = AboutFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, aboutFragment)
            .addToBackStack(null)
            .commit()
    }

    // Inflate FAQ fragment
    private fun showFAQpage(){
        val faqFragment = FaqFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, faqFragment)
            .addToBackStack(null)
            .commit()

    }

    // Opens the Google form for support
    private fun openGoogleApp() {
        try {
            // URL for the Google Form
            val formUrl = "https://docs.google.com/forms/d/e/1FAIpQLSci-UHF6PjLyTdzAEr_5TKxWdTbQQi8jx7Y8HsXbPrypzTmeQ/viewform"
            val gmmIntentUri = Uri.parse("googlechrome://navigate?url=$formUrl")
            val intent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            intent.setPackage("com.google.android.apps.chrome")
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback to opening the form in any available browser if Chrome is not installed
            val gmmIntentUri =
                Uri.parse("https://docs.google.com/forms/d/e/1FAIpQLSci-UHF6PjLyTdzAEr_5TKxWdTbQQi8jx7Y8HsXbPrypzTmeQ/viewform")
            val intent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            startActivity(intent)
        }
    }

    // Navigates to the login activity
    private fun goToLoginActivity() {
        val intent = Intent(activity, LoginActivity::class.java)
        // You can also put extra data to pass to the destination activity
        intent.putExtra("key", "value")
        startActivity(intent)
    }

    // Signs the user out
    private fun signOut() {
        // Firebase sign out
        FirebaseAuth.getInstance().signOut()

        // Google sign out
        googleSignInClient.signOut().addOnCompleteListener {
            // Handle sign out result
        }
    }



}


