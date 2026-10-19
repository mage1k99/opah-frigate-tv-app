package app.opah.tv.data.network

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Backs the user-visible, off-by-default "Allow unverified certificate" setting.
 * Checked by the shared [OkHttpClient]'s trust manager/hostname verifier so the
 * one Frigate-bound client this app uses everywhere can honor it without being
 * rebuilt per profile. Kept in sync with the active [app.opah.tv.data.model.ConnectionProfile]
 * by [app.opah.tv.data.ProfileRepository] and [app.opah.tv.ui.Phase0ViewModel].
 */
object InsecureTlsGate {
    private val flag = AtomicBoolean(false)

    var enabled: Boolean
        get() = flag.get()
        set(value) = flag.set(value)
}
