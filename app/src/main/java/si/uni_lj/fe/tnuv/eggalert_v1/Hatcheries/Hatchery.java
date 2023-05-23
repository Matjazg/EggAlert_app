package si.uni_lj.fe.tnuv.eggalert_v1.Hatcheries;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

import si.uni_lj.fe.tnuv.eggalert_v1.blegatt.BluetoothLeService;

public class Hatchery implements Parcelable {
    private String name;
    private Boolean eggPresence;
    private String temperature;
    private String pressure;

    private String timeStamp;
    private Boolean connectionStatus;

    private String bleDeviceAddress;
/*
    public Hatchery(String name, Boolean eggPresence, String temperature, String pressure, Boolean connectionStatus) {
        this.name = name;
        this.eggPresence = eggPresence;
        this.temperature = temperature;
        this.pressure = pressure;
        this.connectionStatus = connectionStatus;
    }*/

    public Hatchery(String name, Boolean connectionStatus, String bleDeviceAddress) {
        this.name = name;
        this.connectionStatus = connectionStatus;
        this.bleDeviceAddress = bleDeviceAddress;
        this.eggPresence = false;
        this.temperature = "-1";
        this.pressure = "-1";
    }

    protected Hatchery(Parcel in) {
        name = in.readString();
        eggPresence = in.readByte() != 0;
        temperature = in.readString();
        pressure = in.readString();
        connectionStatus = in.readByte() != 0;
    }
    public static final Creator<Hatchery> CREATOR = new Creator<Hatchery>() {
        @Override
        public Hatchery createFromParcel(Parcel in) {
            return new Hatchery(in);
        }

        @Override
        public Hatchery[] newArray(int size) {
            return new Hatchery[size];
        }
    };
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getEggPresence() {
        return eggPresence;
    }

    public void setEggPresence(Boolean eggPresence) {
        this.eggPresence = eggPresence;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getPressure() {
        return pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public Boolean getConnectionStatus() {
        return connectionStatus;
    }


    public String getBleDeviceAddress() {
        return bleDeviceAddress;
    }

    public void setBleDeviceAddress(String bleDeviceAddress) {
        this.bleDeviceAddress = bleDeviceAddress;
    }

    public void setConnectionStatus(Boolean connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeByte((byte) (eggPresence ? 1 : 0));
        dest.writeString(temperature);
        dest.writeString(pressure);
        dest.writeByte((byte) (connectionStatus ? 1 : 0));


    }
    public int describeContents() {
        return 0;
    }
}
