package org.jenkins.plugins.qualitytrends.model;

import com.google.common.base.Objects;

/**
 * @author Emanuele Zattin
 */

public class Build {
    private int build_id;
    private int build_number;
    private String commit_sha1;

    public Build(int build_id, int build_number) {
        this.build_id = build_id;
        this.build_number = build_number;
        this.commit_sha1 = null;
    }

    public Build(int build_id, int build_number, String commit_sha1) {
        this.build_id = build_id;
        this.build_number = build_number;
        this.commit_sha1 = commit_sha1;
    }

    public int getBuild_id() {
        return build_id;
    }

    public void setBuild_id(int build_id) {
        this.build_id = build_id;
    }

    public int getBuild_number() {
        return build_number;
    }

    public void setBuild_number(int build_number) {
        this.build_number = build_number;
    }

    public String getCommit_sha1() {
        return commit_sha1;
    }

    public void setCommit_sha1(String commit_sha1) {
        this.commit_sha1 = commit_sha1;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("build_id", build_id)
                .add("build_number", build_number)
                .add("commit_sha1", commit_sha1)
                .toString();
    }
}
