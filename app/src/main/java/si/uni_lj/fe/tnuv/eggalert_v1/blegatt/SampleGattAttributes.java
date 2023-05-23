package si.uni_lj.fe.tnuv.eggalert_v1.blegatt;

import java.util.HashMap;

public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String TEMPERATURE = "00002A6E-0000-1000-8000-00805F9B34FB";
    public static String PRESSURE = "00002A6D-0000-1000-8000-00805F9B34FB";
    public static String ML_PREDICT = "19B10003-E8F2-537E-4F6C-D104768A1214";

    public static String EGG_ALERT = "181A";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    static {
        //attributes.put("19B10000-E8F2-537E-4F6C-D104768A1214", "Prediciton service");
        attributes.put("0000181A-0000-1000-8000-00805F9B34FB", "ESS service");
        // Sample Services.
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        // Sample Characteristics.
        //attributes.put(EGG_ALERT,"EggAlertApp");
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put(TEMPERATURE, "Temperature measurement");
        attributes.put(PRESSURE, "Humidity Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
