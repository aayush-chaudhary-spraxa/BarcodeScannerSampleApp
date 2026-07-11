package com.barcodescannersampleapp.datawedge

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

object DataWedgeConstants {
    const val ACTION_SCAN = "com.barcodescannersampleapp.ACTION_SCAN"
    const val ACTION_DATAWEDGE = "com.symbol.datawedge.api.ACTION"
    const val ACTION_RESULT = "com.barcodescannersampleapp.ACTION_RESULT"

    const val EXTRA_DATA_STRING = "com.symbol.datawedge.data_string"
    const val EXTRA_LABEL_TYPE = "com.symbol.datawedge.label_type"
    const val EXTRA_SOURCE = "com.symbol.datawedge.source"

    const val EXTRA_CREATE_PROFILE = "com.symbol.datawedge.api.CREATE_PROFILE"
    const val EXTRA_SET_CONFIG = "com.symbol.datawedge.api.SET_CONFIG"
    const val EXTRA_SEND_RESULT = "SEND_RESULT"
    const val EXTRA_RESULT_ACTION = "COMMAND_IDENTIFIER"

    const val PROFILE_NAME = "BarcodeScannerSampleApp"
}

class ScanReceiver(private val listener: Listener) : BroadcastReceiver() {

    interface Listener {
        fun onScan(data: String, labelType: String, source: String)
        fun onDataWedgeResult(intent: Intent)
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            DataWedgeConstants.ACTION_SCAN -> {
                val data = intent.getStringExtra(DataWedgeConstants.EXTRA_DATA_STRING) ?: return
                val labelType = intent.getStringExtra(DataWedgeConstants.EXTRA_LABEL_TYPE) ?: "UNKNOWN"
                val source = intent.getStringExtra(DataWedgeConstants.EXTRA_SOURCE) ?: "scanner"
                listener.onScan(data, labelType, source)
            }
            DataWedgeConstants.ACTION_RESULT -> {
                listener.onDataWedgeResult(intent)
            }
        }
    }
}
