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

public class BodyCompositionRecyclerAdapter extends AbstractRecyclerAdapter<Map<OHQMeasurementRecordKey, Object>, BodyCompositionRecyclerAdapter.ViewHolder> {

    public BodyCompositionRecyclerAdapter(@NonNull Context context, @NonNull List<Map<OHQMeasurementRecordKey, Object>> objects) {
        super(context, R.layout.measurement_body_composition, objects);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(mResourceId, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        Map<OHQMeasurementRecordKey, Object> item = mObjects.get(position);
        holder.timestamp.setText((CharSequence) item.get(OHQMeasurementRecordKey.TimeStampKey));

        if (item.containsKey(OHQMeasurementRecordKey.UserIndexKey)) {
            holder.userIndex.setVisibility(View.VISIBLE);
            holder.userIndex.setText(Common.getNumberString((BigDecimal) item.get(OHQMeasurementRecordKey.UserIndexKey)));
        }
        if (item.containsKey(OHQMeasurementRecordKey.SequenceNumberKey)) {
            holder.sequenceNumber.setVisibility(View.VISIBLE);
            holder.sequenceNumber.setText("# " + Common.getNumberString((BigDecimal) item.get(OHQMeasurementRecordKey.SequenceNumberKey)));
        }
        holder.weight.setText(Common.getDecimalString((BigDecimal) item.get(OHQMeasurementRecordKey.WeightKey), 1));
        holder.weightUnit.setText((CharSequence) item.get(OHQMeasurementRecordKey.WeightUnitKey));

        if (item.containsKey(OHQMeasurementRecordKey.BodyFatPercentageKey)) {
            holder.bodyFatPercentage.setText(Common.getPercentString((BigDecimal) item.get(OHQMeasurementRecordKey.BodyFatPercentageKey), 1));
        }
    }

    class ViewHolder extends AbstractRecyclerAdapter.ViewHolder {
        TextView timestamp;
        TextView userIndex;
        TextView sequenceNumber;
        TextView weight;
        TextView weightUnit;
        TextView bodyFatPercentage;

        ViewHolder(View itemView) {
            super(itemView);
            timestamp = (TextView) itemView.findViewById(R.id.timestamp);
            userIndex = (TextView) itemView.findViewById(R.id.userIndex);
            sequenceNumber = (TextView) itemView.findViewById(R.id.sequenceNumber);
            weight = (TextView) itemView.findViewById(R.id.weight);
            weightUnit = (TextView) itemView.findViewById(R.id.weightUnit);
            bodyFatPercentage = (TextView) itemView.findViewById(R.id.bodyFatPercentage);
        }
    }
}
