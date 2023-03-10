//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package moe.shizuku.api;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ParcelFileDescriptor.AutoCloseInputStream;
import android.os.ParcelFileDescriptor.AutoCloseOutputStream;
import android.os.Parcelable.Creator;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import moe.shizuku.server.IRemoteProcess;
import moe.shizuku.server.IRemoteProcess.Stub;

public class RemoteProcess extends Process implements Parcelable {
    private final IRemoteProcess mRemote;
    public static final Creator<RemoteProcess> CREATOR = new Creator<RemoteProcess>() {
        public RemoteProcess createFromParcel(Parcel in) {
            return new RemoteProcess(in);
        }

        public RemoteProcess[] newArray(int size) {
            return new RemoteProcess[size];
        }
    };

    RemoteProcess(IRemoteProcess remote) {
        this.mRemote = remote;
    }

    public OutputStream getOutputStream() {
        try {
            return new AutoCloseOutputStream(this.mRemote.getOutputStream());
        } catch (RemoteException var2) {
            throw new RuntimeException(var2);
        }
    }

    public InputStream getInputStream() {
        try {
            return new AutoCloseInputStream(this.mRemote.getInputStream());
        } catch (RemoteException var2) {
            throw new RuntimeException(var2);
        }
    }

    public InputStream getErrorStream() {
        try {
            return new AutoCloseInputStream(this.mRemote.getErrorStream());
        } catch (RemoteException var2) {
            throw new RuntimeException(var2);
        }
    }

    public int waitFor() throws InterruptedException {
        try {
            return this.mRemote.waitFor();
        } catch (RemoteException var2) {
            throw new RuntimeException(var2);
        }
    }

    public int exitValue() {
        try {
            return this.mRemote.exitValue();
        } catch (RemoteException var2) {
            throw new RuntimeException(var2);
        }
    }

    public void destroy() {
        try {
            this.mRemote.destroy();
        } catch (RemoteException var2) {
            throw new RuntimeException(var2);
        }
    }

    public boolean alive() {
        try {
            return this.mRemote.alive();
        } catch (RemoteException var2) {
            throw new RuntimeException(var2);
        }
    }

    public boolean waitForTimeout(long timeout, TimeUnit unit) throws InterruptedException {
        try {
            return this.mRemote.waitForTimeout(timeout, unit.toString());
        } catch (RemoteException var5) {
            throw new RuntimeException(var5);
        }
    }

    public IBinder asBinder() {
        return this.mRemote.asBinder();
    }

    private RemoteProcess(Parcel in) {
        this.mRemote = Stub.asInterface(in.readStrongBinder());
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStrongBinder(this.mRemote.asBinder());
    }
}
