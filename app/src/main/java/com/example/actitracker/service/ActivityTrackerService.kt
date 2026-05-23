package com.example.actitracker.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.view.View
import android.widget.RemoteViews
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.core.graphics.createBitmap
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import coil.size.Size
import com.example.actitracker.ActiTrackerApplication
import com.example.actitracker.MainActivity
import com.example.actitracker.R
import com.example.actitracker.data.SettingsDataStore
import com.example.actitracker.data.model.ActivityItem
import com.example.actitracker.data.search.IconSearchRepository
import com.example.actitracker.receiver.ActivityActionReceiver
import com.example.actitracker.ui.components.IconMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class ActivityTrackerService : Service() {

    companion object {
        const val ACTION_START = "com.example.actitracker.START"
        const val ACTION_STOP = "com.example.actitracker.STOP"
        const val EXTRA_ACTIVITY_ID = "activity_id"
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "activity_tracker_channel"
        private const val MAX_NOTIFICATION_ACTIVITIES = 10
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var currentActivities: List<ActivityItem> = emptyList()
    private var activeActivityId: Long? = null

    private var backgroundColor: Int = SettingsDataStore.DEFAULT_COLOR_ARGB
    private var contentColor: Int = SettingsDataStore.DEFAULT_CONTENT_COLOR_ARGB

    private lateinit var app: ActiTrackerApplication
    private lateinit var notificationManager: NotificationManager
    private lateinit var imageLoader: ImageLoader
    private lateinit var iconSearchRepository: IconSearchRepository
    private val iconCache = mutableMapOf<String, Bitmap>()

    override fun onCreate() {
        super.onCreate()
        app = application as ActiTrackerApplication
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        imageLoader = ImageLoader.Builder(this)
            .components { add(SvgDecoder.Factory()) }
            .build()

        iconSearchRepository = IconSearchRepository(this)
        serviceScope.launch {
            iconSearchRepository.initialize()
        }

        createNotificationChannel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                buildSimpleNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, buildSimpleNotification())
        }

        observeState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val activityId = intent.getLongExtra(EXTRA_ACTIVITY_ID, -1L)
                if (activityId != -1L) handleStart(activityId)
            }

            ACTION_STOP -> {
                val activityId = intent.getLongExtra(EXTRA_ACTIVITY_ID, -1L)
                if (activityId != -1L) handleStop(activityId)
            }
        }
        return START_STICKY
    }

    private fun observeState() {
        serviceScope.launch {
            combine(
                app.repository.getQuickPanelActivities(),
                app.repository.getActiveSessionFlow(),
                app.settingsDataStore.backgroundColorFlow,
                app.settingsDataStore.contentColorFlow
            ) { quickPanelEntities, activeSession, bg, content ->
                activeActivityId = activeSession?.activityId
                backgroundColor = bg
                contentColor = content

                quickPanelEntities
                    .take(MAX_NOTIFICATION_ACTIVITIES)
                    .map { entity ->
                        ActivityItem(
                            id = entity.id,
                            name = entity.name,
                            color = androidx.compose.ui.graphics
                                .Color(entity.color.toULong()),
                            icon = entity.icon,
                            showInQuickPanel = true
                        )
                    }
            }.collect { items ->
                currentActivities = items

                /**
                 * Pre-load icons
                 * */
                items.forEach { activity ->
                    if (!iconCache.containsKey(activity.icon)) {
                        loadIconToCache(activity)
                    }
                }

                if (items.isEmpty()) {
                    stopForegroundService()
                } else {
                    updateNotification()
                }
            }
        }
    }

    private fun stopForegroundService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun handleStart(activityId: Long) {
        serviceScope.launch {
            app.repository.closeAllActiveSessionsExcept(activityId)
            app.repository.startActivitySession(activityId)

            val now = System.currentTimeMillis()
            val currentStarts = app.settingsDataStore.firstStartTimesFlow.first().toMutableMap()

            if (currentStarts[activityId] == null
                || !isSameDay(currentStarts[activityId]!!, now)
            ) {
                currentStarts[activityId] = now
                app.settingsDataStore.saveFirstStartTimes(currentStarts)
            }
        }
    }

    private fun isSameDay(time1: Long, time2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun handleStop(activityId: Long) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                app.repository.stopActivitySession(activityId)
            } catch (e: Exception) {
                android.util.Log.e(
                    "ActivityTrackerService",
                    "Error stopping activity $activityId", e
                )
            }
        }
    }

    private fun loadIconToCache(activity: ActivityItem) {
        val iconInfo = IconMapper.getIconInfo(activity.icon) ?: return

        serviceScope.launch(Dispatchers.IO) {
            val request = ImageRequest.Builder(this@ActivityTrackerService)
                .data(iconInfo.assetPath!!)
                .size(Size(100, 100))
                .allowHardware(false) // Required for drawing to Canvas if it's a Bitmap
                .build()

            val result = imageLoader.execute(request)
            result.drawable?.let { drawable ->
                val b = createBitmap(100, 100)
                val canvas = android.graphics.Canvas(b)
                /**
                 * If it's a Vector/SVG, we need to tint it white before caching
                 * so setColorFilter in RemoteViews
                 * can then apply the actual activity color correctly.
                 */
                drawable.setTint(Color.WHITE)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)

                iconCache[activity.icon] = b
                withContext(Dispatchers.Main) {
                    updateNotification()
                }
            }
        }
    }

    private fun updateNotification() {
        try {
            notificationManager.notify(NOTIFICATION_ID, buildNotification())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun buildSimpleNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_today_outline)
            .setContentTitle(getString(R.string.app_name))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun buildNotification(): Notification {
        val openAppIntent = buildOpenAppIntent()

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_today_outline)
            .setContentIntent(openAppIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCustomContentView(buildRemoteViews())
            .setCustomBigContentView(buildRemoteViews())
            .build()
    }

    private fun buildOpenAppIntent(): PendingIntent {
        return PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildRemoteViews(): RemoteViews {
        val views = RemoteViews(packageName, R.layout.notification_activity_list)

        views.setInt(
            R.id.notification_root,
            "setBackgroundColor",
            backgroundColor
        )

        // Show header only on older Android versions (before Android 12 / S)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            views.setViewVisibility(R.id.notification_header, View.VISIBLE)
            views.setTextColor(R.id.notification_app_name, contentColor)
            views.setInt(
                R.id.notification_app_icon,
                "setColorFilter",
                contentColor
            )
        } else {
            views.setViewVisibility(R.id.notification_header, View.GONE)
        }

        views.removeAllViews(R.id.notification_activities_container)

        currentActivities.forEachIndexed { index, activity ->
            val isActive = activity.id == activeActivityId
            val itemView = RemoteViews(packageName, R.layout.notification_activity_item)

            val bitmap = iconCache[activity.icon]
            val activityColor = if (activity.color.toArgb() != 0) {
                activity.color.toArgb()
            } else {
                /**
                 * If toArgb is 0, it might be due to the shifted 64-bit format seen in the DB
                 * Extracting the 32-bit ARGB from the Color's internal value if possible
                 */
                (activity.color.value shr 32).toInt()
                    .let { if (it == 0) activity.color.toArgb() else it }
            }

            if (bitmap != null) {
                itemView.setViewVisibility(
                    R.id.activity_icon_image,
                    View.VISIBLE
                )

                /**
                 * Using setImageViewBitmap + setColorFilter as it's more reliable for RemoteViews
                 */
                itemView.setImageViewBitmap(R.id.activity_icon_image, bitmap)
                itemView.setInt(
                    R.id.activity_icon_image,
                    "setColorFilter",
                    activityColor)
            } else {
                itemView.setViewVisibility(
                    R.id.activity_icon_image,
                    View.GONE)
            }

            itemView.setTextViewText(R.id.activity_name, activity.name)
            itemView.setTextColor(R.id.activity_name, contentColor)

            if (isActive) {
                itemView.setInt(
                    R.id.activity_active_indicator,
                    "setColorFilter",
                    contentColor
                )
                itemView.setInt(
                    R.id.activity_active_indicator,
                    "setImageAlpha",
                    255
                )
            } else {
                itemView.setInt(
                    R.id.activity_active_indicator,
                    "setImageAlpha",
                    0
                )
            }

            itemView.setInt(
                R.id.activity_divider,
                "setColorFilter",
                contentColor
            )
            itemView.setInt(
                R.id.activity_divider,
                "setImageAlpha",
                50
            )
            itemView.setViewVisibility(
                R.id.activity_divider,
                if (index < currentActivities.size - 1) View.VISIBLE else View.GONE
            )

            val toggleIntent = Intent(
                this,
                ActivityActionReceiver::class.java
            ).apply {
                action = ActivityActionReceiver.ACTION_TOGGLE
                putExtra(ActivityActionReceiver.EXTRA_ACTIVITY_ID, activity.id)
                putExtra(ActivityActionReceiver.EXTRA_IS_ACTIVE, isActive)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                activity.id.toInt(),
                toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            itemView.setOnClickPendingIntent(R.id.activity_item_root, pendingIntent)

            views.addView(R.id.notification_activities_container, itemView)
        }

        return views
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
