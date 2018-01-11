/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Comapi (trading name of Dynmark International Limited)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons
 * to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.comapi.sample.store;

import android.util.Log;

import com.comapi.chat.model.ChatConversation;
import com.comapi.chat.model.ChatConversationBase;
import com.comapi.chat.model.ChatMessage;
import com.comapi.chat.model.ChatMessageStatus;
import com.comapi.chat.model.ChatStore;
import com.comapi.sample.Const;
import com.comapi.sample.store.model.DBChatMessage;
import com.comapi.sample.store.model.DBConversation;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Sample implementation of the ChatStore interface that executes all changes to db in a single transaction using Realm Java database.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
@SuppressWarnings("WeakerAccess")
public class ChatStoreImplementation extends ChatStore {

    /**
     * Realm Java database instance. You can find documentation here https://realm.io/docs/java/latest/
     */
    private Realm realm;

    /**
     * Profile id of an logged in user to check if an message was sent by the same user that has received it.
     */
    private final String forProfile;

    /**
     * Recommended constructor.
     *
     * @param forProfile The logged in user profile id.
     */
    public ChatStoreImplementation(String forProfile) {
        if (forProfile == null) {
            Log.e(Const.TAG, "Null profile id in Chat Store instance.");
        }
        this.forProfile = forProfile;
    }

    /*

        Implementation of ChatStore interface which allows the SDK to internally manipulate the data which is accessed by the UI components of the app.

     */

    @Override
    public ChatConversationBase getConversation(String conversationId) {
        try {
            checkState();
            // Find conversation with given conversationId and adapt it for the SDK
            return DBConversation.adapt(realm.where(DBConversation.class).equalTo(DBConversation.CONVERSATION_ID, conversationId).findFirst());
        } catch (IllegalStateException e) {
            Log.e(Const.TAG,e.getLocalizedMessage());
        }  catch (Exception e) {
            e.printStackTrace();
            // End transaction if failed
            endTransaction();
        }
        return null;
    }

    @Override
    public List<ChatConversationBase> getAllConversations() {
        try {
            checkState();
            // Find all conversations that are not soft deleted. Conversation can be sorted based on e.g. timestamp for last received message in it.
            return DBConversation.adapt(realm.where(DBConversation.class).equalTo(DBConversation.IS_DELETED, false).findAllSorted(DBConversation.UPDATED_ON, Sort.DESCENDING));
        } catch (IllegalStateException e) {
            Log.e(Const.TAG,e.getLocalizedMessage());
        }  catch (Exception e) {
            e.printStackTrace();
            // End transaction if failed
            endTransaction();
        }
        return new ArrayList<>();
    }

    @Override
    public boolean upsert(ChatConversation c) {
        try {
            checkState();
            // Find conversation with given conversationId
            DBConversation saved = realm.where(DBConversation.class).equalTo(DBConversation.CONVERSATION_ID, c.getConversationId()).findFirst();
            if (saved == null) {
                // Create conversation in db
                realm.copyToRealm(DBConversation.adapt(c));
            } else {
                // Update saved conversation with a new data.
                saved.updateFrom(c);
            }
            return true;
        } catch (IllegalStateException e) {
            Log.e(Const.TAG,e.getLocalizedMessage());
        }  catch (Exception e) {
            e.printStackTrace();
            // End transaction if failed
            endTransaction();
        }
        return false;
    }

    @Override
    public boolean update(ChatConversationBase c) {
        try {
            checkState();
            // Find conversation with given conversationId
            DBConversation saved = realm.where(DBConversation.class).equalTo(DBConversation.CONVERSATION_ID, c.getConversationId()).findFirst();
            if (saved != null) {
                // Update saved conversation with a new data.
                saved.updateFrom(c);
                return true;
            } else {
                Log.w(Const.TAG, "no conversation "+c.getConversationId()+" to update");
                return false;
            }
        } catch (IllegalStateException e) {
            Log.e(Const.TAG,e.getLocalizedMessage());
        }  catch (Exception e) {
            e.printStackTrace();
            // End transaction if failed
            endTransaction();
        }
        return false;
    }

