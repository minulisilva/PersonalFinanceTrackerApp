package com.example.personalfinancetrackerapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.personalfinancetrackerapp.model.OnboardingPage

class OnboardingActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var dot1: View
    private lateinit var dot2: View
    private lateinit var dot3: View
    private lateinit var btnNext: Button
    private lateinit var btnSkip: TextView
    private var currentPage = 0
    private lateinit var pages: List<OnboardingPage>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        // Initialize views
        imageView = findViewById(R.id.imageView)
        titleTextView = findViewById(R.id.titleTextView)
        descriptionTextView = findViewById(R.id.descriptionTextView)
        dot1 = findViewById(R.id.dot1)
        dot2 = findViewById(R.id.dot2)
        dot3 = findViewById(R.id.dot3)
        btnNext = findViewById(R.id.btnNext)
        btnSkip = findViewById(R.id.btnSkip)

        // Define onboarding pages with the new PNGs
        pages = listOf(
            OnboardingPage(
                R.drawable.track_expenses,
                "Track Your Expenses",
                "Observe ongoing and tract it best categories."
            ),
            OnboardingPage(
                R.drawable.track_evolution,
                "Track Your Evolution",
                "Track your evolution from day to day, week to week and month to month."
            ),
            OnboardingPage(
                R.drawable.reminders_notifications,
                "Get Reminders & Notifications",
                "Reminders your bills due date before it’s late, notify you on more spending."
            )
        )

        // Show the first page
        updatePage(0)

        // Handle Next button
        btnNext.setOnClickListener {
            if (currentPage < pages.size - 1) {
                currentPage++
                updatePage(currentPage)
            } else {
                startGetStarted()
            }
        }

        // Handle Skip button
        btnSkip.setOnClickListener {
            startGetStarted()
        }
    }

    private fun updatePage(pageIndex: Int) {
        // Update content
        val page = pages[pageIndex]
        imageView.setImageResource(page.imageResId)
        titleTextView.text = page.title
        descriptionTextView.text = page.description

        // Update dots
        dot1.backgroundTintList = ContextCompat.getColorStateList(
            this,
            if (pageIndex == 0) R.color.purple else R.color.grey
        )
        dot2.backgroundTintList = ContextCompat.getColorStateList(
            this,
            if (pageIndex == 1) R.color.purple else R.color.grey
        )
        dot3.backgroundTintList = ContextCompat.getColorStateList(
            this,
            if (pageIndex == 2) R.color.purple else R.color.grey
        )

        // Update button visibility
        btnSkip.visibility = if (pageIndex == pages.size - 1) View.GONE else View.VISIBLE
    }

    private fun startGetStarted() {
        startActivity(Intent(this, GetStarted::class.java))
    }
}