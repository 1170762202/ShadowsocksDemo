package com.github.shadowsocks.bg;

import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.format.Formatter;

import com.github.shadowsocks.Core;
import com.github.shadowsocks.aidl.IShadowsocksServiceCallback;
import com.github.shadowsocks.aidl.TrafficStats;
import com.github.shadowsocks.core.R;
import com.github.shadowsocks.utils.Action;

import kotlin.TypeCastException;

/**
 * @author 陈志鹏
 * @date 2019/2/25
 */
public class ServiceNotification{

    private BaseService.Interface service;
    private String profileName;
    private String channel;
    private boolean visible;

    private KeyguardManager keyGuard;
    private NotificationManager nm;
    private IShadowsocksServiceCallback callback = new IShadowsocksServiceCallback.Stub() {
        @Override
        public void stateChanged(int state, String profileName, String msg) throws RemoteException {

        }

        @Override
        public void trafficUpdated(long profileId, TrafficStats stats) throws RemoteException {
            if (profileId != 0L) {
                return;

            }
            Context context = (Context) service;
            String txr = context.getString(R.string.speed, Formatter.formatFileSize(context, stats.getTxRate()));
            String rxr = context.getString(R.string.speed, Formatter.formatFileSize(context, stats.getRxRate()));
            builder.setContentText((CharSequence) (txr + "↑\t" + rxr + '↓'));
            style.bigText(context.getString(R.string.stat_summary, txr, rxr,
                    Formatter.formatFileSize(context, stats.getTxTotal()),
                    Formatter.formatFileSize(context, stats.getRxTotal())));
            show();
        }

        @Override
        public void trafficPersisted(long profileId) throws RemoteException {

        }
    };
    private BroadcastReceiver lockReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            update(intent.getAction(), false);
        }
    };
    private boolean callbackRegistered = false;

    private NotificationCompat.Builder builder;

    private NotificationCompat.BigTextStyle style;
    private boolean isVisible = true;

    public ServiceNotification(BaseService.Interface service, String profileName, String channel) {
        this(service, profileName, channel, false);
    }

    public ServiceNotification(BaseService.Interface service, String profileName, String channel, boolean visible) {
        this.service = service;
        this.profileName = profileName;
        this.channel = channel;
        this.visible = visible;

        keyGuard = (KeyguardManager) ContextCompat.getSystemService((Context) service, KeyguardManager.class);
        nm = (NotificationManager) ContextCompat.getSystemService((Context) service, NotificationManager.class);
        builder = new NotificationCompat.Builder((Context) service, channel)
                .setWhen(0)
                .setColor(ContextCompat.getColor((Context) service, R.color.material_primary_500))
                .setTicker(((Context) service).getString(R.string.forward_success))
                .setContentTitle(profileName)
                .setContentIntent((PendingIntent) Core.configureIntent.invoke((Context) service))
                .setSmallIcon(R.drawable.ic_service_active);
        style = new NotificationCompat.BigTextStyle(builder).bigText("");

        Context context = (Context) service;
        if (Build.VERSION.SDK_INT < 24) {
            builder.addAction(R.drawable.ic_navigation_close,
                    context.getString(R.string.stop), PendingIntent.getBroadcast(context, 0, new Intent(Action.CLOSE), 0));
        }

        update(ContextCompat.getSystemService(context, PowerManager.class).isInteractive() != false ?
                Intent.ACTION_SCREEN_ON : Intent.ACTION_SCREEN_OFF, true);
        IntentFilter screenFilter = new IntentFilter();
        screenFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
        if (visible && Build.VERSION.SDK_INT < 26) {
            screenFilter.addAction(Intent.ACTION_USER_PRESENT);
        }
        context.registerReceiver(lockReceiver, screenFilter);
    }

    private void update(String action, boolean forceShow) {
        if (forceShow || service.getData().getState() == BaseService.CONNECTED) {
            switch (action) {
                case Intent.ACTION_SCREEN_OFF:
                    setVisible(false, forceShow);
                    unregisterCallback();
                    break;
                case Intent.ACTION_SCREEN_ON:
                    setVisible(visible && !keyGuard.isKeyguardLocked(), forceShow);
                    try {
                        service.getData().getBinder().registerCallback(callback);
                        service.getData().getBinder().startListeningForBandwidth(callback);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    callbackRegistered = true;
                    break;
                case Intent.ACTION_USER_PRESENT:
                    setVisible(true, forceShow);
                    break;
                default:
                    break;
            }
        }
    }

    private void unregisterCallback() {
        if (this.callbackRegistered) {
            try {
                this.service.getData().getBinder().unregisterCallback(callback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            this.callbackRegistered = false;
        }
    }

    private void setVisible(boolean visible, boolean forceShow) {
        if (this.isVisible != visible) {
            this.isVisible = visible;
            builder.setPriority(visible ? NotificationCompat.PRIORITY_LOW : NotificationCompat.PRIORITY_MIN);
            this.show();
        } else if (forceShow) {
            this.show();
        }
    }

    private void show() {
        if (this.service == null) {
            throw new TypeCastException("null cannot be cast to non-null type android.app.Service");
        } else {
            ((Service) service).startForeground(1, this.builder.build());
        }
    }

    public final void destroy() {
        if (this.service == null) {
            throw new TypeCastException("null cannot be cast to non-null type android.app.Service");
        } else {
            ((Service) service).unregisterReceiver(this.lockReceiver);
            this.unregisterCallback();
            ((Service) this.service).stopForeground(true);
            nm.cancel(1);
        }
    }
}
