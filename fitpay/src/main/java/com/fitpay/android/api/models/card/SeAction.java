package com.fitpay.android.api.models.card;

import com.fitpay.android.api.models.apdu.ApduCommand;

import java.util.List;

/**
 * SE action
 */
public class SeAction {
    private List<ApduCommand> apduCommands;

    public List<ApduCommand> getApduCommands(){
        return apduCommands;
    }
}
