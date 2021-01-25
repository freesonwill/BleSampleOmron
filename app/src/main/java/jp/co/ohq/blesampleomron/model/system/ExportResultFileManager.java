package jp.co.ohq.blesampleomron.model.system;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jp.co.ohq.blesampleomron.controller.util.AppLog;
import jp.co.ohq.blesampleomron.model.entity.HistoryData;
import jp.co.ohq.utility.StringEx;

public class ExportResultFileManager {

    private static final String TAG = "ExportResultFileManager";
    private static final String OVERLAP_RENAME_CHAR = "_";
    private static byte[] buf = new byte[1024];
    @Nullable
    private static ExportResultFileManager sInstance;

    private ExportResultFileManager(@NonNull Context context) {
    }

    public static ExportResultFileManager init(@NonNull Context context) {
        if (null != sInstance) {
            throw new IllegalStateException("An instance has already been created.");
        }
        return sInstance = new ExportResultFileManager(context);
    }

    public static ExportResultFileManager sharedInstance() {
        if (null == sInstance) {
            throw new IllegalStateException("Instance has not been created.");
        }
        return sInstance;
    }

    private static String CompressZipFromFiles(File[] files, String outFilePath, OverlapActionEnum overlapAction) {

        File outFile = createOutFile(outFilePath, overlapAction);
        ZipOutputStream zos = null;
        FileOutputStream fos = null;
        FileInputStream fis = null;

        try {
            if (outFile != null) {
                fos = new FileOutputStream(outFile);
                zos = new ZipOutputStream(fos);

                if (files != null) {
                    CompressZip(zos, fis, files, "");
                }
                zos.closeEntry();
                return outFile.getAbsolutePath();
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException");
        } catch (IOException e) {
            Log.e(TAG, "IOException");
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    Log.e(TAG, "IOException:" + e.getMessage());
                }
            }
            if (zos != null) {
                try {
                    zos.close();
                } catch (IOException e) {
                    Log.e(TAG, "IOException:" + e.getMessage());
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.e(TAG, "IOException:" + e.getMessage());
                }
            }
        }
        return null;
    }

    private static void CompressZip(ZipOutputStream zos, FileInputStream fis, File[] files, String currentFolder) throws IOException {
        for (File file : files) {
            if (file.isDirectory()) {
                if (file.listFiles().length == 0) {
                    zos.putNextEntry(new ZipEntry(currentFolder + file.getName() + File.separator));
                } else {
                    String subFolder = currentFolder + file.getName() + File.separator;
                    CompressZip(zos, fis, file.listFiles(), subFolder);
                }
            } else {
                fis = new FileInputStream(file);
                zos.putNextEntry(new ZipEntry(currentFolder + file.getName()));

                int length;
                while ((length = fis.read(buf)) > 0) {
                    zos.write(buf, 0, length);
                }
            }
        }
    }

    private static File createOutFile(String outFilePath, OverlapActionEnum overlapAction) {
        File outFile = new File(outFilePath);
        boolean loop = true;
        while (loop && outFile.exists()) {
            switch (overlapAction) {
                case OVERLAP_ACTION_INTERRUPT:
                    return null;

                case OVERLAP_ACTION_OVERWRITE:
                    loop = false;
                    break;

                case OVERLAP_ACTION_RENAME:
                    StringBuilder sb = new StringBuilder();
                    String name = outFile.getName();
                    int index = name.lastIndexOf('.');
                    if (index != -1) {
                        sb.append(name.substring(0, index));
                        sb.append(OVERLAP_RENAME_CHAR);
                        sb.append(name.substring(index));
                    } else {
                        sb.append(name);
                        sb.append(OVERLAP_RENAME_CHAR);
                    }
                    outFile = new File(outFile.getParent() + File.separator + sb.toString());
                    break;
            }
        }
        return outFile;
    }

    // @return output file path
    public String ExportCompressedFile(@NonNull Context context, HistoryData historyData, OverlapActionEnum overlapAction) {
        File resultFileDirectory = new File(getResultFileDirectoryPath(context, historyData));
        String pathOutFile = getResultDirectoryPath(context) + "/" + getOutFileName(historyData);
        if (!resultFileDirectory.exists()) {
            return null;
        }

        if (resultFileDirectory.isDirectory()) {
            File[] files = resultFileDirectory.listFiles();
            return CompressZipFromFiles(files, pathOutFile, overlapAction);
        }

        return null;
    }

    private String getResultFileDirectoryPath(@NonNull Context context, HistoryData historyData) {
        return getResultDirectoryPath(context) + "/" + StringEx.toDateString(historyData.getReceivedDate(), StringEx.FormatType.Form3);
    }

    private String getResultDirectoryPath(@NonNull Context context) {
        return getAppDirectoryPath(context) + "/RESULT";
    }

    private String getAppDirectoryPath(@NonNull Context context) {
        return AppConfig.sharedInstance().getExternalApplicationDirectoryPath(context);
    }

    private String getFolderName(HistoryData historyData) {
        return StringEx.toDateString(historyData.getReceivedDate(), StringEx.FormatType.Form3);
    }

    private String getOutFileName(HistoryData historyData) {
        return getFolderName(historyData) + ".zip";
    }

    public void deleteTempFile(String pathFile) {
        File f = new File(pathFile);
        if (!f.exists()) {
            return;
        }
        if (f.isFile()) {
            if (!f.delete()) {
                AppLog.e("Delete file failed.");
            }
        }
    }

    public enum OverlapActionEnum {
        OVERLAP_ACTION_INTERRUPT,
        OVERLAP_ACTION_OVERWRITE,
        OVERLAP_ACTION_RENAME
    }

}
