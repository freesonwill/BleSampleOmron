package jp.co.ohq.ble.advertising;


import com.neovisionaries.bluetooth.ble.advertising.ADManufacturerSpecific;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import jp.co.ohq.utility.Bytes;


public class EachUserData extends ADManufacturerSpecific {
    private static final int ENABLE_DATA_TYPE = 0x01;
    private static final int DATA_TYPE_INDEX = 2;
    private static final int FLAGS_INDEX = 3;
    private static final int USERS_INDEX = 4;
    private final List<User> users = new LinkedList<>();
    private int numberOfUser;
    private boolean isTimeNotSet;
    private boolean isPairingMode;
    private EachUserData(int length, int type, byte[] data, int companyId) {
        super(length, type, data, companyId);
        parse(data);
    }

    /**
     * Create an {@link EachUserData} instance.
     */
    public static EachUserData create(int length, int type, byte[] data, int companyId) {
        if (data == null) {
            return null;
        }
        return new EachUserData(length, type, data, companyId);
    }

    public int getNumberOfUser() {
        return numberOfUser;
    }

    public boolean isTimeNotSet() {
        return isTimeNotSet;
    }

    public boolean isPairingMode() {
        return isPairingMode;
    }

    public List<User> getUsers() {
        return users;
    }

    private void parse(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("The byte sequence cannot be parsed as an Each UserDataKey Data.");
        }
        int datatyoe = data[DATA_TYPE_INDEX];
        if (datatyoe != ENABLE_DATA_TYPE) {
            return;
        }
        byte flags = data[FLAGS_INDEX];
        numberOfUser = (flags & 0x03) + 1;
        isTimeNotSet = (flags & 0x04) > 0;
        isPairingMode = (flags & 0x08) > 0;

        int offset = USERS_INDEX;
        for (int i = 0; numberOfUser > i; i++) {
            User user = new User();
            user.lastSequenceNumber = Bytes.parse2BytesAsInt(data, offset, true);
            offset += 2;
            user.numberOfRecords = data[offset];
            offset += 1;
            users.add(user);
        }
    }

    @Override
    public String toString() {
        return "EachUserData{" +
                "users=" + users +
                ", numberOfUser=" + numberOfUser +
                ", isTimeNotSet=" + isTimeNotSet +
                ", isPairingMode=" + isPairingMode +
                '}';
    }

    public static class User implements Serializable {
        public int lastSequenceNumber;
        public int numberOfRecords;
    }
}
