package org.rs2server.rs2.tickable.impl;

import com.google.inject.Inject;
import org.rs2server.GitCommitFetcher;
import org.rs2server.rs2.domain.service.api.AsyncExecutorService;
import org.rs2server.rs2.domain.service.api.WorldService;
import org.rs2server.rs2.tickable.Tickable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitCommitTick/* extends Tickable */{
/*
    private static final Logger logger = LoggerFactory.getLogger(GitCommitTick.class);
    private final AsyncExecutorService asyncExecutorService;
    private final WorldService worldService;

    @Inject
    public GitCommitTick(final AsyncExecutorService asyncExecutorService, final WorldService worldService) {
        super(600);
        this.asyncExecutorService = asyncExecutorService;
        this.worldService = worldService;
    }

    @Override
    public void execute() {
        asyncExecutorService.submit(() -> worldService.sendGlobalMessage("<img=1> Most Recent Update <img=1>",
                "<img=0> " + GitCommitFetcher.getHeadAnnounceCommit().getMessage().replace("[announce]", "") + " <img=0>"));
    }*/
}
