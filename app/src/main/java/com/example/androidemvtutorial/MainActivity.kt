package com.example.androidemvtutorial

import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.NfcAdapter.ReaderCallback
import android.nfc.NfcManager
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.androidemvtutorial.data.CardData
import com.example.androidemvtutorial.ui.theme.AndroidEMVTutorialTheme
import com.github.devnied.emvnfccard.parser.EmvTemplate
import java.io.IOException
import java.time.LocalDate
import java.time.ZoneId

class MainActivity : ComponentActivity(), ReaderCallback {
    private var mNfcAdapter: NfcAdapter? = null
    private lateinit var cardData: MutableState<List<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val manager = getSystemService(Context.NFC_SERVICE) as NfcManager
        mNfcAdapter = manager.defaultAdapter
        setContent {
            cardData = CardData.getData()
            AndroidEMVTutorialTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AppContent(cardData = cardData)
                }
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onTagDiscovered(tag: Tag?) {
        val isoDep: IsoDep?
        val data = arrayListOf<String>()
        try {
            isoDep = IsoDep.get(tag)
            if(isoDep != null) {
                (getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(
                    VibrationEffect.createOneShot(150,50)
                )
            }
            isoDep.connect()
            val provider = PcscProvider()
            provider.setmTagCom(isoDep)
            val config = EmvTemplate.Config()
                .setContactLess(true)
                .setReadAllAids(true)
                .setReadTransactions(true)
                .setRemoveDefaultParsers(false)
                .setReadAt(true)

            val parser = EmvTemplate.Builder()
                .setProvider(provider)
                .setConfig(config)
                .build()

            val card = parser.readEmvCard()
            val cardNumber = card.cardNumber
            data.add("Card Number: $cardNumber")
            Log.d("CardNumber: ", cardNumber)

            val cardType = card.type.getName()
            data.add("Card Type: $cardType")
            Log.d("cardType: ", cardType)

            val expireDate = card.expireDate
            var date = LocalDate.of(1999,12,31)
            if(expireDate != null) {
                date = expireDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
            data.add("Expiry Date: $date")
            Log.d("CardDate: ", date.toString())

            try {
                isoDep.close()
                var newData = ArrayList<String>()
                newData = data
                CardData.updateData(newData)
                cardData.value = newData
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        if (mNfcAdapter != null) {
            val options = Bundle()
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)

            mNfcAdapter!!.enableReaderMode(
                this,
                this,
                NfcAdapter.FLAG_READER_NFC_A or
                        NfcAdapter.FLAG_READER_NFC_B or
                        NfcAdapter.FLAG_READER_NFC_F or
                        NfcAdapter.FLAG_READER_NFC_V or
                        NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
                options
            )
        }
    }
}

@Composable
fun CardDetail(cardData: MutableState<List<String>>, modifier: Modifier) {
    Surface {
        Column(modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (data in cardData.value) {
                Text(text = data, modifier = modifier)
            }
        }
    }
}

@Composable
fun AppContent(cardData: MutableState<List<String>>) {
    CardDetail(cardData = cardData, modifier = Modifier)
}

@Preview(showBackground = true)
@Composable
fun CardDetailPreview() {
    AndroidEMVTutorialTheme {
        CardDetail(
            cardData = mutableStateOf(listOf("Card Number: 1234 5678 9012 3456", "Expiry Date: 12/99")),
            modifier = Modifier
        )
    }
}
