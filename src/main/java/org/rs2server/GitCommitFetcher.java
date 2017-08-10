package org.rs2server;

import com.diffplug.common.base.Errors;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import javax.annotation.concurrent.Immutable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * A class with some functions used to interact with the bitbucket web api (version 2.0).
 *
 * @author Twelve
 */
public final class GitCommitFetcher {
/*
    private static final Gson GSON = new Gson();

    private static final String GIT_USERNAME = "Lost-Isle";
    private static final String GIT_PASSWORD = "GesfPles2015";

    private static final String AUTHENTICATION = Base64.getEncoder().encodeToString((GIT_USERNAME + ":" + GIT_PASSWORD).getBytes());

    private static final String REPO_SLUG = "lost-rsps/";
    private static final String REPO_OWNER = "atomicc/";

    private static final String API_ENDPOINT = "https://bitbucket.org/api/2.0/repositories/";
    private static final String COMMIT_ENDPOINT = API_ENDPOINT + REPO_OWNER + REPO_SLUG + "commits/";

    public static HttpURLConnection connect(String endpoint) {
        return Errors.log().getWithDefault(() -> {
            URLConnection connection = new URL(endpoint).openConnection();
            connection.setRequestProperty("Authorization", "Basic " + AUTHENTICATION);
            connection.setRequestProperty("Content-type", "Application/JSON");
            return (HttpURLConnection) connection;
        }, null);
    }

    public static List<GitCommit> getCommits() {
        return Errors.log().getWithDefault(() -> {
            HttpURLConnection endpoint = connect(COMMIT_ENDPOINT);
            BufferedReader reader = new BufferedReader(new InputStreamReader(endpoint.getInputStream()));
            String line = reader.readLine().replace("\\n", "");
            reader.close();
            endpoint.disconnect();
            return GSON.fromJson(line, GitContainer.class).commits;
        }, null);
    }

    public static List<GitCommit> getAnnounceCommits() {
        return Errors.log().getWithDefault(() -> getCommits().stream().filter(m -> m.getMessage().startsWith("[announce]")).collect(toList()), null);
    }

    public static GitCommit getHeadAnnounceCommit() {
        return gitHeadCommit(getAnnounceCommits());
    }

    public static GitCommit gitHeadCommit() {
        return gitHeadCommit(getCommits());
    }

    public static GitCommit gitHeadCommit(List<GitCommit> commits) {
        return commits.get(0);
    }

    @Immutable
    public final class GitContainer {
        @SerializedName("values")
        private final List<GitCommit> commits;

        public GitContainer(List<GitCommit> commits) {
            this.commits = commits;
        }

        public final List<GitCommit> getCommits() {
            return commits;
        }
    }

    @Immutable
    public final class GitProfile {

        private final String username;

        @SerializedName("display_name")
        private final String displayName;

        public GitProfile(String username, String displayName) {
            this.username = username;
            this.displayName = displayName;
        }

        public final String getDisplayName() {
            return displayName;
        }

        public final String getUsername() {
            return username;
        }
    }

    @Immutable
    public final class GitCommitAuthor {
        @SerializedName("user")
        private final GitProfile profile;

        @SerializedName("raw")
        private final String nameAndEmail;

        public GitCommitAuthor(GitProfile profile, String nameAndEmail) {
            this.profile = profile;
            this.nameAndEmail = nameAndEmail;
        }

        public final GitProfile getProfile() {
            return profile;
        }

        public final String getNameAndEmail() {
            return nameAndEmail;
        }
    }

    @Immutable
    public final class GitCommit {
        private final String message;
        private final GitCommitAuthor author;

        public GitCommit(String message, GitCommitAuthor author) {
            this.message = message;
            this.author = author;
        }

        public final String getMessage() {
            return message;
        }

        public final GitCommitAuthor getAuthor() {
            return author;
        }
    }
*/
}
