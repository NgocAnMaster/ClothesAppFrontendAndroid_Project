package com.master.myapplication2

import android.app.Activity
import com.master.myapplication2.models.Product
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
//import android.widget.RecyclerView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.master.myapplication2.models.TryonImage
import com.master.myapplication2.network.ApiClient
import com.master.myapplication2.network.ApiService
import com.master.myapplication2.utils.UserSessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CreateFragment : Fragment() {

    companion object {
        private const val REQUEST_VIEW_TRYON = 1001
    }

    private lateinit var btnTakePhoto: Button
    private lateinit var btnChoosePicture: Button
    private lateinit var recyclerHistory: RecyclerView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var textEmpty: TextView
    private lateinit var adapter: TryonHistoryAdapter

    private var photoUri: Uri? = null

    // ---- CAMERA LAUNCHERS ----
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && photoUri != null) {
                val intent = Intent(requireContext(), EditImageActivity::class.java)
                intent.putExtra("imageUri", photoUri.toString())
                startActivity(intent)
            }
        }

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                launchCamera()
            }
        }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val intent = Intent(requireContext(), EditImageActivity::class.java)
                intent.putExtra("imageUri", it.toString())
                startActivity(intent)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create, container, false)

        btnTakePhoto = view.findViewById(R.id.btn_take_photo)
        btnChoosePicture = view.findViewById(R.id.btn_choose_picture)
        recyclerHistory = view.findViewById(R.id.recyclerHistory)
        layoutEmpty = view.findViewById(R.id.layoutEmpty)
        textEmpty = view.findViewById(R.id.textEmpty)

        recyclerHistory.layoutManager = GridLayoutManager(requireContext(), 3)
//        adapter = TryonHistoryAdapter(emptyList())

        adapter = TryonHistoryAdapter(emptyList()) { position ->
            openTryonViewer(position)
        }

        recyclerHistory.adapter = adapter

        btnTakePhoto.setOnClickListener {
            requestCameraPermission.launch(android.Manifest.permission.CAMERA)
        }

        btnChoosePicture.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        loadTryonHistory()

        return view
    }

    private fun launchCamera() {
        val context = requireContext()
        val photoFile = File.createTempFile(
            "IMG_",
            ".jpg",
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        )
        photoUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
        takePictureLauncher.launch(photoUri)
    }

    // ---- FETCH HISTORY ----
    private fun loadTryonHistory() {
        // Check login
        val isLoggedIn = UserSessionManager.isLoggedIn(requireContext())
        if (!isLoggedIn) {
            layoutEmpty.visibility = View.VISIBLE
            recyclerHistory.visibility = View.GONE
            textEmpty.text = "Log in or sign up to save your tryons"
            return
        }

        val userId = UserSessionManager.getUserId(requireContext())
        Toast.makeText(
            this.requireContext(),
            "Id: $userId",
            Toast.LENGTH_SHORT
        ).show()
        val api = ApiClient.createService(ApiService::class.java)
        api.getTryonImagesByUser(userId).enqueue(object : Callback<List<TryonImage>> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<List<TryonImage>>,
                response: Response<List<TryonImage>>
            ) {
                val tryons = response.body()
                if (tryons.isNullOrEmpty()) {
                    layoutEmpty.visibility = View.VISIBLE
                    recyclerHistory.visibility = View.GONE
                    textEmpty.text = "No tryons yet. Start creating one now!"
                } else {
                    layoutEmpty.visibility = View.GONE
                    recyclerHistory.visibility = View.VISIBLE

                    val enriched = tryons.map {
                        TryonHistoryAdapter.TryonImageWithProduct(
                            tryonId = it._id,
                            imageUrl = it.imageUrl ?: "",
                            productName = getProductNameFromId(it.productId),
                            createdAgo = getTimeAgo(it.createdAt),
                            productId = it.productId // ✅ added
                        )
                    }
                    adapter.updateList(enriched)
                }
            }

            override fun onFailure(call: Call<List<TryonImage>>, t: Throwable) {
                layoutEmpty.visibility = View.VISIBLE
                recyclerHistory.visibility = View.GONE
                textEmpty.text = "Failed to load tryons: $t"
            }
        })
    }

    // ---- UTILITIES ----
//    private fun getProductNameFromId(productId: String): String {
//        // Return a placeholder first
//        var productName = "Loading..."
//
//        val api = ApiClient.createService(ApiService::class.java)
//        api.getProductById(productId).enqueue(object : Callback<Product> {
//            override fun onResponse(call: Call<Product>, response: Response<Product>) {
//                val product = response.body()
//                if (product != null) {
//                    // Update item in adapter dynamically when we get the name
//                    val updatedList = adapter.currentItems.map {
//                        if (it.productName == "Product $productId")
//                            it.copy(productName = product.name)
//                        else it
//                    }
//                    adapter.updateList(updatedList)
//                }
//            }
//
//            override fun onFailure(call: Call<Product>, t: Throwable) {
//                // You can log or ignore failure
//            }
//        })
//
//        // return temporary placeholder
//        return "Product $productId"
//    }

    private fun getProductNameFromId(product: Product?): String {
        return product?.name ?: "Unknown Product"
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun getTimeAgo(isoDate: String): String {
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        val created = LocalDateTime.parse(isoDate, formatter)
        val now = LocalDateTime.now()
        val duration = Duration.between(created, now)
        val days = duration.toDays()
        return when {
            days < 1 -> "Created today"
            days == 1L -> "Created yesterday"
            days < 30 -> "Created $days days ago"
            days < 365 -> "Created ${days / 30} months ago"
            else -> "Created ${days / 365} years ago"
        }
    }

    private fun openTryonViewer(position: Int) {
        val items = adapter.currentItems

        val imageUrls = ArrayList(
            items.map {
                when {
                    it.imageUrl.startsWith("http") -> it.imageUrl
                    it.imageUrl.startsWith("/api/") ->
                        BuildConfig.BASE_URL.trimEnd('/') + it.imageUrl
                    else ->
                        BuildConfig.BASE_URL.trimEnd('/') + "/api" + it.imageUrl
                }
            }
        )

        val productIds = ArrayList(items.map { it.productId?._id ?: "" })
        val tryonIds = ArrayList(items.map { it.tryonId })

        val userId = UserSessionManager.getUserId(requireContext())

        val intent = Intent(requireContext(), TryonImageViewerActivity::class.java)
        intent.putStringArrayListExtra("images", imageUrls)
        intent.putStringArrayListExtra("productIds", productIds)
        intent.putStringArrayListExtra("tryonIds", tryonIds)
        intent.putExtra("userId", userId)
        intent.putExtra("position", position)

        startActivityForResult(intent, REQUEST_VIEW_TRYON)
    }

    @Deprecated("This onActivityResult method is deprecated. Consider using other methods if possible.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_VIEW_TRYON && resultCode == Activity.RESULT_OK) {
            loadTryonHistory() // 🔄 reload after delete
        }
    }
}
