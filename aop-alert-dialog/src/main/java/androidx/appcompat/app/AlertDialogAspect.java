package androidx.appcompat.app;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.hss01248.aop.alertdialog.R;
import com.hss01248.dialog.StyledDialog;
import com.hss01248.dialog.Tool;
import com.hss01248.dialog.adapter.SuperLvHolder;
import com.hss01248.dialog.config.ConfigBean;
import com.hss01248.dialog.interfaces.MyDialogListener;
import com.hss01248.dialog.interfaces.MyItemDialogListener;
import com.hss01248.dialog.ios.IosAlertDialogHolder;
import com.hss01248.dialog.ios.IosCenterItemHolder;
import com.hss01248.logforaop.LogMethodAspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * by hss
 * data:2020/7/17
 * desc:
 */
@Aspect
public class AlertDialogAspect {

    private static final String TAG = "AlertDialogAspect";


    @Around("execution(* androidx.appcompat.app.AlertDialog.Builder.create(..))")
    public Object weaveJoinPoint(ProceedingJoinPoint joinPoint) throws Throwable {
        LogMethodAspect.logBefore(true, TAG, joinPoint, new LogMethodAspect.IBefore() {
            @Override
            public void before(JoinPoint joinPoin, String desc) {
                LogMethodAspect.IBefore.super.before(joinPoin, desc);
            }
        });

        AlertDialog.Builder builder = (AlertDialog.Builder) joinPoint.getThis();
        Field field = AlertDialog.Builder.class.getDeclaredField("P");
        field.setAccessible(true);
        AlertController.AlertParams params = (AlertController.AlertParams) field.get(builder);

        //params.mView
        //params.bu

        AlertDialog dialog = new AlertDialog(builder.getContext());
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        ConfigBean bean = null;

        SuperLvHolder<ConfigBean> holder0 = null;

        if(params.mItems == null){
            bean = StyledDialog.buildIosAlert(params.mTitle, params.mMessage, new MyDialogListener() {
                @Override
                public void onFirst() {
                    if(params.mPositiveButtonListener != null){
                        dialog.dismiss();
                        params.mPositiveButtonListener.onClick(dialog,0);
                    }else {
                        dialog.dismiss();
                    }
                }

                @Override
                public void onSecond() {
                    if(params.mNegativeButtonListener != null){
                        dialog.dismiss();
                        params.mNegativeButtonListener.onClick(dialog,0);
                    }else {
                        dialog.dismiss();
                    }
                }

                @Override
                public void onThird() {
                    if(params.mNeutralButtonListener != null){
                        dialog.dismiss();
                        params.mNeutralButtonListener.onClick(dialog,0);
                    }else {
                        dialog.dismiss();
                    }
                }
            });
            IosAlertDialogHolder  holder = new IosAlertDialogHolder(builder.getContext());
            holder.tvMsg.setVisibility(TextUtils.isEmpty(params.mMessage) ? View.GONE: View.VISIBLE);
            holder.et1.setVisibility(View.GONE);
            holder.et2.setVisibility(View.GONE);
            //内部view
            if(params.mView != null){
                LinearLayout llContainer = holder.rootView.findViewById(R.id.ll_container);
                llContainer.addView(params.mView);
            }
            //三个按钮
            bean.setBtnText(params.mPositiveButtonText, params.mNegativeButtonText, params.mNeutralButtonText);

            bean.viewHolder = holder;
            //bean.dialog.setContentView(holder.rootView);
            holder.assingDatasAndEvents(bean.context,bean);

            int height = Tool.mesureHeight(holder.rootView,holder.tvMsg,holder.et1,holder.et2);
            bean.viewHeight = height;
            holder0 = holder;
        }else {
            // 单选,多选的适配
            CharSequence[] mItems = params.mItems;
            List<CharSequence> strs = new ArrayList<>();
            for (CharSequence mItem : mItems) {
                strs.add(mItem);
            }

            bean = StyledDialog.buildIosSingleChoose(strs, new MyItemDialogListener() {
                @Override
                public void onItemClick(CharSequence text, int position) {
                    dialog.dismiss();
                    params.mOnClickListener.onClick(dialog, position);
                }
            });
            IosCenterItemHolder holder = new IosCenterItemHolder(builder.getContext());
            bean.title = params.mTitle;
            holder.assingDatasAndEvents(builder.getContext(), bean);
            holder0 = holder;
        }
        dialog.setView(holder0.rootView);

        dialog.setCancelable(params.mCancelable);



        //dialog.getWindow().setGravity(builder.);
        //dialog.setView();
        return dialog;

    }



}
