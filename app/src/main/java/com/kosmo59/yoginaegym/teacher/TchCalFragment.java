package com.kosmo59.yoginaegym.teacher;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.kosmo59.yoginaegym.R;
import com.kosmo59.yoginaegym.common.AppVO;
import com.kosmo59.yoginaegym.common.GymDBHelper;
import com.kosmo59.yoginaegym.member.calendar.EventDecorator;
import com.kosmo59.yoginaegym.member.calendar.OneDayDecorator;
import com.kosmo59.yoginaegym.member.calendar.SaturdayDecorator;
import com.kosmo59.yoginaegym.member.calendar.SundayDecorator;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class TchCalFragment extends Fragment {
    Dialog dlg = null;

    private final OneDayDecorator oneDayDecorator = new OneDayDecorator();
    MaterialCalendarView materialcalendarview;

    private Context context;
    GymDBHelper gymDBHelper = null;
    SQLiteDatabase db = null;
    AppVO vo = null;

    public TchCalFragment() {
        Log.i("TchCalFragment", "TchCalFragment 호출");

        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_tch_cal, container, false);
        Log.i("TchCalFragment", "onCreateView 호출");
        context = container.getContext();
        gymDBHelper = new GymDBHelper(this.context);
        db = gymDBHelper.getWritableDatabase();
        vo = (AppVO) this.context.getApplicationContext();

        materialcalendarview = (MaterialCalendarView) view.findViewById(R.id.calendarView_tch);
        Log.i("TchCalFragment", "materialcalendarview : " + materialcalendarview);

        materialcalendarview.state().edit()
                .setFirstDayOfWeek(Calendar.SUNDAY)
                .setMinimumDate(CalendarDay.from(2019, 01, 1)) // 달력의 시작
                .setMaximumDate(CalendarDay.from(2021, 12, 31)) // 달력의 끝
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit();
        materialcalendarview.addDecorators(
                new SundayDecorator(),
                new SaturdayDecorator(),
                oneDayDecorator);
///////////////////////////////SQLite /////////////////////////////////////////////
        String log_days_sel = "SELECT DISTINCT(ex_date)" +
                " FROM tch_log" +
                " WHERE tch_no ="+vo.getTchNum() +
                " ORDER BY ex_date desc";
        Log.i("테스트", "log_days_sel : " + log_days_sel);
        String[] log_days = null;
        Cursor cursor = db.rawQuery(log_days_sel, null);
        if(cursor.getCount()>0){
            log_days = new String[cursor.getCount()];
            int cnt = 0;
            while(cursor.moveToNext()){
                log_days[cnt++] = cursor.getString(0);
                Log.i("TchCalFragment", "log_days : " + log_days);
            }
        }
        if(log_days == null){
            log_days = new String[1];
            log_days[0] = "2000-02-10";
        }
        ///////////////////////////////SQLite 끝/////////////////////////////////////////////
        new ApiSimulator(log_days).executeOnExecutor(Executors.newSingleThreadExecutor());

        materialcalendarview.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                int Year = date.getYear();
                int Month = date.getMonth() + 1;
                int Day = date.getDay();

                Log.i("Year test", Year + "");
                Log.i("Month test", Month + "");
                Log.i("Day test", Day + "");
                String cho_month = null;
                if(Month<10){
                    cho_month = "0"+Month;
                }
                else {
                    cho_month = "" + Month;
                }
                String cho_day = null;
                if(Day<10){
                    cho_day = "0"+Day;
                }
                else {
                    cho_day = "" + Day;
                }
                String shot_Day = Year + "-" + cho_month + "-" + cho_day;

                Log.i("shot_Day test", shot_Day + "");
                //materialCalendarView.clearSelection();
                // Toast.makeText(view.getContext(), shot_Day, Toast.LENGTH_SHORT).show();
                // 커스텀 다이얼로그를 정의하기위해 Dialog클래스를 생성한다.
                dlg = new Dialog(context);

                // 액티비티의 타이틀바를 숨긴다.
                dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

                // 커스텀 다이얼로그의 레이아웃을 설정한다.
                dlg.setContentView(R.layout.fragment_mem_time_table_a_detail);
                ((TextView)dlg.findViewById(R.id.tv_log)).setText("내 일정");
                ((TextView)dlg.findViewById(R.id.log_title)).setText("일정 이름");
                ((TextView)dlg.findViewById(R.id.log_day)).setText("내 일정");
                ///////////////////////////////////SQLite DB연동/////////////////////////////////
                TextView tv_log_title = dlg.findViewById(R.id.tv_log_title);
                TextView tv_log_cont = dlg.findViewById(R.id.tv_log_cont);
                TextView tv_regDate = dlg.findViewById(R.id.tv_log_regDate);
                TextView tv_exDate = dlg.findViewById(R.id.tv_exDate);
                TextView tv_stime = dlg.findViewById(R.id.tv_stime);
                TextView tv_etime = dlg.findViewById(R.id.tv_etime);
                String log_sel = "SELECT log_title, log_cont, reg_date, ex_date, ex_stime, ex_etime"
                        + " FROM tch_log"
                        + " WHERE tch_no = "+vo.getTchNum()
                        + " AND ex_date = '" + shot_Day + "'";
                Log.i("TchCalFragment", "log_sel : " + log_sel);
                Cursor cursor = db.rawQuery(log_sel, null);
                Log.i("TchCalFragment", "cursor.getCount() : " + cursor.getCount());
                if(cursor.getCount()>0){
                    while(cursor.moveToNext()){
                        int cnt = 0;
                        Log.i("TchCalFragment", "cursor.getString(cnt++) : " + cursor.getString(cnt));
                        tv_log_title.setText(cursor.getString(cnt++));
                        tv_log_cont.setText(cursor.getString(cnt++));
                        tv_regDate.setText(cursor.getString(cnt++));
                        tv_exDate.setText(cursor.getString(cnt++));
                        tv_stime.setText(cursor.getString(cnt++));
                        tv_etime.setText(cursor.getString(cnt++));
                    }
                    // 커스텀 다이얼로그를 노출한다.
                    dlg.show();
                }

                //닫기 버튼
                ImageView icon_close = dlg.findViewById(R.id.icon_close);
                icon_close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dlg.hide();
                    }
                });

            }
        });
        return view;
    }

    private class ApiSimulator extends AsyncTask<Void, Void, List<CalendarDay>> {

        String[] Time_Result;

        ApiSimulator(String[] Time_Result) {
            Log.i("테스트", "ApiSimulator 호출");
            this.Time_Result = Time_Result;
            for (String a : Time_Result) {
                Log.i("Time_Result  : ", "Time_Result : " + a);
            }
        }

        @Override
        protected List<CalendarDay> doInBackground(@NonNull Void... voids) {

            Log.i("테스트", "doInBackground 호출");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Calendar calendar = Calendar.getInstance();
            ArrayList<CalendarDay> dates = new ArrayList<>();

            /*특정날짜 달력에 점표시해주는곳*/
            /*월은 0이 1월 년,일은 그대로*/
            //string 문자열인 Time_Result 을 받아와서 ,를 기준으로짜르고 string을 int 로 변환
            for (int i = 0; i < Time_Result.length; i++) {
                CalendarDay day = null;
                String[] time = Time_Result[i].split("-");
                int year = Integer.parseInt(time[0]);
                int month = Integer.parseInt(time[1]);
                int dayy = Integer.parseInt(time[2]);

                calendar.set(year, month - 1, dayy);
                day = CalendarDay.from(calendar);
                dates.add(day);
            }


            Log.i("dates : ", "dates : " + dates);
            return dates;
        }

        @Override
        protected void onPostExecute(@NonNull List<CalendarDay> calendarDays) {
            super.onPostExecute(calendarDays);
            Log.i("onPostExecute 호출", "onPostExecute 호출 calendarDays : " + calendarDays);
//            if (isFinishing()) {
//                Log.i("finishing 호출", "finishing 호출");
//                return;
//            }
            Log.i("테스트", "onPostExecute 호출");
            materialcalendarview.addDecorator(new EventDecorator(Color.BLUE, calendarDays, TchCalFragment.this));
            Log.i("테스트", "addDecorator 다음 코드");
        }


    }
}
