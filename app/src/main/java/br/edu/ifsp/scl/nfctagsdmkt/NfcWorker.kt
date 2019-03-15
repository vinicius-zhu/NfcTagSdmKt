package br.edu.ifsp.scl.nfctagsdmkt

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.os.Parcelable
import java.nio.charset.Charset

class NfcWorker {
    private lateinit var tag: Tag

    fun lerGravarTag(intent: Intent, gravar: Boolean, payload: String=""): String {
        tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)

        val listaNdefMsgs: MutableList<NdefMessage> = mutableListOf()

        //Leitura
        var infosTagSb: StringBuffer = StringBuffer()
        infosTagSb.append("Tag ID: ${tag.id}\n")
        infosTagSb.append("Lista de tecnologias")
        tag.techList.withIndex().forEach{
            infosTagSb.append("\t ${it.index} - ${it.value}\n")
        }

        when(intent.action) {
            NfcAdapter.ACTION_NDEF_DISCOVERED -> {
                val ndefMessages: Array<Parcelable> = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
                ndefMessages.withIndex().forEach{
                    msgParcelable -> infosTagSb.append("Mensagem: ${msgParcelable.index}\n")
                    val ndefMessage: NdefMessage = msgParcelable.value as NdefMessage
                    ndefMessage.records.withIndex().forEach{registro ->
                        infosTagSb.append("\t\t Registro: ${registro.index}\n")
                        infosTagSb.append("\t\t TNF: ${registro.value.tnf}\n")
                        infosTagSb.append("\t\t Tipo payload: ${String(registro.value.type)}\n")
                        infosTagSb.append("\t\t Payload puro: ${String(registro.value.payload)}\n")
                    }

                    listaNdefMsgs.add(ndefMessage)
                }
            }
            NfcAdapter.ACTION_TECH_DISCOVERED -> infosTagSb.append("Tipo de payload desconhecido ou não foi tratado pela Activiry")
            NfcAdapter.ACTION_TAG_DISCOVERED -> infosTagSb.append("Tecnologia não suportada pela Activiry")
        }

        // Escrita
        if (gravar) {
            gravarTag(listaNdefMsgs, payload)
        }
        return infosTagSb.toString()
    }

    private fun gravarTag(listaNdefMsgs: MutableList<NdefMessage>, payload: String){
        // lista geral de NDEF record
        val listaNdefRecs: MutableList<NdefRecord> = mutableListOf()

        // Adicionar todos os recs na lista geral de recs ndef
        listaNdefMsgs.forEach { listaNdefRecs.addAll(it.records)}

        // Criar um novo registro
        val charset: Charset = Charset.forName("UTF-8")
        val novoNdefRec = NdefRecord(
            NdefRecord.TNF_WELL_KNOWN,
            NdefRecord.RTD_TEXT,
            ByteArray(0),
            payload.toByteArray(charset)
        )
        listaNdefRecs.add(novoNdefRec)

        val novaNdefMsg = NdefMessage(listaNdefRecs.toTypedArray())
        listaNdefMsgs.add(novaNdefMsg)

        listaNdefMsgs.forEach {ndefMsg ->
            if (NdefFormatable.get(tag) != null) {
                NdefFormatable.get(tag).use {nf ->
                    nf.connect()
                    nf.format(ndefMsg)
                    nf.close()
                }
            }
            else {
                Ndef.get(tag)?.use { ndef ->
                    ndef.connect()
                    ndef.writeNdefMessage(ndefMsg)
                    ndef.close()
                }
            }
        }
    }
}