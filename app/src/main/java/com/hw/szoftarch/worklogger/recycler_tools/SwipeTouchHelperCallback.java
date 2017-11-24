package com.hw.szoftarch.worklogger.recycler_tools;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

public class SwipeTouchHelperCallback extends ItemTouchHelper.Callback{
    private final ItemSwipeHelperAdapter mAdapter;

    public SwipeTouchHelperCallback(final ItemSwipeHelperAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;  //The directions for the drap and drop
        int swipeFlags = ItemTouchHelper.END;                       //The directions for the swipe
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        mAdapter.onItemSwipe(viewHolder.getAdapterPosition(), direction);
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }
}
