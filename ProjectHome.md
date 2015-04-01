NOTE:
Since Android 4.0, wifi peer-to-peer communication has been unlocked without needing root access. See details here: http://developer.android.com/guide/topics/connectivity/wifip2p.html

This project was developed for pre-4.0 Android devices. The project was created to satisfy requirements for a mobile development course at Arizona State University in 2011.


This app is to be used for academic purposes only and inherits the licensing from adhoc-on-android and Posit-mobile projects (links below). We are not responsible for any damage that may result due to the use of this app.

The connection classes of this code was taken from adhoc-on-android (http://code.google.com/p/adhoc-on-android/) and Posit-mobile (http://code.google.com/p/posit-mobile/) projects.

This app allows users to communicate over a peer-to-peer network. It has been tested on the G1 and HTC Hero.

Please note that this app only works on rooted phones. Superuser permissions must be granted to the app in order to activate the wifi hardware as an access point. Each node is both a server and client on the same SSID labeled "MobiNode"

Please also note that WiFi will be permanently enabled once the start button is pressed. The best way to disable is to reboot the mobile device.

Be sure that all nodes in the network select the same "mode" on connect. The "Pull" mode has been tested to be more efficient.

Once the app is started, 3 files will be created on the sd card:
> -Adhoc-Social\_Log.txt
> > Logs all packets sent and received in text format.

> -Adhoc-Social\_Log.xls
> > Logs all packets sent and received in a tab delimited, spreadsheet friendly format.

> -Adhoc-Social\_Buddies.htm
> > Logs all buddy activity such as add, removed, pinged, updated, pull (color coded)

Files may be provided on the sd card to enable some extra functionality. They include:

> -defaultname.txt
> > Provide in this file the name that will display by default in the name textbox on start

> -receive.txt
> > A list of physical addresses you are allowed to receive packets from. Packets from nodes with addresses not appearing in this list will be ignored. Separate each entry with a new line. If this file is not given, all packets will be accepted. This file is used for testing purposes in order to create mock network orientations.