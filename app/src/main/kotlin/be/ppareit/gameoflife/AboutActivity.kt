package be.ppareit.gameoflife

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class AboutActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about)

        val aboutText = findViewById<TextView>(R.id.about_content)
        aboutText.text = getString(R.string.about_text, App.getVersion())
    }
}
