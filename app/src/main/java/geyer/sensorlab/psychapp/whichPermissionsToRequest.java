package geyer.sensorlab.psychapp;

import android.Manifest;

import java.util.ArrayList;

public class whichPermissionsToRequest {

    public ArrayList generatePermissionToRequest(int i){
        ArrayList<String> permissionsToRequest = new ArrayList<>();

        /**
        calender
         */

        if(permissionDeclarationPage.READ_CALENDER == i){
            permissionsToRequest.add(android.Manifest.permission.READ_CALENDAR);
        }

        if(permissionDeclarationPage.WRITE_CALENDAR == i){
            permissionsToRequest.add(Manifest.permission.WRITE_CALENDAR);
        }

        /**
         * Call log
         */

        //requires permission 16
        if(permissionDeclarationPage.READ_CALL_LOG == i){
            permissionsToRequest.add(Manifest.permission.READ_CALL_LOG);
        }
        //requires permission 16
        if(permissionDeclarationPage.WRITE_CALL_LOG == i){
            permissionsToRequest.add(Manifest.permission.WRITE_CALL_LOG);
        }

        if(permissionDeclarationPage.PROCESS_OUTGOING_CALLS == i){
            permissionsToRequest.add(Manifest.permission.PROCESS_OUTGOING_CALLS);
        }

        /**
         * Camera
         */

        if(permissionDeclarationPage.CAMERA == i){
            permissionsToRequest.add(Manifest.permission.CAMERA);
        }

        /**
         * Contacts
         */

        if(permissionDeclarationPage.READ_CONTACTS == i){
            permissionsToRequest.add(Manifest.permission.READ_CONTACTS);
        }
        if(permissionDeclarationPage.WRITE_CONTACTS == i){
            permissionsToRequest.add(Manifest.permission.WRITE_CONTACTS);
        }
        if(permissionDeclarationPage.GET_ACCOUNTS == i){
            permissionsToRequest.add(Manifest.permission.GET_ACCOUNTS);
        }

        /**
         * Location
         */

        if(permissionDeclarationPage.ACCESS_FINE_LOCATION == i){
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(permissionDeclarationPage.ACCESS_COARSE_LOCATION == i){
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        /**
         * Microphone
         */

        if(permissionDeclarationPage.RECORD_AUDIO == i){
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO);
        }

        /**
         * Phone
         */

        if(permissionDeclarationPage.READ_PHONE_STATE == i){
            permissionsToRequest.add(Manifest.permission.READ_PHONE_STATE);
        }
        //requires permission read phone numbers
        if(permissionDeclarationPage.READ_PHONE_NUMBERS == i){
            permissionsToRequest.add(Manifest.permission.READ_PHONE_NUMBERS);
        }
        if(permissionDeclarationPage.CALL_PHONE == i){
            permissionsToRequest.add(Manifest.permission.CALL_PHONE);
        }
        //requires permission answer phone
        if(permissionDeclarationPage.ANSWER_PHONE_CALLS == i){
            permissionsToRequest.add(Manifest.permission.ANSWER_PHONE_CALLS);
        }
        if(permissionDeclarationPage.ADD_VOICEMAIL == i){
            permissionsToRequest.add(Manifest.permission.ADD_VOICEMAIL);
        }
        if(permissionDeclarationPage.USE_SIP == i){
            permissionsToRequest.add(Manifest.permission.USE_SIP);
        }

        /**
         * Sensors
         */

        if(permissionDeclarationPage.BODY_SENSORS == i){
            permissionsToRequest.add(Manifest.permission.BODY_SENSORS);
        }

        /**
         * SMS
         */

        if(permissionDeclarationPage.SEND_SMS == i){
            permissionsToRequest.add(Manifest.permission.SEND_SMS);
        }
        if(permissionDeclarationPage.RECEIVE_SMS == i){
            permissionsToRequest.add(Manifest.permission.RECEIVE_SMS);
        }
        if(permissionDeclarationPage.READ_SMS == i){
            permissionsToRequest.add(Manifest.permission.READ_SMS);
        }
        if(permissionDeclarationPage.RECEIVE_WAP_PUSH == i){
            permissionsToRequest.add(Manifest.permission.RECEIVE_WAP_PUSH);
        }
        if(permissionDeclarationPage.RECEIVE_MMS == i){
            permissionsToRequest.add(Manifest.permission.RECEIVE_MMS);
        }

        /**
         * External storage
         */

        //requires sdk version 16
        if(permissionDeclarationPage.READ_EXTERNAL_STORAGE == i){
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if(permissionDeclarationPage.WRITE_EXTERNAL_STORAGE == i){
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        return permissionsToRequest;
    }

}
