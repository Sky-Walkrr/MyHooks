package me.gchfeng.hooks;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.widget.Toast;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by coder on 2018/1/25 0025.
 */

public class Main extends XposedHelper  implements IXposedHookZygoteInit, IXposedHookLoadPackage, IXposedHookInitPackageResources {
    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {

    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        switch (lpparam.packageName) {
            case "com.miui.contentextension":
                XposedBridge.log("Hook MIUI 传送门");
                Class<?> clazz = XposedHelper.findClass("com.miui.contentextension.text.card.CardViewAdapter", lpparam.classLoader);
                XposedHelper.findAndHookMethod("com.miui.contentextension.text.card.CardViewAdapter$TextSegmentCardView.OnSegmentScreenClickListener",
                        clazz.getClassLoader(), "onTranslateClick", String.class, String.class,
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                Context context = AndroidAppHelper.currentApplication();
                                for (Object arg : param.args) {
                                    XposedBridge.log("param: " + arg.toString());
                                    Toast.makeText(context, "Hooked!", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                super.afterHookedMethod(param);
                            }
                        });
                break;
            case "com.tencent.mobileqq":
                XposedBridge.log("Hook QQ");
                break;
            default:
                break;
        }
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {

    }
}
