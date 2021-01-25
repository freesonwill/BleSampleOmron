package jp.co.ohq.blesampleomron.view.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class AbstractRecyclerAdapter<T, H extends AbstractRecyclerAdapter.ViewHolder>
        extends RecyclerView.Adapter<H> {

    @NonNull
    protected final Object mLock;
    @NonNull
    protected final LayoutInflater mInflater;
    @LayoutRes
    protected final int mResourceId;
    @NonNull
    protected final List<T> mObjects;
    @Nullable
    private OnItemClickListener mListener;

    public AbstractRecyclerAdapter(@NonNull Context context, @LayoutRes int resourceId) {
        this(context, resourceId, new ArrayList<T>(0), null);
    }

    public AbstractRecyclerAdapter(@NonNull Context context, @LayoutRes int resourceId, @NonNull T[] objects) {
        this(context, resourceId, Arrays.asList(objects));
    }

    public AbstractRecyclerAdapter(@NonNull Context context, @LayoutRes int resourceId, @NonNull List<T> objects) {
        this(context, resourceId, objects, null);
    }

    public AbstractRecyclerAdapter(@NonNull Context context, @LayoutRes int resourceId, @NonNull OnItemClickListener listener) {
        this(context, resourceId, new ArrayList<T>(0), listener);
    }

    public AbstractRecyclerAdapter(
            @NonNull Context context, @LayoutRes int resourceId, @NonNull List<T> objects, @Nullable OnItemClickListener listener) {
        mLock = new Object();
        mInflater = LayoutInflater.from(context);
        mResourceId = resourceId;
        mObjects = new ArrayList<>(objects);
        mListener = listener;
    }

    @Override
    public void onBindViewHolder(H holder, int position) {
        holder.position = holder.getAdapterPosition();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public int getItemCount() {
        synchronized (mLock) {
            return mObjects.size();
        }
    }

    public T getItem(int position) {
        synchronized (mLock) {
            return mObjects.get(position);
        }
    }

    public void setItem(int position, @NonNull T item) {
        synchronized (mLock) {
            mObjects.set(position, item);
        }
        notifyItemChanged(position);
    }

    public void add(@Nullable T object) {
        synchronized (mLock) {
            mObjects.add(object);
        }
        notifyItemChanged(mObjects.size());
    }

    public void addAll(@NonNull Collection<? extends T> collection) {
        synchronized (mLock) {
            mObjects.addAll(collection);
        }
        notifyDataSetChanged();
    }

    public void remove(@Nullable T object) {
        synchronized (mLock) {
            mObjects.remove(object);
        }
        notifyDataSetChanged();
    }

    public void clear() {
        synchronized (mLock) {
            mObjects.clear();
        }
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(View item, int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        int position;

        public ViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onItemClick(v, position);
                    }
                }
            });
        }
    }
}
