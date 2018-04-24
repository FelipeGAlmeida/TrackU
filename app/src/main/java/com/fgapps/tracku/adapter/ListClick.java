package com.fgapps.tracku.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.fgapps.tracku.helper.ListListener;

/**
 * Created by (Engenharia) Felipe on 26/03/2018.
 */

public class ListClick implements RecyclerView.OnItemTouchListener{

    ListListener listListener;
    GestureDetector gestureDetector;

    public ListClick(Context context, final RecyclerView recyclerView, final ListListener listListener) {
        this.listListener = listListener;
        this.gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if(child != null && listListener != null)
                    listListener.OnClick(child, recyclerView.getChildLayoutPosition(child));
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if(child != null && listListener != null)
                    listListener.OnLongClick(child, recyclerView.getChildLayoutPosition(child));
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        gestureDetector.onTouchEvent(e);
//        View child = rv.findChildViewUnder(e.getX(), e.getY());
//        if(child != null && listListener != null && gestureDetector.onTouchEvent(e))
//            listListener.OnClick(child, rv.getChildLayoutPosition(child));
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }
}
