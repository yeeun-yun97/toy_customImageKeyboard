package com.github.yeeun_yun97.toy.imagekeyboard.service.utils

import android.app.AppOpsManager
import android.content.ClipDescription
import android.content.Context
import android.content.pm.PackageManager
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputBinding
import android.view.inputmethod.InputConnection
import androidx.core.view.inputmethod.EditorInfoCompat

class SupportUtil {
    companion object {
        /**
         * 컨텐츠 타입이 맞는지 확인하는 함수
         */
        fun isCommitContentSupported(
            context:Context,
            inputConnection: InputConnection?,
            editorInfo: EditorInfo?, mimeType: String,
            packageManager: PackageManager,
            inputBinding: InputBinding
        ): Boolean {
            if (editorInfo == null || inputConnection == null) {
                return false
            }
            if (!validatePackageName(context, editorInfo, packageManager , inputBinding )) {
                return false
            }
            val supportedMimeTypes = EditorInfoCompat.getContentMimeTypes(editorInfo)
            println(editorInfo)
            for (supportedMimeType in supportedMimeTypes) {
                if (ClipDescription.compareMimeTypes(mimeType, supportedMimeType)) {
                    return true
                }
            }
            return false
        }

        private fun validatePackageName(
            context: Context,
            editorInfo: EditorInfo?,
            packageManager: PackageManager,
            inputBinding: InputBinding
        ): Boolean {
            if (editorInfo == null) {
                return false
            }
            val packageName = editorInfo.packageName ?: return false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return true
            }
            if (inputBinding == null) {
                // Due to b.android.com/225029, it is possible that getCurrentInputBinding() returns
                // null even after onStartInputView() is called.
                // TODO: Come up with a way to work around this bug....
                Log.e(
                    "TAG", "inputBinding should not be null here. "
                            + "You are likely to be hitting b.android.com/225029"
                )
                return false
            }
            val packageUid = inputBinding.uid
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                val appOpsManager =
                    context.getSystemService(InputMethodService.APP_OPS_SERVICE) as AppOpsManager
                try {
                    appOpsManager.checkPackage(packageUid, packageName)
                } catch (e: Exception) {
                    return false
                }
                return true
            }
            val possiblePackageNames = packageManager.getPackagesForUid(packageUid)
            for (possiblePackageName in possiblePackageNames!!) {
                if (packageName == possiblePackageName) {
                    return true
                }
            }
            return false
        }
    }
}