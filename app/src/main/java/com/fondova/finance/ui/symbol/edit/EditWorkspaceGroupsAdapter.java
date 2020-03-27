package com.fondova.finance.ui.symbol.edit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.fondova.finance.R;
import com.fondova.finance.workspace.Workspace;
import com.fondova.finance.workspace.WorkspaceFactory;
import com.fondova.finance.workspace.WorkspaceGroup;
import com.fondova.finance.persistance.AppStorage;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditWorkspaceGroupsAdapter extends RecyclerView.Adapter<EditWorkspaceGroupsAdapter.QuoteViewHolder>
        implements DragItemTouchHelperAdapter {

    private static final String SYMBOL_NAME_FORMAT = "%s[%s]";

    private OnAdapterActionListener onAdapterActionListener;
    private AppStorage appStorage;
    private List<WorkspaceGroup> groups;
    private String workspaceId;
    private Context context;

    EditWorkspaceGroupsAdapter(Context context) {
        this.context = context;
    }

    public void setWorkspace(Workspace newWorkspace) {
        this.groups = newWorkspace.getGroups();
        notifyDataSetChanged();
    }

    public Workspace getWorkspace() {
        Workspace workspace = new WorkspaceFactory().emptyWorkspace();
        workspace.setGroups(groups);
        return workspace;
    }

    void setOnAdapterActionListener(OnAdapterActionListener listener) {
        onAdapterActionListener = listener;
    }

    private void showRenameGroupDialog(int position) {
        EditText input = new EditText(context);
        input.setText(groups.get(position).getDisplayName());
        new AlertDialog.Builder(context)
                .setMessage(R.string.enter_new_group_name)
                .setView(input)
                .setPositiveButton(R.string.ok, (dialogInterface, i) -> renameGroup(position, input.getText().toString()))
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show();
    }

    private void renameGroup(int position, String newName) {
        if (newName == null || newName.isEmpty()) {
            return;
        }
        groups.get(position).setDisplayName(newName);
        notifyDataSetChanged();
    }

    @Override
    public QuoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new QuoteViewHolder(inflater.inflate(R.layout.adapter_edit_quote, parent, false));
    }

    @Override
    public void onBindViewHolder(QuoteViewHolder h, int position) {
        WorkspaceGroup group = groups.get(position);

        h.clRow.setBackgroundResource(R.color.grey_3f);

        h.clRow.setOnClickListener(view -> showRenameGroupDialog(position));

        String name = group.getDisplayName();

        h.tvSymbol.setText(name);
        h.tvSymbol.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_edit, 0, 0, 0);
        h.tvSymbol.setCompoundDrawablePadding(4);

        if (onAdapterActionListener != null) {
            h.ivReorder.setOnTouchListener((v, event) -> {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    onAdapterActionListener.onStartDrag(h);
                }
                return false;
            });
        }

        if (onAdapterActionListener != null) {
            h.ivDelete.setOnClickListener(v -> {
                if (!group.getListOfQuotes().isEmpty()) {
                    new AlertDialog.Builder(context)
                            .setMessage(R.string.delete_group_dialog_message)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    removeItem(position);
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .create()
                            .show();
                } else {
                    removeItem(position);
                }
            });
        }
    }

    void removeItem(int position) {
        if (position == -1) return;
        groups.remove(position);

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(groups, fromPosition, toPosition);

        notifyItemMoved(fromPosition, toPosition);
    }


    public static class QuoteViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.cl_row) ConstraintLayout clRow;
        @BindView(R.id.tv_symbol) TextView tvSymbol;
        @BindView(R.id.tv_symbol_name) TextView tvSymbolName;
        @BindView(R.id.iv_delete) ImageView ivDelete;
        @BindView(R.id.iv_reorder) ImageView ivReorder;

        QuoteViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
