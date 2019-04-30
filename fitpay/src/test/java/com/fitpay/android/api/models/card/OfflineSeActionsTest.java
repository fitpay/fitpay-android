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

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OfflineSeActionsTest extends BaseTestActions {

    @Test
    public void ensurePlatformConfigurationIsSetupCorrectly() {
        final String actionsString = "{\n" +
                "        \"topOfWallet\": {\n" +
                "            \"apduCommands\": [\n" +
                "                {\n" +
                "                    \"sequence\": 0,\n" +
                "                    \"command\": \"00A4040009A00000015143525300\",\n" +
                "                    \"type\": \"SELECT\",\n" +
                "                    \"description\": \"Select CRS\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"sequence\": 1,\n" +
                "                    \"command\": \"80F002020D4F0BA0000000041010AA030002\",\n" +
                "                    \"type\": \"SET_STATUS\",\n" +
                "                    \"description\": \"Set Volatile Priority\"\n" +
                "                }\n" +
                "            ]\n" +
                "        },\n" +
                "        \"activate\": {\n" +
                "            \"apduCommands\": [\n" +
                "                {\n" +
                "                    \"sequence\": 0,\n" +
                "                    \"command\": \"00A4040009A00000015143525300\",\n" +
                "                    \"type\": \"SELECT\",\n" +
                "                    \"description\": \"Select CRS\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"sequence\": 1,\n" +
                "                    \"command\": \"80C3010000\",\n" +
                "                    \"type\": \"SET_PARAM\",\n" +
                "                    \"description\": \"Deactivate Default Card\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"sequence\": 2,\n" +
                "                    \"command\": \"80F002020D4F0BA0000000041010AA030002\",\n" +
                "                    \"type\": \"SET_DEFAULT_CARD\",\n" +
                "                    \"description\": \"Set Default Card\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"sequence\": 3,\n" +
                "                    \"command\": \"80C3010100\",\n" +
                "                    \"type\": \"SET_PARAM\",\n" +
                "                    \"description\": \"Activate Default Card\"\n" +
                "                }\n" +
                "            ]\n" +
                "        },\n" +
                "        \"deactivate\": {\n" +
                "            \"apduCommands\": [\n" +
                "                {\n" +
                "                    \"sequence\": 0,\n" +
                "                    \"command\": \"00A4040009A00000015143525300\",\n" +
                "                    \"type\": \"SELECT\",\n" +
                "                    \"description\": \"Select CRS\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"sequence\": 1,\n" +
                "                    \"command\": \"80C3010000\",\n" +
                "                    \"type\": \"SET_PARAM\",\n" +
                "                    \"description\": \"Deactivate Default Card\"\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    }";
        OfflineSeActions actions = Constants.getGson().fromJson(actionsString, OfflineSeActions.class);
        Assert.assertNotNull("actions is empty", actions);
        List<ApduCommand> towCommands = actions.getCommands(OfflineSeActionTypes.TOP_OF_WALLET);
        Assert.assertNotNull("tow commands is empty", towCommands);
        List<ApduCommand> activate = actions.getCommands(OfflineSeActionTypes.ACTIVATE);
        Assert.assertNotNull("activate is empty", activate);
        List<ApduCommand> deactivate = actions.getCommands(OfflineSeActionTypes.DEACTIVATE);
        Assert.assertNotNull("deactivate is empty", deactivate);
    }
}
