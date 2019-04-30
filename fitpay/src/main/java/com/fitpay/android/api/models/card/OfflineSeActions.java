package com.fitpay.android.api.models.card;

import androidx.annotation.Nullable;

import com.fitpay.android.api.enums.OfflineSeActionTypes;
import com.fitpay.android.api.models.apdu.ApduCommand;

import java.util.List;
import java.util.Map;

/**
 * Offline SE Actions
 */
public final class OfflineSeActions {
    private Map<String, SeAction> actions;

    public OfflineSeActions(Map<String, SeAction> actions){
        this.actions = actions;
    }

    /**
     * Get SE action commands
     * @param type action type
     * @return commands
     */
    @Nullable
    public List<ApduCommand> getCommands(@OfflineSeActionTypes.Type String type) {
        SeAction action = getAction(type);
        return action != null ? action.getApduCommands() : null;
    }

    /**
     * Get SE action
     * @param type action type
     * @return se action
     */
    @Nullable
    public SeAction getAction(@OfflineSeActionTypes.Type String type) {
        return actions != null && actions.containsKey(type) ? actions.get(type) : null;
    }
}
