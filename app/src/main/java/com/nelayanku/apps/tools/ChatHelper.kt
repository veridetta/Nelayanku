package com.nelayanku.apps.tools

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.nelayanku.apps.chat.ChatActivity
import com.nelayanku.apps.model.ChatHeaderModel
import com.nelayanku.apps.model.UserDetail
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

fun insertChatHeader(jenis:String,documentId:String, uidUser: String, uidSeller: String, nameUser: String, nameSeller: String,
                     isAdmin: Boolean, lastChat: String, lastTanggal: String, lastJam: String, unReadCount: Int, context: Context, productId:String){
        val db = FirebaseFirestore.getInstance()
        val TAG = "InsertChatHeader"
        var nameSeller = ""
        var documentIds = documentId
        db.collection("users").document(uidSeller).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    //log
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    nameSeller = document.data?.get("name").toString()
                    Log.d(TAG, "DocumentSnapshot data: $nameSeller")
                    val uid = UUID.randomUUID().toString()
                    // Atur zona waktu ke WIB (Waktu Indonesia Barat)
                    val timeZone = TimeZone.getTimeZone("Asia/Jakarta")
// Buat objek SimpleDateFormat dengan zona waktu yang diatur
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    dateFormat.timeZone = timeZone
                    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    timeFormat.timeZone = timeZone
// Ambil tanggal dan jam sekarang berdasarkan zona waktu WIB
                    val tanggal = dateFormat.format(Date())
                    val jam = timeFormat.format(Date())
                    val chat = hashMapOf(
                        "uid" to uid,
                        "uidUser" to uidUser,
                        "nameUser" to nameUser,
                        "uidSeller" to uidSeller,
                        "nameSeller" to nameSeller,
                        "lastChat" to lastChat,
                        "lastTanggal" to lastTanggal,
                        "unread" to 0,
                        "lastJam" to lastJam,
                        "isAdmin" to isAdmin
                    )
                    //cek dahulu di collection ChatHeader ada data dengan uidUser dan uidSeller tidak
                    db.collection("ChatHeader")
                        .whereEqualTo("uidUser", uidUser)
                        .whereEqualTo("uidSeller", uidSeller)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (documents.isEmpty) {
                                // Membuat referensi ke koleksi "ChatHeader"
                                val chatHeaderCollection = db.collection("ChatHeader")
                                // Menambahkan dokumen baru ke koleksi "ChatHeader" dan mendapatkan referensi ke dokumen tersebut
                                val newChatHeaderDoc = chatHeaderCollection.document()
                                newChatHeaderDoc.set(chat)
                                    .addOnSuccessListener { _ ->
                                        // Dokumen berhasil ditambahkan
                                        val documentIdx = newChatHeaderDoc.id
                                        Log.d(TAG, "DocumentSnapshot written with ID: ${documentIdx}")
                                        //readChatHeader
                                        //insert product ke chat
                                        //intent ke chatActivity
                                        insertChat(
                                            documentIdx,
                                            uidUser,
                                            documentIdx,
                                            "product",
                                            productId,
                                            tanggal,
                                            jam,
                                            false,
                                            uidUser
                                        )
                                        val intent = Intent(context, ChatActivity::class.java)
                                        intent.putExtra("documentId", documentIdx)
                                        intent.putExtra("uid", uid)
                                        intent.putExtra("uidSeller", uidSeller)
                                        context.startActivity(intent)
                                    }
                                    .addOnFailureListener { e ->
                                        // Kesalahan saat menambahkan dokumen
                                    }
                            } else {
                                insertChat(documents.documents[0].id, uidUser,
                                    documents.documents[0].id,
                                    "product", productId, tanggal, jam, false,uidUser)
                                //readChatHeader
                                readChatHeader(documentIds,context)
                                //intent ke chatActivity
                                val intent = Intent(context, ChatActivity::class.java)
                                intent.putExtra("documentId", documents.documents[0].id)
                                //intent uid
                                intent.putExtra("uid", uid)
                                intent.putExtra("uid", uidSeller)
                                context.startActivity(intent)
                            }
                        }
                        .addOnFailureListener { exception ->
                            //log
                            Log.d(TAG, "get failed with ", exception)
                        }
                } else {
                    //log
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                //log
                Log.d(TAG, "get failed with ", exception)
            }
    }
