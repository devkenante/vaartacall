package com.varenia.vaarta.util;

import org.jetbrains.annotations.NotNull;

/**
 * Created by VCIMS-PC2 on 04-01-2018.
 */

public class Constants {



    public static final int REQUEST_PERMISSION_KEY = 1;
    public static final String QB_USERS_FOR_DIALOG = "qb_users";
    public static final String PROGRESS_DIALOG = "p_dialog";

    public static final String EXTRA_DIALOG_ID = "dialog_id";
    public static final String EXTRA_CHAT_DIALOG_ID = "chat_dialog_id";
    public static final String EXTRA_DIALOG_OCCUPANTS = "dialog_occupants";
    public static final String EXTRA_AS_LISTENER = "dialog_listener";
    public static final String EXTRA_USER_TYPE = "user_type";
    public static final String EXTRA_QB_OCCUPANTS_IDS = "qb_occupants_ids";
    public static final String EXTRA_OPPONENT_ID = "opponent_id";
    public static final String EXTRA_PUBLISHED_USERS_ARRAY = "published_array";
    public static final String CURRENT_PRESENT_USERS = "current_present_users";
    public static final String EXTRA_SEE_ARRAY = "see_array";
    public static final String EXTRA_CHAT_ARRAY = "chat_array";
    public static final String EXTRA_HEAR_ARRAY = "hear_array";
    public static final String EXTRA_VIDEO_SHOW_WIDTH = "video_show_width";
    public static final String EXTRA_VIDEO_SHOW_HEIGHT = "video_show_height";
    public static final String EXTRA_USER_NAME = "user_name";
    public static final String EXTRA_FIREBASE_LIST = "firebase_list";
    public static final String EXTRA_FB_LIST_ITEM = "fb_list_item";
    public static final String EXTRA_FB_VIDEO_CLOSE = "fb_video_close";
    public static final String EXTRA_TIME = "time";
    public static final String EXTRA_DAY = "day";
    public static final String EXTRA_DATE = "date";
    public static final String USER_NAMES = "names";
    public static final String EXTRA_GROUP_ONGOING = "ongoing_group";
    public static final String EXTRA_IS_ROOM_SECURE = "is_room_secure";
    public static final String EXTRA_DYNAMIC_LINK_ENTRY = "is_dynamic_link_entry";

    public static final String EXTRA_FVIDEO_MODEL = "firebase_video";
    public static final String EXTRA_IS_MOD = "is_mod";

    public static final String EXTRA_DYNAMIC_LINK_URI = "dynamic_call_uri";

    public static final String APP_NAME = "Kenante";
    public static final String LOGIN_STATUS = "LOGIN_STATUS";
    public static final String USER_ID = "USER_ID";
    public static final String QB_ID = "QB_ID";
    public static final String LOGIN = "LOGIN";
    public static final String FULLNAME = "FULLNAME";
    public static final String PASSWORD = "PASSWORD";
    public static final String EMAIL = "EMAIL";
    public static final String EXT_ID = "EXT_ID";
    public static final String FACEBOOK = "FACEBOOK";
    public static final String TWITTER = "TWITTER";
    public static final String TWITTER_DIGITS = "TWITTER_DIGITS";
    public static final String TAGS = "TAGS";
    public static final String LAST_SIGN_IN = "LAST_SIGN_IN";
    public static final String CREATED_AT = "CREATED_AT";
    public static final String APP_ID = "APP_ID";
    public static final String AUTH_KEY = "AUTH_KEY";
    public static final String AUTH_SECRET = "AUTH_SECRET";
    public static final String ACCOUNT_KEY = "ACCOUNT_KEY";
    public static final String API_DOMAIN = "API_DOMAIN";
    public static final String CHAT_DOMAIN = "CHAT_DOMAIN";
    public static final String JANUS_SERVER = "JANUS_SERVER";
    public static final String JANUS_PROTOCOL = "JANUS_PROTOCOL";
    public static final String JANUS_PLUGIN = "JANUS_PLUGIN";

    public static final String USER_TYPE = "USER_TYPE";
    public static final String SUBSCRIPTION_ID = "SUBSCRIPTION_ID";
    public static final String CALL_STARTED = "CALL_STARTED";

    public static final String PREF_LANGUAGE = "pref_language";

    //ADMIN will also record Moderator Screen
    public static final String isIncomingCall = "incoming";


