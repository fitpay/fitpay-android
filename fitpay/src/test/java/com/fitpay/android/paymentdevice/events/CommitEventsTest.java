package com.fitpay.android.paymentdevice.events;

import com.fitpay.android.api.models.device.Commit;
import com.fitpay.android.paymentdevice.enums.CommitResult;
import com.fitpay.android.utils.Constants;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CommitEventsTest {

    private Commit commit;

    @Before
    public void before() {
        String commitStr = "{\n" +
                "\"commitId\":\"918273\",\n" +
                "\"commitType\":\"CREDITCARD_CREATED\"\n" +
                "}";

        commit = Constants.getGson().fromJson(commitStr, Commit.class);
    }

    @Test
    public void successTest() {
        CommitSuccess success = new CommitSuccess.Builder()
                .commit(commit)
                .build();

        Assert.assertEquals(success.getCommit(), commit);
        Assert.assertEquals(success.getCommitId(), commit.getCommitId());
        Assert.assertEquals(success.getCommitType(), commit.getCommitType());
    }

    @Test
    public void skippedTest() {
        CommitSkipped skipped = new CommitSkipped.Builder()
                .commit(commit)
                .errorCode(101)
                .errorMessage("101Message")
                .build();

        Assert.assertEquals(skipped.getCommit(), commit);
        Assert.assertEquals(skipped.getCommitId(), commit.getCommitId());
        Assert.assertEquals(skipped.getCommitType(), commit.getCommitType());
        Assert.assertEquals(101, skipped.getErrorCode());
        Assert.assertEquals("101Message", skipped.getErrorMessage());
    }

    @Test
    public void failedTest() {
        CommitFailed failed = new CommitFailed.Builder()
                .commit(commit)
                .errorCode(101)
                .errorMessage("101Message")
                .build();

        Assert.assertEquals(failed.getCommit(), commit);
        Assert.assertEquals(failed.getCommitId(), commit.getCommitId());
        Assert.assertEquals(failed.getCommitType(), commit.getCommitType());
        Assert.assertEquals(101, failed.getErrorCode());
        Assert.assertEquals("101Message", failed.getErrorMessage());
    }

    @Test
    public void commitResultTest() {
        CommitResult result = new CommitResult(CommitResult.SUCCESS);
        Assert.assertEquals(CommitResult.SUCCESS, result.getType());
    }
}