fun editChatHeader(documentId: String, lastChat: String, lastTanggal: String, lastJam: String, unReadCount: Int, type:String){
    val db = FirebaseFirestore.getInstance()
    val TAG = "EditChatHeader"
    //buat data chat
    val chat = hashMapOf(
        "lastChat" to lastChat,
        "lastTanggal" to lastTanggal,
        "lastJam" to lastJam,
        "unread" to unReadCount,
        "type" to type
    )
    //simpan data ke collection ChatHeader
    db.collection("ChatHeader").document(documentId).update(chat as Map<String, Any>)
        .addOnSuccessListener {
            //log
            Log.d(TAG, "DocumentSnapshot successfully written!")
        }
        .addOnFailureListener { e ->
            //log
            Log.w(TAG, "Error writing document", e)
        }
}
fun readChatHeader(documentId: String,context: Context){
    val db = FirebaseFirestore.getInstance()
    val TAG = "ReadChatHeader"
    val chat = hashMapOf(
        "unread" to 0
    )
    //simpan data ke collection ChatHeader
    db.collection("ChatHeader").document(documentId).update(chat as Map<String, Any>)
        .addOnSuccessListener {
            //log
            Log.d(TAG, "DocumentSnapshot successfully written!")
            //readchat
            readChat(documentId)
        }
        .addOnFailureListener { e ->
            //log
            Log.w(TAG, "Error writing document", e)
        }

}
fun deleteChatHeader(documentId: String){
    val db = FirebaseFirestore.getInstance()
    val TAG = "DeleteChatHeader"
    //simpan data ke collection ChatHeader
    db.collection("ChatHeader").document(documentId).delete()
        .addOnSuccessListener {
            //log
            Log.d(TAG, "DocumentSnapshot successfully written!")
        }
        .addOnFailureListener { e ->
            //log
            Log.w(TAG, "Error writing document", e)
        }
}

fun insertChat(documentId: String, uid: String, chatHeaderId: String, type: String,
               message: String, tanggal: String,
               jam: String, read: Boolean,senderId:String){
    val db = FirebaseFirestore.getInstance()
    val TAG = "InsertChat"
    //buat data chat
    val chat = hashMapOf(
        "uid" to uid,
        "chatHeaderId" to chatHeaderId,
        "type" to type,
        "message" to message,
        "tanggal" to tanggal,
        "jam" to jam,
        "read" to read,
        "senderId" to senderId
    )
    //simpan data ke collection Chat
    db.collection("ChatHeader").document(documentId).collection("Chat").document(uid).set(chat as Map<String, Any>)
        .addOnSuccessListener {
            //log
            Log.d(TAG, "DocumentSnapshot successfully written!")
            //ambil data dahulu dari collection ChatHeader untuk mendapatkan unReadCount
            db.collection("ChatHeader").document(chatHeaderId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        //log
                        Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                        //ambil data chatHeader
                        val chatHeader = document.toObject(ChatHeaderModel::class.java)
                        //tambahkan unReadCount
                        val unReadCount = chatHeader!!.unRead!! + 1
                        //update data di collection ChatHeader
                        editChatHeader(chatHeaderId, message, tanggal, jam, unReadCount,type)
                    } else {
                        //log
                        Log.d(TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    //log
                    Log.d(TAG, "get failed with ", exception)
                }
        }
        .addOnFailureListener { e ->
            //log
            Log.w(TAG, "Error writing document", e)
        }
}
//update semua chat yang belum di read
fun readChat(chatHeaderId: String){
    val db = FirebaseFirestore.getInstance()
    val TAG = "ReadChat"
    //ambil data dahulu dari collection ChatHeader untuk mendapatkan unReadCount
    db.collection("ChatHeader").document(chatHeaderId).get()
        .addOnSuccessListener { document ->
            if (document != null) {
                //log
                Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                //ambil data chatHeader
                val chatHeader = document.toObject(ChatHeaderModel::class.java)
                //update data di collection ChatHeader
                editChatHeader(chatHeaderId, chatHeader!!.lastChat!!, chatHeader.lastTanggal!!, chatHeader.lastJam!!, 0,chatHeader.type!!)
            } else {
                //log
                Log.d(TAG, "No such document")
            }
        }
        .addOnFailureListener { exception ->
            //log
            Log.d(TAG, "get failed with ", exception)
        }
}