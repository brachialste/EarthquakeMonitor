package com.brachialste.earthquakemonitor.web.error;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Looper;
import android.os.StatFs;
import android.util.Log;

import com.brachialste.earthquakemonitor.R;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.Locale;
import java.lang.Thread.UncaughtExceptionHandler;

/**
 * Created by brachialste on 17/02/14.
 */
public class UnCaughtException implements UncaughtExceptionHandler{
    private Context context;
    private static Context context1;

    public UnCaughtException(Context ctx) {
            context = ctx;
            context1 = ctx;
    }

    private StatFs getStatFs() {
        File path = Environment.getDataDirectory();
        return new StatFs(path.getPath());
    }

    private long getAvailableInternalMemorySize(StatFs stat) {
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    private long getTotalInternalMemorySize(StatFs stat) {
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    private void addInformation(StringBuilder message) {
            message.append(context.getString(R.string.inf_0) + " ").append(Locale.getDefault()).append('\n');
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi;
            pi = pm.getPackageInfo(context.getPackageName(), 0);
            message.append(context.getString(R.string.inf_1) + " ").append(pi.versionName).append('\n');
            message.append(context.getString(R.string.inf_2) + " ").append(pi.packageName).append('\n');
        } catch (Exception e) {
            Log.e("CustomExceptionHandler", "Error", e);
            message.append(context.getString(R.string.err_3) + " ").append(
            context.getPackageName());
        }

        message.append(context.getString(R.string.inf_3) + " ").append(android.os.Build.MODEL)
        .append('\n');
        message.append(context.getString(R.string.inf_4) + " ")
        .append(android.os.Build.VERSION.RELEASE).append('\n');
        message.append(context.getString(R.string.inf_5) + " ").append(android.os.Build.BOARD).append('\n');
        message.append(context.getString(R.string.inf_6) + " ").append(android.os.Build.BRAND).append('\n');
        message.append(context.getString(R.string.inf_7) + " ").append(android.os.Build.DEVICE).append('\n');
        message.append(context.getString(R.string.inf_8) + " ").append(android.os.Build.HOST).append('\n');
        message.append(context.getString(R.string.inf_9) + " ").append(android.os.Build.ID).append('\n');
        message.append(context.getString(R.string.inf_10) + " ").append(android.os.Build.MODEL).append('\n');
        message.append(context.getString(R.string.inf_11) + " ").append(android.os.Build.PRODUCT)
        .append('\n');
        message.append(context.getString(R.string.inf_12) + " ").append(android.os.Build.TYPE).append('\n');
        StatFs stat = getStatFs();
        message.append(context.getString(R.string.inf_13) + " ")
        .append(getTotalInternalMemorySize(stat)).append('\n');
        message.append(context.getString(R.string.inf_14) + " ")
        .append(getAvailableInternalMemorySize(stat)).append('\n');
    }

    public void uncaughtException(Thread t, Throwable e) {
        try {
            StringBuilder report = new StringBuilder();
            Date curDate = new Date();
            report.append(context.getString(R.string.err_title) + " ")
            .append(curDate.toString()).append('\n').append('\n');
            report.append(context.getString(R.string.err_device)).append('\n');
            addInformation(report);
            report.append('\n').append('\n');
            report.append(context.getString(R.string.err_stack) + "\n");
            final Writer result = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(result);
            e.printStackTrace(printWriter);
            report.append(result.toString());
            printWriter.close();
            report.append('\n');
            report.append(context.getString(R.string.err_end));
            Log.e(UnCaughtException.class.getName(),
            context.getString(R.string.err_1) + "\n" +report);
            sendErrorMail(report);
        } catch (Throwable ignore) {
            Log.e(UnCaughtException.class.getName(),
                    context.getString(R.string.err_2), ignore);
        }
    }

    /**
     * Este método llama al diálogo de alerta cuando la aplicación falla
     */
    public void sendErrorMail(final StringBuilder errorContent) {
    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            new Thread() {
        @Override
        public void run() {
            Looper.prepare();
            builder.setTitle(context.getString(R.string.err_msg1));
            builder.create();
            builder.setNegativeButton(context.getString(R.string.err_btn2),
            new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog,
            int which) {
            // matamos cualquier resto
            android.os.Process.sendSignal(android.os.Process.myPid(), android.os.Process.SIGNAL_KILL);
            android.os.Process.killProcess(android.os.Process.myPid());
            // finaliza la aplicación
            System.runFinalizersOnExit(true);
            System.exit(0);
            }
            });
            builder.setPositiveButton(context.getString(R.string.err_btn1),
            new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog,
            int which) {
            Intent sendIntent = new Intent(
            Intent.ACTION_SEND);
            String subject = context.getString(R.string.err_msg2);
            StringBuilder body = new StringBuilder(context.getString(R.string.app_name));
            body.append('\n').append('\n');
            body.append(errorContent).append('\n')
            .append('\n');
            // sendIntent.setType("text/plain");
            sendIntent.setType("message/rfc822");
            sendIntent.putExtra(Intent.EXTRA_EMAIL,
            new String[] { context.getString(R.string.err_mail) });
            sendIntent.putExtra(Intent.EXTRA_TEXT,
            body.toString());
            sendIntent.putExtra(Intent.EXTRA_SUBJECT,
            subject);
            sendIntent.setType("message/rfc822");
            context1.startActivity(sendIntent);
            // matamos cualquier resto
            android.os.Process.sendSignal(android.os.Process.myPid(), android.os.Process.SIGNAL_KILL);
            android.os.Process.killProcess(android.os.Process.myPid());
            // finaliza la aplicación
            System.runFinalizersOnExit(true);
            System.exit(0);
            }
            });
            builder.setMessage(context.getString(R.string.err_msg3));
            builder.show();
            Looper.loop();
            }
        }.start();
    }
}
