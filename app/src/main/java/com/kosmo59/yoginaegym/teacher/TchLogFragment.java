package com.kosmo59.yoginaegym.teacher;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.kosmo59.yoginaegym.R;
import com.kosmo59.yoginaegym.common.AppVO;
import com.kosmo59.yoginaegym.common.GymDBHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TchLogFragment extends Fragment {
    private Context context;
    private CardView dailyRecord_reg;
    private ListView tchLogList;
    private Context mContext;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    GymDBHelper gymDBHelper = null;
    SQLiteDatabase db = null;
    AppVO vo = null;

    public TchLogFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tch_log, container, false);
        context = container.getContext();

        gymDBHelper = new GymDBHelper(this.context);
        db = gymDBHelper.getWritableDatabase();
        vo = (AppVO) this.context.getApplicationContext();


        dailyRecord_reg = view.findViewById(R.id.dailyRecord_reg);
        dailyRecord_reg.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                // 커스텀 다이얼로그를 생성한다. 사용자가 만든 클래스이다.
                TchLogRegDialog tchLogRegDialog = new TchLogRegDialog(context, TchLogFragment.this);

                // 커스텀 다이얼로그를 호출한다.
                tchLogRegDialog.openTchLogRegDialog();
            }
        });

        ///////////////////////////////SQLite /////////////////////////////////////////////
        String log_sel = "SELECT _id, reg_date, ex_date, log_title, ex_stime, ex_etime, log_cont" +
                " FROM tch_log" +
                " WHERE tch_no ="+vo.getTchNum() +
                " ORDER BY ex_date desc";
        Log.i("테스트", "log_sel : " + log_sel);
        Cursor cursor = db.rawQuery(log_sel, null);
        Map<String, Object> oneRow = null;
        List<Map<String, Object>> rows = new ArrayList<>();
        while(cursor.moveToNext()){
            int cnt = 0;
            oneRow = new HashMap<>();
            oneRow.put("_id", cursor.getString(cnt++));
            oneRow.put("reg_date", cursor.getString(cnt++));
            oneRow.put("ex_date", cursor.getString(cnt++));
            oneRow.put("log_title", cursor.getString(cnt++));
            oneRow.put("ex_stime", cursor.getString(cnt++));
            oneRow.put("ex_etime", cursor.getString(cnt++));
            oneRow.put("log_cont", cursor.getString(cnt++));
            rows.add(oneRow);
        }
        ///////////////////////////////SQLite 끝/////////////////////////////////////////////
        ////////////////////////////Adapter 연결///////////////////////////////////////////
        TchLogAdapter tchLogAdapter = new TchLogAdapter(mContext, R.layout.tch_log_item, rows, TchLogFragment.this);
        tchLogList = view.findViewById(R.id.tch_log_list);
        tchLogList.setAdapter(tchLogAdapter);
        ////////////////////////////Adapter 연결 끝///////////////////////////////////////////


        return view;
    }
    public void refresh() {
        Log.e("테스트", "refresh");
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commit();
    }
}
