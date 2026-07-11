package com.barcodescannersampleapp.datawedge

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.facebook.react.bridge.LifecycleEventListener

class DataWedgeModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext), ScanReceiver.Listener, LifecycleEventListener {

    private val scanReceiver = ScanReceiver(this)
    private var isReceiverRegistered = false

    init {
        reactContext.addLifecycleEventListener(this)
    }

    override fun getName() = "DataWedgeModule"

    private fun registerReceiver() {
        if (isReceiverRegistered) return
        val filter = IntentFilter().apply {
            addAction(DataWedgeConstants.ACTION_SCAN)
            addAction(DataWedgeConstants.ACTION_RESULT)
        }
        ContextCompat.registerReceiver(
            reactApplicationContext,
            scanReceiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED,
        )
        isReceiverRegistered = true
    }

    private fun unregisterReceiver() {
        if (!isReceiverRegistered) return
        reactApplicationContext.unregisterReceiver(scanReceiver)
        isReceiverRegistered = false
    }

    override fun onHostResume() = registerReceiver()

    override fun onHostPause() = unregisterReceiver()

    override fun onHostDestroy() = unregisterReceiver()

    override fun onScan(data: String, labelType: String, source: String) {
        val payload = Arguments.createMap().apply {
            putString("data", data)
            putString("labelType", labelType)
            putString("source", source)
        }
        emit("onDataWedgeScan", payload)
    }

    override fun onDataWedgeResult(intent: Intent) {
        val payload = Arguments.createMap().apply {
            putString("command", intent.getStringExtra("COMMAND"))
            putString("result", intent.getStringExtra("RESULT"))
        }
        emit("onDataWedgeResult", payload)
    }

    private fun emit(eventName: String, payload: com.facebook.react.bridge.WritableMap) {
        reactApplicationContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(eventName, payload)
    }

    /**
     * Creates (or updates) a DataWedge profile associated with this app's package name,
     * with the scanner input enabled and intent output pointed at our broadcast action.
     * Mirrors Zebra's documented profile-provisioning flow so no manual DataWedge
     * configuration is required on the device.
     */
    @ReactMethod
    fun createProfile() {
        val context = reactApplicationContext

        context.sendBroadcast(
            Intent(DataWedgeConstants.ACTION_DATAWEDGE).apply {
                putExtra(DataWedgeConstants.EXTRA_CREATE_PROFILE, DataWedgeConstants.PROFILE_NAME)
            },
        )

        val appConfig = Bundle().apply {
            putString("PACKAGE_NAME", context.packageName)
            putStringArray("ACTIVITY_LIST", arrayOf("*"))
        }

        val barcodeConfig = Bundle().apply {
            putString("PLUGIN_NAME", "BARCODE")
            putBundle("PARAM_LIST", Bundle())
            putString("RESET_CONFIG", "true")
        }

        val intentConfig = Bundle().apply {
            putString("PLUGIN_NAME", "INTENT")
            putBundle(
                "PARAM_LIST",
                Bundle().apply {
                    putString("intent_output_enabled", "true")
                    putString("intent_action", DataWedgeConstants.ACTION_SCAN)
                    putString("intent_delivery", "2")
                },
            )
            putString("RESET_CONFIG", "true")
        }

        val profileConfig = Bundle().apply {
            putString("PROFILE_NAME", DataWedgeConstants.PROFILE_NAME)
            putString("PROFILE_ENABLED", "true")
            putString("CONFIG_MODE", "UPDATE")
            putParcelableArray("APP_LIST", arrayOf(appConfig))
        }

        context.sendBroadcast(
            Intent(DataWedgeConstants.ACTION_DATAWEDGE).apply {
                putExtra(DataWedgeConstants.EXTRA_SET_CONFIG, profileConfig)
            },
        )

        context.sendBroadcast(
            Intent(DataWedgeConstants.ACTION_DATAWEDGE).apply {
                putExtra(
                    DataWedgeConstants.EXTRA_SET_CONFIG,
                    Bundle(profileConfig).apply { putBundle("PLUGIN_CONFIG", barcodeConfig) },
                )
            },
        )

        context.sendBroadcast(
            Intent(DataWedgeConstants.ACTION_DATAWEDGE).apply {
                putExtra(
                    DataWedgeConstants.EXTRA_SET_CONFIG,
                    Bundle(profileConfig).apply { putBundle("PLUGIN_CONFIG", intentConfig) },
                )
            },
        )
    }

    /** Triggers a scan on devices without a hardware trigger button (e.g. via an on-screen button). */
    @ReactMethod
    fun startSoftScan() = sendSoftTrigger("START_SCANNING")

    @ReactMethod
    fun stopSoftScan() = sendSoftTrigger("STOP_SCANNING")

    private fun sendSoftTrigger(state: String) {
        reactApplicationContext.sendBroadcast(
            Intent(DataWedgeConstants.ACTION_DATAWEDGE).apply {
                putExtra("com.symbol.datawedge.api.SOFT_SCAN_TRIGGER", state)
            },
        )
    }
}
