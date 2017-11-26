package com.hw.szoftarch.worklogger.admin;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hw.szoftarch.worklogger.R;
import com.hw.szoftarch.worklogger.entities.User;
import com.hw.szoftarch.worklogger.entities.WorkingHour;
import com.hw.szoftarch.worklogger.recycler_tools.DeleteCallback;
import com.hw.szoftarch.worklogger.recycler_tools.ItemSwipeHelperAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class UserManagementAdapter extends RecyclerView.Adapter<UserManagementAdapter.ItemViewHolder>
        implements ItemSwipeHelperAdapter {
    private List<User> mUsers = new ArrayList<>();
    private DeleteCallback mDeleteCallback;
    private boolean needToNotifyItem = true;

    UserManagementAdapter(final DeleteCallback deleteCallback) {
        this.mDeleteCallback = deleteCallback;
    }

    User getItem(final int position) {
        return mUsers.get(position);
    }

    void setNotNeedToNotify() {
        needToNotifyItem = false;
    }

    void notifyItemChangedIfNeeded(final int position) {
        if (needToNotifyItem) {
            notifyItemChanged(position);
        }
        needToNotifyItem = true;
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView googleId, name, level;

        ItemViewHolder(View view) {
            super(view);
            googleId = view.findViewById(R.id.googleId);
            name = view.findViewById(R.id.name);
            level = view.findViewById(R.id.level);
        }
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_rowitem, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {
        final User user = mUsers.get(position);

        holder.googleId.setText(user.getGoogleId());
        holder.name.setText(user.getName());
        holder.level.setText(user.getLevel());
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    @Override
    public void onItemSwipe(final int position, int direction) {
        if (direction == ItemTouchHelper.END) {
            mDeleteCallback.deleteItem(position);
        } else {
            notifyItemChanged(position);
        }
    }

    void remove(final int positionToDelete) {
        mUsers.remove(positionToDelete);
        notifyItemRemoved(positionToDelete);
    }

    void add(final User user) {
        mUsers.add(user);
        notifyItemInserted(mUsers.size() - 1);
    }

    void update(final  User user) {
        for (int i = 0; i < mUsers.size(); i++) {
            if (mUsers.get(i).getGoogleId().equals(user.getGoogleId())) {
                mUsers.set(i, user);
                notifyItemChanged(i);
            }
        }
    }

    void setUsers(final List<User> users) {
        mUsers = users;
        notifyDataSetChanged();
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
    }
}
