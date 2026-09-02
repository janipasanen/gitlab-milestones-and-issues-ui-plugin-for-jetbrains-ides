package io.phinfotech.gitlab.models

import com.fasterxml.jackson.annotation.JsonProperty

data class GitLabProject(
    val id: Int,
    val name: String,
    val name_with_namespace: String,
    val path: String,
    val path_with_namespace: String,
    val description: String?,
    val web_url: String,
    val avatar_url: String?,
    val visibility: String,
    val owner: GitLabUser? = null,
    val namespace: GitLabNamespace? = null,
    val created_at: String?,
    val last_activity_at: String?,
    val star_count: Int? = null,
    val topics: List<String>? = null,
    val permissions: Permissions? = null
)

data class GitLabUser(
    val id: Int,
    val username: String,
    val name: String,
    val state: String,
    val avatar_url: String,
    val web_url: String
)

data class GitLabNamespace(
    val id: Int,
    val name: String,
    val path: String,
    val kind: String,
    val full_path: String,
    val web_url: String
)

data class Permissions(
    @JsonProperty("project_access") val projectAccess: ProjectAccess? = null,
    @JsonProperty("group_access") val groupAccess: GroupAccess? = null
)

data class ProjectAccess(
    val access_level: Int,
    val notification_level: Int? = null
)

data class GroupAccess(
    val access_level: Int,
    val notification_level: Int? = null
)

data class GitLabMilestone(
    val id: Int,
    val iid: Int,
    val project_id: Int,
    val title: String,
    val description: String?,
    val description_html: String?,
    val state: String,
    val state_count: Double,
    val total_time_spent: Double?,
    val hours_spent: Double?,
    val start_date: String?,
    val due_date: String?,
    val created_at: String?,
    val updated_at: String?,
    val expired: Boolean?,
    val urls: MilestoneUrls? = null
)

data class MilestoneUrls(
    @JsonProperty("issues_url") val issuesUrl: String
)

data class GitLabIssue(
    val id: Int,
    val iid: Int,
    val project_id: Int,
    val title: String,
    val description: String?,
    val description_html: String?,
    val state: String,
    val state_change_performed_by: GitLabUser? = null,
    val labels: List<String>? = null,
    val labels_data: List<LabelData>? = null,
    val _links: GitLabLinks? = null,
    val merged_by: GitLabUser? = null,
    val merged_by_data: GitLabUser? = null,
    val created_at: String?,
    val updated_at: String?,
    val closed_at: String?,
    val closed_by: GitLabUser? = null,
    val closed_by_data: GitLabUser? = null,
    val milestone: GitLabMilestone? = null,
    val milestone_data: GitLabMilestone? = null,
    val assignees: List<GitLabUser>? = null,
    val assignees_data: List<GitLabUser>? = null,
    val author: GitLabUser,
    val user_notes_count: Int = 0,
    val merge_requests_count: Int = 0,
    val severity: String?,
    val category: String?,
    val time_estimate: Int? = null,
    val total_time_spent: Int? = null,
    val human_time_estimate: String? = null,
    val human_total_time_spent: String? = null,
    val weight: Int? = null,
    val has_labels: Boolean? = null,
    val board_list: BoardList? = null,
    val conflict_conflict: List<String>? = null,
    val subscribe: Boolean? = null,
    val user_stati: List<String>? = null,
    val user_can_update: Boolean? = null,
    val user_can_close: Boolean? = null,
    val resource_label_events_url: String? = null,
    val resource_state_events_url: String? = null,
    val resource_milestone_events_url: String? = null,
    val resource_label_events_path: String? = null,
    val resource_milestone_events_path: String? = null,
    val confidential: Boolean? = null,
    val task_completion_count: Int? = null,
    val tasks: List<TaskItem>? = null,
    val discussion_locked: Boolean? = null,
    val ids: Ids? = null,
    val weight_calc_override: Boolean? = null,
    val is_visible_in_dashboard: Boolean? = null,
    val is_work_in_progress: Boolean? = null,
    val details: Details? = null,
    val iteration: Iteration? = null,
    val version_alignments: List<VersionAlignment>? = null,
    val resource_counterpart_events_path: String? = null,
    val resource_group_change_events_path: String? = null,
    val resource_mention_events_path: String? = null,
    val resource_iteration_events_path: String? = null,
    val resource_mention_events_url: String? = null,
    val web_url: String? = null
) {
    data class Note(
        val id: Int,
        val body: String,
        val createdAt: String?,
        val updatedAt: String?,
        val author: GitLabUser? = null
    )

    data class NoteRequest(
        val body: String
    )
}

data class LabelData(
    val id: Int? = null,
    val name: String? = null,
    val color: String? = null,
    val description: String? = null,
    val description_html: String? = null,
    val text_color: String? = null,
    val type: String? = null,
    val group_id: Int? = null
)

data class GitLabLinks(
    @JsonProperty("notes_url") val notesUrl: String? = null,
    @JsonProperty("project") val project: GitLabProject? = null,
    @JsonProperty("issues_url") val issuesUrl: String? = null,
    @JsonProperty("award_emoji_url_url") val awardEmojiUrl: String? = null
)

data class BoardList(
    val type: String? = null,
    val value: String? = null,
    val label: String? = null
)

data class Ids(
    val iid: Int? = null,
    val pid: Int? = null,
    val gid: Int? = null,
    val sid: Int? = null,
    val eid: Int? = null,
    val wid: Int? = null
)

data class Details(
    val created_at: String? = null,
    val updated_at: String? = null,
    val updated_by: GitLabUser? = null
)

data class Iteration(
    val id: Int? = null,
    val start_date: String? = null,
    val due_date: String? = null,
    val title: String? = null
)

data class VersionAlignment(
    val project_id: Int? = null,
    val merge_request_iid: Int? = null,
    val merge_request_url: String? = null,
    val state: String? = null
)

data class TaskItem(
    val id: Int? = null,
    val title: String? = null,
    val author: GitLabUser? = null,
    val body: String? = null,
    val state: String? = null,
    val state_count: Double? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val url: String? = null,
    val author_data: GitLabUser? = null
)
