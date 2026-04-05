package androidx.appcompat.app;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint;
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod;
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod;
import com.flyjingfish.android_aop_annotation.enums.MatchType;
import com.hss01248.aop.alertdialog.R;
import com.hss01248.dialog.ScreenUtil;
import com.hss01248.dialog.StyledDialog;
import com.hss01248.dialog.Tool;
import com.hss01248.dialog.adapter.SuperLvHolder;
import com.hss01248.dialog.config.ConfigBean;
import com.hss01248.dialog.interfaces.MyDialogListener;
import com.hss01248.dialog.interfaces.MyItemDialogListener;
import com.hss01248.dialog.ios.IosAlertDialogHolder;
import com.hss01248.dialog.ios.IosCenterItemHolder;
import com.hss01248.logforaop.LogMethodAspect;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * AlertDialog.Builder#create 替换为 iOS 风格（AndroidAOP）。
 */
public class AlertDialogAspect {

    private static final String TAG = "AlertDialogAspect";

    public static Object interceptBuilderCreate(ProceedJoinPoint joinPoint) throws Throwable {
        LogMethodAspect.logBefore(true, TAG, joinPoint, new LogMethodAspect.IBefore() {
            @Override
            public void before(ProceedJoinPoint joinPoin, String desc) {
            }
        });

        AlertDialog.Builder builder = (AlertDialog.Builder) joinPoint.getTarget();
        Field field = AlertDialog.Builder.class.getDeclaredField("P");
        field.setAccessible(true);
        AlertController.AlertParams params = (AlertController.AlertParams) field.get(builder);

        AlertDialog dialog = new AlertDialog(builder.getContext());
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ConfigBean bean;
        SuperLvHolder<ConfigBean> holder0;

        if (params.mItems == null) {
            bean = StyledDialog.buildIosAlert(params.mTitle, params.mMessage, new MyDialogListener() {
                @Override
                public void onFirst() {
                    if (params.mPositiveButtonListener != null) {
                        dialog.dismiss();
                        params.mPositiveButtonListener.onClick(dialog, 0);
                    } else {
                        dialog.dismiss();
                    }
                }

                @Override
                public void onSecond() {
                    if (params.mNegativeButtonListener != null) {
                        dialog.dismiss();
                        params.mNegativeButtonListener.onClick(dialog, 0);
                    } else {
                        dialog.dismiss();
                    }
                }

                @Override
                public void onThird() {
                    if (params.mNeutralButtonListener != null) {
                        dialog.dismiss();
                        params.mNeutralButtonListener.onClick(dialog, 0);
                    } else {
                        dialog.dismiss();
                    }
                }
            });
            IosAlertDialogHolder holder = new IosAlertDialogHolder(builder.getContext());
            holder.tvMsg.setVisibility(TextUtils.isEmpty(params.mMessage) ? View.GONE : View.VISIBLE);
            holder.et1.setVisibility(View.GONE);
            holder.et2.setVisibility(View.GONE);
            if (params.mView != null) {
                LinearLayout llContainer = holder.rootView.findViewById(R.id.ll_container);
                llContainer.addView(params.mView);
            }
            bean.setBtnText(params.mPositiveButtonText, params.mNegativeButtonText, params.mNeutralButtonText);

            bean.viewHolder = holder;
            holder.assingDatasAndEvents(bean.context, bean);

            int height = Tool.mesureHeight(holder.rootView, holder.tvMsg, holder.et1, holder.et2);
            bean.viewHeight = height;
            holder0 = holder;
        } else {
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
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog0) {
                WindowManager.LayoutParams attributes = dialog.getWindow().getAttributes();
                attributes.width = (int) (ScreenUtil.getWindowManager().getDefaultDisplay().getWidth() * 0.8f);
                dialog.getWindow().setAttributes(attributes);
            }
        });

        return dialog;
    }
}

@AndroidAopMatchClassMethod(
        targetClassName = "androidx.appcompat.app.AlertDialog$Builder",
        methodName = {"create"},
        type = MatchType.SELF
)
class AlertDialogBuilderCreateMatch implements MatchClassMethod {
    @Override
    public Object invoke(ProceedJoinPoint joinPoint, String methodName) throws Throwable {
        return AlertDialogAspect.interceptBuilderCreate(joinPoint);
    }
}
