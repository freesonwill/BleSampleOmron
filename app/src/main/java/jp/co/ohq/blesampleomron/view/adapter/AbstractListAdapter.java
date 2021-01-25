package jp.co.ohq.blesampleomron.view.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class AbstractListAdapter<T> extends BaseAdapter {

    @NonNull
    private final Context mContext;
    @NonNull
    private final Object mLock = new Object();
    @NonNull
    private List<T> mObjects;

    public AbstractListAdapter(@NonNull Context context) {
        this(context, new ArrayList<T>());
    }

    public AbstractListAdapter(@NonNull Context context, @NonNull T[] items) {
        this(context, Arrays.asList(items));
    }

    public AbstractListAdapter(@NonNull Context context, @NonNull List<T> items) {
        super();
        mContext = context;
        mObjects = items;
    }

    @NonNull
    public Context getContext() {
        return mContext;
    }

    public void add(@Nullable T object) {
        synchronized (mLock) {
            mObjects.add(object);
        }
        notifyDataSetChanged();
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

    @Override
    public int getCount() {
        return mObjects.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    @NonNull
    public T getItem(int position) throws IndexOutOfBoundsException {
        return mObjects.get(position);
    }

    public void setItem(int position, @NonNull T item) throws IndexOutOfBoundsException {
        mObjects.set(position, item);
        notifyDataSetChanged();
    }

    @NonNull
    public List<T> getItems() {
        return mObjects;
    }

    public boolean hasItems() {
        return 0 < mObjects.size();
    }

    public int getPosition(@NonNull T item) {
        return mObjects.indexOf(item);
    }

    @Override
    @NonNull
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (null == convertView) {
            convertView = onCreateView(position, parent);
        }
        onBindView(position, convertView);
        return convertView;
    }

    @NonNull
    abstract protected View onCreateView(int position, @NonNull ViewGroup parent);

    abstract protected void onBindView(int position, @NonNull View view);
}
