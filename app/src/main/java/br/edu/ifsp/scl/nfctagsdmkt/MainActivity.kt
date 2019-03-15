package br.edu.ifsp.scl.nfctagsdmkt

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    val nfcWorker = NfcWorker()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gravarBt.setOnClickListener {
            if (escritaEt.text.toString().isNotEmpty()) {
                it.isEnabled = false
                escritaEt.isEnabled = false
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val infoTag: String = nfcWorker.lerGravarTag(intent, !gravarBt.isEnabled, escritaEt.text.toString())

        leituraTv.text = infoTag

        if (!gravarBt.isEnabled) {
            gravarBt.isEnabled = true
            escritaEt.setText("")
            escritaEt.isEnabled = true
        }
    }
}
