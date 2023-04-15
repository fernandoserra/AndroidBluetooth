package com.example.androidbluetooth.ui.dashboard

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.androidbluetooth.databinding.FragmentDashboardBinding


class DashboardFragment : Fragment() {

    private val TAG = "DashboardFragment"
    private val REQUEST_ENABLE_BT = 1

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    //**********
    val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    //**********

    private var pendingIntent: PendingIntent? = null
    var nfcAdapter: NfcAdapter? = null

    //var pendingIntent: PendingIntent? = null



    private var requestBluetooth = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            //granted
        }else{
            //deny
        }
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("test006", "${it.key} = ${it.value}")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textDashboard
        dashboardViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        /**
        * Pidiendo permisos en tiempo de ejecución
        * */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT))
        }
        else{
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBtIntent)
        }
        /***/


        /**
         * Activando el Bluetooth si esta desactivado
         */
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }


        /**
         * Comprobando si el dispositivo es compatible con bluetooth
         */
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Log.i(TAG, "El dispositivo no es compatible con bluetooth ")
        }else{
            Log.i(TAG, "Compatible ")
        }


        //val nfc = NfcAdapter.getDefaultAdapter(context)
        //Initialise NfcAdapter
        /*binding.button.setOnClickListener {

            if (nfc != null && nfc.isEnabled){
                Toast.makeText(context, "NFC Disponible", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(context, "NFC No disponible", Toast.LENGTH_SHORT).show()
            }
        }*/


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nfcAdapter = NfcAdapter.getDefaultAdapter(context)

        pendingIntent = PendingIntent.getActivity(
            context,0,
            Intent(context, this.javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )
    }


    override fun onResume() {
        super.onResume()
        assert(nfcAdapter != null)
        nfcAdapter!!.enableForegroundDispatch(activity, pendingIntent, null, null)

       /* NfcAdapter.getDefaultAdapter(context)?.let { nfcAdapter ->
            // An Intent to start your current Activity. Flag to singleTop
            // to imply that it should only be delivered to the current
            // instance rather than starting a new instance of the Activity.
            val launchIntent = Intent(context, this.javaClass)
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

            // Supply this launch intent as the PendingIntent, set to cancel
            // one if it's already in progress. It never should be.
            val pendingIntent = PendingIntent.getActivity(
                context, 0, launchIntent, PendingIntent.FLAG_MUTABLE
            )

            // Define your filters and desired technology types
            val filters = arrayOf(IntentFilter(ACTION_TECH_DISCOVERED))
            val techTypes = arrayOf(arrayOf(IsoDep::class.java.name))

            // And enable your Activity to receive NFC events. Note that there
            // is no need to manually disable dispatch in onPause() as the system
            // very strictly performs this for you. You only need to disable
            // dispatch if you don't want to receive tags while resumed.
            //nfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, techTypes
                nfcAdapter.enableForegroundDispatch(activity, pendingIntent, filters, techTypes
            )
        }*/


    }

    override fun onPause() {
        super.onPause()
        //Onpause stop listening
        if (nfcAdapter != null) {
            nfcAdapter!!.disableForegroundDispatch(activity)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}