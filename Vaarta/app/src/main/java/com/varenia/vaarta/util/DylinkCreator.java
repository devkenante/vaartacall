package com.varenia.vaarta.util;

import android.net.Uri;

import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.ktx.Firebase;

public class DylinkCreator {

     public static Uri CreateLink(String base64){
         String url_value=Constants.MAIN_DYNAMIC_URL+base64;
         DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                 .setLink(Uri.parse(url_value))
                 .setDomainUriPrefix("https://vaartaz.page.link/")
                 // Open links with this app on Android
                 .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                 .buildDynamicLink();

         Uri dynamicLinkUri = dynamicLink.getUri();


         return dynamicLinkUri;
    }
}
