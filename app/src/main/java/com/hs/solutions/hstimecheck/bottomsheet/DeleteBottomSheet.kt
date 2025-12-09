package com.hs.solutions.hstimecheck.bottomsheet

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hs.solutions.hstimecheck.R

class DeleteBottomSheet(
    context: Context,
    private val onConfirm: (Action) -> Unit
) : BottomSheetDialog(context) {

    enum class Action {
        REMOVE_LIST, RETURN_NORMAL, DELETE_ALL
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sheet_delete_actions)

        val rbRemove = findViewById<RadioButton>(R.id.rbRemoveList)
        val rbNormal = findViewById<RadioButton>(R.id.rbReturnNormal)
        val rbDelete = findViewById<RadioButton>(R.id.rbDeletePermanent)

        val btnConfirm = findViewById<Button>(R.id.btnConfirm)

        btnConfirm?.setOnClickListener {
            val act = when {
                rbRemove?.isChecked == true -> Action.REMOVE_LIST
                rbNormal?.isChecked == true -> Action.RETURN_NORMAL
                else -> Action.DELETE_ALL
            }
            onConfirm(act)
            dismiss()
        }
    }
}
