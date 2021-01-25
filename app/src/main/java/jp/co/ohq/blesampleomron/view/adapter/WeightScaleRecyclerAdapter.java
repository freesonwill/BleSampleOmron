package jp.co.ohq.blesampleomron.view.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import jp.co.ohq.ble.enumerate.OHQMeasurementRecordKey;
import jp.co.ohq.blesampleomron.R;
import jp.co.ohq.blesampleomron.controller.util.Common;


public class WeightScaleRecyclerAdapter extends AbstractRecyclerAdapter<Map<OHQMeasurementRecordKey, Object>, WeightScaleRecyclerAdapter.ViewHolder> {

    public WeightScaleRecyclerAdapter(@NonNull Context context, @NonNull List<Map<OHQMeasurementRecordKey, Object>> objects) {
        super(context, R.layout.measurement_weight_scale, objects);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(mResourceId, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        Map<OHQMeasurementRecordKey, Object> measurementRecord = mObjects.get(position);
        holder.timestamp.setText((CharSequence) measurementRecord.get(OHQMeasurementRecordKey.TimeStampKey));
        if (measurementRecord.containsKey(OHQMeasurementRecordKey.UserIndexKey)) {
            holder.userIndex.setVisibility(View.VISIBLE);
            holder.userIndex.setText(Common.getNumberString((BigDecimal) measurementRecord.get(OHQMeasurementRecordKey.UserIndexKey)));
        }
        holder.weight.setText(Common.getDecimalString((BigDecimal) measurementRecord.get(OHQMeasurementRecordKey.WeightKey), 1));
        holder.weightUnit.setText((CharSequence) measurementRecord.get(OHQMeasurementRecordKey.WeightUnitKey));
    }

    class ViewHolder extends AbstractRecyclerAdapter.ViewHolder {
        TextView timestamp;
        TextView userIndex;
        TextView sequenceNumber;
        TextView weight;
        TextView weightUnit;

        ViewHolder(View itemView) {
            super(itemView);
            timestamp = (TextView) itemView.findViewById(R.id.timestamp);
            userIndex = (TextView) itemView.findViewById(R.id.userIndex);
            sequenceNumber = (TextView) itemView.findViewById(R.id.sequenceNumber);
            weight = (TextView) itemView.findViewById(R.id.weight);
            weightUnit = (TextView) itemView.findViewById(R.id.weightUnit);
        }
    }
}
