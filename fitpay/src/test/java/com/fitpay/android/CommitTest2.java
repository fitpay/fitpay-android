package com.fitpay.android;

import com.fitpay.android.api.callbacks.ApiCallback;
import com.fitpay.android.api.enums.ResponseState;
import com.fitpay.android.api.enums.ResultCode;
import com.fitpay.android.api.models.card.CreditCard;
import com.fitpay.android.api.models.card.CreditCardInfo;
import com.fitpay.android.api.models.collection.Collections;
import com.fitpay.android.api.models.device.Commit;
import com.fitpay.android.api.models.device.CommitConfirm;
import com.fitpay.android.api.models.device.Device;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Created by tgs on 5/31/16.
 */
public class CommitTest2 extends TestActions {

    @Test
    public void testCanConfirmCommits() throws Exception {

        Device device = getTestDevice();
        Device createdDevice = createDevice(user, device);
        assertNotNull("created device", createdDevice);

        Collections.DeviceCollection devices = getDevices(user);
        assertNotNull("devices collection should not be null", devices);
        assertEquals("should have one device", 1, devices.getTotalResults());

        String pan = "9999504454545450";
        CreditCardInfo creditCardInfo = getTestCreditCardInfo(pan);
        CreditCard createdCard = createCreditCard(user, creditCardInfo);
        assertNotNull("card not created", createdCard);

        createdCard = acceptTerms(createdCard);
        waitForActivation(createdCard);

        pan = "9999504454545451";
        creditCardInfo = getTestCreditCardInfo(pan);
        createdCard = createCreditCard(user, creditCardInfo);
        assertNotNull("card not created", createdCard);

        acceptTerms(createdCard);

        Collections.CommitsCollection commits = getCommits(createdDevice, null);
        assertNotNull("commits collection", commits);
        assertTrue("number of commits should be 2 or more.  Got: " + commits.getTotalResults(), commits.getTotalResults() >= 2);

        for (Commit commit : commits.getResults()) {
            final CountDownLatch latch = new CountDownLatch(1);

            if (commit.canConfirmCommit()) {
                CommitConfirm confirm = new CommitConfirm(ResponseState.SUCCESS);

                commit.confirm(confirm, new ApiCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                        fail("commit confirm failed");
                    }
                });
            } else {
                // TODO: uncomment out once the paltform supports this feature fully
//                if (!commit.getCommitType().equals(CommitTypes.APDU_PACKAGE)) {
//                    fail("expected confirm link on commit: " + commit);
//                }
            }

            latch.await(5000, TimeUnit.MILLISECONDS);
        }
    }

    @Test
    public void testCanGetCommitsAfter() throws Exception {

        Device device = getTestDevice();
        Device createdDevice = createDevice(user, device);
        assertNotNull("created device", createdDevice);

        Collections.DeviceCollection devices = getDevices(user);
        assertNotNull("devices collection should not be null", devices);
        assertEquals("should have one device", 1, devices.getTotalResults());

        String pan = "9999504454545450";
        CreditCardInfo creditCardInfo = getTestCreditCardInfo(pan);
        CreditCard createdCard = createCreditCard(user, creditCardInfo);
        assertNotNull("card not created", createdCard);

        createdCard = acceptTerms(createdCard);
        waitForActivation(createdCard);

        pan = "9999504454545451";
        creditCardInfo = getTestCreditCardInfo(pan);
        createdCard = createCreditCard(user, creditCardInfo);
        assertNotNull("card not created", createdCard);

        acceptTerms(createdCard);

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
        CreditCardInfo creditCardInfo = getTestCreditCardInfo(pan);
        CreditCard createdCard = createCreditCard(user, creditCardInfo);
        assertNotNull("card not created", createdCard);

        createdCard = acceptTerms(createdCard);
        waitForActivation(createdCard);

        pan = "9999504454545451";
        creditCardInfo = getTestCreditCardInfo(pan);
        createdCard = createCreditCard(user, creditCardInfo);
        assertNotNull("card not created", createdCard);

        acceptTerms(createdCard);

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
        CreditCardInfo creditCardInfo = getTestCreditCardInfo(pan);
        CreditCard createdCard = createCreditCard(user, creditCardInfo);
        assertNotNull("card not created", createdCard);

        createdCard = acceptTerms(createdCard);
        waitForActivation(createdCard);

        pan = "9999504454545451";
        creditCardInfo = getTestCreditCardInfo(pan);
        createdCard = createCreditCard(user, creditCardInfo);
        assertNotNull("card not created", createdCard);

        acceptTerms(createdCard);

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
                CreditCardInfo creditCardInfo = getTestCreditCardInfo(pan);
                CreditCard createdCard = createCreditCard(user, creditCardInfo);
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

        //wait for all commits
        final int correctResult = 39;
        int totalResults = 0;

        for (int i = 0; i < 3; i++) {
            Collections.CommitsCollection commits = getAllCommits(createdDevice, null);
            assertNotNull(commits);
            assertTrue("number of commits should be 10 or more.  Got: " + commits.getTotalResults(), commits.getTotalResults() >= 10);

            totalResults = commits.getTotalResults();
        }
        assertEquals("number of commits should be 39.  Got: " + totalResults, totalResults, correctResult);
    }
}
