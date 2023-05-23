package si.uni_lj.fe.tnuv.eggalert_v1.Hatcheries;

import si.uni_lj.fe.tnuv.eggalert_v1.blegatt.bleSensorData;

public interface HatcheryStateListener {
    void onHatcheryStateChanged(bleSensorData sensorData);
}