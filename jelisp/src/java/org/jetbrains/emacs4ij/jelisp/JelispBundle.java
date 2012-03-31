package org.jetbrains.emacs4ij.jelisp;

import com.intellij.CommonBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;

/**
 * Created by IntelliJ IDEA.
 * User: kate
 * Date: 3/31/12
 * Time: 2:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class JelispBundle {
    private static Reference<ResourceBundle> ourBundle;

    @NonNls
    private static final String BUNDLE = "org.jetbrains.emacs4ij.jelisp.JelispBundle";

    private JelispBundle() {
    }

    public static String message(@PropertyKey(resourceBundle = BUNDLE)String key, Object... params) {
        return CommonBundle.message(getBundle(), key, params);
    }

    public static String message(@PropertyKey(resourceBundle = BUNDLE)String key) {
        return CommonBundle.message(getBundle(), key);
    }

    private static ResourceBundle getBundle() {
        ResourceBundle bundle = null;
        if (ourBundle != null) bundle = ourBundle.get();
        if (bundle == null) {
            bundle = ResourceBundle.getBundle(BUNDLE);
            ourBundle = new SoftReference<>(bundle);
        }
        return bundle;
    }
}
