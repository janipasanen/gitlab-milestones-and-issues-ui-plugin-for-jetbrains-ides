package io.phinfotech.gitlab.services

import io.phinfotech.gitlab.api.GitLabApiClient
import io.phinfotech.gitlab.models.GitLabPipeline
import io.phinfotech.gitlab.models.GitLabUser

class GitLabPipelineService(private val client: GitLabApiClient) {

    fun getPipelines(projectId: Int, status: String? = null, ref: String? = null, page: Int = 1): List<GitLabPipeline> {
        val params = mutableMapOf("page" to page.toString(), "per_page" to "100")
        status?.let { params["status"] = it }
        ref?.let { params["ref"] = it }

        val result = client.get(
            "/projects/$projectId/pipelines",
            params,
            List::class.java
        )
        return result.filterIsInstance<GitLabPipeline>()
    }

    fun getPipeline(projectId: Int, pipelineIid: Int): GitLabPipeline {
        return client.get(
            "/projects/$projectId/pipelines/$pipelineIid",
            GitLabPipeline::class.java
        )
    }

    fun stopPipeline(projectId: Int, pipelineIid: Int): GitLabPipeline {
        return client.post(
            "/projects/$projectId/pipelines/$pipelineIid/stop",
            emptyMap<String, String>(),
            GitLabPipeline::class.java
        )
    }

    fun retryPipeline(projectId: Int, pipelineIid: Int): GitLabPipeline {
        return client.post(
            "/projects/$projectId/pipelines/$pipelineIid/retry",
            emptyMap<String, String>(),
            GitLabPipeline::class.java
        )
    }

    fun cancelPipeline(projectId: Int, pipelineIid: Int): GitLabPipeline {
        return client.post(
            "/projects/$projectId/pipelines/$pipelineIid/cancel",
            emptyMap<String, String>(),
            GitLabPipeline::class.java
        )
    }

    fun getPipelineJobs(projectId: Int, pipelineIid: Int): List<GitLabPipelineJob> {
        val result = client.get(
            "/projects/$projectId/pipelines/$pipelineIid/jobs",
            mapOf("per_page" to "100"),
            List::class.java
        )
        return result.filterIsInstance<GitLabPipelineJob>()
    }

    fun cancelJob(projectId: Int, jobId: Int) {
        client.post(
            "/projects/$projectId/jobs/$jobId/cancel",
            emptyMap<String, String>(),
            GitLabPipelineJob::class.java
        )
    }

    data class GitLabPipelineJob(
        val id: Int,
        val iid: Int,
        val project_id: Int,
        val pipeline_id: Int,
        val status: String,
        val name: String,
        val stage: String,
        val created_at: String?,
        val started_at: String?,
        val finished_at: String?,
        val duration: Double?,
        val queue_duration: Double?,
        val user: GitLabUser? = null,
        val web_url: String? = null,
        val tag: Boolean? = null,
        val allow_failure: Boolean? = null
    )
}
