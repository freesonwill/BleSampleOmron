package jp.co.ohq.blesampleomron.view.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.neovisionaries.bluetooth.ble.advertising.ADManufacturerSpecific;
import com.neovisionaries.bluetooth.ble.advertising.ADStructure;
import com.neovisionaries.bluetooth.ble.advertising.Flags;
import com.neovisionaries.bluetooth.ble.advertising.IBeacon;
import com.neovisionaries.bluetooth.ble.advertising.LocalName;
import com.neovisionaries.bluetooth.ble.advertising.ServiceData;
import com.neovisionaries.bluetooth.ble.advertising.TxPowerLevel;
import com.neovisionaries.bluetooth.ble.advertising.UUIDs;
import com.neovisionaries.bluetooth.ble.util.Bytes;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import jp.co.ohq.ble.advertising.EachUserData;
import jp.co.ohq.blesampleomron.R;
import jp.co.ohq.blesampleomron.controller.util.AppLog;
import jp.co.ohq.blesampleomron.model.entity.MultipleLineItem;
import jp.co.ohq.blesampleomron.view.adapter.MultipleLineListAdapter;


public class AdvertisementDataFragment extends BaseFragment {

    private static final String ARG_AD_STRUCTURES = "ARG_AD_STRUCTURES";

    private static final Map<Integer, String> AD_TYPE_STRING_MAP;
    private static final Map<Integer, String> COMPANY_ID_STRING_MAP;

    static {
        LinkedHashMap<Integer, String> map = new LinkedHashMap<>();
        map.put(0x01, "Flags");
        map.put(0x02, "Incomplete List of 16-bit Service Class UUIDs");
        map.put(0x03, "Complete List of 16-bit Service Class UUIDs");
        map.put(0x04, "Incomplete List of 32-bit Service Class UUIDs");
        map.put(0x05, "Complete List of 32-bit Service Class UUIDs");
        map.put(0x06, "Incomplete List of 128-bit Service Class UUIDs");
        map.put(0x07, "Complete List of 128-bit Service Class UUIDs");
        map.put(0x08, "Shortened Local Name");
        map.put(0x09, "Complete Local Name");
        map.put(0x0A, "Tx Power Level");
        map.put(0x14, "List of 16-bit Service Solicitation UUIDs");
        map.put(0x15, "List of 128-bit Service Solicitation UUIDs");
        map.put(0x16, "Service Data - 16-bit UUID");
        map.put(0x1F, "List of 32-bit Service Solicitation UUIDs");
        map.put(0x20, "Service Data - 32-bit UUID");
        map.put(0x21, "Service Data - 128-bit UUID");
        map.put(0xFF, "Manufacturer Specific Data");
        AD_TYPE_STRING_MAP = Collections.unmodifiableMap(map);
    }

    static {
        LinkedHashMap<Integer, String> map = new LinkedHashMap<>();
        map.put(0x004C, "Apple, Inc.");
        map.put(0x020E, "Omron Healthcare Co., LTD");
        COMPANY_ID_STRING_MAP = Collections.unmodifiableMap(map);
    }

    private MultipleLineListAdapter mAdapter;

    @NonNull
    public static AdvertisementDataFragment newInstance(@NonNull List<ADStructure> structures) {
        AdvertisementDataFragment fragment = new AdvertisementDataFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_AD_STRUCTURES, structures.toArray(new ADStructure[0]));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        AppLog.vMethodIn();
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        ADStructure[] structures = (ADStructure[]) args.getSerializable(ARG_AD_STRUCTURES);
        if (null == structures) {
            throw new IllegalArgumentException("Argument '" + ARG_AD_STRUCTURES + "' must not be null.");
        }

        mAdapter = new MultipleLineListAdapter(getContext(), adStructuresToItems(structures));
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AppLog.vMethodIn();
        return inflater.inflate(R.layout.fragment_advertisement_data, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ListView listView = (ListView) view.findViewById(R.id.list);
        listView.setAdapter(mAdapter);
    }

    @NonNull
    @Override
    protected String onGetTitle() {
        return getString(R.string.advertisement_data).toUpperCase();
    }

