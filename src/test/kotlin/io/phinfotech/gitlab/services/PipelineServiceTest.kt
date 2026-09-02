package io.phinfotech.gitlab.services

import io.phinfotech.gitlab.models.GitLabPipeline
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.junit.Test
import org.junit.Assert.*

class PipelineServiceTest {

    private val objectMapper = ObjectMapper().registerModule(kotlinModule())

    @Test
    fun testPipelineDeserialization() {
        val json = """
        {
            "id": 12345,
            "iid": 1,
            "project_id": 42,
            "ref": "main",
            "sha": "abc123def456",
            "status": "success",
            "created_at": "2024-01-01T00:00:00Z",
            "updated_at": "2024-01-01T00:05:00Z",
            "started_at": "2024-01-01T00:01:00Z",
            "finished_at": "2024-01-01T00:05:00Z",
            "web_url": "https://gitlab.com/project/pipelines/1"
        }
        """.trimIndent()

        val pipeline = objectMapper.readValue(json, GitLabPipeline::class.java)
        
        assertEquals(12345, pipeline.id)
        assertEquals(1, pipeline.iid)
        assertEquals(42, pipeline.project_id)
        assertEquals("main", pipeline.ref)
        assertEquals("abc123def456", pipeline.sha)
        assertEquals("success", pipeline.status)
        assertEquals("https://gitlab.com/project/pipelines/1", pipeline.web_url)
    }

    @Test
    fun testPipelineDeserializationWithCommit() {
        val json = """
        {
            "id": 12345,
            "iid": 1,
            "project_id": 42,
            "ref": "feature/login",
            "sha": "def456abc123",
            "status": "failed",
            "created_at": "2024-01-01T00:00:00Z",
            "commit": {
                "id": "def456abc123",
                "short_id": "def456a",
                "title": "Add login feature",
                "message": "Add login feature",
                "author_name": "Jane Developer",
                "author_email": "jane@example.com",
                "committed_date": "2024-01-01T00:00:00Z"
            }
        }
        """.trimIndent()

        val pipeline = objectMapper.readValue(json, GitLabPipeline::class.java)
        
        assertNotNull(pipeline.commit)
        assertEquals("Add login feature", pipeline.commit?.message)
        assertEquals("Jane Developer", pipeline.commit?.author_name)
    }

    @Test
    fun testPipelineDeserializationWithRunner() {
        val json = """
        {
            "id": 12345,
            "iid": 1,
            "project_id": 42,
            "ref": "main",
            "sha": "abc123",
            "status": "running",
            "created_at": "2024-01-01T00:00:00Z",
            "runner": {
                "id": 567,
                "name": "shared-runner-abc123"
            }
        }
        """.trimIndent()

        val pipeline = objectMapper.readValue(json, GitLabPipeline::class.java)
        
        assertNotNull(pipeline.runner)
        assertEquals("shared-runner-abc123", pipeline.runner?.name)
    }

    @Test
    fun testPipelineAllStatuses() {
        val statuses = listOf("created", "pending", "running", "success", "failed", "canceled", "skipped", "manual", "scheduled")
        
        for (status in statuses) {
            val json = """
            {
                "id": 1,
                "iid": 1,
                "project_id": 1,
                "ref": "main",
                "sha": "abc123",
                "status": "$status",
                "created_at": "2024-01-01T00:00:00Z"
            }
            """.trimIndent()

            val pipeline = objectMapper.readValue(json, GitLabPipeline::class.java)
            assertEquals(status, pipeline.status)
        }
    }

    @Test
    fun testJobDeserialization() {
        val json = """
        {
            "id": 98765,
            "iid": 1,
            "project_id": 42,
            "pipeline_id": 12345,
            "status": "success",
            "name": "build",
            "stage": "build",
            "created_at": "2024-01-01T00:00:00Z",
            "started_at": "2024-01-01T00:01:00Z",
            "finished_at": "2024-01-01T00:05:00Z",
            "duration": 240.5,
            "queue_duration": 5.2,
            "user": {
                "id": 1,
                "username": "jane",
                "name": "Jane Developer",
                "state": "active",
                "avatar_url": "",
                "web_url": ""
            },
            "web_url": "https://gitlab.com/project/jobs/98765",
            "tag": false,
            "allow_failure": false
        }
        """.trimIndent()

        val job = objectMapper.readValue(json, GitLabPipelineService.GitLabPipelineJob::class.java)
        
        assertEquals(98765, job.id)
        assertEquals(1, job.iid)
        assertEquals(42, job.project_id)
        assertEquals(12345, job.pipeline_id)
        assertEquals("success", job.status)
        assertEquals("build", job.name)
        assertEquals("build", job.stage)
        val duration = job.duration!!
        val queueDuration = job.queue_duration!!
        assertEquals(240.5, duration, 0.01)
        assertEquals(5.2, queueDuration, 0.01)
        assertNotNull(job.user)
        assertEquals("jane", job.user?.username)
    }

    @Test
    fun testJobWithNullOptionalFields() {
        val json = """
        {
            "id": 98765,
            "iid": 1,
            "project_id": 42,
            "pipeline_id": 12345,
            "status": "created",
            "name": "test",
            "stage": "test",
            "created_at": "2024-01-01T00:00:00Z",
            "started_at": null,
            "finished_at": null,
            "duration": null,
            "queue_duration": null,
            "user": null,
            "web_url": null,
            "tag": null,
            "allow_failure": null
        }
        """.trimIndent()

        val job = objectMapper.readValue(json, GitLabPipelineService.GitLabPipelineJob::class.java)
        
        assertEquals("created", job.status)
        assertNull(job.started_at)
        assertNull(job.finished_at)
        assertNull(job.duration)
        assertNull(job.queue_duration)
        assertNull(job.user)
        assertNull(job.web_url)
        assertNull(job.tag)
        assertNull(job.allow_failure)
    }
}
