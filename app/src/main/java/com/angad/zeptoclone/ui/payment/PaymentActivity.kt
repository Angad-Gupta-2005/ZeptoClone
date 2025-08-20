package com.angad.zeptoclone.ui.payment

import com.angad.zeptoclone.BuildConfig
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.angad.zeptoclone.data.repository.PaymentRepository
import com.angad.zeptoclone.ui.screens.payments.PaymentSuccessScreen
import com.angad.zeptoclone.ui.screens.payments.PaymentSummaryScreen
import com.angad.zeptoclone.ui.theme.ZeptoCloneTheme
import com.angad.zeptoclone.ui.viewmodel.CartViewModel
import com.angad.zeptoclone.ui.viewmodel.PaymentViewModel
import com.angad.zeptoclone.utils.PaymentStatus
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import javax.inject.Inject

@AndroidEntryPoint
class PaymentActivity : ComponentActivity(), PaymentResultListener {

    @Inject
    lateinit var paymentRepository: PaymentRepository

    private lateinit var paymentViewModel: PaymentViewModel
    private lateinit var cartViewModel: CartViewModel
    private var checkOut: Checkout? = null
    private val TAG = "Payment Activity"

    //    Intent data - simplified to essential fields
    private var totalAmount: Double = 0.0
    private var orderId: String = ""
    private var userEmail: String = ""
    private var userPhone: String = ""

    //    This receiver will help catch and prevent the Razorpay receiver leak
    private val razorpayCleanupReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            //    Just log the action to help the debug
            Log.d(TAG, "onReceive: ${intent?.action}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate: Payment Activity Starting")

        //    Extract data from intent
        extractIntentData()

        //    Initialised razorpay and register receiver
        initialisedRazorpay()

