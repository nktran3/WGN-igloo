package com.example.wgn_igloo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.wgn_igloo.auth.LoginActivity
import com.example.wgn_igloo.auth.SignUpActivity
import com.example.wgn_igloo.database.FirestoreHelper
import com.example.wgn_igloo.databinding.ActivityMainBinding
import com.example.wgn_igloo.grocery.ShoppingListPage
import com.example.wgn_igloo.home.HomePage
import com.example.wgn_igloo.inbox.InboxPage
import com.example.wgn_igloo.account.AccountPage
import com.example.wgn_igloo.recipe.RecipeDetailsFragment
import com.example.wgn_igloo.recipe.RecipeSearchFragment
import com.example.wgn_igloo.recipe.RecipeViewModel
import com.example.wgn_igloo.recipe.RecipesPage
import com.firebase.ui.auth.AuthUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var firestoreHelper: FirestoreHelper
    private lateinit var viewModel: RecipeViewModel

    // Fragments
    val recipesPage by lazy { RecipesPage() }
    private val shoppingListPage by lazy { ShoppingListPage() }
    private val homePage by lazy { HomePage() }
    private val inboxPage by lazy { InboxPage() }
    private val accountPage by lazy { AccountPage() }


    // Current active fragment
    private var activeFragment: Fragment = homePage

    // Feature fragments
    private var recipeSearchFragment: RecipeSearchFragment? = null
    private var recipeDetailsFragment: RecipeDetailsFragment? = null

    // Firebase Authentication
    private lateinit var auth: FirebaseAuth

    // Tag of the active fragment
    private var activeFragmentTag: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val callback = object : OnBackPressedCallback(true ) {
            override fun handleOnBackPressed() {
                var currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                // If current frag is recipe search set it to null and
                // make sure to set the viewModel data to null to prevent zombie
                Log.d(TAG, "Current Frag: $currentFragment")
                if (currentFragment is RecipeDetailsFragment) {
                    Log.d(TAG, "Stepping out RecipeDetailPage")
                    recipeDetailsFragment = null
                    viewModel.currentRecipeDetailsFragment.value = null
                    recipeSearchFragment?.let { switchFragments(it) }
                }
                else if (currentFragment is RecipeSearchFragment) {
                    Log.d(TAG, "Stepping out RecipeSearchPage")
                    recipeSearchFragment = null
                    viewModel.currentRecipeSearchFragment.value = null
                    switchFragments(recipesPage)
                } else {
                    Log.d(TAG, "In recipe page")
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)



        bottomNavigation = binding.bottomNavigation
        bottomNavigation.selectedItemId = R.id.home_nav
        firestoreHelper = FirestoreHelper(this)

        // Initialize Firebase Auth
        auth = Firebase.auth


        // Initialize listeners to update the variable holding the state of the fragments
        viewModel = ViewModelProvider(this).get(RecipeViewModel::class.java)
        viewModel.currentRecipeSearchFragment.observe(this, Observer { fragment ->
            recipeSearchFragment = fragment as RecipeSearchFragment?
        })
        viewModel.currentRecipeDetailsFragment.observe(this, Observer { fragment ->
            recipeDetailsFragment = fragment as RecipeDetailsFragment?
        })

        setupFragments()
        setupBottomNavigation()

        // Restore the active fragment in the case of config change
        if (savedInstanceState != null) {
            activeFragmentTag = savedInstanceState.getString("activeFragmentTag", "")
            Log.d(TAG, activeFragmentTag)

            // Restore the active fragment based on the saved tag
            val restoredFragment = supportFragmentManager.findFragmentByTag(activeFragmentTag)
            Log.d(TAG, "$restoredFragment")
            if (restoredFragment != null) {
                switchFragments(restoredFragment)
            }
        }
    }

    // Function used to set up all fragments with tags and hide all except for the home page fragment
    private fun setupFragments() {
        supportFragmentManager.beginTransaction().apply {
            add(R.id.fragment_container, recipesPage, "recipes").hide(recipesPage)
            add(R.id.fragment_container, shoppingListPage, "shoppingList").hide(shoppingListPage)
            add(R.id.fragment_container, inboxPage, "inbox").hide(inboxPage)
            add(R.id.fragment_container, accountPage, "profile").hide(accountPage)
            add(R.id.fragment_container, homePage, "home").hide(homePage)
        }.commit()
    }


    // Function used to set up the bottom navigation bar to switch between fragments
    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            val fragmentToShow = when (item.itemId) {
                R.id.recipe_nav -> {
                    if (recipeDetailsFragment == null) {
                        Log.d(TAG, "recipeDetailsFragment is null")
                    }
                    if (recipeSearchFragment == null) {
                        Log.d(TAG, "recipeSearchFragment is null")
                    }
                    recipeDetailsFragment ?: recipeSearchFragment ?: recipesPage
                }
                R.id.shopping_list_nav -> shoppingListPage
                R.id.home_nav -> homePage
                R.id.inbox_nav -> inboxPage
                R.id.account_nav -> accountPage
                else -> return@setOnItemSelectedListener false
            }

            switchFragments(fragmentToShow)
            true
        }
    }

    // Function used to perform fragment transactions
    fun switchFragments(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        supportFragmentManager.fragments.forEach {
            transaction.hide(it)
        }
        if (!fragment.isAdded) {
            transaction.add(R.id.fragment_container, fragment)
        }
        transaction.show(fragment)
        // Add every fragment change to the back stack unless it's a main navigation item
        if (fragment !in arrayOf(homePage, shoppingListPage, inboxPage, accountPage, recipesPage)) {
            transaction.addToBackStack(null)
        }
        transaction.commit()
        activeFragment = fragment
        Log.d(TAG, "Fragment switched to: ${fragment.javaClass.simpleName}")
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("activeFragmentTag", activeFragmentTag)
    }

}