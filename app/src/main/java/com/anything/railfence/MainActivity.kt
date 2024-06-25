package com.anything.railfence

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.anything.railfence.R

class MainActivity : AppCompatActivity() {

    private lateinit var editTextPlainText: EditText
    private lateinit var editTextDepth: EditText
    private lateinit var buttonEncrypt: Button
    private lateinit var buttonDecrypt: Button
    private lateinit var buttonCopyText: Button
    private lateinit var textViewEncryptedResult: TextView
    private lateinit var textViewDecryptedResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextPlainText = findViewById(R.id.editTextPlainText)
        editTextDepth = findViewById(R.id.editTextDepth)
        buttonEncrypt = findViewById(R.id.buttonEncrypt)
        buttonDecrypt = findViewById(R.id.buttonDecrypt)
        buttonCopyText = findViewById(R.id.buttonCopyText)
        textViewEncryptedResult = findViewById(R.id.textViewEncryptedResult)
        textViewDecryptedResult = findViewById(R.id.textViewDecryptedResult)

        buttonEncrypt.setOnClickListener {
            val plainText = editTextPlainText.text.toString()
            val depthStr = editTextDepth.text.toString()

            if (plainText.isEmpty()) {
                showToast("Please enter text to encrypt")
                return@setOnClickListener
            }

            val depth = depthStr.toIntOrNull()
            if (depth == null || depth <= 0) {
                showToast("Please enter a valid depth (positive integer)")
                return@setOnClickListener
            }

            val encryptedText = railFenceEncrypt(plainText, depth)
            textViewEncryptedResult.text = encryptedText
        }

        buttonDecrypt.setOnClickListener {
            val encryptedText = editTextPlainText.text.toString()
            val depthStr = editTextDepth.text.toString()

            if (encryptedText.isEmpty()) {
                showToast("Please enter text to decrypt")
                return@setOnClickListener
            }

            val depth = depthStr.toIntOrNull()
            if (depth == null || depth <= 0) {
                showToast("Please enter a valid depth (positive integer)")
                return@setOnClickListener
            }

            val decryptedText = railFenceDecrypt(encryptedText, depth)
            textViewDecryptedResult.text = decryptedText
        }

        buttonCopyText.setOnClickListener {
            val textToCopy = if (textViewEncryptedResult.text.isNotEmpty()) {
                textViewEncryptedResult.text.toString()
            } else {
                textViewDecryptedResult.text.toString()
            }

            if (textToCopy.isNotEmpty()) {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Copied Text", textToCopy)
                clipboard.setPrimaryClip(clip)
                showToast("Text copied to clipboard")
            } else {
                showToast("No text to copy")
            }
        }
    }

    private fun railFenceEncrypt(plainText: String, depth: Int): String {
        if (depth == 1) return plainText

        val rails = Array(depth) { StringBuilder() }
        var currentRail = 0
        var goingDown = false

        for (char in plainText) {
            rails[currentRail].append(char)

            if (currentRail == 0 || currentRail == depth - 1) {
                goingDown = !goingDown
            }

            currentRail += if (goingDown) 1 else -1
        }

        return rails.joinToString("")
    }

    private fun railFenceDecrypt(encryptedText: String, depth: Int): String {
        if (depth == 1) return encryptedText

        val n = encryptedText.length
        val rails = Array(depth) { CharArray(n) }
        val positions = IntArray(n)
        var currentRail = 0
        var goingDown = false
        var idx = 0

        // Step 1: Mark the positions in the rails where the characters will be placed
        for (i in 0 until n) {
            positions[i] = currentRail
            rails[currentRail][i] = '*'

            if (currentRail == 0 || currentRail == depth - 1) {
                goingDown = !goingDown
            }

            currentRail += if (goingDown) 1 else -1
        }

        // Step 2: Fill the rails with the encrypted text
        for (currentRail in 0 until depth) {
            for (i in 0 until n) {
                if (rails[currentRail][i] == '*' && idx < encryptedText.length) {
                    rails[currentRail][i] = encryptedText[idx++]
                }
            }
        }

        // Step 3: Read the rails in the zigzag manner to reconstruct the original text
        val decoded = CharArray(n)
        for (i in 0 until n) {
            decoded[i] = rails[positions[i]][i]
        }

        return String(decoded)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
