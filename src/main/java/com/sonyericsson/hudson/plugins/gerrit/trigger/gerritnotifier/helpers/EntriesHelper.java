package com.sonyericsson.hudson.plugins.gerrit.trigger.gerritnotifier.helpers;

import com.sonyericsson.hudson.plugins.gerrit.trigger.gerritnotifier.model.BuildMemory;
import com.sonyericsson.hudson.plugins.gerrit.trigger.hudsontrigger.GerritTrigger;
import hudson.model.Result;
import hudson.model.Run;

import static com.sonyericsson.hudson.plugins.gerrit.trigger.utils.Logic.shouldSkip;

/**
 * Set of methods to handle a array of entries.
 */
public final class EntriesHelper {
    /**
     * Private constructor.
     */
    private EntriesHelper() {
    }

    /**
     * Returns the worst result from the list of entities.
     * If entity is null, it will be skipped
     * If project is null, or build is null, or build is still ongoing,
     * it will be considered as Result.FAILURE
     *
     * @param entries the array of entries.
     * @return the worst result.
     */
    public static Result getWorstResult(BuildMemory.MemoryImprint.Entry[] entries) {
        // First implementation of skipVote feature did not skip vote if all jobs were marked as "skip vote"
        boolean doNotSkipVotes = areAllBuildResultsSkipped(entries);

        Result worstResult = Result.SUCCESS;
        for (BuildMemory.MemoryImprint.Entry entry : entries) {
            if (entry != null) {
                Run build = entry.getBuild();
                if (build != null && entry.isBuildCompleted() && build.getResult() != null) {
                    GerritTrigger trigger = GerritTrigger.getTrigger(entry.getProject());
                    boolean respectVote = trigger != null && !shouldSkip(trigger.getSkipVote(), build.getResult());
                    if (doNotSkipVotes || respectVote) {
                        if (build.getResult().isWorseThan(worstResult)) {
                            worstResult = build.getResult();
                        }
                    }
                } else {
                    return Result.FAILURE;
                }
            }
        }
        return worstResult;
    }

    /**
     * If all entry's results are configured to be skipped.
     *
     * @param entries the array of entries.
     * @return true if so.
     * @see #getWorstResult(BuildMemory.MemoryImprint.Entry[])
     */
    private static boolean areAllBuildResultsSkipped(BuildMemory.MemoryImprint.Entry[] entries) {
        for (BuildMemory.MemoryImprint.Entry entry : entries) {
            if (entry == null) {
                continue;
            }
            Run build = entry.getBuild();
            if (build == null) {
                return false;
            } else if (!entry.isBuildCompleted()) {
                return false;
            }
            Result buildResult = build.getResult();
            GerritTrigger trigger = GerritTrigger.getTrigger(entry.getProject());
            if (!shouldSkip(trigger.getSkipVote(), buildResult)) {
                return false;
            }
        }
        return true;
    }
}
