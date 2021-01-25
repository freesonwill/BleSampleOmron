package jp.co.ohq.blesampleomron.model.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Locale;

public class MultipleLineItem implements Parcelable {

    public static final Parcelable.Creator<MultipleLineItem> CREATOR = new Parcelable.Creator<MultipleLineItem>() {
        @Override
        public MultipleLineItem createFromParcel(Parcel source) {
            return new MultipleLineItem(source);
        }

        @Override
        public MultipleLineItem[] newArray(int size) {
            return new MultipleLineItem[size];
        }
    };
    @Nullable
    private String category;
    @Nullable
    private String title;
    @Nullable
    private String summary;

    public MultipleLineItem(@NonNull String category) {
        this.category = category;
    }

    public MultipleLineItem(@NonNull String title, @Nullable String summary) {
        this.title = title;
        this.summary = summary;
    }

    public MultipleLineItem(@NonNull String title, int summary) {
        this.title = title;
        this.summary = String.format(Locale.US, "%d", summary);
    }

    protected MultipleLineItem(Parcel in) {
        this.category = in.readString();
        this.title = in.readString();
        this.summary = in.readString();
    }

    @Nullable
    public String getCategory() {
        return category;
    }

    public void setCategory(@Nullable String category) {
        this.category = category;
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    public void setTitle(@Nullable String title) {
        this.title = title;
    }

    @Nullable
    public String getSummary() {
        return summary;
    }

    public void setSummary(@Nullable String summary) {
        this.summary = summary;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.category);
        dest.writeString(this.title);
        dest.writeString(this.summary);
    }
}
