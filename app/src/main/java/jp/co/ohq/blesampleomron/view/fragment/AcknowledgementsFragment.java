package jp.co.ohq.blesampleomron.view.fragment;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import jp.co.ohq.blesampleomron.R;
import jp.co.ohq.blesampleomron.controller.util.AppLog;

public class AcknowledgementsFragment extends BaseFragment {

    @NonNull
    @Override
    protected String onGetTitle() {
        return getString(R.string.acknowledgements).toUpperCase();
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AppLog.vMethodIn();
        View rootView = inflater.inflate(R.layout.fragment_acknowledgements, container, false);

        Bundle bundle = getArguments();
        String key = bundle.getString("key");

        TextView license_text = (TextView) rootView.findViewById(R.id.license_text);

        AssetManager as = getResources().getAssets();
        String line;
        StringBuilder sb = new StringBuilder();

        if ("Gson".equals(key)) {
            try {
                InputStream iStream = as.open("license_gson.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(iStream, "UTF-8"));

                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if ("nv_bluetooth".equals(key)) {
            try {
                InputStream iStream = as.open("license_nv_bluetooth.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(iStream, "UTF-8"));

                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        license_text.setText(sb);
        return rootView;
    }
}
