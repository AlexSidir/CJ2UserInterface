package com.egduserinterface.cj2userinterface;

import java.util.ArrayList;

public class NavigationActivity {

    public static String getDestination(ArrayList<String> address) {

        String searchQuery = address.get(0);
        searchQuery = searchQuery.replace("navigate to","");
        //showToastMessages(searchQuery);
        return searchQuery;
    }
}