# HW4's description

This assignment implements a notification that reminds the user to get back to the app after they haven't used it for a while. Additionally it implements a base for opening the camera by shaking the phone.

If notifications aren't already allowed the permission to send them is requested on app startup with `ActivityCompat.checkSelfPermission()`. Notificaitons are sent using a background job launched with `lifecycleScope.launch` which is executed in `MainActivity.onStop()`. It waits for 5 seconds and send the notification after that delay.

This solution required me to cancel the background job in `onStart()`, because for example changing the app's orientation invokes `MainActivity.onStop()` and that shouldn't cause an inactivity notification to be sent.

Adding notification intent `Intent.FLAG_ACTIVITY_NEW_TASK` for `MainActivity::class.java` made clicking the notification open the application.

As a placeholder for opening a camera I displayed an AlertDialog when the accelerometer sensor detects multiple sharp turns within a specific timeframe. These turns are detected by having a history of previous accelerometer readings and comparing a) the magnitude of average reading and b) the average magnitude of readings. As a threshold for a sharp turn I used (b > 10*a) and it seemed to work pretty well. This works because during a shake a) stays low, because the phone should remain near the starting point and b) depicts the total moved distance.
