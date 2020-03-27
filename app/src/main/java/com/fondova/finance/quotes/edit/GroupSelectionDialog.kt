package com.fondova.finance.quotes.edit

import android.content.Context
import com.fondova.finance.workspace.WorkspaceGroup

class GroupSelectionDialog {

    companion object {

        fun showDialog(context: Context, itemName: String, values: List<WorkspaceGroup>, listener: ItemSelectedListener?) {
            SelectStringDialog.showDialog(context, itemName, values.map { it.getDisplayName() ?: "--" }, listener)
        }
    }
}