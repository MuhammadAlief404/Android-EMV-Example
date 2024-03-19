package com.example.androidemvtutorial

import android.nfc.tech.IsoDep
import com.github.devnied.emvnfccard.exception.CommunicationException
import com.github.devnied.emvnfccard.parser.IProvider
import java.io.IOException

class PcscProvider : IProvider {

    private lateinit var mTagCom: IsoDep

    override fun transceive(pCommand: ByteArray?): ByteArray {
        var response: ByteArray? = null
        try {
            if(mTagCom.isConnected) {
                response = mTagCom.transceive(pCommand)
            }
        } catch (e: IOException) {
            throw CommunicationException(e.message)
        }
        return response!!
    }

    override fun getAt(): ByteArray {
        var result: ByteArray? = null
        result = mTagCom.historicalBytes
        if (result == null) {
            result = mTagCom.hiLayerResponse
        }
        return result
    }

    fun setmTagCom(tagCom: IsoDep) {
        mTagCom = tagCom
    }
}