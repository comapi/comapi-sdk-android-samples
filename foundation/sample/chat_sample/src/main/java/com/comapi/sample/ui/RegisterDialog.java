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

package com.comapi.sample.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.comapi.sample.R;

/**
 * Dialog to ask for profile id to start authenticated session.
 *
 * @author Marcin Swierczek
 */
public class RegisterDialog extends DialogFragment {

    private EditText profileIdEditText;

    /**
     * Interface to tell the Activity that the user has chosen the profile id to login as. Should be implemented by calling Activity.
     */
    interface RegisterInterface {
        /**
         * Profile id for the authenticated session has been chosen, the app should authenticate using this profile id as a subject in JWT token.
         *
         * @param profileId Profile id for the authenticated session to start.
         */
        void register(String profileId);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sets styling for the dialog.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            setStyle(STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
        } else {
            setStyle(STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
        }
        // Cannot cancel dialog by clicking outside dialog frame. We want to make sure user logs in before he start using the app.
        setCancelable(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.register_dialog, container, false);

        profileIdEditText = (EditText) view.findViewById(R.id.profile_id_edit_text);

        Button createButton = (Button) view.findViewById(R.id.start_button);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Tell the calling Activity what the profile id to authenticate user is
                Activity activity = getActivity();
                String profileId = profileIdEditText.getText().toString();
                if (activity != null && activity instanceof RegisterInterface && !TextUtils.isEmpty(profileId)) {
                    ((RegisterInterface) activity).register(profileId);
                    getDialog().dismiss();
                }
            }
        });

        return view;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        // Tell the calling Activity that user canceled the login process
        final Activity activity = getActivity();
        if (activity instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
        }
    }
}