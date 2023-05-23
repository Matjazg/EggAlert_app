package si.uni_lj.fe.tnuv.eggalert_v1.blegatt;

import android.bluetooth.BluetoothDevice;

public class bleSensorData {
    private Boolean eggPresence;
    private String temperature;
    private String pressure;
    private String timeStamp;

    private BluetoothDevice bleDevice;
    public bleSensorData() {
    }

    public bleSensorData(Boolean eggPresence, String temperature, String pressure, String timeStamp) {
        this.eggPresence = eggPresence;
        this.temperature = temperature;
        this.pressure = pressure;
        this.timeStamp = timeStamp;
    }

    public bleSensorData(Boolean eggPresence, String temperature, String pressure, String timeStamp, BluetoothDevice bleDevice) {
        this.eggPresence = eggPresence;
        this.temperature = temperature;
        this.pressure = pressure;
        this.timeStamp = timeStamp;
        this.bleDevice = bleDevice;
    }

    public boolean getEggPresence() {
        if (eggPresence != null) {
            return eggPresence.booleanValue();
        }
        return false; // Or any default value that makes sense in your context
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

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public BluetoothDevice getBleDevice() {
        return bleDevice;
    }

    public void setBleDevice(BluetoothDevice bleDevice) {
        this.bleDevice = bleDevice;
    }
}
