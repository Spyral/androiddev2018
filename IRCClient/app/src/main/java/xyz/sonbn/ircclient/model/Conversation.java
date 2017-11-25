package xyz.sonbn.ircclient.model;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by sonbn on 11/22/2017.
 */

public class Conversation extends RealmObject {
    private RealmList<Message> mMessages;
}
