package com.example.wgn_igloo

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.wgn_igloo.databinding.FragmentShoppingNewItemsFormBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*


class NewShoppingItemFormFragment : Fragment() {
    private var _binding: FragmentShoppingNewItemsFormBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firestoreHelper: FirestoreHelper
//    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    companion object {
        fun newInstance(): NewShoppingItemFormFragment {
            return NewShoppingItemFormFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestoreHelper = FirestoreHelper(requireContext())
    }

//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentShoppingNewItemsFormBinding.inflate(inflater, container, false)
//        return binding.root
//    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentShoppingNewItemsFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSpinners()
        setupSubmitButton()
    }

    private fun setupSpinners() {
        val categories = arrayOf("Choose an option", "Meat", "Vegetables", "Dairy", "Fruits", "Carbohydrates")
        val defaultSharedWith = arrayOf("No one")

        binding.categoryInput.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories)
        binding.sharedWithInput.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, defaultSharedWith)
        // Fetch and update the sharedWith spinner with dynamic data
        fetchFriendsAndUpdateSpinner()
    }

    private fun fetchFriendsAndUpdateSpinner() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).collection("friends")
                .get()
                .addOnSuccessListener { documents ->
                    val friendsUsernames = mutableListOf("Choose an option")
                    for (document in documents) {
                        document.getString("username")?.let { username ->
                            friendsUsernames.add(username)
                        }
                    }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, friendsUsernames)
                    binding.sharedWithInput.adapter = adapter
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to fetch friends: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "User is not logged in", Toast.LENGTH_SHORT).show()
        }
    }


    private fun setupSubmitButton() {
        binding.submitButton.setOnClickListener {
            if (validateInputs()) {
                submitShoppingItem()
            } else {
                Toast.makeText(context, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateInputs(): Boolean {
        // Use ?. to safely call methods on nullable receivers and ?: to provide a default value
        val isItemInputNotEmpty = binding.itemInput.text?.isNotEmpty() == true
        val isQuantityValid = binding.quantityInput.text?.toString()?.toIntOrNull() != null
        val isCategorySelected = binding.categoryInput.selectedItemPosition > 0
        val isSharedWithSelected = binding.sharedWithInput.selectedItemPosition > 0

        return isItemInputNotEmpty && isQuantityValid && isCategorySelected && isSharedWithSelected
    }


    private fun submitShoppingItem() {
        val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Toast.makeText(context, "User is not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val shoppingItem = ShoppingListItem(
            category = binding.categoryInput.selectedItem.toString(),
            lastPurchased = Timestamp(Date()), // Assuming you have a Timestamp constructor taking Date
            name = binding.itemInput.text.toString(),
            quantity = binding.quantityInput.text.toString().toInt(),
            purchasedBy = userUid
        )

        firestoreHelper.addShoppingListItem(userUid, shoppingItem)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}