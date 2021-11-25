package me.gchfeng.hooks;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.Window;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by coder on 2018/1/25 0025.
 */

public class Main extends XposedHelper implements IXposedHookZygoteInit, IXposedHookLoadPackage, IXposedHookInitPackageResources {
    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {

    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        switch (lpparam.packageName) {
            case "com.miui.contentextension":
                XposedBridge.log("Hook MIUI 传送门");
                // hook translate btn click action
                XposedHelpers.findAndHookMethod("com.miui.contentextension.text.cardview.TaplusRecognitionExpandedTextCard", lpparam.classLoader,
                        "doTranslate", new XC_MethodReplacement() {
                            @Override
                            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                                XposedBridge.log("Hooked doTranslate----->");
                                Field mSegmentAdapter = param.thisObject.getClass().getDeclaredField("mSegmentAdapter");
                                if (mSegmentAdapter != null) {
                                    mSegmentAdapter.setAccessible(true);
                                    Object mSegmentAdapterObj = mSegmentAdapter.get(param.thisObject);
                                    Method getSelectedWords = mSegmentAdapterObj.getClass().getDeclaredMethod("getSelectedWords");
                                    Object methodResult = getSelectedWords.invoke(mSegmentAdapterObj);
                                    String selectedWords = (String) methodResult;
                                    XposedBridge.log("Selected words: " + selectedWords);
                                    // Replace with the system sharing!
                                    Intent intent = new Intent();
                                    intent.setType("text/plain");
                                    intent.putExtra(android.content.Intent.EXTRA_TEXT, selectedWords);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.setAction(Intent.ACTION_SEND);
                                    Field mTranslate = param.thisObject.getClass().getDeclaredField("mTranslate");
                                    mTranslate.setAccessible(true);
                                    View view = (View) mTranslate.get(param.thisObject);
                                    Context context = view.getContext();
                                    context.startActivity(intent);
                                } else {
                                    XposedBridge.log("Cannot find declared field mSegmentAdapter");
                                }
                                return null;
                            }
                        });
                XposedHelpers.findAndHookConstructor("com.miui.contentextension.text.cardview.TaplusRecognitionExpandedTextCard",
                        lpparam.classLoader, Context.class,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                super.afterHookedMethod(param);
                                String shareText = "分享";
                                // Change the text
                                Field mTranslate = param.thisObject.getClass().getDeclaredField("mTranslate");
                                mTranslate.setAccessible(true);
                                Object tvObj = mTranslate.get(param.thisObject);
                                Method methodSetText = tvObj.getClass().getDeclaredMethod("setText", CharSequence.class);
                                methodSetText.invoke(tvObj, shareText);
                                // Another way
//                                XposedHelpers.callMethod(tvObj, "setText", shareText);
                            }
                        });
                break;
            case "com.tencent.mobileqq":
                XposedBridge.log("Hook QQ");
                // source: https://github.com/35099644/closeDigWithXp/blob/d203be6b77dfb4dbe71fe352d126b46003588a37/src/com/mhook/dialog/Module.java
                XC_MethodHook callback1 = new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        param.args[0] = true;
                        XposedBridge.log("对话框已设置为可取消");
                    }
                };
                XC_MethodHook XC_MethodHook1 = new XC_MethodHook() {
                    public void beforeHookedMethod(XC_MethodHook.MethodHookParam param) {
                        if (param.args[0] instanceof DialogInterface.OnCancelListener) {
                            DialogInterface.OnCancelListener OnCancelListener1 = new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface p1) {
                                    XposedBridge.log("对话框替换成功");
                                }
                            };
                            param.args[0] = OnCancelListener1;
                        }
                    }
                };
                Class<?> clazz = null;
                try {
                    clazz = Class.forName("android.support.v7.app.AlertDialog.Builder");
                } catch (ClassNotFoundException e) {
                    XposedBridge.log("class未发现:" + e);
                }
                if (clazz != null) {
                    XposedBridge.hookAllMethods(clazz, "setCancelable", callback1);
                    XposedBridge.hookAllMethods(clazz, "setOnCancelListener",
                            XC_MethodHook1);
                } else {
                    XposedBridge.log("clazz未知");
                }

                XposedBridge.hookAllMethods(Dialog.class, "setCancelable", callback1);
                XposedBridge.hookAllMethods(Dialog.class, "setCanceledOnTouchOutside",
                        callback1);
                XposedBridge.hookAllMethods(AlertDialog.Builder.class, "setCancelable",
                        callback1);
                XposedBridge.hookAllMethods(Activity.class, "setFinishOnTouchOutside",
                        callback1);
                XposedBridge.hookAllMethods(AlertDialog.Builder.class,
                        "setOnCancelListener", XC_MethodHook1);
                XposedBridge.hookAllMethods(Dialog.class, "setOnCancelListener",
                        XC_MethodHook1);
                XposedBridge.hookAllMethods(Window.class, "setCloseOnTouchOutside",
                        callback1);
                XposedBridge.hookAllMethods(Window.class,
                        "setCloseOnTouchOutsideIfNotSet", callback1);
                break;
            default:
                break;
        }
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {

    }
}
