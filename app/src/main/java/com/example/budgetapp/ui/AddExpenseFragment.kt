package com.example.budgetapp.ui

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.example.budgetapp.Data.Expense
import com.example.budgetapp.R
import com.example.budgetapp.viewmodel.ExpenseViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddExpenseFragment : Fragment() {

    private lateinit var viewModel: ExpenseViewModel

    private lateinit var tilAmount: TextInputLayout
    private lateinit var tilDescription: TextInputLayout
    private lateinit var tilCategory: TextInputLayout
    private lateinit var tilDate: TextInputLayout
    private lateinit var etAmount: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var etDate: TextInputEditText
    private lateinit var actvCategory: AutoCompleteTextView
    private lateinit var btnSave: MaterialButton
    private lateinit var btnClear: MaterialButton
    private lateinit var btnTakePhoto: MaterialButton
    private lateinit var btnPickGallery: MaterialButton
    private lateinit var ivPhotoPreview: ImageView

    private var currentPhotoPath: String? = null
    private var cameraImageUri: Uri? = null

    private var selectedDateMillis: Long = System.currentTimeMillis()

    private val categories = listOf(
        "Food", "Transport", "Entertainment",
        "Shopping", "Health", "Education", "Other"
    )

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraImageUri != null) {
            ivPhotoPreview.load(cameraImageUri) {
                crossfade(true)
                placeholder(android.R.drawable.ic_menu_gallery)
                error(android.R.drawable.stat_notify_error)
            }
            ivPhotoPreview.visibility = View.VISIBLE
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                // Only take persistable permission if the intent flags allow it
                // Many gallery pickers don't support this, so we wrap it to prevent crashes
                requireContext().contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                Log.e("AddExpense", "Could not take persistable permission: ${e.message}")
            }
            
            currentPhotoPath = uri.toString()
            ivPhotoPreview.load(uri) {
                crossfade(true)
                placeholder(android.R.drawable.ic_menu_gallery)
                error(android.R.drawable.stat_notify_error)
            }
            ivPhotoPreview.visibility = View.VISIBLE
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // Use view lifecycle check to ensure fragment is still active
            view?.post { launchCamera() }
        } else {
            Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_expense, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[ExpenseViewModel::class.java]

        tilAmount      = view.findViewById(R.id.tilAmount)
        tilDescription = view.findViewById(R.id.tilDescription)
        tilCategory    = view.findViewById(R.id.tilCategory)
        tilDate        = view.findViewById(R.id.tilDate)
        etAmount       = view.findViewById(R.id.etAmount)
        etDescription  = view.findViewById(R.id.etDescription)
        etDate         = view.findViewById(R.id.etDate)
        actvCategory   = view.findViewById(R.id.actvCategory)
        btnSave        = view.findViewById(R.id.btnSave)
        btnClear       = view.findViewById(R.id.btnClear)
        btnTakePhoto   = view.findViewById(R.id.btnTakePhoto)
        btnPickGallery = view.findViewById(R.id.btnPickGallery)
        ivPhotoPreview = view.findViewById(R.id.ivPhotoPreview)

        etDate.setText(formatDate(selectedDateMillis))

        val categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            categories
        )
        actvCategory.setAdapter(categoryAdapter)

        etDate.setOnClickListener { showDatePicker() }
        tilDate.setEndIconOnClickListener { showDatePicker() }

        btnTakePhoto.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                launchCamera()
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        btnPickGallery.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        btnSave.setOnClickListener {
            if (validateInputs()) saveExpense()
        }

        btnClear.setOnClickListener { clearForm() }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = selectedDateMillis
        }

        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val picked = Calendar.getInstance().apply {
                    set(year, month, day, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                selectedDateMillis = picked.timeInMillis
                etDate.setText(formatDate(selectedDateMillis))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.maxDate = System.currentTimeMillis()
        }.show()
    }

    private fun launchCamera() {
        try {
            val photoFile = createImageFile()
            cameraImageUri = FileProvider.getUriForFile(
                requireContext(),
                "com.example.budgetapp.fileprovider",
                photoFile
            )
            currentPhotoPath = photoFile.absolutePath
            cameraLauncher.launch(cameraImageUri)
        } catch (e: Exception) {
            Log.e("AddExpense", "Error launching camera: ${e.message}")
            Toast.makeText(requireContext(), "Could not prepare camera storage", Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs()
        }
        return File.createTempFile("EXPENSE_${timestamp}_", ".jpg", storageDir)
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        val amountText = etAmount.text.toString().trim()
        when {
            amountText.isEmpty() -> {
                tilAmount.error = "Please enter an amount"
                isValid = false
            }
            amountText.toDoubleOrNull() == null || amountText.toDouble() <= 0 -> {
                tilAmount.error = "Please enter a valid amount"
                isValid = false
            }
            else -> tilAmount.error = null
        }

        if (etDescription.text.toString().trim().isEmpty()) {
            tilDescription.error = "Please enter a description"
            isValid = false
        } else {
            tilDescription.error = null
        }

        if (actvCategory.text.toString().isEmpty()) {
            tilCategory.error = "Please select a category"
            isValid = false
        } else {
            tilCategory.error = null
        }

        return isValid
    }

    private fun saveExpense() {
        val expense = Expense(
            amount      = etAmount.text.toString().trim().toDouble(),
            description = etDescription.text.toString().trim(),
            category    = actvCategory.text.toString(),
            date        = selectedDateMillis,
            photoPath   = currentPhotoPath
        )
        viewModel.addExpense(expense)
        Toast.makeText(requireContext(), "Expense saved! ✅", Toast.LENGTH_SHORT).show()
        clearForm()
    }

    private fun clearForm() {
        etAmount.text?.clear()
        etDescription.text?.clear()
        actvCategory.text.clear()
        tilAmount.error = null
        tilDescription.error = null
        tilCategory.error = null
        tilDate.error = null
        selectedDateMillis = System.currentTimeMillis()
        etDate.setText(formatDate(selectedDateMillis))
        ivPhotoPreview.visibility = View.GONE
        ivPhotoPreview.load(null as Uri?)
        currentPhotoPath = null
        cameraImageUri = null
        etAmount.requestFocus()
    }

    private fun formatDate(timestamp: Long): String {
        return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}
