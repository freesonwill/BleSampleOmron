package jp.co.ohq.blesampleomron.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AndroidRuntimeException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import io.realm.Realm;
import jp.co.ohq.ble.enumerate.OHQGender;
import jp.co.ohq.blesampleomron.R;
import jp.co.ohq.blesampleomron.controller.util.AppLog;
import jp.co.ohq.blesampleomron.model.entity.UserInfo;
import jp.co.ohq.blesampleomron.model.system.AppConfig;
import jp.co.ohq.blesampleomron.view.adapter.AbstractListAdapter;

public class UserSelectionFragment extends BaseFragment
        implements AdapterView.OnItemClickListener {

    private Realm mRealm;
    private EventListener mListener;
    private ListAdapter mListAdapter;

    public static UserSelectionFragment newInstance() {
        return new UserSelectionFragment();
    }

    @Override
    public void onAttach(Context context) {
        AppLog.vMethodIn();
        super.onAttach(context);
        if (!(context instanceof EventListener)) {
            throw new AndroidRuntimeException("Activity is must be implement 'EventListener'");
        }
        mListener = (EventListener) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRealm = Realm.getDefaultInstance();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AppLog.vMethodIn();
        View rootView = inflater.inflate(R.layout.fragment_user_selection, container, false);

        ListView list = (ListView) rootView.findViewById(R.id.list);
        list.setOnItemClickListener(this);
        View footerView = View.inflate(getActivity(), R.layout.list_item_guest, null);
        list.addFooterView(footerView);
        mListAdapter = new ListAdapter(getContext(), mRealm.where(UserInfo.class).findAll());
        list.setAdapter(mListAdapter);
        ImageView imageView = (ImageView) rootView.findViewById(R.id.user_icon);
        if (AppConfig.sharedInstance().getNameOfCurrentUser().equals(AppConfig.GUEST)) {
            imageView.setImageResource(R.drawable.img_anonymous_black_100dp);
        }

        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AppLog.vMethodIn();
        Bundle args = new Bundle();
        UserInfo userInfo;

        switch (view.getId()) {
            case R.id.detailInfoButton:
                userInfo = mListAdapter.getItem(position);
                replaceFragmentWithAddingToBackStack(android.R.id.content, UserProfileFragment.newInstance(userInfo.getName()));
                break;
            case R.id.guest:
                args.putString(Arg.UserName.name(), AppConfig.GUEST);
                mListener.onUserSelectionFragmentEvent(Event.UserSelected, args);
                break;
            default:
                userInfo = mListAdapter.getItem(position);
                args.putString(Arg.UserName.name(), userInfo.getName());
                mListener.onUserSelectionFragmentEvent(Event.UserSelected, args);
                break;
        }
    }

    @NonNull
    @Override
    protected String onGetTitle() {
        return getString(R.string.select_user).toUpperCase();
    }

    public enum Event {
        UserSelected,
    }

    public enum Arg {
        UserName,
    }

    public interface EventListener {
        void onUserSelectionFragmentEvent(@NonNull Event event, @NonNull Bundle args);
    }

    private static class ListAdapter extends AbstractListAdapter<UserInfo> {

        ListAdapter(@NonNull Context context, @NonNull List<UserInfo> items) {
            super(context, items);
        }

        @NonNull
        @Override
        protected View onCreateView(int position, @NonNull ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.list_item_user, parent, false);

            ViewHolder holder = new ViewHolder();
            holder.userName = (TextView) view.findViewById(R.id.user_name);
            holder.intoButton = (ImageButton) view.findViewById(R.id.detailInfoButton);
            holder.userIcon = (ImageView) view.findViewById(R.id.user_icon);

            final ListView parentList = (ListView) parent;
            holder.intoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = (Integer) view.getTag();
                    parentList.performItemClick(view, position, view.getId());
                }
            });

            view.setTag(holder);

            return view;
        }

        @Override
        protected void onBindView(final int position, @NonNull View view) {
            UserInfo userInfo = getItem(position);
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.userName.setText(userInfo.getName());
            holder.intoButton.setTag(position);
            holder.userIcon.setImageResource(OHQGender.Male == userInfo.getGender() ? R.drawable.img_male_black_100dp : R.drawable.img_female_black_100dp);
        }

        private static class ViewHolder {
            TextView userName;
            ImageButton intoButton;
            ImageView userIcon;
        }
    }
}
