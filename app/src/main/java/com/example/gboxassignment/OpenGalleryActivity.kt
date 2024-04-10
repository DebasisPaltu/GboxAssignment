package com.example.gboxassignment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.String as String1

class OpenGalleryActivity : AppCompatActivity() {

    private var lastSavedImageFileName: String1? = null
    private val REQUEST_SELECT_IMAGE = 100
    private val REQUEST_SHARE_IMAGE = 200

    private lateinit var selectedImageView: ImageView
    private lateinit var editText: EditText
    private lateinit var logoImageView: ImageView
    private lateinit var bitmap: Bitmap
    private var offsetX = 0f
    private var offsetY = 0f

    private var textOffsetX: Float = 0f
    private var textOffsetY: Float = 0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_gallery)
        selectedImageView = findViewById(R.id.selectedImageView)
        editText = findViewById(R.id.editText)
        logoImageView = findViewById(R.id.logoImageView)

        val openGalleryButton: Button = findViewById(R.id.openGalleryButton)
        val saveButton: Button = findViewById(R.id.saveButton)
        val shareButton: Button = findViewById(R.id.shareButton)

        openGalleryButton.setOnClickListener {
            openGallery()
        }

        saveButton.setOnClickListener {
            if (selectedImageView.drawable == null) {
                Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val name = editText.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter a name for the image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val bitmap = (selectedImageView.drawable as BitmapDrawable).bitmap
            val file = createImageFile(name)

            if (file != null) {
                if (file.exists()) {
                    Toast.makeText(this, "An image with the same name already exists", Toast.LENGTH_SHORT).show()
                } else {
                    try {
                        val stream = FileOutputStream(file)
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                        stream.flush()
                        stream.close()
                        Toast.makeText(this, "Image saved successfully", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this, "The Image Already exist", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "The Image Already exist", Toast.LENGTH_SHORT).show()
            }
        }

        shareButton.setOnClickListener {
            shareImage()
        }

        selectedImageView.setOnTouchListener { view, event ->
            handleTouch(view, event)
            true
        }

        logoImageView.setOnTouchListener { view, event ->
            handleTouch(view, event)
            true
        }


        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No need for any action here
            }

            @SuppressLint("ClickableViewAccessibility")
            override fun afterTextChanged(s: Editable?) {
                // Enable text movement after user input
                editText.setOnTouchListener { view, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            textOffsetX = event.x - view.x
                            textOffsetY = event.y - view.y
                        }
                        MotionEvent.ACTION_MOVE -> {
                            view.x = event.x - textOffsetX
                            view.y = event.y - textOffsetY
                        }
                    }
                    true
                }
            }
        })

    }

    @SuppressLint("ResourceAsColor")
    private fun addEditTextOnImage() {
        val layoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)

        editText.layoutParams = layoutParams
        editText.hint = "Enter Text"
        editText.setBackgroundColor(android.R.color.transparent)
        editText.setTextColor(resources.getColor(android.R.color.black))

        val parentLayout = findViewById<ViewGroup>(R.id.selectedImageLayout)
        parentLayout.addView(editText)


    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_SELECT_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SELECT_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri: Uri = data.data ?: return
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImageUri)
                selectedImageView.setImageBitmap(bitmap)
                selectedImageView.visibility = View.VISIBLE
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun handleTouch(view: View, event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                offsetX = event.x - view.x
                offsetY = event.y - view.y
            }
            MotionEvent.ACTION_MOVE -> {
                view.x = event.x - offsetX
                view.y = event.y - offsetY
            }
        }
    }

    private fun createImageFile(name: String1): File? {
        val directory = getExternalFilesDir(MediaStore.Images.Media.DISPLAY_NAME)
        val file = File(directory, "$name.jpg")


        if (file.exists()) {
            return null
        }

        return file
    }




    private fun shareImage() {


        val filename = "image.jpg"
        val file = File(externalCacheDir, filename)
        val uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)


        val whatsappIntent = Intent(Intent.ACTION_SEND)
        whatsappIntent.type = "image/*"
        whatsappIntent.putExtra(Intent.EXTRA_STREAM, uri)
        whatsappIntent.setPackage("com.whatsapp") // Specify WhatsApp package
        whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            startActivity(whatsappIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "install Whatsapp", Toast.LENGTH_SHORT).show()
        }

        val instagramIntent = Intent(Intent.ACTION_SEND)
        instagramIntent.type = "image/*"
        instagramIntent.putExtra(Intent.EXTRA_STREAM, uri)
        instagramIntent.setPackage("com.instagram.android") // Specify Instagram package
        instagramIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            startActivity(instagramIntent)
        } catch (e: ActivityNotFoundException) {

            Toast.makeText(this, "install Instagram", Toast.LENGTH_SHORT).show()


        }
        val sameAppIntent = Intent(Intent.ACTION_SEND)
        sameAppIntent.type = "image/*"
        sameAppIntent.putExtra(Intent.EXTRA_STREAM, uri)
        sameAppIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            startActivity(Intent.createChooser(sameAppIntent, "Share image via"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Your app is not Available", Toast.LENGTH_SHORT).show()
        }
    }


    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private inner class OnTouchListener : View.OnTouchListener {
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    textOffsetX = event.x - editText.x
                    textOffsetY = event.y - editText.y
                }
            }
            return false
        }
    }

    private inner class DragTouchListener : View.OnTouchListener {
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            val x = event.x
            val y = event.y
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    textOffsetX = x - editText.x
                    textOffsetY = y - editText.y
                }
                MotionEvent.ACTION_MOVE -> {
                    editText.x = x - textOffsetX
                    editText.y = y - textOffsetY
                }
            }
            return true
        }
    }
}


