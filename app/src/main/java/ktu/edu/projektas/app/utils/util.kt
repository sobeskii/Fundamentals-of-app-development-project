package ktu.edu.projektas.app.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageItemInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.util.Base64
import android.util.Log
import java.security.MessageDigest

     fun keyHashes(context: Context){
        var info : PackageInfo = context.packageManager.getPackageInfo(context.packageName,PackageManager.GET_SIGNATURES)

        for(signature : Signature in info.signatures){
            var messageDigest : MessageDigest = MessageDigest.getInstance("SHA")
            messageDigest.update(signature.toByteArray())
            var keyHashes : String = String(Base64.encode(messageDigest.digest(),0))
            Log.d("Key Hashes","Facebook Key Hashes:"+  keyHashes)
        }
    }

