package io.phinfotech.gitlab.models

data class GitLabPipeline(
    val id: Int,
    val iid: Int,
    val project_id: Int,
    val ref: String,
    val sha: String,
    val status: String,
    val created_at: String?,
    val updated_at: String?,
    val started_at: String?,
    val finished_at: String?,
    val commit: GitLabCommit? = null,
    val web_url: String? = null,
    val runner: GitLabPipelineRunner? = null
)

data class GitLabCommit(
    val id: String,
    val short_id: String? = null,
    val title: String? = null,
    val message: String? = null,
    val author_name: String? = null,
    val author_email: String? = null,
    val committed_date: String? = null
)

data class GitLabPipelineRunner(
    val id: Int? = null,
    val name: String? = null
)
