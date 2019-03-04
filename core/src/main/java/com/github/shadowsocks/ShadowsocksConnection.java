package com.github.shadowsocks;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.Fragment;

import com.github.shadowsocks.aidl.IShadowsocksService;
import com.github.shadowsocks.aidl.IShadowsocksServiceCallback;
import com.github.shadowsocks.bg.BaseService;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.WeakHashMap;

import kotlin.jvm.internal.Intrinsics;

/**
 * @author 陈志鹏
 * @date 2019/2/27
 */
public class ShadowsocksConnection implements ServiceConnection {

    private Interface instance;
    private static final WeakHashMap connections = new WeakHashMap<Interface, ShadowsocksConnection>();
    private boolean connectionActive = false;
    private boolean callbackRegistered = false;
    private IBinder binder;
    private Context context;
    private boolean listeningForBandwidth = false;
    @Nullable
    private IShadowsocksService service;

    public ShadowsocksConnection(Interface instance) {
        this.instance = instance;

        if (instance instanceof Fragment) {
            context = ((Fragment) instance).getActivity();
        } else {
            context = (Context) instance;
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        this.binder = binder;
        if (this.instance.getListenForDeath()) {
            try {
                binder.linkToDeath(this.instance, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        IShadowsocksService var10000 = IShadowsocksService.Stub.asInterface(binder);
        if (var10000 == null) {
            Intrinsics.throwNpe();
        }

        IShadowsocksService service = var10000;
        this.service = service;
        if (this.instance.getServiceCallback() != null && !this.callbackRegistered) {
            try {
                service.registerCallback(this.instance.getServiceCallback());
                this.callbackRegistered = true;
                if (this.listeningForBandwidth) {
                    service.startListeningForBandwidth(this.instance.getServiceCallback());
                }
            } catch (RemoteException var5) {
                ;
            }
        }

        this.instance.onServiceConnected(service);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        this.unregisterCallback();
        this.instance.onServiceDisconnected();
        this.service = null;
        this.binder = null;
    }

    private final void unregisterCallback() {
        IShadowsocksService service = this.service;
        if (service != null && this.instance.getServiceCallback() != null && this.callbackRegistered) {
            try {
                service.unregisterCallback(this.instance.getServiceCallback());
            } catch (RemoteException var3) {
                ;
            }
        }
        this.callbackRegistered = false;
    }

    public interface Interface extends IBinder.DeathRecipient {
        @Nullable
        IShadowsocksServiceCallback getServiceCallback();

        @NotNull
        ShadowsocksConnection getConnection();

        boolean getListenForDeath();

        void onServiceConnected(@NotNull IShadowsocksService var1);

        void onServiceDisconnected();

        @Override
        void binderDied();

        public static final class DefaultImpls {
            @Nullable
            public static IShadowsocksServiceCallback getServiceCallback(ShadowsocksConnection.Interface $this) {
                return null;
            }

            @NotNull
            public static ShadowsocksConnection getConnection(ShadowsocksConnection.Interface $this) {
                Object value$iv = ShadowsocksConnection.connections.get($this);
                Object var10000;
                if (value$iv == null) {
                    Object answer$iv = new ShadowsocksConnection($this);
                    ShadowsocksConnection.connections.put($this, answer$iv);
                    var10000 = answer$iv;
                } else {
                    var10000 = value$iv;
                }

                return (ShadowsocksConnection) var10000;
            }

            public static boolean getListenForDeath(ShadowsocksConnection.Interface $this) {
                return false;
            }

            public static void onServiceConnected(ShadowsocksConnection.Interface $this, @NotNull IShadowsocksService service) {

            }

            public static void onServiceDisconnected(ShadowsocksConnection.Interface $this) {
            }

            public static void binderDied(ShadowsocksConnection.Interface $this) {
                $this.getConnection().setService(null);
            }
        }
    }

    public final void connect() {
        if (!this.connectionActive) {
            this.connectionActive = true;
            Intent intent = (new Intent(getContext(), BaseService.getServiceClass())).setAction("com.github.shadowsocks.SERVICE");
            getContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
        }
    }

    public void setListeningForBandwidth(boolean value) {
        if (listeningForBandwidth != value && service != null && instance.getServiceCallback() != null){
            if (value) {
                try {
                    service.startListeningForBandwidth(instance.getServiceCallback());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    service.stopListeningForBandwidth(instance.getServiceCallback());
                } catch (DeadObjectException e) {
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        listeningForBandwidth = value;
    }

    public Context getContext() {
        return context;
    }

    public final void disconnect() {
        this.unregisterCallback();
        this.instance.onServiceDisconnected();
        if (this.connectionActive) {
            try {
                this.getContext().unbindService(this);
            } catch (IllegalArgumentException var2) {
                ;
            }
        }

        this.connectionActive = false;
        if (this.instance.getListenForDeath()) {
            IBinder var10000 = this.binder;
            if (this.binder != null) {
                var10000.unlinkToDeath(this.instance, 0);
            }
        }

        this.binder = null;
        if (this.instance.getServiceCallback() != null) {
            IShadowsocksService var3 = this.service;
            if (this.service != null) {
                try {
                    var3.stopListeningForBandwidth(this.instance.getServiceCallback());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        this.service = null;
    }

    public void setService(@Nullable IShadowsocksService service) {
        this.service = service;
    }
}