    @Override
    public boolean deleteConversation(String conversationId) {
        try {
            checkState();
            // Find conversation with a given conversationId
            DBConversation saved = realm.where(DBConversation.class).equalTo(DBConversation.CONVERSATION_ID, conversationId).findFirst();
            if (saved != null) {
                // Set the conversation soft deleted
                saved.setDeleted(true);
            }
            return true;
        } catch (IllegalStateException e) {
            Log.e(Const.TAG,e.getLocalizedMessage());
        }  catch (Exception e) {
            e.printStackTrace();
            // End transaction if failed
            endTransaction();
        }
        return false;
    }

    @Override
    public boolean upsert(ChatMessage message) {
        try {
            checkState();
            // Adapt for Realm model
            DBChatMessage dbMessage = DBChatMessage.adapt(message, forProfile);
            realm.copyToRealmOrUpdate(dbMessage);
            return true;
        } catch (IllegalStateException e) {
            Log.e(Const.TAG,e.getLocalizedMessage());
        }  catch (Exception e) {
            e.printStackTrace();
            // End transaction if failed
            endTransaction();
        }
        return false;
    }

    @Override
    public boolean update(ChatMessageStatus status) {
        try {
            checkState();
            // Find message with a given messageId
            DBChatMessage saved = realm.where(DBChatMessage.class).equalTo(DBChatMessage.MESSAGE_ID, status.getMessageId()).findFirst();
            if (saved != null) {
                saved.updateStatus(status);
                return true;
            }
            return false;
        } catch (IllegalStateException e) {
            Log.e(Const.TAG,e.getLocalizedMessage());
        }  catch (Exception e) {
            e.printStackTrace();
            // End transaction if failed
            endTransaction();
        }
        return false;
    }

    @Override
    public boolean clearDatabase() {
        try {
            checkState();
            // Delete all db content
            realm.deleteAll();
            return true;
        } catch (IllegalStateException e) {
            Log.e(Const.TAG,e.getLocalizedMessage());
        }  catch (Exception e) {
            e.printStackTrace();
            // End transaction if failed
            endTransaction();
        }
        return false;
    }

    @Override
    public boolean deleteAllMessages(String conversationId) {
        try {
            checkState();
            // Find all messages for a given conversation
            RealmResults<DBChatMessage> dbSavedTempMessages = realm.where(DBChatMessage.class).equalTo(DBChatMessage.CONVERSATION_ID, conversationId).findAll();
            dbSavedTempMessages.deleteAllFromRealm();
            return true;
        } catch (IllegalStateException e) {
            Log.e(Const.TAG,e.getLocalizedMessage());
        }  catch (Exception e) {
            e.printStackTrace();
            // End transaction if failed
            endTransaction();
        }
        return false;
    }

    @Override
    public boolean deleteMessage(String conversationId, String messageId) {
        try {
            checkState();
            // Find message with a given messageId
            DBChatMessage saved = realm.where(DBChatMessage.class).equalTo(DBChatMessage.MESSAGE_ID, messageId).findFirst();
            if (saved != null) {
                saved.deleteFromRealm();
            }
            return true;
        } catch (IllegalStateException e) {
            Log.e(Const.TAG,e.getLocalizedMessage());
        } catch (Exception e) {
            e.printStackTrace();
            // End transaction if failed
            endTransaction();
        }
        return false;
    }

    @Override
    public void beginTransaction() {
        // check state
        if (realm != null) {
            Log.e(Const.TAG,"Transaction already started.");
            return;
        }
        // Create realm database with default configuration.
        realm = Realm.getDefaultInstance();
        realm.beginTransaction();
    }

    @Override
    public void endTransaction() {
        // check state
        if (realm == null) {
            Log.e(Const.TAG,"Transaction not started.");
            return;
        }
        realm.commitTransaction();
        realm.close();
    }

    /**
     * Checks if transaction was started.
     */
    private void checkState() {
        if (realm == null || !realm.isInTransaction()) {
            throw new IllegalStateException("["+Const.TAG+"] DB transaction not started.");
        }
    }
}