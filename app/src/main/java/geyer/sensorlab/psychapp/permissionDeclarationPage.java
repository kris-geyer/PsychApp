package geyer.sensorlab.psychapp;

public final class permissionDeclarationPage {

    /**
     * permissions to request
     *
     * place 1 next to permission if it is essential for app
     * place 2 next to permission if it is optional for app
     * must add an commented permission underneath to manifest for app to function
     */

    /**
     * Calendar
     */
    static final int READ_CALENDER = 0;
    //<uses-permission android:name="android.permission.READ_CALENDAR"/>
    static final int WRITE_CALENDAR = 0;
    //<uses-permission android:name="android.permission.WRITE_CALENDAR"/>

    /**
     * Call log
     */

    static final int READ_CALL_LOG = 0;
    //<uses-permission android:name="android.permission.READ_CALL_LOG"/>
    static final int WRITE_CALL_LOG = 0;
    //<uses-permission android:name="android.permission.WRITE_CALL_LOG"/>
    static final int PROCESS_OUTGOING_CALLS = 0;
    //<uses-permission android:name="android.permission.PROCESS_OUTGOING_CALLS"/>

    /**
     * Camera
     */

    static final int CAMERA = 0;
    //<uses-permission android:name="android.permission.CAMERA"/>

    /**
     * Contacts
     */

    static final int READ_CONTACTS = 0;
    //<uses-permission android:name="android.permission.READ_CONTACTS"/>
    static final int WRITE_CONTACTS = 0;
    //<uses-permission android:name="android.permission.WRITE_CONTACTS"/>
    static final int GET_ACCOUNTS = 0;
    //<uses-permission android:name="android.permission.GET_ACCOUNTS"/>

    /**
     * Location
     */

    static final int ACCESS_FINE_LOCATION = 0;
    //<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
    static final int ACCESS_COARSE_LOCATION = 0;
    //<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>

    /**
     * Microphone
     */

    static final int RECORD_AUDIO = 0;
    //<uses-permission android:name="android.permission.RECORD_AUDIO"/>

    /**
     * Phone
     */

    static final int READ_PHONE_STATE = 0;
    //<uses-permission android:name="android.permission.READ_PHONE_STATE"/>
    static final int READ_PHONE_NUMBERS = 0;
    //<uses-permission android:name="android.permission.READ_PHONE_NUMBERS"/>
    static final int CALL_PHONE = 0;
    //<uses-permission android:name="android.permission.CALL_PHONE"/>
    static final int ANSWER_PHONE_CALLS = 0;
    //<uses-permission android:name="android.permission.ANSWER_PHONE_CALLS"/>
    static final int ADD_VOICEMAIL = 0;
    //<uses-permission android:name="com.android.voicemail.permission.ADD_VOICEMAIL"/>
    static final int USE_SIP = 0;
    //<uses-permission android:name="android.permission.USE_SIP"/>

    /**
     * Sensors
     */

    static final int BODY_SENSORS = 0;
    //<uses-permission android:name="android.permission.BODY_SENSORS"/>

    /**
     * SMS
     */

    static final int SEND_SMS = 0;
    //<uses-permission android:name="android.permission.SEND_SMS"/>
    static final int RECEIVE_SMS = 0;
    //<uses-permission android:name="android.permission.RECEIVE_SMS"/>
    static final int READ_SMS = 0;
    //<uses-permission android:name="android.permission.READ_SMS"/>
    static final int RECEIVE_WAP_PUSH = 0;
    //<uses-permission android:name="android.permission.RECEIVE_WAP_PUSH"/>
    static final int RECEIVE_MMS = 0;
    //<uses-permission android:name="android.permission.RECEIVE_MMS"/>

    /**
     * External storage
     */

    static final int READ_EXTERNAL_STORAGE = 0;
    //<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
    static final int WRITE_EXTERNAL_STORAGE = 0;
    //<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
}