    //Web Services
    public static final String MAIN_URL = "https://vareniacims.com/";
    //public static final String LOGIN_URL = MAIN_URL + "vcimsweb/wsr.php?tasks=vcuserlogin";
    //public static final String LOGIN_URL = MAIN_URL + "vcimsweb/wsr.php?tasks=vcuserlogintest";
    public static final String LOGIN_URL = MAIN_URL + "siteadmin/api/vaarta_user_reg";

    //public static final String SYNC_USERS = MAIN_URL + "vcimsweb/wsr.php?tasks=vcusertaglist";
    //public static final String SYNC_USERS = MAIN_URL + "vcimsweb/wsr.php?tasks=vcusertaglisttest";
    public static final String SYNC_USERS = MAIN_URL + "vcimsweb/wsr.php?tasks=vcusertaglistdev";

    public static final String LOGOUT_URL = MAIN_URL + "vcimsweb/wsr.php?tasks=vcuserlogout";

    public static final String ICE_FAILED_REASON = "ICE failed";

    public static final String PROPERTY_SAVE_TO_HISTORY = "save_to_history";

    public static final int CHAT_HISTORY_ITEMS_PER_PAGE = 20;
    public static final String CHAT_HISTORY_ITEMS_SORT_FIELD = "date_sent";

    public static final String STATUS = "status";
    public static final String KEY = "key";

    //Network Callbacks
    public static final String ON_AVAILABLE = "onAvailable";
    public static final String ON_LOSING = "onLosing";
    public static final String ON_LOST = "onLost";
    public static final String ON_UNAVAILABLE = "onUnavailable";
    public static final String ON_CAPABILITIES_CHANGED = "onCapabilitiesChanged";
    public static final String ON_LINK_PROPERTIES_CHANGED = "onLinkPropertiesChanged";
    public static final String EXTRA_MAX_MS_TO_LIVE = "max_ms_to_live";

    public static final String ENLARGE_VIDEO_FRAG = "enlarge_video";
    public static final String BRIEF_FRAG = "brief_frag";
    public static final String HISTORY_FRAG = "history";
    public static final String FAQ_FRAG = "faq";
    public static final String CHANGE_LANGUAGE_FRAG = "change_language";
    public static final String NOTIFICATION_FRAG = "notification";
    public static final String USERS_LIST_FRAG = "users_list";
    public static final String FILE_OPENER_FRAG = "file_opener";
    public static final String PIP_FRAG = "pip_frag";
    public static final String VIDEO_SHOW_FRAG = "video_show";

    //Firebase Show Fragment
    public static final String GROUP_LIST = "group_list";
    public static final String GROUP_IDS = "group_ids";
    public static final String VIDEO_LIST = "video_list";
    public static final String IMAGE_LIST = "image_list";
    public static final String PPT_LIST = "ppt_list";

    public static final String DIALOG_ID = "dialogId";
    public static final String OCCUPANTS = "occupants";
    public static final String ROOM = "room";
    public static final String AWS_KEY = "aws_key";
    public static final String AWS_SECRET_KEY = "aws_secret_key";


    //New Kenante Janus Constants
    public static final String ROOM_ID = "room_id";
    public static final String KID = "k_user_id";
    public static final String NAME = "name";
    public static final String DNAME = "dname";
    public static final String AUDIO_CODEC = "audio_codec";
    public static final String VIDEO_CODEC = "video_codec";
    public static final String RECORDING = "recording";
    public static final String RECORDING_DIR = "recording_fir";
    public static final String BITRATE = "bitrate";
    public static final String VAARTA_SERVER_URL = "wss://dev.kenante.com:8989/janus";
    public static final String VAARTA_PROTOCOL_VAL = "janus-protocol";
    public static final String VAARTA_PLUGIN_VAL = "janus.plugin.videoroom";
    public static final String CHAT_END_POINT = "wss://dev.kenante.com/";

    public static final String CREATE_ROOM = "api/janus_api/";
    public static final String VAARTA_SERVER_URL_WEB = "https://dev.kenante.com/";
    public static final String UID = "uid";
    public static final String MOBILE = "mobile";
    public static final String TOKEN = "token";
    public static final String CREATED_AT_LOGiN = "created_at";

    public static final String LAST_LOGIN = "last_login";
    @NotNull
    public static final String MAIN_DYNAMIC_URL ="https://vaartacall.com/vc/j/";
    public static final String MEETING_VALIDATION_URL="https://vareniacims.com/siteadmin/api/vaarta_check_meeding_id";
    public static final String MEETING_INVITATATION_URL="https://vareniacims.com/siteadmin/api/vaarta_send_invite";

    // public static final String NAME = "name";



}