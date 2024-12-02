//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.mg.winkl.service;

import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE;

import android.app.Notification;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobInfo.Builder;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.mg.winkl.KeepLive;
import com.mg.winkl.config.NotificationUtils;
import com.mg.winkl.receiver.NotificationClickReceiver;
import com.mg.winkl.utils.ServiceUtils;

@RequiresApi(api = 21)
public final class JobHandlerService extends JobService {
    private JobScheduler mJobScheduler;
    private int jobId = 100;

    public JobHandlerService() {
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        this.startService(this);

        if (VERSION.SDK_INT >= 21) {
            this.mJobScheduler = (JobScheduler) this.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            this.mJobScheduler.cancel(this.jobId);
            Builder builder = new Builder(this.jobId, new ComponentName(this.getPackageName(), JobHandlerService.class.getName()));
            if (VERSION.SDK_INT >= 24) {
                builder.setMinimumLatency(5000L);
                builder.setOverrideDeadline(5000L);
                builder.setMinimumLatency(5000L);
                builder.setBackoffCriteria(5000L, JobInfo.BACKOFF_POLICY_LINEAR);
            } else {
                builder.setPeriodic(5000L);
            }

            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
            builder.setPersisted(true);
            this.mJobScheduler.schedule(builder.build());
        }

        return Service.START_STICKY;
    }

    private void startService(Context context) {
        Intent localIntent;
        if (VERSION.SDK_INT >= 26 && KeepLive.foregroundNotification != null) {
            localIntent = new Intent(this.getApplicationContext(), NotificationClickReceiver.class);
            localIntent.setAction("CLICK_NOTIFICATION");
            Notification notification = NotificationUtils.createNotification(this, KeepLive.foregroundNotification.getTitle(), KeepLive.foregroundNotification.getDescription(), KeepLive.foregroundNotification.getIconRes(), localIntent);

            if (VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(2, notification, FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
            }else {
                this.startForeground(13691, notification);
            }
        }

        localIntent = new Intent(context, LocalService.class);
        Intent guardIntent = new Intent(context, RemoteService.class);
        this.startService(localIntent);
        this.startService(guardIntent);
    }

    public boolean onStartJob(JobParameters jobParameters) {
        if (!ServiceUtils.isServiceRunning(this.getApplicationContext(), "com.mg.winkl.service.LocalService") || !ServiceUtils.isRunningTaskExist(this.getApplicationContext(), this.getPackageName() + ":remote")) {
            this.startService(this);
        }

        return false;
    }

    public boolean onStopJob(JobParameters jobParameters) {
        if (!ServiceUtils.isServiceRunning(this.getApplicationContext(), "com.mg.winkl.service.LocalService") || !ServiceUtils.isRunningTaskExist(this.getApplicationContext(), this.getPackageName() + ":remote")) {
            this.startService(this);
        }

        return false;
    }
}
