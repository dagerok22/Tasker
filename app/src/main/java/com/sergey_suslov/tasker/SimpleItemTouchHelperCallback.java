package com.sergey_suslov.tasker;

import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;

/**
 * Created by PMI51 on 07.02.2017.
 */

public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final MyTasksAdapter mAdapter;

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    public SimpleItemTouchHelperCallback(MyTasksAdapter adapter) {
        mAdapter = adapter;
    }

    public boolean isLongPressDragEnabled() {
        return false;
    }
    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        final int dragFlags = 0;
        final int swipeFlags = ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        // просто cдвигаем итем в направлении свайпа
        Log.d("SimpleItemTouch", String.valueOf(dX));
        viewHolder.itemView.setAlpha(1 - dX * 0.0025f);
        viewHolder.itemView.setTranslationX(dX-dX*0.35f);
    }
}