        setContent {
            paymentViewModel = viewModel()
            val navController = rememberNavController()

            //    Set data in viewModel using LaunchedEffect
            LaunchedEffect(Unit) {
                paymentViewModel.setAmount(totalAmount)
                paymentViewModel.setOrderId(orderId)
                paymentViewModel.setUserEmail(userEmail)
                paymentViewModel.setUserPhone(userPhone)
            }

            ZeptoCloneTheme {
                NavHost(navController = navController, startDestination = "payment_summary") {
                    composable("payment_summary") {
                        PaymentSummaryScreen(
                            paymentViewModel = paymentViewModel,
                            onPaymentInit = { amount -> startRazorpayPayment(amount) },
                            onNavigateBack = { finish() }
                        )
                    }

                    composable("payment_success/{paymentId}") { backStackEntry ->
                        val paymentId = backStackEntry.arguments?.getString("paymentId") ?: ""
                        PaymentSuccessScreen(
                            paymentId = paymentId,
                            onDone = {
                                setResult(RESULT_OK)
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }

    private fun extractIntentData() {
        try {
            //    Extract only essential data
            totalAmount = intent?.getDoubleExtra("TOTAL_AMOUNT", 0.0) ?: 0.0
            orderId = intent?.getStringExtra("ORDER_ID") ?: ""
            userEmail = intent?.getStringExtra("USER_EMAIL") ?: ""
            userPhone = intent?.getStringExtra("USER_PHONE") ?: ""
            Log.d(TAG, "extractIntentData: amount $totalAmount")
        } catch (e: Exception) {
            Log.e(TAG, "extractIntentData: $e", e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun initialisedRazorpay() {
        //    Preload razorpay
        try {
            Checkout.preload(applicationContext)

            //    Register our cleanup receiver
            val intentFilter = IntentFilter().apply {
                addAction("com.google.android.gms.auth.api.phone.SMS_RETRIEVED")
                addAction("android.provider.Telephony.SMS_RECEIVED")
                addAction("rzp.device_token.shared")
            }
            registerReceiver(razorpayCleanupReceiver, intentFilter, RECEIVER_NOT_EXPORTED)
        } catch (e: Exception) {
            Log.e(TAG, "initialisedRazorpay: $e", e)
        }
    }

    private fun startRazorpayPayment(amount: Double) {
        try {
            checkOut = Checkout()

            //    Set API key
            val apiKey = BuildConfig.PAYMENT_API_KEY
            if (apiKey.isNullOrBlank()) {
                throw IllegalStateException("PAYMENT_API_KEY is missing")
            }
            checkOut?.setKeyID(apiKey)

            //    Create payment options
            val options = JSONObject().apply {
                //    Basic details
                put("name", "Zepto")
                put("description", "Order Payment")
                put("currency", "INR")
                put("amount", (amount * 100).toInt())

                //    Users details
                val prefill = JSONObject().apply {
                    put(
                        "email",
                        userEmail.takeIf { it.isNotEmpty() && it.contains("@") }
                            ?: "customer@example.com")
                    put(
                        "contact",
                        userPhone.takeIf { it.isNotEmpty() && it.length >= 10 } ?: "9999999999")
                }
                put("prefill", prefill)

                //    Optional settings
                put("theme.color", "#FF3F6C")
                put("readonly.sms_retriever", false)
            }
            //    Start payment
            checkOut?.open(this, options)
        } catch (e: Exception) {
            Log.e(TAG, "startRazorpayPayment: Error starting payment", e)
            Toast.makeText(this, "Payment setup failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    //    Update the onPaymentSuccess method
    @Suppress("DEPRECATION")
    override fun onPaymentSuccess(razorpayPaymentId: String) {
        Log.i(TAG, "onPaymentSuccess: Payment successful with ID $razorpayPaymentId")
        try {
            val amount = paymentViewModel.amount.value
            val orderId = paymentViewModel.orderId.value

            //    Extract cart items data
            val cartItems =
                intent?.getStringArrayListExtra("CART_ITEMS_DATA")?.toList() ?: emptyList()

            //    Extract cart item imageUrls
            val imageUrlsIntent =
                intent?.getSerializableExtra("CART_ITEM_IMAGES") as? HashMap<String, String>
            val itemImageUrls = imageUrlsIntent ?: emptyMap()

            Log.d(
                TAG,
                "Payment Successful - Cart items: ${cartItems.size}, imageUrls ${itemImageUrls.size}"
            )

            itemImageUrls.forEach { (key, value) ->
                Log.d(TAG, "Payment Successful - Key: $key, Value: $value")
            }

            //    Set image Url in the viewmodel
            paymentViewModel.setItemImageUrls(itemImageUrls)

            //    Create a dedicated coroutine for the repository call that would not be canceled
            val job = paymentViewModel.savePaymentRecord(
                paymentId = razorpayPaymentId,
                orderId = orderId,
                amount = amount,
                items = cartItems,
                itemImageUrls = itemImageUrls
            )

            val resultIntent = Intent().apply {
                putExtra("PAYMENT_ID", razorpayPaymentId)
                putExtra("ORDER_ID", orderId)
                putExtra("PAYMENT_SUCCESSFUL", true)
            }
            setResult(RESULT_OK, resultIntent)

            //    Navigate to success screen
            paymentViewModel.getNavController()?.navigate("payment_success/$razorpayPaymentId")
        } catch (e: Exception) {
            Log.e(TAG, "onPaymentSuccess: $e", e)
            Toast.makeText(this, "Payment recorded but an error occurred", Toast.LENGTH_SHORT)
                .show()
            setResult(RESULT_OK)
            finish()
        }
    }

    override fun onPaymentError(code: Int, description: String?) {
        val errorDescription = description ?: "Unknown Error"

        //    Show error to the user
        Toast.makeText(this, "Payment Failed: $errorDescription", Toast.LENGTH_SHORT).show()

        //    Update payment state
        paymentViewModel.updatePaymentStatus(PaymentStatus.Error(errorDescription))

        //    Navigate back to the summary screen
        paymentViewModel.getNavController()?.popBackStack()

        //    Clean up
        cleanup()
    }

    private fun cleanup() {
        //    Clear checkout
        checkOut = null

        //    Use Razorpay's cleanup Method
        try {
            Checkout.clearUserData(this)
        } catch (e: Exception) {
            Log.d(TAG, "cleanup: Error in Razorpay cleanup")
        }
    }

    override fun onStop() {
        super.onStop()
        cleanup()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(razorpayCleanupReceiver)
            //    Final cleanup
            cleanup()
        } catch (e: Exception) {
            Log.e(TAG, "onDestroy: Error in final cleanup $e", e)
        }
    }
}