    @NonNull
    private List<MultipleLineItem> adStructuresToItems(@NonNull ADStructure[] structures) {
        List<MultipleLineItem> items = new LinkedList<>();
        for (ADStructure structure : structures) {
            if (!(structure instanceof Flags)) {
                items.add(new MultipleLineItem(adTypeString(structure.getType())));
            }
            if (structure instanceof Flags) {
                Flags flag = (Flags) structure;
                if (flag.isLimitedDiscoverable() ||
                        flag.isGeneralDiscoverable() ||
                        flag.isLegacySupported() ||
                        flag.isControllerSimultaneitySupported() ||
                        flag.isHostSimultaneitySupported()) {
                    items.add(new MultipleLineItem(adTypeString(structure.getType())));
                }
                if (flag.isLimitedDiscoverable()) {
                    items.add(new MultipleLineItem("Limited Discoverable Mode", "YES"));
                    ;
                }
                if (flag.isGeneralDiscoverable()) {
                    items.add(new MultipleLineItem("General Discoverable Mode", "YES"));
                    ;
                }
                if (flag.isLegacySupported()) {
                    items.add(new MultipleLineItem("BR/EDR Not Supported", "YES"));
                    ;
                }
                if (flag.isControllerSimultaneitySupported()) {
                    items.add(new MultipleLineItem("Simultaneous LE and BR/EDR to Same Device Capable (Controller)", "YES"));
                    ;
                }
                if (flag.isHostSimultaneitySupported()) {
                    items.add(new MultipleLineItem("Simultaneous LE and BR/EDR to Same Device Capable (Host)", "YES"));
                    ;
                }
            } else if (structure instanceof UUIDs) {
                UUIDs uuids = (UUIDs) structure;
                for (UUID uuid : uuids.getUUIDs()) {
                    items.add(new MultipleLineItem(uuid.toString(), null));
                }
            } else if (structure instanceof LocalName) {
                LocalName localName = (LocalName) structure;
                items.add(new MultipleLineItem(localName.getLocalName(), null));
            } else if (structure instanceof TxPowerLevel) {
                TxPowerLevel txPowerLevel = (TxPowerLevel) structure;
                items.add(new MultipleLineItem(String.format(Locale.US, "%d dBm", txPowerLevel.getLevel()), null));
            } else if (structure instanceof ServiceData) {
                ServiceData serviceData = (ServiceData) structure;
                items.add(new MultipleLineItem(serviceData.getServiceUUID().toString(), null));
            } else if (structure instanceof ADManufacturerSpecific) {
                ADManufacturerSpecific adManufacturerSpecific = (ADManufacturerSpecific) structure;
                items.add(new MultipleLineItem("Company Identifier", companyIdString(adManufacturerSpecific.getCompanyId())));
                if (adManufacturerSpecific instanceof IBeacon) {
                    IBeacon iBeacon = (IBeacon) adManufacturerSpecific;
                    items.add(new MultipleLineItem("Proximity UUID", iBeacon.getUUID().toString()));
                    items.add(new MultipleLineItem("Major Value", iBeacon.getMajor()));
                    items.add(new MultipleLineItem("Minor Value", iBeacon.getMinor()));
                    items.add(new MultipleLineItem("Power", iBeacon.getPower()));
                } else if (adManufacturerSpecific instanceof EachUserData) {
                    EachUserData eachUserData = (EachUserData) adManufacturerSpecific;
                    items.add(new MultipleLineItem("Number of User", eachUserData.getNumberOfUser()));
                    if (eachUserData.isTimeNotSet()) {
                        items.add(new MultipleLineItem("Time Not Configured", "YES"));
                    }
                    if (eachUserData.isPairingMode()) {
                        items.add(new MultipleLineItem("Pairing Mode", "YES"));
                    }
                    int userIndex = 1;
                    for (EachUserData.User user : eachUserData.getUsers()) {
                        items.add(new MultipleLineItem(String.format(Locale.US, "Last Sequence Number (User Index %d)", userIndex), user.lastSequenceNumber));
                        items.add(new MultipleLineItem(String.format(Locale.US, "Number of Records (User Index %d)", userIndex), user.numberOfRecords));
                        userIndex++;
                    }
                } else {
                    items.add(new MultipleLineItem("Data", "0x" + Bytes.toHexString(adManufacturerSpecific.getData(), true)));
                }
            } else {
                items.add(new MultipleLineItem("0x" + Bytes.toHexString(structure.getData(), true), null));
            }
        }
        return items;
    }

    @NonNull
    private String adTypeString(int adType) {
        final String ret;
        if (AD_TYPE_STRING_MAP.containsKey(adType)) {
            ret = AD_TYPE_STRING_MAP.get(adType);
        } else {
            ret = String.format(Locale.US, "Unknown (0x%02x)", adType);
        }
        return ret;
    }

    @NonNull
    private String companyIdString(int id) {
        final String ret;
        if (COMPANY_ID_STRING_MAP.containsKey(id)) {
            ret = String.format(Locale.US, "%s (0x%04x)", COMPANY_ID_STRING_MAP.get(id), id);
        } else {
            ret = String.format(Locale.US, "Unknown (0x%04x)", id);
        }
        return ret;
    }
}
