package com.vineet.qnnmodelrun

import android.os.*
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.*

class MainActivity : AppCompatActivity() {
    private val TAG = "QNNExecutor"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val btnRunModel0 = Button(this).apply { text = "Run Model 0bin" }
        val btnRunModel6 = Button(this).apply { text = "Run Model 6bin" }
        val textView = TextView(this).apply { text = "Waiting..." }

        btnRunModel0.setOnClickListener {
            textView.text = "Running model_0bin..."
            runQnnCommand(textView, getModel0Command())
        }

        btnRunModel6.setOnClickListener {
            textView.text = "Running model_6bin..."
            runQnnCommand(textView, getModel6Command())
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 150, 32, 32)
            addView(btnRunModel0)
            addView(btnRunModel6)
            addView(textView)
        }

        setContentView(layout)
    }

    private fun runQnnCommand(outputView: TextView, shellCommand: String) {
        Thread {
            try {
                val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", shellCommand))
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val errorReader = BufferedReader(InputStreamReader(process.errorStream))

                val output = StringBuilder()
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    output.appendLine("OUT: $line")
                    Log.d(TAG, line ?: "")
                }

                while (errorReader.readLine().also { line = it } != null) {
                    output.appendLine("ERR: $line")
                    Log.e(TAG, line ?: "")
                }

                val exitCode = process.waitFor()
                output.appendLine("Finished. Exit code: $exitCode")

                runOnUiThread {
                    outputView.text = output.toString()
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error", e)
                runOnUiThread {
                    outputView.text = "Exception: ${e.message}"
                }
            }
        }.start()
    }

    private fun getModel0Command(): String {
        return """
            su -c "
            cd /data/local/tmp/test &&
            export VENDOR_LIB=/vendor/lib64 &&
            export LD_LIBRARY_PATH=/data/local/tmp/test:/vendor/dsp/cdsp:\${'$'}VENDOR_LIB &&
            export ADSP_LIBRARY_PATH=/data/local/tmp/test:/system/lib/rfsa/adsp:/system/vendor/lib/rfsa/adsp:/dsp &&
            export PATH=\${'$'}PATH:/data/local/tmp/test &&
            ./qnn-net-run --retrieve_context model_0bin.bin --backend libQnnHttp.so --input_list raw_list.txt --config_file config_bin.json --duration 100
            "
        """.trimIndent()
    }

    private fun getModel6Command(): String {
        return """
            su -c "
            cd /data/local/tmp/test &&
            export VENDOR_LIB=/vendor/lib64 &&
            export LD_LIBRARY_PATH=/data/local/tmp/test:/vendor/dsp/cdsp:\${'$'}VENDOR_LIB &&
            export ADSP_LIBRARY_PATH=/data/local/tmp/test:/system/lib/rfsa/adsp:/system/vendor/lib/rfsa/adsp:/dsp &&
            export PATH=\${'$'}PATH:/data/local/tmp/test &&
            ./qnn-net-run --retrieve_context model_6bin.bin --backend libQnnHttp.so --input_list raw_list.txt --config_file config_bin6.json --duration 100
            "
        """.trimIndent()
    }
}
