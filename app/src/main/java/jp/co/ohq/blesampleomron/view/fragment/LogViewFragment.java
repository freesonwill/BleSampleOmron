package jp.co.ohq.blesampleomron.view.fragment;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import jp.co.ohq.blesampleomron.R;
import jp.co.ohq.blesampleomron.controller.util.AppLog;
import jp.co.ohq.blesampleomron.model.service.LogViewService;

public class LogViewFragment extends ListFragment implements View.OnClickListener {

    private LogViewService mLogViewService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            AppLog.vMethodIn();
            mLogViewService = ((LogViewService.LocalBinder) service).getService();
            updateLogView();
        }
        public void onServiceDisconnected(ComponentName name) {
            AppLog.vMethodIn();
            mLogViewService = null;
        }
    };

    public static LogViewFragment newInstance() {
        AppLog.vMethodIn();
        return new LogViewFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        AppLog.vMethodIn();
        super.onCreate(savedInstanceState);
        bindLogService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AppLog.vMethodIn();
        return inflater.inflate(R.layout.fragment_log_view, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        AppLog.vMethodIn();
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btnUpdate).setOnClickListener(this);
        view.findViewById(R.id.btnClear).setOnClickListener(this);
        view.findViewById(R.id.btnLogLevel).setOnClickListener(this);
        view.findViewById(R.id.btnSave).setOnClickListener(this);
        view.findViewById(R.id.btnDeleteLogFile).setOnClickListener(this);
        view.findViewById(R.id.btnSendMail).setOnClickListener(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(getString(R.string.log).toUpperCase());
    }

    @Override
    public void onDestroy() {
        AppLog.vMethodIn();
        super.onDestroy();
        unbindLogService();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        String item = (String) getListAdapter().getItem(position);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setMessage(item);
        alertDialogBuilder.setPositiveButton("OK", null);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void bindLogService() {
        getContext().bindService(new Intent(getActivity(), LogViewService.class),
                mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindLogService() {
        if (null == mLogViewService) {
            return;
        }
        getContext().unbindService(mServiceConnection);
        mLogViewService = null;
    }

    @Override
    public void onClick(View v) {
        AppLog.vMethodIn();
        if (v == v.findViewById(R.id.btnUpdate)) {
            updateLogView();
        } else if (v == v.findViewById(R.id.btnClear)) {
            clearLog();
        } else if (v == v.findViewById(R.id.btnLogLevel)) {
            setLogLevel();
        } else if (v == v.findViewById(R.id.btnSave)) {
            saveLog();
        } else if (v == v.findViewById(R.id.btnDeleteLogFile)) {
            deleteLogAll();
        } else if (v == v.findViewById(R.id.btnSendMail)) {
            sendMail(saveLog());
        }
    }

    private void updateLogView() {
        AppLog.vMethodIn();
        List<String> logList = mLogViewService.getLog();
        ArrayAdapter<String> logListAdapter = new ArrayAdapter<>(getActivity(), R.layout.activity_logview_list_row, logList);
        setListAdapter(logListAdapter);
        getListView().setSelection(this.getListView().getCount() - 1);
    }

    private void clearLog() {
        AppLog.vMethodIn();
        mLogViewService.clearLog();
        ArrayAdapter<String> logListAdapter = new ArrayAdapter<>(getActivity(), R.layout.activity_logview_list_row, new ArrayList<String>(0));
        setListAdapter(logListAdapter);
        getListView().setSelection(this.getListView().getCount() - 1);
    }

    private void setLogLevel() {
        AppLog.vMethodIn();
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select log level");
        builder.setSingleChoiceItems(new String[]{
                LogViewService.LogLevel.Verbose.name(),
                LogViewService.LogLevel.Debug.name(),
                LogViewService.LogLevel.Info.name(),
                LogViewService.LogLevel.Warning.name(),
                LogViewService.LogLevel.Error.name()
        }, mLogViewService.getLogLevel().ordinal(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LogViewService.LogLevel logLevel = LogViewService.LogLevel.values()[which];
                dialog.dismiss();
                mLogViewService.setLogLevel(logLevel);
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    private String saveLog() {
        AppLog.vMethodIn();
        List<String> logList = mLogViewService.getLog();
        File logDir = new File(getDocumentsDirectoryPath(), "/" + getActivity().getPackageName());
        if (!logDir.exists()) {
            if (!logDir.mkdirs()) {
                AppLog.e("Failed to make directory " + logDir);
                Toast.makeText(getActivity(), R.string.save_failed, Toast.LENGTH_SHORT).show();
                return null;
            }
        }
        String fileName = createLogFileName();
        String fullPath = logDir.getPath() + "/" + fileName;
        AppLog.d("Log file:" + fullPath);

        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fullPath), "UTF-8"));
        } catch (IOException ioe) {
            Toast.makeText(getActivity(), R.string.save_failed, Toast.LENGTH_SHORT).show();
            return null;
        }
        try {
            for (String line : logList) {
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(getActivity(), R.string.save_failed, Toast.LENGTH_SHORT).show();
            return null;
        }
        try {
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), R.string.save_failed, Toast.LENGTH_SHORT).show();
            return null;
        }
        Toast.makeText(getActivity(), R.string.save_complete, Toast.LENGTH_SHORT).show();
        Toast.makeText(getActivity(), fileName, Toast.LENGTH_SHORT).show();
        return fullPath;
    }

    private void deleteLogAll() {
        AppLog.vMethodIn();
        File logDir = new File(getDocumentsDirectoryPath(), "/" + getActivity().getPackageName());
        boolean success = deleteLog(logDir);
        if (success) {
            Toast.makeText(getActivity(), R.string.delete_file_complete, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), R.string.delete_file_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean deleteLog(File dirOrFile) {
        AppLog.vMethodIn();
        if (dirOrFile.isDirectory()) {
            String[] children = dirOrFile.list();
            if (null != children) {
                for (String child : children) {
                    boolean success = deleteLog(new File(dirOrFile, child));
                    if (!success) {
                        return false;
                    }
                }
            }
        }
        return dirOrFile.delete();
    }

    private void sendMail(String fileName) {
        AppLog.vMethodIn("fileName:" + fileName);
        if (null == fileName) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_SUBJECT, getActivity().getPackageName() + " log");
        intent.putExtra(Intent.EXTRA_TEXT, "Attached.\r\n");
        Uri uri = FileProvider.getUriForFile(getActivity(), getActivity().getApplicationContext().getPackageName() + ".provider", new File(fileName));
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setType("text/plain");
        startActivity(Intent.createChooser(intent, "Select E-mail application"));
    }

    private String createLogFileName() {
        Calendar cal = Calendar.getInstance();
        String fileName = "log_";
        fileName += String.format(Locale.US, "%1$04d", cal.get(Calendar.YEAR));
        fileName += String.format(Locale.US, "%1$02d", cal.get(Calendar.MONTH) + 1);
        fileName += String.format(Locale.US, "%1$02d", cal.get(Calendar.DATE));
        fileName += String.format(Locale.US, "%1$02d", cal.get(Calendar.HOUR_OF_DAY));
        fileName += String.format(Locale.US, "%1$02d", cal.get(Calendar.MINUTE));
        fileName += String.format(Locale.US, "%1$02d", cal.get(Calendar.SECOND));
        fileName += ".txt";
        return fileName;
    }

    private String getDocumentsDirectoryPath() {
        String ret;
        if (Build.VERSION_CODES.KITKAT > Build.VERSION.SDK_INT) {
            ret = Environment.getExternalStorageDirectory().getPath() + "/Documents";
        } else {
            ret = getExternalStorageDocumentsDirectory();
        }
        return ret;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private String getExternalStorageDocumentsDirectory() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath();
    }
}
