package jp.co.ohq.ble.entity.internal;

import android.support.annotation.NonNull;
import android.util.AndroidRuntimeException;

public class UserControlPoint {

    @NonNull
    public static Request newRegisterNewUser(int consentCode) {
        return new Request(OpCode.RegisterNewUser, null, consentCode);
    }

    @NonNull
    public static Request newRegisterNewUserWithUserIndex(int userIndex, int consentCode) {
        return new Request(OpCode.RegisterNewUserWithUserIndex, userIndex, consentCode);
    }

    @NonNull
    public static Request newConsent(int userIndex, int consentCode) {
        return new Request(OpCode.Consent, userIndex, consentCode);
    }

    @NonNull
    public static Request newDeleteUserData() {
        return new Request(OpCode.DeleteUserData, null, null);
    }

    @NonNull
    public static Response parseResponse(@NonNull byte[] responsePacket) {
        final OpCode opCode = OpCode.valueOf(responsePacket[0]);
        final OpCode requestOpCode = OpCode.valueOf(responsePacket[1]);
        final ResponseValue responseValue = ResponseValue.valueOf(responsePacket[2]);
        final Integer userIndex;
        if (opCode != OpCode.ResponseCode) {
            throw new IllegalArgumentException("Invalid data.");
        }
        switch (requestOpCode) {
            case RegisterNewUser:
                if (responseValue == ResponseValue.Success) {
                    userIndex = (int) responsePacket[3];
                } else {
                    userIndex = null;
                }
                break;
            default:
                userIndex = null;
                break;
        }
        return new Response(opCode, requestOpCode, responseValue, userIndex);
    }

    public enum OpCode {
        Reserved((byte) 0x00),
        RegisterNewUser((byte) 0x01),
        Consent((byte) 0x02),
        DeleteUserData((byte) 0x03),
        ResponseCode((byte) 0x20),
        RegisterNewUserWithUserIndex((byte) 0x40),;
        final byte value;

        OpCode(byte value) {
            this.value = value;
        }

        static OpCode valueOf(byte value) {
            for (OpCode type : values()) {
                if (type.value() == value) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid value : " + value);
        }

        byte value() {
            return value;
        }
    }

    public enum ResponseValue {
        Reserved((byte) 0x00),
        Success((byte) 0x01),
        OpCodeNotSupported((byte) 0x02),
        InvalidParameter((byte) 0x03),
        OperationFailed((byte) 0x04),
        UserNotAuthorized((byte) 0x05),;
        final byte value;

        ResponseValue(byte value) {
            this.value = value;
        }

        static ResponseValue valueOf(byte value) {
            for (ResponseValue type : values()) {
                if (type.value() == value) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid value : " + value);
        }

        byte value() {
            return value;
        }
    }

    public static class Request {
        public final OpCode opCode;
        public final Integer userIndex;
        public final Integer consentCode;

        private Request(OpCode opCode, Integer userIndex, Integer consentCode) {
            this.opCode = opCode;
            this.userIndex = userIndex;
            this.consentCode = consentCode;
        }

        @NonNull
        public byte[] getPacket() {
            final byte[] packet;
            switch (opCode) {
                case RegisterNewUser:
                    packet = new byte[3];
                    packet[1] = (byte) (consentCode & 0x000000ff);
                    packet[2] = (byte) ((consentCode >> 8) & 0x000000ff);
                    break;
                case RegisterNewUserWithUserIndex:
                    packet = new byte[4];
                    packet[1] = userIndex.byteValue();
                    packet[2] = (byte) (consentCode & 0x000000ff);
                    packet[3] = (byte) ((consentCode >> 8) & 0x000000ff);
                    break;
                case Consent:
                    packet = new byte[4];
                    packet[1] = userIndex.byteValue();
                    packet[2] = (byte) (consentCode & 0x000000ff);
                    packet[3] = (byte) ((consentCode >> 8) & 0x000000ff);
                    break;
                case DeleteUserData:
                    packet = new byte[1];
                    break;
                default:
                    throw new AndroidRuntimeException("Invalid op code.");
            }
            packet[0] = opCode.value();
            return packet;
        }

        @Override
        public String toString() {
            return "UCP.Request{" +
                    "opCode=" + opCode +
                    ", userIndex=" + userIndex +
                    ", consentCode=" + consentCode +
                    '}';
        }
    }

    public static class Response {
        public final OpCode opCode;
        public final OpCode requestOpCode;
        public final ResponseValue responseValue;
        public final Integer userIndex;

        private Response(OpCode opCode, OpCode requestOpCode, ResponseValue responseValue, Integer userIndex) {
            this.opCode = opCode;
            this.requestOpCode = requestOpCode;
            this.responseValue = responseValue;
            this.userIndex = userIndex;
        }

        @Override
        public String toString() {
            return "UCP.Response{" +
                    "opCode=" + opCode +
                    ", requestOpCode=" + requestOpCode +
                    ", responseValue=" + responseValue +
                    ", userIndex=" + userIndex +
                    '}';
        }
    }
}
