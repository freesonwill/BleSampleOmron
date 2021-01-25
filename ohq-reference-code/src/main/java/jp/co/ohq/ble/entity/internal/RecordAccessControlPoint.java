package jp.co.ohq.ble.entity.internal;

import android.support.annotation.NonNull;
import android.util.AndroidRuntimeException;

import jp.co.ohq.utility.Bytes;

public class RecordAccessControlPoint {

    @NonNull
    public static Request newReportStoredRecordsOfAllRecords() {
        return new Request(OpCode.ReportStoredRecords, Operator.AllRecords, null, null);
    }

    @NonNull
    public static Request newReportStoredRecordsOfGreaterThanOrEqualTo(int sequenceNumber) {
        return new Request(OpCode.ReportStoredRecords, Operator.GreaterThanOrEqualTo, FilterType.SequenceNumber, sequenceNumber);
    }

    @NonNull
    public static Request newReportNumberOfStoredRecordsOfAllRecords() {
        return new Request(OpCode.ReportNumberOfStoredRecords, Operator.AllRecords, null, null);
    }

    @NonNull
    public static Request newReportNumberOfStoredRecordsOfGreaterThanOrEqualTo(int sequenceNumber) {
        return new Request(OpCode.ReportNumberOfStoredRecords, Operator.GreaterThanOrEqualTo, FilterType.SequenceNumber, sequenceNumber);
    }

    @NonNull
    public static Request newReportSequenceNumberOfLatestRecord() {
        return new Request(OpCode.ReportSequenceNumberOfLatestRecord, Operator.Null, null, null);
    }

    @NonNull
    public static Response parseResponse(@NonNull byte[] responsePacket) {
        final OpCode opCode = OpCode.valueOf(responsePacket[0]);
        final OpCode requestOpCode;
        final ResponseValue responseValue;
        final Integer numberOfRecords;
        final Integer sequenceNumber;
        switch (opCode) {
            case NumberOfStoredRecordsResponse:
                requestOpCode = null;
                responseValue = null;
                numberOfRecords = Bytes.parse2BytesAsInt(responsePacket, 2, true);
                sequenceNumber = null;
                break;
            case ResponseCode:
                requestOpCode = OpCode.valueOf(responsePacket[2]);
                responseValue = ResponseValue.valueOf(responsePacket[3]);
                numberOfRecords = null;
                sequenceNumber = null;
                break;
            case SequenceNumberOfLatestRecordResponse:
                requestOpCode = null;
                responseValue = null;
                numberOfRecords = null;
                sequenceNumber = Bytes.parse2BytesAsInt(responsePacket, 2, true);
                break;
            default:
                throw new IllegalArgumentException("Invalid data.");
        }
        return new Response(opCode, requestOpCode, responseValue, numberOfRecords, sequenceNumber);
    }

    public enum OpCode {
        Reserved((byte) 0x00),
        ReportStoredRecords((byte) 0x01),
        ReportNumberOfStoredRecords((byte) 0x04),
        NumberOfStoredRecordsResponse((byte) 0x05),
        ResponseCode((byte) 0x06),
        ReportSequenceNumberOfLatestRecord((byte) 0x10),
        SequenceNumberOfLatestRecordResponse((byte) 0x11),;
        private final byte value;

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

    public enum Operator {
        Null((byte) 0x00),
        AllRecords((byte) 0x01),
        GreaterThanOrEqualTo((byte) 0x03),;
        final byte value;

        Operator(byte value) {
            this.value = value;
        }

        byte value() {
            return value;
        }
    }

    public enum FilterType {
        Reserved((byte) 0x00),
        SequenceNumber((byte) 0x01),
        UserFacingTime((byte) 0x02),;
        final byte value;

        FilterType(byte value) {
            this.value = value;
        }

        byte value() {
            return value;
        }
    }

    public enum ResponseValue {
        Reserved((byte) 0x00),
        Success((byte) 0x01),
        OpCodeNotSupported((byte) 0x02),
        InvalidOperator((byte) 0x03),
        OperatorNotSupported((byte) 0x04),
        InvalidOperand((byte) 0x05),
        NoRecordsFound((byte) 0x06),
        AbortUnsuccessful((byte) 0x07),
        ProcedureNotCompleted((byte) 0x08),
        OperandNotSupported((byte) 0x09),;
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
        public final Operator operator;
        public final FilterType filterType;
        public final Integer sequenceNumber;

        private Request(OpCode opCode, Operator operator, FilterType filterType, Integer sequenceNumber) {
            this.opCode = opCode;
            this.operator = operator;
            this.filterType = filterType;
            this.sequenceNumber = sequenceNumber;
        }

        @NonNull
        public byte[] getPacket() {
            final byte[] packet;
            switch (opCode) {
                case ReportStoredRecords:
                    switch (operator) {
                        case AllRecords:
                            packet = new byte[2];
                            break;
                        case GreaterThanOrEqualTo:
                            packet = new byte[5];
                            packet[2] = FilterType.SequenceNumber.value();
                            packet[3] = (byte) (sequenceNumber & 0x000000ff);
                            packet[4] = (byte) ((sequenceNumber >> 8) & 0x000000ff);
                            break;
                        default:
                            throw new AndroidRuntimeException("Invalid operator.");
                    }
                    break;
                case ReportNumberOfStoredRecords:
                    switch (operator) {
                        case AllRecords:
                            packet = new byte[2];
                            break;
                        case GreaterThanOrEqualTo:
                            packet = new byte[5];
                            packet[2] = FilterType.SequenceNumber.value();
                            packet[3] = (byte) (sequenceNumber & 0x000000ff);
                            packet[4] = (byte) ((sequenceNumber >> 8) & 0x000000ff);
                            break;
                        default:
                            throw new AndroidRuntimeException("Invalid operator.");
                    }
                    break;
                case ReportSequenceNumberOfLatestRecord:
                    packet = new byte[2];
                    break;
                default:
                    throw new AndroidRuntimeException("Invalid op code.");
            }
            packet[0] = opCode.value();
            packet[1] = operator.value();
            return packet;
        }

        @Override
        public String toString() {
            return "RACP.Request{" +
                    "opCode=" + opCode +
                    ", operator=" + operator +
                    ", filterType=" + filterType +
                    ", sequenceNumber=" + sequenceNumber +
                    '}';
        }
    }

    public static class Response {
        public final OpCode opCode;
        public final OpCode requestOpCode;
        public final ResponseValue responseValue;
        public final Integer numberOfRecords;
        public final Integer sequenceNumber;

        private Response(OpCode opCode, OpCode requestOpCode, ResponseValue responseValue, Integer numberOfRecords, Integer sequenceNumber) {
            this.opCode = opCode;
            this.requestOpCode = requestOpCode;
            this.responseValue = responseValue;
            this.numberOfRecords = numberOfRecords;
            this.sequenceNumber = sequenceNumber;
        }

        @Override
        public String toString() {
            return "RACP.Response{" +
                    "opCode=" + opCode +
                    ", requestOpCode=" + requestOpCode +
                    ", responseValue=" + responseValue +
                    ", numberOfRecords=" + numberOfRecords +
                    ", sequenceNumber=" + sequenceNumber +
                    '}';
        }
    }
}
