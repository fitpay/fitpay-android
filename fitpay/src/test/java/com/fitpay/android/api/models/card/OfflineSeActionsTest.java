package com.fitpay.android.api.models.card;

import com.fitpay.android.BaseTestActions;
import com.fitpay.android.api.enums.OfflineSeActionTypes;
import com.fitpay.android.api.models.apdu.ApduCommand;
import com.fitpay.android.utils.Constants;
import com.fitpay.android.utils.NamedResource;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;

import static org.junit.Assert.assertNotNull;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OfflineSeActionsTest extends BaseTestActions {

    @ClassRule
    public static NamedResource rule = new NamedResource(OfflineSeActionsTest.class);

    @Test
    public void ensurePlatformConfigurationIsSetupCorrectly() {
        final String actionsString = "{\n" +
                "\"topOfWallet\": {\n" +
                "\"apduCommands\": [\n" +
                "{\n" +
                "\"sequence\": 0,\n" +
                "\"command\": \"00A4040009A00000015143525300\",\n" +
                "\"type\": \"SELECT\",\n" +
                "\"description\": \"Select CRS\",\n" +
                "\"continueOnFailure\": false\n" +
                "}\n" +
                "]\n" +
                "}\n" +
                "}";
        OfflineSeActions actions = Constants.getGson().fromJson(actionsString, OfflineSeActions.class);
        Assert.assertNotNull("actions is empty", actions);
        List<ApduCommand> towCommands = actions.getCommands(OfflineSeActionTypes.TOP_OF_WALLET);
        Assert.assertNotNull("tow commands is empty", towCommands);
    }
}
