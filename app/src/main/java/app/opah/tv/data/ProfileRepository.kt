package app.opah.tv.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.opah.tv.data.model.ConnectionProfile
import app.opah.tv.data.network.InsecureTlsGate
import kotlinx.coroutines.flow.first

private val Context.connectionDataStore by preferencesDataStore(name = "connection_profile")

interface ConnectionProfileStore {
    suspend fun load(): ConnectionProfile?
    suspend fun save(profile: ConnectionProfile)
    suspend fun clear()
}

class ProfileRepository(private val context: Context) : ConnectionProfileStore {
    override suspend fun load(): ConnectionProfile? {
        val preferences = context.connectionDataStore.data.first()
        val url = preferences[API_URL] ?: return null
        val username = preferences[USERNAME] ?: return null
        val profile = ConnectionProfile(
            apiBaseUrl = url,
            username = username,
            rtspHostOverride = preferences[RTSP_HOST],
            rtspPort = preferences[RTSP_PORT] ?: 8554,
            allowUnverifiedTls = preferences[ALLOW_UNVERIFIED_TLS] ?: false,
        )
        // Every background/consumer path (realtime, alerts, image fetches) reaches the
        // Frigate server through load() rather than the setup screen, so this is the one
        // place that must keep the shared OkHttpClient's trust gate in sync with the
        // saved server's choice.
        InsecureTlsGate.enabled = profile.allowUnverifiedTls
        return profile
    }

    override suspend fun save(profile: ConnectionProfile) {
        context.connectionDataStore.edit { preferences ->
            preferences[API_URL] = profile.apiBaseUrl
            preferences[USERNAME] = profile.username
            profile.rtspHostOverride?.let { preferences[RTSP_HOST] = it }
                ?: preferences.remove(RTSP_HOST)
            preferences[RTSP_PORT] = profile.rtspPort
            preferences[ALLOW_UNVERIFIED_TLS] = profile.allowUnverifiedTls
        }
    }

    override suspend fun clear() {
        context.connectionDataStore.edit { it.clear() }
        InsecureTlsGate.enabled = false
    }

    private companion object {
        val API_URL = stringPreferencesKey("api_base_url")
        val USERNAME = stringPreferencesKey("username")
        val RTSP_HOST = stringPreferencesKey("rtsp_host_override")
        val RTSP_PORT = intPreferencesKey("rtsp_port")
        val ALLOW_UNVERIFIED_TLS = booleanPreferencesKey("allow_unverified_tls")
    }
}
