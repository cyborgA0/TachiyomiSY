package eu.kanade.tachiyomi.ui.base.controller

import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import eu.kanade.tachiyomi.ui.base.changehandler.OneWayFadeChangeHandler
import eu.kanade.tachiyomi.util.system.openInBrowser

fun Router.setRoot(controller: Controller, id: Int) {
    setRoot(controller.withFadeTransaction().tag(id.toString()))
}

fun Router.popControllerWithTag(tag: String): Boolean {
    val controller = getControllerWithTag(tag)
    if (controller != null) {
        popController(controller)
        return true
    }
    return false
}

fun Router.pushController(controller: Controller) {
    pushController(controller.withFadeTransaction())
}

fun Controller.requestPermissionsSafe(permissions: Array<String>, requestCode: Int) {
    val activity = activity ?: return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        permissions.forEach { permission ->
            if (ContextCompat.checkSelfPermission(activity, permission) != PERMISSION_GRANTED) {
                requestPermissions(arrayOf(permission), requestCode)
            }
        }
    }
}

fun Controller.withFadeTransaction(): RouterTransaction {
    return RouterTransaction.with(this)
        .pushChangeHandler(OneWayFadeChangeHandler())
        .popChangeHandler(OneWayFadeChangeHandler())
}

fun Controller.openInBrowser(url: String) {
    activity?.openInBrowser(url.toUri())
}
