package com.fitpay.android.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for listener. Uses in {@link NotificationManager}
 */
public abstract class Listener {

    protected Map<Class, Command> mCommands;

    private String filter;
    private boolean filterApplied = false;

    public Listener() {
        mCommands = new HashMap<>();
    }

    public Listener(String filter) {
        this();
        this.filter = filter;
    }

    Map<Class, Command> getCommands() {
        if (!StringUtils.isEmpty(filter) && !filterApplied) {
            filterApplied = true;

            Map<Class, Command> filterCommands = new HashMap<>(mCommands.size());
            for (Map.Entry<Class, Command> pair : mCommands.entrySet()) {
                filterCommands.put(pair.getKey(), new FilterCommand() {
                    @Override
                    public String filter() {
                        return filter;
                    }

                    @Override
                    public void execute(Object data) {
                        pair.getValue().execute(data);
                    }
                });
            }

            mCommands = filterCommands;
        }

        return mCommands;
    }
}