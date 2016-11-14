package com.fitpay.android;

import com.fitpay.android.api.models.card.CreditCard;
import com.fitpay.android.api.models.collection.Collections;
import com.fitpay.android.api.models.device.Commit;
import com.fitpay.android.api.models.device.Device;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Created by tgs on 5/31/16.
 */
public class CommitTest2 extends TestActions {

    @Test
    public void testCanGetCommitsAfter() throws Exception {

        Device device = getTestDevice();
        Device createdDevice = createDevice(user, device);
        assertNotNull("created device", createdDevice);

        Collections.DeviceCollection devices = getDevices(user);
        assertNotNull("devices collection should not be null", devices);
        assertEquals("should have one device", 1, devices.getTotalResults());

        String pan = "9999504454545450";
        CreditCard creditCard = getTestCreditCard(pan);
        CreditCard createdCard = createCreditCard(user, creditCard);
        assertNotNull("card not created", createdCard);

        createdCard = acceptTerms(createdCard);

        pan = "9999504454545451";
        creditCard = getTestCreditCard(pan);
        createdCard = createCreditCard(user, creditCard);
        assertNotNull("card not created", createdCard);

        createdCard = acceptTerms(createdCard);

        Collections.CommitsCollection commits = getCommits(createdDevice, null);
        assertNotNull("commits collection", commits);
        int totalResults = commits.getTotalResults();
        assertTrue("number of commits should be 2 or more.  Got: " + commits.getTotalResults(), commits.getTotalResults() >= 2);

        for (Commit commit : commits.getResults()) {
            Collections.CommitsCollection lastCommits = getCommits(createdDevice, commit.getCommitId());
            assertEquals("number of commits with lastId", --totalResults, lastCommits.getTotalResults());
        }
    }

    @Test
    public void testCanGetCommits() throws Exception {

        Device device = getTestDevice();
        Device createdDevice = createDevice(user, device);
        assertNotNull("created device", createdDevice);

        Collections.DeviceCollection devices = getDevices(user);
        assertNotNull("devices collection should not be null", devices);
        assertEquals("should have one device", 1, devices.getTotalResults());

        String pan = "9999504454545450";
        CreditCard creditCard = getTestCreditCard(pan);
        CreditCard createdCard = createCreditCard(user, creditCard);
        assertNotNull("card not created", createdCard);

        createdCard = acceptTerms(createdCard);

        pan = "9999504454545451";
        creditCard = getTestCreditCard(pan);
        createdCard = createCreditCard(user, creditCard);
        assertNotNull("card not created", createdCard);

        createdCard = acceptTerms(createdCard);

        Collections.CommitsCollection commits = getCommits(createdDevice, null);
        assertNotNull("commits collection", commits);
        int totalResults = commits.getTotalResults();
        assertTrue("number of commits should be 2 or more.  Got: " + totalResults, totalResults >= 2);
    }

    @Test
    public void testCanGetAllCommits() throws Exception {

        Device device = getTestDevice();
        Device createdDevice = createDevice(user, device);
        assertNotNull("created device", createdDevice);

        Collections.DeviceCollection devices = getDevices(user);
        assertNotNull("devices collection should not be null", devices);
        assertEquals("should have one device", 1, devices.getTotalResults());

        String pan = "9999504454545450";
        CreditCard creditCard = getTestCreditCard(pan);
        CreditCard createdCard = createCreditCard(user, creditCard);
        assertNotNull("card not created", createdCard);

        createdCard = acceptTerms(createdCard);

        pan = "9999504454545451";
        creditCard = getTestCreditCard(pan);
        createdCard = createCreditCard(user, creditCard);
        assertNotNull("card not created", createdCard);

        createdCard = acceptTerms(createdCard);

        Collections.CommitsCollection commits = getCommits(createdDevice, null);
        assertNotNull("commits collection", commits);
        int totalResults = commits.getTotalResults();
        assertTrue("number of commits should be 2 or more.  Got: " + commits.getTotalResults(), commits.getTotalResults() >= 2);

        commits = getAllCommits(createdDevice, null);
        assertNotNull("allCommits collection", commits);
        assertEquals("number of allCommits", totalResults, commits.getTotalResults());

        for (Commit commit : commits.getResults()) {
            Collections.CommitsCollection lastCommits = getAllCommits(createdDevice, commit.getCommitId());
            assertEquals("number of commits with lastId", --totalResults, lastCommits.getTotalResults());
        }
    }

    @Test
    public void testCanGetLofOfCommits() throws Exception {

        Device device = getTestDevice();
        Device createdDevice = createDevice(user, device);
        assertNotNull("created device", createdDevice);

        Collections.DeviceCollection devices = getDevices(user);
        assertNotNull("devices collection should not be null", devices);
        assertEquals("should have one device", 1, devices.getTotalResults());

        CreditCard[] creditCardArray = new CreditCard[8];
        String panBase = "99995044545454";
        int count = 0;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 4; j++) {
                String pan = panBase + i + j;
                CreditCard creditCard = getTestCreditCard(pan);
                CreditCard createdCard = createCreditCard(user, creditCard);
                assertNotNull("card not created", createdCard);

                creditCardArray[count] = acceptTerms(createdCard);
                count++;

            }
        }

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < count; j++) {
                if (creditCardArray[j].canMakeDefault()) {
                    makeDefaultCard(creditCardArray[j]);
                }
            }
        }

        Collections.CommitsCollection commits = getCommits(createdDevice, null);
        assertNotNull("commits collection", commits);
        int totalResults = commits.getTotalResults();
        assertTrue("number of commits should be 10 or more.  Got: " + commits.getTotalResults(), commits.getTotalResults() >= 10);

        commits = getAllCommits(createdDevice, null);
        assertNotNull("allCommits collection", commits);
        assertEquals("number of allCommits", totalResults, commits.getTotalResults());
    }
}
